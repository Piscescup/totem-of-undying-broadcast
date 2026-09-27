package io.github.piscescup.fabricmc.datagen.lang;

import io.github.piscescup.fabricmc.config.BroadcastLanguage;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.core.HolderLookup;

import java.util.concurrent.CompletableFuture;

/**
 *
 * @author REN YuanTong
 * @since
 */
public final class ENUSLanguageProvider
    extends LanguageGenerator
{
    public ENUSLanguageProvider(
        FabricPackOutput output,
        CompletableFuture<HolderLookup.Provider> registryLookup
    ) {
        super(output, BroadcastLanguage.EN_US, registryLookup);
    }
}