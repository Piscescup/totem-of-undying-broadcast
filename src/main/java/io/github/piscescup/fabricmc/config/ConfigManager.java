package io.github.piscescup.fabricmc.config;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import io.github.piscescup.fabricmc.io.ConfigIO;
import net.fabricmc.loader.api.FabricLoader;

import static io.github.piscescup.fabricmc.TotemOfUndyingBroadcastReferences.*;

public final class ConfigManager {

    private final List<Configurable> cfgs = List.of(
        new BroadcastCommonConfig(),
        new BroadcastCheckConfig()
    );

    public ConfigManager() {}

    // ---------- load ----------

    public synchronized void load() {
        for (Configurable cfg : cfgs) {
            Path file = cfg.toCfgFile();

            if (Files.notExists(file)) {
                try {
                    ConfigIO.write(file, cfg.toJson());
                    MOD_LOGGER.info("Created default config at {}", file);
                } catch (IOException e) {
                    MOD_LOGGER.error("Could not create default config at {}", file, e);
                }
                continue;
            }

            try {
                ConfigIO.read(file, cfg);
                MOD_LOGGER.info("Loaded config from {}", file);
            } catch (IOException | RuntimeException e) {
                MOD_LOGGER.error("Could not load config from {}; using defaults", file, e);
            }
        }
    }


    public synchronized void save() throws IOException {
        for (Configurable cfg : cfgs) {
            ConfigIO.write(cfg.toCfgFile(), cfg.toJson());
        }
    }

    public <T> T getProperty(String key, Function<String, T> converter) {
        Configurable cfg = find(key);
        if (cfg == null) {
            throw new IllegalArgumentException("Unknown config key: " + key);
        }
        return cfg.getProperty(key, converter);
    }

    public <T> T getProperty(String key, T defaultValue, Function<String, T> converter) {
        Configurable cfg = find(key);
        if (cfg == null) {
            return defaultValue;
        }
        String raw = rawOrNull(cfg, key);
        if (raw == null) {
            return defaultValue;
        }
        return converter.apply(raw);
    }

    public synchronized void setProperty(String key, Object value) throws IOException {
        Configurable cfg = find(key);
        if (cfg == null) {
            throw new IllegalArgumentException("Unknown config key: " + key);
        }
        cfg.updateProperty(key, value);
        ConfigIO.write(cfg.toCfgFile(), cfg.toJson());
    }


    private Configurable find(String key) {
        for (Configurable cfg : cfgs) {
            if (cfg.keySet().contains(key)) {
                return cfg;
            }
        }
        return null;
    }


    private static String rawOrNull(Configurable cfg, String key) {
        try {
            return cfg.getProperty(key, Function.identity());
        } catch (RuntimeException e) {
            return null;
        }
    }
}
