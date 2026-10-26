package com.babastian.dynamicshops.commands;

import com.babastian.dynamicshops.money.MoneyComponent;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;

public class MoneyCommands {

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(Commands.literal("money")

                    .then(Commands.literal("balance")
                            .executes(context -> {
                                ServerPlayer player = context.getSource().getPlayerOrException();
                                int balance = MoneyComponent.getBalance(player);
                                context.getSource().sendSuccess(
                                        () -> Component.translatable("command.money.balance.pocket.self", balance),
                                        false
                                );
                                return balance;
                            })
                            .then(Commands.literal("pocket")
                                    .executes(context -> {
                                        ServerPlayer player = context.getSource().getPlayerOrException();
                                        int balance = MoneyComponent.getBalance(player);
                                        context.getSource().sendSuccess(
                                                () -> Component.translatable("command.money.balance.pocket.self", balance),
                                                false
                                        );
                                        return balance;
                                    })
                                    .then(Commands.argument("player", EntityArgument.player())
                                            .executes(context -> {
                                                ServerPlayer target = EntityArgument.getPlayer(context, "player");
                                                int balance = MoneyComponent.getBalance(target);
                                                context.getSource().sendSuccess(
                                                        () -> Component.translatable("command.money.balance.pocket.other", target.getDisplayName().getString(), balance),
                                                        false
                                                );
                                                return balance;
                                            })
                                    )
                            )
                            .then(Commands.literal("bank")
                                    .executes(context -> {
                                        ServerPlayer player = context.getSource().getPlayerOrException();
                                        int bankBalance = MoneyComponent.getBankBalance(player);
                                        context.getSource().sendSuccess(
                                                () -> Component.translatable("command.money.balance.bank.self", bankBalance),
                                                false
                                        );
                                        return bankBalance;
                                    })
                                    .then(Commands.argument("player", EntityArgument.player())
                                            .executes(context -> {
                                                ServerPlayer target = EntityArgument.getPlayer(context, "player");
                                                int bankBalance = MoneyComponent.getBankBalance(target);
                                                context.getSource().sendSuccess(
                                                        () -> Component.translatable("command.money.balance.bank.other", target.getDisplayName().getString(), bankBalance),
                                                        false
                                                );
                                                return bankBalance;
                                            })
                                    )
                            )
                    )

                    .then(Commands.literal("add")
                            .requires(MoneyCommands::isOpOrConsole)
                            .then(Commands.literal("pocket")
                                    .then(Commands.argument("players", EntityArgument.players())
                                            .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                                    .executes(context -> {
                                                        Collection<ServerPlayer> targets = EntityArgument.getPlayers(context, "players");
                                                        int amount = IntegerArgumentType.getInteger(context, "amount");
                                                        int total = 0;

                                                        for (ServerPlayer player : targets) {
                                                            MoneyComponent.addMoney(player, amount);
                                                            int newBalance = MoneyComponent.getBalance(player);
                                                            context.getSource().sendSuccess(
                                                                    () -> Component.translatable("command.money.add.pocket.success", amount, player.getDisplayName().getString(), newBalance),
                                                                    true
                                                            );
                                                            total += amount;
                                                        }
                                                        return total;
                                                    })
                                            )
                                    )
                            )
                            .then(Commands.literal("bank")
                                    .then(Commands.argument("players", EntityArgument.players())
                                            .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                                    .executes(context -> {
                                                        Collection<ServerPlayer> targets = EntityArgument.getPlayers(context, "players");
                                                        int amount = IntegerArgumentType.getInteger(context, "amount");
                                                        int total = 0;

                                                        for (ServerPlayer player : targets) {
                                                            MoneyComponent.addBankMoney(player, amount);
                                                            int newBalance = MoneyComponent.getBankBalance(player);
                                                            context.getSource().sendSuccess(
                                                                    () -> Component.translatable("command.money.add.bank.success", amount, player.getDisplayName().getString(), newBalance),
                                                                    true
                                                            );
                                                            total += amount;
                                                        }
                                                        return total;
                                                    })
                                            )
                                    )
                            )
                    )

                    .then(Commands.literal("subtract")
                            .requires(MoneyCommands::isOpOrConsole)
                            .then(Commands.literal("pocket")
                                    .then(Commands.argument("players", EntityArgument.players())
                                            .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                                    .executes(context -> {
                                                        Collection<ServerPlayer> targets = EntityArgument.getPlayers(context, "players");
                                                        int amount = IntegerArgumentType.getInteger(context, "amount");
                                                        int total = 0;

                                                        for (ServerPlayer player : targets) {
                                                            MoneyComponent.removeMoney(player, amount);
                                                            int newBalance = MoneyComponent.getBalance(player);
                                                            context.getSource().sendSuccess(
                                                                    () -> Component.translatable("command.money.subtract.pocket.success", amount, player.getDisplayName().getString(), newBalance),
                                                                    true
                                                            );
                                                            total += amount;
                                                        }
                                                        return total;
                                                    })
                                            )
                                    )
                            )
                            .then(Commands.literal("bank")
                                    .then(Commands.argument("players", EntityArgument.players())
                                            .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                                    .executes(context -> {
                                                        Collection<ServerPlayer> targets = EntityArgument.getPlayers(context, "players");
                                                        int amount = IntegerArgumentType.getInteger(context, "amount");
                                                        int total = 0;

                                                        for (ServerPlayer player : targets) {
                                                            MoneyComponent.removeBankMoney(player, amount);
                                                            int newBalance = MoneyComponent.getBankBalance(player);
                                                            context.getSource().sendSuccess(
                                                                    () -> Component.translatable("command.money.subtract.bank.success", amount, player.getDisplayName().getString(), newBalance),
                                                                    true
                                                            );
                                                            total += amount;
                                                        }
                                                        return total;
                                                    })
                                            )
                                    )
                            )
                    )

                    .then(Commands.literal("set")
                            .requires(MoneyCommands::isOpOrConsole)
                            .then(Commands.literal("pocket")
                                    .then(Commands.argument("players", EntityArgument.players())
                                            .then(Commands.argument("amount", IntegerArgumentType.integer(MoneyComponent.POCKET_MIN_BALANCE, MoneyComponent.MAX_BALANCE))
                                                    .executes(context -> {
                                                        Collection<ServerPlayer> targets = EntityArgument.getPlayers(context, "players");
                                                        int amount = IntegerArgumentType.getInteger(context, "amount");
                                                        int total = 0;

                                                        for (ServerPlayer player : targets) {
                                                            MoneyComponent.setBalance(player, amount);
                                                            context.getSource().sendSuccess(
                                                                    () -> Component.translatable("command.money.set.pocket.success", player.getDisplayName().getString(), amount),
                                                                    true
                                                            );
                                                            total += amount;
                                                        }
                                                        return total;
                                                    })
                                            )
                                    )
                            )
                            .then(Commands.literal("bank")
                                    .then(Commands.argument("players", EntityArgument.players())
                                            .then(Commands.argument("amount", IntegerArgumentType.integer(MoneyComponent.BANK_MIN_BALANCE, MoneyComponent.MAX_BALANCE))
                                                    .executes(context -> {
                                                        Collection<ServerPlayer> targets = EntityArgument.getPlayers(context, "players");
                                                        int amount = IntegerArgumentType.getInteger(context, "amount");
                                                        int total = 0;

                                                        for (ServerPlayer player : targets) {
                                                            MoneyComponent.setBankBalance(player, amount);
                                                            context.getSource().sendSuccess(
                                                                    () -> Component.translatable("command.money.set.bank.success", player.getDisplayName().getString(), amount),
                                                                    true
                                                            );
                                                            total += amount;
                                                        }
                                                        return total;
                                                    })
                                            )
                                    )
                            )
                    )
            );
        });
    }

    private static boolean isOpOrConsole(CommandSourceStack source) {
        if (source.getEntity() == null) {
            return true;
        }
        if (source.getEntity() instanceof ServerPlayer player) {
            return source.getServer().getPlayerList().isOp(player.nameAndId());
        }
        return false;
    }
}