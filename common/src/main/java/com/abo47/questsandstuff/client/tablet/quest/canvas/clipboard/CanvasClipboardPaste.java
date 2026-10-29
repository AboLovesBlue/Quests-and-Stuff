package com.abo47.questsandstuff.client.tablet.quest.canvas.clipboard;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.world.entity.player.Player;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.quest.canvas.geometry.CanvasGeometry;
import com.abo47.questsandstuff.client.tablet.quest.canvas.geometry.CanvasGridFitController;
import com.abo47.questsandstuff.client.tablet.quest.canvas.layer.CanvasLayerMutations;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasViewport;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.CanvasPoint;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.QuestCardLayout;
import com.abo47.questsandstuff.client.tablet.quest.canvas.selection.CanvasSelectionActions;
import com.abo47.questsandstuff.client.tablet.quest.editor.EditorCanvasCommandClient;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory;
import com.abo47.questsandstuff.client.tablet.ui.state.TabletStateQueries;
import com.abo47.questsandstuff.quest.model.canvas.CanvasExclusiveChoice;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;
import com.abo47.questsandstuff.util.naming.StableIdAllocator;

public final class CanvasClipboardPaste {
    private CanvasClipboardPaste() {
    }

    public static boolean pasteAtContext(Player player, TabletUiState state) {
        int anchorX = TabletUiFactory.snapToGrid(state, state.contextMenu.contextLogicalX);
        int anchorY = TabletUiFactory.snapToGrid(state, state.contextMenu.contextLogicalY);
        return pasteAt(player, state, anchorX, anchorY, "context");
    }

    public static boolean pasteNearSelectionOrViewportCenter(Player player, TabletUiState state, CanvasViewport canvasViewport) {
        CanvasPoint anchor = keyboardPasteAnchor(state, canvasViewport);
        return pasteAt(player, state, anchor.x, anchor.y, "shortcut");
    }

    private static boolean pasteAt(Player player, TabletUiState state, int anchorX, int anchorY, String source) {
        String chapter = TabletStateQueries.selectedChapterName(state);
        if (chapter.isBlank() || !CanvasClipboardCopy.hasClipboardContent(state)) {
            return false;
        }

        state.canvas.canvasSelection.questIds().clear();
        state.canvas.canvasSelection.setPrimaryImageId("");
        state.canvas.canvasSelection.imageIds().clear();
        state.canvas.canvasSelection.setPrimaryTextId("");
        state.canvas.canvasSelection.textIds().clear();
        state.canvas.canvasSelection.setPrimaryEcId("");
        state.canvas.canvasSelection.ecIds().clear();
        state.clipboard.canvasClipboard.clearPendingPastedLayers();

        boolean pastedElements = pasteCanvasElements(state, chapter, anchorX, anchorY);
        if (state.clipboard.canvasClipboard.hasQuestClipboard()) {
            EditorCanvasCommandClient.runCanvasPasteClipboardAction(player, chapter, anchorX, anchorY);
        }

        QuestsAndStuffMod.debugLog("[QnS:UI:Clipboard] paste requested source={} chapter={} quests={} images={} texts={} anchor={},{}",
                source, chapter, state.clipboard.canvasClipboard.hasQuestClipboard(), state.clipboard.canvasClipboard.imageCount(), state.clipboard.canvasClipboard.textCount(), anchorX, anchorY);
        return state.clipboard.canvasClipboard.hasQuestClipboard() || pastedElements;
    }

