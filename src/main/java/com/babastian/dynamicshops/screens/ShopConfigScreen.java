package com.babastian.dynamicshops.screens;

import com.babastian.dynamicshops.DynamicShops;
import com.babastian.dynamicshops.blocks.ShopBlockEntity;
import com.babastian.dynamicshops.network.ShopConfigSavePacket;
import com.babastian.dynamicshops.sounds.ModSounds;
import com.mojang.authlib.GameProfile;
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
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ShopConfigScreen
        extends AbstractContainerScreen<ShopConfigScreenHandler> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(
                    DynamicShops.MOD_ID,
                    "textures/gui/shop_config.png"
            );

    private static final int IMAGE_WIDTH = 176;
    private static final int IMAGE_HEIGHT = 166;

    private EditBox priceInput;
    private EditBox stockInput;
    private EditBox nameInput;
    private CustomButton toggleButton;
    private Button doneButton;
    private Button collaboratorButton;

    private boolean isChecked = false;

    // Variables para el sistema de colaborador
    private UUID selectedCollaboratorUuid = null;
    private boolean showCollaboratorDropdown = false;
    private final List<Button> dropdownButtons = new ArrayList<>();

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

    private static final int COLLABORATOR_BUTTON_X = 173;
    private static final int COLLABORATOR_BUTTON_Y = 1;
    private static final int COLLABORATOR_BUTTON_WIDTH = 80;
    private static final int COLLABORATOR_BUTTON_HEIGHT = 15;

    private static final int DONE_BUTTON_X = 85;
    private static final int DONE_BUTTON_Y = 47;
    private static final int DONE_BUTTON_WIDTH = 80;
    private static final int DONE_BUTTON_HEIGHT = 15;

    public ShopConfigScreen(
            ShopConfigScreenHandler handler,
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

        ShopBlockEntity blockEntity = this.menu.getBlockEntity();
        this.isChecked = blockEntity.hasLimitedStock();
        this.selectedCollaboratorUuid = blockEntity.getCollaboratorUuid();

        this.priceInput = new EditBox(
                this.font,
                x + PRICE_INPUT_X,
                y + PRICE_INPUT_Y,
                PRICE_INPUT_WIDTH,
                PRICE_INPUT_HEIGHT,
                Component.literal("")
        );

        this.priceInput.setMaxLength(9);
        this.priceInput.setValue(String.valueOf(blockEntity.getPrice()));
        this.priceInput.setResponder(text -> {
            if (!text.isEmpty()) {
                try {
                    int value = Integer.parseInt(text);
                    this.priceInput.setTextColor(value > 999_999_999 ? 0xFFFF0000 : 0xFFFFFFFF);
                } catch (NumberFormatException e) {
                    this.priceInput.setTextColor(0xFFFF0000);
                }
            } else {
                this.priceInput.setTextColor(0xFFFFFFFF);
            }
        });
        this.priceInput.setTooltip(Tooltip.create(Component.translatable("tooltip.dynamicshops.buy_price")));
        this.addRenderableWidget(this.priceInput);

        this.stockInput = new EditBox(
                this.font,
                x + STOCK_INPUT_X,
                y + STOCK_INPUT_Y,
                STOCK_INPUT_WIDTH,
                STOCK_INPUT_HEIGHT,
                Component.literal("")
        );
        this.stockInput.setMaxLength(4);
        this.stockInput.setValue(String.valueOf(blockEntity.getMaxStock()));
        this.stockInput.setResponder(text -> {
            if (!text.isEmpty()) {
                try {
                    int value = Integer.parseInt(text);
                    this.stockInput.setTextColor(value > 1024 ? 0xFFFF0000 : 0xFFFFFFFF);
                } catch (NumberFormatException e) {
                    this.stockInput.setTextColor(0xFFFF0000);
                }
            } else {
                this.stockInput.setTextColor(0xFFFFFFFF);
            }
        });
        this.stockInput.setTooltip(Tooltip.create(Component.translatable("tooltip.dynamicshops.stock")));
        this.addRenderableWidget(this.stockInput);

        this.nameInput = new EditBox(
                this.font,
                x + NAME_INPUT_X,
                y + NAME_INPUT_Y,
                NAME_INPUT_WIDTH,
                NAME_INPUT_HEIGHT,
                Component.literal("")
        );
        this.nameInput.setMaxLength(64);
        this.nameInput.setValue(blockEntity.getCustomName());
        this.nameInput.setTooltip(Tooltip.create(Component.translatable("tooltip.dynamicshops.custom_name")));
        this.nameInput.setCanLoseFocus(true);
        this.nameInput.setFocused(false);
        this.addRenderableWidget(this.nameInput);

        this.toggleButton = new CustomButton(
                x + TOGGLE_BUTTON_X,
                y + TOGGLE_BUTTON_Y,
                TOGGLE_BUTTON_WIDTH,
                TOGGLE_BUTTON_HEIGHT,
                Component.literal(isChecked ? "[✓] Stock limitado" : "[ ] Stock limitado"),
                button -> {
                    isChecked = !isChecked;
                    button.setMessage(Component.literal(isChecked ? "[✓] Stock limitado" : "[ ] Stock limitado"));
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
        this.toggleButton.setTooltip(Tooltip.create(Component.translatable("tooltip.dynamicshops.limited_stock")));
        this.addRenderableWidget(this.toggleButton);

        this.stockInput.setEditable(this.isChecked);
        if (!this.isChecked) {
            this.stockInput.setValue("0");
            this.stockInput.setTextColor(0x808080);
        }

        // ==========================================
        // BOTÓN PRINCIPAL DE COLABORADOR
        // ==========================================
        Identifier currentSkinTexture = null;
        String resolvedName = resolvePlayerName(this.selectedCollaboratorUuid);
        Component mainLabel;

        if (this.selectedCollaboratorUuid == null) {
            mainLabel = Component.translatable("button.dynamicshops.collaborator.none");
        } else {
            mainLabel = Component.translatable("button.dynamicshops.collaborator.assigned", resolvedName);
        }

        if (this.selectedCollaboratorUuid != null && this.minecraft != null && this.minecraft.getConnection() != null) {
            for (var entry : this.minecraft.getConnection().getOnlinePlayers()) {
                if (entry.getProfile().id().equals(this.selectedCollaboratorUuid)) {
                    PlayerSkin skin = entry.getSkin();
                    if (skin != null && skin.body() != null) {
                        currentSkinTexture = skin.body().texturePath();
                    }
                    break;
                }
            }
        }

        this.collaboratorButton = new CollaboratorMainButton(
                x + COLLABORATOR_BUTTON_X,
                y + COLLABORATOR_BUTTON_Y,
                COLLABORATOR_BUTTON_WIDTH,
                COLLABORATOR_BUTTON_HEIGHT,
                mainLabel,
                currentSkinTexture,
                b -> toggleCollaboratorDropdown()
        );
        this.collaboratorButton.setTooltip(Tooltip.create(Component.translatable("tooltip.dynamicshops.collaborator")));
        this.addRenderableWidget(this.collaboratorButton);

        this.doneButton = Button.builder(
                        Component.translatable("button.dynamicshops.save"),
                        this::onSave
                ).bounds(x + DONE_BUTTON_X, y + DONE_BUTTON_Y, DONE_BUTTON_WIDTH, DONE_BUTTON_HEIGHT)
                .tooltip(Tooltip.create(Component.translatable("tooltip.dynamicshops.save")))
                .build();
        this.addRenderableWidget(this.doneButton);
    }

    private String resolvePlayerName(UUID uuid) {
        if (uuid == null) return Component.translatable("display.dynamicshops.collaborator.none_name").getString();

        ShopBlockEntity be = this.menu.getBlockEntity();
        if (be != null && uuid.equals(be.getCollaboratorUuid()) && be.getCollaboratorName() != null) {
            return be.getCollaboratorName();
        }

        if (this.minecraft != null) {
            if (this.minecraft.hasSingleplayerServer() && this.minecraft.getSingleplayerServer() != null) {
                for (var serverPlayer : this.minecraft.getSingleplayerServer().getPlayerList().getPlayers()) {
                    if (serverPlayer.getGameProfile().id().equals(uuid)) {
                        return serverPlayer.getGameProfile().name();
                    }
                }
            } else if (this.minecraft.getConnection() != null) {
                for (var entry : this.minecraft.getConnection().getOnlinePlayers()) {
                    if (entry.getProfile().id().equals(uuid)) {
                        return entry.getProfile().name();
                    }
                }
            }
        }
        return Component.translatable("display.dynamicshops.collaborator.unknown_name").getString();
    }

    private void toggleCollaboratorDropdown() {
        this.showCollaboratorDropdown = !this.showCollaboratorDropdown;

        for (Button btn : dropdownButtons) {
            this.removeWidget(btn);
        }
        dropdownButtons.clear();

        if (this.showCollaboratorDropdown && this.minecraft != null) {
            int startX = this.leftPos + COLLABORATOR_BUTTON_X;
            int startY = this.topPos + COLLABORATOR_BUTTON_Y + COLLABORATOR_BUTTON_HEIGHT;

            Button noneBtn = new DropdownItemButton(
                    startX, startY, COLLABORATOR_BUTTON_WIDTH, 14,
                    Component.translatable("button.dynamicshops.collaborator.dropdown_none"), null, b -> {
                this.selectedCollaboratorUuid = null;

                this.removeWidget(this.collaboratorButton);
                this.collaboratorButton = new CollaboratorMainButton(
                        this.leftPos + COLLABORATOR_BUTTON_X,
                        this.topPos + COLLABORATOR_BUTTON_Y,
                        COLLABORATOR_BUTTON_WIDTH,
                        COLLABORATOR_BUTTON_HEIGHT,
                        Component.translatable("button.dynamicshops.collaborator.none"),
                        null,
                        btnAction -> toggleCollaboratorDropdown()
                );
                this.collaboratorButton.setTooltip(Tooltip.create(Component.translatable("tooltip.dynamicshops.collaborator")));
                this.addRenderableWidget(this.collaboratorButton);

                toggleCollaboratorDropdown();
            });

            this.addRenderableWidget(noneBtn);
            dropdownButtons.add(noneBtn);
            startY += 14;

            List<GameProfile> profiles = new ArrayList<>();
            if (this.minecraft.hasSingleplayerServer() && this.minecraft.getSingleplayerServer() != null) {
                for (var serverPlayer : this.minecraft.getSingleplayerServer().getPlayerList().getPlayers()) {
                    profiles.add(serverPlayer.getGameProfile());
                }
            } else if (this.minecraft.getConnection() != null) {
                for (var entry : this.minecraft.getConnection().getOnlinePlayers()) {
                    profiles.add(entry.getProfile());
                }
            }

            for (var profile : profiles) {
                final UUID profileUuid = profile.id();
                String profileName = profile.name();

                Identifier skinTexture = null;
                if (this.minecraft.getConnection() != null) {
                    for (var entry : this.minecraft.getConnection().getOnlinePlayers()) {
                        if (entry.getProfile().id().equals(profileUuid)) {
                            PlayerSkin skin = entry.getSkin();
                            if (skin != null && skin.body() != null) {
                                skinTexture = skin.body().texturePath();
                            }
                            break;
                        }
                    }
                }

                final Identifier finalSkinTexture = skinTexture;
                Button playerBtn = new DropdownItemButton(
                        startX, startY, COLLABORATOR_BUTTON_WIDTH, 14,
                        Component.literal(profileName), skinTexture, b -> {
                    this.selectedCollaboratorUuid = profileUuid;

                    this.removeWidget(this.collaboratorButton);
                    this.collaboratorButton = new CollaboratorMainButton(
                            this.leftPos + COLLABORATOR_BUTTON_X,
                            this.topPos + COLLABORATOR_BUTTON_Y,
                            COLLABORATOR_BUTTON_WIDTH,
                            COLLABORATOR_BUTTON_HEIGHT,
                            Component.translatable("button.dynamicshops.collaborator.assigned", profileName),
                            finalSkinTexture,
                            btnAction -> toggleCollaboratorDropdown()
                    );
                    this.collaboratorButton.setTooltip(Tooltip.create(Component.translatable("tooltip.dynamicshops.collaborator")));
                    this.addRenderableWidget(this.collaboratorButton);

                    toggleCollaboratorDropdown();
                });

                this.addRenderableWidget(playerBtn);
                dropdownButtons.add(playerBtn);
                startY += 14;
            }

            this.removeWidget(this.collaboratorButton);
            this.addRenderableWidget(this.collaboratorButton);
        }
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (this.nameInput.isFocused()) {
            if (this.nameInput.keyPressed(event)) {
                return true;
            }
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        if (this.nameInput.isFocused() && this.nameInput.charTyped(event)) {
            return true;
        }
        return super.charTyped(event);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 0 && this.nameInput.isMouseOver(event.x(), event.y())) {
            this.nameInput.setFocused(true);
            return true;
        }
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

        this.priceInput.setTextColor(priceValid ? 0xFFFFFFFF : 0xFFFF0000);
        this.stockInput.setTextColor(stockValid ? 0xFFFFFFFF : 0xFFFF0000);

        ItemStack buyItem = this.menu.getBlockEntity().getBuyItem();
        if (buyItem.isEmpty() || !priceValid || (this.isChecked && !stockValid)) {
            playErrorSound();
            return;
        }

        if (this.minecraft == null || this.minecraft.player == null) return;

        int price = Integer.parseInt(priceText);
        int maxStock = this.isChecked ? Integer.parseInt(stockText) : 0;
        boolean hasLimitedStock = this.isChecked;

        ShopBlockEntity blockEntity = this.menu.getBlockEntity();

        blockEntity.setBuyItem(buyItem);
        blockEntity.setPrice(price);
        blockEntity.setMaxStock(maxStock);
        blockEntity.setHasLimitedStock(hasLimitedStock);
        blockEntity.setCustomName(nameText);
        blockEntity.setCollaboratorUuid(this.selectedCollaboratorUuid);

        net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(
                new ShopConfigSavePacket(
                        this.menu.getPos(),
                        buyItem,
                        price,
                        maxStock,
                        hasLimitedStock,
                        nameText,
                        this.selectedCollaboratorUuid
                )
        );

        playSuccessSound();
        this.minecraft.gui.setScreen(null);
    }

    private boolean isValidNumber(String text, int minimum, int maximum) {
        if (text == null || text.isEmpty()) return false;
        try {
            int value = Integer.parseInt(text);
            return value >= minimum && value <= maximum;
        } catch (NumberFormatException exception) {
            return false;
        }
    }

    private void playErrorSound() {
        if (this.minecraft == null || this.minecraft.player == null) return;
        this.minecraft.player.level().playSound(
                this.minecraft.player, this.minecraft.player.blockPosition(),
                ModSounds.ERROR, SoundSource.UI, 1.0F, 1.0F
        );
    }

    private void playSuccessSound() {
        if (this.minecraft == null || this.minecraft.player == null) return;
        this.minecraft.player.level().playSound(
                this.minecraft.player, this.minecraft.player.blockPosition(),
                SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.UI, 1.0F, 1.0F
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

        graphics.text(this.font, this.title, this.leftPos + this.titleLabelX, this.topPos + this.titleLabelY, 0x404040, false);
        graphics.text(this.font, this.playerInventoryTitle, this.leftPos + this.inventoryLabelX, this.topPos + this.inventoryLabelY, 0x404040, false);

        super.extractRenderState(graphics, mouseX, mouseY, delta);
    }

    public class CustomButton extends Button {
        public CustomButton(int x, int y, int width, int height, Component message, OnPress onPress) {
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
            boolean isActive = ShopConfigScreen.this.isChecked;

            int textureV = isActive ? (isHovered ? 36 : 12) : (isHovered ? 24 : 0);

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

            int textColor = this.isActive() ? 0xFFFFFF : 0x808080;

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

    public static class CollaboratorMainButton extends Button {
        private final Identifier skinTexture;

        public CollaboratorMainButton(int x, int y, int width, int height, Component message, Identifier skinTexture, OnPress onPress) {
            super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
            this.skinTexture = skinTexture;
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

            int bgColor = isHovered ? 0xCC252525 : 0xEE151515;
            graphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, bgColor);

            int borderColor = isHovered ? 0xFF777777 : 0xFF444444;
            graphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + 1, borderColor);
            graphics.fill(this.getX(), this.getY() + this.height - 1, this.getX() + this.width, this.getY() + this.height, borderColor);
            graphics.fill(this.getX(), this.getY(), this.getX() + 1, this.getY() + this.height, borderColor);
            graphics.fill(this.getX() + this.width - 1, this.getY(), this.getX() + this.width, this.getY() + this.height, borderColor);

            int textXOffset = 4;

            if (this.skinTexture != null) {
                int headX = this.getX() + 3;
                int headY = this.getY() + (this.height - 8) / 2;
                int headSize = 8;

                graphics.blit(
                        RenderPipelines.GUI_TEXTURED,
                        this.skinTexture,
                        headX, headY,
                        8, 8,
                        headSize, headSize,
                        64, 64
                );
                graphics.blit(
                        RenderPipelines.GUI_TEXTURED,
                        this.skinTexture,
                        headX, headY,
                        40, 8,
                        headSize, headSize,
                        64, 64
                );
                textXOffset = 14;
            }

            Minecraft mc = Minecraft.getInstance();
            int maxTextWidth = this.width - textXOffset - 4;
            String fullText = this.getMessage().getString();
            String renderText = fullText;

            if (mc.font.width(renderText) > maxTextWidth) {
                int ellipsisWidth = mc.font.width("...");
                renderText = mc.font.plainSubstrByWidth(fullText, maxTextWidth - ellipsisWidth) + "...";
            }

            int textColor = isHovered ? 0xFFFFFFFF : 0xFFDDDDDD;
            graphics.text(
                    mc.font,
                    Component.literal(renderText),
                    this.getX() + textXOffset,
                    this.getY() + (this.height - 8) / 2,
                    textColor,
                    false
            );
        }
    }

    public static class DropdownItemButton extends Button {
        private final Identifier skinTexture;

        public DropdownItemButton(int x, int y, int width, int height, Component message, Identifier skinTexture, OnPress onPress) {
            super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
            this.skinTexture = skinTexture;
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
            if (this.visible && this.active && this.isMouseOver(event.x(), event.y())) {
                this.playDownSound(Minecraft.getInstance().getSoundManager());
                this.onPress(null);
                return true;
            }
            return super.mouseClicked(event, doubleClick);
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

            int bgColor = isHovered ? 0xCC202020 : 0xEE101010;
            graphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, bgColor);

            int borderColor = isHovered ? 0xFF666666 : 0xFF333333;
            graphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + 1, borderColor);
            graphics.fill(this.getX(), this.getY() + this.height - 1, this.getX() + this.width, this.getY() + this.height, borderColor);
            graphics.fill(this.getX(), this.getY(), this.getX() + 1, this.getY() + this.height, borderColor);
            graphics.fill(this.getX() + this.width - 1, this.getY(), this.getX() + this.width, this.getY() + this.height, borderColor);

            int textXOffset = 4;

            if (this.skinTexture != null) {
                int headX = this.getX() + 3;
                int headY = this.getY() + 3;
                int headSize = 8;

                graphics.blit(
                        RenderPipelines.GUI_TEXTURED,
                        this.skinTexture,
                        headX, headY,
                        8, 8,
                        headSize, headSize,
                        64, 64
                );
                graphics.blit(
                        RenderPipelines.GUI_TEXTURED,
                        this.skinTexture,
                        headX, headY,
                        40, 8,
                        headSize, headSize,
                        64, 64
                );
                textXOffset = 15;
            }

            int textColor = isHovered ? 0xFFFFFFFF : 0xFFCCCCCC;
            graphics.text(
                    Minecraft.getInstance().font,
                    this.getMessage(),
                    this.getX() + textXOffset,
                    this.getY() + (this.height - 8) / 2,
                    textColor,
                    false
            );
        }
    }
}