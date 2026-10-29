package com.abo47.questsandstuff.client.tablet.quest.canvas.render;

import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import com.abo47.questsandstuff.client.tablet.state.UiState;

public final class CanvasChapterSwitchAnimation {
    private CanvasChapterSwitchAnimation() {
    }

    public static void trackSelectedChapter(UiState state, String selectedChapter) {
        if (state == null) {
            return;
        }
        state.chapterPanel.canvasChapterSwitchGroup = normalize(selectedChapter);
        clear(state);
    }

    public static WidgetGroup wrap(UiState state, WidgetGroup content) {
        return content;
    }

    public static boolean finishIfDone(UiState state) {
        if (state == null || state.chapterPanel.canvasChapterSwitchAnimationStartMs <= 0L) {
            return false;
        }
        clear(state);
        return true;
    }

    private static void clear(UiState state) {
        state.chapterPanel.canvasChapterSwitchAnimationStartMs = 0L;
        state.chapterPanel.canvasChapterSwitchDirection = 1;
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}
