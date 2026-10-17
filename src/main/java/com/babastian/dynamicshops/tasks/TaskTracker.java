package com.babastian.dynamicshops.tasks;

import com.babastian.dynamicshops.sounds.ModSounds;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.core.registries.BuiltInRegistries;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import com.babastian.dynamicshops.DynamicShops;
import com.babastian.dynamicshops.money.MoneyComponent;
import com.babastian.dynamicshops.config.TaskConfig;

import java.text.NumberFormat;
import java.util.Locale;

public class TaskTracker {

    // Tipos de adjuntos para cada task
    public static final AttachmentType<Integer> COMMON_BLOCKS = AttachmentRegistry.createPersistent(
            DynamicShops.id("common_blocks"), com.mojang.serialization.Codec.INT);
    public static final AttachmentType<Integer> LOGS = AttachmentRegistry.createPersistent(
            DynamicShops.id("logs"), com.mojang.serialization.Codec.INT);
    public static final AttachmentType<Integer> ORES = AttachmentRegistry.createPersistent(
            DynamicShops.id("ores"), com.mojang.serialization.Codec.INT);

    public static final AttachmentType<Integer> PASSIVE_MOBS = AttachmentRegistry.createPersistent(
            DynamicShops.id("passive_mobs"), com.mojang.serialization.Codec.INT);
    public static final AttachmentType<Integer> WEAK_HOSTILES = AttachmentRegistry.createPersistent(
            DynamicShops.id("weak_hostiles"), com.mojang.serialization.Codec.INT);
    public static final AttachmentType<Integer> STRONG_HOSTILES = AttachmentRegistry.createPersistent(
            DynamicShops.id("strong_hostiles"), com.mojang.serialization.Codec.INT);
    public static final AttachmentType<Integer> BOSSES = AttachmentRegistry.createPersistent(
            DynamicShops.id("bosses"), com.mojang.serialization.Codec.INT);

    public static final AttachmentType<Integer> CROPS = AttachmentRegistry.createPersistent(
            DynamicShops.id("crops"), com.mojang.serialization.Codec.INT);
    public static final AttachmentType<Integer> FISH = AttachmentRegistry.createPersistent(
            DynamicShops.id("fish"), com.mojang.serialization.Codec.INT);

    public static final AttachmentType<Integer> DISTANCE = AttachmentRegistry.createPersistent(
            DynamicShops.id("distance"), com.mojang.serialization.Codec.INT);
    public static final AttachmentType<Integer> ADVANCEMENTS = AttachmentRegistry.createPersistent(
            DynamicShops.id("advancements"), com.mojang.serialization.Codec.INT);
    public static final AttachmentType<Integer> TRADES = AttachmentRegistry.createPersistent(
            DynamicShops.id("trades"), com.mojang.serialization.Codec.INT);
    public static final AttachmentType<Integer> BLOCKS_PLACED = AttachmentRegistry.createPersistent(
            DynamicShops.id("blocks_placed"), com.mojang.serialization.Codec.INT);
    public static final AttachmentType<Integer> COOKING = AttachmentRegistry.createPersistent(
            DynamicShops.id("cooking"), com.mojang.serialization.Codec.INT);

