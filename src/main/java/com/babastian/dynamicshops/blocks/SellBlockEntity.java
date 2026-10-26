package com.babastian.dynamicshops.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import com.babastian.dynamicshops.DynamicShops;
import com.babastian.dynamicshops.config.GlobalEconomyData;
import com.babastian.dynamicshops.config.ModConfig;
import com.babastian.dynamicshops.screens.SellConfigScreenHandler;
import com.babastian.dynamicshops.screens.SellScreenHandler;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import org.jetbrains.annotations.Nullable;

public class SellBlockEntity extends BlockEntity implements Container, ExtendedMenuProvider<BlockPos> {

    public static BlockEntityType<SellBlockEntity> TYPE;

    public SellBlockEntity(BlockPos pos, BlockState state) {
        super(TYPE, pos, state);
    }

    public static void registerBlockEntity() {
        ResourceKey<BlockEntityType<?>> id = ResourceKey.create(
                BuiltInRegistries.BLOCK_ENTITY_TYPE.key(),
                DynamicShops.id("sell_block_entity")
        );
        TYPE = Registry.register(
                BuiltInRegistries.BLOCK_ENTITY_TYPE,
                id,
                net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder.create(
                        SellBlockEntity::new,
                        ModBlocks.WHITE_SELL_BLOCK,
                        ModBlocks.RED_SELL_BLOCK,
                        ModBlocks.BLUE_SELL_BLOCK,
                        ModBlocks.GREEN_SELL_BLOCK,
                        ModBlocks.YELLOW_SELL_BLOCK,
                        ModBlocks.LIME_SELL_BLOCK,
                        ModBlocks.BLACK_SELL_BLOCK,
                        ModBlocks.GRAY_SELL_BLOCK,
                        ModBlocks.LIGHT_GRAY_SELL_BLOCK,
                        ModBlocks.BROWN_SELL_BLOCK,
                        ModBlocks.CYAN_SELL_BLOCK,
                        ModBlocks.LIGHT_BLUE_SELL_BLOCK,
                        ModBlocks.PURPLE_SELL_BLOCK,
                        ModBlocks.MAGENTA_SELL_BLOCK,
                        ModBlocks.ORANGE_SELL_BLOCK,
                        ModBlocks.PINK_SELL_BLOCK
                ).build()
        );
    }

    private ItemStack sellItem = ItemStack.EMPTY;
    private int price = 0;
    private int maxStock = 0;
    private boolean hasLimitedStock = false;
    private int soldCount = 0;
    private long lastResetCycle = -1;
    private int timeRemainingTicks = 0;
    private String customName = "";

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return sellItem.isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        return slot == 0 ? sellItem : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        if (slot == 0 && !sellItem.isEmpty()) {
            ItemStack result = sellItem.split(amount);
            if (sellItem.isEmpty()) {
                sellItem = ItemStack.EMPTY;
            }
            setChanged();
            return result;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        if (slot == 0 && !sellItem.isEmpty()) {
            ItemStack result = sellItem.copy();
            sellItem = ItemStack.EMPTY;
            return result;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (slot == 0) {
            if (stack.isEmpty() || stack.getCount() <= 0) {
                this.sellItem = ItemStack.EMPTY;
            } else {
                this.sellItem = stack.copy();
            }
            setChanged();
            if (level != null) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
            }
        }
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void clearContent() {
        sellItem = ItemStack.EMPTY;
        setChanged();
    }

    public ItemStack getSellItem() {
        return sellItem;
    }

    public void setSellItem(ItemStack item) {
        this.sellItem = item;
        setChanged();
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
        setChanged();
    }

    public int getMaxStock() {
        return maxStock;
    }

    public void setMaxStock(int maxStock) {
        this.maxStock = maxStock;
        setChanged();
    }

    public boolean hasLimitedStock() {
        return hasLimitedStock;
    }

    public void setHasLimitedStock(boolean hasLimitedStock) {
        this.hasLimitedStock = hasLimitedStock;
        setChanged();
    }

    public int getSoldCount() {
        return soldCount;
    }

    public void setSoldCount(int soldCount) {
        this.soldCount = soldCount;
        syncToClients();
    }

    public String getCustomName() {
        return customName;
    }

    public void setCustomName(String customName) {
        this.customName = customName != null ? customName : "";
        setChanged();
    }

    public int getTimeRemainingTicks() {
        return timeRemainingTicks;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        if (!sellItem.isEmpty() && sellItem.getItem() != net.minecraft.world.item.Items.AIR) {
            output.store("sell_item", ItemStack.CODEC, sellItem);
        }

        output.putInt("sell_price", price);
        output.putInt("max_stock", maxStock);
        output.putBoolean("has_limited_stock", hasLimitedStock);
        output.putInt("sold_count", soldCount);
        output.putInt("time_remaining_ticks", timeRemainingTicks);
        output.putString("custom_name", customName);
        super.saveAdditional(output);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);

        sellItem = input.read("sell_item", ItemStack.CODEC).orElse(ItemStack.EMPTY);

        if (sellItem.getItem() == net.minecraft.world.item.Items.AIR) {
            sellItem = ItemStack.EMPTY;
        }

        price = input.getIntOr("sell_price", 0);
        maxStock = input.getIntOr("max_stock", 0);
        hasLimitedStock = input.getBooleanOr("has_limited_stock", false);
        soldCount = input.getIntOr("sold_count", 0);
        timeRemainingTicks = input.getIntOr("time_remaining_ticks", 0);
        customName = input.getStringOr("custom_name", "");
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registryLookup) {
        CompoundTag nbt = new CompoundTag();

        ItemStack.CODEC.encodeStart(
                registryLookup.createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE),
                sellItem
        ).result().ifPresent(tag -> nbt.put("sell_item", tag));

