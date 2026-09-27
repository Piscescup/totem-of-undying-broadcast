package io.github.piscescup.fabricmc.config;

import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * A type-safe key describing how to read and replace one config value.
 *
 * <p>The registry is keyed by the names used by commands and the JSON file,
 * while the actual config remains an immutable {@link BroadcastConfig}.</p>
 */
public final class ConfigKey<T> {
    public static final ConfigKey<BroadcastLanguage> LANGUAGE = new ConfigKey<>(
        "lang",
        BroadcastConfig::language,
        (settings, language) -> new BroadcastConfig(
            language,
            settings.warningThreshold(),
            settings.enabled()
        ),
        BroadcastLanguage::code
    );

    public static final ConfigKey<Integer> WARNING_THRESHOLD = new ConfigKey<>(
        "warning-threshold",
        BroadcastConfig::warningThreshold,
        (settings, warningThreshold) -> new BroadcastConfig(
            settings.language(),
            warningThreshold,
            settings.enabled()
        ),
        String::valueOf
    );

    public static final ConfigKey<Boolean> ENABLED = new ConfigKey<>(
        "enable",
        BroadcastConfig::enabled,
        (settings, enabled) -> new BroadcastConfig(
            settings.language(),
            settings.warningThreshold(),
            enabled
        ),
        String::valueOf
    );

    private static final Map<String, ConfigKey<?>> BY_NAME = Map.of(
        LANGUAGE.name(), LANGUAGE,
        WARNING_THRESHOLD.name(), WARNING_THRESHOLD,
        ENABLED.name(), ENABLED
    );

    private final String name;
    private final Function<BroadcastConfig, T> getter;
    private final BiFunction<BroadcastConfig, T, BroadcastConfig> updater;
    private final Function<T, String> formatter;

    private ConfigKey(
        String name,
        Function<BroadcastConfig, T> getter,
        BiFunction<BroadcastConfig, T, BroadcastConfig> updater,
        Function<T, String> formatter
    ) {
        this.name = Objects.requireNonNull(name, "name");
        this.getter = Objects.requireNonNull(getter, "getter");
        this.updater = Objects.requireNonNull(updater, "updater");
        this.formatter = Objects.requireNonNull(formatter, "formatter");
    }

    public String name() {
        return name;
    }

    public T get(BroadcastConfig settings) {
        return getter.apply(Objects.requireNonNull(settings, "settings"));
    }

    public String format(BroadcastConfig settings) {
        return formatter.apply(get(settings));
    }

    public BroadcastConfig withValue(BroadcastConfig settings, T value) {
        return updater.apply(
            Objects.requireNonNull(settings, "settings"),
            Objects.requireNonNull(value, "value")
        );
    }

    public static ConfigKey<?> fromName(String name) {
        ConfigKey<?> key = BY_NAME.get(name);
        if (key == null) {
            throw new IllegalArgumentException("Unknown config key: " + name);
        }

        return key;
    }

    public static Map<String, ConfigKey<?>> registry() {
        return BY_NAME;
    }
}
