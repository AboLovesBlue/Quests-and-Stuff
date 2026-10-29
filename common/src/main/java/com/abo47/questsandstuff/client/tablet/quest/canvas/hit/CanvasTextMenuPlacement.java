package com.abo47.questsandstuff.client.tablet.quest.canvas.hit;

import com.abo47.questsandstuff.client.tablet.controls.TextStyleButtons;
import com.abo47.questsandstuff.client.tablet.quest.canvas.layer.CanvasLayerMutations;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.geometry.CanvasElementGeometry;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;

import static com.abo47.questsandstuff.util.MathUtils.clamp;

public final class CanvasTextMenuPlacement {
    private static final int TEXT_MENU_MARGIN = 4;

    private CanvasTextMenuPlacement() {
    }

    public static int[] canvasTextMenuBounds(TabletUiState state, CanvasTextLayer text, int viewportW, int viewportH, int toolCount) {
        int menuW = TextStyleButtons.menuWidthForAvailable(Math.max(1, viewportW) - TEXT_MENU_MARGIN * 2);
        int columns = TextStyleButtons.columnsForWidth(menuW);
        int menuH = TextStyleButtons.menuHeightForColumns(columns);
        int buttonW = TextStyleButtons.buttonWidth(menuW, columns);
        CanvasTextLayer drawText = CanvasLayerMutations.effectiveCanvasText(state, text);
        int[] textBounds = rotatedTextScreenBounds(state, drawText);
        MenuCandidate best = bestMenuCandidate(textBounds, viewportW, viewportH, menuW, menuH);
        int x = best.x();
        int y = best.y();
        return new int[]{x, y, menuW, menuH, buttonW, columns};
    }

    private static int[] rotatedTextScreenBounds(TabletUiState state, CanvasTextLayer text) {
        return CanvasElementGeometry.screenBounds(state, text.x(), text.y(), text.w(), text.h(), text.rotation());
    }

    private static MenuCandidate bestMenuCandidate(int[] avoidBounds, int viewportW, int viewportH, int menuW, int occupiedH) {
        int safeViewportW = Math.max(1, viewportW);
        int safeViewportH = Math.max(1, viewportH);
        int targetCenterX = (avoidBounds[0] + avoidBounds[2]) / 2;
        int targetCenterY = (avoidBounds[1] + avoidBounds[3]) / 2;
        int[][] rawCandidates = {
                {targetCenterX - menuW / 2, avoidBounds[1] - occupiedH - TextStyleButtons.TEXTBOX_FRAME_GAP},
                {targetCenterX - menuW / 2, avoidBounds[3] + TextStyleButtons.TEXTBOX_FRAME_GAP},
                {avoidBounds[0] - menuW - TextStyleButtons.TEXTBOX_FRAME_GAP, targetCenterY - occupiedH / 2},
                {avoidBounds[2] + TextStyleButtons.TEXTBOX_FRAME_GAP, targetCenterY - occupiedH / 2},
                {avoidBounds[0] - menuW - TextStyleButtons.TEXTBOX_FRAME_GAP, avoidBounds[1] - occupiedH - TextStyleButtons.TEXTBOX_FRAME_GAP},
                {avoidBounds[2] + TextStyleButtons.TEXTBOX_FRAME_GAP, avoidBounds[1] - occupiedH - TextStyleButtons.TEXTBOX_FRAME_GAP},
                {avoidBounds[0] - menuW - TextStyleButtons.TEXTBOX_FRAME_GAP, avoidBounds[3] + TextStyleButtons.TEXTBOX_FRAME_GAP},
                {avoidBounds[2] + TextStyleButtons.TEXTBOX_FRAME_GAP, avoidBounds[3] + TextStyleButtons.TEXTBOX_FRAME_GAP}
        };

        MenuCandidate best = null;
        for (int i = 0; i < rawCandidates.length; i++) {
            int rawX = rawCandidates[i][0];
            int rawY = rawCandidates[i][1];
            int x = clamp(rawX, TEXT_MENU_MARGIN, Math.max(TEXT_MENU_MARGIN, safeViewportW - menuW - TEXT_MENU_MARGIN));
            int y = clamp(rawY, TEXT_MENU_MARGIN, Math.max(TEXT_MENU_MARGIN, safeViewportH - occupiedH - TEXT_MENU_MARGIN));
            int overlap = overlapArea(x, y, menuW, occupiedH, avoidBounds);
            int overflow = overflowAmount(x, y, menuW, occupiedH, safeViewportW, safeViewportH);
            int shift = Math.abs(x - rawX) + Math.abs(y - rawY);
            long score = (long) overlap * 100_000L + (long) overflow * 1_000L + (long) shift * 10L + i;
            MenuCandidate candidate = new MenuCandidate(x, y, score);
            if (best == null || candidate.score() < best.score()) {
                best = candidate;
            }
        }
        return best == null ? new MenuCandidate(TEXT_MENU_MARGIN, TEXT_MENU_MARGIN, 0L) : best;
    }


    private static int overlapArea(int x, int y, int w, int h, int[] bounds) {
        int left = Math.max(x, bounds[0]);
        int top = Math.max(y, bounds[1]);
        int right = Math.min(x + w, bounds[2]);
        int bottom = Math.min(y + h, bounds[3]);
        if (right <= left || bottom <= top) {
            return 0;
        }
        return (right - left) * (bottom - top);
    }

    private static int overflowAmount(int x, int y, int w, int h, int viewportW, int viewportH) {
        int overflow = 0;
        overflow += Math.max(0, TEXT_MENU_MARGIN - x);
        overflow += Math.max(0, TEXT_MENU_MARGIN - y);
        overflow += Math.max(0, x + w + TEXT_MENU_MARGIN - viewportW);
        overflow += Math.max(0, y + h + TEXT_MENU_MARGIN - viewportH);
        return overflow;
    }

    private record MenuCandidate(int x, int y, long score) {
    }
}
