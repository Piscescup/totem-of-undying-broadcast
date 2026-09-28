package io.github.piscescup.fabricmc.util;

import io.github.piscescup.fabricmc.config.BroadcastLanguage;
import io.github.piscescup.fabricmc.datagen.lang.TotemTranslation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;

/**
 *
 * @author REN YuanTong
 * @since
 */
public final class Feedback {
    private Feedback() {}

    public static void sendLowTotemCount(
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

    public static void sendMissingOffhandTotem(
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

    public static void sendCheckWarnings(
        Minecraft client, LocalPlayer player, int count, BroadcastLanguage language
    ) {
        ClientPacketListener connection = client.getConnection();
        if (connection == null) {
            return;
        }

        String missingMessage = TotemTranslation.OFFHAND_TOTEM_MISSING
            .component(language, player.getName())
            .getString();
        connection.sendChat(missingMessage);

        String lowMessage = TotemTranslation.LOW_TOTEM_COUNT
            .component(language, player.getName(), count)
            .getString();
        connection.sendChat(lowMessage);
    }
}
