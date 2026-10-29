package com.abo47.questsandstuff.client.tablet.modal.settings;

import java.util.List;

import com.abo47.questsandstuff.QuestsAndStuffConfig;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;

final class DebugSettingsOptions {
    private DebugSettingsOptions() {
    }

    static List<SettingsOptionDescriptor> options(TabletUiState state) {
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
