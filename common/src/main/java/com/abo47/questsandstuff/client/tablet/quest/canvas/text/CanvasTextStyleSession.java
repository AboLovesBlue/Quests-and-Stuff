package com.abo47.questsandstuff.client.tablet.quest.canvas.text;

import com.abo47.questsandstuff.client.tablet.state.UiState;

public final class CanvasTextStyleSession {
    public enum Surface {
        MAIN_CANVAS,
        QUEST_DETAILS
    }

    private CanvasTextStyleSession() {
    }

    public static void openMainCanvas(UiState state, String textId) {
        state.canvas.canvasTextMenuOpen = true;
        state.canvas.canvasTextMenuTarget = safe(textId);
    }

    public static void closeMainCanvas(UiState state) {
        state.canvas.canvasTextMenuOpen = false;
        state.canvas.canvasTextMenuTarget = "";
        state.canvas.canvasTextFontSizeFieldTarget = "";
    }

    public static void openQuestDetails(UiState state, String textId) {
        state.questDetails.questDetailsTextStyleOpen = true;
        state.questDetails.questDetailsTextStyleTarget = safe(textId);
    }

    public static void closeQuestDetails(UiState state) {
        state.questDetails.questDetailsTextStyleOpen = false;
        state.questDetails.questDetailsTextStyleTarget = "";
        state.questDetails.questDetailsTextFontSizeFieldTarget = "";
        resetQuestDetailsBounds(state);
    }

    public static boolean questDetailsOpenOrEditingFont(UiState state) {
        return state.questDetails.questDetailsTextStyleOpen || !state.questDetails.questDetailsTextFontSizeFieldTarget.isBlank();
    }

    public static void markQuestDetailsInteraction(UiState state) {
        state.questDetails.questDetailsTextStyleInteractionAtMs = System.currentTimeMillis();
    }

    public static void setFontSizeTarget(UiState state, Surface surface, String target) {
        if (surface == Surface.QUEST_DETAILS) {
            state.questDetails.questDetailsTextFontSizeFieldTarget = safe(target);
        } else {
            state.canvas.canvasTextFontSizeFieldTarget = safe(target);
        }
    }

    public static String fontSizeTarget(UiState state, Surface surface) {
        return surface == Surface.QUEST_DETAILS ? state.questDetails.questDetailsTextFontSizeFieldTarget : state.canvas.canvasTextFontSizeFieldTarget;
    }

    public static boolean isAnyFontSizeFieldOpen(UiState state) {
        return !state.chapterPanel.chapterTextFontSizeFieldTarget.isBlank()
                || !state.canvas.canvasTextFontSizeFieldTarget.isBlank()
                || !state.questDetails.questDetailsTextFontSizeFieldTarget.isBlank();
    }

    public static void setQuestDetailsBounds(UiState state, int x, int y, int width, int height) {
        state.questDetails.questDetailsTextStyleMenuX = x;
        state.questDetails.questDetailsTextStyleMenuY = y;
        state.questDetails.questDetailsTextStyleMenuW = width;
        state.questDetails.questDetailsTextStyleMenuH = height;
    }

    public static void resetQuestDetailsBounds(UiState state) {
        state.questDetails.questDetailsTextStyleMenuX = 0;
        state.questDetails.questDetailsTextStyleMenuY = 0;
        state.questDetails.questDetailsTextStyleMenuW = 0;
        state.questDetails.questDetailsTextStyleMenuH = 0;
    }

    public static boolean recentlyHandledQuestDetailsClick(UiState state, long nowMillis, long windowMillis) {
        long handledAt = state.questDetails.questDetailsTextStyleInteractionAtMs;
        return handledAt > 0L && nowMillis - handledAt <= windowMillis;
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
