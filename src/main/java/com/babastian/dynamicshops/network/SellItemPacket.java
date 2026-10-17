package com.babastian.dynamicshops.network;

import com.babastian.dynamicshops.DynamicShops;
import com.babastian.dynamicshops.blocks.SellBlockEntity;
import com.babastian.dynamicshops.money.MoneyComponent;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public record SellItemPacket(
        BlockPos pos,
        int amount,
        int totalPrice
) implements CustomPacketPayload {

    public static final Type<SellItemPacket> TYPE =
            new Type<>(
                    Identifier.fromNamespaceAndPath(
                            DynamicShops.MOD_ID,
                            "sell_item"
                    )
            );

    public static final StreamCodec<RegistryFriendlyByteBuf, SellItemPacket> CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC,
                    SellItemPacket::pos,
                    StreamCodec.of(
                            RegistryFriendlyByteBuf::writeInt,
                            RegistryFriendlyByteBuf::readInt
                    ),
                    SellItemPacket::amount,
                    StreamCodec.of(
                            RegistryFriendlyByteBuf::writeInt,
                            RegistryFriendlyByteBuf::readInt
                    ),
                    SellItemPacket::totalPrice,
                    SellItemPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(
            ServerPlayer player,
            SellItemPacket packet
    ) {
        if (player.level() == null) {
            return;
        }

        BlockEntity blockEntity = player.level().getBlockEntity(packet.pos());

        if (!(blockEntity instanceof SellBlockEntity sellBlockEntity)) {
            return;
        }

        // Check y reset de stock global antes de validar
        sellBlockEntity.checkAndResetStock();

        ItemStack sellItem = sellBlockEntity.getSellItem();

        if (sellItem.isEmpty()) {
            return;
        }

        int playerAmount = countItemInInventory(player, sellItem);

        if (playerAmount < packet.amount()) {
            return;
        }

        int maxStock = sellBlockEntity.getMaxStock();
        boolean hasLimitedStock = sellBlockEntity.hasLimitedStock();
        int soldCount = sellBlockEntity.getSoldCount();
        int availableStock = hasLimitedStock ? (maxStock - soldCount) : Integer.MAX_VALUE;

        if (availableStock < packet.amount()) {
            return;
        }

        if (!removeItemsFromInventory(player, sellItem, packet.amount())) {
            return;
        }

        MoneyComponent.addMoney(player, packet.totalPrice());

        sellBlockEntity.setSoldCount(soldCount + packet.amount());
        sellBlockEntity.setChanged();
    }

    private static int countItemInInventory(ServerPlayer player, ItemStack targetItem) {
        int count = 0;

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && ItemStack.isSameItemSameComponents(targetItem, stack)) {
                count += stack.getCount();
            }
        }

        return count;
    }

    private static boolean removeItemsFromInventory(ServerPlayer player, ItemStack targetItem, int amount) {
        int remaining = amount;

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            if (remaining <= 0) {
                break;
            }

            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && ItemStack.isSameItemSameComponents(targetItem, stack)) {
                int removeCount = Math.min(stack.getCount(), remaining);
                stack.shrink(removeCount);
                remaining -= removeCount;
            }
        }

        if (remaining > 0) {
            return false;
        }

        player.getInventory().setChanged();
        return true;
    }
}