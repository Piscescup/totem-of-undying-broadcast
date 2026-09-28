package io.github.piscescup.fabricmc.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.piscescup.fabricmc.io.ConfigIO;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;


public class BroadcastCommonConfig
    implements Configurable
{
    public static final int MIN_WARNING_THRESHOLD = 1;
    public static final int DEFAULT_WARNING_THRESHOLD = 3;

    public static final BroadcastCommonConfig DEFAULT_COMMON_CONFIGS =
        new BroadcastCommonConfig(BroadcastLanguage.EN_US,
            DEFAULT_WARNING_THRESHOLD,
            false);

    public static final String KEY_LANGUAGE          = "language";
    public static final String KEY_WARNING_THRESHOLD = "warningThreshold";
    public static final String KEY_ENABLED           = "enabled";

    private final Map<String, String> cfg = new LinkedHashMap<>();

    public BroadcastCommonConfig() {
        this(BroadcastLanguage.EN_US, DEFAULT_WARNING_THRESHOLD, true);
    }

    public BroadcastCommonConfig(BroadcastLanguage language, int warningThreshold, boolean enable) {
        cfg.put(KEY_LANGUAGE, language.name());
        cfg.put(KEY_WARNING_THRESHOLD, String.valueOf(warningThreshold));
        cfg.put(KEY_ENABLED, String.valueOf(enable));
    }

    @Override
    public <T> T getProperty(String key, Function<String, T> converter) {
        return converter.apply(cfg.get(key));
    }

    @Override
    public <T> void updateProperty(String key, T value) {
        cfg.put(key, String.valueOf(value));
    }

    @Override
    public JsonElement toJson() {
        JsonObject obj = new JsonObject();
        cfg.forEach(obj::addProperty);
        return obj;
    }

    @Override
    public Set<String> keySet() {
        return Set.of(
            KEY_LANGUAGE,
            KEY_WARNING_THRESHOLD,
            KEY_ENABLED
        );
    }

    @Override
    public void parseFromJson(JsonElement jsonElement) {
        this.cfg.clear();
        if (jsonElement == null || jsonElement.isJsonNull()) {
            this.cfg.put(KEY_LANGUAGE, BroadcastLanguage.EN_US.name());
            this.cfg.put(KEY_ENABLED, String.valueOf(false));
            this.cfg.put(KEY_WARNING_THRESHOLD, String.valueOf(DEFAULT_WARNING_THRESHOLD));
            return;
        }

        if (!jsonElement.isJsonObject()) {
            throw new IllegalArgumentException(
                "Expected a JSON object for BroadcastCommonConfig");
        }
        JsonObject obj = jsonElement.getAsJsonObject();

        BroadcastLanguage lang = Configurable.parse(
            obj, KEY_LANGUAGE, BroadcastLanguage.EN_US,
            BroadcastLanguage::fromCode
        );
        int threshold = Configurable.parse(
            obj, KEY_WARNING_THRESHOLD, MIN_WARNING_THRESHOLD,
            Integer::parseInt
        );
        boolean enabled = Configurable.parse(
            obj, KEY_ENABLED, true,
            Boolean::parseBoolean
        );

        this.cfg.put(KEY_WARNING_THRESHOLD, String.valueOf(threshold));
        this.cfg.put(KEY_ENABLED, String.valueOf(enabled));
        this.cfg.put(KEY_LANGUAGE, lang.name());
    }

    @Override
    public Path toCfgFile() {
        return ConfigIO.commonConfigFile();
    }
}
