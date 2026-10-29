package com.abo47.questsandstuff.client.tablet.quest.canvas.interaction;

import java.util.Map;

import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasViewport;
import com.abo47.questsandstuff.client.tablet.quest.canvas.hit.CanvasHitTester;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.QuestCardLayout;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.CanvasRenderer;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.gizmo.CanvasTransformGizmo;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.gizmo.CanvasTransformGizmoAxis;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.gizmo.CanvasTransformGizmoHit;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.geometry.CanvasLayerKind;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.geometry.CanvasTransformMode;
import com.abo47.questsandstuff.client.tablet.quest.canvas.selection.CanvasBoxSelectionController;
import com.abo47.questsandstuff.client.tablet.quest.canvas.selection.CanvasSelectionActions;
import com.abo47.questsandstuff.client.tablet.quest.canvas.viewport.CanvasElementTransformController;
import com.abo47.questsandstuff.client.tablet.quest.canvas.viewport.CanvasInlineTextEditor;
import com.abo47.questsandstuff.client.tablet.quest.canvas.viewport.CanvasSelectionTransformController;
import com.abo47.questsandstuff.client.tablet.state.UiState;
import com.abo47.questsandstuff.quest.model.canvas.CanvasExclusiveChoice;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;

final class CanvasTransformHandleClicks {
    private CanvasTransformHandleClicks() {
    }

    static boolean handleElementTransformStart(
            CanvasViewport canvasViewport,
            UiState state,
            Runnable refresher,
            Map<String, QuestCardLayout> byQuestId,
            CanvasInlineTextEditor textEditor,
            CanvasElementTransformController elementTransforms,
            CanvasSelectionTransformController selectionTransforms,
            int localX,
            int localY,
            CanvasImageLayer imageHit,
            CanvasTextLayer textHit,
            CanvasExclusiveChoice exclusiveChoiceHit
    ) {
        int selectionCount = CanvasSelectionActions.totalCanvasSelectionCount(state);
        if (selectionCount > 1) {
            if (CanvasRenderer.isSelectionRotateHandleHit(state, localX, localY)) {
                selectionTransforms.beginRotate(localX, localY, byQuestId);
                refresher.run();
                return true;
            }
            if (CanvasRenderer.isSelectionResizeHandleHit(state, localX, localY)) {
                selectionTransforms.beginResize(localX, localY, byQuestId);
                refresher.run();
                return true;
            }
            if (!canvasViewport.shiftDown() && !canvasViewport.ctrlDown() && CanvasRenderer.isSelectionBoundsHit(state, localX, localY)) {
                selectionTransforms.beginDrag(localX, localY, byQuestId);
                canvasViewport.beginSelectionDragPreview();
                refresher.run();
                return true;
            }
        }
        boolean textTransformHandleHit = textHit != null
                && (CanvasRenderer.isCanvasTextResizeHandleHit(state, textHit, localX, localY)
                || CanvasRenderer.isCanvasTextRotateHandleHit(state, textHit, localX, localY));
        boolean imageTransformHandleHit = imageHit != null && imageTransformHit(canvasViewport, state, imageHit, localX, localY);
        boolean exclusiveChoiceTransformHandleHit = exclusiveChoiceHit != null
                && (CanvasRenderer.isCanvasExclusiveChoiceResizeHandleHit(state, exclusiveChoiceHit, localX, localY)
                || CanvasRenderer.isCanvasExclusiveChoiceRotateHandleHit(state, exclusiveChoiceHit, localX, localY));
        if (textTransformHandleHit) {
            state.canvas.canvasTextLastClickId = "";
            elementTransforms.beginTextTransform(textHit, localX, localY);
            refresher.run();
            return true;
        }
        if (imageTransformHandleHit) {
            elementTransforms.beginImageTransform(imageHit, localX, localY);
            refresher.run();
            return true;
        }
        if (exclusiveChoiceTransformHandleHit) {
            elementTransforms.beginExclusiveChoiceTransform(exclusiveChoiceHit, localX, localY);
            refresher.run();
            return true;
        }
        if (state.canvas.canvasSelection.questIds().size() == 1) {
            QuestCardLayout questCard = byQuestId.get(state.canvas.canvasSelection.questIds().iterator().next());
            if (questCard != null && CanvasHitTester.isCanvasQuestResizeHandleHit(state, questCard, localX, localY)) {
                selectionTransforms.beginResize(localX, localY, byQuestId);
                refresher.run();
                return true;
            }
        }
        if (textHit != null) {
            if (canvasViewport.ctrlDown()) {
                CanvasBoxSelectionController.toggleCanvasTextSelection(state, textHit.id());
                refresher.run();
                return true;
            }
            if (canvasViewport.shiftDown()) {
                CanvasRangeSelection.rangeSelect(state, byQuestId, state.canvas.canvasSelectionRangeAnchorKind, state.canvas.canvasSelectionRangeAnchorId, CanvasLayerKind.TEXT, textHit.id());
                refresher.run();
                return true;
            }
            long now = System.currentTimeMillis();
            boolean doubleClick = textHit.id().equals(state.canvas.canvasTextLastClickId) && now - state.canvas.canvasTextLastClickAtMs <= 350L;
            state.canvas.canvasTextLastClickId = textHit.id();
            state.canvas.canvasTextLastClickAtMs = now;
            if (doubleClick) {
                textEditor.begin(textHit);
                refresher.run();
                return true;
            }
            elementTransforms.beginTextTransform(textHit, localX, localY);
            refresher.run();
            return true;
        }
        if (imageHit != null) {
            if (canvasViewport.ctrlDown()) {
                CanvasBoxSelectionController.toggleCanvasImageSelection(state, imageHit.id());
                refresher.run();
                return true;
            }
            if (canvasViewport.shiftDown()) {
                CanvasRangeSelection.rangeSelect(state, byQuestId, state.canvas.canvasSelectionRangeAnchorKind, state.canvas.canvasSelectionRangeAnchorId, CanvasLayerKind.IMAGE, imageHit.id());
                refresher.run();
                return true;
            }
            elementTransforms.beginImageTransform(imageHit, localX, localY);
            refresher.run();
            return true;
        }
        if (exclusiveChoiceHit != null) {
            if (canvasViewport.ctrlDown()) {
                CanvasBoxSelectionController.toggleCanvasExclusiveChoiceSelection(state, exclusiveChoiceHit.id());
                refresher.run();
                return true;
            }
            if (canvasViewport.shiftDown()) {
                CanvasRangeSelection.rangeSelect(state, byQuestId, state.canvas.canvasSelectionRangeAnchorKind, state.canvas.canvasSelectionRangeAnchorId, CanvasLayerKind.EXCLUSIVE_CHOICE, exclusiveChoiceHit.id());
                refresher.run();
                return true;
            }
            elementTransforms.beginExclusiveChoiceTransform(exclusiveChoiceHit, localX, localY);
            refresher.run();
            return true;
        }
        return false;
    }

