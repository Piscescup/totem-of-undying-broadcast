package io.github.piscescup.fabricmc.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sun.jna.platform.unix.X11;
import io.github.piscescup.fabricmc.exception.GroupConflictException;
import io.github.piscescup.fabricmc.exception.InvalidGroupException;
import io.github.piscescup.fabricmc.group.Group;
import io.github.piscescup.fabricmc.io.ConfigIO;
import net.minecraft.client.searchtree.IdSearchTree;
import net.minecraft.world.entity.monster.Giant;

import java.nio.file.Path;
import java.text.Normalizer;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.IntSupplier;

import static io.github.piscescup.fabricmc.group.Group.validateName;

/**
 * Persistent groups joined by this client.
 *
 * <p>Only stable invitation data is stored here. Runtime player objects cannot
 * be restored when the game starts and therefore do not belong in this file.</p>
 *
 * @author REN YuanTong
 * @since 1.0.0
 */
public final class GroupConfig implements ConfigFile {
    public static final String GROUPS_KEY = "groups";

    private static final String NAME_KEY = "name";
    private static final String ID_KEY = "id";

    private static final int RANDOM_ID_ATTEMPTS = 64;

    private final Map<String, Group> groupsByName = new LinkedHashMap<>();
    private final Map<Integer, Group> groupsById = new HashMap<>();
    private final IntSupplier idSupplier;

    public GroupConfig() {
        this(() -> ThreadLocalRandom.current()
            .nextInt(1, Integer.MAX_VALUE));
    }

    GroupConfig(IntSupplier idSupplier) {
        this.idSupplier = Objects.requireNonNull(idSupplier);
    }

    public synchronized Group create(String name) {
        String displayName = validateName(name);
        String normalizedName = normalizeName(displayName);
        Group existing = groupsByName.get(normalizedName);
        if (existing != null) {
            throw GroupConflictException.name(existing);
        }

        Group group = new Group(displayName, nextUniqueId());
        add(group);
        return group;
    }

    private void add(Group group) {
        groupsByName.put(normalizeName(group.name()), group);
        groupsById.put(group.id(), group);
    }

    public synchronized Group.JoinResult join(String name, int id) {
        String displayName = validateName(name);
        if (id <= 0) {
            throw InvalidGroupException.id(id);
        }

        String normalizedName = normalizeName(displayName);
        Group sameName = groupsByName.get(normalizedName);
        if (sameName != null) {
            if (sameName.id() == id) {
                return new Group.JoinResult(sameName, false);
            }
            throw GroupConflictException.name(sameName);
        }

        Group sameId = groupsById.get(id);
        if (sameId != null) {
            throw GroupConflictException.id(sameId);
        }

        Group group = new Group(displayName, id);
        add(group);
        return new Group.JoinResult(group, true);
    }

    private int nextUniqueId() {
        for (int attempt = 0; attempt < RANDOM_ID_ATTEMPTS; attempt++) {
            int candidate = idSupplier.getAsInt();
            if (candidate > 0 && !groupsById.containsKey(candidate)) {
                return candidate;
            }
        }

        for (int candidate = 1; candidate < Integer.MAX_VALUE; candidate++) {
            if (!groupsById.containsKey(candidate)) {
                return candidate;
            }
        }
        throw new IllegalStateException("No positive group ids are available");
    }

    public synchronized List<Group> groups() {
        return List.copyOf(groupsByName.values());
    }

    synchronized void remove(Group group) {
        String normalizedName = normalizeName(group.name());
        groupsByName.remove(normalizedName, group);
        groupsById.remove(group.id(), group);
    }

    @Override
    public synchronized JsonElement toJson() {
        JsonArray groups = new JsonArray();
        for (Group group : groupsByName.values()) {
            JsonObject entry = new JsonObject();
            entry.addProperty(NAME_KEY, group.name());
            entry.addProperty(ID_KEY, group.id());
            groups.add(entry);
        }

        JsonObject root = new JsonObject();
        root.add(GROUPS_KEY, groups);
        return root;
    }

    @Override
    public synchronized void parseFromJson(JsonElement jsonElement) {
        if (jsonElement == null || !jsonElement.isJsonObject()) {
            throw new IllegalArgumentException("Expected a JSON object for GroupConfig");
        }

        JsonObject root = jsonElement.getAsJsonObject();
        JsonElement groupsElement = root.get(GROUPS_KEY);
        if (groupsElement == null || !groupsElement.isJsonArray()) {
            throw new IllegalArgumentException("Expected a groups array in GroupConfig");
        }

        List<Group> parsedGroups = new ArrayList<>();
        Map<String, Group> parsedByName = new LinkedHashMap<>();
        Map<Integer, Group> parsedById = new HashMap<>();

        for (JsonElement element : groupsElement.getAsJsonArray()) {
            Group group = parseGroup(element);
            String normalizedName = normalizeName(group.name());
            Group sameName = parsedByName.putIfAbsent(normalizedName, group);
            if (sameName != null) {
                throw new IllegalArgumentException(
                    "Duplicate group name: " + group.name());
            }
            Group sameId = parsedById.putIfAbsent(group.id(), group);
            if (sameId != null) {
                throw new IllegalArgumentException(
                    "Duplicate group id: " + group.id());
            }
            parsedGroups.add(group);
        }

        groupsByName.clear();
        groupsById.clear();
        for (Group group : parsedGroups) {
            add(group);
        }
    }

    @Override
    public Path toCfgFile() {
        return ConfigIO.groupConfigFile();
    }


    private static String normalizeName(String name) {
        return Normalizer.normalize(name, Normalizer.Form.NFKC)
            .strip()
            .toLowerCase(Locale.ROOT);
    }

    private static Group parseGroup(JsonElement element) {
        if (!element.isJsonObject()) {
            throw new IllegalArgumentException("Expected a JSON object for a group");
        }

        JsonObject object = element.getAsJsonObject();
        if (!object.has(NAME_KEY) || !object.has(ID_KEY)) {
            throw new IllegalArgumentException("A group must contain name and id");
        }

        String name;
        int id;
        try {
            name = object.get(NAME_KEY).getAsString();
            id = Integer.parseInt(object.get(ID_KEY).getAsString());
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException("Invalid group name or id", exception);
        }
        return new Group(name, id);
    }

}