    private static boolean pasteCanvasElements(TabletUiState state, String chapter, int anchorX, int anchorY) {
        boolean pasted = false;
        Set<String> existingImageIds = existingImageIds(state, chapter);
        Set<String> existingTextIds = existingTextIds(state, chapter);
        Set<String> existingEcIds = existingExclusiveChoiceIds(state, chapter);
        int index = 0;
        for (CanvasImageLayer image : state.clipboard.canvasClipboard.imageLayers()) {
            String id = uniqueLayerId("img", existingImageIds);
            int x = TabletUiFactory.snapToGrid(state, anchorX + image.x() - state.clipboard.canvasClipboard.originX());
            int y = TabletUiFactory.snapToGrid(state, anchorY + image.y() - state.clipboard.canvasClipboard.originY());
            CanvasPoint clamped = CanvasGeometry.clampRotatedAnchorToCanvas(state, x, y, image.w(), image.h(), image.pivotX(), image.pivotY(), image.rotation());
            CanvasImageLayer duplicate = new CanvasImageLayer(id, image.asset(), clamped.x, clamped.y, image.w(), image.h(), image.rotation(), image.entityYaw(), image.entitySpinSpeed(), image.modelPitch(), image.pivotX(), image.pivotY());
            if (state.canvas.gridSnapLocked) {
                duplicate = CanvasGridFitController.fittedImage(state, duplicate);
            }
            CanvasLayerMutations.putCanvasImage(state, chapter, duplicate);
            state.canvas.canvasSelection.imageIds().add(id);
            state.canvas.canvasSelection.setPrimaryImageId(id);
            state.clipboard.canvasClipboard.recordPastedImage(id);
            pasted = true;
            index++;
        }
        for (CanvasTextLayer text : state.clipboard.canvasClipboard.textLayers()) {
            String id = uniqueLayerId("txt", existingTextIds);
            int x = TabletUiFactory.snapToGrid(state, anchorX + text.x() - state.clipboard.canvasClipboard.originX());
            int y = TabletUiFactory.snapToGrid(state, anchorY + text.y() - state.clipboard.canvasClipboard.originY());
            CanvasPoint clamped = CanvasGeometry.clampRotatedAnchorToCanvas(state, x, y, text.w(), text.h(), text.w() / 2, text.h() / 2, text.rotation());
            CanvasTextLayer duplicate = new CanvasTextLayer(id, text.text(), clamped.x, clamped.y, text.w(), text.h(), text.rotation(), text.align(), text.style(), text.color(), text.fontSize(), text.spans());
            if (state.canvas.gridSnapLocked) {
                duplicate = CanvasGridFitController.fittedText(state, duplicate);
            }
            CanvasLayerMutations.putCanvasText(state, chapter, duplicate);
            state.canvas.canvasSelection.textIds().add(id);
            state.canvas.canvasSelection.setPrimaryTextId(id);
            state.clipboard.canvasClipboard.recordPastedText(id);
            pasted = true;
            index++;
        }
        for (CanvasExclusiveChoice ec : state.clipboard.canvasClipboard.exclusiveChoiceLayers()) {
            String id = uniqueLayerId("ec", existingEcIds);
            int x = TabletUiFactory.snapToGrid(state, anchorX + ec.x() - state.clipboard.canvasClipboard.originX());
            int y = TabletUiFactory.snapToGrid(state, anchorY + ec.y() - state.clipboard.canvasClipboard.originY());
            CanvasPoint clamped = CanvasGeometry.clampRotatedAnchorToCanvas(state, x, y, ec.w(), ec.h(), ec.pivotX(), ec.pivotY(), ec.rotation());
            CanvasExclusiveChoice duplicate = new CanvasExclusiveChoice(id, clamped.x, clamped.y, ec.w(), ec.h(), ec.rotation(), ec.connectionQuestIds(), ec.prerequisiteQuestIds(), ec.background(), ec.connectionColors(), ec.connectionModes(), ec.connectionTextures(), ec.connectionTextureSpacings(), ec.hiddenConnections());
            if (state.canvas.gridSnapLocked) {
                duplicate = CanvasGridFitController.fittedExclusiveChoice(state, duplicate);
            }
            CanvasLayerMutations.putCanvasExclusiveChoice(state, chapter, duplicate);
            state.canvas.canvasSelection.ecIds().add(id);
            state.canvas.canvasSelection.setPrimaryEcId(id);
            state.clipboard.canvasClipboard.recordPastedExclusiveChoice(id);
            pasted = true;
            index++;
        }
        return pasted || index > 0;
    }

