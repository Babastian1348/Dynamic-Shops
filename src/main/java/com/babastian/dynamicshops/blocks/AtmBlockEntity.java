package com.babastian.dynamicshops.blocks;

import com.babastian.dynamicshops.DynamicShops;
import com.babastian.dynamicshops.config.ClockConfig;
import com.babastian.dynamicshops.config.GlobalBankClock;
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
import net.minecraft.sounds.SoundEvents;
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

    // === LÓGICA DE TICK E INTERESES ===

    public void tick() {
        if (level == null || level.isClientSide()) {
            return;
        }

        checkAndApplyInterest();
    }

    public void checkAndApplyInterest() {
        if (level == null || level.isClientSide()) {
            return;
        }

        ClockConfig config = ClockConfig.getInstance();
        if (!config.isBankInterestEnabled()) {
            return;
        }

        int intervalHours = config.getBankInterestIntervalHours();
        if (intervalHours <= 0) {
            return;
        }

        long intervalTicks = intervalHours * 1000L;
        long totalTime;

        if (intervalHours <= 24) {
            if (level instanceof ServerLevel serverLevel) {
                totalTime = serverLevel.getOverworldClockTime();
            } else {
                totalTime = level.getGameTime();
            }
        } else {
            totalTime = level.getGameTime();
        }

        long currentCycle = totalTime / intervalTicks;

        if (GlobalBankClock.getInstance().shouldApplyInterest(currentCycle, intervalHours)) {
            applyInterestToOnlinePlayers();
            level.playSound(
                    null,
                    worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5,
                    ModSounds.INTEREST,
                    SoundSource.BLOCKS,
                    1.0F,
                    1.0F
            );

            DynamicShops.LOGGER.info("[AtmBlock] Tiered interest applied successfully for cycle {}", currentCycle);
        }
    }

    private void applyInterestToOnlinePlayers() {
        if (level == null || level.getServer() == null) {
            return;
        }

        ClockConfig config = ClockConfig.getInstance();

        for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
            int bankBalance = MoneyComponent.getBankBalance(player);

            if (bankBalance > 0 && bankBalance < MoneyComponent.MAX_BALANCE) {
                double ratePercent = getInterestRateForBalance(bankBalance, config);

                if (ratePercent > 0.0) {
                    int interest = (int) Math.floor(bankBalance * (ratePercent / 100.0));

                    if (interest > 0) {
                        MoneyComponent.addBankMoney(player, interest);
                    }
                }
            }
        }
    }

    private double getInterestRateForBalance(int balance, ClockConfig config) {
        for (ClockConfig.InterestTier tier : config.getBankInterestTiers()) {
            if (balance <= tier.maxBalance) {
                return tier.ratePercent;
            }
        }
        // Si supera todos los tramos configurados, devuelve 0% por defecto
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
        checkAndApplyInterest();
        return new AtmScreenHandler(containerId, inventory, worldPosition, level);
    }
}