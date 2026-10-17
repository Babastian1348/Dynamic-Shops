package com.babastian.dynamicshops.config;

import com.babastian.dynamicshops.DynamicShops;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class GlobalStockClock {
    private static GlobalStockClock instance;

    // Ciclo de último reset
    private long lastResetCycle = 0;

    // Ruta del archivo de datos
    private static final Path DATA_FILE = FabricLoader.getInstance()
            .getConfigDir()
            .resolve("dynamic-shops")
            .resolve("global-stock-clock.dat");

    public static GlobalStockClock getInstance() {
        if (instance == null) {
            instance = load();
        }
        return instance;
    }

    private static GlobalStockClock load() {
        instance = new GlobalStockClock();

        try {
            Files.createDirectories(DATA_FILE.getParent());
        } catch (IOException e) {
            DynamicShops.LOGGER.error("Failed to create data directory", e);
        }

        if (Files.exists(DATA_FILE)) {
            try (DataInputStream in = new DataInputStream(new FileInputStream(DATA_FILE.toFile()))) {
                instance.lastResetCycle = in.readLong();
                DynamicShops.LOGGER.info("[GlobalStockClock] Loaded (cycle: {})", instance.lastResetCycle);
            } catch (IOException e) {
                DynamicShops.LOGGER.error("Failed to load global-stock-clock.dat", e);
            }
        } else {
            DynamicShops.LOGGER.info("[GlobalStockClock] Created new (cycle: 0)");
        }

        return instance;
    }

    public static void save() {
        if (instance == null) {
            return;
        }

        try (DataOutputStream out = new DataOutputStream(new FileOutputStream(DATA_FILE.toFile()))) {
            out.writeLong(instance.lastResetCycle);
        } catch (IOException e) {
            DynamicShops.LOGGER.error("Failed to save global-stock-clock.dat", e);
        }
    }

    public boolean shouldResetStock(long currentResetCycle, int intervalHours) {
        if (intervalHours <= 24) {
            if (currentResetCycle != lastResetCycle) {
                lastResetCycle = currentResetCycle;
                save();
                return true;
            }
        } else {
            if (currentResetCycle > lastResetCycle) {
                lastResetCycle = currentResetCycle;
                save();
                return true;
            }
        }
        return false;
    }

    public long getLastResetCycle() {
        return lastResetCycle;
    }
}