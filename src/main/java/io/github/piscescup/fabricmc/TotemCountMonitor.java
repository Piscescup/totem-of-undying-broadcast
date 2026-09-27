package io.github.piscescup.fabricmc;

import io.github.piscescup.fabricmc.config.BroadcastLanguage;
import io.github.piscescup.fabricmc.config.BroadcastConfig;
import io.github.piscescup.fabricmc.config.ConfigManager;
import io.github.piscescup.fabricmc.datagen.lang.TotemTranslation;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class TotemCountMonitor {
    private final ConfigManager configManager;
    private Integer previousTotemCount;

    public TotemCountMonitor(ConfigManager configManager) {
        this.configManager = configManager;
    }

    public void register() {
        ClientTickEvents.END_CLIENT_TICK.register(this::checkPlayer);
        ClientPlayConnectionEvents.DISCONNECT.register((listener, client) -> previousTotemCount = null);
    }

    private void checkPlayer(Minecraft client) {
        LocalPlayer player = client.player;
        if (player == null || client.getConnection() == null) {
            previousTotemCount = null;
            return;
        }

        BroadcastConfig settings = configManager.settings();
        int currentCount = countTotems(player);
        Integer previousCount = previousTotemCount;
        previousTotemCount = currentCount;

        if (!settings.enabled()) {
            return;
        }

        boolean firstLowCountCheck = previousCount == null && currentCount < settings.warningThreshold();
        boolean decreasedWhileLow = previousCount != null
                && currentCount < previousCount
                && currentCount < settings.warningThreshold();
        String playerName = player.getName().getString();

        if (firstLowCountCheck || decreasedWhileLow) {
            sendLowTotemCount(client, playerName, currentCount, settings.language());
        }
    }

    public ManualCheckResult runManualCheck() {
        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;
        if (player == null || client.getConnection() == null) {
            return ManualCheckResult.UNAVAILABLE;
        }

        BroadcastConfig settings = configManager.settings();
        String playerName = player.getName().getString();
        boolean warningSent = false;

        int currentCount = countTotems(player);
        if (currentCount < settings.warningThreshold()) {
            sendLowTotemCount(client, playerName, currentCount, settings.language());
            warningSent = true;
        }

        if (!hasTotemInOffhand(player)) {
            sendMissingOffhandTotem(client, playerName, settings.language());
            warningSent = true;
        }

        return warningSent ? ManualCheckResult.WARNING_SENT : ManualCheckResult.PASSED;
    }

    private static int countTotems(LocalPlayer player) {
        int count = 0;

        for (ItemStack stack : player.getInventory().getNonEquipmentItems()) {
            if (stack.getItem() == Items.TOTEM_OF_UNDYING) {
                count += stack.getCount();
            }
        }

        ItemStack offhandStack = player.getOffhandItem();
        if (offhandStack.getItem() == Items.TOTEM_OF_UNDYING) {
            count += offhandStack.getCount();
        }

        return count;
    }

    private static boolean hasTotemInOffhand(LocalPlayer player) {
        return player.getOffhandItem().getItem() == Items.TOTEM_OF_UNDYING;
    }

    private static void sendLowTotemCount(
            Minecraft client,
            String playerName,
            int currentCount,
            BroadcastLanguage language
    ) {
        ClientPacketListener connection = client.getConnection();
        if (connection == null) {
            return;
        }

        String message = TotemTranslation.LOW_TOTEM_COUNT
                .component(language, playerName, currentCount)
                .getString();
        connection.sendChat(message);
    }

    private static void sendMissingOffhandTotem(
            Minecraft client,
            String playerName,
            BroadcastLanguage language
    ) {
        ClientPacketListener connection = client.getConnection();
        if (connection == null) {
            return;
        }

        String message = TotemTranslation.OFFHAND_TOTEM_MISSING
                .component(language, playerName)
                .getString();
        connection.sendChat(message);
    }

    public enum ManualCheckResult {
        UNAVAILABLE,
        PASSED,
        WARNING_SENT
    }
}
