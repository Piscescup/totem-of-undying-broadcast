package io.github.piscescup.fabricmc.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.piscescup.fabricmc.config.BroadcastCommonConfig;
import io.github.piscescup.fabricmc.config.BroadcastLanguage;
import io.github.piscescup.fabricmc.config.ConfigManager;
import io.github.piscescup.fabricmc.datagen.lang.TotemTranslation;
import io.github.piscescup.fabricmc.exception.GroupConflictException;
import io.github.piscescup.fabricmc.exception.InvalidGroupException;
import io.github.piscescup.fabricmc.group.Group;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;

import java.io.IOException;

import static io.github.piscescup.fabricmc.TotemOfUndyingBroadcastReferences.MOD_LOGGER;

final class GroupCommand {
    private static final String NAME_ARGUMENT = "name";
    private static final String INVITATION_ARGUMENT = "invitation";
    private static final String COMMAND_PREFIX = "/num-tou group join ";

    private GroupCommand() {}

    static LiteralArgumentBuilder<FabricClientCommandSource> command(ConfigManager configManager) {
        return ClientCommands.literal("group")
            .then(ClientCommands.literal("create")
                .then(ClientCommands.argument(
                        NAME_ARGUMENT,
                        StringArgumentType.greedyString())
                    .executes(context -> create(
                        context.getSource(),
                        configManager,
                        StringArgumentType.getString(context, NAME_ARGUMENT)))))
            .then(ClientCommands.literal("join")
                .then(ClientCommands.argument(
                        INVITATION_ARGUMENT,
                        StringArgumentType.greedyString())
                    .executes(context -> join(
                        context.getSource(),
                        configManager,
                        StringArgumentType.getString(context, INVITATION_ARGUMENT)))));
    }

    static String invitationCommand(Group group) {
        return COMMAND_PREFIX + group.name() + "/" + group.id();
    }

    static MutableComponent invitationLink(
        Group group,
        BroadcastLanguage language
    ) {
        String command = invitationCommand(group);
        return TotemTranslation.GROUP_JOIN_LINK.component(language, group.name())
            .withStyle(style -> style
                .withColor(ChatFormatting.GREEN)
                .withUnderlined(true)
                .withClickEvent(new ClickEvent.SuggestCommand(command))
                .withHoverEvent(new HoverEvent.ShowText(
                    TotemTranslation.GROUP_JOIN_HINT.component(language, command))));
    }

    static String createdGroupName(String creatorName, String requestedName) {
        String suffix = Group.validateName(requestedName);
        return Group.validateName(creatorName + "-" + suffix);
    }

    private static int create(
        FabricClientCommandSource source,
        ConfigManager configManager,
        String name
    ) {
        BroadcastLanguage language = currentLanguage(configManager);
        try {
            String creatorName = source.getPlayer().getGameProfile().name();
            Group group = configManager.createGroup(
                createdGroupName(creatorName, name));
            Component message = TotemTranslation.GROUP_CREATED
                .component(language, group.name(), group.id())
                .append(" ")
                .append(invitationLink(group, language));
            source.sendFeedback(message);
            GroupInvitationChat.broadcast(source, group);
            return 1;
        } catch (InvalidGroupException | GroupConflictException exception) {
            source.sendError(Component.literal(exception.getMessage()));
        } catch (IOException exception) {
            saveFailed(source, language, exception);
        }
        return 0;
    }

    private static int join(
        FabricClientCommandSource source,
        ConfigManager configManager,
        String rawInvitation
    ) {
        BroadcastLanguage language = currentLanguage(configManager);
        String invitation = rawInvitation.strip();
        int separator = invitation.lastIndexOf('/');
        if (separator <= 0 || separator == invitation.length() - 1) {
            source.sendError(TotemTranslation.GROUP_INVITATION_INVALID.component(language));
            return 0;
        }

        String name = invitation.substring(0, separator).strip();
        String rawId = invitation.substring(separator + 1);
        Integer id = parsePositiveId(rawId);
        if (id == null) {
            source.sendError(TotemTranslation.GROUP_INVITATION_INVALID.component(language));
            return 0;
        }

        try {
            Group.JoinResult result = configManager.joinGroup(name, id);
            TotemTranslation translation = result.joined()
                ? TotemTranslation.GROUP_JOINED
                : TotemTranslation.GROUP_ALREADY_JOINED;
            source.sendFeedback(translation.component(
                language,
                result.group().name(),
                result.group().id()));
            return 1;
        } catch (InvalidGroupException | GroupConflictException exception) {
            source.sendError(Component.literal(exception.getMessage()));
        } catch (IOException exception) {
            saveFailed(source, language, exception);
        }
        return 0;
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

    static BroadcastLanguage currentLanguage(ConfigManager configManager) {
        return configManager.getProperty(
            BroadcastCommonConfig.KEY_LANGUAGE,
            BroadcastLanguage.EN_US,
            BroadcastLanguage::fromCode);
    }

    private static void saveFailed(
        FabricClientCommandSource source,
        BroadcastLanguage language,
        IOException exception
    ) {
        MOD_LOGGER.error("Could not save a group changed by a command", exception);
        source.sendError(TotemTranslation.GROUP_SAVE_FAILED.component(language));
    }
}
