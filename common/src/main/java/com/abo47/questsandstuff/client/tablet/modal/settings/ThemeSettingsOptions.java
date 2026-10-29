package com.abo47.questsandstuff.client.tablet.modal.settings;

import java.util.ArrayList;
import java.util.List;

import com.abo47.questsandstuff.client.tablet.state.UiState;
import com.abo47.questsandstuff.client.tablet.theme.codec.UiThemeManager;

final class ThemeSettingsOptions {
    private ThemeSettingsOptions() {
    }

    static List<SettingsOptionDescriptor> options(UiState state) {
        List<SettingsOptionDescriptor> out = new ArrayList<>();
        for (UiThemeManager.ThemeInfo t : UiThemeManager.availableThemes()) {
            out.add(new SettingsOptionDescriptor(t.id(), t));
        }
        return out;
    }
}
