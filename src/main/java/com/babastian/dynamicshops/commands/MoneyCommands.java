package com.babastian.dynamicshops.commands;

import com.babastian.dynamicshops.money.MoneyComponent;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

import java.util.Collection;

public class MoneyCommands {

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(Commands.literal("money")
                    // /money balance [jugador] - Disponible para todos
                    .then(Commands.literal("balance")
                            .executes(context -> {
                                ServerPlayer player = context.getSource().getPlayerOrException();
                                int balance = MoneyComponent.getBalance(player);
                                context.getSource().sendSuccess(
                                        () -> Component.translatable("command.money.balance.self", balance),
                                        false
                                );
                                return balance;
                            })
                            .then(Commands.argument("jugador", EntityArgument.player())
                                    .executes(context -> {
                                        ServerPlayer target = EntityArgument.getPlayer(context, "jugador");
                                        int balance = MoneyComponent.getBalance(target);
                                        context.getSource().sendSuccess(
                                                () -> Component.translatable("command.money.balance.other", target.getDisplayName().getString(), balance),
                                                false
                                        );
                                        return balance;
                                    })
                            )
                    )
                    // /money add <jugador> <cantidad> - Solo operadores y consola
                    .then(Commands.literal("add")
                            .requires(source -> {
                                if (source.getEntity() == null) {
                                    return true; // Permite la consola, RCON y bloques de comando
                                }
                                if (source.getEntity() instanceof ServerPlayer player) {
                                    return source.getServer().getPlayerList().isOp(player.nameAndId());
                                }
                                return false;
                            })
                            .then(Commands.argument("jugador", EntityArgument.players())
                                    .then(Commands.argument("cantidad", IntegerArgumentType.integer(1))
                                            .executes(context -> {
                                                Collection<ServerPlayer> targets = EntityArgument.getPlayers(context, "jugador");
                                                int amount = IntegerArgumentType.getInteger(context, "cantidad");
                                                int total = 0;

                                                for (ServerPlayer player : targets) {
                                                    MoneyComponent.addMoney(player, amount);
                                                    int newBalance = MoneyComponent.getBalance(player);
                                                    context.getSource().sendSuccess(
                                                            () -> Component.translatable("command.money.add.success", amount, player.getDisplayName().getString(), newBalance),
                                                            true
                                                    );
                                                    total += amount;
                                                }

                                                return total;
                                            })
                                    )
                            )
                    )
                    // /money set <jugador> <cantidad> - Solo operadores y consola
                    .then(Commands.literal("set")
                            .requires(source -> {
                                if (source.getEntity() == null) {
                                    return true; // Permite la consola, RCON y bloques de comando
                                }
                                if (source.getEntity() instanceof ServerPlayer player) {
                                    return source.getServer().getPlayerList().isOp(player.nameAndId());
                                }
                                return false;
                            })
                            .then(Commands.argument("jugador", EntityArgument.players())
                                    .then(Commands.argument("cantidad", IntegerArgumentType.integer(0, 999_999_999))
                                            .executes(context -> {
                                                Collection<ServerPlayer> targets = EntityArgument.getPlayers(context, "jugador");
                                                int amount = IntegerArgumentType.getInteger(context, "cantidad");
                                                int total = 0;

                                                for (ServerPlayer player : targets) {
                                                    MoneyComponent.setBalance(player, amount);
                                                    context.getSource().sendSuccess(
                                                            () -> Component.translatable("command.money.set.success", player.getDisplayName().getString(), amount),
                                                            true
                                                    );
                                                    total += amount;
                                                }

                                                return total;
                                            })
                                    )
                            )
                    )
            );
        });
    }
}