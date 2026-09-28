package io.github.piscescup.fabricmc;

import io.github.piscescup.fabricmc.command.SettingsCommand;
import io.github.piscescup.fabricmc.config.ConfigManager;
import io.github.piscescup.fabricmc.core.TotemCountChecker;
import net.fabricmc.api.ClientModInitializer;

import static io.github.piscescup.fabricmc.TotemOfUndyingBroadcastReferences.MOD_LOGGER;

public class TotemOfUndyingBroadcast implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		ConfigManager configManager = new ConfigManager();

		configManager.load();
		TotemCountChecker monitor = new TotemCountChecker(configManager);
		SettingsCommand.register(configManager, monitor);
		monitor.register();

		MOD_LOGGER.info("Client-side totem count broadcasts initialized; use /num-tou settings to configure them");
	}

}
