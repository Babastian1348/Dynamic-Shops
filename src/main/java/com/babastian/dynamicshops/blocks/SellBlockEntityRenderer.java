package com.babastian.dynamicshops.blocks;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;

import java.util.HashMap;
import java.util.Map;

public class SellBlockEntityRenderer implements BlockEntityRenderer<SellBlockEntity, SellBlockEntityRenderState> {

    private final BlockEntityRendererProvider.Context context;
    private final Minecraft minecraft;

    // === MAPAS PERSISTENTES PARA ALPHA ===
    private static final Map<BlockPos, Float> alphaMap = new HashMap<>();
    private static final Map<BlockPos, Boolean> visibleMap = new HashMap<>();

    public SellBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.context = context;
        this.minecraft = Minecraft.getInstance();
    }

    @Override
    public SellBlockEntityRenderState createRenderState() {
        return new SellBlockEntityRenderState();
    }

    @Override
    public void extractRenderState(
            SellBlockEntity blockEntity,
            SellBlockEntityRenderState state,
            float partialTicks,
            net.minecraft.world.phys.Vec3 cameraPosition,
            net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay breakProgress
    ) {
        state.reset();

        BlockEntityRenderState.extractBase(blockEntity, state, breakProgress);
        state.blockState = blockEntity.getBlockState();
        state.level = blockEntity.getLevel();
        state.setSellItem(blockEntity.getSellItem().copy());
        state.partialTicks = partialTicks;

        // === DATOS PARA EL DISPLAY ===
        state.itemName = blockEntity.getItemName();
        state.itemPrice = blockEntity.getPrice();

        // === USAR getCurrentStock() EN LUGAR DE getStockRemaining() ===
        state.currentStock = blockEntity.getCurrentStock();
        state.hasStock = state.currentStock > 0 || !blockEntity.hasLimitedStock();
        state.stockRemaining = state.currentStock; // Para compatibilidad

        state.timeUntilReset = blockEntity.getFormattedTimeUntilReset();
        state.hasLimitedStock = blockEntity.hasLimitedStock();
        state.nameColor = 0xFF000000 | blockEntity.getNameColor();

        // === DETECTAR SI EL JUGADOR ESTÁ MIRANDO EL BLOQUE (ENDERMAN STYLE) ===
        if (state.level != null && minecraft.player != null) {
            var cameraPos = minecraft.player.getEyePosition(partialTicks);
            var lookVec = minecraft.player.getLookAngle();

            // Vector hacia el bloque
            var blockVec = net.minecraft.world.phys.Vec3.atCenterOf(state.blockPos);
            var directionToBlock = blockVec.subtract(cameraPos).normalize();

            // Calcular ángulo entre la mirada y el bloque
            double dotProduct = lookVec.dot(directionToBlock);
            double angle = Math.acos(dotProduct) * (180.0 / Math.PI); // En grados

            // Distancia
            double distToBlock = cameraPos.distanceTo(blockVec);

            // Debe estar mirando dentro de ~15 grados Y a menos de 6 bloques
            boolean isLooking = angle < 15.0 && distToBlock < 6.0;

            // === USAR MAPAS PERSISTENTES ===
            Float currentAlpha = alphaMap.get(state.blockPos);
            if (currentAlpha == null) currentAlpha = 0.0f;

            float targetAlpha = isLooking ? 1.0f : 0.0f;

            // Aplicar lerp
            float newAlpha;
            if (Math.abs(currentAlpha - targetAlpha) < 0.01f) {
                newAlpha = targetAlpha;
            } else {
                float speed = isLooking ? 0.15f : 0.4f;
                newAlpha = Mth.lerp(partialTicks * speed, currentAlpha, targetAlpha);
            }

            alphaMap.put(state.blockPos, newAlpha);
            visibleMap.put(state.blockPos, isLooking);

            state.displayAlpha = newAlpha;

            // Limpiar si alpha es 0
            if (newAlpha < 0.01f && !isLooking) {
                alphaMap.remove(state.blockPos);
                visibleMap.remove(state.blockPos);
            }
        } else {
            state.displayAlpha = 0.0f;
        }

        // Actualizar el render state del item
        if (!state.getSellItem().isEmpty() && state.level != null) {
            state.itemRenderState.clear();
            context.itemModelResolver().updateForTopItem(
                    state.itemRenderState,
                    state.getSellItem(),
                    ItemDisplayContext.FIXED,
                    state.level,
                    null,
                    state.blockPos.hashCode()
            );
        }
    }

    @Override
    public void submit(
            SellBlockEntityRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            CameraRenderState camera
    ) {
        // === 1. OBTENER FACING ===
        var facing = net.minecraft.core.Direction.SOUTH;
        if (state.blockState.hasProperty(HorizontalDirectionalBlock.FACING)) {
            facing = state.blockState.getValue(HorizontalDirectionalBlock.FACING);
        }

        // === 2. TIEMPO PARA ANIMACIÓN ===
        long time = state.level != null ? state.level.getGameTime() : 0L;
        float gameTimeWithPartial = time + state.partialTicks;
        float floatOffset = Mth.sin(gameTimeWithPartial * 0.17f) * 0.07f;
        float spinAngle = gameTimeWithPartial * 2.1f;

        // === 3. RENDERIZAR ITEM (SIEMPRE VISIBLE) ===
        poseStack.pushPose();
        poseStack.translate(0.5, 1.3 + floatOffset, 0.5);

        float yaw = switch (facing) {
            case NORTH -> 180.0f;
            case SOUTH -> 0.0f;
            case WEST -> 90.0f;
            case EAST -> -90.0f;
            default -> 0.0f;
        };
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(spinAngle + yaw));
        poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(0.0f));
        poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(0.0f));

        float scale = 0.75f;
        poseStack.scale(scale, scale, scale);

        var sellItem = state.getSellItem();
        if (sellItem.isEmpty()) {
            var barrierRenderState = new ItemStackRenderState();
            var barrierStack = new net.minecraft.world.item.ItemStack(net.minecraft.world.level.block.Blocks.BARRIER);
            if (state.level != null) {
                context.itemModelResolver().updateForTopItem(
                        barrierRenderState,
                        barrierStack,
                        ItemDisplayContext.FIXED,
                        state.level,
                        null,
                        state.blockPos.hashCode()
                );
            }
            if (!barrierRenderState.isEmpty()) {
                barrierRenderState.submit(
                        poseStack,
                        submitNodeCollector,
                        state.lightCoords,
                        OverlayTexture.NO_OVERLAY,
                        EntityRenderState.NO_OUTLINE
                );
            }
        } else {
            if (!state.itemRenderState.isEmpty()) {
                state.itemRenderState.submit(
                        poseStack,
                        submitNodeCollector,
                        state.lightCoords,
                        OverlayTexture.NO_OVERLAY,
                        EntityRenderState.NO_OUTLINE
                );
            }
        }
        poseStack.popPose();

        // === 4. RENDERIZAR TEXTO (SOLO SI ALPHA > 0.01) ===
        if (state.displayAlpha > 0.01f) {
            renderTextDisplay(state, poseStack, submitNodeCollector, camera);
        }
    }

    private void renderTextDisplay(
            SellBlockEntityRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            CameraRenderState camera
    ) {
        float alpha = state.displayAlpha;
        int textColorBase = (int)(alpha * 255) << 24;

        poseStack.pushPose();

        // === POSICIÓN: Arriba del item ===
        poseStack.translate(0.5, 2.2, 0.5);

        // === ROTAR PARA QUE MIRE AL JUGADOR (Billboard) ===
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-camera.yRot + 180.0f));
        poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(180.0f));

        // === ESCALA DEL TEXTO ===
        float textScale = 0.025f;
        poseStack.scale(textScale, textScale, textScale);

        // === PREPARAR LÍNEAS DE TEXTO ===
        java.util.List<String> lines = new java.util.ArrayList<>();
        java.util.List<Integer> colors = new java.util.ArrayList<>();

