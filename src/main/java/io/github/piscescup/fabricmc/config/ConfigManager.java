package io.github.piscescup.fabricmc.config;

import io.github.piscescup.fabricmc.group.Group;
import io.github.piscescup.fabricmc.io.ConfigIO;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.IntSupplier;

import static io.github.piscescup.fabricmc.TotemOfUndyingBroadcastReferences.MOD_LOGGER;

public final class ConfigManager {
    private final BroadcastCommonConfig commonConfig = new BroadcastCommonConfig();
    private final BroadcastCheckConfig checkConfig = new BroadcastCheckConfig();
    private final GroupConfig groupConfig = new GroupConfig();

    private final List<ConfigFile> cfgs = List.of(
        commonConfig,
        checkConfig,
        groupConfig
    );
    private final List<Configurable<String>> settingCfgs = List.of(
        commonConfig,
        checkConfig
    );

    public ConfigManager() {}

    public synchronized void load() {
        for (ConfigFile cfg : cfgs) {
            Path file = cfg.toCfgFile();

            if (Files.notExists(file)) {
                try {
                    ConfigIO.write(file, cfg.toJson());
                    MOD_LOGGER.info("Created default config at {}", file);
                } catch (IOException exception) {
                    MOD_LOGGER.error("Could not create default config at {}", file, exception);
                }
                continue;
            }

            try {
                ConfigIO.read(file, cfg);
                MOD_LOGGER.info("Loaded config from {}", file);
            } catch (IOException | RuntimeException exception) {
                MOD_LOGGER.error("Could not load config from {}; using defaults", file, exception);
            }
        }
    }

    public synchronized void save() throws IOException {
        for (ConfigFile cfg : cfgs) {
            ConfigIO.write(cfg.toCfgFile(), cfg.toJson());
        }
    }

    public <T> T getProperty(String key, Function<String, T> converter) {
        Configurable<String> cfg = findSetting(key);
        if (cfg == null) {
            throw new IllegalArgumentException("Unknown config key: " + key);
        }
        return converter.apply(cfg.getProperty(key));
    }

    public <T> T getProperty(
        String key,
        T defaultValue,
        Function<String, T> converter
    ) {
        Configurable<String> cfg = findSetting(key);
        if (cfg == null) {
            return defaultValue;
        }
        String raw = cfg.getProperty(key);
        if (raw == null) {
            return defaultValue;
        }
        return converter.apply(raw);
    }

    public synchronized void setProperty(String key, Object value) throws IOException {
        Configurable<String> cfg = findSetting(key);
        if (cfg == null) {
            throw new IllegalArgumentException("Unknown config key: " + key);
        }
        cfg.updateProperty(key, String.valueOf(value));
        ConfigIO.write(cfg.toCfgFile(), cfg.toJson());
    }

    public synchronized void setProperties(Map<String, ?> properties) throws IOException {
        for (String key : properties.keySet()) {
            if (findSetting(key) == null) {
                throw new IllegalArgumentException("Unknown config key: " + key);
            }
        }

        properties.forEach((key, value) ->
            findSetting(key).updateProperty(key, String.valueOf(value)));
        save();
    }

    public synchronized Group createGroup(String name) throws IOException {
        Group group = groupConfig.create(name);
        try {
            saveGroups();
            return group;
        } catch (IOException exception) {
            groupConfig.remove(group);
            throw exception;
        }
    }

    public synchronized Group.JoinResult joinGroup(String name, int id) throws IOException {
        Group.JoinResult result = groupConfig.join(name, id);
        if (!result.joined()) {
            return result;
        }

        try {
            saveGroups();
            return result;
        } catch (IOException exception) {
            groupConfig.remove(result.group());
            throw exception;
        }
    }

    public synchronized List<Group> groups() {
        return groupConfig.groups();
    }

    private void saveGroups() throws IOException {
        ConfigIO.write(groupConfig.toCfgFile(), groupConfig.toJson());
    }

    private Configurable<String> findSetting(String key) {
        for (Configurable<String> cfg : settingCfgs) {
            if (cfg.keySet().contains(key)) {
                return cfg;
            }
        }
        return null;
    }
}
