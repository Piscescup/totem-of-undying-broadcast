package io.github.piscescup.fabricmc.config;

import com.google.gson.JsonElement;

import java.nio.file.Path;

/**
 * A configuration file that can be serialized and loaded in place.
 */
public interface ConfigFile {
    JsonElement toJson();

    void parseFromJson(JsonElement jsonElement);

    Path toCfgFile();
}
