package com.abo47.questsandstuff.client.tablet.quest.canvas.selection;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

import com.abo47.questsandstuff.client.sync.state.ClientQuestStateFacade;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuController;
import com.abo47.questsandstuff.client.tablet.quest.canvas.geometry.CanvasGeometry;
import com.abo47.questsandstuff.client.tablet.quest.canvas.layer.CanvasLayerMutations;
import com.abo47.questsandstuff.client.tablet.quest.canvas.transform.CanvasTransformSessions;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.CanvasPoint;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.QuestCardLayout;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.geometry.CanvasElementGeometry;
import com.abo47.questsandstuff.client.tablet.quest.editor.EditorCanvasCommandClient;
import com.abo47.questsandstuff.client.tablet.state.UiState;
import com.abo47.questsandstuff.client.tablet.ui.state.StateQueries;
import com.abo47.questsandstuff.quest.model.canvas.CanvasExclusiveChoice;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;

public final class CanvasSelectionActions {
    private CanvasSelectionActions() {
    }

    public static boolean isImageSelected(UiState state, String imageId) {
        return imageId != null && (imageId.equals(state.canvas.canvasSelection.primaryImageId()) || state.canvas.canvasSelection.imageIds().contains(imageId));
    }

    public static boolean isTextSelected(UiState state, String textId) {
        return textId != null && (textId.equals(state.canvas.canvasSelection.primaryTextId()) || state.canvas.canvasSelection.textIds().contains(textId));
    }

    public static boolean isExclusiveChoiceSelected(UiState state, String exclusiveChoiceId) {
        return exclusiveChoiceId != null && (exclusiveChoiceId.equals(state.canvas.canvasSelection.primaryExclusiveChoiceId()) || state.canvas.canvasSelection.exclusiveChoiceIds().contains(exclusiveChoiceId));
    }

    public static Set<String> selectedImageIds(UiState state) {
        Set<String> images = new LinkedHashSet<>(state.canvas.canvasSelection.imageIds());
        if (!state.canvas.canvasSelection.primaryImageId().isBlank()) {
            images.add(state.canvas.canvasSelection.primaryImageId());
        }
        return images;
    }

    public static Set<String> selectedTextIds(UiState state) {
        Set<String> texts = new LinkedHashSet<>(state.canvas.canvasSelection.textIds());
        if (!state.canvas.canvasSelection.primaryTextId().isBlank()) {
            texts.add(state.canvas.canvasSelection.primaryTextId());
        }
        return texts;
    }

    public static Set<String> selectedExclusiveChoiceIds(UiState state) {
        Set<String> ecs = new LinkedHashSet<>(state.canvas.canvasSelection.exclusiveChoiceIds());
        if (!state.canvas.canvasSelection.primaryExclusiveChoiceId().isBlank()) {
            ecs.add(state.canvas.canvasSelection.primaryExclusiveChoiceId());
        }
        return ecs;
    }

    public static int totalCanvasSelectionCount(UiState state) {
        return CanvasSelectionSet.current(state).size();
    }

    public static void clearCanvasSelection(UiState state) {
        state.canvas.canvasSelection.questIds().clear();
        state.canvas.canvasSelection.setPrimaryImageId("");
        state.canvas.canvasSelection.setPrimaryTextId("");
        state.canvas.canvasSelection.setPrimaryExclusiveChoiceId("");
        state.canvas.canvasSelection.imageIds().clear();
        state.canvas.canvasSelection.textIds().clear();
        state.canvas.canvasSelection.exclusiveChoiceIds().clear();
        CanvasTransformSessions.clearMainCanvasSession(state);
    }

