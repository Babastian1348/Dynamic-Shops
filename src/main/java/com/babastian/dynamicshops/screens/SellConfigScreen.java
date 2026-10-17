package com.babastian.dynamicshops.screens;

import com.babastian.dynamicshops.DynamicShops;
import com.babastian.dynamicshops.blocks.SellBlockEntity;
import com.babastian.dynamicshops.network.SellConfigSavePacket;
import com.babastian.dynamicshops.sounds.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class SellConfigScreen
        extends AbstractContainerScreen<SellConfigScreenHandler> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(
                    DynamicShops.MOD_ID,
                    "textures/gui/sell_config.png"
            );

    private static final int IMAGE_WIDTH = 176;
    private static final int IMAGE_HEIGHT = 166;

    private EditBox priceInput;
    private EditBox stockInput;
    private EditBox nameInput;
    private CustomButton toggleButton;
    private Button doneButton;

    private boolean isChecked = false;

    // POSICIONES CUSTOM
    public static final int ITEM_SLOT_X = 11;
    public static final int ITEM_SLOT_Y = 18;

    private static final int PRICE_INPUT_X = 25;
    private static final int PRICE_INPUT_Y = 40;
    private static final int PRICE_INPUT_WIDTH = 50;
    private static final int PRICE_INPUT_HEIGHT = 12;

    private static final int STOCK_INPUT_X = 25;
    private static final int STOCK_INPUT_Y = 57;
    private static final int STOCK_INPUT_WIDTH = 50;
    private static final int STOCK_INPUT_HEIGHT = 12;

    private static final int NAME_INPUT_X = 33;
    private static final int NAME_INPUT_Y = 19;
    private static final int NAME_INPUT_WIDTH = 132;
    private static final int NAME_INPUT_HEIGHT = 14;

    private static final int TOGGLE_BUTTON_X = 10;
    private static final int TOGGLE_BUTTON_Y = 57;
    private static final int TOGGLE_BUTTON_WIDTH = 12;
    private static final int TOGGLE_BUTTON_HEIGHT = 12;

    private static final int DONE_BUTTON_X = 85;
    private static final int DONE_BUTTON_Y = 47;
    private static final int DONE_BUTTON_WIDTH = 80;
    private static final int DONE_BUTTON_HEIGHT = 15;

    public SellConfigScreen(
            SellConfigScreenHandler handler,
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

        this.isChecked = this.menu.getBlockEntity().hasLimitedStock();

        // Input de precio
        this.priceInput = new EditBox(
                this.font,
                x + PRICE_INPUT_X,
                y + PRICE_INPUT_Y,
                PRICE_INPUT_WIDTH,
                PRICE_INPUT_HEIGHT,
                Component.literal("")
        );

        this.priceInput.setMaxLength(9);
        this.priceInput.setValue(
                String.valueOf(this.menu.getBlockEntity().getPrice())
        );

        this.priceInput.setResponder(text -> {
            if (!text.isEmpty()) {
                try {
                    int value = Integer.parseInt(text);
                    if (value > 999_999_999) {
                        this.priceInput.setTextColor(0xFFFF0000);
                    } else {
                        this.priceInput.setTextColor(0xFFFFFFFF);
                    }
                } catch (NumberFormatException e) {
                    this.priceInput.setTextColor(0xFFFF0000);
                }
            } else {
                this.priceInput.setTextColor(0xFFFFFFFF);
            }
        });

        this.priceInput.setTooltip(Tooltip.create(
                Component.translatable("tooltip.dynamicshops.sell_price")
        ));

        this.addRenderableWidget(this.priceInput);

        // Input de stock
        this.stockInput = new EditBox(
                this.font,
                x + STOCK_INPUT_X,
                y + STOCK_INPUT_Y,
                STOCK_INPUT_WIDTH,
                STOCK_INPUT_HEIGHT,
                Component.literal("")
        );

        this.stockInput.setMaxLength(4);
        this.stockInput.setValue(
                String.valueOf(this.menu.getBlockEntity().getMaxStock())
        );

        this.stockInput.setResponder(text -> {
            if (!text.isEmpty()) {
                try {
                    int value = Integer.parseInt(text);
                    if (value > 1024) {
                        this.stockInput.setTextColor(0xFFFF0000);
                    } else {
                        this.stockInput.setTextColor(0xFFFFFFFF);
                    }
                } catch (NumberFormatException e) {
                    this.stockInput.setTextColor(0xFFFF0000);
                }
            } else {
                this.stockInput.setTextColor(0xFFFFFFFF);
            }
        });

        this.stockInput.setTooltip(Tooltip.create(
                Component.translatable("tooltip.dynamicshops.limit")
        ));

        this.addRenderableWidget(this.stockInput);

        // === NUEVO: Input de nombre personalizado ===
        this.nameInput = new EditBox(
                this.font,
                x + NAME_INPUT_X,
                y + NAME_INPUT_Y,
                NAME_INPUT_WIDTH,
                NAME_INPUT_HEIGHT,
                Component.literal("")
        );

        this.nameInput.setMaxLength(64);
        this.nameInput.setValue(
                this.menu.getBlockEntity().getCustomName()
        );

        this.nameInput.setTooltip(Tooltip.create(
                Component.translatable("tooltip.dynamicshops.custom_name")
        ));

        this.nameInput.setCanLoseFocus(true);
        this.nameInput.setFocused(false);

        this.addRenderableWidget(this.nameInput);

        // Botón toggle custom
        this.toggleButton = new CustomButton(
                x + TOGGLE_BUTTON_X,
                y + TOGGLE_BUTTON_Y,
                TOGGLE_BUTTON_WIDTH,
                TOGGLE_BUTTON_HEIGHT,
                Component.literal(isChecked ? "[✓] Stock limitado" : "[ ] Stock limitado"),
                button -> {
                    isChecked = !isChecked;
                    button.setMessage(Component.literal(
                            isChecked ? "[✓] Stock limitado" : "[ ] Stock limitado"
                    ));
                    stockInput.setEditable(isChecked);
                    if (!isChecked) {
                        stockInput.setValue("0");
                        stockInput.setTextColor(0x808080);
                    } else {
                        stockInput.setEditable(true);
                        stockInput.setValue("1");
                        stockInput.setTextColor(0xFFFFFFFF);
                    }
                }
        );

        this.toggleButton.setTooltip(Tooltip.create(
                Component.translatable("tooltip.dynamicshops.limited_sales")
        ));

        this.addRenderableWidget(this.toggleButton);

        this.stockInput.setEditable(this.isChecked);
        if (!this.isChecked) {
            this.stockInput.setValue("0");
            this.stockInput.setTextColor(0x808080);
        }

        // Botón Guardar
        this.doneButton = Button.builder(
                        Component.translatable("button.dynamicshops.save"),
                        this::onSave
                ).bounds(x + DONE_BUTTON_X, y + DONE_BUTTON_Y, DONE_BUTTON_WIDTH, DONE_BUTTON_HEIGHT)
                .tooltip(Tooltip.create(
                        Component.translatable("tooltip.dynamicshops.save")
                ))
                .build();

        this.addRenderableWidget(this.doneButton);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        // Si el nameInput tiene el foco, dejar que maneje TODAS las teclas
        if (this.nameInput.isFocused()) {
            // Si el input procesó la tecla, retornar true
            if (this.nameInput.keyPressed(event)) {
                return true;
            }
            // Si es una tecla de movimiento (flechas, etc.), también manejarla
            return true; // NO llamar a super.keyPressed() para evitar que cierre con E
        }
        // Si no tiene el foco, comportamiento normal
        return super.keyPressed(event);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        // Si el nameInput tiene el foco, dejar que maneje el caracter PRIMERO
        if (this.nameInput.isFocused() && this.nameInput.charTyped(event)) {
            return true;
        }
        // Si no, comportamiento normal
        return super.charTyped(event);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        // Si hace click en el nameInput, darle el foco
        if (event.button() == 0 && this.nameInput.isMouseOver(event.x(), event.y())) {
            this.nameInput.setFocused(true);
            return true;
        }
        // Si hace click fuera, quitar el foco
        if (event.button() == 0) {
            this.nameInput.setFocused(false);
        }
        return super.mouseClicked(event, doubleClick);
    }

    private void onSave(Button button) {
        String priceText = this.priceInput.getValue();
        String stockText = this.stockInput.getValue();
        String nameText = this.nameInput.getValue();

        boolean priceValid = isValidNumber(priceText, 1, 999_999_999);
        boolean stockValid = isValidNumber(stockText, 1, 1024);

        this.priceInput.setTextColor(
                priceValid ? 0xFFFFFFFF : 0xFFFF0000
        );

        this.stockInput.setTextColor(
                stockValid ? 0xFFFFFFFF : 0xFFFF0000
        );

        ItemStack sellItem = this.menu.getBlockEntity().getSellItem();
        if (sellItem.isEmpty()) {
            playErrorSound();
            return;
        }

        if (!priceValid) {
            playErrorSound();
            return;
        }

        if (this.isChecked && !stockValid) {
            playErrorSound();
            return;
        }

        if (this.minecraft == null || this.minecraft.player == null) {
            return;
        }

        int price = Integer.parseInt(priceText);

        int maxStock = this.isChecked ? Integer.parseInt(stockText) : 0;
        boolean hasLimitedStock = this.isChecked;

        SellBlockEntity blockEntity = this.menu.getBlockEntity();

        blockEntity.setSellItem(sellItem);
        blockEntity.setPrice(price);
        blockEntity.setMaxStock(maxStock);
        blockEntity.setHasLimitedStock(hasLimitedStock);
        blockEntity.setCustomName(nameText);

        net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(
                new SellConfigSavePacket(
                        this.menu.getPos(),
                        sellItem,
                        price,
                        maxStock,
                        hasLimitedStock,
                        nameText
                )
        );

        playSuccessSound();

        if (this.minecraft != null) {
            this.minecraft.gui.setScreen(null);
        }
    }

    private boolean isValidNumber(
            String text,
            int minimum,
            int maximum
    ) {
        if (text == null || text.isEmpty()) {
            return false;
        }

        try {
            int value = Integer.parseInt(text);
            return value >= minimum && value <= maximum;
        } catch (NumberFormatException exception) {
            return false;
        }
    }

    private void playErrorSound() {
        if (this.minecraft == null || this.minecraft.player == null) {
            return;
        }

        Level level = this.minecraft.player.level();

        level.playSound(
                this.minecraft.player,
                this.minecraft.player.blockPosition(),
                ModSounds.ERROR,
                SoundSource.UI,
                1.0F,
                1.0F
        );
    }

    private void playSuccessSound() {
        if (this.minecraft == null || this.minecraft.player == null) {
            return;
        }

        Level level = this.minecraft.player.level();

        level.playSound(
                this.minecraft.player,
                this.minecraft.player.blockPosition(),
                SoundEvents.EXPERIENCE_ORB_PICKUP,
                SoundSource.UI,
                1.0F,
                1.0F
        );
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
                this.title,
                this.leftPos + this.titleLabelX,
                this.topPos + this.titleLabelY,
                0x404040,
                false
        );

        graphics.text(
                this.font,
                this.playerInventoryTitle,
                this.leftPos + this.inventoryLabelX,
                this.topPos + this.inventoryLabelY,
                0x404040,
                false
        );

        super.extractRenderState(graphics, mouseX, mouseY, delta);
    }

    // Clase interna del botón custom
    public class CustomButton extends Button {

        public CustomButton(
                int x,
                int y,
                int width,
                int height,
                Component message,
                OnPress onPress
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
            boolean isActive = SellConfigScreen.this.isChecked;

            int textureV = 0;

            if (isActive) {
                if (isHovered) {
                    textureV = 36;
                } else {
                    textureV = 12;
                }
            } else {
                if (isHovered) {
                    textureV = 24;
                } else {
                    textureV = 0;
                }
            }

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

            int textColor = 0xFFFFFF;
            if (!this.isActive()) {
                textColor = 0x808080;
            }

            graphics.text(
                    Minecraft.getInstance().font,
                    this.getMessage(),
                    this.getX() + this.width + 4,
                    this.getY() + (this.height - 8) / 2,
                    textColor,
                    false
            );
        }
    }
}