    private static Set<String> existingImageIds(TabletUiState state, String chapter) {
        Set<String> ids = new LinkedHashSet<>();
        for (CanvasImageLayer image : state.canvas.canvasImagesByChapter.getOrDefault(chapter, List.of())) {
            ids.add(image.id());
        }
        return ids;
    }

    private static Set<String> existingTextIds(TabletUiState state, String chapter) {
        Set<String> ids = new LinkedHashSet<>();
        for (CanvasTextLayer text : state.canvas.canvasTextsByChapter.getOrDefault(chapter, List.of())) {
            ids.add(text.id());
        }
        return ids;
    }

    private static Set<String> existingExclusiveChoiceIds(TabletUiState state, String chapter) {
        Set<String> ids = new LinkedHashSet<>();
        for (CanvasExclusiveChoice ec : state.canvas.canvasExclusiveChoicesByChapter.getOrDefault(chapter, List.of())) {
            ids.add(ec.id());
        }
        return ids;
    }

    private static String uniqueLayerId(String prefix, Set<String> existingIds) {
        String id = StableIdAllocator.nextId(prefix, existingIds);
        existingIds.add(id);
        return id;
    }

    private static CanvasPoint keyboardPasteAnchor(TabletUiState state, CanvasViewport canvasViewport) {
        int step = CanvasGeometry.gridSize(state);
        CanvasPoint selectionAnchor = selectedCanvasAnchor(state, canvasViewport, step);
        if (selectionAnchor != null) {
            return selectionAnchor;
        }
        CanvasPoint center = CanvasGeometry.anchorForScreenVisualCenter(
                state,
                Math.max(0, canvasViewport.getSizeWidth() / 2),
                Math.max(0, canvasViewport.getSizeHeight() / 2),
                1.0f
        );
        return new CanvasPoint(
                TabletUiFactory.snapToGrid(state, center.x),
                TabletUiFactory.snapToGrid(state, center.y)
        );
    }

    private static CanvasPoint selectedCanvasAnchor(TabletUiState state, CanvasViewport canvasViewport, int step) {
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        for (String questId : state.canvas.canvasSelection.questIds()) {
            QuestCardLayout card = canvasViewport.cardLookup().get(questId);
            if (card == null) {
                continue;
            }
            minX = Math.min(minX, card.logicalX());
            minY = Math.min(minY, card.logicalY());
        }
        String chapter = TabletStateQueries.selectedChapterName(state);
        Set<String> imageIds = CanvasSelectionActions.selectedImageIds(state);
        for (CanvasImageLayer image : state.canvas.canvasImagesByChapter.getOrDefault(chapter, List.of())) {
            if (imageIds.contains(image.id())) {
                minX = Math.min(minX, image.x());
                minY = Math.min(minY, image.y());
            }
        }
        Set<String> textIds = CanvasSelectionActions.selectedTextIds(state);
        for (CanvasTextLayer text : state.canvas.canvasTextsByChapter.getOrDefault(chapter, List.of())) {
            if (textIds.contains(text.id())) {
                minX = Math.min(minX, text.x());
                minY = Math.min(minY, text.y());
            }
        }
        Set<String> ecIds = CanvasSelectionActions.selectedEcIds(state);
        for (CanvasExclusiveChoice ec : state.canvas.canvasExclusiveChoicesByChapter.getOrDefault(chapter, List.of())) {
            if (ecIds.contains(ec.id())) {
                minX = Math.min(minX, ec.x());
                minY = Math.min(minY, ec.y());
            }
        }
        if (minX == Integer.MAX_VALUE || minY == Integer.MAX_VALUE) {
            return null;
        }
        return new CanvasPoint(
                TabletUiFactory.snapToGrid(state, minX + step),
                TabletUiFactory.snapToGrid(state, minY + step)
        );
    }
}
