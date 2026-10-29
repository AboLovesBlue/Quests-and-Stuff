package com.abo47.questsandstuff.client.tablet.quest.canvas.viewport;

import com.abo47.questsandstuff.client.tablet.state.UiState;

public final class CanvasViewportZoom {
    private CanvasViewportZoom() {
    }

    public static void zoomAt(UiState state, Runnable refresh, int localX, int localY, double wheelDelta) {
        CanvasCameraController.zoomAt(state, refresh, localX, localY, wheelDelta);
    }
}
