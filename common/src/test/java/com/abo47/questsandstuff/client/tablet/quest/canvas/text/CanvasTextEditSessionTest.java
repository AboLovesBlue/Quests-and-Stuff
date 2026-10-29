package com.abo47.questsandstuff.client.tablet.quest.canvas.text;

import org.junit.jupiter.api.Test;

import com.abo47.questsandstuff.client.tablet.state.UiState;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TextEditSessionTest {
    @Test
    void mainCanvasBeginClosesQuestDetailsDraftAndOpensCanvasStyleOwner() {
        UiState state = new UiState();
        CanvasTextEditSession.beginQuestDetails(state, "desc:text", "Description");

        CanvasTextEditSession.beginMainCanvas(state, "canvas:text", "Canvas");

        assertTrue(CanvasTextEditSession.isMainCanvasEditing(state));
        assertFalse(CanvasTextEditSession.isQuestDetailsEditing(state));
        assertEquals("", state.questDetails.questDetailsTextEditTarget);
        assertEquals("", state.questDetails.questDetailsTextEditDraft);
        assertEquals("canvas:text", state.canvas.canvasTextEditTarget);
        assertEquals("Canvas", state.canvas.canvasTextEditDraft);
        assertEquals(6, state.canvas.canvasTextEditCursor);
        assertEquals(6, state.canvas.canvasTextSelectionAnchor);
        assertTrue(state.canvas.canvasTextMenuOpen);
        assertEquals("canvas:text", state.canvas.canvasTextMenuTarget);
    }

    @Test
    void questDetailsBeginUsesSharedDraftCursorAndClosesCanvasStyleOwner() {
        UiState state = new UiState();
        CanvasTextStyleSession.openMainCanvas(state, "canvas:text");

        CanvasTextEditSession.beginQuestDetails(state, "desc:text", "Description");

        assertTrue(CanvasTextEditSession.isQuestDetailsEditing(state));
        assertFalse(CanvasTextEditSession.isMainCanvasEditing(state));
        assertEquals("desc:text", state.questDetails.questDetailsTextEditTarget);
        assertEquals("desc:text", state.canvas.canvasTextEditTarget);
        assertEquals("Description", state.questDetails.questDetailsTextEditDraft);
        assertEquals("Description", state.canvas.canvasTextEditDraft);
        assertEquals(11, state.canvas.canvasTextEditCursor);
        assertEquals(11, state.canvas.canvasTextSelectionAnchor);
        assertFalse(state.canvas.canvasTextMenuOpen);
        assertEquals("", state.canvas.canvasTextMenuTarget);
    }

    @Test
    void replacementNormalizesSelectionAndKeepsQuestDetailsDraftInSync() {
        UiState state = new UiState();
        CanvasTextEditSession.beginQuestDetails(state, "desc:text", "abcdef");
        CanvasTextEditSession.moveCursor(state, 2, false);
        CanvasTextEditSession.moveCursor(state, 5, true);

        CanvasTextEditSession.Replacement replacement = CanvasTextEditSession.insert(state, "XY");

        assertEquals(2, replacement.start());
        assertEquals(5, replacement.end());
        assertEquals("XY", replacement.value());
        assertEquals("abXYf", state.canvas.canvasTextEditDraft);
        assertEquals("abXYf", state.questDetails.questDetailsTextEditDraft);
        assertEquals(4, state.canvas.canvasTextEditCursor);
        assertEquals(4, state.canvas.canvasTextSelectionAnchor);
        assertFalse(CanvasTextEditSession.hasSelection(state));
    }

    @Test
    void insertCapsDraftAtSharedMaximum() {
        UiState state = new UiState();
        CanvasTextEditSession.beginMainCanvas(state, "text", "a".repeat(CanvasTextEditSession.MAX_DRAFT_LENGTH - 1));

        CanvasTextEditSession.Replacement replacement = CanvasTextEditSession.insert(state, "bcdef");

        assertEquals("b", replacement.value());
        assertEquals(CanvasTextEditSession.MAX_DRAFT_LENGTH, state.canvas.canvasTextEditDraft.length());
    }

    @Test
    void closeQuestDetailsClearsStaleQuestFieldsEvenWhenCommonOpenFlagIsOff() {
        UiState state = new UiState();
        state.canvas.canvasTextEditOpen = false;
        state.questDetails.questDetailsTextEditTarget = "stale";
        state.questDetails.questDetailsTextEditDraft = "draft";

        CanvasTextEditSession.closeQuestDetails(state, true);

        assertEquals("", state.questDetails.questDetailsTextEditTarget);
        assertEquals("", state.questDetails.questDetailsTextEditDraft);
    }
}
