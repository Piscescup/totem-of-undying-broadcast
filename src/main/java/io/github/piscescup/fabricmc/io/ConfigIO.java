package io.github.piscescup.fabricmc.io;

import com.google.gson.*;
import io.github.piscescup.fabricmc.config.Configurable;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.function.Function;

import static io.github.piscescup.fabricmc.TotemOfUndyingBroadcastReferences.MOD_ID;

public final class ConfigIO {

    private static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID + "/config-io");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static Path configDir() {
        return FabricLoader.getInstance().getConfigDir().resolve(MOD_ID);
    }

    public static Path commonConfigFile() {
        return configDir().resolve("common.json");
    }

    public static Path checkConfigFile() {
        return configDir().resolve("check.json");
    }

    public static Path groupConfigFile() {
        return configDir().resolve("group.json");
    }

    private ConfigIO() {}

    public static <T extends Configurable> T read(
        Path file, T fallback, Function<JsonElement, T> parser) {

        if (Files.notExists(file)) {
            try {
                write(file, fallback.toJson());
                LOGGER.info("Created default config at {}", file);
            } catch (IOException e) {
                LOGGER.error("Could not create default config at {}", file, e);
            }
            return fallback;
        }

        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            JsonElement root = JsonParser.parseReader(reader);
            T loaded = parser.apply(root);
            LOGGER.info("Loaded config from {}", file);
            return loaded;
        } catch (IOException | JsonParseException | IllegalArgumentException e) {
            LOGGER.error("Could not load config from {}; using defaults", file, e);
            return fallback;
        }
    }

    public static void read(Path file, Configurable target) throws IOException {
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            JsonElement root = JsonParser.parseReader(reader);
            target.parseFromJson(root);
        } catch (JsonParseException | IllegalArgumentException e) {
            throw new IOException("Invalid JSON in " + file, e);
        }
    }


    public static void write(Path file, JsonElement data) throws IOException {
        Path parent = file.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        Path tmp = Files.createTempFile(parent, file.getFileName().toString(), ".tmp");
        try {
            try (Writer writer = Files.newBufferedWriter(tmp, StandardCharsets.UTF_8)) {
                GSON.toJson(data, writer);
            }
            try {
                Files.move(tmp, file,
                    StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(tmp);
        }
    }
}