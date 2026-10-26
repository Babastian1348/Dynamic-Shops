package com.babastian.dynamicshops.network;

import com.babastian.dynamicshops.DynamicShops;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;

import java.util.List;

public record VoucherSavePacket(
        InteractionHand hand,
        String text
) implements CustomPacketPayload {

    public static final Type<VoucherSavePacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(DynamicShops.MOD_ID, "voucher_save"));

    // Codec robusto utilizando ByteBufCodecs oficiales de Minecraft
    public static final StreamCodec<RegistryFriendlyByteBuf, VoucherSavePacket> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BYTE.map(
                            b -> InteractionHand.values()[b],
                            hand -> (byte) hand.ordinal()
                    ),
                    VoucherSavePacket::hand,
                    ByteBufCodecs.stringUtf8(256),
                    VoucherSavePacket::text,
                    VoucherSavePacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ServerPlayer player, VoucherSavePacket packet) {

        if (player.level() == null) return;

        ItemStack stack = player.getItemInHand(packet.hand());
        if (stack.isEmpty()) return;

        CustomData existingData = stack.get(DataComponents.CUSTOM_DATA);
        if (existingData != null && !existingData.copyTag().getString("voucher_text").orElse("").isEmpty()) {
            return;
        }

        CompoundTag tag = existingData != null ? existingData.copyTag() : new CompoundTag();
        tag.putString("voucher_text", packet.text());
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

        List<Component> loreLines = List.of(Component.literal("§7" + packet.text()));
        stack.set(DataComponents.LORE, new ItemLore(loreLines));

        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }
}