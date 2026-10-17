package com.babastian.dynamicshops.config;

import com.babastian.dynamicshops.DynamicShops;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class GlobalBankClock {
    private static GlobalBankClock instance;

    private long lastResetCycle = 0;

    private static final Path DATA_FILE = FabricLoader.getInstance()
            .getConfigDir()
            .resolve("dynamic-shops")
            .resolve("global-bank-clock.dat");

    public static GlobalBankClock getInstance() {
        if (instance == null) {
            instance = load();
        }
        return instance;
    }

    private static GlobalBankClock load() {
        instance = new GlobalBankClock();

        try {
            Files.createDirectories(DATA_FILE.getParent());
        } catch (IOException e) {
            DynamicShops.LOGGER.error("Failed to create data directory", e);
        }

        if (Files.exists(DATA_FILE)) {
            try (DataInputStream in = new DataInputStream(new FileInputStream(DATA_FILE.toFile()))) {
                instance.lastResetCycle = in.readLong();
                DynamicShops.LOGGER.info("[GlobalBankClock] Loaded (cycle: {})", instance.lastResetCycle);
            } catch (IOException e) {
                DynamicShops.LOGGER.error("Failed to load global-bank-clock.dat", e);
            }
        } else {
            DynamicShops.LOGGER.info("[GlobalBankClock] Created new (cycle: 0)");
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
            DynamicShops.LOGGER.error("Failed to save global-bank-clock.dat", e);
        }
    }

    public boolean shouldApplyInterest(long currentCycle, int intervalHours) {
        if (intervalHours <= 24) {
            if (currentCycle != lastResetCycle) {
                lastResetCycle = currentCycle;
                save();
                return true;
            }
        } else {
            if (currentCycle > lastResetCycle) {
                lastResetCycle = currentCycle;
                save();
                return true;
            }
        }
        return false;
    }

    public long getLastResetCycle() {
        return lastResetCycle;
    }

    public void setLastResetCycle(long cycle) {
        this.lastResetCycle = cycle;
        save();
    }
}