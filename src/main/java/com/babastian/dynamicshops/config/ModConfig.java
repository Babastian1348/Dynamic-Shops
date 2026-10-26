package com.babastian.dynamicshops.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import com.babastian.dynamicshops.DynamicShops;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ModConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static ModConfig instance;

    @SerializedName("CurrencyValues")
    public Map<String, Integer> currencyValues = new LinkedHashMap<>();

    public int MAX_CAPACITY = 100000;

    public int sellLimitResetIntervalHours = 24;
    public int shopStockResetIntervalHours = 24;

    public int collaboratorSalesThreshold = 100;
    public int collaboratorBonusReward = 25;

    public boolean bankTaxEnabled = true;
    public int bankTaxIntervalHours = 168;

    public List<TaxTier> bankTaxTiers = List.of(
            new TaxTier(1000, 0.0),
            new TaxTier(10000, 0.5),
            new TaxTier(50000, 1.0),
            new TaxTier(100000, 1.5),
            new TaxTier(500000, 2.0),
            new TaxTier(Integer.MAX_VALUE, 3.0)
    );

    public static class TaxTier {
        public int maxBalance;
        public double taxPercent;

        public TaxTier(int maxBalance, double taxPercent) {
            this.maxBalance = maxBalance;
            this.taxPercent = taxPercent;
        }
    }

    // === OBJETO ANIDADO: TASKS ===
    @SerializedName("Tasks")
    public TasksSection tasks = new TasksSection();

    public static class TasksSection {
        public BreakCommonBlocksTask BreakCommonBlocks = new BreakCommonBlocksTask();
        public ChopLogsTask ChopLogs = new ChopLogsTask();
        public MineOresTask MineOres = new MineOresTask();
        public KillPassiveMobsTask KillPassiveMobs = new KillPassiveMobsTask();
        public KillWeakHostileMobsTask KillWeakHostileMobs = new KillWeakHostileMobsTask();
        public KillStrongHostileMobsTask KillStrongHostileMobs = new KillStrongHostileMobsTask();
        public KillBossesTask KillBosses = new KillBossesTask();
        public HarvestCropsTask HarvestCrops = new HarvestCropsTask();
        public CatchFishesTask CatchFishes = new CatchFishesTask();
        public WalkDistanceTask WalkDistance = new WalkDistanceTask();
        public CompleteAdvancementsTask CompleteAdvancements = new CompleteAdvancementsTask();
        public TradeWithVillagersTask TradeWithVillagers = new TradeWithVillagersTask();
        public PlaceBlocksTask PlaceBlocks = new PlaceBlocksTask();
        public CookItemsTask CookItems = new CookItemsTask();

        public List<String> oreWhitelist = List.of(
                "minecraft:coal_ore", "minecraft:deepslate_coal_ore",
                "minecraft:iron_ore", "minecraft:deepslate_iron_ore",
                "minecraft:copper_ore", "minecraft:deepslate_copper_ore",
                "minecraft:gold_ore", "minecraft:deepslate_gold_ore",
                "minecraft:redstone_ore", "minecraft:deepslate_redstone_ore",
                "minecraft:emerald_ore", "minecraft:deepslate_emerald_ore",
                "minecraft:lapis_ore", "minecraft:deepslate_lapis_ore",
                "minecraft:diamond_ore", "minecraft:deepslate_diamond_ore",
                "minecraft:nether_gold_ore", "minecraft:nether_quartz_ore",
                "minecraft:ancient_debris"
        );

        public List<String> logWhitelist = List.of(
                "minecraft:oak_log", "minecraft:birch_log", "minecraft:spruce_log",
                "minecraft:jungle_log", "minecraft:acacia_log", "minecraft:dark_oak_log",
                "minecraft:mangrove_log", "minecraft:cherry_log",
                "minecraft:crimson_stem", "minecraft:warped_stem"
        );

        public List<String> BlacklistPlacement = List.of(
                "#minecraft:flowers",
                "#minecraft:replaceable"
        );
    }

    // === CLASES ESPECÍFICAS PARA CADA TAREA ===
    public static class BreakCommonBlocksTask {
        public int commonBlocksAmount = 500;
        public int commonBlocksReward = 5;
    }
    public static class ChopLogsTask {
        public int logsAmount = 64;
        public int logsReward = 20;
    }
    public static class MineOresTask {
        public int oresAmount = 32;
        public int oresReward = 15;
    }
    public static class KillPassiveMobsTask {
        public int passiveMobsAmount = 50;
        public int passiveMobsReward = 10;
    }
    public static class KillWeakHostileMobsTask {
        public int weakHostilesAmount = 100;
        public int weakHostilesReward = 25;
    }
    public static class KillStrongHostileMobsTask {
        public int strongHostilesAmount = 50;
        public int strongHostilesReward = 50;
    }
    public static class KillBossesTask {
        public int bossesAmount = 1;
        public int bossesReward = 500;
    }
    public static class HarvestCropsTask {
        public int cropsAmount = 128;
        public int cropsReward = 25;
    }
    public static class CatchFishesTask {
        public int fishAmount = 50;
        public int fishReward = 50;
    }
    public static class WalkDistanceTask {
        public int distanceAmount = 50000;
        public int distanceReward = 10;
    }
    public static class CompleteAdvancementsTask {
        public int advancementsAmount = 5;
        public int advancementsReward = 250;
    }
    public static class TradeWithVillagersTask {
        public int tradesAmount = 100;
        public int tradesReward = 25;
    }
    public static class PlaceBlocksTask {
        public int blocksPlacedAmount = 500;
        public int blocksPlacedReward = 5;
    }
    public static class CookItemsTask {
        public int cookingAmount = 64;
        public int cookingReward = 20;
    }

    public static ModConfig getInstance() {
        if (instance == null) {
            instance = load();
        }
        return instance;
    }

    private static ModConfig load() {
        Path configPath = FabricLoader.getInstance().getConfigDir()
                .resolve("dynamic-shops")
                .resolve("config.json");

        try {
            Files.createDirectories(configPath.getParent());
        } catch (IOException e) {
            DynamicShops.LOGGER.error("Failed to create config directory", e);
        }

        if (!Files.exists(configPath)) {
            instance = new ModConfig();
            instance.loadDefaults();
            save(instance, configPath);
            DynamicShops.LOGGER.info("Created default config.json");
            return instance;
        }

        try (Reader reader = Files.newBufferedReader(configPath)) {
            instance = GSON.fromJson(reader, ModConfig.class);
            if (instance.currencyValues == null || instance.currencyValues.isEmpty()) {
                instance.loadDefaults();
            }
            DynamicShops.LOGGER.info("Loaded config.json successfully");
            return instance;
        } catch (Exception e) {
            DynamicShops.LOGGER.error("Failed to load config.json, using defaults", e);
            instance = new ModConfig();
            instance.loadDefaults();
            return instance;
        }
    }

    private void loadDefaults() {
        if (currencyValues.isEmpty()) {
            currencyValues.put("copper_coin", 1);
            currencyValues.put("copper_bill", 5);
            currencyValues.put("iron_coin", 10);
            currencyValues.put("iron_bill", 50);
            currencyValues.put("gold_coin", 100);
            currencyValues.put("gold_bill", 500);
            currencyValues.put("emerald_coin", 1000);
            currencyValues.put("emerald_bill", 5000);
            currencyValues.put("diamond_coin", 10000);
            currencyValues.put("diamond_bill", 50000);
            currencyValues.put("netherite_bill", 100000);
        }
        if (tasks == null) {
            tasks = new TasksSection();
        }
    }

    private static void save(ModConfig config, Path path) {
        try (Writer writer = Files.newBufferedWriter(path)) {
            GSON.toJson(config, writer);
        } catch (IOException e) {
            DynamicShops.LOGGER.error("Failed to save config.json", e);
        }
    }

    public static void save() {
        if (instance != null) {
            Path configPath = FabricLoader.getInstance().getConfigDir()
                    .resolve("dynamic-shops")
                    .resolve("config.json");
            save(instance, configPath);
        }
    }

    public int getCurrencyValue(String itemId) {
        String cleanId = itemId.contains(":") ? itemId.split(":")[1] : itemId;
        return currencyValues.getOrDefault(cleanId, currencyValues.getOrDefault(itemId, 1));
    }

    public int getSellLimitResetIntervalHours() { return sellLimitResetIntervalHours; }
    public int getShopStockResetIntervalHours() { return shopStockResetIntervalHours; }
    public boolean isBankTaxEnabled() { return bankTaxEnabled; }
    public int getBankTaxIntervalHours() { return bankTaxIntervalHours; }
    public List<TaxTier> getBankTaxTiers() { return bankTaxTiers; }

    public List<String> getBlacklistPlacement() {
        return tasks.BlacklistPlacement;
    }

    public boolean isOre(String blockId) {
        return tasks.oreWhitelist.contains(blockId);
    }

    public boolean isLog(String blockId) {
        return tasks.logWhitelist.contains(blockId);
    }
}