package com.abolovesblue.questsandstuff.client.tablet.quest.canvas.render;

import java.util.Set;

import com.abolovesblue.questsandstuff.client.sync.state.ClientQuestStateFacade;
import com.abolovesblue.questsandstuff.client.tablet.contextmenu.ContextMenuController;
import com.abolovesblue.questsandstuff.client.tablet.state.UiState;
import com.abolovesblue.questsandstuff.client.tablet.ui.state.StateQueries;

final class CanvasRenderStateController {
    private CanvasRenderStateController() {
    }

    static String prepareRebuild(UiState state) {
        String selectedChapter = StateQueries.selectedChapterName(state);
        state.canvas.canvasZoom = CanvasRenderer.clampZoom(state.canvas.canvasZoom);
        return selectedChapter;
    }

    static void setContentBounds(UiState state, int contentX, int contentY, int contentW, int contentH) {
        state.canvas.canvasContentX = contentX;
        state.canvas.canvasContentY = contentY;
        state.canvas.canvasContentW = contentW;
        state.canvas.canvasContentH = contentH;
    }

    static void pruneStaleInteractiveState(UiState state, Set<String> visibleQuestIds) {
        state.canvas.canvasSelection.questIds().retainAll(visibleQuestIds);
        if (!state.canvas.connectSourceQuestId.isBlank() && !ClientQuestStateFacade.containsQuest(state.canvas.connectSourceQuestId)) {
            state.canvas.connectSourceQuestId = "";
        }
        state.canvas.connectSourceQuestIds.removeIf(questId -> !ClientQuestStateFacade.containsQuest(questId));
    }

    static void closeEditOnlyStateWhenReadOnly(UiState state) {
        if (state.root.canEdit) {
            return;
        }
        ContextMenuController.close(state);
        state.canvas.boxSelecting = false;
    }
}
