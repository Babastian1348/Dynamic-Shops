package com.babastian.dynamicshops.mixin;

import com.babastian.dynamicshops.config.ModConfig;
import com.babastian.dynamicshops.items.CurrencyItem;
import com.mojang.serialization.DataResult;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BundleContents;
import org.apache.commons.lang3.math.Fraction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BundleContents.class)
public class WalletContentsMixin {

    @Inject(method = "getWeight", at = @At("HEAD"), cancellable = true)
    private static void getCustomWeight(ItemInstance item, CallbackInfoReturnable<DataResult<Fraction>> cir) {
        ItemStack stack = item instanceof ItemStack ? (ItemStack) item : null;
        if (stack == null && item instanceof net.minecraft.world.item.ItemStackTemplate template) {
            stack = template.create();
        }

        if (stack != null && stack.getItem() instanceof CurrencyItem) {
            var resourceLocation = BuiltInRegistries.ITEM.getKey(stack.getItem());
            int value = ModConfig.getInstance().getCurrencyValue(resourceLocation.toString());
            if (value <= 0) value = 1;

            int maxCapacity = ModConfig.getInstance().MAX_CAPACITY;
            if (maxCapacity <= 0) maxCapacity = 10000;

            // CORRECTO: Peso de UNA SOLA unidad (el juego lo multiplica por stack.count() automáticamente).
            // Ejemplo: 1 de valor / 10,000 de capacidad máxima.
            Fraction weight = Fraction.getFraction(value, maxCapacity);
            cir.setReturnValue(DataResult.success(weight));
        }
    }
}