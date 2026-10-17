package com.babastian.dynamicshops.commands;

import com.babastian.dynamicshops.money.MoneyComponent;
import com.babastian.dynamicshops.tasks.TaskTracker;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.List;

public class TaskCommands {

    // Lista de todas las tareas para iterar fácilmente cuando se use "all"
    private static final List<AttachmentType<Integer>> ALL_TASK_TYPES = List.of(
            TaskTracker.COMMON_BLOCKS, TaskTracker.LOGS, TaskTracker.ORES,
            TaskTracker.PASSIVE_MOBS, TaskTracker.WEAK_HOSTILES, TaskTracker.STRONG_HOSTILES,
            TaskTracker.BOSSES, TaskTracker.CROPS, TaskTracker.FISH,
            TaskTracker.DISTANCE, TaskTracker.ADVANCEMENTS, TaskTracker.TRADES,
            TaskTracker.BLOCKS_PLACED, TaskTracker.COOKING
    );

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(Commands.literal("task")
                    // /task complete <jugador> <task> - Solo operadores y consola
                    .then(Commands.literal("complete")
                            .requires(source -> {
                                if (source.getEntity() == null) {
                                    return true; // Permite consola, RCON y bloques de comandos
                                }
                                if (source.getEntity() instanceof ServerPlayer player) {
                                    return source.getServer().getPlayerList().isOp(player.nameAndId());
                                }
                                return false;
                            })
                            .then(Commands.argument("jugador", EntityArgument.players())
                                    .then(Commands.argument("task", StringArgumentType.string())
                                            .suggests((context, builder) -> {
                                                String input = builder.getRemaining().toLowerCase();

                                                String[] tasks = {
                                                        "common_blocks", "logs", "ores", "passive_mobs",
                                                        "weak_hostiles", "strong_hostiles", "bosses",
                                                        "crops", "fish", "distance", "advancements",
                                                        "trades", "blocks_placed", "cooking"
                                                };

                                                for (String task : tasks) {
                                                    if (task.toLowerCase().startsWith(input)) {
                                                        builder.suggest(task);
                                                    }
                                                }

                                                return builder.buildFuture();
                                            })
                                            .executes(context -> {
                                                String taskName = StringArgumentType.getString(context, "task");
                                                Collection<ServerPlayer> targets = EntityArgument.getPlayers(context, "jugador");

                                                AttachmentType<Integer> taskType = getTaskTypeByName(taskName);
                                                if (taskType == null) {
                                                    context.getSource().sendSuccess(
                                                            () -> Component.translatable("command.task.error", taskName).withStyle(net.minecraft.network.chat.Style.EMPTY.withColor(0xFF0000)),
                                                            false
                                                    );
                                                    return 0;
                                                }

                                                int total = 0;
                                                for (ServerPlayer player : targets) {
                                                    int reward = TaskTracker.getRewardForTask(taskType);
                                                    MoneyComponent.addMoney(player, reward);
                                                    player.setAttached(taskType, 0);

                                                    context.getSource().sendSuccess(
                                                            () -> Component.translatable("command.task.complete.success", taskName, player.getDisplayName().getString(), reward),
                                                            true
                                                    );
                                                    total += reward;
                                                }

                                                return total;
                                            })
                                    )
                            )
                    )
                    // /task progress <jugador> <task> - Disponible para todos
                    .then(Commands.literal("progress")
                            .then(Commands.argument("jugador", EntityArgument.player())
                                    .then(Commands.argument("task", StringArgumentType.string())
                                            .suggests((context, builder) -> {
                                                String input = builder.getRemaining().toLowerCase();

                                                String[] tasks = {
                                                        "common_blocks", "logs", "ores", "passive_mobs",
                                                        "weak_hostiles", "strong_hostiles", "bosses",
                                                        "crops", "fish", "distance", "advancements",
                                                        "trades", "blocks_placed", "cooking"
                                                };

                                                for (String task : tasks) {
                                                    if (task.toLowerCase().startsWith(input)) {
                                                        builder.suggest(task);
                                                    }
                                                }

                                                return builder.buildFuture();
                                            })
                                            .executes(context -> {
                                                String taskName = StringArgumentType.getString(context, "task");
                                                ServerPlayer target = EntityArgument.getPlayer(context, "jugador");

                                                AttachmentType<Integer> taskType = getTaskTypeByName(taskName);
                                                if (taskType == null) {
                                                    context.getSource().sendSuccess(
                                                            () -> Component.translatable("command.task.error", taskName).withStyle(net.minecraft.network.chat.Style.EMPTY.withColor(0xFF0000)),
                                                            false
                                                    );
                                                    return 0;
                                                }

                                                int current = target.getAttachedOrCreate(taskType, () -> 0);
                                                int threshold = TaskTracker.getThresholdForTask(taskType);

                                                context.getSource().sendSuccess(
                                                        () -> Component.translatable("command.task.progress", taskName, target.getDisplayName().getString(), current, threshold),
                                                        false
                                                );
                                                return current;
                                            })
                                    )
                            )
                    )
                    // /task clear <jugador> <task|all> - Solo operadores y consola
                    .then(Commands.literal("clear")
                            .requires(source -> {
                                if (source.getEntity() == null) {
                                    return true; // Permite consola, RCON y bloques de comandos
                                }
                                if (source.getEntity() instanceof ServerPlayer player) {
                                    return source.getServer().getPlayerList().isOp(player.nameAndId());
                                }
                                return false;
                            })
                            .then(Commands.argument("jugador", EntityArgument.players())
                                    .then(Commands.argument("task", StringArgumentType.string())
                                            .suggests((context, builder) -> {
                                                String input = builder.getRemaining().toLowerCase();

                                                // Incluye "all" en las opciones
                                                String[] tasks = {
                                                        "all", "common_blocks", "logs", "ores", "passive_mobs",
                                                        "weak_hostiles", "strong_hostiles", "bosses",
                                                        "crops", "fish", "distance", "advancements",
                                                        "trades", "blocks_placed", "cooking"
                                                };

                                                for (String task : tasks) {
                                                    if (task.toLowerCase().startsWith(input)) {
                                                        builder.suggest(task);
                                                    }
                                                }

                                                return builder.buildFuture();
                                            })
                                            .executes(context -> {
                                                String taskName = StringArgumentType.getString(context, "task");
                                                Collection<ServerPlayer> targets = EntityArgument.getPlayers(context, "jugador");

                                                // CASO 1: Si se especifica "all"
                                                if ("all".equalsIgnoreCase(taskName)) {
                                                    int count = 0;
                                                    for (ServerPlayer player : targets) {
                                                        for (AttachmentType<Integer> taskType : ALL_TASK_TYPES) {
                                                            player.setAttached(taskType, 0);
                                                        }
                                                        context.getSource().sendSuccess(
                                                                () -> Component.translatable("command.task.clear.all.success", player.getDisplayName().getString()),
                                                                true
                                                        );
                                                        count++;
                                                    }
                                                    return count;
                                                }

                                                // CASO 2: Si se especifica una tarea individual
                                                AttachmentType<Integer> taskType = getTaskTypeByName(taskName);
                                                if (taskType == null) {
                                                    context.getSource().sendSuccess(
                                                            () -> Component.translatable("command.task.error", taskName).withStyle(net.minecraft.network.chat.Style.EMPTY.withColor(0xFF0000)),
                                                            false
                                                    );
                                                    return 0;
                                                }

                                                int count = 0;
                                                for (ServerPlayer player : targets) {
                                                    player.setAttached(taskType, 0);
                                                    context.getSource().sendSuccess(
                                                            () -> Component.translatable("command.task.clear.success", taskName, player.getDisplayName().getString()),
                                                            true
                                                    );
                                                    count++;
                                                }

                                                return count;
                                            })
                                    )
                            )
                    )
            );
        });
    }

    private static AttachmentType<Integer> getTaskTypeByName(String name) {
        switch (name) {
            case "common_blocks": return TaskTracker.COMMON_BLOCKS;
            case "logs": return TaskTracker.LOGS;
            case "ores": return TaskTracker.ORES;
            case "passive_mobs": return TaskTracker.PASSIVE_MOBS;
            case "weak_hostiles": return TaskTracker.WEAK_HOSTILES;
            case "strong_hostiles": return TaskTracker.STRONG_HOSTILES;
            case "bosses": return TaskTracker.BOSSES;
            case "crops": return TaskTracker.CROPS;
            case "fish": return TaskTracker.FISH;
            case "distance": return TaskTracker.DISTANCE;
            case "advancements": return TaskTracker.ADVANCEMENTS;
            case "trades": return TaskTracker.TRADES;
            case "blocks_placed": return TaskTracker.BLOCKS_PLACED;
            case "cooking": return TaskTracker.COOKING;
            default: return null;
        }
    }
}