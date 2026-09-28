package io.github.piscescup.fabricmc.core;

import io.github.piscescup.fabricmc.config.BroadcastCheckConfig;
import io.github.piscescup.fabricmc.config.BroadcastLanguage;
import io.github.piscescup.fabricmc.config.BroadcastCommonConfig;
import io.github.piscescup.fabricmc.config.ConfigManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

import static io.github.piscescup.fabricmc.util.Feedback.*;


public final class TotemCountChecker {
    private ConfigManager configManager;
    private boolean reactiveEnabled;
    private boolean periodicEnabled;
    private int checkTick;
    private int threshold;

    private TOUMonitor monitor;
    private BroadcastLanguage language;

    public TotemCountChecker(ConfigManager configManager) {
        this.configManager = configManager;
    }

    private void updateCfg(ConfigManager configManager) {
        this.reactiveEnabled = configManager.getProperty(
            BroadcastCommonConfig.KEY_ENABLED,
            false,
            Boolean::parseBoolean);

        this.threshold = configManager.getProperty(
            BroadcastCommonConfig.KEY_WARNING_THRESHOLD,
            BroadcastCommonConfig.DEFAULT_WARNING_THRESHOLD,
            Integer::parseInt);

        this.periodicEnabled = configManager.getProperty(
            BroadcastCheckConfig.KEY_CHECK_ENABLED,
            BroadcastCheckConfig.DEFAULT_CHECK_ENABLED,
            Boolean::parseBoolean);

        this.checkTick = configManager.getProperty(
            BroadcastCheckConfig.KEY_CHECK_TICK,
            BroadcastCheckConfig.DEFAULT_CHECK_TICK,
            Integer::parseInt);

        this.language = configManager.getProperty(
            BroadcastCommonConfig.KEY_LANGUAGE,
            BroadcastLanguage.EN_US,
            BroadcastLanguage::fromCode);
    }

    public void register() {
        ClientTickEvents.END_CLIENT_TICK.register(this::checkPlayer);
        ClientTickEvents.END_CLIENT_TICK.register(this::manualCheck);
        ClientPlayConnectionEvents.DISCONNECT.register((listener, client) -> reset());
    }

    private void reset() {
        monitor = null;
    }

    public TOUMonitor getTotemMonitor() {
        return monitor;
    }

    private void checkPlayer(Minecraft client) {
        updateCfg(configManager);
        LocalPlayer player = client.player;
        if (player == null || client.getConnection() == null) {
            reset();
            return;
        }
        if (client.isPaused()) {
            return;
        }

        if (monitor == null || monitor.player() != player) {
            monitor = TOUMonitor.create(player);
        }


        if (!reactiveEnabled && !periodicEnabled) {
            monitor.reset();
            return;
        }

        if (monitor.checkPeriod(periodicEnabled, checkTick)) {
            sendCheckWarnings(client, player, monitor.countTotems(), language);
            return;
        }

        if (!reactiveEnabled) {
            return;
        }

        if (monitor.checkTotemNumber(threshold)) {
            sendLowTotemCount(client, player.getName().getString(), monitor.countTotems(), language);
        }
    }

    public TOUMonitor.ManualCheckResult manualCheck(Minecraft client) {
        updateCfg(configManager);
        LocalPlayer player = client.player;
        if (player == null || client.getConnection() == null) {
            reset();
            return TOUMonitor.ManualCheckResult.PASS;
        }
        if (client.isPaused()) {
            return TOUMonitor.ManualCheckResult.PASS;
        }

        if (monitor == null || monitor.player() != player) {
            monitor = TOUMonitor.create(player);
        }

        return monitor.runManualCheck(threshold);
    }

}
