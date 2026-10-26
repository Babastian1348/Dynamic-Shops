package com.babastian.dynamicshops.blocks;

import com.babastian.dynamicshops.DynamicShops;
import com.babastian.dynamicshops.config.GlobalEconomyData;
import com.babastian.dynamicshops.config.ModConfig;
import com.babastian.dynamicshops.money.MoneyComponent;
import com.babastian.dynamicshops.screens.AtmScreenHandler;
import com.babastian.dynamicshops.sounds.ModSounds;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

public class AtmBlockEntity extends BlockEntity implements ExtendedMenuProvider<BlockPos> {

    public static BlockEntityType<AtmBlockEntity> TYPE;

    private int storedMoney = 0;
    private String customName = "";

    public AtmBlockEntity(BlockPos pos, BlockState state) {
        super(TYPE, pos, state);
    }

    public static void registerBlockEntity() {
        ResourceKey<BlockEntityType<?>> id = ResourceKey.create(
                BuiltInRegistries.BLOCK_ENTITY_TYPE.key(),
                DynamicShops.id("atm_block_entity")
        );
        TYPE = Registry.register(
                BuiltInRegistries.BLOCK_ENTITY_TYPE,
                id,
                net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder.create(
                        AtmBlockEntity::new,
                        ModBlocks.WHITE_ATM,
                        ModBlocks.BLACK_ATM
                ).build()
        );
    }

    // === LÓGICA DE TICK E IMPUESTOS ===

    public void tick() {
        if (level == null || level.isClientSide()) {
            return;
        }

        checkAndApplyTaxes();
    }

    public void checkAndApplyTaxes() {
        if (level == null || level.isClientSide() || !(level instanceof ServerLevel serverLevel)) {
            return;
        }

        ModConfig config = ModConfig.getInstance();
        if (!config.isBankTaxEnabled()) {
            return;
        }

        int intervalHours = config.getBankTaxIntervalHours();
        if (intervalHours <= 0) {
            return;
        }

        long intervalTicks = intervalHours * 1000L;
        long totalTime;

        if (intervalHours <= 24) {
            totalTime = serverLevel.getOverworldClockTime();
        } else {
            totalTime = level.getGameTime();
        }

        long currentCycle = totalTime / intervalTicks;

        // Comprobación de ciclo usando el nuevo GlobalEconomyData unificado
        if (GlobalEconomyData.getInstance(serverLevel).shouldApplyBankTaxes(currentCycle, intervalHours)) {
            applyTaxesToOnlinePlayers(serverLevel);
            level.playSound(
                    null,
                    worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5,
                    ModSounds.INTEREST,
                    SoundSource.BLOCKS,
                    1.0F,
                    0.5F // Pitch más bajo para indicar un cobro/gasto fiscal
            );

            DynamicShops.LOGGER.info("[AtmBlock] Bank taxes applied successfully for cycle {}", currentCycle);
        }
    }

    private void applyTaxesToOnlinePlayers(ServerLevel serverLevel) {
        if (serverLevel.getServer() == null) {
            return;
        }

        ModConfig config = ModConfig.getInstance();

        for (ServerPlayer player : serverLevel.getServer().getPlayerList().getPlayers()) {
            int bankBalance = MoneyComponent.getBankBalance(player);

            // Solo se cobran impuestos si hay saldo positivo en el banco
            if (bankBalance > 0) {
                double taxPercent = getTaxRateForBalance(bankBalance, config);

                if (taxPercent > 0.0) {
                    int taxAmount = (int) Math.ceil(bankBalance * (taxPercent / 100.0));

                    if (taxAmount > 0) {
                        MoneyComponent.removeBankMoney(player, taxAmount);
                    }
                }
            }
        }
    }

    private double getTaxRateForBalance(int balance, ModConfig config) {
        for (ModConfig.TaxTier tier : config.getBankTaxTiers()) {
            if (balance <= tier.maxBalance) {
                return tier.taxPercent;
            }
        }
        return 0.0;
    }

    // === GETTERS & SETTERS ===

    public int getStoredMoney() {
        return storedMoney;
    }

    public void setStoredMoney(int storedMoney) {
        this.storedMoney = storedMoney;
        setChanged();
    }

    public String getCustomName() {
        return customName;
    }

    public void setCustomName(String customName) {
        this.customName = customName != null ? customName : "";
        setChanged();
    }

    // === NBT & SINCRONIZACIÓN ===

    @Override
    protected void saveAdditional(ValueOutput output) {
        output.putInt("stored_money", storedMoney);
        output.putString("custom_name", customName);
        super.saveAdditional(output);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        storedMoney = input.getIntOr("stored_money", 0);
        customName = input.getStringOr("custom_name", "");
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registryLookup) {
        CompoundTag nbt = new CompoundTag();
        nbt.putInt("stored_money", storedMoney);
        nbt.putString("custom_name", customName);
        return nbt;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    public void syncToClients() {
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    // === MENÚS Y GUI ===

    @Override
    public Component getDisplayName() {
        if (!customName.isEmpty()) {
            return Component.literal(customName);
        }
        return Component.translatable("display.dynamicshops.atm_block");
    }

    @Override
    public BlockPos getScreenOpeningData(ServerPlayer player) {
        return this.worldPosition;
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        checkAndApplyTaxes();
        return new AtmScreenHandler(containerId, inventory, worldPosition, level);
    }
}