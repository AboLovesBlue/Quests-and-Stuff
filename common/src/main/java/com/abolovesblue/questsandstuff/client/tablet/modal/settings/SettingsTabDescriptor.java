package com.abolovesblue.questsandstuff.client.tablet.modal.settings;

import java.util.List;
import java.util.function.Function;

import com.abolovesblue.questsandstuff.client.tablet.state.UiState;

public record SettingsTabDescriptor(
        int id,
        String logName,
        String labelKey,
        Function<UiState, List<SettingsOptionDescriptor>> optionProvider
) {
    public List<SettingsOptionDescriptor> options(UiState state) {
        return optionProvider.apply(state);
    }
}
