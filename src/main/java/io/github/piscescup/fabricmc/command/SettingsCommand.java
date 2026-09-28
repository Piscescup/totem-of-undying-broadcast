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

import io.github.piscescup.fabricmc.core.TOUMonitor;
import io.github.piscescup.fabricmc.core.TotemCountChecker;
import io.github.piscescup.fabricmc.config.*;
import io.github.piscescup.fabricmc.datagen.lang.TotemTranslation;
import io.github.piscescup.fabricmc.gui.SettingsScreen;
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

    public static void register(ConfigManager configManager, TotemCountChecker checker) {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
            dispatcher.register(commandTree(configManager, checker)));
    }

    static LiteralArgumentBuilder<FabricClientCommandSource> commandTree(
        ConfigManager configManager,
        TotemCountChecker checker
    ) {
        return ClientCommands.literal("num-tou")
            .then(ClientCommands.literal("settings")
                .executes(context -> showAll(context.getSource(), configManager))
                .then(langSettingCommand(configManager))
                .then(warningThresholdSettingCommand(configManager))
                .then(enableSettingCommand(configManager))
                .then(checkSettingCommands(configManager))
                .then(ClientCommands.literal("gui")
                    .executes(context -> openGui(context.getSource(), configManager)))
            )
            .then(ClientCommands.literal("check")
                .executes(context -> runCheck(context.getSource(), configManager, checker))
            );
    }

    private static int runCheck(
        FabricClientCommandSource source,
        ConfigManager configManager,
        TotemCountChecker checker
    ) {
        TOUMonitor.ManualCheckResult result = checker.manualCheck(source.getClient());
        BroadcastLanguage language = currentLanguage(configManager);

        return switch (result) {
            case UNAVAILABLE -> success(source, TotemTranslation.CHECK_UNAVAILABLE.component(language));
            case MISSING -> {
                source.sendError(TotemTranslation.OFFHAND_TOTEM_MISSING.component(
                    language,
                    source.getPlayer().getName())
                );
                yield 0;
            }
            case LOW_COUNT -> {
                source.sendError(TotemTranslation.LOW_TOTEM_COUNT.component(
                        language,
                        source.getPlayer().getName(),
                        checker.getTotemMonitor().countTotems()
                    )
                );
                yield 0;
            }
            case ALL -> {
                source.sendError(TotemTranslation.OFFHAND_TOTEM_MISSING.component(
                    language,
                    source.getPlayer().getName())
                );
                source.sendError(TotemTranslation.LOW_TOTEM_COUNT.component(
                        language,
                        source.getPlayer().getName(),
                        checker.getTotemMonitor().countTotems()
                    )
                );
                yield 0;
            }
            case PASS -> success(source, TotemTranslation.CHECK_PASSED.component(language));
        };
    }

    private static LiteralArgumentBuilder<FabricClientCommandSource> settingCommand(
        ConfigManager configManager,
        String name,
        String key,
        TotemTranslation currentTranslation,
        TotemTranslation updatedTranslation,
        RequiredArgumentBuilder<FabricClientCommandSource, ?> valueArgument,
        Function<CommandContext<FabricClientCommandSource>, Object> valueGetter,
        Function<ConfigManager, String> display
    ) {
        return ClientCommands.literal(name)
            .then(ClientCommands.literal(GETTER)
                .executes(context -> success(
                    context.getSource(),
                    currentTranslation.component(
                        currentLanguage(configManager),
                        display.apply(configManager)))))
            .then(ClientCommands.literal(SETTER)
                .then(valueArgument.executes(context -> {
                    try {
                        configManager.setProperty(key, valueGetter.apply(context));
                        return success(
                            context.getSource(),
                            updatedTranslation.component(
                                currentLanguage(configManager),
                                display.apply(configManager)));
                    } catch (IOException exception) {
                        return saveFailed(context.getSource(), configManager, exception);
                    }
                })));
    }

    // ---------- 三个 common 设置 ----------

    private static LiteralArgumentBuilder<FabricClientCommandSource> langSettingCommand(ConfigManager configManager) {
        RequiredArgumentBuilder<FabricClientCommandSource, ?> valueArgument = ClientCommands
            .argument(VALUE_ARGUMENT, StringArgumentType.word())
            .suggests((context, builder) -> {
                Arrays.stream(BroadcastLanguage.values())
                    .map(BroadcastLanguage::code)
                    .forEach(builder::suggest);
                return builder.buildFuture();
            });

        return settingCommand(
            configManager,
            BroadcastCommonConfig.KEY_LANGUAGE,
            BroadcastCommonConfig.KEY_LANGUAGE,
            TotemTranslation.LANGUAGE_CURRENT,
            TotemTranslation.LANGUAGE_UPDATED,
            valueArgument,
            context -> BroadcastLanguage.fromCode(
                StringArgumentType.getString(context, VALUE_ARGUMENT)),
            mgr -> BroadcastLanguage.fromCode(readRaw(mgr,
                    BroadcastCommonConfig.KEY_LANGUAGE,
                    BroadcastLanguage.EN_US.name()))
                .code()
        );
    }

    private static LiteralArgumentBuilder<FabricClientCommandSource> warningThresholdSettingCommand(
        ConfigManager configManager
    ) {
        return settingCommand(
            configManager,
            BroadcastCommonConfig.KEY_WARNING_THRESHOLD,
            BroadcastCommonConfig.KEY_WARNING_THRESHOLD,
            TotemTranslation.WARNING_THRESHOLD_CURRENT,
            TotemTranslation.WARNING_THRESHOLD_UPDATED,
            ClientCommands.argument(VALUE_ARGUMENT, IntegerArgumentType.integer(1)),
            context -> IntegerArgumentType.getInteger(context, VALUE_ARGUMENT),
            mgr -> readRaw(mgr,
                BroadcastCommonConfig.KEY_WARNING_THRESHOLD,
                String.valueOf(BroadcastCommonConfig.DEFAULT_WARNING_THRESHOLD))
        );
    }

    private static LiteralArgumentBuilder<FabricClientCommandSource> enableSettingCommand(ConfigManager configManager) {
        return settingCommand(
            configManager,
            BroadcastCommonConfig.KEY_ENABLED,
            BroadcastCommonConfig.KEY_ENABLED,
            TotemTranslation.ENABLED_CURRENT,
            TotemTranslation.ENABLED_UPDATED,
            ClientCommands.argument(VALUE_ARGUMENT, BoolArgumentType.bool()),
            context -> BoolArgumentType.getBool(context, VALUE_ARGUMENT),
            mgr -> readRaw(mgr,
                BroadcastCommonConfig.KEY_ENABLED,
                "false")
        );
    }

    // ---------- check 设置 ----------

    private static LiteralArgumentBuilder<FabricClientCommandSource> checkSettingCommands(ConfigManager configManager) {
        return ClientCommands.literal("check")
            .executes(context -> {
                BroadcastLanguage language = currentLanguage(configManager);
                return success(context.getSource(),
                    TotemTranslation.CHECK_SETTINGS_SUMMARY.component(
                        language,
                        readRaw(configManager, BroadcastCheckConfig.KEY_CHECK_TICK,
                            String.valueOf(BroadcastCheckConfig.DEFAULT_CHECK_TICK)),
                        readRaw(configManager, BroadcastCheckConfig.KEY_CHECK_ENABLED,
                            String.valueOf(BroadcastCheckConfig.DEFAULT_CHECK_ENABLED))));
            })
            .then(settingCommand(
                configManager,
                "tick",
                BroadcastCheckConfig.KEY_CHECK_TICK,
                TotemTranslation.SETTING_CHECK_TICK_CURRENT,
                TotemTranslation.SETTING_CHECK_TICK_UPDATED,
                ClientCommands.argument(VALUE_ARGUMENT, IntegerArgumentType.integer(1)),
                context -> IntegerArgumentType.getInteger(context, VALUE_ARGUMENT),
                mgr -> readRaw(mgr, BroadcastCheckConfig.KEY_CHECK_TICK,
                    String.valueOf(BroadcastCheckConfig.DEFAULT_CHECK_TICK))
            ))
            .then(settingCommand(
                configManager,
                "enable",
                BroadcastCheckConfig.KEY_CHECK_ENABLED,
                TotemTranslation.CHECK_ENABLED_CURRENT,
                TotemTranslation.CHECK_ENABLED_UPDATED,
                ClientCommands.argument(VALUE_ARGUMENT, BoolArgumentType.bool()),
                context -> BoolArgumentType.getBool(context, VALUE_ARGUMENT),
                mgr -> readRaw(mgr, BroadcastCheckConfig.KEY_CHECK_ENABLED,
                    String.valueOf(BroadcastCheckConfig.DEFAULT_CHECK_ENABLED))
            ));
    }

    // ---------- /num-tou settings ----------

    private static int openGui(
        FabricClientCommandSource source,
        ConfigManager configManager
    ) {
        source.getClient().schedule(() ->
            source.getClient().setScreenAndShow(new SettingsScreen(null, configManager)));
        return 1;
    }

    private static int showAll(FabricClientCommandSource source, ConfigManager configManager) {
        BroadcastLanguage language = currentLanguage(configManager);
        return success(
            source,
            TotemTranslation.SETTINGS_SUMMARY.component(
                language,
                BroadcastLanguage.fromCode(readRaw(configManager,
                        BroadcastCommonConfig.KEY_LANGUAGE,
                        BroadcastLanguage.EN_US.name()))
                    .code(),
                readRaw(configManager, BroadcastCommonConfig.KEY_WARNING_THRESHOLD,
                    String.valueOf(BroadcastCommonConfig.DEFAULT_WARNING_THRESHOLD)),
                readRaw(configManager, BroadcastCommonConfig.KEY_ENABLED, "false"),
                readRaw(configManager, BroadcastCheckConfig.KEY_CHECK_TICK,
                    String.valueOf(BroadcastCheckConfig.DEFAULT_CHECK_TICK)),
                readRaw(configManager, BroadcastCheckConfig.KEY_CHECK_ENABLED,
                    String.valueOf(BroadcastCheckConfig.DEFAULT_CHECK_ENABLED))
            )
        );
    }

    // ---------- 工具 ----------

    private static BroadcastLanguage currentLanguage(ConfigManager configManager) {
        return configManager.getProperty(
            BroadcastCommonConfig.KEY_LANGUAGE,
            BroadcastLanguage.EN_US,
            BroadcastLanguage::fromCode);
    }

    /** 读指定 key 的原始字符串；缺字段用 defaultValue。 */
    private static String readRaw(ConfigManager configManager, String key, String defaultValue) {
        return configManager.getProperty(key, defaultValue, Function.identity());
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
        source.sendError(TotemTranslation.SAVE_FAILED.component(currentLanguage(configManager)));
        return 0;
    }
}
