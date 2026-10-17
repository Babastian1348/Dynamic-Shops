package com.babastian.dynamicshops.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.item.trading.MerchantOffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.babastian.dynamicshops.tasks.TaskTracker;

@Mixin(AbstractVillager.class)
public class TradeTaskMixin {

    @Inject(method = "notifyTrade", at = @At("HEAD"))
    private void onNotifyTrade(MerchantOffer offer, CallbackInfo ci) {
        AbstractVillager villager = (AbstractVillager)(Object)this;

        if (villager.getTradingPlayer() instanceof ServerPlayer serverPlayer) {
            TaskTracker.incrementAndCheck(serverPlayer, TaskTracker.TRADES, 1, "task.trades");
        }
    }
}