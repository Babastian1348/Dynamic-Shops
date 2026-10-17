package com.babastian.dynamicshops.network;

import com.babastian.dynamicshops.DynamicShops;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record BankUpdatePacket(
        int newBankBalance
) implements CustomPacketPayload {

    public static final Type<BankUpdatePacket> TYPE =
            new Type<>(
                    Identifier.fromNamespaceAndPath(
                            DynamicShops.MOD_ID,
                            "bank_update"
                    )
            );

    public static final StreamCodec<RegistryFriendlyByteBuf, BankUpdatePacket> CODEC =
            StreamCodec.composite(
                    StreamCodec.of(
                            RegistryFriendlyByteBuf::writeInt,
                            RegistryFriendlyByteBuf::readInt
                    ),
                    BankUpdatePacket::newBankBalance,
                    BankUpdatePacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}