    public static boolean alignSelectedToCanvasCenter(Player player, UiState state, boolean verticalCenterLine) {
        if (state == null || totalCanvasSelectionCount(state) <= 0) {
            return false;
        }
        String chapter = StateQueries.selectedChapterName(state);
        if (chapter.isBlank()) {
            return false;
        }

        SelectionBounds bounds = selectedBounds(state, chapter);
        if (!bounds.valid()) {
            return false;
        }
        int offset = centeredOffset(state, bounds, verticalCenterLine);
        if (offset == 0) {
            return false;
        }

        boolean changed = false;
        Map<String, CanvasPoint> questPositions = new LinkedHashMap<>();
        for (String questId : state.canvas.canvasSelection.questIds()) {
            if (!ClientQuestStateFacade.containsQuest(questId)) {
                continue;
            }
            CompoundTag tag = ClientQuestStateFacade.quest(questId);
            QuestCardLayout card = CanvasGeometry.layoutQuest(questId, tag, state, chapter);
            CanvasPoint aligned = movedQuestPosition(state, card, offset, verticalCenterLine);
            if (aligned.x != card.logicalX() || aligned.y != card.logicalY()) {
                questPositions.put(questId, aligned);
                changed = true;
            }
        }

        Set<String> imageIds = selectedImageIds(state);
        for (CanvasImageLayer image : state.canvas.canvasImagesByChapter.getOrDefault(chapter, List.of())) {
            if (!imageIds.contains(image.id())) {
                continue;
            }
            CanvasImageLayer aligned = movedImage(state, image, offset, verticalCenterLine);
            if (!aligned.equals(image)) {
                CanvasLayerMutations.putCanvasImage(state, chapter, aligned);
                changed = true;
            }
        }

        Set<String> textIds = selectedTextIds(state);
        for (CanvasTextLayer text : state.canvas.canvasTextsByChapter.getOrDefault(chapter, List.of())) {
            if (!textIds.contains(text.id())) {
                continue;
            }
            CanvasTextLayer aligned = movedText(state, text, offset, verticalCenterLine);
            if (!aligned.equals(text)) {
                CanvasLayerMutations.putCanvasText(state, chapter, aligned);
                changed = true;
            }
        }

        Set<String> exclusiveChoiceIds = selectedExclusiveChoiceIds(state);
        for (CanvasExclusiveChoice ec : state.canvas.canvasExclusiveChoicesByChapter.getOrDefault(chapter, List.of())) {
            if (!exclusiveChoiceIds.contains(ec.id())) {
                continue;
            }
            CanvasExclusiveChoice aligned = movedExclusiveChoice(state, ec, offset, verticalCenterLine);
            if (!aligned.equals(ec)) {
                CanvasLayerMutations.putCanvasExclusiveChoice(state, chapter, aligned);
                changed = true;
            }
        }

        if (!questPositions.isEmpty()) {
            EditorCanvasCommandClient.runCanvasMoveAction(player, state, questPositions);
        }
        if (changed) {
            ContextMenuController.clearDeleteConfirm(state);
        }
        return changed;
    }

    private static SelectionBounds selectedBounds(UiState state, String chapter) {
        SelectionBounds bounds = new SelectionBounds();
        for (String questId : state.canvas.canvasSelection.questIds()) {
            if (!ClientQuestStateFacade.containsQuest(questId)) {
                continue;
            }
            CompoundTag tag = ClientQuestStateFacade.quest(questId);
            QuestCardLayout card = CanvasGeometry.layoutQuest(questId, tag, state, chapter);
            bounds.include(card.visualLogicalX(), card.visualLogicalY(), card.logicalRight(), card.logicalBottom());
        }

        Set<String> imageIds = selectedImageIds(state);
        for (CanvasImageLayer image : state.canvas.canvasImagesByChapter.getOrDefault(chapter, List.of())) {
            if (!imageIds.contains(image.id())) {
                continue;
            }
            int[] box = CanvasElementGeometry.logicalBoundsAtPivot(image.x(), image.y(), image.w(), image.h(), image.pivotX(), image.pivotY(), image.rotation());
            bounds.include(box[0], box[1], box[2], box[3]);
        }

        Set<String> textIds = selectedTextIds(state);
        for (CanvasTextLayer text : state.canvas.canvasTextsByChapter.getOrDefault(chapter, List.of())) {
            if (!textIds.contains(text.id())) {
                continue;
            }
            int[] box = CanvasElementGeometry.logicalBounds(text.x(), text.y(), text.w(), text.h(), text.rotation());
            bounds.include(box[0], box[1], box[2], box[3]);
        }

        Set<String> exclusiveChoiceIds = selectedExclusiveChoiceIds(state);
        for (CanvasExclusiveChoice ec : state.canvas.canvasExclusiveChoicesByChapter.getOrDefault(chapter, List.of())) {
            if (!exclusiveChoiceIds.contains(ec.id())) {
                continue;
            }
            int[] box = CanvasElementGeometry.logicalBounds(ec.x(), ec.y(), ec.w(), ec.h(), ec.rotation());
            bounds.include(box[0], box[1], box[2], box[3]);
        }
        return bounds;
    }

