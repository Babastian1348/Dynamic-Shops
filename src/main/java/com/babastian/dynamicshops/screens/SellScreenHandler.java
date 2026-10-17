package com.babastian.dynamicshops.screens;

import com.babastian.dynamicshops.DynamicShops;
import com.babastian.dynamicshops.blocks.SellBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class SellScreenHandler extends AbstractContainerMenu {

    private final SellBlockEntity blockEntity;
    private final Level level;
    private final BlockPos pos;

    // Constructor para cliente
    public SellScreenHandler(
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

    // Constructor para servidor
    public SellScreenHandler(
            int containerId,
            Inventory playerInventory,
            BlockPos pos,
            Level level
    ) {
        super(ModScreenHandlers.SELL_HANDLER, containerId);
        this.level = level;
        this.pos = pos;

        this.blockEntity = (SellBlockEntity) level.getBlockEntity(pos);

        if (this.blockEntity == null) {
            DynamicShops.LOGGER.error("SellBlockEntity is NULL at {}", pos);
            throw new IllegalStateException("SellBlockEntity is null at " + pos);
        }

        // Slot 0: Item de venta (fuera de pantalla, solo para sincronización)
        this.addSlot(new Slot(blockEntity, 0, -1000, -1000) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;  // No se puede modificar en modo venta
            }

            @Override
            public boolean mayPickup(Player player) {
                return false;  // No se puede sacar
            }
        });

        // Inventario del jugador
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

        // Hotbar
        for (int x = 0; x < 9; x++) {
            this.addSlot(
                    new Slot(playerInventory, x, 8 + x * 18, 142)
            );
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
                // Del slot de venta al inventario (no debería pasar)
                if (!this.moveItemStackTo(itemStack2, 1, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // Del inventario al slot de venta (no debería pasar)
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

    public SellBlockEntity getBlockEntity() {
        return blockEntity;
    }

    public BlockPos getPos() {
        return pos;
    }
}