package com.abo47.questsandstuff.client.tablet.modal.settings;

import java.util.List;

import com.abo47.questsandstuff.QuestsAndStuffConfig;
import com.abo47.questsandstuff.client.tablet.state.UiState;

final class AnimationSettingsOptions {
    private AnimationSettingsOptions() {
    }

    static List<SettingsOptionDescriptor> options(UiState state) {
        return List.of(
                new SettingsOptionDescriptor(
                        "uiAnimations",
                        "ui.questsandstuff.settings.ui_animations",
                        "ui.questsandstuff.settings.ui_animations_desc",
                        QuestsAndStuffConfig::uiAnimationsEnabled,
                        QuestsAndStuffConfig::setUiAnimationsEnabled,
                        false,
                        false
                ),
                new SettingsOptionDescriptor(
                        "contextMenuAnimations",
                        "ui.questsandstuff.settings.context_menu_animations",
                        "ui.questsandstuff.settings.context_menu_animations_desc",
                        QuestsAndStuffConfig::contextMenuAnimationSettingEnabled,
                        QuestsAndStuffConfig::setContextMenuAnimationsEnabled,
                        false,
                        true
                ),
                new SettingsOptionDescriptor(
                        "toolsMenuAnimations",
                        "ui.questsandstuff.settings.tools_menu_animations",
                        "ui.questsandstuff.settings.tools_menu_animations_desc",
                        QuestsAndStuffConfig::toolsMenuAnimationSettingEnabled,
                        QuestsAndStuffConfig::setToolsMenuAnimationsEnabled,
                        false,
                        true
                ),
                new SettingsOptionDescriptor(
                        "minimapAnimations",
                        "ui.questsandstuff.settings.minimap_animations",
                        "ui.questsandstuff.settings.minimap_animations_desc",
                        QuestsAndStuffConfig::minimapAnimationSettingEnabled,
                        QuestsAndStuffConfig::setMinimapAnimationsEnabled,
                        false,
                        true
                ),
                new SettingsOptionDescriptor(
                        "questWindowAnimations",
                        "ui.questsandstuff.settings.quest_window_animations",
                        "ui.questsandstuff.settings.quest_window_animations_desc",
                        QuestsAndStuffConfig::questWindowAnimationSettingEnabled,
                        QuestsAndStuffConfig::setQuestWindowAnimationsEnabled,
                        false,
                        true
                ),
                new SettingsOptionDescriptor(
                        "popupWindowAnimations",
                        "ui.questsandstuff.settings.popup_window_animations",
                        "ui.questsandstuff.settings.popup_window_animations_desc",
                        QuestsAndStuffConfig::popupWindowAnimationSettingEnabled,
                        QuestsAndStuffConfig::setPopupWindowAnimationsEnabled,
                        false,
                        true
                ),
                new SettingsOptionDescriptor(
                        "connectionAnimations",
                        "ui.questsandstuff.settings.connection_animations",
                        "ui.questsandstuff.settings.connection_animations_desc",
                        QuestsAndStuffConfig::connectionAnimationSettingEnabled,
                        QuestsAndStuffConfig::setConnectionAnimationsEnabled,
                        false,
                        true
                ),
                new SettingsOptionDescriptor(
                        "chapterSwitchAnimations",
                        "ui.questsandstuff.settings.chapter_switch_animations",
                        "ui.questsandstuff.settings.chapter_switch_animations_desc",
                        QuestsAndStuffConfig::chapterSwitchAnimationSettingEnabled,
                        QuestsAndStuffConfig::setChapterSwitchAnimationsEnabled,
                        false,
                        true
                ),
                new SettingsOptionDescriptor(
                        "questHoverExpandAnimations",
                        "ui.questsandstuff.settings.quest_hover_expand_animations",
                        "ui.questsandstuff.settings.quest_hover_expand_animations_desc",
                        QuestsAndStuffConfig::questHoverExpandAnimationSettingEnabled,
                        QuestsAndStuffConfig::setQuestHoverExpandAnimationsEnabled,
                        false,
                        true
                ),
                new SettingsOptionDescriptor(
                        "questHoverExpandPercent",
                        "ui.questsandstuff.settings.quest_hover_expand_percent",
                        "ui.questsandstuff.settings.quest_hover_expand_percent_desc",
                        QuestsAndStuffConfig::questHoverExpandPercent,
                        QuestsAndStuffConfig::setQuestHoverExpandPercent,
                        QuestsAndStuffConfig.MIN_QUEST_HOVER_EXPAND_PERCENT,
                        QuestsAndStuffConfig.MAX_QUEST_HOVER_EXPAND_PERCENT,
                        3,
                        false,
                        ""
                )
        );
    }
}
