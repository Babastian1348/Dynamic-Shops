package com.babastian.dynamicshops.blocks;

import com.babastian.dynamicshops.DynamicShops;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;

public class ModBlocks {

    // === ATM BLOCKS ===
    public static final Block WHITE_ATM = Registry.register(
            BuiltInRegistries.BLOCK,
            ResourceKey.create(BuiltInRegistries.BLOCK.key(), DynamicShops.id("white_atm")),
            new AtmBlock(Block.Properties.of()
                    .setId(ResourceKey.create(BuiltInRegistries.BLOCK.key(), DynamicShops.id("white_atm")))
                    .noOcclusion()) // Importante para que no haga transparentes los bloques de al lado
    );

    public static final Block BLACK_ATM = Registry.register(
            BuiltInRegistries.BLOCK,
            ResourceKey.create(BuiltInRegistries.BLOCK.key(), DynamicShops.id("black_atm")),
            new AtmBlock(Block.Properties.ofFullCopy(WHITE_ATM)
                    .setId(ResourceKey.create(BuiltInRegistries.BLOCK.key(), DynamicShops.id("black_atm")))
                    .noOcclusion())
    );

    // === SELL BLOCKS ===
    public static final Block WHITE_SELL_BLOCK = Registry.register(
            BuiltInRegistries.BLOCK,
            ResourceKey.create(BuiltInRegistries.BLOCK.key(), DynamicShops.id("white_sell_block")),
            new SellBlock(Block.Properties.of()
                    .setId(ResourceKey.create(BuiltInRegistries.BLOCK.key(), DynamicShops.id("white_sell_block"))))
    );

    public static final Block ORANGE_SELL_BLOCK = Registry.register(
            BuiltInRegistries.BLOCK,
            ResourceKey.create(BuiltInRegistries.BLOCK.key(), DynamicShops.id("orange_sell_block")),
            new SellBlock(Block.Properties.ofFullCopy(WHITE_SELL_BLOCK)
                    .setId(ResourceKey.create(BuiltInRegistries.BLOCK.key(), DynamicShops.id("orange_sell_block"))))
    );

    public static final Block MAGENTA_SELL_BLOCK = Registry.register(
            BuiltInRegistries.BLOCK,
            ResourceKey.create(BuiltInRegistries.BLOCK.key(), DynamicShops.id("magenta_sell_block")),
            new SellBlock(Block.Properties.ofFullCopy(WHITE_SELL_BLOCK)
                    .setId(ResourceKey.create(BuiltInRegistries.BLOCK.key(), DynamicShops.id("magenta_sell_block"))))
    );

    public static final Block LIGHT_BLUE_SELL_BLOCK = Registry.register(
            BuiltInRegistries.BLOCK,
            ResourceKey.create(BuiltInRegistries.BLOCK.key(), DynamicShops.id("light_blue_sell_block")),
            new SellBlock(Block.Properties.ofFullCopy(WHITE_SELL_BLOCK)
                    .setId(ResourceKey.create(BuiltInRegistries.BLOCK.key(), DynamicShops.id("light_blue_sell_block"))))
    );

    public static final Block YELLOW_SELL_BLOCK = Registry.register(
            BuiltInRegistries.BLOCK,
            ResourceKey.create(BuiltInRegistries.BLOCK.key(), DynamicShops.id("yellow_sell_block")),
            new SellBlock(Block.Properties.ofFullCopy(WHITE_SELL_BLOCK)
                    .setId(ResourceKey.create(BuiltInRegistries.BLOCK.key(), DynamicShops.id("yellow_sell_block"))))
    );

    public static final Block LIME_SELL_BLOCK = Registry.register(
            BuiltInRegistries.BLOCK,
            ResourceKey.create(BuiltInRegistries.BLOCK.key(), DynamicShops.id("lime_sell_block")),
            new SellBlock(Block.Properties.ofFullCopy(WHITE_SELL_BLOCK)
                    .setId(ResourceKey.create(BuiltInRegistries.BLOCK.key(), DynamicShops.id("lime_sell_block"))))
    );

