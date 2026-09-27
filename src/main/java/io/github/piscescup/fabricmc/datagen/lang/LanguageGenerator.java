package io.github.piscescup.fabricmc.datagen.lang;

import io.github.piscescup.fabricmc.config.BroadcastLanguage;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.core.HolderLookup;
import org.jspecify.annotations.NonNull;

import java.util.concurrent.CompletableFuture;

/**
 *
 * @author REN YuanTong
 * @since
 */
public class LanguageGenerator extends FabricLanguageProvider {

    private final BroadcastLanguage language;

    LanguageGenerator(
        FabricPackOutput output,
        BroadcastLanguage language,
        CompletableFuture<HolderLookup.Provider> registryLookup) {
        super(output, language.code(), registryLookup);
        this.language = language;
    }

    @Override
    public final void generateTranslations(
        HolderLookup.@NonNull Provider registryLookup,
        FabricLanguageProvider.@NonNull TranslationBuilder translationBuilder
    ) {
        for (TotemTranslation translation : TotemTranslation.values()) {
            translationBuilder.add(translation.key(language), translation.text(language));
        }
    }
}
