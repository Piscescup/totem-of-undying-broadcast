package io.github.piscescup.fabricmc.command;

import io.github.piscescup.fabricmc.group.Group;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

final class GroupInvitationChatTest {
    private static final String INVITATION_PREFIX =
        "[NumToU Group Invite] ";

    @Test
    void invitationMessageIncludesTheCreatorPrefixedNameAndId() {
        Group group = new Group("Alice-Dragon Squad", 42);

        assertEquals(
            INVITATION_PREFIX + "Alice-Dragon Squad/42",
            GroupInvitationChat.invitationMessage(group));
    }

    @Test
    void parseInvitationAcceptsAValidCreatorPrefixedName() {
        Group expected = new Group("Alice-Dragon Squad", 42);

        assertEquals(
            expected,
            GroupInvitationChat.parseInvitation(
                INVITATION_PREFIX + "Alice-Dragon Squad/42"));
    }

    @Test
    void parseInvitationRejectsTheWrongPrefix() {
        assertNull(GroupInvitationChat.parseInvitation(
            "[Other Group Invite] Alice-Dragon Squad/42"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "not-a-number",
        "-1",
        "0",
        "2147483648"
    })
    void parseInvitationRejectsInvalidNonPositiveAndOverflowIds(String id) {
        assertNull(GroupInvitationChat.parseInvitation(
            INVITATION_PREFIX + "Alice-Dragon Squad/" + id));
    }

    @Test
    void parseInvitationRejectsANameContainingSlash() {
        assertNull(GroupInvitationChat.parseInvitation(
            INVITATION_PREFIX + "Alice-Red/Blue/42"));
    }
}
