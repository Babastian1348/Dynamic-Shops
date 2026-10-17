package com.babastian.dynamicshops.network;

import com.babastian.dynamicshops.DynamicShops;
import com.babastian.dynamicshops.items.ModItems;
import com.babastian.dynamicshops.money.MoneyComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.LinkedHashMap;
import java.util.Map;

public record AtmWithdrawPhysicalPacket(
        BlockPos pos,
        int amount
) implements CustomPacketPayload {

    public static final Type<AtmWithdrawPhysicalPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(DynamicShops.MOD_ID, "atm_withdraw_physical"));

    public static final StreamCodec<RegistryFriendlyByteBuf, AtmWithdrawPhysicalPacket> CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC,
                    AtmWithdrawPhysicalPacket::pos,
                    StreamCodec.of(
                            RegistryFriendlyByteBuf::writeInt,
                            RegistryFriendlyByteBuf::readInt
                    ),
                    AtmWithdrawPhysicalPacket::amount,
                    AtmWithdrawPhysicalPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ServerPlayer player, AtmWithdrawPhysicalPacket packet) {
        if (player.level() == null) return;

        int amountToWithdraw = packet.amount();
        int bankBalance = MoneyComponent.getBankBalance(player);

        // 1. Verificación de seguridad básica
        if (amountToWithdraw <= 0 || bankBalance < amountToWithdraw) {
            return;
        }

        // 2. Calcular el cambio en físico
        Map<Item, Integer> physicalMoney = calculatePhysicalMoney(amountToWithdraw);

        // 3. Verificación de espacio (SIMULACIÓN ANTES DE COBRAR)
        if (!hasEnoughSpaceForMultipleItems(player, physicalMoney)) {
            return; // No hay espacio, cancelamos transacción
        }

        // 4. Descontar del banco (solo si sabemos que hay espacio)
        MoneyComponent.setBankBalance(player, bankBalance - amountToWithdraw);

        // 5. Entregar los ítems al jugador
        for (Map.Entry<Item, Integer> entry : physicalMoney.entrySet()) {
            Item moneyItem = entry.getKey();
            int count = entry.getValue();

            if (count > 0) {
                ItemStack stackToGive = new ItemStack(moneyItem);
                stackToGive.setCount(count);
                addItemToInventory(player, stackToGive);
            }
        }
    }

    /**
     * Algoritmo Codicioso: Calcula la menor cantidad de billetes/monedas.
     */
    public static Map<Item, Integer> calculatePhysicalMoney(int amount) {
        Map<Item, Integer> result = new LinkedHashMap<>();
        int remaining = amount;

        // Nuevos valores (1, 5, 10, 50...)
        int[] values = {100_000, 50_000, 10_000, 5_000, 1_000, 500, 100, 50, 10, 5, 1};
        Item[] items = {
                ModItems.NETHERITE_BILL,
                ModItems.DIAMOND_BILL,
                ModItems.DIAMOND_COIN,
                ModItems.EMERALD_BILL,
                ModItems.EMERALD_COIN,
                ModItems.GOLD_BILL,
                ModItems.GOLD_COIN,
                ModItems.IRON_BILL,
                ModItems.IRON_COIN,
                ModItems.COPPER_BILL,
                ModItems.COPPER_COIN
        };

        for (int i = 0; i < values.length; i++) {
            if (remaining >= values[i]) {
                int count = remaining / values[i];
                result.put(items[i], count);
                remaining %= values[i];
            }
        }
        return result;
    }

    /**
     * SIMULADOR DE ESPACIO: Verifica si múltiples tipos de ítems caben a la vez.
     */
    public static boolean hasEnoughSpaceForMultipleItems(Player player, Map<Item, Integer> itemsToAdd) {
        if (player == null) return false;

        // 1. Contar slots completamente vacíos (0-35)
        int emptySlots = 0;
        for (int i = 0; i < 36; i++) {
            if (player.getInventory().getItem(i).isEmpty()) {
                emptySlots++;
            }
        }

        // 2. Simular llenar el inventario con cada tipo de billete/moneda
        for (Map.Entry<Item, Integer> entry : itemsToAdd.entrySet()) {
            Item item = entry.getKey();
            int amountNeeded = entry.getValue();
            if (amountNeeded <= 0) continue;

            ItemStack template = new ItemStack(item);
            int maxStack = template.getMaxStackSize();

            // Llenar stacks existentes primero
            for (int i = 0; i < 36; i++) {
                if (amountNeeded <= 0) break;
                ItemStack stack = player.getInventory().getItem(i);

                if (!stack.isEmpty() && ItemStack.isSameItemSameComponents(template, stack)) {
                    int spaceInStack = maxStack - stack.getCount();
                    if (spaceInStack > 0) {
                        amountNeeded -= Math.min(amountNeeded, spaceInStack);
                    }
                }
            }

            // Si aún falta meter ítems, calcular cuántos slots vacíos necesitamos
            if (amountNeeded > 0) {
                int stacksNeeded = (int) Math.ceil((double) amountNeeded / maxStack);
                emptySlots -= stacksNeeded;

                // Si nos quedamos en números negativos, no caben
                if (emptySlots < 0) {
                    return false;
                }
            }
        }
        return true;
    }

    // === HELPER: Añade items (SIN armadura, SIN offhand) y dropea el sobrante ===
    private static int addItemToInventory(ServerPlayer player, ItemStack stack) {
        int originalCount = stack.getCount();
        int remaining = originalCount;

        for (int i = 0; i < 36; i++) {
            if (remaining <= 0) break;
            ItemStack existingStack = player.getInventory().getItem(i);
            if (!existingStack.isEmpty() && ItemStack.isSameItemSameComponents(stack, existingStack)) {
                int canAdd = Math.min(existingStack.getMaxStackSize() - existingStack.getCount(), remaining);
                if (canAdd > 0) {
                    existingStack.grow(canAdd);
                    remaining -= canAdd;
                }
            }
        }

        if (remaining > 0) {
            for (int i = 0; i < 36; i++) {
                if (remaining <= 0) break;
                ItemStack existingStack = player.getInventory().getItem(i);
                if (existingStack.isEmpty()) {
                    int canAdd = Math.min(stack.getMaxStackSize(), remaining);
                    ItemStack newStack = stack.copy();
                    newStack.setCount(canAdd);
                    player.getInventory().setItem(i, newStack);
                    remaining -= canAdd;
                }
            }
        }

        if (remaining > 0) {
            ItemStack leftover = stack.copy();
            leftover.setCount(remaining);
            player.drop(leftover, false);
        }

        return originalCount - remaining;
    }
}