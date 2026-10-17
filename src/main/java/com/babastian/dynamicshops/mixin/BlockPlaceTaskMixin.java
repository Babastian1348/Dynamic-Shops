package com.babastian.dynamicshops.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.babastian.dynamicshops.tasks.TaskTracker;

@Mixin(BlockItem.class)
public class BlockPlaceTaskMixin {

    @Inject(method = "place", at = @At("RETURN"))
    private void onPlace(BlockPlaceContext context, CallbackInfoReturnable<InteractionResult> cir) {
        if (cir.getReturnValue() != InteractionResult.SUCCESS) return;
        if (context.getLevel().isClientSide()) return;

        // Obtener el jugador
        if (context.getPlayer() instanceof ServerPlayer player) {
            // Se colocó un bloque exitosamente
            TaskTracker.incrementAndCheck(player, TaskTracker.BLOCKS_PLACED, 1, "task.blocks_placed");
        }
    }
}