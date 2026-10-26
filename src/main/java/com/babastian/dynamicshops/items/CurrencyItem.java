package com.babastian.dynamicshops.items;

import com.babastian.dynamicshops.config.ModConfig;
import com.babastian.dynamicshops.money.MoneyComponent;
import com.babastian.dynamicshops.sounds.ModSounds;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.function.Consumer;

public class CurrencyItem extends Item {

    public CurrencyItem(Properties properties) {
        super(properties);
    }

    public int getValue(ItemStack stack) {
        var resourceLocation = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return ModConfig.getInstance().getCurrencyValue(resourceLocation.toString());
    }

    @Override
    @SuppressWarnings("deprecation")
    public void appendHoverText(ItemStack itemStack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag tooltipFlag) {
        int value = getValue(itemStack);
        NumberFormat format = NumberFormat.getNumberInstance(Locale.US);

        // Creamos la parte del dinero coloreada completamente en verde oscuro
        Component formattedValue = Component.literal("$" + format.format(value))
                .withStyle(net.minecraft.ChatFormatting.DARK_GREEN);

        // Inyectamos el componente ya coloreado en la traducción
        builder.accept(Component.translatable("tooltip.dynamicshops.currency.value", formattedValue));

        super.appendHoverText(itemStack, context, display, builder, tooltipFlag);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        if (MoneyComponent.isAtMax(player)) {
            return InteractionResult.FAIL;
        }

        int currentMoney = MoneyComponent.getBalance(player);
        int moneyPerItem = getValue(stack);
        int itemsToConsume = 1;

        boolean isSneaking = player.isCrouching();
        if (isSneaking && stack.getCount() > 1) {
            int maxMoneyWeCanAdd = MoneyComponent.MAX_BALANCE - currentMoney;
            int maxItemsWeCanUse = maxMoneyWeCanAdd / moneyPerItem;

            if (maxItemsWeCanUse <= 0) {
                return InteractionResult.FAIL;
            }

            itemsToConsume = Math.min(stack.getCount(), maxItemsWeCanUse);
        }

        int moneyToAdd = moneyPerItem * itemsToConsume;
        int actualMoneyToAdd = moneyToAdd;
        int actualItemsToConsume = itemsToConsume;

        if (currentMoney + moneyToAdd > MoneyComponent.MAX_BALANCE) {
            actualMoneyToAdd = MoneyComponent.MAX_BALANCE - currentMoney;
        }

        MoneyComponent.addMoney(player, actualMoneyToAdd);

        NumberFormat format = NumberFormat.getNumberInstance(Locale.US);
        String formattedMoney = format.format(actualMoneyToAdd);

        Component message = Component.literal("+$" + formattedMoney)
                .withStyle(Style.EMPTY.withColor(0x00AA00).withBold(Boolean.TRUE));

        if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            serverPlayer.connection.send(new net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket(message));
        }

        if (isSneaking) {
            level.playSound(null, player.blockPosition(), ModSounds.MULTICOIN, SoundSource.PLAYERS, 1.0F, 1.0F);
        } else {
            level.playSound(null, player.blockPosition(), ModSounds.COIN, SoundSource.PLAYERS, 1.0F, 1.0F);
        }

        if (!player.getAbilities().instabuild) {
            stack.shrink(actualItemsToConsume);
        }

        return InteractionResult.SUCCESS_SERVER;
    }
}