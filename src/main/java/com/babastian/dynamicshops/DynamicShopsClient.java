package com.babastian.dynamicshops;

import com.babastian.dynamicshops.blocks.SellBlockEntity;
import com.babastian.dynamicshops.blocks.SellBlockEntityRenderer;
import com.babastian.dynamicshops.blocks.ShopBlockEntity;
import com.babastian.dynamicshops.blocks.ShopBlockEntityRenderer;
import com.babastian.dynamicshops.network.BankUpdatePacket;
import com.babastian.dynamicshops.network.ModPacketsClient;
import com.babastian.dynamicshops.network.MoneyUpdatePacket;
import com.babastian.dynamicshops.screens.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;

public class DynamicShopsClient implements ClientModInitializer {

    // Saldo sincronizado en el CLIENTE para leer desde las pantallas (Screen)
    public static int clientBankBalance = 0;
    public static int clientMoneyBalance = 100;

    @Override
    public void onInitializeClient() {
        // Registro de pantallas
        MenuScreens.register(ModScreenHandlers.SELL_CONFIG_HANDLER, SellConfigScreen::new);
        MenuScreens.register(ModScreenHandlers.SELL_HANDLER, SellScreen::new);
        MenuScreens.register(ModScreenHandlers.SHOP_CONFIG_HANDLER, ShopConfigScreen::new);
        MenuScreens.register(ModScreenHandlers.SHOP_HANDLER, ShopScreen::new);
        MenuScreens.register(ModScreenHandlers.ATM_HANDLER, AtmScreen::new);

        // Registro de renderers de bloques
        BlockEntityRenderers.register(SellBlockEntity.TYPE, SellBlockEntityRenderer::new);
        BlockEntityRenderers.register(ShopBlockEntity.TYPE, ShopBlockEntityRenderer::new);

        // Registro de paquetes de red del cliente
        ModPacketsClient.register();

        // Receptor de sincronización de saldo del BANCO
        ClientPlayNetworking.registerGlobalReceiver(
                BankUpdatePacket.TYPE,
                (payload, context) -> {
                    context.client().execute(() -> {
                        clientBankBalance = payload.newBankBalance();
                    });
                }
        );

        // Receptor de sincronización de saldo de la CARTERA / BOLSILLO
        ClientPlayNetworking.registerGlobalReceiver(
                MoneyUpdatePacket.TYPE,
                (payload, context) -> {
                    context.client().execute(() -> {
                        clientMoneyBalance = payload.newBalance();
                    });
                }
        );
    }
}