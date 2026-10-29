package com.abo47.questsandstuff.client.tablet.quest.canvas.interaction;

import java.util.List;
import java.util.Map;

import com.abo47.questsandstuff.client.tablet.quest.canvas.model.QuestCardLayout;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.geometry.CanvasElementGeometry;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.geometry.CanvasLayerKind;
import com.abo47.questsandstuff.client.tablet.quest.canvas.selection.CanvasSelectionActions;
import com.abo47.questsandstuff.client.tablet.state.UiState;
import com.abo47.questsandstuff.client.tablet.ui.state.StateQueries;
import com.abo47.questsandstuff.quest.model.canvas.CanvasExclusiveChoice;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;

final class CanvasRangeSelection {
    private CanvasRangeSelection() {
    }

    static void rangeSelect(
            UiState state,
            Map<String, QuestCardLayout> byQuestId,
            CanvasLayerKind anchorKind,
            String anchorId,
            CanvasLayerKind clickedKind,
            String clickedId
    ) {
        if (anchorId.isBlank()) {
            CanvasSelectionActions.clearCanvasSelection(state);
            selectSingle(state, clickedKind, clickedId);
            state.canvas.canvasSelectionRangeAnchorKind = clickedKind;
            state.canvas.canvasSelectionRangeAnchorId = clickedId;
            return;
        }
        if (anchorKind == null) {
            return;
        }
        String chapter = StateQueries.selectedChapterName(state);
        List<CanvasImageLayer> images = state.canvas.canvasImagesByChapter.getOrDefault(chapter, List.of());
        List<CanvasTextLayer> texts = state.canvas.canvasTextsByChapter.getOrDefault(chapter, List.of());
        List<CanvasExclusiveChoice> ecs = state.canvas.canvasExclusiveChoicesByChapter.getOrDefault(chapter, List.of());
        int[] anchorBounds = elementBounds(byQuestId, images, texts, ecs, anchorKind, anchorId);
        int[] clickBounds = elementBounds(byQuestId, images, texts, ecs, clickedKind, clickedId);
        if (anchorBounds == null || clickBounds == null) {
            return;
        }
        int minX = Math.min(anchorBounds[0], clickBounds[0]);
        int minY = Math.min(anchorBounds[1], clickBounds[1]);
        int maxX = Math.max(anchorBounds[2], clickBounds[2]);
        int maxY = Math.max(anchorBounds[3], clickBounds[3]);
        CanvasSelectionActions.clearCanvasSelection(state);
        for (QuestCardLayout card : byQuestId.values()) {
            if (card.x() < maxX && card.x() + card.width() > minX
                    && card.y() < maxY && card.y() + card.height() > minY) {
                state.canvas.canvasSelection.questIds().add(card.questId());
            }
        }
        for (CanvasImageLayer img : images) {
            int[] imgBounds = CanvasElementGeometry.logicalBoundsAtPivot(img.x(), img.y(), img.w(), img.h(), img.pivotX(), img.pivotY(), img.rotation());
            if (imgBounds[0] < maxX && imgBounds[2] > minX && imgBounds[1] < maxY && imgBounds[3] > minY) {
                state.canvas.canvasSelection.imageIds().add(img.id());
                state.canvas.canvasSelection.setPrimaryImageId(img.id());
            }
        }
        for (CanvasTextLayer txt : texts) {
            int[] txtBounds = CanvasElementGeometry.logicalBounds(txt.x(), txt.y(), txt.w(), txt.h(), txt.rotation());
            if (txtBounds[0] < maxX && txtBounds[2] > minX && txtBounds[1] < maxY && txtBounds[3] > minY) {
                state.canvas.canvasSelection.textIds().add(txt.id());
                state.canvas.canvasSelection.setPrimaryTextId(txt.id());
            }
        }
        for (CanvasExclusiveChoice ec : ecs) {
            int[] exclusiveChoiceBounds = CanvasElementGeometry.logicalBoundsAtPivot(ec.x(), ec.y(), ec.w(), ec.h(), ec.pivotX(), ec.pivotY(), ec.rotation());
            if (exclusiveChoiceBounds[0] < maxX && exclusiveChoiceBounds[2] > minX && exclusiveChoiceBounds[1] < maxY && exclusiveChoiceBounds[3] > minY) {
                state.canvas.canvasSelection.exclusiveChoiceIds().add(ec.id());
                state.canvas.canvasSelection.setPrimaryExclusiveChoiceId(ec.id());
            }
        }
    }

    private static void selectSingle(UiState state, CanvasLayerKind kind, String id) {
        switch (kind) {
            case QUEST -> state.canvas.canvasSelection.questIds().add(id);
            case IMAGE -> {
                state.canvas.canvasSelection.imageIds().add(id);
                state.canvas.canvasSelection.setPrimaryImageId(id);
            }
            case TEXT -> {
                state.canvas.canvasSelection.textIds().add(id);
                state.canvas.canvasSelection.setPrimaryTextId(id);
            }
            case EXCLUSIVE_CHOICE -> {
                state.canvas.canvasSelection.exclusiveChoiceIds().add(id);
                state.canvas.canvasSelection.setPrimaryExclusiveChoiceId(id);
            }
            case CONNECTION -> {
            }
        }
    }

    private static int[] elementBounds(
            Map<String, QuestCardLayout> byQuestId,
            List<CanvasImageLayer> images,
            List<CanvasTextLayer> texts,
            List<CanvasExclusiveChoice> ecs,
            CanvasLayerKind kind,
            String id
    ) {
        switch (kind) {
            case QUEST -> {
                QuestCardLayout card = byQuestId.get(id);
                if (card != null) {
                    return new int[]{card.x(), card.y(), card.x() + card.width(), card.y() + card.height()};
                }
            }
            case IMAGE -> {
                for (CanvasImageLayer img : images) {
                    if (img.id().equals(id)) {
                        return CanvasElementGeometry.logicalBoundsAtPivot(img.x(), img.y(), img.w(), img.h(), img.pivotX(), img.pivotY(), img.rotation());
                    }
                }
            }
            case TEXT -> {
                for (CanvasTextLayer txt : texts) {
                    if (txt.id().equals(id)) {
                        return CanvasElementGeometry.logicalBounds(txt.x(), txt.y(), txt.w(), txt.h(), txt.rotation());
                    }
                }
            }
            case EXCLUSIVE_CHOICE -> {
                for (CanvasExclusiveChoice ec : ecs) {
                    if (ec.id().equals(id)) {
                        return CanvasElementGeometry.logicalBoundsAtPivot(ec.x(), ec.y(), ec.w(), ec.h(), ec.pivotX(), ec.pivotY(), ec.rotation());
                    }
                }
            }
            case CONNECTION -> {
            }
        }
        return null;
    }
}
