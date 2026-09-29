package io.github.piscescup.fabricmc.datagen.lang;

import io.github.piscescup.fabricmc.config.BroadcastLanguage;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public enum TotemTranslation {
    LOW_TOTEM_COUNT(
            "message.totem-of-undying-broadcast.low_totem_count",
            "[Warning] %1$s only has %2$s Totem of Undying left!",
            "【警告】 %1$s 的不死图腾仅剩 %2$s 个！"),
    OFFHAND_TOTEM_MISSING(
            "message.totem-of-undying-broadcast.offhand_totem_missing",
            "[Warning] %1$s does not have a Totem of Undying in their offhand!",
            "【警告】 %1$s 的副手没有不死图腾！"),
    SETTINGS_SUMMARY(
            "command.totem-of-undying-broadcast.settings.summary",
            "Num of ToU Broadcast settings — [language: %1$s, warning threshold: %2$s, count broadcasts: %3$s, check interval: %4$s tick(s), scheduled checks: %5$s]",
            "不死图腾数量播报设置—[语言：%1$s，警告阈值：%2$s，数量变化播报：%3$s，检查间隔：%4$s tick，定时检查：%5$s]"),
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
    SETTING_CHECK_TICK_CURRENT(
        "command.totem-of-undying-broadcast.settings.check.tick.current",
        "Scheduled check interval: %1$s tick(s)",
        "定时检查间隔：%1$s tick"
    ),
    SETTING_CHECK_TICK_UPDATED(
        "command.totem-of-undying-broadcast.settings.check.tick.updated",
        "Scheduled check interval set to %1$s tick(s).",
        "定时检查间隔已设置为 %1$s tick。"
    ),
    CHECK_SETTINGS_SUMMARY(
            "command.totem-of-undying-broadcast.settings.check.summary",
            "Scheduled checks — [interval: %1$s tick(s), enabled: %2$s]",
            "定时检查设置—[间隔：%1$s tick，启用：%2$s]"),
    CHECK_ENABLED_CURRENT(
            "command.totem-of-undying-broadcast.settings.check.enable.current",
            "Scheduled checks currently enabled: %1$s",
            "当前是否启用定时检查：%1$s"),
    CHECK_ENABLED_UPDATED(
            "command.totem-of-undying-broadcast.settings.check.enable.updated",
            "Scheduled checks enabled: %1$s.",
            "定时检查启用状态已设置为：%1$s。"),
    CHECK_PASSED(
            "command.totem-of-undying-broadcast.check.passed",
            "Check passed: the totem count is sufficient and the offhand contains a Totem of Undying.",
            "检查通过：不死图腾数量充足，且副手持有不死图腾。"),
    CHECK_UNAVAILABLE(
            "command.totem-of-undying-broadcast.check.unavailable",
            "This check is only available while playing in a world or on a server.",
            "只有进入世界或服务器后才能进行检查。"),
    GROUP_CREATED(
            "command.totem-of-undying-broadcast.group.created",
            "Created group %1$s with ID %2$s. Invitation:",
            "已创建群组 %1$s，ID 为 %2$s。邀请链接："),
    GROUP_JOIN_LINK(
            "command.totem-of-undying-broadcast.group.join_link",
            "[Join %1$s]",
            "[加入 %1$s]"),
    GROUP_JOIN_HINT(
            "command.totem-of-undying-broadcast.group.join_hint",
            "Click to put %1$s in the chat box",
            "点击将 %1$s 填入聊天栏"),
    GROUP_JOINED(
            "command.totem-of-undying-broadcast.group.joined",
            "Joined group %1$s (ID: %2$s).",
            "已加入群组 %1$s（ID：%2$s）。"),
    GROUP_ALREADY_JOINED(
            "command.totem-of-undying-broadcast.group.already_joined",
            "You have already joined group %1$s (ID: %2$s).",
            "你已经加入群组 %1$s（ID：%2$s）。"),
    GROUP_NAME_EXISTS(
            "command.totem-of-undying-broadcast.group.name_exists",
            "Group %1$s already exists with ID %2$s.",
            "群组 %1$s 已存在，其 ID 为 %2$s。"),
    GROUP_NAME_CONFLICT(
            "command.totem-of-undying-broadcast.group.name_conflict",
            "Group %1$s already uses ID %2$s, not %3$s.",
            "群组 %1$s 已使用 ID %2$s，而不是 %3$s。"),
    GROUP_ID_CONFLICT(
            "command.totem-of-undying-broadcast.group.id_conflict",
            "Group ID %1$s is already used by %2$s.",
            "群组 ID %1$s 已被群组 %2$s 使用。"),
    GROUP_NAME_INVALID(
            "command.totem-of-undying-broadcast.group.name_invalid",
            "Group names must contain 1-64 characters and cannot contain '/' or control characters.",
            "群组名称须为 1 至 64 个字符，且不能包含“/”或控制字符。"),
    GROUP_INVITATION_INVALID(
            "command.totem-of-undying-broadcast.group.invitation_invalid",
            "Invalid group invitation. Use <name>/<positive integer ID>.",
            "群组邀请无效，请使用 <名称>/<正整数 ID>。"),
    GROUP_SAVE_FAILED(
            "command.totem-of-undying-broadcast.group.save_failed",
            "Unable to save the group. Check the client log for details.",
            "无法保存群组，请查看客户端日志了解详情。"),
    GUI_TITLE(
            "screen.totem-of-undying-broadcast.title",
            "Totem of Undying Broadcast",
            "不死图腾数量播报"),
    GUI_COMMON_TITLE(
            "screen.totem-of-undying-broadcast.common.title",
            "Common",
            "通用设置"),
    GUI_TICK_CHECK_TITLE(
            "screen.totem-of-undying-broadcast.tick_check.title",
            "Tick Check",
            "定时检查"),
    GUI_LANGUAGE(
            "screen.totem-of-undying-broadcast.common.language",
            "Language",
            "语言"),
    GUI_WARNING_THRESHOLD(
            "screen.totem-of-undying-broadcast.common.warning_threshold",
            "Warning threshold",
            "警告阈值"),
    GUI_BROADCAST_ENABLED(
            "screen.totem-of-undying-broadcast.common.enabled",
            "Count broadcasts",
            "数量变化播报"),
    GUI_CHECK_TICK(
            "screen.totem-of-undying-broadcast.tick_check.tick",
            "Check interval (ticks)",
            "检查间隔（tick）"),
    GUI_CHECK_ENABLED(
            "screen.totem-of-undying-broadcast.tick_check.enabled",
            "Scheduled checks",
            "定时检查"),
    GUI_ON(
            "screen.totem-of-undying-broadcast.value.on",
            "On",
            "开启"),
    GUI_OFF(
            "screen.totem-of-undying-broadcast.value.off",
            "Off",
            "关闭"),
    GUI_DONE(
            "screen.totem-of-undying-broadcast.done",
            "Done",
            "完成"),
    GUI_CANCEL(
            "screen.totem-of-undying-broadcast.cancel",
            "Cancel",
            "取消"),
    GUI_INVALID_POSITIVE_INTEGER(
            "screen.totem-of-undying-broadcast.error.positive_integer",
            "Enter a positive whole number.",
            "请输入正整数。"),
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
