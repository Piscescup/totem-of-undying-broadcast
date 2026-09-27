package io.github.piscescup.fabricmc.datagen.lang;

import io.github.piscescup.fabricmc.config.BroadcastLanguage;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public enum TotemTranslation {
    LOW_TOTEM_COUNT(
            "message.totem-of-undying-broadcast.low_totem_count",
            "⚠ %1$s only has %2$s Totem of Undying left!",
            "⚠ %1$s 的不死图腾仅剩 %2$s 个！"),
    OFFHAND_TOTEM_MISSING(
            "message.totem-of-undying-broadcast.offhand_totem_missing",
            "⚠ %1$s does not have a Totem of Undying in his(her) offhand!",
            "⚠ %1$s 的副手没有不死图腾！"),
    SETTINGS_SUMMARY(
            "command.totem-of-undying-broadcast.settings.summary",
            "Num of ToU Broadcast settings — [language: %1$s, warning threshold: %2$s, enabled: %3$s]",
            "不死图腾数量播报设置—[语言：%1$s，警告阈值：%2$s，启用：%3$s]"),
    LANGUAGE_CURRENT(
            "command.totem-of-undying-broadcast.settings.lang.current",
            "Current language: %1$s",
            "当前语言：%1$s"),
    LANGUAGE_UPDATED(
            "command.totem-of-undying-broadcast.settings.lang.updated",
            "Language set to %1$s.",
            "语言已设置为 %1$s。"),
    WARNING_THRESHOLD_CURRENT(
            "command.totem-of-undying-broadcast.settings.warning_threshold.current",
            "Current warning threshold: %1$s",
            "当前警告阈值：%1$s"),
    WARNING_THRESHOLD_UPDATED(
            "command.totem-of-undying-broadcast.settings.warning_threshold.updated",
            "Warning threshold set to %1$s.",
            "警告阈值已设置为 %1$s。"),
    ENABLED_CURRENT(
            "command.totem-of-undying-broadcast.settings.enable.current",
            "Broadcasts currently enabled: %1$s",
            "当前是否启用播报：%1$s"),
    ENABLED_UPDATED(
            "command.totem-of-undying-broadcast.settings.enable.updated",
            "Broadcasts enabled: %1$s.",
            "播报启用状态已设置为：%1$s。"),
    CHECK_PASSED(
            "command.totem-of-undying-broadcast.check.passed",
            "Check passed: the totem count is sufficient and the offhand contains a Totem of Undying.",
            "检查通过：不死图腾数量充足，且副手持有不死图腾。"),
    CHECK_UNAVAILABLE(
            "command.totem-of-undying-broadcast.check.unavailable",
            "This check is only available while playing in a world or on a server.",
            "只有进入世界或服务器后才能进行检查。"),
    SAVE_FAILED(
            "command.totem-of-undying-broadcast.settings.save_failed",
            "Unable to save the settings. Check the client log for details.",
            "无法保存设置，请查看客户端日志了解详情。");

    private final String baseKey;
    private final String englishText;
    private final String chineseText;

    TotemTranslation(String baseKey, String englishText, String chineseText) {
        this.baseKey = baseKey;
        this.englishText = englishText;
        this.chineseText = chineseText;
    }

    public String key(BroadcastLanguage language) {
        return baseKey + "." + language.code();
    }

    public String text(BroadcastLanguage language) {
        return switch (language) {
            case EN_US -> englishText;
            case ZH_CN -> chineseText;
        };
    }

    public MutableComponent component(BroadcastLanguage language, Object... arguments) {
        return Component.translatableWithFallback(key(language), text(language), arguments);
    }
}
