package com.babastian.dynamicshops.screens;

import com.babastian.dynamicshops.DynamicShops;
import com.babastian.dynamicshops.blocks.ShopBlockEntity;
import com.babastian.dynamicshops.money.MoneyComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ShopScreenHandler extends AbstractContainerMenu {

    private final ShopBlockEntity blockEntity;
    private final Level level;
    private final BlockPos pos;

    public ShopScreenHandler(
            int containerId,
            Inventory playerInventory,
            RegistryFriendlyByteBuf buf
    ) {
        this(
                containerId,
                playerInventory,
                BlockPos.STREAM_CODEC.decode(buf),
                playerInventory.player.level()
        );
    }

    public ShopScreenHandler(
            int containerId,
            Inventory playerInventory,
            BlockPos pos,
            Level level
    ) {
        super(ModScreenHandlers.SHOP_HANDLER, containerId);
        this.level = level;
        this.pos = pos;

        this.blockEntity = (ShopBlockEntity) level.getBlockEntity(pos);

        if (this.blockEntity == null) {
            DynamicShops.LOGGER.error("ShopBlockEntity is NULL at {}", pos);
            throw new IllegalStateException("ShopBlockEntity is null at " + pos);
        }

        this.addSlot(new Slot(blockEntity, 0, -1000, -1000) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }

            @Override
            public boolean mayPickup(Player player) {
                return false;
            }
        });

        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 9; x++) {
                this.addSlot(
                        new Slot(
                                playerInventory,
                                x + y * 9 + 9,
                                8 + x * 18,
                                84 + y * 18
                        )
                );
            }
        }

        for (int x = 0; x < 9; x++) {
            this.addSlot(
                    new Slot(playerInventory, x, 8 + x * 18, 142)
            );
        }
    }

    public void executePurchase(ServerPlayer player) {
        if (this.level.isClientSide()) return;
        if (this.blockEntity == null || this.blockEntity.getBuyItem().isEmpty()) return;

        int price = this.blockEntity.getPrice();

        if (MoneyComponent.hasEnough(player, price)) {
            MoneyComponent.removeMoney(player, price);

            ItemStack boughtStack = this.blockEntity.getBuyItem().copy();
            player.getInventory().placeItemBackInInventory(boughtStack);

            if (this.blockEntity.hasLimitedStock()) {
                this.blockEntity.setSoldCount(this.blockEntity.getSoldCount() + 1);
            }

            // Registrar venta para el colaborador
            this.blockEntity.recordSale(player);
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack itemStack2 = slot.getItem();
            itemStack = itemStack2.copy();

            if (index == 0) {
                if (!this.moveItemStackTo(itemStack2, 1, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (!this.moveItemStackTo(itemStack2, 0, 1, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (itemStack2.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return itemStack;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.blockEntity != null && player.distanceToSqr(
                this.blockEntity.getBlockPos().getX() + 0.5,
                this.blockEntity.getBlockPos().getY() + 0.5,
                this.blockEntity.getBlockPos().getZ() + 0.5
        ) <= 64.0;
    }

    public ShopBlockEntity getBlockEntity() {
        return blockEntity;
    }

    public BlockPos getPos() {
        return pos;
    }
}