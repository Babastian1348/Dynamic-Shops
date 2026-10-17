package com.babastian.dynamicshops.tasks;

import com.babastian.dynamicshops.config.TaskConfig;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.ElderGuardian;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

public class TaskListeners {

    private static final Map<ServerPlayer, Vec3> LAST_POSITIONS = new WeakHashMap<>();

    // Caches para la lista negra (Evita lag por recreación de Identifier/TagKey en cada bloque roto)
    private static final Set<String> BLACKLISTED_BLOCK_IDS = new HashSet<>();
    private static final List<TagKey<Block>> BLACKLISTED_TAGS = new ArrayList<>();

    public static void register() {
        // Pre-procesar la blacklist una sola vez
        cacheBlacklist();

        // 1. Minería y Agricultura
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, entity) -> {
            if (!(player instanceof ServerPlayer serverPlayer)) return;
            // No contar en modo creativo o espectador
            if (serverPlayer.isCreative() || serverPlayer.isSpectator()) return;
            if (state == null) return;

            // Verificar lista negra con la caché ultra-rápida
            if (isBlacklisted(state)) return;

            String blockId = BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();

            if (state.is(BlockTags.LOGS) || TaskConfig.getInstance().isLog(blockId)) {
                TaskTracker.incrementAndCheck(serverPlayer, TaskTracker.LOGS, 1, "task.logs");
            }
            else if (TaskConfig.getInstance().isOre(blockId)) {
                TaskTracker.incrementAndCheck(serverPlayer, TaskTracker.ORES, 1, "task.ores");
            }
            else if (state.getBlock() instanceof CropBlock || state.is(BlockTags.CROPS)) {
                TaskTracker.incrementAndCheck(serverPlayer, TaskTracker.CROPS, 1, "task.crops");
            }
            else {
                TaskTracker.incrementAndCheck(serverPlayer, TaskTracker.COMMON_BLOCKS, 1, "task.common_blocks");
            }
        });

        // 2. Combate
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
            if (!(source.getEntity() instanceof ServerPlayer player)) return;
            if (player.isCreative() || player.isSpectator()) return;
            if (!(entity instanceof Mob mob)) return;

            double maxHealth = mob.getMaxHealth();

            if (mob instanceof EnderDragon || mob instanceof WitherBoss ||
                    mob instanceof ElderGuardian || maxHealth >= 101) {
                TaskTracker.incrementAndCheck(player, TaskTracker.BOSSES, 1, "task.bosses");
            }
            else if (mob instanceof Enemy) {
                if (maxHealth <= 20) {
                    TaskTracker.incrementAndCheck(player, TaskTracker.WEAK_HOSTILES, 1, "task.weak_hostiles");
                } else {
                    TaskTracker.incrementAndCheck(player, TaskTracker.STRONG_HOSTILES, 1, "task.strong_hostiles");
                }
            }
            else {
                TaskTracker.incrementAndCheck(player, TaskTracker.PASSIVE_MOBS, 1, "task.passive_mobs");
            }
        });

        // 3. Exploración (Servidor Global - Se ejecuta exactamente cada 5 ticks del servidor)
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTickCount() % 5 != 0) return;

            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                if (player.isSpectator()) continue;

                Vec3 currentPos = player.position();
                Vec3 prevPos = LAST_POSITIONS.get(player);

                if (prevPos != null) {
                    // Cálculo de distancia en 2D (Plano Horizontal X, Z) para ignorar el eje Y (saltos/caídas/elevadores)
                    double deltaX = currentPos.x - prevPos.x;
                    double deltaZ = currentPos.z - prevPos.z;
                    double distanceBlocks = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

                    // Ignorar movimientos micro (menos de 0.1 bloques) y teletransportes (más de 100 bloques)
                    if (distanceBlocks > 0.1 && distanceBlocks <= 100) {
                        int distanceCm = (int)(distanceBlocks * 100);
                        TaskTracker.incrementAndCheck(player, TaskTracker.DISTANCE, distanceCm / 10, "task.distance");
                    }
                }

                LAST_POSITIONS.put(player, currentPos);
            }
        });

        // Limpiar caché de posiciones cuando un jugador se desconecta
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
                LAST_POSITIONS.remove(handler.getPlayer())
        );
    }

    private static void cacheBlacklist() {
        BLACKLISTED_BLOCK_IDS.clear();
        BLACKLISTED_TAGS.clear();

        for (String entry : TaskConfig.getInstance().BlacklistPlacement) {
            if (entry.startsWith("#")) {
                String tagName = entry.substring(1);
                BLACKLISTED_TAGS.add(TagKey.create(Registries.BLOCK, Identifier.parse(tagName)));
            } else {
                BLACKLISTED_BLOCK_IDS.add(entry);
            }
        }
    }

    private static boolean isBlacklisted(BlockState state) {
        String blockId = BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();
        if (BLACKLISTED_BLOCK_IDS.contains(blockId)) return true;

        for (TagKey<Block> tagKey : BLACKLISTED_TAGS) {
            if (state.is(tagKey)) return true;
        }
        return false;
    }
}