package com.babastian.dynamicshops.items;

import com.babastian.dynamicshops.money.MoneyComponent;
import com.babastian.dynamicshops.sounds.ModSounds;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class CurrencyItem extends Item {
    private final int value;

    public CurrencyItem(Properties properties, int value) {
        super(properties);
        this.value = value;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        // Si ya tiene el dinero máximo, se cancela la acción
        if (MoneyComponent.isAtMax(player)) {
            return InteractionResult.FAIL;
        }

        int currentMoney = MoneyComponent.getBalance(player);
        int moneyPerItem = this.value;
        int itemsToConsume = 1;

        // === SHIFT + CLICK DERECHO: Consumir todo el stack ===
        boolean isSneaking = player.isCrouching();
        if (isSneaking && stack.getCount() > 1) {
            // Calcular cuántos items podemos consumir sin pasar el máximo
            int maxMoneyWeCanAdd = MoneyComponent.MAX_BALANCE - currentMoney;
            int maxItemsWeCanUse = maxMoneyWeCanAdd / moneyPerItem;

            // Si no podemos usar ni uno, cancelar
            if (maxItemsWeCanUse <= 0) {
                return InteractionResult.FAIL;
            }

            // Usar el menor entre: items en stack, items que podemos usar
            itemsToConsume = Math.min(stack.getCount(), maxItemsWeCanUse);
        }

        // Calcular dinero total a añadir
        int moneyToAdd = moneyPerItem * itemsToConsume;

        // === CORRECCIÓN: Si sobrepasa el máximo, igual se consume el item pero solo añade lo que falta ===
        int actualMoneyToAdd = moneyToAdd;
        int actualItemsToConsume = itemsToConsume;

        if (currentMoney + moneyToAdd > MoneyComponent.MAX_BALANCE) {
            // Solo añade lo que falta para el máximo
            actualMoneyToAdd = MoneyComponent.MAX_BALANCE - currentMoney;
            // Igual consume los items calculados (aunque no añada el valor completo)
        }

        // Añade el dinero al bolsillo del jugador
        MoneyComponent.addMoney(player, actualMoneyToAdd);

        // 1. Formateamos el número con comas (ej. 100000 -> "100,000")
        java.text.NumberFormat format = java.text.NumberFormat.getNumberInstance(java.util.Locale.US);
        String formattedMoney = format.format(actualMoneyToAdd);

        // 2. Mensaje diferente si es shift+click o normal
        Component message = Component.literal("+$" + formattedMoney)
                .withStyle(Style.EMPTY.withColor(0x00AA00).withBold(Boolean.TRUE));

        // 3. Se envía a la actionbar SOLO del jugador que usó el ítem
        if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            serverPlayer.connection.send(new net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket(message));
        }

        // === SONIDOS DIFERENTES ===
        if (isSneaking) {
            // Shift + Click: Sonido más "épico" (nivel up)
            level.playSound(null, player.blockPosition(), ModSounds.MULTICOIN, SoundSource.PLAYERS, 1.0F, 1.0F);
        } else {
            // Click normal: Sonido de experiencia (original)
            level.playSound(null, player.blockPosition(), ModSounds.COIN, SoundSource.PLAYERS, 1.0F, 1.0F);
        }

        // Consume los items si no está en modo creativo
        if (!player.getAbilities().instabuild) {
            stack.shrink(actualItemsToConsume);
        }

        return InteractionResult.SUCCESS_SERVER;
    }
}