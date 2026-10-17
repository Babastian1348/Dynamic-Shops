package com.babastian.dynamicshops.screens;

import com.babastian.dynamicshops.DynamicShops;
import com.babastian.dynamicshops.blocks.SellBlockEntity;
import com.babastian.dynamicshops.money.MoneyComponent;
import com.babastian.dynamicshops.network.SellItemPacket;
import com.babastian.dynamicshops.sounds.ModSounds;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.text.NumberFormat;
import java.util.Locale;

public class SellScreen extends AbstractContainerScreen<SellScreenHandler> {

    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
            DynamicShops.MOD_ID,
            "textures/gui/sell.png"
    );

    private static final int IMAGE_WIDTH = 176;
    private static final int IMAGE_HEIGHT = 166;

    private CustomButton sell1Button;
    private CustomButton sell10Button;
    private CustomButton sell64Button;

    // POSICIONES CUSTOM
    private static final int SELL_1_BUTTON_X = 105;
    private static final int SELL_1_BUTTON_Y = 17;
    private static final int SELL_1_BUTTON_WIDTH = 64;
    private static final int SELL_1_BUTTON_HEIGHT = 12;

    private static final int SELL_10_BUTTON_X = 105;
    private static final int SELL_10_BUTTON_Y = 37;
    private static final int SELL_10_BUTTON_WIDTH = 64;
    private static final int SELL_10_BUTTON_HEIGHT = 12;

    private static final int SELL_64_BUTTON_X = 105;
    private static final int SELL_64_BUTTON_Y = 59;
    private static final int SELL_64_BUTTON_WIDTH = 64;
    private static final int SELL_64_BUTTON_HEIGHT = 12;

    // POSICIÓN DEL ITEM DE VENTA
    private static final int ITEM_DISPLAY_X = 35;
    private static final int ITEM_DISPLAY_Y = 36;

    // POSICIONES PARA UI
    private static final int MONEY_X = 170;
    private static final int MONEY_Y = 6;

    public SellScreen(
            SellScreenHandler handler,
            Inventory inventory,
            Component title
    ) {
        super(handler, inventory, title, IMAGE_WIDTH, IMAGE_HEIGHT);
    }

    @Override
    protected void init() {
        super.init();

        int x = this.leftPos;
        int y = this.topPos;

        // Botón Vender 1
        this.sell1Button = new CustomButton(
                x + SELL_1_BUTTON_X,
                y + SELL_1_BUTTON_Y,
                SELL_1_BUTTON_WIDTH,
                SELL_1_BUTTON_HEIGHT,
                Component.translatable("button.dynamicshops.sell_1"),
                button -> onSell(1),
                false
        );
        updateSellButtonTooltip(this.sell1Button, 1);
        this.addRenderableWidget(this.sell1Button);

        // Botón Vender 10
        this.sell10Button = new CustomButton(
                x + SELL_10_BUTTON_X,
                y + SELL_10_BUTTON_Y,
                SELL_10_BUTTON_WIDTH,
                SELL_10_BUTTON_HEIGHT,
                Component.translatable("button.dynamicshops.sell_10"),
                button -> onSell(10),
                false
        );
        updateSellButtonTooltip(this.sell10Button, 10);
        this.addRenderableWidget(this.sell10Button);

        // Botón Vender 64
        this.sell64Button = new CustomButton(
                x + SELL_64_BUTTON_X,
                y + SELL_64_BUTTON_Y,
                SELL_64_BUTTON_WIDTH,
                SELL_64_BUTTON_HEIGHT,
                Component.translatable("button.dynamicshops.sell_64"),
                button -> onSell(64),
                false
        );
        updateSellButtonTooltip(this.sell64Button, 64);
        this.addRenderableWidget(this.sell64Button);

        updateButtonStates();
    }

    // === HELPER: Actualiza tooltip con precio dinámico (VENDER) ===
    private void updateSellButtonTooltip(CustomButton button, int amount) {
        SellBlockEntity blockEntity = this.menu.getBlockEntity();

        if (blockEntity == null || blockEntity.getSellItem().isEmpty()) {
            button.setTooltip(Tooltip.create(
                    Component.translatable("tooltip.dynamicshops.sell_" + amount)
            ));
            return;
        }

        int price = blockEntity.getPrice();
        int total = price * amount;

        // Formateador numérico
        NumberFormat format = NumberFormat.getNumberInstance(Locale.US);
        String formattedTotal = format.format(total);

        button.setTooltip(Tooltip.create(
                Component.literal("+$" + formattedTotal)
                        .withStyle(style -> style.withColor(0xFF287D25)) // Verde
        ));
    }

    private void onSell(int amount) {
        if (this.minecraft == null || this.minecraft.player == null) {
            return;
        }

        Player player = this.minecraft.player;
        Level level = player.level();
        SellBlockEntity blockEntity = this.menu.getBlockEntity();

        if (blockEntity == null) {
            return;
        }

        ItemStack sellItem = blockEntity.getSellItem();

        if (sellItem.isEmpty()) {
            return;
        }

        int price = blockEntity.getPrice();
        int maxStock = blockEntity.getMaxStock();
        boolean hasLimitedStock = blockEntity.hasLimitedStock();
        int soldCount = blockEntity.getSoldCount();

        int availableStock = hasLimitedStock ? (maxStock - soldCount) : Integer.MAX_VALUE;

        int playerAmount = countItemInInventory(player, sellItem);

        if (playerAmount < amount || availableStock < amount) {
            level.playSound(
                    player,
                    player.blockPosition(),
                    ModSounds.ERROR,
                    SoundSource.UI,
                    1.0F,
                    1.0F
            );
            return;
        }

        int totalPrice = price * amount;

        net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(
                new SellItemPacket(
                        this.menu.getPos(),
                        amount,
                        totalPrice
                )
        );

        level.playSound(
                player,
                player.blockPosition(),
                ModSounds.SELL,
                SoundSource.UI,
                1.0F,
                1.0F
        );
    }

    private int countItemInInventory(Player player, ItemStack targetItem) {
        int count = 0;

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && ItemStack.isSameItemSameComponents(targetItem, stack)) {
                count += stack.getCount();
            }
        }

        return count;
    }

    private void updateButtonStates() {
        ItemStack sellItem = this.menu.getBlockEntity().getSellItem();

        if (sellItem.isEmpty()) {
            this.sell1Button.active = false;
            this.sell10Button.active = false;
            this.sell64Button.active = false;
        } else {
            boolean isStackable = sellItem.isStackable();

            this.sell1Button.active = true;
            this.sell10Button.active = isStackable;
            this.sell64Button.active = isStackable;
        }
    }

    @Override
    public void containerTick() {
        super.containerTick();
        // Actualizar tooltips cada tick
        updateSellButtonTooltip(this.sell1Button, 1);
        updateSellButtonTooltip(this.sell10Button, 10);
        updateSellButtonTooltip(this.sell64Button, 64);
    }

    @Override
    public void extractRenderState(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float delta
    ) {
        updateButtonStates();

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
                this.playerInventoryTitle,
                this.leftPos + this.inventoryLabelX,
                this.topPos + this.inventoryLabelY,
                0x404040,
                false
        );

        ItemStack sellItem = this.menu.getBlockEntity().getSellItem();

        if (!sellItem.isEmpty()) {
            int itemX = this.leftPos + ITEM_DISPLAY_X;
            int itemY = this.topPos + ITEM_DISPLAY_Y;
            int baseSize = 16;
            int scale = 2;
            int scaledSize = baseSize * scale;

            graphics.pose().pushMatrix();
            graphics.pose().translate((float)(itemX + 8), (float)(itemY + 8));
            graphics.pose().scale(2.0F, 2.0F);
            graphics.pose().translate((float)-(itemX + 8), (float)-(itemY + 8));

            graphics.item(sellItem, itemX, itemY);
            graphics.itemDecorations(this.font, sellItem, itemX, itemY);

            graphics.pose().popMatrix();

            int offsetX = (scaledSize - baseSize) / 2;
            int tooltipX = itemX - offsetX;
            int tooltipY = itemY - offsetX;
            int tooltipSize = scaledSize;

            if (mouseX >= tooltipX && mouseX < tooltipX + tooltipSize &&
                    mouseY >= tooltipY && mouseY < tooltipY + tooltipSize) {

                graphics.setTooltipForNextFrame(
                        this.font,
                        sellItem,
                        mouseX,
                        mouseY
                );
            }
        }

        super.extractRenderState(graphics, mouseX, mouseY, delta);

        renderButtonText(graphics, this.sell1Button);
        renderButtonText(graphics, this.sell10Button);
        renderButtonText(graphics, this.sell64Button);

        if (this.minecraft != null && this.minecraft.player != null) {
            int money = MoneyComponent.getBalance(this.minecraft.player);

            // Formateador numérico para el balance del jugador
            NumberFormat format = NumberFormat.getNumberInstance(Locale.US);
            String moneyText = "$" + format.format(money);
            int moneyWidth = this.font.width(moneyText);

            int moneyX = this.leftPos + MONEY_X - moneyWidth;
            int moneyY = this.topPos + MONEY_Y;

            // Si el jugador llegó al límite, el texto cambia a color dorado/ámbar
            int moneyColor = (money >= MoneyComponent.MAX_BALANCE) ? 0xFFFFAA00 : 0xFF287D25;

            graphics.text(
                    this.font,
                    moneyText,
                    moneyX,
                    moneyY,
                    moneyColor,
                    false
            );
        }
    }

    private void renderButtonText(GuiGraphicsExtractor graphics, CustomButton button) {
        if (button == null || button.getMessage() == null) {
            return;
        }

        int textColor = 0xFFFFFFFF;
        if (!button.isActive()) {
            textColor = 0xFF808080;
        }

        int textWidth = this.font.width(button.getMessage());
        int textX = button.getX() + (button.getWidth() - textWidth) / 2;
        int textY = button.getY() + (button.getHeight() - 9) / 2 + 1;

        graphics.text(
                this.font,
                button.getMessage(),
                textX,
                textY,
                textColor,
                false
        );
    }

    public class CustomButton extends net.minecraft.client.gui.components.Button {

        public CustomButton(
                int x,
                int y,
                int width,
                int height,
                Component message,
                OnPress onPress,
                boolean isHoveredOnly
        ) {
            super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
        }

        @Override
        public void onPress(InputWithModifiers input) {
            super.onPress(input);
        }

        @Override
        protected void extractContents(
                GuiGraphicsExtractor graphics,
                int mouseX,
                int mouseY,
                float delta
        ) {
            boolean isHovered = mouseX >= this.getX() && mouseX < this.getX() + this.width &&
                    mouseY >= this.getY() && mouseY < this.getY() + this.height;

            int textureV = isHovered ? 12 : 0;

            graphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    TEXTURE,
                    this.getX(),
                    this.getY(),
                    176,
                    textureV,
                    this.width,
                    this.height,
                    256,
                    256
            );
        }
    }
}