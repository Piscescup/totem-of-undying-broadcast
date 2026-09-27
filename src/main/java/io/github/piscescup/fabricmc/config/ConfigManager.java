package io.github.piscescup.fabricmc.config;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import net.fabricmc.loader.api.FabricLoader;

import static io.github.piscescup.fabricmc.TotemOfUndyingBroadcastReferences.MOD_ID;
import static io.github.piscescup.fabricmc.TotemOfUndyingBroadcastReferences.MOD_LOGGER;

public final class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String CONFIG_FILE_NAME = MOD_ID + ".json";

    private final Path configFile;
    private volatile BroadcastConfig current = BroadcastConfig.DEFAULT;
    private boolean requiresSave;

    public ConfigManager() {
        this(FabricLoader.getInstance().getConfigDir().resolve(CONFIG_FILE_NAME));
    }

    ConfigManager(Path configFile) {
        this.configFile = configFile.toAbsolutePath().normalize();
    }

    public BroadcastConfig settings() {
        return current;
    }

    public synchronized void load() {
        if (Files.notExists(configFile)) {
            current = BroadcastConfig.DEFAULT;

            try {
                save(current);
                requiresSave = false;
                MOD_LOGGER.info("Created default settings at {}", configFile);
            } catch (IOException exception) {
                requiresSave = true;
                MOD_LOGGER.error("Could not create the default settings file at {}", configFile, exception);
            }

            return;
        }

        try (Reader reader = Files.newBufferedReader(configFile, StandardCharsets.UTF_8)) {
            JsonElement rootElement = JsonParser.parseReader(reader);
            if (!rootElement.isJsonObject()) {
                throw new JsonParseException("The settings root must be a JSON object");
            }

            current = parse(rootElement.getAsJsonObject());
            requiresSave = false;
            MOD_LOGGER.info("Loaded settings from {}", configFile);
        } catch (IOException | JsonParseException | IllegalArgumentException exception) {
            current = BroadcastConfig.DEFAULT;
            requiresSave = true;
            MOD_LOGGER.error("Could not load settings from {}; using defaults", configFile, exception);
        }
    }

    public synchronized BroadcastConfig setLanguage(BroadcastLanguage language) throws IOException {
        BroadcastConfig old = current;
        return update(new BroadcastConfig(language, old.warningThreshold(), old.enabled()));
    }

    public synchronized BroadcastConfig setWarningThreshold(int warningThreshold) throws IOException {
        BroadcastConfig old = current;
        return update(new BroadcastConfig(old.language(), warningThreshold, old.enabled()));
    }

    public synchronized BroadcastConfig setEnabled(boolean enabled) throws IOException {
        BroadcastConfig old = current;
        return update(new BroadcastConfig(old.language(), old.warningThreshold(), enabled));
    }

    private BroadcastConfig update(BroadcastConfig next) throws IOException {
        if (requiresSave || !next.equals(current)) {
            save(next);
            current = next;
            requiresSave = false;
        }

        return current;
    }

    private void save(BroadcastConfig settings) throws IOException {
        Path parent = configFile.getParent();
        Files.createDirectories(parent);

        JsonObject root = new JsonObject();
        root.addProperty("lang", settings.language().code());
        root.addProperty("warning-threshold", settings.warningThreshold());
        root.addProperty("enable", settings.enabled());

        Path temporaryFile = Files.createTempFile(parent, CONFIG_FILE_NAME, ".tmp");
        try {
            try (Writer writer = Files.newBufferedWriter(temporaryFile, StandardCharsets.UTF_8)) {
                GSON.toJson(root, writer);
            }

            try {
                Files.move(
                        temporaryFile,
                        configFile,
                        StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(temporaryFile, configFile, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporaryFile);
        }
    }

    private static BroadcastConfig parse(JsonObject root) {
        String languageCode = requiredString(root, "lang");
        int warningThreshold = requiredWarningThreshold(root);
        boolean enabled = requiredBoolean(root, "enable");

        return new BroadcastConfig(
                BroadcastLanguage.fromCode(languageCode),
                warningThreshold,
                enabled);
    }

    private static String requiredString(JsonObject root, String propertyName) {
        JsonElement element = root.get(propertyName);
        if (element == null || !element.isJsonPrimitive() || !element.getAsJsonPrimitive().isString()) {
            throw new JsonParseException("Property '" + propertyName + "' must be a string");
        }

        return element.getAsString();
    }

    private static boolean requiredBoolean(JsonObject root, String propertyName) {
        JsonElement element = root.get(propertyName);
        if (element == null || !element.isJsonPrimitive() || !element.getAsJsonPrimitive().isBoolean()) {
            throw new JsonParseException("Property '" + propertyName + "' must be a boolean");
        }

        return element.getAsBoolean();
    }

    private static int requiredWarningThreshold(JsonObject root) {
        JsonElement element = root.has("warning-threshold")
                ? root.get("warning-threshold")
                : root.get("warning-treshold");

        if (element == null || !element.isJsonPrimitive() || !element.getAsJsonPrimitive().isNumber()) {
            throw new JsonParseException("Property 'warning-threshold' must be an integer");
        }

        try {
            return element.getAsBigDecimal().intValueExact();
        } catch (ArithmeticException | NumberFormatException exception) {
            throw new JsonParseException("Property 'warning-threshold' must be an integer", exception);
        }
    }

}
