package io.github.piscescup.fabricmc.config;

import com.google.gson.JsonElement;
import io.github.piscescup.fabricmc.io.ConfigIO;
import net.minecraft.client.player.LocalPlayer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NonNull;

import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;

/**
 *
 * @author REN YuanTong
 * @since 1.0.0
 */
public final class GroupConfig implements Configurable {
    public static final String SENDER_KEY = "sender";
    // public static final String RECEIVERS_KEY = "receivers";

    private final Map<String, Set<String>> msgCollector;

    private GroupConfig(LocalPlayer sender) {
        this.msgCollector = new HashMap<>();
    }

    @Override
    public JsonElement toJson() {

        return null;
    }

    @Override
    public void parseFromJson(JsonElement jsonElement) {

    }

    @Override
    public <T> T getProperty(String key, Function<String, T> converter) {
        return converter.apply(msgCollector.get(key));
    }

    @Override
    public <T> void updateProperty(String key, T value) {
        msgCollector.computeIfAbsent(
            key, _ -> new TreeSet<>()
        )
            .add(String.valueOf(value));
    }

    @Override
    @NonNull
    @Unmodifiable
    @Contract(value = " -> new", pure = true)
    public Set<String> keySet() {
        return Set.of(
            SENDER_KEY
            // RECEIVERS_KEY
        );
    }

    @Override
    @NonNull
    public Path toCfgFile() {
        return ConfigIO.groupConfigFile();
    }
}
