package io.github.piscescup.fabricmc.integration;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import io.github.piscescup.fabricmc.TotemOfUndyingBroadcast;
import io.github.piscescup.fabricmc.gui.SettingsScreen;

public final class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> new SettingsScreen(
            parent,
            TotemOfUndyingBroadcast.configManager());
    }
}
