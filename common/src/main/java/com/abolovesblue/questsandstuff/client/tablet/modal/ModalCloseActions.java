package com.abolovesblue.questsandstuff.client.tablet.modal;

import com.abolovesblue.questsandstuff.client.quest.sound.QuestSoundPreview;
import com.abolovesblue.questsandstuff.client.tablet.quest.canvas.transform.CanvasTransformSessions;
import com.abolovesblue.questsandstuff.client.tablet.state.UiState;
import com.abolovesblue.questsandstuff.client.tablet.ui.state.ModalState;

public final class ModalCloseActions {
    private ModalCloseActions() {
    }

    public static void closeAll(UiState state) {
        QuestSoundPreview.stop();
        clearTransformSessions(state);
        ModalState.closeAllModals(state);
    }

    public static void closeAllImmediately(UiState state) {
        QuestSoundPreview.stop();
        clearTransformSessions(state);
        ModalState.closeAllModalsImmediately(state);
    }

    public static void closeColorPicker(UiState state) {
        closeAll(state);
    }

    private static void clearTransformSessions(UiState state) {
        CanvasTransformSessions.clearMainCanvasSession(state);
        CanvasTransformSessions.clearQuestDetailsSession(state);
    }
}