    public static final Block PINK_SELL_BLOCK = Registry.register(
            BuiltInRegistries.BLOCK,
            ResourceKey.create(BuiltInRegistries.BLOCK.key(), DynamicShops.id("pink_sell_block")),
            new SellBlock(Block.Properties.ofFullCopy(WHITE_SELL_BLOCK)
                    .setId(ResourceKey.create(BuiltInRegistries.BLOCK.key(), DynamicShops.id("pink_sell_block"))))
    );

    public static final Block GRAY_SELL_BLOCK = Registry.register(
            BuiltInRegistries.BLOCK,
            ResourceKey.create(BuiltInRegistries.BLOCK.key(), DynamicShops.id("gray_sell_block")),
            new SellBlock(Block.Properties.ofFullCopy(WHITE_SELL_BLOCK)
                    .setId(ResourceKey.create(BuiltInRegistries.BLOCK.key(), DynamicShops.id("gray_sell_block"))))
    );

    public static final Block LIGHT_GRAY_SELL_BLOCK = Registry.register(
            BuiltInRegistries.BLOCK,
            ResourceKey.create(BuiltInRegistries.BLOCK.key(), DynamicShops.id("light_gray_sell_block")),
            new SellBlock(Block.Properties.ofFullCopy(WHITE_SELL_BLOCK)
                    .setId(ResourceKey.create(BuiltInRegistries.BLOCK.key(), DynamicShops.id("light_gray_sell_block"))))
    );

    public static final Block CYAN_SELL_BLOCK = Registry.register(
            BuiltInRegistries.BLOCK,
            ResourceKey.create(BuiltInRegistries.BLOCK.key(), DynamicShops.id("cyan_sell_block")),
            new SellBlock(Block.Properties.ofFullCopy(WHITE_SELL_BLOCK)
                    .setId(ResourceKey.create(BuiltInRegistries.BLOCK.key(), DynamicShops.id("cyan_sell_block"))))
    );

    public static final Block PURPLE_SELL_BLOCK = Registry.register(
            BuiltInRegistries.BLOCK,
            ResourceKey.create(BuiltInRegistries.BLOCK.key(), DynamicShops.id("purple_sell_block")),
            new SellBlock(Block.Properties.ofFullCopy(WHITE_SELL_BLOCK)
                    .setId(ResourceKey.create(BuiltInRegistries.BLOCK.key(), DynamicShops.id("purple_sell_block"))))
    );

    public static final Block BLUE_SELL_BLOCK = Registry.register(
            BuiltInRegistries.BLOCK,
            ResourceKey.create(BuiltInRegistries.BLOCK.key(), DynamicShops.id("blue_sell_block")),
            new SellBlock(Block.Properties.ofFullCopy(WHITE_SELL_BLOCK)
                    .setId(ResourceKey.create(BuiltInRegistries.BLOCK.key(), DynamicShops.id("blue_sell_block"))))
    );

    public static final Block BROWN_SELL_BLOCK = Registry.register(
            BuiltInRegistries.BLOCK,
            ResourceKey.create(BuiltInRegistries.BLOCK.key(), DynamicShops.id("brown_sell_block")),
            new SellBlock(Block.Properties.ofFullCopy(WHITE_SELL_BLOCK)
                    .setId(ResourceKey.create(BuiltInRegistries.BLOCK.key(), DynamicShops.id("brown_sell_block"))))
    );

    public static final Block GREEN_SELL_BLOCK = Registry.register(
            BuiltInRegistries.BLOCK,
            ResourceKey.create(BuiltInRegistries.BLOCK.key(), DynamicShops.id("green_sell_block")),
            new SellBlock(Block.Properties.ofFullCopy(WHITE_SELL_BLOCK)
                    .setId(ResourceKey.create(BuiltInRegistries.BLOCK.key(), DynamicShops.id("green_sell_block"))))
    );

