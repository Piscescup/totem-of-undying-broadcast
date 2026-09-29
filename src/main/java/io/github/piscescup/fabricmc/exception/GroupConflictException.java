package io.github.piscescup.fabricmc.exception;

import io.github.piscescup.fabricmc.group.Group;

/**
 *
 * @author REN YuanTong
 * @since 1.0.0
 */
public final class GroupConflictException extends IllegalArgumentException {
    private final Group existing;

    public GroupConflictException(Group existing, String conflictKey) {
        super("Group " + conflictKey + " is already used in " + existing);
        this.existing = existing;
    }

    public Group existing() {
        return existing;
    }

    public static GroupConflictException name(Group existing) {
        return new GroupConflictException(existing, "name");
    }

    public static GroupConflictException id(Group existing) {
        return new GroupConflictException(existing, "id");
    }
}