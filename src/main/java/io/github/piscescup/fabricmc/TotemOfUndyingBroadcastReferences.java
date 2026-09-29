package io.github.piscescup.fabricmc;


import net.minecraft.resources.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;

/**
 *
 * @author REN YuanTong
 * @since 1.0.0
 */
public final class TotemOfUndyingBroadcastReferences {
    public static final String MOD_ID = "totem-of-undying-broadcast";

    public static final String MOD_NAME = "Num of ToU Broadcast";

    public static final Logger MOD_LOGGER = LogManager.getLogger(MOD_NAME);

    @Contract("_ -> new")
    public static @NonNull Identifier fromPath(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