    private static int centeredOffset(UiState state, SelectionBounds bounds, boolean verticalCenterLine) {
        double target = verticalCenterLine ? state.canvas.canvasContentW / 2.0 : state.canvas.canvasContentH / 2.0;
        double center = verticalCenterLine ? bounds.centerX() : bounds.centerY();
        int offset = (int) Math.round(target - center);
        if (state.canvas.gridSnapLocked) {
            int grid = Math.max(1, CanvasGeometry.gridSize(state));
            offset = Math.round((float) offset / (float) grid) * grid;
        }
        if (state.canvas.gridCanvasLocked) {
            int min = verticalCenterLine ? bounds.minX : bounds.minY;
            int max = verticalCenterLine ? bounds.maxX : bounds.maxY;
            int canvas = verticalCenterLine ? state.canvas.canvasContentW : state.canvas.canvasContentH;
            if (min + offset < 0) {
                offset = -min;
            }
            if (max + offset > canvas) {
                offset = canvas - max;
            }
        }
        return offset;
    }

    private static CanvasPoint movedQuestPosition(UiState state, QuestCardLayout card, int offset, boolean verticalCenterLine) {
        int x = verticalCenterLine ? card.logicalX() + offset : card.logicalX();
        int y = verticalCenterLine ? card.logicalY() : card.logicalY() + offset;
        return CanvasGeometry.clampAnchorToCanvas(state, x, y, card.slotLogicalWidth(), card.slotLogicalHeight());
    }

    private static CanvasImageLayer movedImage(UiState state, CanvasImageLayer image, int offset, boolean verticalCenterLine) {
        CanvasPoint clamped = movedElementPosition(state, image.x(), image.y(), image.w(), image.h(), image.pivotX(), image.pivotY(), image.rotation(), offset, verticalCenterLine);
        return image.moveTo(clamped.x, clamped.y);
    }

    private static CanvasTextLayer movedText(UiState state, CanvasTextLayer text, int offset, boolean verticalCenterLine) {
        CanvasPoint clamped = movedElementPosition(state, text.x(), text.y(), text.w(), text.h(), text.w() / 2, text.h() / 2, text.rotation(), offset, verticalCenterLine);
        return new CanvasTextLayer(text.id(), text.text(), clamped.x, clamped.y, text.w(), text.h(), text.rotation(), text.align(), text.style(), text.color(), text.fontSize(), text.spans());
    }

    private static CanvasExclusiveChoice movedExclusiveChoice(UiState state, CanvasExclusiveChoice ec, int offset, boolean verticalCenterLine) {
        CanvasPoint clamped = movedElementPosition(state, ec.x(), ec.y(), ec.w(), ec.h(), ec.pivotX(), ec.pivotY(), ec.rotation(), offset, verticalCenterLine);
        return new CanvasExclusiveChoice(ec.id(), clamped.x, clamped.y, ec.w(), ec.h(), ec.rotation(), ec.connectionQuestIds(), ec.prerequisiteQuestIds(), ec.background(), ec.connectionColors(), ec.connectionModes(), ec.connectionTextures(), ec.connectionTextureSpacings(), ec.hiddenConnections());
    }

    private static CanvasPoint movedElementPosition(UiState state, int x, int y, int width, int height, int pivotX, int pivotY, int rotation, int offset, boolean verticalCenterLine) {
        int movedX = verticalCenterLine ? x + offset : x;
        int movedY = verticalCenterLine ? y : y + offset;
        return CanvasGeometry.clampRotatedAnchorToCanvas(state, movedX, movedY, width, height, pivotX, pivotY, rotation);
    }

    private static final class SelectionBounds {
        private int minX = Integer.MAX_VALUE;
        private int minY = Integer.MAX_VALUE;
        private int maxX = Integer.MIN_VALUE;
        private int maxY = Integer.MIN_VALUE;

        private void include(int left, int top, int right, int bottom) {
            minX = Math.min(minX, left);
            minY = Math.min(minY, top);
            maxX = Math.max(maxX, right);
            maxY = Math.max(maxY, bottom);
        }

        private boolean valid() {
            return minX <= maxX && minY <= maxY;
        }

        private double centerX() {
            return (minX + maxX) / 2.0;
        }

        private double centerY() {
            return (minY + maxY) / 2.0;
        }
    }
}
