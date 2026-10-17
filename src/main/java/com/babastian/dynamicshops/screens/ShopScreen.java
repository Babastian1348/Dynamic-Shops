package com.babastian.dynamicshops.screens;

import com.babastian.dynamicshops.DynamicShops;
import com.babastian.dynamicshops.blocks.ShopBlockEntity;
import com.babastian.dynamicshops.money.MoneyComponent;
import com.babastian.dynamicshops.network.BuyItemPacket;
import com.babastian.dynamicshops.sounds.ModSounds;
import net.minecraft.client.Minecraft;
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

public class ShopScreen extends AbstractContainerScreen<ShopScreenHandler> {

    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
            DynamicShops.MOD_ID,
            "textures/gui/shop.png"
    );

    private static final int IMAGE_WIDTH = 176;
    private static final int IMAGE_HEIGHT = 166;

    private CustomButton buy1Button;
    private CustomButton buy10Button;
    private CustomButton buy64Button;

    // === BOTONES A LA IZQUIERDA ===
    private static final int BUY_1_BUTTON_X = 10;
    private static final int BUY_1_BUTTON_Y = 17;
    private static final int BUY_1_BUTTON_WIDTH = 64;
    private static final int BUY_1_BUTTON_HEIGHT = 12;

    private static final int BUY_10_BUTTON_X = 10;
    private static final int BUY_10_BUTTON_Y = 37;
    private static final int BUY_10_BUTTON_WIDTH = 64;
    private static final int BUY_10_BUTTON_HEIGHT = 12;

    private static final int BUY_64_BUTTON_X = 10;
    private static final int BUY_64_BUTTON_Y = 59;
    private static final int BUY_64_BUTTON_WIDTH = 64;
    private static final int BUY_64_BUTTON_HEIGHT = 12;

    // === RENDER A LA DERECHA ===
    private static final int ITEM_DISPLAY_X = 115;
    private static final int ITEM_DISPLAY_Y = 36;

    private static final int MONEY_X = 170;
    private static final int MONEY_Y = 6;

    public ShopScreen(
            ShopScreenHandler handler,
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

        this.buy1Button = new CustomButton(
                x + BUY_1_BUTTON_X,
                y + BUY_1_BUTTON_Y,
                BUY_1_BUTTON_WIDTH,
                BUY_1_BUTTON_HEIGHT,
                Component.translatable("button.dynamicshops.buy_1"),
                button -> onBuy(1),
                false
        );
        updateBuyButtonTooltip(this.buy1Button, 1);
        this.addRenderableWidget(this.buy1Button);

        this.buy10Button = new CustomButton(
                x + BUY_10_BUTTON_X,
                y + BUY_10_BUTTON_Y,
                BUY_10_BUTTON_WIDTH,
                BUY_10_BUTTON_HEIGHT,
                Component.translatable("button.dynamicshops.buy_10"),
                button -> onBuy(10),
                false
        );
        updateBuyButtonTooltip(this.buy10Button, 10);
        this.addRenderableWidget(this.buy10Button);

        this.buy64Button = new CustomButton(
                x + BUY_64_BUTTON_X,
                y + BUY_64_BUTTON_Y,
                BUY_64_BUTTON_WIDTH,
                BUY_64_BUTTON_HEIGHT,
                Component.translatable("button.dynamicshops.buy_64"),
                button -> onBuy(64),
                false
        );
        updateBuyButtonTooltip(this.buy64Button, 64);
        this.addRenderableWidget(this.buy64Button);

        updateButtonStates();
    }

    // === HELPER: Actualiza tooltip con precio dinámico (COMPRAR) ===
    private void updateBuyButtonTooltip(CustomButton button, int amount) {
        ShopBlockEntity blockEntity = this.menu.getBlockEntity();

        if (blockEntity == null || blockEntity.getBuyItem().isEmpty()) {
            button.setTooltip(Tooltip.create(
                    Component.translatable("tooltip.dynamicshops.buy_" + amount)
            ));
            return;
        }

        int price = blockEntity.getPrice();
        int total = price * amount;

        // Formateador numérico para el tooltip
        NumberFormat format = NumberFormat.getNumberInstance(Locale.US);
        String formattedTotal = format.format(total);

        button.setTooltip(Tooltip.create(
                Component.literal("-$" + formattedTotal)
                        .withStyle(style -> style.withColor(0xFFD12828)) // Rojo
        ));
    }

    private void onBuy(int amount) {
        if (this.minecraft == null || this.minecraft.player == null) {
            return;
        }

        Player player = this.minecraft.player;
        Level level = player.level();
        ShopBlockEntity blockEntity = this.menu.getBlockEntity();

        if (blockEntity == null) {
            return;
        }

        ItemStack buyItem = blockEntity.getBuyItem();

        if (buyItem.isEmpty()) {
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

        int price = blockEntity.getPrice();
        int maxStock = blockEntity.getMaxStock();
        boolean hasLimitedStock = blockEntity.hasLimitedStock();
        int soldCount = blockEntity.getSoldCount();

        int availableStock = hasLimitedStock ? (maxStock - soldCount) : Integer.MAX_VALUE;

        int playerMoney = MoneyComponent.getBalance(player);
        int totalPrice = price * amount;

        if (playerMoney < totalPrice || availableStock < amount) {
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

        int canAdd = getCanAddAmount(player, buyItem, amount);
        if (canAdd < amount) {
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

        net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(
                new BuyItemPacket(
                        this.menu.getPos(),
                        amount,
                        totalPrice
                )
        );

        level.playSound(
                player,
                player.blockPosition(),
                ModSounds.BUY,
                SoundSource.UI,
                1.0F,
                1.0F
        );
    }

    private void updateButtonStates() {
        ItemStack buyItem = this.menu.getBlockEntity().getBuyItem();

        if (buyItem.isEmpty()) {
            this.buy1Button.active = false;
            this.buy10Button.active = false;
            this.buy64Button.active = false;
        } else {
            boolean isStackable = buyItem.isStackable();

            this.buy1Button.active = true;
            this.buy10Button.active = isStackable;
            this.buy64Button.active = isStackable;
        }
    }

    private int getCanAddAmount(Player player, ItemStack template, int desiredAmount) {
        if (player == null) {
            return 0;
        }

        int remaining = desiredAmount;

        for (int i = 0; i < 36; i++) {
            if (remaining <= 0) {
                break;
            }

            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && ItemStack.isSameItemSameComponents(template, stack)) {
                int canAdd = Math.min(stack.getMaxStackSize() - stack.getCount(), remaining);
                if (canAdd > 0) {
                    remaining -= canAdd;
                }
            }
        }

        if (remaining > 0) {
            for (int i = 0; i < 36; i++) {
                if (remaining <= 0) {
                    break;
                }

                ItemStack stack = player.getInventory().getItem(i);
                if (stack.isEmpty()) {
                    int canAdd = Math.min(template.getMaxStackSize(), remaining);
                    remaining -= canAdd;
                }
            }
        }

        return desiredAmount - remaining;
    }

    @Override
    public void containerTick() {
        super.containerTick();
        // Actualizar tooltips cada tick
        updateBuyButtonTooltip(this.buy1Button, 1);
        updateBuyButtonTooltip(this.buy10Button, 10);
        updateBuyButtonTooltip(this.buy64Button, 64);
    }

    @Override
    public void extractRenderState(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float delta
    ) {
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

        ItemStack buyItem = this.menu.getBlockEntity().getBuyItem();

        if (!buyItem.isEmpty()) {
            int itemX = this.leftPos + ITEM_DISPLAY_X;
            int itemY = this.topPos + ITEM_DISPLAY_Y;
            int baseSize = 16;
            int scale = 2;
            int scaledSize = baseSize * scale;

            graphics.pose().pushMatrix();
            graphics.pose().translate((float)(itemX + 8), (float)(itemY + 8));
            graphics.pose().scale(2.0F, 2.0F);
            graphics.pose().translate((float)-(itemX + 8), (float)-(itemY + 8));

            graphics.item(buyItem, itemX, itemY);
            graphics.itemDecorations(this.font, buyItem, itemX, itemY);

            graphics.pose().popMatrix();

            int offsetX = (scaledSize - baseSize) / 2;
            int tooltipX = itemX - offsetX;
            int tooltipY = itemY - offsetX;
            int tooltipSize = scaledSize;

            if (mouseX >= tooltipX && mouseX < tooltipX + tooltipSize &&
                    mouseY >= tooltipY && mouseY < tooltipY + tooltipSize) {

                graphics.setTooltipForNextFrame(
                        this.font,
                        buyItem,
                        mouseX,
                        mouseY
                );
            }
        }

        super.extractRenderState(graphics, mouseX, mouseY, delta);

        renderButtonText(graphics, this.buy1Button);
        renderButtonText(graphics, this.buy10Button);
        renderButtonText(graphics, this.buy64Button);

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