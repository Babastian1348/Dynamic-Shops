package com.babastian.dynamicshops.config;

import com.babastian.dynamicshops.DynamicShops;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class GlobalLimitClock {
    private static GlobalLimitClock instance;

    // Ciclo de último reset
    private long lastResetCycle = 0;

    // Ruta del archivo de datos
    private static final Path DATA_FILE = FabricLoader.getInstance()
            .getConfigDir()
            .resolve("dynamic-shops")
            .resolve("global-limit-clock.dat");

    public static GlobalLimitClock getInstance() {
        if (instance == null) {
            instance = load();
        }
        return instance;
    }

    private static GlobalLimitClock load() {
        instance = new GlobalLimitClock();

        try {
            Files.createDirectories(DATA_FILE.getParent());
        } catch (IOException e) {
            DynamicShops.LOGGER.error("Failed to create data directory", e);
        }

        if (Files.exists(DATA_FILE)) {
            try (DataInputStream in = new DataInputStream(new FileInputStream(DATA_FILE.toFile()))) {
                instance.lastResetCycle = in.readLong();
                DynamicShops.LOGGER.info("[GlobalLimitClock] Loaded (cycle: {})", instance.lastResetCycle);
            } catch (IOException e) {
                DynamicShops.LOGGER.error("Failed to load global-limit-clock.dat", e);
            }
        } else {
            DynamicShops.LOGGER.info("[GlobalLimitClock] Created new (cycle: 0)");
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
            DynamicShops.LOGGER.error("Failed to save global-limit-clock.dat", e);
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