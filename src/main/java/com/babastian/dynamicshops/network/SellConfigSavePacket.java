package com.babastian.dynamicshops.network;

import com.babastian.dynamicshops.DynamicShops;
import com.babastian.dynamicshops.blocks.SellBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public record SellConfigSavePacket(
        BlockPos pos,
        ItemStack sellItem,
        int price,
        int maxStock,
        boolean hasLimitedStock,
        String customName // NUEVO
) implements CustomPacketPayload {

    public static final Type<SellConfigSavePacket> TYPE =
            new Type<>(
                    Identifier.fromNamespaceAndPath(
                            DynamicShops.MOD_ID,
                            "sell_config_save"
                    )
            );

    public static final StreamCodec<RegistryFriendlyByteBuf, SellConfigSavePacket> CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC,
                    SellConfigSavePacket::pos,
                    ItemStack.STREAM_CODEC,
                    SellConfigSavePacket::sellItem,
                    StreamCodec.of(
                            RegistryFriendlyByteBuf::writeInt,
                            RegistryFriendlyByteBuf::readInt
                    ),
                    SellConfigSavePacket::price,
                    StreamCodec.of(
                            RegistryFriendlyByteBuf::writeInt,
                            RegistryFriendlyByteBuf::readInt
                    ),
                    SellConfigSavePacket::maxStock,
                    StreamCodec.of(
                            RegistryFriendlyByteBuf::writeBoolean,
                            RegistryFriendlyByteBuf::readBoolean
                    ),
                    SellConfigSavePacket::hasLimitedStock,
                    StreamCodec.of(
                            (buf, value) -> buf.writeUtf(value, 64),
                            buf -> buf.readUtf(64)
                    ),
                    SellConfigSavePacket::customName, // NUEVO
                    SellConfigSavePacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(
            ServerPlayer player,
            SellConfigSavePacket packet
    ) {
        if (player.level() == null) {
            return;
        }

        BlockEntity blockEntity = player.level().getBlockEntity(packet.pos());

        if (!(blockEntity instanceof SellBlockEntity sellBlockEntity)) {
            return;
        }

        // Validar permisos (creativo o OP)
        boolean isCreative = player.isCreative();
        boolean isOp = player.permissions().hasPermission(
                net.minecraft.server.permissions.Permissions.COMMANDS_ADMIN
        );

        if (!isCreative && !isOp) {
            return;
        }

        // Validar rangos
        if (packet.price() < 1 || packet.price() > 999_999_999) {
            return;
        }

        if (packet.hasLimitedStock() && (packet.maxStock() < 1 || packet.maxStock() > 1024)) {
            return;
        }

        // Guardar datos en el servidor
        sellBlockEntity.setSellItem(packet.sellItem());
        sellBlockEntity.setPrice(packet.price());
        sellBlockEntity.setMaxStock(packet.maxStock());
        sellBlockEntity.setHasLimitedStock(packet.hasLimitedStock());
        sellBlockEntity.setCustomName(packet.customName()); // NUEVO

        // Marcar como cambiado para sincronizar con el cliente
        sellBlockEntity.setChanged();
    }
}