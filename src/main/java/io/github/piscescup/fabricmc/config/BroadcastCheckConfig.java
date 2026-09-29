package io.github.piscescup.fabricmc.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.piscescup.fabricmc.io.ConfigIO;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;


/**
 *
 * @author REN YuanTong
 * @since
 */
public class BroadcastCheckConfig
    implements Configurable<String>
{

    public static final int DEFAULT_CHECK_TICK = 100;
    public static final boolean DEFAULT_CHECK_ENABLED = false;

    public static final BroadcastCheckConfig DEFAULT_CHECK_CONFIG =
        new BroadcastCheckConfig(DEFAULT_CHECK_TICK, DEFAULT_CHECK_ENABLED);

    public static final String KEY_CHECK_TICK = "checkTick";
    public static final String KEY_CHECK_ENABLED = "checkEnabled";

    private final Map<String, String> cfg = new LinkedHashMap<>();

    public BroadcastCheckConfig() {
        this(DEFAULT_CHECK_TICK, DEFAULT_CHECK_ENABLED);
    }

    public BroadcastCheckConfig(int defaultCheckTick, boolean defaultCheckEnabled) {
        cfg.put(KEY_CHECK_TICK, String.valueOf(defaultCheckTick));
        cfg.put(KEY_CHECK_ENABLED, String.valueOf(defaultCheckEnabled));
    }

    public static BroadcastCheckConfig ofEnable(boolean enable) {
        return new BroadcastCheckConfig(DEFAULT_CHECK_TICK, enable);
    }

    public static BroadcastCheckConfig ofCheckTick(int checkTick) {
        return new BroadcastCheckConfig(checkTick, true);
    }

    @Override
    public Set<String> keySet() {
        return Set.of(
            KEY_CHECK_TICK,
            KEY_CHECK_ENABLED
        );
    }

    @Override
    public String getProperty(String key) {
        return cfg.get(key);
    }

    @Override
    public void updateProperty(String key, String value) {
        cfg.put(key, value);
    }

    @Override
    public JsonElement toJson() {
        JsonObject obj = new JsonObject();
        cfg.forEach(obj::addProperty);
        return obj;
    }

    @Override
    public void parseFromJson(JsonElement jsonElement) {
        this.cfg.clear();
        if (jsonElement == null || jsonElement.isJsonNull()) {
            this.cfg.put(KEY_CHECK_TICK, String.valueOf(DEFAULT_CHECK_TICK));
            this.cfg.put(KEY_CHECK_ENABLED, String.valueOf(DEFAULT_CHECK_ENABLED));
            return;
        }

        if (!jsonElement.isJsonObject()) {
            throw new IllegalArgumentException("Expected a JSON object for BroadcastCheckConfig");
        }
        JsonObject obj = jsonElement.getAsJsonObject();

        int tick = Configurable.parse(
            obj, KEY_CHECK_TICK, DEFAULT_CHECK_TICK,
            Integer::parseInt
        );

        boolean enabled = Configurable.parse(
            obj, KEY_CHECK_ENABLED, false,
            Boolean::parseBoolean
        );

        this.cfg.put(KEY_CHECK_TICK, String.valueOf(tick));
        this.cfg.put(KEY_CHECK_ENABLED, String.valueOf(enabled));
    }

    @Override
    public Path toCfgFile() {
        return ConfigIO.checkConfigFile();
    }
}