    private static boolean imageTransformHit(CanvasViewport canvasViewport, UiState state, CanvasImageLayer image, int localX, int localY) {
        if (!CanvasTransformGizmoAxis.supports(image.asset())) {
            return CanvasRenderer.isCanvasImageResizeHandleHit(state, image, localX, localY)
                    || CanvasRenderer.isCanvasImageRotateHandleHit(state, image, localX, localY);
        }
        if (canvasViewport.shiftDown()
                && CanvasTransformGizmoAxis.activeMode(state) == CanvasTransformMode.RESIZE
                && CanvasTransformGizmoHit.boundsHitAtPivot(state, image.x(), image.y(), image.w(), image.h(), image.pivotX(), image.pivotY(), image.rotation(), localX, localY)) {
            return true;
        }
        CanvasTransformMode hitMode = CanvasTransformGizmoHit.modeAtPivot(state, image.x(), image.y(), image.w(), image.h(), image.pivotX(), image.pivotY(), image.rotation(), image.entityYaw(), image.modelPitch(), localX, localY);
        return hitMode != null
                || (canvasViewport.shiftDown()
                && CanvasTransformGizmoAxis.activeMode(state) == CanvasTransformMode.MOVE
                && CanvasTransformGizmoHit.boundsHitAtPivot(state, image.x(), image.y(), image.w(), image.h(), image.pivotX(), image.pivotY(), image.rotation(), localX, localY));
    }
}
