package io.github.piscescup.fabricmc.gui;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import io.github.piscescup.fabricmc.config.BroadcastCheckConfig;
import io.github.piscescup.fabricmc.config.BroadcastCommonConfig;
import io.github.piscescup.fabricmc.config.BroadcastLanguage;
import io.github.piscescup.fabricmc.config.ConfigManager;
import io.github.piscescup.fabricmc.datagen.lang.TotemTranslation;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import static io.github.piscescup.fabricmc.TotemOfUndyingBroadcastReferences.MOD_LOGGER;

public final class SettingsScreen extends Screen {
    private static final int LABEL_WIDTH = 145;
    private static final int CONTROL_WIDTH = 150;
    private static final int CONTROL_HEIGHT = 20;
    private static final int COLUMN_SPACING = 8;

    private final Screen parent;
    private final ConfigManager configManager;
    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);

    private BroadcastLanguage selectedLanguage;
    private String warningThresholdText;
    private boolean broadcastsEnabled;
    private String checkTickText;
    private boolean scheduledChecksEnabled;

    private EditBox warningThresholdBox;
    private EditBox checkTickBox;
    private StringWidget statusWidget;

    public SettingsScreen(Screen parent, ConfigManager configManager) {
        super(TotemTranslation.GUI_TITLE.component(readLanguage(configManager)));
        this.parent = parent;
        this.configManager = configManager;
        this.selectedLanguage = readLanguage(configManager);
        this.warningThresholdText = configManager.getProperty(
            BroadcastCommonConfig.KEY_WARNING_THRESHOLD,
            String.valueOf(BroadcastCommonConfig.DEFAULT_WARNING_THRESHOLD),
            value -> value);
        this.broadcastsEnabled = configManager.getProperty(
            BroadcastCommonConfig.KEY_ENABLED,
            false,
            Boolean::parseBoolean);
        this.checkTickText = configManager.getProperty(
            BroadcastCheckConfig.KEY_CHECK_TICK,
            String.valueOf(BroadcastCheckConfig.DEFAULT_CHECK_TICK),
            value -> value);
        this.scheduledChecksEnabled = configManager.getProperty(
            BroadcastCheckConfig.KEY_CHECK_ENABLED,
            BroadcastCheckConfig.DEFAULT_CHECK_ENABLED,
            Boolean::parseBoolean);
    }

    @Override
    protected void init() {
        layout.removeChildren();
        layout.addTitleHeader(text(TotemTranslation.GUI_TITLE), font);

        LinearLayout content = LinearLayout.vertical().spacing(7);
        content.addChild(sectionTitle(TotemTranslation.GUI_COMMON_TITLE), centered());
        content.addChild(createCommonSettings());
        content.addChild(sectionTitle(TotemTranslation.GUI_TICK_CHECK_TITLE), centered());
        content.addChild(createTickCheckSettings());

        statusWidget = new StringWidget(
            LABEL_WIDTH + COLUMN_SPACING + CONTROL_WIDTH,
            CONTROL_HEIGHT,
            Component.empty(),
            font);
        content.addChild(statusWidget, centered());
        layout.addToContents(content, centered());

        LinearLayout footer = LinearLayout.horizontal().spacing(COLUMN_SPACING);
        footer.addChild(Button.builder(text(TotemTranslation.GUI_DONE), button -> save()).width(120).build());
        footer.addChild(Button.builder(text(TotemTranslation.GUI_CANCEL), button -> onClose()).width(120).build());
        layout.addToFooter(footer, centered());

        layout.visitWidgets(this::addRenderableWidget);
        repositionElements();
    }

    private GridLayout createCommonSettings() {
        GridLayout grid = new GridLayout().columnSpacing(COLUMN_SPACING).rowSpacing(4);
        GridLayout.RowHelper rows = grid.createRowHelper(2);

        rows.addChild(label(TotemTranslation.GUI_LANGUAGE));
        rows.addChild(CycleButton
            .builder(language -> Component.literal(language.code()), selectedLanguage)
            .withValues(Arrays.asList(BroadcastLanguage.values()))
            .displayOnlyValue()
            .create(
                0,
                0,
                CONTROL_WIDTH,
                CONTROL_HEIGHT,
                text(TotemTranslation.GUI_LANGUAGE),
                (button, language) -> changeLanguage(language)));

        rows.addChild(label(TotemTranslation.GUI_WARNING_THRESHOLD));
        warningThresholdBox = numberBox(
            TotemTranslation.GUI_WARNING_THRESHOLD,
            warningThresholdText,
            value -> warningThresholdText = value);
        rows.addChild(warningThresholdBox);

        rows.addChild(label(TotemTranslation.GUI_BROADCAST_ENABLED));
        rows.addChild(booleanButton(
            TotemTranslation.GUI_BROADCAST_ENABLED,
            broadcastsEnabled,
            value -> broadcastsEnabled = value));

        return grid;
    }

    private GridLayout createTickCheckSettings() {
        GridLayout grid = new GridLayout().columnSpacing(COLUMN_SPACING).rowSpacing(4);
        GridLayout.RowHelper rows = grid.createRowHelper(2);

        rows.addChild(label(TotemTranslation.GUI_CHECK_TICK));
        checkTickBox = numberBox(
            TotemTranslation.GUI_CHECK_TICK,
            checkTickText,
            value -> checkTickText = value);
        rows.addChild(checkTickBox);

        rows.addChild(label(TotemTranslation.GUI_CHECK_ENABLED));
        rows.addChild(booleanButton(
            TotemTranslation.GUI_CHECK_ENABLED,
            scheduledChecksEnabled,
            value -> scheduledChecksEnabled = value));

        return grid;
    }

    private StringWidget sectionTitle(TotemTranslation translation) {
        return new StringWidget(
            text(translation).copy().withStyle(ChatFormatting.BOLD),
            font);
    }

    private StringWidget label(TotemTranslation translation) {
        return new StringWidget(
            LABEL_WIDTH,
            CONTROL_HEIGHT,
            text(translation),
            font);
    }

    private EditBox numberBox(
        TotemTranslation translation,
        String value,
        java.util.function.Consumer<String> responder
    ) {
        EditBox box = new EditBox(font, CONTROL_WIDTH, CONTROL_HEIGHT, text(translation));
        box.setMaxLength(10);
        box.setValue(value);
        box.setResponder(responder);
        return box;
    }

    private CycleButton<Boolean> booleanButton(
        TotemTranslation translation,
        boolean value,
        java.util.function.Consumer<Boolean> responder
    ) {
        return CycleButton
            .<Boolean>builder(
                enabled -> text(enabled ? TotemTranslation.GUI_ON : TotemTranslation.GUI_OFF),
                value)
            .withValues(Boolean.FALSE, Boolean.TRUE)
            .displayOnlyValue()
            .create(
                0,
                0,
                CONTROL_WIDTH,
                CONTROL_HEIGHT,
                text(translation),
                (button, enabled) -> responder.accept(enabled));
    }

    private void changeLanguage(BroadcastLanguage language) {
        selectedLanguage = language;
        rebuildWidgets();
    }

    private void save() {
        Integer warningThreshold = positiveInteger(warningThresholdText);
        Integer checkTick = positiveInteger(checkTickText);
        if (warningThreshold == null || checkTick == null) {
            showValidationError(warningThreshold == null, checkTick == null);
            return;
        }

        try {
            configManager.setProperties(Map.of(
                BroadcastCommonConfig.KEY_LANGUAGE, selectedLanguage,
                BroadcastCommonConfig.KEY_WARNING_THRESHOLD, warningThreshold,
                BroadcastCommonConfig.KEY_ENABLED, broadcastsEnabled,
                BroadcastCheckConfig.KEY_CHECK_TICK, checkTick,
                BroadcastCheckConfig.KEY_CHECK_ENABLED, scheduledChecksEnabled));
            onClose();
        } catch (IOException exception) {
            MOD_LOGGER.error("Could not save settings changed in the config screen", exception);
            statusWidget.setMessage(
                text(TotemTranslation.SAVE_FAILED).copy().withStyle(ChatFormatting.RED));
        }
    }

    private void showValidationError(boolean invalidThreshold, boolean invalidCheckTick) {
        warningThresholdBox.setTextColor(invalidThreshold ? 0xFFFF5555 : 0xFFE0E0E0);
        checkTickBox.setTextColor(invalidCheckTick ? 0xFFFF5555 : 0xFFE0E0E0);
        statusWidget.setMessage(
            text(TotemTranslation.GUI_INVALID_POSITIVE_INTEGER)
                .copy()
                .withStyle(ChatFormatting.RED));
    }

    private static Integer positiveInteger(String value) {
        try {
            int parsed = Integer.parseInt(value);
            return parsed > 0 ? parsed : null;
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private Component text(TotemTranslation translation) {
        return translation.component(selectedLanguage);
    }

    private static BroadcastLanguage readLanguage(ConfigManager configManager) {
        return configManager.getProperty(
            BroadcastCommonConfig.KEY_LANGUAGE,
            BroadcastLanguage.EN_US,
            BroadcastLanguage::fromCode);
    }

    private static java.util.function.Consumer<LayoutSettings> centered() {
        return settings -> settings.alignHorizontallyCenter();
    }

    @Override
    protected void repositionElements() {
        layout.arrangeElements();
    }

    @Override
    public void onClose() {
        minecraft.setScreenAndShow(parent);
    }
}
