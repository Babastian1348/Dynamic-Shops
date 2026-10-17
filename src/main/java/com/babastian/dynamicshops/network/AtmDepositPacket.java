package com.babastian.dynamicshops.network;

import com.babastian.dynamicshops.DynamicShops;
import com.babastian.dynamicshops.money.MoneyComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

public record AtmDepositPacket(
        BlockPos pos,
        int amount
) implements CustomPacketPayload {

    public static final Type<AtmDepositPacket> TYPE =
            new Type<>(
                    Identifier.fromNamespaceAndPath(
                            DynamicShops.MOD_ID,
                            "atm_deposit"
                    )
            );

    public static final StreamCodec<RegistryFriendlyByteBuf, AtmDepositPacket> CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC,
                    AtmDepositPacket::pos,
                    StreamCodec.of(
                            RegistryFriendlyByteBuf::writeInt,
                            RegistryFriendlyByteBuf::readInt
                    ),
                    AtmDepositPacket::amount,
                    AtmDepositPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(
            ServerPlayer player,
            AtmDepositPacket packet
    ) {
        if (player.level() == null || packet.amount() <= 0) {
            return;
        }

        int currentBalance = MoneyComponent.getBalance(player);
        int currentBankBalance = MoneyComponent.getBankBalance(player);

        // 1. Validación: Tener suficiente dinero en el bolsillo
        if (currentBalance < packet.amount()) {
            return;
        }

        // 2. Validación: Que el banco no esté ya lleno o supere el límite de $999,999,999
        if (currentBankBalance >= MoneyComponent.MAX_BALANCE) {
            return;
        }

        if ((long) currentBankBalance + packet.amount() > MoneyComponent.MAX_BALANCE) {
            return;
        }

        // Transferir: Bolsillo -> Banco
        MoneyComponent.removeMoney(player, packet.amount());
        MoneyComponent.addBankMoney(player, packet.amount());
    }
}