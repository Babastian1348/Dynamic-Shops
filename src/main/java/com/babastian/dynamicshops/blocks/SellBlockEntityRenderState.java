package com.babastian.dynamicshops.blocks;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class SellBlockEntityRenderState extends BlockEntityRenderState {
    private ItemStack sellItem = ItemStack.EMPTY;
    public BlockState blockState;
    public Level level;
    public final ItemStackRenderState itemRenderState = new ItemStackRenderState();
    public float partialTicks;
    public String itemName = "";
    public int itemPrice = 0;
    public int stockRemaining = -1;
    public int currentStock = -1;
    public boolean hasStock = true;
    public String timeUntilReset = "";
    public boolean hasLimitedStock = false;
    public int nameColor = 0xFFFFFFFF;
    public float displayAlpha = 0.0f;

    public ItemStack getSellItem() {
        return sellItem;
    }

    public void setSellItem(ItemStack sellItem) {
        this.sellItem = sellItem;
    }

    public void reset() {
        itemName = "";
        itemPrice = 0;
        stockRemaining = -1;
        currentStock = -1;
        hasStock = true;
        timeUntilReset = "";
        hasLimitedStock = false;
        nameColor = 0xFFFFFFFF;
        // NO resetear displayAlpha - se maneja en el renderer con mapas
    }
}