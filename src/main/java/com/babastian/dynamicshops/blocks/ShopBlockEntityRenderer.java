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

public class ShopBlockEntityRenderer implements BlockEntityRenderer<ShopBlockEntity, ShopBlockEntityRenderState> {

    private final BlockEntityRendererProvider.Context context;
    private final Minecraft minecraft;

    private static final Map<BlockPos, Float> alphaMap = new HashMap<>();
    private static final Map<BlockPos, Boolean> visibleMap = new HashMap<>();

    public ShopBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.context = context;
        this.minecraft = Minecraft.getInstance();
    }

    @Override
    public ShopBlockEntityRenderState createRenderState() {
        return new ShopBlockEntityRenderState();
    }

    @Override
    public void extractRenderState(
            ShopBlockEntity blockEntity,
            ShopBlockEntityRenderState state,
            float partialTicks,
            net.minecraft.world.phys.Vec3 cameraPosition,
            net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay breakProgress
    ) {
        state.reset();

        BlockEntityRenderState.extractBase(blockEntity, state, breakProgress);
        state.blockState = blockEntity.getBlockState();
        state.level = blockEntity.getLevel();
        state.setBuyItem(blockEntity.getBuyItem().copy());
        state.partialTicks = partialTicks;

        state.itemName = blockEntity.getItemName();
        state.itemPrice = blockEntity.getPrice();

        state.currentStock = blockEntity.getCurrentStock();
        state.hasStock = state.currentStock > 0 || !blockEntity.hasLimitedStock();
        state.stockRemaining = state.currentStock;

        state.timeUntilReset = blockEntity.getFormattedTimeUntilReset();
        state.hasLimitedStock = blockEntity.hasLimitedStock();
        state.nameColor = 0xFF000000 | blockEntity.getNameColor();

        if (state.level != null && minecraft.player != null) {
            var cameraPos = minecraft.player.getEyePosition(partialTicks);
            var lookVec = minecraft.player.getLookAngle();

            var blockVec = net.minecraft.world.phys.Vec3.atCenterOf(state.blockPos);
            var directionToBlock = blockVec.subtract(cameraPos).normalize();

            double dotProduct = lookVec.dot(directionToBlock);
            double angle = Math.acos(dotProduct) * (180.0 / Math.PI);

            double distToBlock = cameraPos.distanceTo(blockVec);

            boolean isLooking = angle < 15.0 && distToBlock < 6.0;

            Float currentAlpha = alphaMap.get(state.blockPos);
            if (currentAlpha == null) currentAlpha = 0.0f;

            float targetAlpha = isLooking ? 1.0f : 0.0f;

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

            if (newAlpha < 0.01f && !isLooking) {
                alphaMap.remove(state.blockPos);
                visibleMap.remove(state.blockPos);
            }
        } else {
            state.displayAlpha = 0.0f;
        }

        if (!state.getBuyItem().isEmpty() && state.level != null) {
            state.itemRenderState.clear();
            context.itemModelResolver().updateForTopItem(
                    state.itemRenderState,
                    state.getBuyItem(),
                    ItemDisplayContext.FIXED,
                    state.level,
                    null,
                    state.blockPos.hashCode()
            );
        }
    }

    @Override
    public void submit(
            ShopBlockEntityRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            CameraRenderState camera
    ) {
        var facing = net.minecraft.core.Direction.SOUTH;
        if (state.blockState.hasProperty(HorizontalDirectionalBlock.FACING)) {
            facing = state.blockState.getValue(HorizontalDirectionalBlock.FACING);
        }

        long time = state.level != null ? state.level.getGameTime() : 0L;
        float gameTimeWithPartial = time + state.partialTicks;
        float floatOffset = Mth.sin(gameTimeWithPartial * 0.17f) * 0.07f;
        float spinAngle = gameTimeWithPartial * 2.1f;

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

        var buyItem = state.getBuyItem();
        if (buyItem.isEmpty()) {
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

        if (state.displayAlpha > 0.01f) {
            renderTextDisplay(state, poseStack, submitNodeCollector, camera);
        }
    }

    private void renderTextDisplay(
            ShopBlockEntityRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            CameraRenderState camera
    ) {
        float alpha = state.displayAlpha;
        int textColorBase = (int)(alpha * 255) << 24;

        poseStack.pushPose();

        poseStack.translate(0.5, 2.2, 0.5);

        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-camera.yRot + 180.0f));
        poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(180.0f));

        float textScale = 0.025f;
        poseStack.scale(textScale, textScale, textScale);

        java.util.List<String> lines = new java.util.ArrayList<>();
        java.util.List<Integer> colors = new java.util.ArrayList<>();

        lines.add(state.itemName);
        colors.add(state.nameColor);

        lines.add(Component.translatable("display.dynamicshops.shop_price", state.itemPrice).getString());
        colors.add(0x00AA00);

        if (state.hasLimitedStock && state.currentStock >= 0) {
            lines.add(Component.translatable("display.dynamicshops.stock_remaining", state.currentStock).getString());
            colors.add(0xFFAA00);
        }

        if (state.hasLimitedStock && !state.timeUntilReset.isEmpty()) {
            lines.add(Component.translatable("display.dynamicshops.time_remaining", state.timeUntilReset).getString());
            colors.add(0xFFAA00);
        }

        int lineHeight = 10;
        int totalHeight = lines.size() * lineHeight;
        int startY = -totalHeight / 2;

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            int color = colors.get(i);
            int textColor = textColorBase | (color & 0xFFFFFF);

            FormattedCharSequence text;
            if (i == 0) {
                text = Component.literal(line).withStyle(style -> style.withBold(true)).getVisualOrderText();
            } else {
                text = Component.literal(line).getVisualOrderText();
            }

            int textWidth = minecraft.font.width(text);

            submitNodeCollector.submitText(
                    poseStack,
                    -textWidth / 2f,
                    startY + i * lineHeight,
                    text,
                    false,
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