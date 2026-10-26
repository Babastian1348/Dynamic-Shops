package com.babastian.dynamicshops.screens;

import com.babastian.dynamicshops.network.VoucherSavePacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public class VoucherEditScreen extends Screen {
    private final ItemStack voucherStack;
    private final InteractionHand hand;
    private EditBox inputField;

    public VoucherEditScreen(ItemStack voucherStack, InteractionHand hand) {
        super(Component.translatable("screen.dynamicshops.voucher_edit"));
        this.voucherStack = voucherStack;
        this.hand = hand;
    }

    @Override
    protected void init() {
        super.init();

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        this.inputField = new EditBox(
                this.font,
                centerX - 100,
                centerY - 20,
                200,
                20,
                Component.literal("")
        );
        this.inputField.setMaxLength(64);
        this.inputField.setHint(Component.literal("Ej: Vale por un rango VIP"));
        this.setFocused(this.inputField);
        this.addRenderableWidget(this.inputField);

        this.addRenderableWidget(
                Button.builder(Component.translatable("button.dynamicshops.save"), button -> {
                    String text = this.inputField.getValue();
                    if (!text.trim().isEmpty()) {
                        ClientPlayNetworking.send(new VoucherSavePacket(this.hand, text.trim()));
                    }
                    this.onClose();
                }).bounds(centerX - 50, centerY + 10, 100, 20).build()
        );
    }

    @Override
    public void extractRenderState(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY, final float a) {
        super.extractRenderState(graphics, mouseX, mouseY, a);

        int centerX = this.width / 2;
        int centerY = this.height / 2;
        Component title = this.getTitle();

        // Método correcto de extracción de texto en esta versión
        graphics.text(this.font, title, centerX - (this.font.width(title) / 2), centerY - 45, -1, true);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}