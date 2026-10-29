package com.abo47.questsandstuff.client.tablet.quest.canvas.interaction;

import java.util.Map;

import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasViewport;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.QuestCardLayout;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.CanvasRenderer;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.geometry.CanvasLayerKind;
import com.abo47.questsandstuff.client.tablet.quest.canvas.selection.CanvasBoxSelectionController;
import com.abo47.questsandstuff.client.tablet.quest.canvas.selection.CanvasSelectionActions;
import com.abo47.questsandstuff.client.tablet.quest.canvas.viewport.CanvasElementTransformController;
import com.abo47.questsandstuff.client.tablet.quest.canvas.viewport.CanvasInlineTextEditor;
import com.abo47.questsandstuff.client.tablet.quest.canvas.viewport.CanvasSelectionTransformController;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsWindow;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.ui.widget.TabletWidgetCoordinates;
import com.abo47.questsandstuff.quest.model.canvas.CanvasExclusiveChoice;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;

public final class CanvasSelectMoveClickActions {
    private CanvasSelectMoveClickActions() {
    }

    public static void handleSelectMove(
            CanvasViewport canvasViewport,
            TabletUiState state,
            Runnable refresher,
            Map<String, QuestCardLayout> byQuestId,
            CanvasInlineTextEditor textEditor,
            CanvasElementTransformController elementTransforms,
            CanvasSelectionTransformController selectionTransforms,
            int localX,
            int localY,
            int button,
            QuestCardLayout hit,
            CanvasImageLayer imageHit,
            CanvasTextLayer textHit,
            CanvasExclusiveChoice exclusiveChoiceHit
    ) {
        if (button == 0 && state.root.canEdit) {
            if (CanvasTransformHandleClicks.handleElementTransformStart(canvasViewport, state, refresher, byQuestId, textEditor, elementTransforms, selectionTransforms, localX, localY, imageHit, textHit, exclusiveChoiceHit)) {
                return;
            }
        }
        if (hit != null) {
            long now = System.currentTimeMillis();
            boolean doubleClick = hit.questId().equals(state.canvas.canvasLastClickedQuestId) && now - state.canvas.canvasLastQuestClickAtMs <= 350L;
            state.canvas.canvasLastClickedQuestId = hit.questId();
            state.canvas.canvasLastQuestClickAtMs = now;
            if (doubleClick && button == 0) {
                int viewportScreenX = TabletWidgetCoordinates.screenX(canvasViewport, state.canvas.canvasPanelX + state.canvas.canvasViewportX);
                int viewportScreenY = TabletWidgetCoordinates.screenY(canvasViewport, state.canvas.canvasPanelY + state.canvas.canvasViewportY);
                QuestDetailsWindow.openAtSource(
                        state,
                        hit.questId(),
                        viewportScreenX + hit.x(),
                        viewportScreenY + hit.y(),
                        hit.width(),
                        hit.height()
                );
                refresher.run();
                return;
            }
            if (canvasViewport.ctrlDown()) {
                if (!state.canvas.canvasSelection.questIds().add(hit.questId())) {
                    state.canvas.canvasSelection.questIds().remove(hit.questId());
                }
            } else if (canvasViewport.shiftDown()) {
                CanvasRangeSelection.rangeSelect(state, byQuestId, state.canvas.canvasSelectionRangeAnchorKind, state.canvas.canvasSelectionRangeAnchorId, CanvasLayerKind.QUEST, hit.questId());
            } else if (!state.canvas.canvasSelection.questIds().contains(hit.questId())) {
                CanvasSelectionActions.clearCanvasSelection(state);
                state.canvas.canvasSelection.questIds().add(hit.questId());
                state.canvas.canvasSelectionRangeAnchorKind = CanvasLayerKind.QUEST;
                state.canvas.canvasSelectionRangeAnchorId = hit.questId();
            }
            selectionTransforms.beginDrag(localX, localY, byQuestId);
            canvasViewport.beginSelectionDragPreview();
        } else {
            state.canvas.draggingSelection = false;
            state.canvas.resizingSelection = false;
            state.canvas.rotatingSelection = false;
            state.canvas.transientQuestPositions.clear();
            state.canvas.transientQuestScales.clear();
            CanvasSelectionActions.clearCanvasSelection(state);
            state.canvas.canvasSelectionRangeAnchorKind = null;
            state.canvas.canvasSelectionRangeAnchorId = "";
            CanvasBoxSelectionController.beginBoxSelection(state, false, localX, localY);
        }
        refresher.run();
    }
}