    public static final Block RED_SELL_BLOCK = Registry.register(
            BuiltInRegistries.BLOCK,
            ResourceKey.create(BuiltInRegistries.BLOCK.key(), DynamicShops.id("red_sell_block")),
            new SellBlock(Block.Properties.ofFullCopy(WHITE_SELL_BLOCK)
                    .setId(ResourceKey.create(BuiltInRegistries.BLOCK.key(), DynamicShops.id("red_sell_block"))))
    );

    public static final Block BLACK_SELL_BLOCK = Registry.register(
            BuiltInRegistries.BLOCK,
            ResourceKey.create(BuiltInRegistries.BLOCK.key(), DynamicShops.id("black_sell_block")),
            new SellBlock(Block.Properties.ofFullCopy(WHITE_SELL_BLOCK)
                    .setId(ResourceKey.create(BuiltInRegistries.BLOCK.key(), DynamicShops.id("black_sell_block"))))
    );

    // === SHOP BLOCKS ===
    public static final Block WHITE_SHOP_BLOCK = Registry.register(
            BuiltInRegistries.BLOCK,
            ResourceKey.create(BuiltInRegistries.BLOCK.key(), DynamicShops.id("white_shop_block")),
            new ShopBlock(Block.Properties.of()
                    .setId(ResourceKey.create(BuiltInRegistries.BLOCK.key(), DynamicShops.id("white_shop_block"))))
    );

    public static final Block ORANGE_SHOP_BLOCK = Registry.register(
            BuiltInRegistries.BLOCK,
            ResourceKey.create(BuiltInRegistries.BLOCK.key(), DynamicShops.id("orange_shop_block")),
            new ShopBlock(Block.Properties.ofFullCopy(WHITE_SHOP_BLOCK)
                    .setId(ResourceKey.create(BuiltInRegistries.BLOCK.key(), DynamicShops.id("orange_shop_block"))))
    );

    public static final Block MAGENTA_SHOP_BLOCK = Registry.register(
            BuiltInRegistries.BLOCK,
            ResourceKey.create(BuiltInRegistries.BLOCK.key(), DynamicShops.id("magenta_shop_block")),
            new ShopBlock(Block.Properties.ofFullCopy(WHITE_SHOP_BLOCK)
                    .setId(ResourceKey.create(BuiltInRegistries.BLOCK.key(), DynamicShops.id("magenta_shop_block"))))
    );

    public static final Block LIGHT_BLUE_SHOP_BLOCK = Registry.register(
            BuiltInRegistries.BLOCK,
            ResourceKey.create(BuiltInRegistries.BLOCK.key(), DynamicShops.id("light_blue_shop_block")),
            new ShopBlock(Block.Properties.ofFullCopy(WHITE_SHOP_BLOCK)
                    .setId(ResourceKey.create(BuiltInRegistries.BLOCK.key(), DynamicShops.id("light_blue_shop_block"))))
    );

    public static final Block YELLOW_SHOP_BLOCK = Registry.register(
            BuiltInRegistries.BLOCK,
            ResourceKey.create(BuiltInRegistries.BLOCK.key(), DynamicShops.id("yellow_shop_block")),
            new ShopBlock(Block.Properties.ofFullCopy(WHITE_SHOP_BLOCK)
                    .setId(ResourceKey.create(BuiltInRegistries.BLOCK.key(), DynamicShops.id("yellow_shop_block"))))
    );

    public static final Block LIME_SHOP_BLOCK = Registry.register(
            BuiltInRegistries.BLOCK,
            ResourceKey.create(BuiltInRegistries.BLOCK.key(), DynamicShops.id("lime_shop_block")),
            new ShopBlock(Block.Properties.ofFullCopy(WHITE_SHOP_BLOCK)
                    .setId(ResourceKey.create(BuiltInRegistries.BLOCK.key(), DynamicShops.id("lime_shop_block"))))
    );

