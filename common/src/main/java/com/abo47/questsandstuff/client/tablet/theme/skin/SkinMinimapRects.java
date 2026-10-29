package com.abo47.questsandstuff.client.tablet.theme.skin;

import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasViewport;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;

final class SkinMinimapRects {
    private static final int RECT_FIELDS = 4;
    private static final int X = 0;
    private static final int Y = 1;
    private static final int W = 2;
    private static final int H = 3;

    private SkinMinimapRects() {
    }

    static int[] bodyRect(TabletUiState state, CanvasViewport viewport) {
        return rect(state, viewport, state.canvas.minimapPanelX, state.canvas.minimapPanelY,
                state.canvas.minimapPanelW, state.canvas.minimapPanelH);
    }

    static int[] toggleRect(TabletUiState state, CanvasViewport viewport) {
        return rect(state, viewport, state.canvas.minimapToggleX, state.canvas.minimapToggleY,
                state.canvas.minimapToggleW, state.canvas.minimapToggleH);
    }

    private static int[] rect(TabletUiState state, CanvasViewport viewport, int offsetX, int offsetY, int w, int h) {
        if (viewport == null || w <= 0 || h <= 0) return null;
        int[] out = new int[RECT_FIELDS];
        out[X] = viewport.getPositionX() + offsetX;
        out[Y] = viewport.getPositionY() + offsetY;
        out[W] = w;
        out[H] = h;
        return out;
    }
}
