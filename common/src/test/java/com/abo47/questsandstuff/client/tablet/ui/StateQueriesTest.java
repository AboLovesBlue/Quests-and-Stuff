package com.abo47.questsandstuff.client.tablet.ui;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.abo47.questsandstuff.client.tablet.state.UiState;
import com.abo47.questsandstuff.client.tablet.ui.state.StateQueries;
import com.abo47.questsandstuff.client.tablet.ui.widget.Layout;
import com.abo47.questsandstuff.util.naming.QuestIdentity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StateQueriesTest {
    @Test
    void rootDimensionsUseLayoutDefaultsUntilStateCarriesARealSize() {
        UiState state = new UiState();

        assertEquals(Layout.ROOT_W, StateQueries.rootWidth(state));
        assertEquals(Layout.ROOT_H, StateQueries.rootHeight(state));

        state.root.tabletRootWidth = 640;
        state.root.tabletRootHeight = 360;

        assertEquals(640, StateQueries.rootWidth(state));
        assertEquals(360, StateQueries.rootHeight(state));
    }

    @Test
    void selectedChapterNameUsesSanitizedEditableSelection() {
        UiState state = new UiState();
        state.root.canEdit = true;
        state.root.selectedChapter = "  main\nchapter with a very very very very very very long suffix  ";

        assertEquals(QuestIdentity.uiChapterName(state.root.selectedChapter), StateQueries.sanitizeChapterName(state.root.selectedChapter));
        assertEquals("main chapter with a very very very very ", StateQueries.selectedChapterName(state));
    }

    @Test
    void selectedQuestHelpersExposeSingleAndSnapshotSelection() {
        UiState state = new UiState();

        assertEquals("", StateQueries.singleSelectedQuestId(state));
        assertFalse(StateQueries.hasSelectedQuests(state));

        state.canvas.canvasSelection.questIds().add("quest_a");
        assertEquals("quest_a", StateQueries.singleSelectedQuestId(state));
        assertTrue(StateQueries.hasSelectedQuests(state));

        List<String> snapshot = StateQueries.selectedQuestIdSnapshot(state);
        state.canvas.canvasSelection.questIds().add("quest_b");

        assertEquals(List.of("quest_a"), snapshot);
        assertEquals("", StateQueries.singleSelectedQuestId(state));
    }
}
