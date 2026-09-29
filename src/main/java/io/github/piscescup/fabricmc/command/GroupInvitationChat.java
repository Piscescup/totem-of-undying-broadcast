package io.github.piscescup.fabricmc.command;

import io.github.piscescup.fabricmc.config.ConfigManager;
import io.github.piscescup.fabricmc.group.Group;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.chat.GuiMessageTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;

import java.util.Objects;

final class GroupInvitationChat {
    private static final String INVITATION_PREFIX = "[NumToU Group Invite] ";

    private GroupInvitationChat() {}

    static void register(ConfigManager configManager) {
        ClientReceiveMessageEvents.ALLOW_CHAT.register((
            message,
            playerChatMessage,
            sender,
            boundChatType,
            timeStamp
        ) -> replaceInvitation(configManager, message, playerChatMessage));
    }

    static void broadcast(
        FabricClientCommandSource source,
        Group group
    ) {
        ClientPacketListener connection = source.getClient().getConnection();
        if (connection != null) {
            connection.sendChat(invitationMessage(group));
        }
    }

    static String invitationMessage(Group group) {
        return INVITATION_PREFIX + group.name() + "/" + group.id();
    }

    static Group parseInvitation(String message) {
        if (message == null || !message.startsWith(INVITATION_PREFIX)) {
            return null;
        }

        String invitation = message.substring(INVITATION_PREFIX.length()).strip();
        int separator = invitation.lastIndexOf('/');
        if (separator <= 0 || separator == invitation.length() - 1) {
            return null;
        }

        String name = invitation.substring(0, separator).strip();
        String rawId = invitation.substring(separator + 1);
        Integer id = parsePositiveId(rawId);
        if (id == null) {
            return null;
        }

        try {
            return new Group(name, id);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private static boolean replaceInvitation(
        ConfigManager configManager,
        Component message,
        PlayerChatMessage playerChatMessage
    ) {
        if (playerChatMessage == null) {
            return true;
        }
        if (!playerChatMessage.filterMask().isEmpty()) {
            return true;
        }

        Group group = parseInvitation(playerChatMessage.signedContent());
        if (group == null) {
            return true;
        }

        Minecraft client = Minecraft.getInstance();
        if (Objects.equals(
            playerChatMessage.sender(),
            client.getGameProfile().id())) {
            return false;
        }

        Component invitation = message.copy()
            .append(" ")
            .append(GroupCommand.invitationLink(
                group,
                GroupCommand.currentLanguage(configManager)));
        GuiMessageTag tag = GuiMessageTag.chatModified(
            playerChatMessage.signedContent());

        client.execute(() ->
            client.gui.hud.getChat().addPlayerMessage(
                invitation,
                playerChatMessage.signature(),
                tag));
        return false;
    }

    private static Integer parsePositiveId(String rawId) {
        if (rawId.isEmpty()) {
            return null;
        }
        for (int index = 0; index < rawId.length(); index++) {
            char character = rawId.charAt(index);
            if (character < '0' || character > '9') {
                return null;
            }
        }
        try {
            int id = Integer.parseInt(rawId);
            return id > 0 ? id : null;
        } catch (NumberFormatException exception) {
            return null;
        }
    }
}
