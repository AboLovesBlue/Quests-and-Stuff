package com.abo47.questsandstuff.client.tablet.quest.details;

import com.abo47.questsandstuff.client.tablet.state.UiState;
import com.abo47.questsandstuff.client.tablet.ui.factory.UiFactory;
import com.abo47.questsandstuff.client.tablet.ui.widget.Layout;

import static com.abo47.questsandstuff.client.tablet.ui.factory.UiFactory.CANVAS_TOP_H_COMPACT;
import static com.abo47.questsandstuff.client.tablet.ui.factory.UiFactory.CHAPTER_W_MAX;
import static com.abo47.questsandstuff.client.tablet.ui.factory.UiFactory.CHAPTER_W_MIN;
import static com.abo47.questsandstuff.client.tablet.ui.factory.UiFactory.GAP;
import static com.abo47.questsandstuff.client.tablet.ui.factory.UiFactory.PANEL_W_MIN;

final class QuestDetailsWindowGeometry {
    private QuestDetailsWindowGeometry() {
    }

    static int leftPanelWidth(UiState state) {
        if (state == null) {
            return UiFactory.CHAPTER_W;
        }
        state.questDetails.questDetailsLeftPanelWidth = clampDetailsLeftWidth(state.questDetails.questDetailsLeftPanelWidth);
        return state.questDetails.questDetailsLeftPanelWidth;
    }

    static int clampDetailsLeftWidth(int width) {
        return Math.max(CHAPTER_W_MIN, Math.min(CHAPTER_W_MAX, width));
    }

    static int canvasPanelWidth(int leftW, int frameW) {
        return Math.max(PANEL_W_MIN, frameW - leftW - GAP);
    }

    static int[] mainCanvasViewport(int canvasW, int frameH) {
        return UiFactory.canvasViewportBounds(canvasW, frameH, CANVAS_TOP_H_COMPACT);
    }

    static int descriptionContentWidth(UiState state) {
        int layerW = Layout.rootWidth(state);
        int frameW = Math.min(QuestDetailsWindow.WINDOW_W, Math.max(64, layerW));
        int leftW = leftPanelWidth(state);
        int canvasW = canvasPanelWidth(leftW, frameW);
        int layerH = Layout.rootHeight(state);
        int frameH = Math.min(QuestDetailsWindow.WINDOW_H, Math.max(64, layerH));
        int[] viewport = mainCanvasViewport(canvasW, frameH);
        return Math.max(1, viewport[2] - 1);
    }
}
