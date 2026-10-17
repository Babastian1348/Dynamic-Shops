package com.babastian.dynamicshops.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.AbstractFurnaceMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.babastian.dynamicshops.tasks.TaskTracker;

@Mixin(AbstractContainerMenu.class)
public class FurnaceTaskMixin {

    @Inject(method = "clicked", at = @At("HEAD"))
    private void onClicked(int slotIndex, int buttonNum, ContainerInput containerInput, Player player, CallbackInfo ci) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        if (slotIndex < 0) return;

        AbstractContainerMenu menu = (AbstractContainerMenu)(Object)this;

        if (slotIndex >= menu.slots.size()) return;

        Slot slot = menu.slots.get(slotIndex);

        // Verificar si es un horno y el slot es el de resultado (slot 2)
        if (menu instanceof AbstractFurnaceMenu && slotIndex == 2) {
            // Obtener el item que está en el slot ANTES del click
            ItemStack slotStack = slot.getItem();

            // Solo contar si el slot tenía un item válido (no aire)
            if (!slotStack.isEmpty()) {
                // Contar la cantidad de items que había en el slot
                int amount = slotStack.getCount();

                if (amount > 0) {
                    TaskTracker.incrementAndCheck(serverPlayer, TaskTracker.COOKING, amount, "task.cooking");
                }
            }
        }
    }
}