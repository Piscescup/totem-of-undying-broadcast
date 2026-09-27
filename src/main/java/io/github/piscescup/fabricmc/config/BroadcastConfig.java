package io.github.piscescup.fabricmc.config;

import java.util.Objects;

public record BroadcastConfig(BroadcastLanguage language, int warningThreshold, boolean enabled) {
    public static final int DEFAULT_WARNING_THRESHOLD = 3;
    public static final BroadcastConfig DEFAULT =
            new BroadcastConfig(BroadcastLanguage.EN_US, DEFAULT_WARNING_THRESHOLD, true);

    public BroadcastConfig {
        Objects.requireNonNull(language, "language");

        if (warningThreshold < 1) {
            throw new IllegalArgumentException("The warning threshold must be at least 1");
        }
    }
}
