package com.abo47.questsandstuff.client.tablet.modal;

import com.abo47.questsandstuff.client.tablet.state.UiState;
import com.abo47.questsandstuff.client.tablet.ui.state.StateQueries;

import static com.abo47.questsandstuff.client.tablet.theme.tokens.UiThemeTokens.*;
import static com.abo47.questsandstuff.util.MathUtils.clamp;

public final class ModalContextMenuPlacement {
    private static final int MARGIN = GRID_4;

    private ModalContextMenuPlacement() {
    }

    public static Placement fitToRootFromModal(UiState state, int x, int y, int w, int h, int modalW, int modalH) {
        int modalX = modalX(state, modalW);
        int modalY = modalY(state, modalH);
        int globalX = modalX + x;
        int globalY = modalY + y;
        int rootW = StateQueries.rootWidth(state);
        int rootH = StateQueries.rootHeight(state);
        return new Placement(
                clamp(globalX, MARGIN, Math.max(MARGIN, rootW - w - MARGIN)) - modalX,
                clamp(globalY, MARGIN, Math.max(MARGIN, rootH - h - MARGIN)) - modalY,
                w,
                h
        );
    }

    public static int localPointerX(UiState state, int modalW) {
        return state.modal.modalWindowLastPointerX - modalX(state, modalW);
    }

    public static int localPointerY(UiState state, int modalH) {
        return state.modal.modalWindowLastPointerY - modalY(state, modalH);
    }

    public static int modalX(UiState state, int modalW) {
        return (StateQueries.rootWidth(state) - modalW) / 2;
    }

    public static int modalY(UiState state, int modalH) {
        return (StateQueries.rootHeight(state) - modalH) / 2;
    }


    public record Placement(int x, int y, int w, int h) {
    }
}
