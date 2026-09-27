package io.github.piscescup.fabricmc.command;

import java.io.IOException;
import java.util.Arrays;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import io.github.piscescup.fabricmc.TotemCountMonitor;
import io.github.piscescup.fabricmc.config.BroadcastLanguage;
import io.github.piscescup.fabricmc.config.BroadcastConfig;
import io.github.piscescup.fabricmc.config.ConfigManager;
import io.github.piscescup.fabricmc.datagen.lang.TotemTranslation;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.network.chat.Component;

import static io.github.piscescup.fabricmc.TotemOfUndyingBroadcastReferences.MOD_LOGGER;

public final class SettingsCommand {
    private static final String VALUE_ARGUMENT = "value";

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

    private static LiteralArgumentBuilder<FabricClientCommandSource> langSettingCommand(ConfigManager configManager) {
        return ClientCommands.literal("lang")
            .executes(context -> showLanguage(context.getSource(), configManager))
            .then(ClientCommands.argument(VALUE_ARGUMENT, StringArgumentType.word())
                .suggests((context, builder) -> {
                    Arrays.stream(BroadcastLanguage.values())
                        .map(BroadcastLanguage::code)
                        .forEach(builder::suggest);

                    return builder.buildFuture();
                })
                .executes(context -> setLanguage(
                        context.getSource(),
                    configManager,
                        BroadcastLanguage.fromCode(StringArgumentType.getString(context, VALUE_ARGUMENT))
                    )
                )
            );
    }

    private static LiteralArgumentBuilder<FabricClientCommandSource> warningThresholdSettingCommand(
        ConfigManager configManager
    ) {
        return ClientCommands.literal("warning-threshold")
            .executes(context -> showWarningThreshold(context.getSource(), configManager))
            .then(ClientCommands.argument(VALUE_ARGUMENT, IntegerArgumentType.integer(1))
                .executes(context -> setWarningThreshold(
                        context.getSource(),
                        configManager,
                        IntegerArgumentType.getInteger(context, VALUE_ARGUMENT)
                    )
                )
            );
    }

    private static LiteralArgumentBuilder<FabricClientCommandSource> enableSettingCommand(ConfigManager configManager) {
        return ClientCommands.literal("enable")
            .executes(context -> showEnabled(context.getSource(), configManager))
            .then(ClientCommands.argument(VALUE_ARGUMENT, BoolArgumentType.bool())
                .executes(context -> setEnabled(
                        context.getSource(),
                    configManager,
                        BoolArgumentType.getBool(context, VALUE_ARGUMENT)
                    )
                )
            );
    }

    private static int showAll(FabricClientCommandSource source, ConfigManager configManager) {
        BroadcastConfig settings = configManager.settings();
        return success(
            source,
            TotemTranslation.SETTINGS_SUMMARY.component(
                settings.language(),
                settings.language()
                    .code(),
                settings.warningThreshold(),
                String.valueOf(settings.enabled())
            )
        );
    }

    private static int showLanguage(FabricClientCommandSource source, ConfigManager configManager) {
        BroadcastConfig settings = configManager.settings();
        return success(
            source,
            TotemTranslation.LANGUAGE_CURRENT.component(
                settings.language(),
                settings.language()
                    .code()
            )
        );
    }

    private static int setLanguage(
        FabricClientCommandSource source,
        ConfigManager configManager,
        BroadcastLanguage language
    ) {
        try {
            BroadcastConfig settings = configManager.setLanguage(language);
            return success(
                source,
                TotemTranslation.LANGUAGE_UPDATED.component(
                    settings.language(),
                    settings.language()
                        .code()
                )
            );
        } catch (IOException exception) {
            return saveFailed(source, configManager, exception);
        }
    }

    private static int showWarningThreshold(FabricClientCommandSource source, ConfigManager configManager) {
        BroadcastConfig settings = configManager.settings();
        return success(
            source,
            TotemTranslation.WARNING_THRESHOLD_CURRENT.component(
                settings.language(),
                settings.warningThreshold()
            )
        );
    }

    private static int setWarningThreshold(
        FabricClientCommandSource source,
        ConfigManager configManager,
        int warningThreshold
    ) {
        try {
            BroadcastConfig settings = configManager.setWarningThreshold(warningThreshold);
            return success(
                source,
                TotemTranslation.WARNING_THRESHOLD_UPDATED.component(
                    settings.language(),
                    settings.warningThreshold()
                )
            );
        } catch (IOException exception) {
            return saveFailed(source, configManager, exception);
        }
    }

    private static int showEnabled(FabricClientCommandSource source, ConfigManager configManager) {
        BroadcastConfig settings = configManager.settings();
        return success(
            source,
            TotemTranslation.ENABLED_CURRENT.component(
                settings.language(),
                String.valueOf(settings.enabled())
            )
        );
    }

    private static int setEnabled(
        FabricClientCommandSource source,
        ConfigManager configManager,
        boolean enabled
    ) {
        try {
            BroadcastConfig settings = configManager.setEnabled(enabled);
            return success(
                source,
                TotemTranslation.ENABLED_UPDATED.component(
                    settings.language(),
                    String.valueOf(settings.enabled())
                )
            );
        } catch (IOException exception) {
            return saveFailed(source, configManager, exception);
        }
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
