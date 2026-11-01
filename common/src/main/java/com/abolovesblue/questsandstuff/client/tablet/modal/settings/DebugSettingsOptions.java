package com.abolovesblue.questsandstuff.client.tablet.modal.settings;

import java.util.List;

import com.abolovesblue.questsandstuff.QuestsAndStuffConfig;
import com.abolovesblue.questsandstuff.client.tablet.state.UiState;

final class DebugSettingsOptions {
    private DebugSettingsOptions() {
    }

    static List<SettingsOptionDescriptor> options(UiState state) {
        return List.of(
                new SettingsOptionDescriptor(
                        "debugLogging",
                        "ui.questsandstuff.settings.debug_logging",
                        "ui.questsandstuff.settings.debug_logging_desc",
                        QuestsAndStuffConfig::debugLoggingEnabled,
                        QuestsAndStuffConfig::setDebugLoggingEnabled,
                        false,
                        false
                )
        );
    }
}
