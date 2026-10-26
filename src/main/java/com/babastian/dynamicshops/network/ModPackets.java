package com.babastian.dynamicshops.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.level.ServerPlayer;
import com.babastian.dynamicshops.money.MoneyComponent;

public class ModPackets {

    public static void register() {

        PayloadTypeRegistry.serverboundPlay().register(
                SellConfigSavePacket.TYPE,
                SellConfigSavePacket.CODEC
        );

        PayloadTypeRegistry.serverboundPlay().register(
                SellItemPacket.TYPE,
                SellItemPacket.CODEC
        );

        // === SHOP BLOCK PACKETS ===
        PayloadTypeRegistry.serverboundPlay().register(
                ShopConfigSavePacket.TYPE,
                ShopConfigSavePacket.CODEC
        );

        PayloadTypeRegistry.serverboundPlay().register(
                BuyItemPacket.TYPE,
                BuyItemPacket.CODEC
        );

        PayloadTypeRegistry.serverboundPlay().register(
                AtmDepositPacket.TYPE,
                AtmDepositPacket.CODEC
        );

        PayloadTypeRegistry.serverboundPlay().register(
                AtmWithdrawPacket.TYPE,
                AtmWithdrawPacket.CODEC
        );

        PayloadTypeRegistry.serverboundPlay().register(
                AtmWithdrawPhysicalPacket.TYPE,
                AtmWithdrawPhysicalPacket.CODEC
        );

        PayloadTypeRegistry.clientboundPlay().register(
                MoneyUpdatePacket.TYPE,
                MoneyUpdatePacket.CODEC
        );

        PayloadTypeRegistry.clientboundPlay().register(
                BankUpdatePacket.TYPE,
                BankUpdatePacket.CODEC
        );

        PayloadTypeRegistry.serverboundPlay().register(
                VoucherSavePacket.TYPE,
                VoucherSavePacket.CODEC
        );


        ServerPlayNetworking.registerGlobalReceiver(
                SellConfigSavePacket.TYPE,
                (packet, context) -> {
                    ServerPlayer player = context.player();
                    SellConfigSavePacket.handle(player, packet);
                }
        );

        ServerPlayNetworking.registerGlobalReceiver(
                SellItemPacket.TYPE,
                (packet, context) -> {
                    ServerPlayer player = context.player();
                    context.server().execute(() -> SellItemPacket.handle(player, packet));
                }
        );

        ServerPlayNetworking.registerGlobalReceiver(
                ShopConfigSavePacket.TYPE,
                (packet, context) -> {
                    ServerPlayer player = context.player();
                    ShopConfigSavePacket.handle(player, packet);
                }
        );

        ServerPlayNetworking.registerGlobalReceiver(
                BuyItemPacket.TYPE,
                (packet, context) -> {
                    ServerPlayer player = context.player();
                    context.server().execute(() -> BuyItemPacket.handle(player, packet));
                }
        );

        ServerPlayNetworking.registerGlobalReceiver(
                AtmDepositPacket.TYPE,
                (packet, context) -> {
                    ServerPlayer player = context.player();
                    context.server().execute(() -> AtmDepositPacket.handle(player, packet));
                }
        );

        ServerPlayNetworking.registerGlobalReceiver(
                AtmWithdrawPacket.TYPE,
                (packet, context) -> {
                    ServerPlayer player = context.player();
                    context.server().execute(() -> AtmWithdrawPacket.handle(player, packet));
                }
        );

        ServerPlayNetworking.registerGlobalReceiver(
                AtmWithdrawPhysicalPacket.TYPE,
                (packet, context) -> {
                    ServerPlayer player = context.player();
                    context.server().execute(() -> AtmWithdrawPhysicalPacket.handle(player, packet));
                }
        );

        ServerPlayNetworking.registerGlobalReceiver(
                VoucherSavePacket.TYPE,
                (packet, context) -> {
                    ServerPlayer player = context.player();
                    context.server().execute(() -> VoucherSavePacket.handle(player, packet));
                }
        );

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayer player = handler.getPlayer();
            int balance = MoneyComponent.getBalance(player);
            int bankBalance = MoneyComponent.getBankBalance(player);
            sender.sendPacket(new MoneyUpdatePacket(balance));
            sender.sendPacket(new BankUpdatePacket(bankBalance));
        });
    }
}