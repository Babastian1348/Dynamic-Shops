package com.babastian.dynamicshops.screens;

import com.babastian.dynamicshops.DynamicShops;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.inventory.MenuType;

public class ModScreenHandlers {

    // === SELL BLOCK HANDLERS ===
    public static final ExtendedMenuType<SellConfigScreenHandler, BlockPos> SELL_CONFIG_HANDLER;
    public static final ExtendedMenuType<SellScreenHandler, BlockPos> SELL_HANDLER;

    // === SHOP BLOCK HANDLERS ===
    public static final ExtendedMenuType<ShopConfigScreenHandler, BlockPos> SHOP_CONFIG_HANDLER;
    public static final ExtendedMenuType<ShopScreenHandler, BlockPos> SHOP_HANDLER;

    // === ATM HANDLER ===
    public static final ExtendedMenuType<AtmScreenHandler, BlockPos> ATM_HANDLER;

    static {
        // Sell Config Handler
        ResourceKey<MenuType<?>> configId = ResourceKey.create(
                BuiltInRegistries.MENU.key(),
                DynamicShops.id("sell_config_handler")
        );
        SELL_CONFIG_HANDLER = Registry.register(
                BuiltInRegistries.MENU,
                configId,
                new ExtendedMenuType<>(
                        (id, inv, pos) -> new SellConfigScreenHandler(id, inv, pos, inv.player.level()),
                        BlockPos.STREAM_CODEC
                )
        );

        // Sell Handler
        ResourceKey<MenuType<?>> sellId = ResourceKey.create(
                BuiltInRegistries.MENU.key(),
                DynamicShops.id("sell_handler")
        );
        SELL_HANDLER = Registry.register(
                BuiltInRegistries.MENU,
                sellId,
                new ExtendedMenuType<>(
                        (id, inv, pos) -> new SellScreenHandler(id, inv, pos, inv.player.level()),
                        BlockPos.STREAM_CODEC
                )
        );

        // Shop Config Handler
        ResourceKey<MenuType<?>> shopConfigId = ResourceKey.create(
                BuiltInRegistries.MENU.key(),
                DynamicShops.id("shop_config_handler")
        );
        SHOP_CONFIG_HANDLER = Registry.register(
                BuiltInRegistries.MENU,
                shopConfigId,
                new ExtendedMenuType<>(
                        (id, inv, pos) -> new ShopConfigScreenHandler(id, inv, pos, inv.player.level()),
                        BlockPos.STREAM_CODEC
                )
        );

        // Shop Handler
        ResourceKey<MenuType<?>> shopId = ResourceKey.create(
                BuiltInRegistries.MENU.key(),
                DynamicShops.id("shop_handler")
        );
        SHOP_HANDLER = Registry.register(
                BuiltInRegistries.MENU,
                shopId,
                new ExtendedMenuType<>(
                        (id, inv, pos) -> new ShopScreenHandler(id, inv, pos, inv.player.level()),
                        BlockPos.STREAM_CODEC
                )
        );

        // ATM Handler
        ResourceKey<MenuType<?>> atmId = ResourceKey.create(
                BuiltInRegistries.MENU.key(),
                DynamicShops.id("atm_handler")
        );
        ATM_HANDLER = Registry.register(
                BuiltInRegistries.MENU,
                atmId,
                new ExtendedMenuType<>(
                        (id, inv, pos) -> new AtmScreenHandler(id, inv, pos, inv.player.level()),
                        BlockPos.STREAM_CODEC
                )
        );
    }

    public static void register() {}
}