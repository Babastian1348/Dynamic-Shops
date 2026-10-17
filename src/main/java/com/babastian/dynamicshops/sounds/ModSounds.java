package com.babastian.dynamicshops.sounds;

import com.babastian.dynamicshops.DynamicShops;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.resources.Identifier;

public class ModSounds {

    public static final SoundEvent BUY = register("buy");
    public static final SoundEvent SELL = register("sell");
    public static final SoundEvent ERROR = register("error");
    public static final SoundEvent COIN = register("singlecoin");
    public static final SoundEvent MULTICOIN = register("groupcoin");
    public static final SoundEvent PHYSICAL_WITHDRAW = register("physicalwithdraw");
    public static final SoundEvent WITHDRAW = register("withdraw");
    public static final SoundEvent DEPOSIT = register("deposit");
    public static final SoundEvent INTEREST = register("interest");
    public static final SoundEvent TASK_COMPLETED = register("taskcomplete");


    private static SoundEvent register(String name) {
        Identifier id = Identifier.fromNamespaceAndPath(DynamicShops.MOD_ID, name);

        SoundEvent soundEvent = SoundEvent.createVariableRangeEvent(id);

        return Registry.register(
                BuiltInRegistries.SOUND_EVENT,
                id,
                soundEvent
        );
    }

    public static void register() {
        // Los sonidos se registran automáticamente al cargar la clase
    }
}