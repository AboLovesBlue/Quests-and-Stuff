package com.abo47.questsandstuff.client.tablet.ui.state;

import java.util.List;

import com.abo47.questsandstuff.client.sync.state.ClientQuestStateFacade;
import com.abo47.questsandstuff.client.tablet.state.UiState;
import com.abo47.questsandstuff.client.tablet.ui.widget.Layout;
import com.abo47.questsandstuff.util.naming.QuestIdentity;

public final class StateQueries {
    private StateQueries() {
    }

    public static int rootWidth(UiState state) {
        return Layout.rootWidth(state);
    }

    public static int rootHeight(UiState state) {
        return Layout.rootHeight(state);
    }

    public static String selectedChapterName(UiState state) {
        String selected = sanitizeChapterName(state == null ? "" : state.root.selectedChapter);
        if (state == null || state.root.canEdit || selected.isBlank() || ClientQuestStateFacade.chapterOpenablePreview(selected)) {
            return selected;
        }
        for (String chapter : ClientQuestStateFacade.selectableChapterOrder(false)) {
            String sanitized = sanitizeChapterName(chapter);
            if (!sanitized.isBlank()) {
                return sanitized;
            }
        }
        return "";
    }

    public static String singleSelectedQuestId(UiState state) {
        if (state == null || state.canvas.canvasSelection.questIds().size() != 1) {
            return "";
        }
        String questId = state.canvas.canvasSelection.questIds().iterator().next();
        return questId == null ? "" : questId;
    }

    public static List<String> selectedQuestIdSnapshot(UiState state) {
        return state == null ? List.of() : List.copyOf(state.canvas.canvasSelection.questIds());
    }

    public static boolean hasSelectedQuests(UiState state) {
        return state != null && !state.canvas.canvasSelection.questIds().isEmpty();
    }

    public static String sanitizeChapterName(String value) {
        return QuestIdentity.uiChapterName(value);
    }
}
