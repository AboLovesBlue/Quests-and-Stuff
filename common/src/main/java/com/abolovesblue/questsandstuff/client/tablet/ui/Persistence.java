package com.abolovesblue.questsandstuff.client.tablet.ui;

import com.abolovesblue.questsandstuff.client.tablet.state.UiState;
import com.abolovesblue.questsandstuff.client.tablet.state.UiStatePersistence;

public final class Persistence {
    private Persistence() {
    }

    public static void readPersistedUiState(UiState state) {
        UiStatePersistence.read(state);
    }

    public static boolean readPersistedEditMode() {
        return UiStatePersistence.readEditMode();
    }

    public static void persistUiState(UiState state) {
        UiStatePersistence.write(state);
    }

    public static void persistEditMode(boolean enabled) {
        UiStatePersistence.writeEditMode(enabled);
    }

    public static void readPersistedSkinState(UiState state) {
        UiStatePersistence.readSkinState(state);
    }

    public static void persistSkinState(UiState state) {
        UiStatePersistence.writeSkinState(state);
    }
}
