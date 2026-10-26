package com.babastian.dynamicshops.items;

import com.babastian.dynamicshops.screens.VoucherEditScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

public class VoucherItem extends Item {

    public VoucherItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        boolean isSigned = customData != null && !customData.copyTag().getString("voucher_text").orElse("").isEmpty();

        if (isSigned) {
            return InteractionResult.PASS;
        }

        // Si está en blanco y hacemos clic derecho, abrimos la interfaz de edición solo en el cliente
        if (level.isClientSide()) {
            openClientScreen(stack, hand);
        }

        return InteractionResult.SUCCESS;
    }

    @Environment(EnvType.CLIENT)
    private void openClientScreen(ItemStack stack, InteractionHand hand) {
        // En Minecraft 26.2, setScreen se movió a la clase Gui o se gestiona a través del cliente
        Minecraft.getInstance().gui.setScreen(new VoucherEditScreen(stack, hand));
    }
}