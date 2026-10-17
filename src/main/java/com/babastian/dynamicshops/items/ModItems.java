package com.babastian.dynamicshops.items;

import com.babastian.dynamicshops.DynamicShops;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

public class ModItems {

    // --- MONEDAS ---
    public static final Item COPPER_COIN = registerItem("copper_coin", new CurrencyItem(properties("copper_coin"), 1));
    public static final Item IRON_COIN = registerItem("iron_coin", new CurrencyItem(properties("iron_coin"), 10));
    public static final Item GOLD_COIN = registerItem("gold_coin", new CurrencyItem(properties("gold_coin"), 100));
    public static final Item EMERALD_COIN = registerItem("emerald_coin", new CurrencyItem(properties("emerald_coin"), 1000));
    public static final Item DIAMOND_COIN = registerItem("diamond_coin", new CurrencyItem(properties("diamond_coin"), 10000));

    // --- BILLETES ---
    public static final Item COPPER_BILL = registerItem("copper_bill", new CurrencyItem(properties("copper_bill"), 5));
    public static final Item IRON_BILL = registerItem("iron_bill", new CurrencyItem(properties("iron_bill"), 50));
    public static final Item GOLD_BILL = registerItem("gold_bill", new CurrencyItem(properties("gold_bill"), 500));
    public static final Item EMERALD_BILL = registerItem("emerald_bill", new CurrencyItem(properties("emerald_bill"), 5000));
    public static final Item DIAMOND_BILL = registerItem("diamond_bill", new CurrencyItem(properties("diamond_bill"), 50000));

    // Billete de Netherite con inmunidad al fuego
    public static final Item NETHERITE_BILL = registerItem("netherite_bill", new CurrencyItem(properties("netherite_bill").fireResistant(), 100000));

    // Método auxiliar requerido para asignar el ID/Key al item en sus propiedades
    private static Item.Properties properties(String name) {
        return new Item.Properties().setId(ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, DynamicShops.id(name)));
    }

    private static Item registerItem(String name, Item item) {
        return Registry.register(BuiltInRegistries.ITEM, DynamicShops.id(name), item);
    }

    public static void register() {
    }
}