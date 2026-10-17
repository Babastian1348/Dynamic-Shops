package com.babastian.dynamicshops.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.babastian.dynamicshops.DynamicShops;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class ClockConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static ClockConfig instance;

    // Intervalo de reset para SellBlock (límite de venta) en "horas"
    public int sellLimitResetIntervalHours = 24;

    // Intervalo de reset para ShopBlock (stock de compra) en "horas"
    public int shopStockResetIntervalHours = 24;

    // === SISTEMA DE INTERESES BANCARIOS ===
    public boolean bankInterestEnabled = true;
    public int bankInterestIntervalHours = 168;  // Cada 24 horas de Minecraft (1 día)

    // Tramos de interés degradado (límite superior y porcentaje de interés)
    public List<InterestTier> bankInterestTiers = List.of(
            new InterestTier(10000, 2.5),
            new InterestTier(50000, 2.0),
            new InterestTier(100000, 1.5),
            new InterestTier(2500000, 1.0),
            new InterestTier(5000000, 0.8),
            new InterestTier(10000000, 0.5),
            new InterestTier(Integer.MAX_VALUE, 0.0) // De 1,000,001 en adelante
    );

    // Clase auxiliar para representar cada tramo en el JSON
    public static class InterestTier {
        public int maxBalance;
        public double ratePercent;

        public InterestTier(int maxBalance, double ratePercent) {
            this.maxBalance = maxBalance;
            this.ratePercent = ratePercent;
        }
    }

    public static ClockConfig getInstance() {
        if (instance == null) {
            instance = load();
        }
        return instance;
    }

    private static ClockConfig load() {
        Path configPath = FabricLoader.getInstance().getConfigDir()
                .resolve("dynamic-shops")
                .resolve("clock-config.json");

        try {
            Files.createDirectories(configPath.getParent());
        } catch (IOException e) {
            DynamicShops.LOGGER.error("Failed to create config directory", e);
        }

        if (!Files.exists(configPath)) {
            instance = new ClockConfig();
            save(instance, configPath);
            DynamicShops.LOGGER.info("Created default clock-config.json config");
            return instance;
        }

        try (Reader reader = Files.newBufferedReader(configPath)) {
            instance = GSON.fromJson(reader, ClockConfig.class);

            // Asegurar que la lista no sea nula si el usuario usa una config viejita
            if (instance.bankInterestTiers == null || instance.bankInterestTiers.isEmpty()) {
                instance.bankInterestTiers = new ClockConfig().bankInterestTiers;
            }

            DynamicShops.LOGGER.info("Loaded clock-config.json config (sell limit: {}h, shop stock: {}h, bank interest tiers enabled)",
                    instance.sellLimitResetIntervalHours, instance.shopStockResetIntervalHours);
            return instance;
        } catch (IOException e) {
            DynamicShops.LOGGER.error("Failed to load clock-config.json config", e);
            instance = new ClockConfig();
            return instance;
        }
    }

    private static void save(ClockConfig config, Path path) {
        try (Writer writer = Files.newBufferedWriter(path)) {
            GSON.toJson(config, writer);
        } catch (IOException e) {
            DynamicShops.LOGGER.error("Failed to save clock-config.json config", e);
        }
    }

    public static void save() {
        if (instance != null) {
            Path configPath = FabricLoader.getInstance().getConfigDir()
                    .resolve("dynamic-shops")
                    .resolve("clock-config.json");
            save(instance, configPath);
        }
    }

    public int getSellLimitResetIntervalHours() {
        return sellLimitResetIntervalHours;
    }

    public int getShopStockResetIntervalHours() {
        return shopStockResetIntervalHours;
    }

    public boolean isBankInterestEnabled() {
        return bankInterestEnabled;
    }

    public int getBankInterestIntervalHours() {
        return bankInterestIntervalHours;
    }

    public List<InterestTier> getBankInterestTiers() {
        return bankInterestTiers;
    }
}