    public static final Block PINK_SHOP_BLOCK = Registry.register(
            BuiltInRegistries.BLOCK,
            ResourceKey.create(BuiltInRegistries.BLOCK.key(), DynamicShops.id("pink_shop_block")),
            new ShopBlock(Block.Properties.ofFullCopy(WHITE_SHOP_BLOCK)
                    .setId(ResourceKey.create(BuiltInRegistries.BLOCK.key(), DynamicShops.id("pink_shop_block"))))
    );

    public static final Block GRAY_SHOP_BLOCK = Registry.register(
            BuiltInRegistries.BLOCK,
            ResourceKey.create(BuiltInRegistries.BLOCK.key(), DynamicShops.id("gray_shop_block")),
            new ShopBlock(Block.Properties.ofFullCopy(WHITE_SHOP_BLOCK)
                    .setId(ResourceKey.create(BuiltInRegistries.BLOCK.key(), DynamicShops.id("gray_shop_block"))))
    );

    public static final Block LIGHT_GRAY_SHOP_BLOCK = Registry.register(
            BuiltInRegistries.BLOCK,
            ResourceKey.create(BuiltInRegistries.BLOCK.key(), DynamicShops.id("light_gray_shop_block")),
            new ShopBlock(Block.Properties.ofFullCopy(WHITE_SHOP_BLOCK)
                    .setId(ResourceKey.create(BuiltInRegistries.BLOCK.key(), DynamicShops.id("light_gray_shop_block"))))
    );

    public static final Block CYAN_SHOP_BLOCK = Registry.register(
            BuiltInRegistries.BLOCK,
            ResourceKey.create(BuiltInRegistries.BLOCK.key(), DynamicShops.id("cyan_shop_block")),
            new ShopBlock(Block.Properties.ofFullCopy(WHITE_SHOP_BLOCK)
                    .setId(ResourceKey.create(BuiltInRegistries.BLOCK.key(), DynamicShops.id("cyan_shop_block"))))
    );

    public static final Block PURPLE_SHOP_BLOCK = Registry.register(
            BuiltInRegistries.BLOCK,
            ResourceKey.create(BuiltInRegistries.BLOCK.key(), DynamicShops.id("purple_shop_block")),
            new ShopBlock(Block.Properties.ofFullCopy(WHITE_SHOP_BLOCK)
                    .setId(ResourceKey.create(BuiltInRegistries.BLOCK.key(), DynamicShops.id("purple_shop_block"))))
    );

    public static final Block BLUE_SHOP_BLOCK = Registry.register(
            BuiltInRegistries.BLOCK,
            ResourceKey.create(BuiltInRegistries.BLOCK.key(), DynamicShops.id("blue_shop_block")),
            new ShopBlock(Block.Properties.ofFullCopy(WHITE_SHOP_BLOCK)
                    .setId(ResourceKey.create(BuiltInRegistries.BLOCK.key(), DynamicShops.id("blue_shop_block"))))
    );

    public static final Block BROWN_SHOP_BLOCK = Registry.register(
            BuiltInRegistries.BLOCK,
            ResourceKey.create(BuiltInRegistries.BLOCK.key(), DynamicShops.id("brown_shop_block")),
            new ShopBlock(Block.Properties.ofFullCopy(WHITE_SHOP_BLOCK)
                    .setId(ResourceKey.create(BuiltInRegistries.BLOCK.key(), DynamicShops.id("brown_shop_block"))))
    );

    public static final Block GREEN_SHOP_BLOCK = Registry.register(
            BuiltInRegistries.BLOCK,
            ResourceKey.create(BuiltInRegistries.BLOCK.key(), DynamicShops.id("green_shop_block")),
            new ShopBlock(Block.Properties.ofFullCopy(WHITE_SHOP_BLOCK)
                    .setId(ResourceKey.create(BuiltInRegistries.BLOCK.key(), DynamicShops.id("green_shop_block"))))
    );

