package com.babastian.dynamicshops;

import com.babastian.dynamicshops.blocks.ModBlocks;
import com.babastian.dynamicshops.items.ModItems;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class ModCreativeTabs {

    public static CreativeModeTab DYNAMIC_SHOPS_TAB;

    public static void register() {
        DYNAMIC_SHOPS_TAB = Registry.register(
                BuiltInRegistries.CREATIVE_MODE_TAB,
                DynamicShops.id("dynamic_shops_tab"),
                CreativeModeTab.builder(
                                CreativeModeTab.Row.TOP,
                                0
                        )
                        .title(Component.translatable("creativetab.dynamic_shops"))
                        .icon(() -> new ItemStack(ModItems.GOLD_COIN))
                        .displayItems((params, output) -> {
                            // --- BLOQUES DE VENTA (SELL BLOCKS) ---
                            output.accept(ModBlocks.WHITE_SELL_BLOCK);
                            output.accept(ModBlocks.LIGHT_GRAY_SELL_BLOCK);
                            output.accept(ModBlocks.GRAY_SELL_BLOCK);
                            output.accept(ModBlocks.BLACK_SELL_BLOCK);
                            output.accept(ModBlocks.BROWN_SELL_BLOCK);
                            output.accept(ModBlocks.RED_SELL_BLOCK);
                            output.accept(ModBlocks.ORANGE_SELL_BLOCK);
                            output.accept(ModBlocks.YELLOW_SELL_BLOCK);
                            output.accept(ModBlocks.LIME_SELL_BLOCK);
                            output.accept(ModBlocks.GREEN_SELL_BLOCK);
                            output.accept(ModBlocks.CYAN_SELL_BLOCK);
                            output.accept(ModBlocks.LIGHT_BLUE_SELL_BLOCK);
                            output.accept(ModBlocks.BLUE_SELL_BLOCK);
                            output.accept(ModBlocks.PURPLE_SELL_BLOCK);
                            output.accept(ModBlocks.MAGENTA_SELL_BLOCK);
                            output.accept(ModBlocks.PINK_SELL_BLOCK);

                            // --- BLOQUES DE COMPRA (SHOP BLOCKS) ---
                            output.accept(ModBlocks.WHITE_SHOP_BLOCK);
                            output.accept(ModBlocks.LIGHT_GRAY_SHOP_BLOCK);
                            output.accept(ModBlocks.GRAY_SHOP_BLOCK);
                            output.accept(ModBlocks.BLACK_SHOP_BLOCK);
                            output.accept(ModBlocks.BROWN_SHOP_BLOCK);
                            output.accept(ModBlocks.RED_SHOP_BLOCK);
                            output.accept(ModBlocks.ORANGE_SHOP_BLOCK);
                            output.accept(ModBlocks.YELLOW_SHOP_BLOCK);
                            output.accept(ModBlocks.LIME_SHOP_BLOCK);
                            output.accept(ModBlocks.GREEN_SHOP_BLOCK);
                            output.accept(ModBlocks.CYAN_SHOP_BLOCK);
                            output.accept(ModBlocks.LIGHT_BLUE_SHOP_BLOCK);
                            output.accept(ModBlocks.BLUE_SHOP_BLOCK);
                            output.accept(ModBlocks.PURPLE_SHOP_BLOCK);
                            output.accept(ModBlocks.MAGENTA_SHOP_BLOCK);
                            output.accept(ModBlocks.PINK_SHOP_BLOCK);

                            // --- CAJEROS AUTOMÁTICOS ---
                            output.accept(ModBlocks.WHITE_ATM);
                            output.accept(ModBlocks.BLACK_ATM);

                            // --- MONEDAS Y BILLETES ---
                            output.accept(ModItems.COPPER_COIN);
                            output.accept(ModItems.COPPER_BILL);
                            output.accept(ModItems.IRON_COIN);
                            output.accept(ModItems.IRON_BILL);
                            output.accept(ModItems.GOLD_COIN);
                            output.accept(ModItems.GOLD_BILL);
                            output.accept(ModItems.EMERALD_COIN);
                            output.accept(ModItems.EMERALD_BILL);
                            output.accept(ModItems.DIAMOND_COIN);
                            output.accept(ModItems.DIAMOND_BILL);
                            output.accept(ModItems.NETHERITE_BILL);
                            output.accept(ModItems.VOUCHER);
                            output.accept(ModItems.WALLET);
                        })
                        .build()
        );
    }
}