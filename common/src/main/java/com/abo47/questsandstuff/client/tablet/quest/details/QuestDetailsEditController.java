package com.abo47.questsandstuff.client.tablet.quest.details;

import com.abo47.questsandstuff.client.tablet.state.UiState;

import static com.abo47.questsandstuff.client.tablet.ui.factory.UiFactory.persistUiState;

public final class QuestDetailsEditController {
    private QuestDetailsEditController() {
    }

    public static boolean editorAvailable(UiState state) {
        return state != null && state.root.editorAvailable;
    }

    public static boolean canEdit(UiState state) {
        return editorAvailable(state) && state.questDetails.questDetailsEditMode;
    }

    public static boolean toggle(UiState state) {
        if (!editorAvailable(state)) {
            return false;
        }
        state.questDetails.questDetailsEditMode = !state.questDetails.questDetailsEditMode;
        persistUiState(state);
        return true;
    }
}