// Línea 1: Nombre del item (YA VIENE DEL BLOCKENTITY, NO SE TRADUCE)
        lines.add(state.itemName);
        colors.add(state.nameColor);

// Línea 2: Precio
        lines.add(Component.translatable("display.dynamicshops.sale_price", state.itemPrice).getString());
        colors.add(0x00AA00);

// Línea 3: Stock (solo si limitado)
        if (state.hasLimitedStock && state.currentStock >= 0) {
            lines.add(Component.translatable("display.dynamicshops.limit_remaining", state.currentStock).getString());
            colors.add(0xFFAA00);
        }

// Línea 4: Tiempo restante (solo si limitado)
        if (state.hasLimitedStock && !state.timeUntilReset.isEmpty()) {
            lines.add(Component.translatable("display.dynamicshops.time_remaining", state.timeUntilReset).getString());
            colors.add(0xFFAA00);
        }

        // === RENDERIZAR CADA LÍNEA ===
        int lineHeight = 10;
        int totalHeight = lines.size() * lineHeight;
        int startY = -totalHeight / 2;

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            int color = colors.get(i);
            int textColor = textColorBase | (color & 0xFFFFFF);

            // === PRIMERA LÍNEA (NOMBRE) EN BOLD ===
            FormattedCharSequence text;
            if (i == 0) {
                // Nombre en negritas
                text = Component.literal(line).withStyle(style -> style.withBold(true)).getVisualOrderText();
            } else {
                // Otras líneas normal
                text = Component.literal(line).getVisualOrderText();
            }

            int textWidth = minecraft.font.width(text);

            // === TEXTO SIN SOMBRA ===
            submitNodeCollector.submitText(
                    poseStack,
                    -textWidth / 2f,
                    startY + i * lineHeight,
                    text,
                    false, // dropShadow = false
                    net.minecraft.client.gui.Font.DisplayMode.NORMAL,
                    state.lightCoords,
                    textColor,
                    0,
                    0
            );
        }

        poseStack.popPose();
    }
}