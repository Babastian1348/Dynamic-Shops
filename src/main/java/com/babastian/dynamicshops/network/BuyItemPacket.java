package com.babastian.dynamicshops.network;

import com.babastian.dynamicshops.DynamicShops;
import com.babastian.dynamicshops.blocks.ShopBlockEntity;
import com.babastian.dynamicshops.money.MoneyComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public record BuyItemPacket(
        BlockPos pos,
        int amount,
        int totalPrice
) implements CustomPacketPayload {

    public static final Type<BuyItemPacket> TYPE =
            new Type<>(
                    Identifier.fromNamespaceAndPath(
                            DynamicShops.MOD_ID,
                            "buy_item"
                    )
            );

    public static final StreamCodec<RegistryFriendlyByteBuf, BuyItemPacket> CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC,
                    BuyItemPacket::pos,
                    StreamCodec.of(
                            RegistryFriendlyByteBuf::writeInt,
                            RegistryFriendlyByteBuf::readInt
                    ),
                    BuyItemPacket::amount,
                    StreamCodec.of(
                            RegistryFriendlyByteBuf::writeInt,
                            RegistryFriendlyByteBuf::readInt
                    ),
                    BuyItemPacket::totalPrice,
                    BuyItemPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(
            ServerPlayer player,
            BuyItemPacket packet
    ) {
        if (player.level() == null) {
            return;
        }

        BlockEntity blockEntity = player.level().getBlockEntity(packet.pos());

        if (!(blockEntity instanceof ShopBlockEntity shopBlockEntity)) {
            return;
        }

        shopBlockEntity.checkAndResetStock();

        ItemStack buyItem = shopBlockEntity.getBuyItem();

        if (buyItem.isEmpty()) {
            return;
        }

        int playerMoney = MoneyComponent.getBalance(player);

        if (playerMoney < packet.totalPrice()) {
            return;
        }

        int maxStock = shopBlockEntity.getMaxStock();
        boolean hasLimitedStock = shopBlockEntity.hasLimitedStock();
        int soldCount = shopBlockEntity.getSoldCount();
        int availableStock = hasLimitedStock ? (maxStock - soldCount) : Integer.MAX_VALUE;

        if (availableStock < packet.amount()) {
            return;
        }

        // === CONTAR ESPACIO DISPONIBLE (SIN ARMADURA) ===
        int canAddAmount = countAvailableSpace(player, buyItem, packet.amount());

        if (canAddAmount < packet.amount()) {
            return;
        }

        // === Si puede almacenar todo, proceder con la compra ===
        MoneyComponent.removeMoney(player, packet.totalPrice());

        ItemStack itemToGive = buyItem.copy();
        itemToGive.setCount(packet.amount());

        // Añadir items manualmente (sin armadura)
        int addedCount = addItemToInventory(player, itemToGive);

        if (addedCount < packet.amount()) {
            // No se añadieron todos, devolver dinero por los no añadidos
            int notAdded = packet.amount() - addedCount;
            int refund = notAdded * shopBlockEntity.getPrice();
            MoneyComponent.addMoney(player, refund);
            shopBlockEntity.setSoldCount(soldCount + addedCount);

            // === REGISTRAR VENTAS PARA EL COLABORADOR (Solo las unidades entregadas) ===
            for (int i = 0; i < addedCount; i++) {
                shopBlockEntity.recordSale(player);
            }
        } else {
            shopBlockEntity.setSoldCount(soldCount + packet.amount());

            // === REGISTRAR VENTAS PARA EL COLABORADOR ===
            for (int i = 0; i < packet.amount(); i++) {
                shopBlockEntity.recordSale(player);
            }
        }

        shopBlockEntity.setChanged();
    }

    // === HELPER: Cuenta espacio (SIN armadura, SIN offhand) ===
    private static int countAvailableSpace(ServerPlayer player, ItemStack template, int desiredAmount) {
        int remaining = desiredAmount;

        // Solo inventario principal (0-35)
        for (int i = 0; i < 36; i++) {
            if (remaining <= 0) {
                break;
            }

            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && ItemStack.isSameItemSameComponents(template, stack)) {
                int canAdd = Math.min(stack.getMaxStackSize() - stack.getCount(), remaining);
                if (canAdd > 0) {
                    remaining -= canAdd;
                }
            }
        }

        // Slots vacíos en inventario principal
        if (remaining > 0) {
            for (int i = 0; i < 36; i++) {
                if (remaining <= 0) {
                    break;
                }

                ItemStack stack = player.getInventory().getItem(i);
                if (stack.isEmpty()) {
                    int canAdd = Math.min(template.getMaxStackSize(), remaining);
                    remaining -= canAdd;
                }
            }
        }

        return desiredAmount - remaining;
    }

    // === HELPER: Añade items (SIN armadura, SIN offhand) ===
    private static int addItemToInventory(ServerPlayer player, ItemStack stack) {
        int originalCount = stack.getCount();
        int remaining = originalCount;

        // Inventario principal (0-35)
        for (int i = 0; i < 36; i++) {
            if (remaining <= 0) {
                break;
            }

            ItemStack existingStack = player.getInventory().getItem(i);
            if (!existingStack.isEmpty() && ItemStack.isSameItemSameComponents(stack, existingStack)) {
                int canAdd = Math.min(existingStack.getMaxStackSize() - existingStack.getCount(), remaining);
                if (canAdd > 0) {
                    existingStack.grow(canAdd);
                    remaining -= canAdd;
                }
            }
        }

        // Slots vacíos en inventario principal
        if (remaining > 0) {
            for (int i = 0; i < 36; i++) {
                if (remaining <= 0) {
                    break;
                }

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

        // Si aún queda, dropar
        if (remaining > 0) {
            ItemStack leftover = stack.copy();
            leftover.setCount(remaining);
            player.drop(leftover, false);
        }

        return originalCount - remaining;
    }
}