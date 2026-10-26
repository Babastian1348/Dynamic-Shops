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
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public record ShopConfigSavePacket(
        BlockPos pos,
        ItemStack buyItem,
        int price,
        int maxStock,
        boolean hasLimitedStock,
        String customName,
        @Nullable UUID collaboratorUuid
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
                    StreamCodec.of(RegistryFriendlyByteBuf::writeInt, RegistryFriendlyByteBuf::readInt),
                    ShopConfigSavePacket::price,
                    StreamCodec.of(RegistryFriendlyByteBuf::writeInt, RegistryFriendlyByteBuf::readInt),
                    ShopConfigSavePacket::maxStock,
                    StreamCodec.of(RegistryFriendlyByteBuf::writeBoolean, RegistryFriendlyByteBuf::readBoolean),
                    ShopConfigSavePacket::hasLimitedStock,
                    StreamCodec.of(RegistryFriendlyByteBuf::writeUtf, RegistryFriendlyByteBuf::readUtf),
                    ShopConfigSavePacket::customName,
                    StreamCodec.of(
                            (buf, uuid) -> {
                                boolean hasUuid = uuid != null;
                                buf.writeBoolean(hasUuid);
                                if (hasUuid) buf.writeUUID(uuid);
                            },
                            buf -> buf.readBoolean() ? buf.readUUID() : null
                    ),
                    ShopConfigSavePacket::collaboratorUuid,
                    ShopConfigSavePacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ServerPlayer player, ShopConfigSavePacket packet) {
        if (player.level() == null) return;

        BlockEntity blockEntity = player.level().getBlockEntity(packet.pos());
        if (!(blockEntity instanceof ShopBlockEntity shopBlockEntity)) return;

        shopBlockEntity.setBuyItem(packet.buyItem());
        shopBlockEntity.setPrice(packet.price());
        shopBlockEntity.setMaxStock(packet.maxStock());
        shopBlockEntity.setHasLimitedStock(packet.hasLimitedStock());
        shopBlockEntity.setCustomName(packet.customName());

        shopBlockEntity.setCollaboratorUuid(packet.collaboratorUuid());

        // Corrección: Usamos player.level().getServer() para obtener el servidor de forma segura
        if (packet.collaboratorUuid() != null && player.level().getServer() != null) {
            ServerPlayer collabPlayer = player.level().getServer().getPlayerList().getPlayer(packet.collaboratorUuid());
            if (collabPlayer != null) {
                shopBlockEntity.setCollaboratorName(collabPlayer.getGameProfile().name());
            } else {
                shopBlockEntity.setCollaboratorName("Desconocido");
            }
        } else {
            shopBlockEntity.setCollaboratorName(null);
        }

        shopBlockEntity.setChanged();
        shopBlockEntity.syncToClients();
    }
}