package com.babastian.dynamicshops.money;

import com.babastian.dynamicshops.DynamicShops;
import com.babastian.dynamicshops.network.BankUpdatePacket;
import com.babastian.dynamicshops.network.MoneyUpdatePacket;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import com.mojang.serialization.Codec;

public class MoneyComponent {
    public static final int MAX_BALANCE = 999_999_999;
// -------------------------------------------------------------------
// DINERO DEL INVENTARIO (BOLSILLO)
// -------------------------------------------------------------------
    public static final AttachmentType<Integer> MONEY_BALANCE =
            AttachmentRegistry.create(
                    Identifier.fromNamespaceAndPath(DynamicShops.MOD_ID, "money_balance"),
                    builder -> builder.initializer(() -> 100).persistent(Codec.INT)
            );

    public static int getBalance(Player player) {
        if (player == null) return 0;
        Integer balance = player.getAttached(MONEY_BALANCE);
        return balance != null ? balance : 100;
    }

    public static void setBalance(Player player, int amount) {
        if (player == null) return;
        amount = Math.min(amount, MAX_BALANCE);
        amount = Math.max(amount, 0);
        player.setAttached(MONEY_BALANCE, amount);

        if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            ServerPlayNetworking.send(serverPlayer, new MoneyUpdatePacket(amount));
        }
    }

    public static void addMoney(Player player, int amount) {
        int current = getBalance(player);
        if (current >= MAX_BALANCE) return;
        setBalance(player, current + amount);
    }

    public static void removeMoney(Player player, int amount) {
        int current = getBalance(player);
        setBalance(player, Math.max(0, current - amount));
    }

    public static boolean hasEnough(Player player, int amount) {
        return getBalance(player) >= amount;
    }

    public static boolean isAtMax(Player player) {
        return getBalance(player) >= MAX_BALANCE;
    }
// -------------------------------------------------------------------
// DINERO DEL BANCO (ATM)
// -------------------------------------------------------------------
    public static final AttachmentType<Integer> BANK_BALANCE =
            AttachmentRegistry.create(
                    Identifier.fromNamespaceAndPath(DynamicShops.MOD_ID, "bank_balance"),
                    builder -> builder.initializer(() -> 0).persistent(Codec.INT)
            );

    public static int getBankBalance(Player player) {
        if (player == null) return 0;
        Integer balance = player.getAttached(BANK_BALANCE);
        return balance != null ? balance : 0;
    }

    public static void setBankBalance(Player player, int amount) {
        if (player == null) return;
        amount = Math.min(amount, MAX_BALANCE);
        amount = Math.max(amount, 0);
        player.setAttached(BANK_BALANCE, amount);

        if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            ServerPlayNetworking.send(serverPlayer, new BankUpdatePacket(amount));
        }
    }

    public static void addBankMoney(Player player, int amount) {
        int current = getBankBalance(player);
        if (current >= MAX_BALANCE) return;
        setBankBalance(player, current + amount);
    }

    public static void removeBankMoney(Player player, int amount) {
        int current = getBankBalance(player);
        setBankBalance(player, Math.max(0, current - amount));
    }

    public static void register() {
    }
}

