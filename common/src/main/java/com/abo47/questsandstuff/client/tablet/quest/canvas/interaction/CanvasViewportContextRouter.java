package com.abo47.questsandstuff.client.tablet.quest.canvas.interaction;

import java.util.List;
import java.util.Map;

import com.abo47.questsandstuff.client.tablet.quest.canvas.geometry.CanvasGeometry;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.CanvasRenderer;
import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuController;
import com.abo47.questsandstuff.client.tablet.entity.motion.EntityMotionEditor;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.CanvasPoint;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.ConnectionHit;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.QuestCardLayout;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.geometry.CanvasLayerOrdering;
import com.abo47.questsandstuff.client.tablet.quest.canvas.selection.CanvasSelectionActions;
import com.abo47.questsandstuff.client.tablet.state.UiState;
import com.abo47.questsandstuff.client.tablet.ui.factory.UiFactory;
import com.abo47.questsandstuff.client.tablet.ui.state.StateQueries;
import com.abo47.questsandstuff.quest.model.canvas.CanvasExclusiveChoice;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;

public final class CanvasViewportContextRouter {
    private CanvasViewportContextRouter() {
    }

    public static void closeContextMenu(UiState state) {
        ContextMenuController.close(state);
    }

    public static void openContextMenu(
            UiState state,
            Runnable refresher,
            List<QuestCardLayout> cards,
            Map<String, QuestCardLayout> byQuestId,
            int localX,
            int localY,
            QuestCardLayout hit,
            CanvasImageLayer imageHit,
            CanvasTextLayer textHit,
            CanvasExclusiveChoice exclusiveChoiceHit
    ) {
        ConnectionHit connectionHit = UiFactory.hitTestConnection(state, cards, byQuestId, localX, localY);
        CanvasPoint anchor = CanvasGeometry.anchorForScreenVisualCenter(state, localX, localY, 1.0f);
        int logicalX = anchor.x;
        int logicalY = anchor.y;
        int pointerLogicalX = CanvasGeometry.screenToNearestLogicalX(state, localX);
        int pointerLogicalY = CanvasGeometry.screenToNearestLogicalY(state, localY);
        ContextMenuController.openCanvas(state, localX, localY, logicalX, logicalY, pointerLogicalX, pointerLogicalY);
        EntityMotionEditor.close(state);
        if (CanvasSelectionActions.totalCanvasSelectionCount(state) > 1 && CanvasRenderer.isSelectionBoundsHit(state, localX, localY)) {
            ContextMenuController.targetSelection(state);
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context menu open target=selection count={}", CanvasSelectionActions.totalCanvasSelectionCount(state));
        } else if (connectionHit != null && connectionAboveHits(state, connectionHit, hit, imageHit, textHit, exclusiveChoiceHit)) {
            ContextMenuController.targetConnection(state, connectionHit.sourceQuestId(), connectionHit.targetQuestId());
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context menu open target=connection source={} target={}", state.contextMenu.contextConnectionSource, state.contextMenu.contextConnectionTarget);
        } else if (exclusiveChoiceHit != null) {
            ContextMenuController.targetExclusiveChoice(state, exclusiveChoiceHit.id());
            state.canvas.canvasSelection.setPrimaryExclusiveChoiceId(exclusiveChoiceHit.id());
            state.canvas.canvasSelection.exclusiveChoiceIds().clear();
            state.canvas.canvasSelection.exclusiveChoiceIds().add(exclusiveChoiceHit.id());
            state.canvas.canvasSelection.setPrimaryImageId("");
            state.canvas.canvasSelection.imageIds().clear();
            state.canvas.canvasSelection.setPrimaryTextId("");
            state.canvas.canvasSelection.textIds().clear();
            state.canvas.canvasSelection.questIds().clear();
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context menu open target=exclusive_choice id={}", exclusiveChoiceHit.id());
        } else if (textHit != null) {
            ContextMenuController.targetText(state, textHit.id());
            state.canvas.canvasSelection.setPrimaryTextId(textHit.id());
            state.canvas.canvasSelection.textIds().clear();
            state.canvas.canvasSelection.textIds().add(textHit.id());
            state.canvas.canvasSelection.setPrimaryImageId("");
            state.canvas.canvasSelection.imageIds().clear();
            state.canvas.canvasSelection.setPrimaryExclusiveChoiceId("");
            state.canvas.canvasSelection.exclusiveChoiceIds().clear();
            state.canvas.canvasSelection.questIds().clear();
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context menu open target=text id={}", textHit.id());
        } else if (imageHit != null) {
            ContextMenuController.targetImage(state, imageHit.id());
            state.canvas.canvasSelection.setPrimaryImageId(imageHit.id());
            state.canvas.canvasSelection.imageIds().clear();
            state.canvas.canvasSelection.imageIds().add(imageHit.id());
            state.canvas.canvasSelection.setPrimaryTextId("");
            state.canvas.canvasSelection.textIds().clear();
            state.canvas.canvasSelection.setPrimaryExclusiveChoiceId("");
            state.canvas.canvasSelection.exclusiveChoiceIds().clear();
            state.canvas.canvasSelection.questIds().clear();
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context menu open target=image id={}", imageHit.id());
        } else if (hit != null) {
            ContextMenuController.targetQuest(state, hit.questId());
            if (!state.canvas.canvasSelection.questIds().contains(hit.questId())) {
                state.canvas.canvasSelection.questIds().clear();
                state.canvas.canvasSelection.questIds().add(hit.questId());
            }
            state.canvas.canvasSelection.setPrimaryExclusiveChoiceId("");
            state.canvas.canvasSelection.exclusiveChoiceIds().clear();
            state.canvas.canvasSelection.setPrimaryImageId("");
            state.canvas.canvasSelection.imageIds().clear();
            state.canvas.canvasSelection.setPrimaryTextId("");
            state.canvas.canvasSelection.textIds().clear();
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context menu open target=quest quest={}", state.contextMenu.contextQuestId);
        } else if (connectionHit != null) {
            ContextMenuController.targetConnection(state, connectionHit.sourceQuestId(), connectionHit.targetQuestId());
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context menu open target=connection source={} target={}", state.contextMenu.contextConnectionSource, state.contextMenu.contextConnectionTarget);
        } else {
            ContextMenuController.targetCanvas(state);
            CanvasSelectionActions.clearCanvasSelection(state);
            state.canvas.connectSourceQuestId = "";
            state.canvas.connectSourceQuestIds.clear();
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context menu open target=canvas logicalX={} logicalY={}", logicalX, logicalY);
        }
        refresher.run();
    }

