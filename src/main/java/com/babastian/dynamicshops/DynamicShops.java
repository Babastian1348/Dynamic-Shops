package com.babastian.dynamicshops;

import com.babastian.dynamicshops.blocks.ModBlocks;
import com.babastian.dynamicshops.commands.MoneyCommands;
import com.babastian.dynamicshops.commands.TaskCommands;
import com.babastian.dynamicshops.config.TaskConfig;
import com.babastian.dynamicshops.items.ModItems;
import com.babastian.dynamicshops.money.MoneyComponent;
import com.babastian.dynamicshops.network.ModPackets;
import com.babastian.dynamicshops.screens.ModScreenHandlers;
import com.babastian.dynamicshops.sounds.ModSounds;
import com.babastian.dynamicshops.tasks.TaskListeners;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DynamicShops implements ModInitializer {
	public static final String MOD_ID = "dynamic-shops";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// Registrar componente de dinero
		MoneyComponent.register();

		// Registrar comandos
		MoneyCommands.register();
		TaskCommands.register();

		// Registrar bloques
		ModBlocks.register();
		ModBlocks.registerBlockEntities();

		// Registrar items
		ModItems.register();

		// Registrar creative tabs
		ModCreativeTabs.register();

		// Registrar screen handlers
		ModScreenHandlers.register();

		// Registrar packets
		ModPackets.register();

		// Registrar sonidos
		ModSounds.register();

		// Registrar listeners de tasks
		TaskListeners.register();

		// Cargar config
		TaskConfig.getInstance();

		// -------------------------------------------------------------------
		// SISTEMA DE MUERTE: Penalización del 50% en bolsillo, 0% en banco
		// -------------------------------------------------------------------
		ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
			int oldPocketMoney = MoneyComponent.getBalance(oldPlayer);
			int oldBankMoney = MoneyComponent.getBankBalance(oldPlayer);

			if (!alive) { // Si el jugador murió (no si solo cambió de dimensión)
				int newPocketMoney = oldPocketMoney / 2; // Pierde el 50%
				MoneyComponent.setBalance(newPlayer, newPocketMoney);
				MoneyComponent.setBankBalance(newPlayer, oldBankMoney); // Mantiene 100% del banco
			} else { // Si solo viajó al End/Nether
				MoneyComponent.setBalance(newPlayer, oldPocketMoney);
				MoneyComponent.setBankBalance(newPlayer, oldBankMoney);
			}
		});

		LOGGER.info("DynamicShops is ready to tear the economy apart.");
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}