    public static final Block RED_SHOP_BLOCK = Registry.register(
            BuiltInRegistries.BLOCK,
            ResourceKey.create(BuiltInRegistries.BLOCK.key(), DynamicShops.id("red_shop_block")),
            new ShopBlock(Block.Properties.ofFullCopy(WHITE_SHOP_BLOCK)
                    .setId(ResourceKey.create(BuiltInRegistries.BLOCK.key(), DynamicShops.id("red_shop_block"))))
    );

    public static final Block BLACK_SHOP_BLOCK = Registry.register(
            BuiltInRegistries.BLOCK,
            ResourceKey.create(BuiltInRegistries.BLOCK.key(), DynamicShops.id("black_shop_block")),
            new ShopBlock(Block.Properties.ofFullCopy(WHITE_SHOP_BLOCK)
                    .setId(ResourceKey.create(BuiltInRegistries.BLOCK.key(), DynamicShops.id("black_shop_block"))))
    );

    // === ITEMS ===
    static {
        // ATM
        Registry.register(BuiltInRegistries.ITEM, DynamicShops.id("white_atm"), new BlockItem(WHITE_ATM, new Item.Properties()
                .setId(ResourceKey.create(BuiltInRegistries.ITEM.key(), DynamicShops.id("white_atm")))));
        Registry.register(BuiltInRegistries.ITEM, DynamicShops.id("black_atm"), new BlockItem(BLACK_ATM, new Item.Properties()
                .setId(ResourceKey.create(BuiltInRegistries.ITEM.key(), DynamicShops.id("black_atm")))));

        // Sells
        Registry.register(BuiltInRegistries.ITEM, DynamicShops.id("white_sell_block"), new BlockItem(WHITE_SELL_BLOCK, new Item.Properties()
                .setId(ResourceKey.create(BuiltInRegistries.ITEM.key(), DynamicShops.id("white_sell_block")))));
        Registry.register(BuiltInRegistries.ITEM, DynamicShops.id("orange_sell_block"), new BlockItem(ORANGE_SELL_BLOCK, new Item.Properties()
                .setId(ResourceKey.create(BuiltInRegistries.ITEM.key(), DynamicShops.id("orange_sell_block")))));
        Registry.register(BuiltInRegistries.ITEM, DynamicShops.id("magenta_sell_block"), new BlockItem(MAGENTA_SELL_BLOCK, new Item.Properties()
                .setId(ResourceKey.create(BuiltInRegistries.ITEM.key(), DynamicShops.id("magenta_sell_block")))));
        Registry.register(BuiltInRegistries.ITEM, DynamicShops.id("light_blue_sell_block"), new BlockItem(LIGHT_BLUE_SELL_BLOCK, new Item.Properties()
                .setId(ResourceKey.create(BuiltInRegistries.ITEM.key(), DynamicShops.id("light_blue_sell_block")))));
        Registry.register(BuiltInRegistries.ITEM, DynamicShops.id("yellow_sell_block"), new BlockItem(YELLOW_SELL_BLOCK, new Item.Properties()
                .setId(ResourceKey.create(BuiltInRegistries.ITEM.key(), DynamicShops.id("yellow_sell_block")))));
        Registry.register(BuiltInRegistries.ITEM, DynamicShops.id("lime_sell_block"), new BlockItem(LIME_SELL_BLOCK, new Item.Properties()
                .setId(ResourceKey.create(BuiltInRegistries.ITEM.key(), DynamicShops.id("lime_sell_block")))));
        Registry.register(BuiltInRegistries.ITEM, DynamicShops.id("pink_sell_block"), new BlockItem(PINK_SELL_BLOCK, new Item.Properties()
                .setId(ResourceKey.create(BuiltInRegistries.ITEM.key(), DynamicShops.id("pink_sell_block")))));
        Registry.register(BuiltInRegistries.ITEM, DynamicShops.id("gray_sell_block"), new BlockItem(GRAY_SELL_BLOCK, new Item.Properties()
                .setId(ResourceKey.create(BuiltInRegistries.ITEM.key(), DynamicShops.id("gray_sell_block")))));
        Registry.register(BuiltInRegistries.ITEM, DynamicShops.id("light_gray_sell_block"), new BlockItem(LIGHT_GRAY_SELL_BLOCK, new Item.Properties()
                .setId(ResourceKey.create(BuiltInRegistries.ITEM.key(), DynamicShops.id("light_gray_sell_block")))));
        Registry.register(BuiltInRegistries.ITEM, DynamicShops.id("cyan_sell_block"), new BlockItem(CYAN_SELL_BLOCK, new Item.Properties()
                .setId(ResourceKey.create(BuiltInRegistries.ITEM.key(), DynamicShops.id("cyan_sell_block")))));
        Registry.register(BuiltInRegistries.ITEM, DynamicShops.id("purple_sell_block"), new BlockItem(PURPLE_SELL_BLOCK, new Item.Properties()
                .setId(ResourceKey.create(BuiltInRegistries.ITEM.key(), DynamicShops.id("purple_sell_block")))));
        Registry.register(BuiltInRegistries.ITEM, DynamicShops.id("blue_sell_block"), new BlockItem(BLUE_SELL_BLOCK, new Item.Properties()
                .setId(ResourceKey.create(BuiltInRegistries.ITEM.key(), DynamicShops.id("blue_sell_block")))));
        Registry.register(BuiltInRegistries.ITEM, DynamicShops.id("brown_sell_block"), new BlockItem(BROWN_SELL_BLOCK, new Item.Properties()
                .setId(ResourceKey.create(BuiltInRegistries.ITEM.key(), DynamicShops.id("brown_sell_block")))));
        Registry.register(BuiltInRegistries.ITEM, DynamicShops.id("green_sell_block"), new BlockItem(GREEN_SELL_BLOCK, new Item.Properties()
                .setId(ResourceKey.create(BuiltInRegistries.ITEM.key(), DynamicShops.id("green_sell_block")))));
        Registry.register(BuiltInRegistries.ITEM, DynamicShops.id("red_sell_block"), new BlockItem(RED_SELL_BLOCK, new Item.Properties()
                .setId(ResourceKey.create(BuiltInRegistries.ITEM.key(), DynamicShops.id("red_sell_block")))));
        Registry.register(BuiltInRegistries.ITEM, DynamicShops.id("black_sell_block"), new BlockItem(BLACK_SELL_BLOCK, new Item.Properties()
                .setId(ResourceKey.create(BuiltInRegistries.ITEM.key(), DynamicShops.id("black_sell_block")))));

        // Shops
        Registry.register(BuiltInRegistries.ITEM, DynamicShops.id("white_shop_block"), new BlockItem(WHITE_SHOP_BLOCK, new Item.Properties()
                .setId(ResourceKey.create(BuiltInRegistries.ITEM.key(), DynamicShops.id("white_shop_block")))));
        Registry.register(BuiltInRegistries.ITEM, DynamicShops.id("orange_shop_block"), new BlockItem(ORANGE_SHOP_BLOCK, new Item.Properties()
                .setId(ResourceKey.create(BuiltInRegistries.ITEM.key(), DynamicShops.id("orange_shop_block")))));
        Registry.register(BuiltInRegistries.ITEM, DynamicShops.id("magenta_shop_block"), new BlockItem(MAGENTA_SHOP_BLOCK, new Item.Properties()
                .setId(ResourceKey.create(BuiltInRegistries.ITEM.key(), DynamicShops.id("magenta_shop_block")))));
        Registry.register(BuiltInRegistries.ITEM, DynamicShops.id("light_blue_shop_block"), new BlockItem(LIGHT_BLUE_SHOP_BLOCK, new Item.Properties()
                .setId(ResourceKey.create(BuiltInRegistries.ITEM.key(), DynamicShops.id("light_blue_shop_block")))));
        Registry.register(BuiltInRegistries.ITEM, DynamicShops.id("yellow_shop_block"), new BlockItem(YELLOW_SHOP_BLOCK, new Item.Properties()
                .setId(ResourceKey.create(BuiltInRegistries.ITEM.key(), DynamicShops.id("yellow_shop_block")))));
        Registry.register(BuiltInRegistries.ITEM, DynamicShops.id("lime_shop_block"), new BlockItem(LIME_SHOP_BLOCK, new Item.Properties()
                .setId(ResourceKey.create(BuiltInRegistries.ITEM.key(), DynamicShops.id("lime_shop_block")))));
        Registry.register(BuiltInRegistries.ITEM, DynamicShops.id("pink_shop_block"), new BlockItem(PINK_SHOP_BLOCK, new Item.Properties()
                .setId(ResourceKey.create(BuiltInRegistries.ITEM.key(), DynamicShops.id("pink_shop_block")))));
        Registry.register(BuiltInRegistries.ITEM, DynamicShops.id("gray_shop_block"), new BlockItem(GRAY_SHOP_BLOCK, new Item.Properties()
                .setId(ResourceKey.create(BuiltInRegistries.ITEM.key(), DynamicShops.id("gray_shop_block")))));
        Registry.register(BuiltInRegistries.ITEM, DynamicShops.id("light_gray_shop_block"), new BlockItem(LIGHT_GRAY_SHOP_BLOCK, new Item.Properties()
                .setId(ResourceKey.create(BuiltInRegistries.ITEM.key(), DynamicShops.id("light_gray_shop_block")))));
        Registry.register(BuiltInRegistries.ITEM, DynamicShops.id("cyan_shop_block"), new BlockItem(CYAN_SHOP_BLOCK, new Item.Properties()
                .setId(ResourceKey.create(BuiltInRegistries.ITEM.key(), DynamicShops.id("cyan_shop_block")))));
        Registry.register(BuiltInRegistries.ITEM, DynamicShops.id("purple_shop_block"), new BlockItem(PURPLE_SHOP_BLOCK, new Item.Properties()
                .setId(ResourceKey.create(BuiltInRegistries.ITEM.key(), DynamicShops.id("purple_shop_block")))));
        Registry.register(BuiltInRegistries.ITEM, DynamicShops.id("blue_shop_block"), new BlockItem(BLUE_SHOP_BLOCK, new Item.Properties()
                .setId(ResourceKey.create(BuiltInRegistries.ITEM.key(), DynamicShops.id("blue_shop_block")))));
        Registry.register(BuiltInRegistries.ITEM, DynamicShops.id("brown_shop_block"), new BlockItem(BROWN_SHOP_BLOCK, new Item.Properties()
                .setId(ResourceKey.create(BuiltInRegistries.ITEM.key(), DynamicShops.id("brown_shop_block")))));
        Registry.register(BuiltInRegistries.ITEM, DynamicShops.id("green_shop_block"), new BlockItem(GREEN_SHOP_BLOCK, new Item.Properties()
                .setId(ResourceKey.create(BuiltInRegistries.ITEM.key(), DynamicShops.id("green_shop_block")))));
        Registry.register(BuiltInRegistries.ITEM, DynamicShops.id("red_shop_block"), new BlockItem(RED_SHOP_BLOCK, new Item.Properties()
                .setId(ResourceKey.create(BuiltInRegistries.ITEM.key(), DynamicShops.id("red_shop_block")))));
        Registry.register(BuiltInRegistries.ITEM, DynamicShops.id("black_shop_block"), new BlockItem(BLACK_SHOP_BLOCK, new Item.Properties()
                .setId(ResourceKey.create(BuiltInRegistries.ITEM.key(), DynamicShops.id("black_shop_block")))));
    }

    public static void register() {}

    public static void registerBlockEntities() {
        SellBlockEntity.registerBlockEntity();
        ShopBlockEntity.registerBlockEntity();
        AtmBlockEntity.registerBlockEntity();
    }
}