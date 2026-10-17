package com.babastian.dynamicshops.network;

import com.babastian.dynamicshops.DynamicShops;
import com.babastian.dynamicshops.blocks.ShopBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public record ShopConfigSavePacket(
        BlockPos pos,
        ItemStack buyItem,
        int price,
        int maxStock,
        boolean hasLimitedStock,
        String customName
) implements CustomPacketPayload {

    public static final Type<ShopConfigSavePacket> TYPE =
            new Type<>(
                    Identifier.fromNamespaceAndPath(
                            DynamicShops.MOD_ID,
                            "shop_config_save"
                    )
            );

    public static final StreamCodec<RegistryFriendlyByteBuf, ShopConfigSavePacket> CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC,
                    ShopConfigSavePacket::pos,
                    ItemStack.STREAM_CODEC,
                    ShopConfigSavePacket::buyItem,
                    StreamCodec.of(
                            RegistryFriendlyByteBuf::writeInt,
                            RegistryFriendlyByteBuf::readInt
                    ),
                    ShopConfigSavePacket::price,
                    StreamCodec.of(
                            RegistryFriendlyByteBuf::writeInt,
                            RegistryFriendlyByteBuf::readInt
                    ),
                    ShopConfigSavePacket::maxStock,
                    StreamCodec.of(
                            RegistryFriendlyByteBuf::writeBoolean,
                            RegistryFriendlyByteBuf::readBoolean
                    ),
                    ShopConfigSavePacket::hasLimitedStock,
                    StreamCodec.of(
                            (buf, value) -> buf.writeUtf(value, 64),
                            buf -> buf.readUtf(64)
                    ),
                    ShopConfigSavePacket::customName,
                    ShopConfigSavePacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(
            ServerPlayer player,
            ShopConfigSavePacket packet
    ) {
        if (player.level() == null) {
            return;
        }

        BlockEntity blockEntity = player.level().getBlockEntity(packet.pos());

        if (!(blockEntity instanceof ShopBlockEntity shopBlockEntity)) {
            return;
        }

        boolean isCreative = player.isCreative();
        boolean isOp = player.permissions().hasPermission(
                net.minecraft.server.permissions.Permissions.COMMANDS_ADMIN
        );

        if (!isCreative && !isOp) {
            return;
        }

        if (packet.price() < 1 || packet.price() > 999_999_999) {
            return;
        }

        if (packet.hasLimitedStock() && (packet.maxStock() < 1 || packet.maxStock() > 1024)) {
            return;
        }

        shopBlockEntity.setBuyItem(packet.buyItem());
        shopBlockEntity.setPrice(packet.price());
        shopBlockEntity.setMaxStock(packet.maxStock());
        shopBlockEntity.setHasLimitedStock(packet.hasLimitedStock());
        shopBlockEntity.setCustomName(packet.customName());

        shopBlockEntity.setChanged();
    }
}