package com.abo47.questsandstuff.client.tablet.quest.canvas.text;

import org.junit.jupiter.api.Test;

import com.abo47.questsandstuff.client.tablet.state.UiState;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TextStyleSessionTest {
    @Test
    void mainCanvasStyleCloseDoesNotTouchQuestDetailsStyleState() {
        UiState state = new UiState();
        CanvasTextStyleSession.openMainCanvas(state, "canvas:text");
        CanvasTextStyleSession.openQuestDetails(state, "desc:text");
        CanvasTextStyleSession.setFontSizeTarget(state, CanvasTextStyleSession.Surface.MAIN_CANVAS, "canvas:text");
        CanvasTextStyleSession.setFontSizeTarget(state, CanvasTextStyleSession.Surface.QUEST_DETAILS, "desc:text");

        CanvasTextStyleSession.closeMainCanvas(state);

        assertFalse(state.canvas.canvasTextMenuOpen);
        assertEquals("", state.canvas.canvasTextMenuTarget);
        assertEquals("", state.canvas.canvasTextFontSizeFieldTarget);
        assertTrue(state.questDetails.questDetailsTextStyleOpen);
        assertEquals("desc:text", state.questDetails.questDetailsTextStyleTarget);
        assertEquals("desc:text", state.questDetails.questDetailsTextFontSizeFieldTarget);
    }

    @Test
    void questDetailsStyleCloseResetsTargetFontFieldAndBounds() {
        UiState state = new UiState();
        CanvasTextStyleSession.openQuestDetails(state, "desc:text");
        CanvasTextStyleSession.setFontSizeTarget(state, CanvasTextStyleSession.Surface.QUEST_DETAILS, "desc:text");
        CanvasTextStyleSession.setQuestDetailsBounds(state, 1, 2, 3, 4);

        CanvasTextStyleSession.closeQuestDetails(state);

        assertFalse(state.questDetails.questDetailsTextStyleOpen);
        assertEquals("", state.questDetails.questDetailsTextStyleTarget);
        assertEquals("", state.questDetails.questDetailsTextFontSizeFieldTarget);
        assertEquals(0, state.questDetails.questDetailsTextStyleMenuX);
        assertEquals(0, state.questDetails.questDetailsTextStyleMenuY);
        assertEquals(0, state.questDetails.questDetailsTextStyleMenuW);
        assertEquals(0, state.questDetails.questDetailsTextStyleMenuH);
    }

    @Test
    void fontSizeFieldOpenAggregatesChapterCanvasAndQuestDetailsFields() {
        UiState state = new UiState();

        assertFalse(CanvasTextStyleSession.isAnyFontSizeFieldOpen(state));

        CanvasTextStyleSession.setFontSizeTarget(state, CanvasTextStyleSession.Surface.MAIN_CANVAS, "canvas:text");
        assertTrue(CanvasTextStyleSession.isAnyFontSizeFieldOpen(state));
        assertEquals("canvas:text", CanvasTextStyleSession.fontSizeTarget(state, CanvasTextStyleSession.Surface.MAIN_CANVAS));

        CanvasTextStyleSession.setFontSizeTarget(state, CanvasTextStyleSession.Surface.MAIN_CANVAS, "");
        CanvasTextStyleSession.setFontSizeTarget(state, CanvasTextStyleSession.Surface.QUEST_DETAILS, "desc:text");
        assertTrue(CanvasTextStyleSession.isAnyFontSizeFieldOpen(state));
        assertEquals("desc:text", CanvasTextStyleSession.fontSizeTarget(state, CanvasTextStyleSession.Surface.QUEST_DETAILS));
    }

    @Test
    void recentQuestDetailsInteractionUsesProvidedClockWindow() {
        UiState state = new UiState();
        state.questDetails.questDetailsTextStyleInteractionAtMs = 1_000L;

        assertTrue(CanvasTextStyleSession.recentlyHandledQuestDetailsClick(state, 1_200L, 350L));
        assertFalse(CanvasTextStyleSession.recentlyHandledQuestDetailsClick(state, 1_500L, 350L));
    }
}
