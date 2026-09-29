package io.github.piscescup.fabricmc.config;

import com.google.gson.JsonObject;

import java.util.Set;
import java.util.function.Function;

/**
 *
 * @author REN YuanTong
 * @since
 */
public interface Configurable<P> extends ConfigFile {

    P getProperty(String key);

    void updateProperty(String key, P value);

    Set<String> keySet();

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
