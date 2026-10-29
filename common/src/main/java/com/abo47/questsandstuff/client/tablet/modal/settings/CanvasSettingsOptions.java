package com.abo47.questsandstuff.client.tablet.modal.settings;

import java.util.List;

import com.abo47.questsandstuff.QuestsAndStuffConfig;
import com.abo47.questsandstuff.client.tablet.bootstrap.TabletScreenManager;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;

final class CanvasSettingsOptions {
    private CanvasSettingsOptions() {
    }

    static List<SettingsOptionDescriptor> options(TabletUiState state) {
        return List.of(
                new SettingsOptionDescriptor(
                        "fullScreenMode",
                        "ui.questsandstuff.settings.full_screen_mode",
                        "ui.questsandstuff.settings.full_screen_mode_desc",
                        QuestsAndStuffConfig::fullScreenModeEnabled,
                        enabled -> setFullScreenMode(state, enabled),
                        false,
                        false
                ),
                new SettingsOptionDescriptor(
                        "minimap",
                        "ui.questsandstuff.settings.minimap",
                        "ui.questsandstuff.settings.minimap_desc",
                        QuestsAndStuffConfig::minimapEnabled,
                        QuestsAndStuffConfig::setMinimapEnabled,
                        false,
                        false
                ),
                new SettingsOptionDescriptor(
                        "visualMinimap",
                        "ui.questsandstuff.settings.visual_minimap",
                        "ui.questsandstuff.settings.visual_minimap_desc",
                        QuestsAndStuffConfig::visualMinimapEnabled,
                        QuestsAndStuffConfig::setVisualMinimapEnabled,
                        false,
                        false
                ),
                new SettingsOptionDescriptor(
                        "readOnlyCanvasFocus",
                        "ui.questsandstuff.settings.read_only_canvas_focus",
                        "ui.questsandstuff.settings.read_only_canvas_focus_desc",
                        QuestsAndStuffConfig::readOnlyCanvasFocusEnabled,
                        QuestsAndStuffConfig::setReadOnlyCanvasFocusEnabled,
                        false,
                        false
                ),
                new SettingsOptionDescriptor(
                        "questEffectIcons",
                        "ui.questsandstuff.settings.quest_effect_icons",
                        "ui.questsandstuff.settings.quest_effect_icons_desc",
                        QuestsAndStuffConfig::questEffectIconsEnabled,
                        QuestsAndStuffConfig::setQuestEffectIconsEnabled,
                        false,
                        false
                ),
                new SettingsOptionDescriptor(
                        "canvasMiniNotifications",
                        "ui.questsandstuff.settings.canvas_mini_notifications",
                        "ui.questsandstuff.settings.canvas_mini_notifications_desc",
                        QuestsAndStuffConfig::canvasMiniNotificationsEnabled,
                        QuestsAndStuffConfig::setCanvasMiniNotificationsEnabled,
                        false,
                        false
                )
        );
    }

    private static void setFullScreenMode(TabletUiState state, boolean enabled) {
        QuestsAndStuffConfig.setFullScreenModeEnabled(enabled);
        TabletScreenManager.applyTabletLayoutMode(state);
    }
}