    private static boolean connectionAboveHits(UiState state, ConnectionHit connectionHit, QuestCardLayout questHit, CanvasImageLayer imageHit, CanvasTextLayer textHit, CanvasExclusiveChoice exclusiveChoiceHit) {
        if (state == null || connectionHit == null) {
            return false;
        }
        String chapter = StateQueries.selectedChapterName(state);
        List<String> order = state.canvas.canvasLayerOrderByChapter.getOrDefault(chapter, List.of());
        String connectionLayerKey = CanvasLayerOrdering.connectionKey(CanvasRenderer.connectionKey(connectionHit.sourceQuestId(), connectionHit.targetQuestId()));
        int connectionIndex = order.indexOf(connectionLayerKey);
        if (connectionIndex < 0) {
            return false;
        }
        return above(order, connectionIndex, questHit == null ? "" : CanvasLayerOrdering.questKey(questHit.questId()))
                && above(order, connectionIndex, imageHit == null ? "" : CanvasLayerOrdering.imageKey(imageHit.id()))
                && above(order, connectionIndex, textHit == null ? "" : CanvasLayerOrdering.textKey(textHit.id()))
                && above(order, connectionIndex, exclusiveChoiceHit == null ? "" : CanvasLayerOrdering.exclusiveChoiceKey(exclusiveChoiceHit.id()));
    }

    private static boolean above(List<String> order, int connectionIndex, String otherKey) {
        if (otherKey == null || otherKey.isBlank()) {
            return true;
        }
        int otherIndex = order.indexOf(otherKey);
            return otherIndex < 0 || connectionIndex > otherIndex;
    }
}
