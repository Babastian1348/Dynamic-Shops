package com.babastian.dynamicshops.screens;

import com.babastian.dynamicshops.DynamicShops;
import com.babastian.dynamicshops.money.MoneyComponent;
import com.babastian.dynamicshops.network.AtmDepositPacket;
import com.babastian.dynamicshops.network.AtmWithdrawPacket;
import com.babastian.dynamicshops.network.AtmWithdrawPhysicalPacket;
import com.babastian.dynamicshops.sounds.ModSounds;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

public class AtmScreen extends AbstractContainerScreen<AtmScreenHandler> {

    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
            DynamicShops.MOD_ID,
            "textures/gui/atm.png"
    );

    private static final int MONEY_X = 170;
    private static final int MONEY_Y = 6;

    private static final int BANK_MONEY_X = 8;
    private static final int BANK_MONEY_Y = 25;

    private static final int AMOUNT_INPUT_X = 8;
    private static final int AMOUNT_INPUT_Y = 41;
    private static final int AMOUNT_INPUT_WIDTH = 70;
    private static final int AMOUNT_INPUT_HEIGHT = 14;

    private static final int DEPOSIT_BUTTON_X = 105;
    private static final int DEPOSIT_BUTTON_Y = 23;
    private static final int DEPOSIT_BUTTON_WIDTH = 64;
    private static final int DEPOSIT_BUTTON_HEIGHT = 14;

    private static final int WITHDRAW_BUTTON_X = 105;
    private static final int WITHDRAW_BUTTON_Y = 41;
    private static final int WITHDRAW_BUTTON_WIDTH = 64;
    private static final int WITHDRAW_BUTTON_HEIGHT = 14;

    private static final int PHYSICAL_WITHDRAW_BUTTON_X = 77;
    private static final int PHYSICAL_WITHDRAW_BUTTON_Y = 59;
    private static final int PHYSICAL_WITHDRAW_BUTTON_WIDTH = 92;
    private static final int PHYSICAL_WITHDRAW_BUTTON_HEIGHT = 14;

    private static final int MAX_INPUT_AMOUNT = 99_999_999; // ← CAMBIO: 99M en lugar de 999M

    private EditBox amountInput;
    private Button depositButton;
    private Button withdrawButton;
    private Button withdrawPhysicalButton;

    public AtmScreen(AtmScreenHandler handler, Inventory inventory, Component title) {
        super(handler, inventory, title, 176, 166);
    }

    @Override
    protected void init() {
        super.init();

        int x = this.leftPos;
        int y = this.topPos;

        this.amountInput = new EditBox(
                this.font,
                x + AMOUNT_INPUT_X,
                y + AMOUNT_INPUT_Y,
                AMOUNT_INPUT_WIDTH,
                AMOUNT_INPUT_HEIGHT,
                Component.literal("")
        );

        this.amountInput.setMaxLength(8); // ← CAMBIO: 8 caracteres
        this.amountInput.setResponder(this::updateInputValidation);
        this.amountInput.setTooltip(Tooltip.create(Component.translatable("tooltip.dynamicshops.bank_amount")));
        this.addRenderableWidget(this.amountInput);

        this.depositButton = Button.builder(
                        Component.translatable("button.dynamicshops.deposit"),
                        b -> onDeposit()
                ).bounds(x + DEPOSIT_BUTTON_X, y + DEPOSIT_BUTTON_Y, DEPOSIT_BUTTON_WIDTH, DEPOSIT_BUTTON_HEIGHT)
                .tooltip(Tooltip.create(Component.translatable("tooltip.dynamicshops.deposit")))
                .build();
        this.addRenderableWidget(this.depositButton);

        this.withdrawButton = Button.builder(
                        Component.translatable("button.dynamicshops.withdraw"),
                        b -> onWithdraw()
                ).bounds(x + WITHDRAW_BUTTON_X, y + WITHDRAW_BUTTON_Y, WITHDRAW_BUTTON_WIDTH, WITHDRAW_BUTTON_HEIGHT)
                .tooltip(Tooltip.create(Component.translatable("tooltip.dynamicshops.withdraw")))
                .build();
        this.addRenderableWidget(this.withdrawButton);

        this.withdrawPhysicalButton = Button.builder(
                        Component.translatable("button.dynamicshops.physical_withdraw"),
                        b -> onWithdrawPhysical()
                ).bounds(x + PHYSICAL_WITHDRAW_BUTTON_X, y + PHYSICAL_WITHDRAW_BUTTON_Y, PHYSICAL_WITHDRAW_BUTTON_WIDTH, PHYSICAL_WITHDRAW_BUTTON_HEIGHT)
                .tooltip(Tooltip.create(Component.translatable("tooltip.dynamicshops.physical_withdraw")))
                .build();
        this.addRenderableWidget(this.withdrawPhysicalButton);
    }

    @Override
    public void containerTick() {
        super.containerTick();
        if (this.minecraft != null && this.minecraft.player != null) {
            int bankBalance = MoneyComponent.getBankBalance(this.minecraft.player);
            this.depositButton.active = bankBalance < MoneyComponent.MAX_BALANCE;

            // ==========================================
            // LOGICA DEL TOOLTIP DINÁMICO DE ERROR
            // ==========================================
            int amount = getEnteredAmount();
            if (amount > 0 && bankBalance >= amount) {
                Map<Item, Integer> simulatedMoney = AtmWithdrawPhysicalPacket.calculatePhysicalMoney(amount);
                boolean hasSpace = AtmWithdrawPhysicalPacket.hasEnoughSpaceForMultipleItems(this.minecraft.player, simulatedMoney);

                if (!hasSpace) {
                    this.withdrawPhysicalButton.setTooltip(Tooltip.create(
                            Component.translatable("tooltip.dynamicshops.inventory_full").withStyle(ChatFormatting.DARK_RED)
                    ));
                } else {
                    this.withdrawPhysicalButton.setTooltip(Tooltip.create(
                            Component.translatable("tooltip.dynamicshops.physical_withdraw")
                    ));
                }
            } else {
                this.withdrawPhysicalButton.setTooltip(Tooltip.create(
                        Component.translatable("tooltip.dynamicshops.physical_withdraw")
                ));
            }
        }
    }

    private void updateInputValidation(String text) {
        if (text.isEmpty() || this.minecraft == null || this.minecraft.player == null) {
            this.amountInput.setTextColor(0xFFFFFFFF);
            return;
        }

        try {
            long value = Long.parseLong(text);
            int bankBalance = MoneyComponent.getBankBalance(this.minecraft.player);

            if (value <= 0 || value > MAX_INPUT_AMOUNT || (bankBalance + value) > MoneyComponent.MAX_BALANCE) {
                this.amountInput.setTextColor(0xFFFF0000);
            } else {
                this.amountInput.setTextColor(0xFFFFFFFF);
            }
        } catch (NumberFormatException e) {
            this.amountInput.setTextColor(0xFFFF0000);
        }
    }

    private void onDeposit() {
        if (this.minecraft == null || this.minecraft.player == null) return;
        Player player = this.minecraft.player;
        int amount = getEnteredAmount();
        int pocketBalance = MoneyComponent.getBalance(player);
        int bankBalance = MoneyComponent.getBankBalance(player);

        if (amount <= 0 || pocketBalance < amount || (bankBalance + amount) > MoneyComponent.MAX_BALANCE) {
            this.amountInput.setTextColor(0xFFFF0000);
            playErrorSound();
            return;
        }

        ClientPlayNetworking.send(new AtmDepositPacket(this.menu.getPos(), amount));
        playDepositSuccessSound(); // ← CAMBIO: Sonido único para depósito
    }

    private void onWithdraw() {
        if (this.minecraft == null || this.minecraft.player == null) return;
        Player player = this.minecraft.player;
        int amount = getEnteredAmount();
        int bankBalance = MoneyComponent.getBankBalance(player);

        if (amount <= 0 || bankBalance < amount) {
            this.amountInput.setTextColor(0xFFFF0000);
            playErrorSound();
            return;
        }

        ClientPlayNetworking.send(new AtmWithdrawPacket(this.menu.getPos(), amount));
        playWithdrawSuccessSound(); // ← CAMBIO: Sonido único para retiro
    }

    private void onWithdrawPhysical() {
        if (this.minecraft == null || this.minecraft.player == null) return;

        Player player = this.minecraft.player;
        int amount = getEnteredAmount();
        int bankBalance = MoneyComponent.getBankBalance(player);

        if (amount <= 0 || bankBalance < amount) {
            this.amountInput.setTextColor(0xFFFF0000);
            playErrorSound();
            return;
        }

        Map<Item, Integer> simulatedMoney = AtmWithdrawPhysicalPacket.calculatePhysicalMoney(amount);

        if (!AtmWithdrawPhysicalPacket.hasEnoughSpaceForMultipleItems(player, simulatedMoney)) {
            this.amountInput.setTextColor(0xFFFF0000);
            playErrorSound();
            return;
        }

        ClientPlayNetworking.send(new AtmWithdrawPhysicalPacket(this.menu.getPos(), amount));
        playPhysicalWithdrawSuccessSound(); // ← CAMBIO: Sonido único para retiro físico
    }

    public int getEnteredAmount() {
        String text = this.amountInput.getValue();
        if (text.isEmpty()) return 0;
        try {
            long value = Long.parseLong(text);
            return (value > 0 && value <= MAX_INPUT_AMOUNT) ? (int) value : 0;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void playErrorSound() {
        if (this.minecraft == null || this.minecraft.player == null) return;
        Level level = this.minecraft.player.level();
        level.playSound(this.minecraft.player, this.minecraft.player.blockPosition(), ModSounds.ERROR, SoundSource.UI, 1.0F, 1.0F);
    }

    private void playDepositSuccessSound() {
        if (this.minecraft == null || this.minecraft.player == null) return;
        Level level = this.minecraft.player.level();
        level.playSound(this.minecraft.player, this.minecraft.player.blockPosition(), ModSounds.DEPOSIT, SoundSource.UI, 1.0F, 1.0F);
    }

    private void playWithdrawSuccessSound() {
        if (this.minecraft == null || this.minecraft.player == null) return;
        Level level = this.minecraft.player.level();
        level.playSound(this.minecraft.player, this.minecraft.player.blockPosition(), ModSounds.WITHDRAW, SoundSource.UI, 1.0F, 1.0F);
    }

    private void playPhysicalWithdrawSuccessSound() {
        if (this.minecraft == null || this.minecraft.player == null) return;
        Level level = this.minecraft.player.level();
        level.playSound(this.minecraft.player, this.minecraft.player.blockPosition(), ModSounds.PHYSICAL_WITHDRAW, SoundSource.UI, 1.0F, 1.0F);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                TEXTURE,
                this.leftPos,
                this.topPos,
                0,
                0,
                176,
                166,
                256,
                256
        );

        graphics.text(
                this.font,
                this.title,
                this.leftPos + this.titleLabelX,
                this.topPos + this.titleLabelY,
                0x404040,
                false
        );

        super.extractRenderState(graphics, mouseX, mouseY, delta);

        if (this.minecraft != null && this.minecraft.player != null) {
            // Formateador universal para separar por comas (ej. $1,000,000)
            NumberFormat format = NumberFormat.getNumberInstance(Locale.US);

            // ==========================================
            // TEXTO DE BOLSILLO
            // ==========================================
            int pocketMoney = MoneyComponent.getBalance(this.minecraft.player);
            String pocketText = "$" + format.format(pocketMoney);
            int pocketWidth = this.font.width(pocketText);

            int pocketX = this.leftPos + MONEY_X - pocketWidth;
            int pocketY = this.topPos + MONEY_Y;

            // Si llegamos al máximo, el texto se pone de color ámbar/dorado (0xFFFFAA00), si no, verde
            int pocketColor = (pocketMoney >= MoneyComponent.MAX_BALANCE) ? 0xFFFFAA00 : 0xFF287D25;

            graphics.text(
                    this.font,
                    pocketText,
                    pocketX,
                    pocketY,
                    pocketColor,
                    false
            );

            // ==========================================
            // TEXTO DEL BANCO (Alineado a la izquierda)
            // ==========================================
            int bankMoney = MoneyComponent.getBankBalance(this.minecraft.player);
            String bankText = "$" + format.format(bankMoney);

            float scale = 1.2F;

            // Fijamos la esquina izquierda en BANK_MONEY_X
            float targetX = this.leftPos + BANK_MONEY_X;
            float targetY = this.topPos + BANK_MONEY_Y;

            // Lógica de color para el banco
            int bankColor = (bankMoney >= MoneyComponent.MAX_BALANCE) ? 0xFFFFAA00 : 0xFF287D25;

            graphics.pose().pushMatrix();
            graphics.pose().translate(targetX, targetY);
            graphics.pose().scale(scale, scale);

            graphics.text(
                    this.font,
                    bankText,
                    0,
                    0,
                    bankColor,
                    false
            );

            graphics.pose().popMatrix();
        }
    }
}