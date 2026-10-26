package com.babastian.dynamicshops.mixin;

import com.babastian.dynamicshops.items.WalletItem;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractContainerScreen.class)
public class WalletBundleContainerMixin {

    @Shadow
    protected Slot hoveredSlot;

    @Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true)
    private void onMouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY, CallbackInfoReturnable<Boolean> cir) {
        if (this.hoveredSlot != null && !this.hoveredSlot.getItem().isEmpty()) {
            ItemStack stack = this.hoveredSlot.getItem();

            if (stack.getItem() instanceof WalletItem) {
                if (scrollY != 0.0) {
                    boolean scrollDown = scrollY < 0;
                    WalletItem.cycleSelectedItem(stack, scrollDown);
                    cir.setReturnValue(true);
                }
            }
        }
    }
}