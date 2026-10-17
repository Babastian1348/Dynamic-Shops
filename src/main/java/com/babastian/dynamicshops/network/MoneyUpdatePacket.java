package com.babastian.dynamicshops.network;

import com.babastian.dynamicshops.DynamicShops;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record MoneyUpdatePacket(
        int newBalance
) implements CustomPacketPayload {

    public static final Type<MoneyUpdatePacket> TYPE =
            new Type<>(
                    Identifier.fromNamespaceAndPath(
                            DynamicShops.MOD_ID,
                            "money_update"
                    )
            );

    public static final StreamCodec<RegistryFriendlyByteBuf, MoneyUpdatePacket> CODEC =
            StreamCodec.composite(
                    StreamCodec.of(
                            RegistryFriendlyByteBuf::writeInt,
                            RegistryFriendlyByteBuf::readInt
                    ),
                    MoneyUpdatePacket::newBalance,
                    MoneyUpdatePacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}