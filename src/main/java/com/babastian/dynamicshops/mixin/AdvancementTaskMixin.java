package com.babastian.dynamicshops.mixin;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.babastian.dynamicshops.tasks.TaskTracker;

@Mixin(PlayerAdvancements.class)
public class AdvancementTaskMixin {

    // Reemplaza la reflexión por un @Shadow nativo para acceder al jugador de forma limpia
    @Shadow private ServerPlayer player;

    @Inject(method = "award", at = @At("RETURN"))
    private void onAward(AdvancementHolder holder, String criterion, CallbackInfoReturnable<Boolean> cir) {
        // 1. Validar si el criterio realmente se otorgó en este tick
        if (!cir.getReturnValueZ()) return;
        if (player == null) return;

        // 2. FILTRO CLAVE: Ignorar logros ocultos (recetas, triggers técnicos, etc.)
        // Solo contamos si el logro tiene información visual para el usuario
        if (holder.value().display().isEmpty()) return;

        // 3. FILTRO ROOT: Ignorar logros raíz (no tienen logro padre)
        if (holder.value().parent().isEmpty()) return;

        // 4. FILTRO EXTRA: Evitar explícitamente la rama de recetas ocultas por si acaso
        if (holder.id().getPath().startsWith("recipes/")) return;

        // 5. Verificar si el logro se completó al 100% (todos sus criterios)
        PlayerAdvancements advancements = (PlayerAdvancements)(Object)this;
        var progress = advancements.getOrStartProgress(holder);
        if (!progress.isDone()) return;

        // 6. Incrementar el contador del jugador
        TaskTracker.incrementAndCheck(player, TaskTracker.ADVANCEMENTS, 1, "task.advancements");
    }
}