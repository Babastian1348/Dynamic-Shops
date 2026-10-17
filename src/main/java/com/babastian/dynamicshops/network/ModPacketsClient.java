package com.babastian.dynamicshops.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import com.babastian.dynamicshops.money.MoneyComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

public class ModPacketsClient {

    public static void register() {
        // Receiver de Dinero de Bolsillo
        ClientPlayNetworking.registerGlobalReceiver(
                MoneyUpdatePacket.TYPE,
                (packet, context) -> {
                    context.client().execute(() -> {
                        Minecraft minecraft = Minecraft.getInstance();
                        Player player = minecraft.player;

                        if (player != null) {
                            MoneyComponent.setBalance(player, packet.newBalance());
                        }
                    });
                }
        );

        // Receiver de Dinero del Banco
        ClientPlayNetworking.registerGlobalReceiver(
                BankUpdatePacket.TYPE,
                (packet, context) -> {
                    context.client().execute(() -> {
                        Minecraft minecraft = Minecraft.getInstance();
                        Player player = minecraft.player;

                        if (player != null) {
                            MoneyComponent.setBankBalance(player, packet.newBankBalance());
                        }
                    });
                }
        );
    }
}