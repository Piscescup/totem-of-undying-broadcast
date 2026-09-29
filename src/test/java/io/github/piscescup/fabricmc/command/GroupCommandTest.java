package io.github.piscescup.fabricmc.command;

import com.mojang.brigadier.tree.CommandNode;
import io.github.piscescup.fabricmc.config.BroadcastLanguage;
import io.github.piscescup.fabricmc.config.ConfigManager;
import io.github.piscescup.fabricmc.exception.InvalidGroupException;
import io.github.piscescup.fabricmc.group.Group;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class GroupCommandTest {
    private static final Group GROUP = new Group("Alice-Dragon Squad", 42);
    private static final String INVITATION_COMMAND =
        "/num-tou group join Alice-Dragon Squad/42";

    @Test
    void commandRegistersCreateAndJoinSubcommands() {
        CommandNode<?> root = GroupCommand.command(new ConfigManager()).build();

        assertNotNull(root.getChild("create"));
        CommandNode<?> join = root.getChild("join");
        assertNotNull(join);
        assertNotNull(join.getChild("invitation"));
    }

    @Test
    void invitationCommandContainsTheExactJoinCommand() {
        assertEquals(INVITATION_COMMAND, GroupCommand.invitationCommand(GROUP));
    }

    @Test
    void invitationLinkSuggestsTheJoinCommandAndExplainsItsAction() {
        MutableComponent link = GroupCommand.invitationLink(
            GROUP,
            BroadcastLanguage.EN_US);
        Style style = link.getStyle();

        assertEquals("[Join Alice-Dragon Squad]", link.getString());
        assertEquals(TextColor.GREEN, style.getColor());
        assertTrue(style.isUnderlined());

        ClickEvent.SuggestCommand clickEvent = assertInstanceOf(
            ClickEvent.SuggestCommand.class,
            style.getClickEvent());
        assertEquals(INVITATION_COMMAND, clickEvent.command());

        HoverEvent.ShowText hoverEvent = assertInstanceOf(
            HoverEvent.ShowText.class,
            style.getHoverEvent());
        assertEquals(
            "Click to put " + INVITATION_COMMAND + " in the chat box",
            hoverEvent.value().getString());
    }

    @Test
    void createdGroupNamePrefixesTheRequestedNameWithTheCreatorName() {
        assertEquals(
            "Alice-Dragon Squad",
            GroupCommand.createdGroupName("Alice", "Dragon Squad"));
    }

    @Test
    void createdGroupNameValidatesTheCombinedNameLength() {
        assertThrows(
            InvalidGroupException.class,
            () -> GroupCommand.createdGroupName("Alice", "x".repeat(60)));
    }
}