    // Método para incrementar y verificar si se completó la task
    public static void incrementAndCheck(ServerPlayer player, AttachmentType<Integer> taskType, int amount, String translationKey) {
        int current = player.getAttachedOrCreate(taskType, () -> 0);
        int newCount = current + amount;

        int threshold = getThresholdForTask(taskType);
        int reward = getRewardForTask(taskType);

        // Formateador para los números con comas (ej. 1,000)
        NumberFormat format = NumberFormat.getNumberInstance(Locale.US);

        // Dar recompensas MÚLTIPLES veces si hay suficiente progreso
        while (newCount >= threshold) {
            // Verificar si la recompensa excedería el máximo
            int currentMoney = MoneyComponent.getBalance(player);
            int moneyAfterReward = currentMoney + reward;

            if (moneyAfterReward >= MoneyComponent.MAX_BALANCE) {
                // Dar solo lo que falta para llegar al máximo (puede ser 0)
                int rewardAdjusted = MoneyComponent.MAX_BALANCE - currentMoney;

                if (rewardAdjusted > 0) {
                    MoneyComponent.addMoney(player, rewardAdjusted);

                    // Formatear recompensa ajustada y mostrar mensaje
                    String formattedRewardAdjusted = format.format(rewardAdjusted);
                    Component message = Component.translatable(translationKey, formattedRewardAdjusted)
                            .withStyle(Style.EMPTY.withColor(0xFFD700).withBold(Boolean.TRUE));

                    player.connection.send(new ClientboundSetActionBarTextPacket(message));
                    playSoundToPlayer(player);
                }
                // Si rewardAdjusted es 0, no mostrar mensaje ni sonido

                // Salir del loop porque ya llegó al máximo
                newCount -= threshold;
                break;
            } else {
                // Dar recompensa normal
                MoneyComponent.addMoney(player, reward);

                // Formatear recompensa normal y mostrar mensaje
                String formattedReward = format.format(reward);
                Component message = Component.translatable(translationKey, formattedReward)
                        .withStyle(Style.EMPTY.withColor(0xFFD700).withBold(Boolean.TRUE));

                player.connection.send(new ClientboundSetActionBarTextPacket(message));
                playSoundToPlayer(player);

                newCount -= threshold;
            }
        }

        // Guardar el progreso restante
        player.setAttached(taskType, newCount);
    }

    private static void playSoundToPlayer(ServerPlayer player) {
        var packet = new ClientboundSoundPacket(
                BuiltInRegistries.SOUND_EVENT.wrapAsHolder(ModSounds.TASK_COMPLETED),
                SoundSource.PLAYERS,
                player.getX(),
                player.getY(),
                player.getZ(),
                1.0f,
                1.0f,
                player.getRandom().nextLong()
        );
        player.connection.send(packet);
    }

    public static int getThresholdForTask(AttachmentType<Integer> taskType) {
        TaskConfig config = TaskConfig.getInstance();
        if (taskType == COMMON_BLOCKS) return config.commonBlocksAmount;
        if (taskType == LOGS) return config.logsAmount;
        if (taskType == ORES) return config.oresAmount;
        if (taskType == PASSIVE_MOBS) return config.passiveMobsAmount;
        if (taskType == WEAK_HOSTILES) return config.weakHostilesAmount;
        if (taskType == STRONG_HOSTILES) return config.strongHostilesAmount;
        if (taskType == BOSSES) return config.bossesAmount;
        if (taskType == CROPS) return config.cropsAmount;
        if (taskType == FISH) return config.fishAmount;
        if (taskType == DISTANCE) return config.distanceAmount;
        if (taskType == ADVANCEMENTS) return config.advancementsAmount;
        if (taskType == TRADES) return config.tradesAmount;
        if (taskType == BLOCKS_PLACED) return config.blocksPlacedAmount;
        if (taskType == COOKING) return config.cookingAmount;
        return 1;
    }

    public static int getRewardForTask(AttachmentType<Integer> taskType) {
        TaskConfig config = TaskConfig.getInstance();
        if (taskType == COMMON_BLOCKS) return config.commonBlocksReward;
        if (taskType == LOGS) return config.logsReward;
        if (taskType == ORES) return config.oresReward;
        if (taskType == PASSIVE_MOBS) return config.passiveMobsReward;
        if (taskType == WEAK_HOSTILES) return config.weakHostilesReward;
        if (taskType == STRONG_HOSTILES) return config.strongHostilesReward;
        if (taskType == BOSSES) return config.bossesReward;
        if (taskType == CROPS) return config.cropsReward;
        if (taskType == FISH) return config.fishReward;
        if (taskType == DISTANCE) return config.distanceReward;
        if (taskType == ADVANCEMENTS) return config.advancementsReward;
        if (taskType == TRADES) return config.tradesReward;
        if (taskType == BLOCKS_PLACED) return config.blocksPlacedReward;
        if (taskType == COOKING) return config.cookingReward;
        return 0;
    }
}