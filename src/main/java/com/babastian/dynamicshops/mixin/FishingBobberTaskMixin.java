package com.babastian.dynamicshops.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.babastian.dynamicshops.tasks.TaskTracker;

@Mixin(FishingHook.class)
public class FishingBobberTaskMixin {

    @Shadow private boolean biting;

    @Inject(method = "retrieve", at = @At("HEAD"))
    private void onRetrieve(ItemStack rod, CallbackInfoReturnable<Integer> cir) {
        FishingHook hook = (FishingHook)(Object)this;

        if (hook.getPlayerOwner() instanceof ServerPlayer player) {
            // Verificar si el anzuelo está en estado de "mordida" (pez atrapado)
            if (biting) {
                TaskTracker.incrementAndCheck(player, TaskTracker.FISH, 1, "task.fish");
            }
        }
    }
}