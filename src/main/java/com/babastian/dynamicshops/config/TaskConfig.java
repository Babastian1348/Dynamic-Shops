package com.babastian.dynamicshops.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.babastian.dynamicshops.DynamicShops;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class TaskConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static TaskConfig instance;

    // Mineria
    public int commonBlocksAmount = 500; // Evita el spam. Recompensa pasiva por jugar normal.
    public int commonBlocksReward = 25;

    public int logsAmount = 64;          // 1 stack exacto de troncos.
    public int logsReward = 20;

    public int oresAmount = 32;          // Medio stack de minerales.
    public int oresReward = 30;

    // Combate
    public int passiveMobsAmount = 25;   // 25 animales está bien para una granja casera.
    public int passiveMobsReward = 15;

    public int weakHostilesAmount = 50;  // 1 moneda por kill (zombies, esqueletos, etc.)
    public int weakHostilesReward = 50;

    public int strongHostilesAmount = 15; // 5 monedas por kill (Endermans, Brutes, etc.)
    public int strongHostilesReward = 75;

    public int bossesAmount = 1;         // ¡Cada jefe derrotado debe sentirse como un logro instantáneo!
    public int bossesReward = 500;

    // Agricultura
    public int cropsAmount = 128;        // 2 stacks de cultivos.
    public int cropsReward = 40;

    // Pesca
    public int fishAmount = 15;          // La pesca es lenta y activa, merece pagar bien por poco tiempo.
    public int fishReward = 50;

    // Exploracion
    public int distanceAmount = 10000;   // Equivale a 1,000 bloques reales recorridos.
    public int distanceReward = 25;      // Recompensa constante por viajes largos.

    // Completionista
    public int advancementsAmount = 5;   // 10 logros se vuelve muy difícil en late-game. 5 es perfecto.
    public int advancementsReward = 250;

    // Comerciante
    public int tradesAmount = 64;        // 1 stack de tradeos (ej: esmeraldas)
    public int tradesReward = 150;

    // Constructor
    public int blocksPlacedAmount = 500;
    public int blocksPlacedReward = 25;

    // Cocina
    public int cookingAmount = 64;       // 1 stack completo cocinado o fundido.
    public int cookingReward = 25;

    public List<String> oreWhitelist = List.of(
            "minecraft:coal_ore",
            "minecraft:deepslate_coal_ore",
            "minecraft:iron_ore",
            "minecraft:deepslate_iron_ore",
            "minecraft:copper_ore",
            "minecraft:deepslate_copper_ore",
            "minecraft:gold_ore",
            "minecraft:deepslate_gold_ore",
            "minecraft:redstone_ore",
            "minecraft:deepslate_redstone_ore",
            "minecraft:emerald_ore",
            "minecraft:deepslate_emerald_ore",
            "minecraft:lapis_ore",
            "minecraft:deepslate_lapis_ore",
            "minecraft:diamond_ore",
            "minecraft:deepslate_diamond_ore",
            "minecraft:nether_gold_ore",
            "minecraft:nether_quartz_ore",
            "minecraft:ancient_debris"
    );

    public List<String> logWhitelist = List.of(
            "minecraft:oak_log",
            "minecraft:birch_log",
            "minecraft:spruce_log",
            "minecraft:jungle_log",
            "minecraft:acacia_log",
            "minecraft:dark_oak_log",
            "minecraft:mangrove_log",
            "minecraft:cherry_log",
            "minecraft:crimson_stem",
            "minecraft:warped_stem"
    );

    public List<String> BlacklistPlacement = List.of(
            "#minecraft:flowers",
            "#minecraft:replaceable"
    );

    public static TaskConfig getInstance() {
        if (instance == null) {
            instance = load();
        }
        return instance;
    }

    private static TaskConfig load() {
        Path configPath = FabricLoader.getInstance().getConfigDir().resolve("dynamic-shops").resolve("tasks.json");

        // Crear directorio si no existe
        try {
            Files.createDirectories(configPath.getParent());
        } catch (IOException e) {
            DynamicShops.LOGGER.error("Failed to create config directory", e);
        }

        // Si no existe, crear con valores por defecto
        if (!Files.exists(configPath)) {
            instance = new TaskConfig();
            save(instance, configPath);
            DynamicShops.LOGGER.info("Created default tasks.json config");
            return instance;
        }

        // Leer archivo
        try (Reader reader = Files.newBufferedReader(configPath)) {
            instance = GSON.fromJson(reader, TaskConfig.class);
            DynamicShops.LOGGER.info("Loaded tasks.json config");
            return instance;
        } catch (IOException e) {
            DynamicShops.LOGGER.error("Failed to load tasks.json config", e);
            instance = new TaskConfig();
            return instance;
        }
    }

    private static void save(TaskConfig config, Path path) {
        try (Writer writer = Files.newBufferedWriter(path)) {
            GSON.toJson(config, writer);
        } catch (IOException e) {
            DynamicShops.LOGGER.error("Failed to save tasks.json config", e);
        }
    }

    public static void save() {
        if (instance != null) {
            Path configPath = FabricLoader.getInstance().getConfigDir().resolve("dynamic-shops").resolve("tasks.json");
            save(instance, configPath);
        }
    }

    // Helpers para verificar si un bloque es mineral o tronco
    public boolean isOre(String blockId) {
        return oreWhitelist.contains(blockId);
    }

    public boolean isLog(String blockId) {
        return logWhitelist.contains(blockId);
    }
}