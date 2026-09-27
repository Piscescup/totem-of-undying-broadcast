package io.github.piscescup.fabricmc.command;

import java.io.IOException;
import java.util.Arrays;
import java.util.function.Function;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

import io.github.piscescup.fabricmc.TotemCountMonitor;
import io.github.piscescup.fabricmc.config.BroadcastLanguage;
import io.github.piscescup.fabricmc.config.BroadcastConfig;
import io.github.piscescup.fabricmc.config.ConfigKey;
import io.github.piscescup.fabricmc.config.ConfigManager;
import io.github.piscescup.fabricmc.datagen.lang.TotemTranslation;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.network.chat.Component;

import static io.github.piscescup.fabricmc.TotemOfUndyingBroadcastReferences.MOD_LOGGER;

public final class SettingsCommand {
    private static final String VALUE_ARGUMENT = "value";

    private static final String GETTER = "get";
    private static final String SETTER = "set";

    private SettingsCommand() {}

    public static void register(ConfigManager configManager, TotemCountMonitor monitor) {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(
            ClientCommands.literal("num-tou")
                .then(ClientCommands.literal("settings")
                    .executes(context -> showAll(context.getSource(), configManager))
                    .then(langSettingCommand(configManager))
                    .then(warningThresholdSettingCommand(configManager))
                    .then(enableSettingCommand(configManager))
                )
                .then(ClientCommands.literal("check")
                    .executes(context -> runCheck(context.getSource(), configManager, monitor))
                )
        ));
    }

    private static int runCheck(
        FabricClientCommandSource source,
        ConfigManager configManager,
        TotemCountMonitor monitor
    ) {
        TotemCountMonitor.ManualCheckResult result = monitor.runManualCheck();
        BroadcastLanguage language = configManager.settings().language();

        return switch (result) {
            case UNAVAILABLE -> {
                source.sendError(TotemTranslation.CHECK_UNAVAILABLE.component(language));
                yield 0;
            }
            case PASSED -> success(source, TotemTranslation.CHECK_PASSED.component(language));
            case WARNING_SENT -> 1;
        };
    }

    private static <T> LiteralArgumentBuilder<FabricClientCommandSource> getCommand(
        ConfigManager configManager,
        ConfigKey<T> key,
        TotemTranslation translation
    ) {
        return ClientCommands.literal(GETTER)
            .executes(context -> {
                BroadcastConfig settings = configManager.settings();

                return success(
                    context.getSource(),
                    translation.component(settings.language(), key.format(settings))
                );
            });
    }

    private static <T> LiteralArgumentBuilder<FabricClientCommandSource> setCommand(
        ConfigManager configManager,
        ConfigKey<T> key,
        TotemTranslation translation,
        RequiredArgumentBuilder<FabricClientCommandSource, ?> valueArgument,
        Function<CommandContext<FabricClientCommandSource>, T> valueGetter
    ) {
        return ClientCommands.literal(SETTER)
            .then(valueArgument.executes(context -> {
                try {
                    BroadcastConfig settings = configManager.set(key, valueGetter.apply(context));

                    return success(
                        context.getSource(),
                        translation.component(settings.language(), key.format(settings))
                    );
                } catch (IOException exception) {
                    return saveFailed(context.getSource(), configManager, exception);
                }
            }));
    }

    private static LiteralArgumentBuilder<FabricClientCommandSource> langSettingCommand(ConfigManager configManager) {
        RequiredArgumentBuilder<FabricClientCommandSource, ?> valueArgument = ClientCommands
            .argument(VALUE_ARGUMENT, StringArgumentType.word())
            .suggests((context, builder) -> {
                Arrays.stream(BroadcastLanguage.values())
                    .map(BroadcastLanguage::code)
                    .forEach(builder::suggest);

                return builder.buildFuture();
            });

        return ClientCommands.literal(ConfigKey.LANGUAGE.name())
            .then(getCommand(configManager, ConfigKey.LANGUAGE, TotemTranslation.LANGUAGE_CURRENT))
            .then(setCommand(
                configManager,
                ConfigKey.LANGUAGE,
                TotemTranslation.LANGUAGE_UPDATED,
                valueArgument,
                context -> BroadcastLanguage.fromCode(StringArgumentType.getString(context, VALUE_ARGUMENT))
            ));
    }

    private static LiteralArgumentBuilder<FabricClientCommandSource> warningThresholdSettingCommand(
        ConfigManager configManager
    ) {
        return ClientCommands.literal(ConfigKey.WARNING_THRESHOLD.name())
            .then(getCommand(
                configManager,
                ConfigKey.WARNING_THRESHOLD,
                TotemTranslation.WARNING_THRESHOLD_CURRENT
            ))
            .then(setCommand(
                configManager,
                ConfigKey.WARNING_THRESHOLD,
                TotemTranslation.WARNING_THRESHOLD_UPDATED,
                ClientCommands.argument(VALUE_ARGUMENT, IntegerArgumentType.integer(1)),
                context -> IntegerArgumentType.getInteger(context, VALUE_ARGUMENT)
            ));
    }

    private static LiteralArgumentBuilder<FabricClientCommandSource> enableSettingCommand(ConfigManager configManager) {
        return ClientCommands.literal(ConfigKey.ENABLED.name())
            .then(getCommand(configManager, ConfigKey.ENABLED, TotemTranslation.ENABLED_CURRENT))
            .then(setCommand(
                configManager,
                ConfigKey.ENABLED,
                TotemTranslation.ENABLED_UPDATED,
                ClientCommands.argument(VALUE_ARGUMENT, BoolArgumentType.bool()),
                context -> BoolArgumentType.getBool(context, VALUE_ARGUMENT)
            ));
    }

    private static int showAll(FabricClientCommandSource source, ConfigManager configManager) {
        BroadcastConfig settings = configManager.settings();
        return success(
            source,
            TotemTranslation.SETTINGS_SUMMARY.component(
                settings.language(),
                ConfigKey.LANGUAGE.format(settings),
                ConfigKey.WARNING_THRESHOLD.format(settings),
                ConfigKey.ENABLED.format(settings)
            )
        );
    }

    private static int success(FabricClientCommandSource source, Component message) {
        source.sendFeedback(message);
        return 1;
    }

    private static int saveFailed(
        FabricClientCommandSource source,
        ConfigManager configManager,
        IOException exception
    ) {
        MOD_LOGGER.error("Could not save settings changed by a command", exception);
        BroadcastLanguage language = configManager.settings()
            .language();
        source.sendError(TotemTranslation.SAVE_FAILED.component(language));
        return 0;
    }
}