        nbt.putInt("sell_price", price);
        nbt.putInt("max_stock", maxStock);
        nbt.putBoolean("has_limited_stock", hasLimitedStock);
        nbt.putInt("sold_count", soldCount);
        nbt.putInt("time_remaining_ticks", timeRemainingTicks);
        nbt.putString("custom_name", customName);

        return nbt;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("display.dynamicshops.sell_block");
    }

    @Override
    public BlockPos getScreenOpeningData(ServerPlayer player) {
        return this.worldPosition;
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        checkAndResetStock();

        boolean isCrouching = player.isCrouching();
        boolean isCreative = player.isCreative();
        boolean isOp = player instanceof ServerPlayer serverPlayer &&
                serverPlayer.permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_ADMIN);

        if (isCrouching && (isCreative || isOp)) {
            return new SellConfigScreenHandler(containerId, inventory, worldPosition, level);
        } else {
            return new SellScreenHandler(containerId, inventory, worldPosition, level);
        }
    }

    public void checkAndResetStock() {
        if (level == null || level.isClientSide() || !(level instanceof ServerLevel serverLevel)) {
            return;
        }

        if (!hasLimitedStock) {
            return;
        }

        int intervalHours = ModConfig.getInstance().getSellLimitResetIntervalHours();
        long intervalTicks = intervalHours * 1000L;

        long totalTime = (intervalHours <= 24) ? serverLevel.getOverworldClockTime() : level.getGameTime();
        boolean shouldReset = (totalTime % intervalTicks == 0);

        long currentResetCycle = totalTime / intervalTicks;
        boolean isNewCycle = GlobalEconomyData.getInstance(serverLevel).shouldResetLimitStock(currentResetCycle, intervalHours);

        if (shouldReset && isNewCycle) {
            soldCount = 0;
            setChanged();
            DynamicShops.LOGGER.info("[SellBlock] Stock reset at {} | Total time: {}, Interval: {} ticks",
                    worldPosition, totalTime, intervalTicks);
        }
    }

    // === MÉTODOS PARA TICK Y DISPLAY ===

    public void tick() {
        if (level == null || level.isClientSide()) {
            return;
        }

        if (!hasLimitedStock) {
            return;
        }

        int intervalHours = ModConfig.getInstance().getSellLimitResetIntervalHours();
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

        long currentResetCycle = totalTime / intervalTicks;

        if (currentResetCycle != lastResetCycle) {
            lastResetCycle = currentResetCycle;
            soldCount = 0;
            setChanged();
        }
    }

    public void updateTimer() {
        if (level == null || level.isClientSide()) {
            return;
        }

        if (!hasLimitedStock) {
            return;
        }

        int intervalHours = ModConfig.getInstance().getSellLimitResetIntervalHours();
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

        long timeInCycle = totalTime % intervalTicks;
        long remaining = intervalTicks - timeInCycle;

        if (remaining % 20 == 0) {
            setChanged();
        }
    }

    public void updateTimeRemaining() {
        if (level == null || level.isClientSide()) {
            return;
        }

        if (!hasLimitedStock) {
            timeRemainingTicks = 0;
            return;
        }

        int intervalHours = ModConfig.getInstance().getSellLimitResetIntervalHours();
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

        long timeInCycle = totalTime % intervalTicks;
        timeRemainingTicks = (int)(intervalTicks - timeInCycle);

        if (timeRemainingTicks % 20 == 0) {
            setChanged();
        }
    }

    public int getCurrentStock() {
        if (!hasLimitedStock) {
            return -1;
        }
        return Math.max(0, maxStock - soldCount);
    }

    // === MÉTODOS PARA EL DISPLAY ===

    public int getStockRemaining() {
        if (!hasLimitedStock) {
            return -1;
        }
        return maxStock - soldCount;
    }

    public long getTimeUntilReset() {
        if (level == null || level.isClientSide()) {
            return 0;
        }

        int intervalHours = ModConfig.getInstance().getSellLimitResetIntervalHours();
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

        long timeInCycle = totalTime % intervalTicks;
        return intervalTicks - timeInCycle;
    }

    public String getFormattedTimeUntilReset() {
        long totalSeconds = timeRemainingTicks / 20;

        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        } else if (minutes > 0) {
            return String.format("%02d:%02d", minutes, seconds);
        } else {
            return String.format("%d", seconds);
        }
    }

    public String getItemName() {
        if (!customName.isEmpty()) {
            return customName;
        }

        if (sellItem.isEmpty()) {
            return net.minecraft.network.chat.Component.translatable("display.dynamicshops.empty_sale").getString();
        }
        return sellItem.getHoverName().getString();
    }

    public int getNameColor() {
        if (sellItem.isEmpty() && customName.isEmpty()) {
            return 0xAA0000;
        }
        Rarity rarity = sellItem.getRarity();
        return switch (rarity) {
            case COMMON -> 0xFFFFFF;
            case UNCOMMON -> 0xFFFF55;
            case RARE -> 0x55FFFF;
            case EPIC -> 0xFF55FF;
        };
    }

    public void syncToClients() {
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }
}