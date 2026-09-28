package io.github.piscescup.fabricmc.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.nio.file.Path;
import java.util.Set;
import java.util.function.Function;

/**
 *
 * @author REN YuanTong
 * @since
 */
public interface Configurable {

    JsonElement toJson();

    void parseFromJson(JsonElement jsonElement);

    <T> T getProperty(String key, Function<String, T> converter);

    <T> void updateProperty(String key, T value);

    Set<String> keySet();

    Path toCfgFile();

    static <T> T parse(
        JsonObject jo, String key, T defaultValue,
        Function<String, T> parser
    ) {
        return jo.has(key) ?
            parser.apply(jo.get(key)
                .getAsString()) :
            defaultValue;
    }
}
