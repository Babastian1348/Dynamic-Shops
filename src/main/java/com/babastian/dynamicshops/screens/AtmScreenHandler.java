package com.babastian.dynamicshops.screens;

import com.babastian.dynamicshops.blocks.AtmBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class AtmScreenHandler extends AbstractContainerMenu {

    private final AtmBlockEntity blockEntity;
    private final BlockPos pos;

    public AtmScreenHandler(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        this(containerId, playerInventory, BlockPos.STREAM_CODEC.decode(buf), playerInventory.player.level());
    }

    public AtmScreenHandler(int containerId, Inventory playerInventory, BlockPos pos, Level level) {
        super(ModScreenHandlers.ATM_HANDLER, containerId);
        this.pos = pos;
        this.blockEntity = (AtmBlockEntity) level.getBlockEntity(pos);

        // Inventario principal del jugador (Slots 0 al 26)
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 9; x++) {
                this.addSlot(new Slot(playerInventory, x + y * 9 + 9, 8 + x * 18, 84 + y * 18));
            }
        }

        // Hotbar del jugador (Slots 27 al 35)
        for (int x = 0; x < 9; x++) {
            this.addSlot(new Slot(playerInventory, x, 8 + x * 18, 142));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack itemStack2 = slot.getItem();
            itemStack = itemStack2.copy();

            if (index < 27) {
                if (!this.moveItemStackTo(itemStack2, 27, 36, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index < 36) {
                if (!this.moveItemStackTo(itemStack2, 0, 27, false)) {
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
        return true;
    }

    public BlockPos getPos() {
        return pos;
    }

    public AtmBlockEntity getBlockEntity() {
        return blockEntity;
    }
}