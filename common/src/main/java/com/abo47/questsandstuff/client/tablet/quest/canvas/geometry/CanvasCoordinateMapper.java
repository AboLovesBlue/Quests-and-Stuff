package com.abo47.questsandstuff.client.tablet.quest.canvas.geometry;

import com.abo47.questsandstuff.client.tablet.quest.canvas.render.CanvasRenderer;
import com.abo47.questsandstuff.client.tablet.state.UiState;

final class CanvasCoordinateMapper {
    private CanvasCoordinateMapper() {
    }

    static int screenX(UiState state, double logicalX) {
        return state.canvas.canvasContentX + state.canvas.canvasOffsetX + Math.round((float) (logicalX * zoom(state)));
    }

    static int screenY(UiState state, double logicalY) {
        return state.canvas.canvasContentY + state.canvas.canvasOffsetY + Math.round((float) (logicalY * zoom(state)));
    }

    static int screenWidth(UiState state, int logicalLeft, int logicalRight) {
        return Math.max(1, screenX(state, logicalRight) - screenX(state, logicalLeft));
    }

    static int screenHeight(UiState state, int logicalTop, int logicalBottom) {
        return Math.max(1, screenY(state, logicalBottom) - screenY(state, logicalTop));
    }

    static int screenSpan(UiState state, int logicalSize) {
        return Math.max(1, Math.round(Math.max(1, logicalSize) * zoom(state)));
    }

    static double screenToLogicalX(UiState state, int screenX) {
        return (screenX - state.canvas.canvasContentX - state.canvas.canvasOffsetX) / zoom(state);
    }

    static double screenToLogicalY(UiState state, int screenY) {
        return (screenY - state.canvas.canvasContentY - state.canvas.canvasOffsetY) / zoom(state);
    }

    static int screenToNearestLogicalX(UiState state, int screenX) {
        return (int) Math.round(screenToLogicalX(state, screenX));
    }

    static int screenToNearestLogicalY(UiState state, int screenY) {
        return (int) Math.round(screenToLogicalY(state, screenY));
    }

    static float zoom(UiState state) {
        return CanvasRenderer.clampZoom(state.canvas.canvasZoom);
    }
}
