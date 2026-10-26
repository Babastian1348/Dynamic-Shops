package com.babastian.dynamicshops.network;

import com.babastian.dynamicshops.DynamicShops;
import com.babastian.dynamicshops.money.MoneyComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

public record AtmWithdrawPacket(
        BlockPos pos,
        int amount
) implements CustomPacketPayload {

    public static final Type<AtmWithdrawPacket> TYPE =
            new Type<>(
                    Identifier.fromNamespaceAndPath(
                            DynamicShops.MOD_ID,
                            "atm_withdraw"
                    )
            );

    public static final StreamCodec<RegistryFriendlyByteBuf, AtmWithdrawPacket> CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC,
                    AtmWithdrawPacket::pos,
                    StreamCodec.of(
                            RegistryFriendlyByteBuf::writeInt,
                            RegistryFriendlyByteBuf::readInt
                    ),
                    AtmWithdrawPacket::amount,
                    AtmWithdrawPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(
            ServerPlayer player,
            AtmWithdrawPacket packet
    ) {
        if (player.level() == null || packet.amount() <= 0) {
            return;
        }

        int currentBankBalance = MoneyComponent.getBankBalance(player);

        // Validación de crédito: Comprobamos si el retiro deja el banco por debajo de -5,000
        int newBankBalance = currentBankBalance - packet.amount();
        if (newBankBalance < MoneyComponent.BANK_MIN_BALANCE) {
            return; // Supera el límite máximo de endeudamiento permitido
        }

        // Transferir: Banco -> Bolsillo
        MoneyComponent.removeBankMoney(player, packet.amount());
        MoneyComponent.addMoney(player, packet.amount());
    }
}