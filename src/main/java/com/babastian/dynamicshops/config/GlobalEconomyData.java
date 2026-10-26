package com.babastian.dynamicshops.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import com.babastian.dynamicshops.DynamicShops;

public class GlobalEconomyData extends SavedData {

    public long bankLastResetCycle;
    public long limitLastResetCycle;
    public long stockLastResetCycle;

    // 1. Constructor con parámetros requerido por el Codec
    public GlobalEconomyData(long bankLastResetCycle, long limitLastResetCycle, long stockLastResetCycle) {
        this.bankLastResetCycle = bankLastResetCycle;
        this.limitLastResetCycle = limitLastResetCycle;
        this.stockLastResetCycle = stockLastResetCycle;
    }

    // 2. Constructor vacío requerido por el SavedDataType (para cuando el archivo no existe aún)
    public GlobalEconomyData() {
        this(0L, 0L, 0L);
    }

    // 3. El Codec de serialización exactamente como lo lee SavedDataStorage
    public static final Codec<GlobalEconomyData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.LONG.fieldOf("BankLastResetCycle").orElse(0L).forGetter(data -> data.bankLastResetCycle),
                    Codec.LONG.fieldOf("LimitLastResetCycle").orElse(0L).forGetter(data -> data.limitLastResetCycle),
                    Codec.LONG.fieldOf("StockLastResetCycle").orElse(0L).forGetter(data -> data.stockLastResetCycle)
            ).apply(instance, GlobalEconomyData::new)
    );

    // 4. El SavedDataType que vincula el ID, el constructor y el Codec
    public static final SavedDataType<GlobalEconomyData> TYPE = new SavedDataType<>(
            DynamicShops.id("economy_data"),
            GlobalEconomyData::new,
            CODEC,
            null
    );

    // 5. Método de acceso rápido global vinculado al Overworld
    public static GlobalEconomyData getInstance(ServerLevel level) {
        ServerLevel overworld = level.getServer().overworld();
        return overworld.getDataStorage().computeIfAbsent(TYPE);
    }

    public boolean shouldApplyBankTaxes(long currentCycle, int intervalHours) {
        if (intervalHours <= 24) {
            if (currentCycle != bankLastResetCycle) {
                bankLastResetCycle = currentCycle;
                setDirty();
                return true;
            }
        } else {
            if (currentCycle > bankLastResetCycle) {
                bankLastResetCycle = currentCycle;
                setDirty();
                return true;
            }
        }
        return false;
    }

    public boolean shouldResetLimitStock(long currentResetCycle, int intervalHours) {
        if (intervalHours <= 24) {
            if (currentResetCycle != limitLastResetCycle) {
                limitLastResetCycle = currentResetCycle;
                setDirty();
                return true;
            }
        } else {
            if (currentResetCycle > limitLastResetCycle) {
                limitLastResetCycle = currentResetCycle;
                setDirty();
                return true;
            }
        }
        return false;
    }

    public boolean shouldResetShopStock(long currentResetCycle, int intervalHours) {
        if (intervalHours <= 24) {
            if (currentResetCycle != stockLastResetCycle) {
                stockLastResetCycle = currentResetCycle;
                setDirty();
                return true;
            }
        } else {
            if (currentResetCycle > stockLastResetCycle) {
                stockLastResetCycle = currentResetCycle;
                setDirty();
                return true;
            }
        }
        return false;
    }
}