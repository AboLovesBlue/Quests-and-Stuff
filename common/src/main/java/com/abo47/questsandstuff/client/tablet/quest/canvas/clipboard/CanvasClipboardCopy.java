package com.abo47.questsandstuff.client.tablet.quest.canvas.clipboard;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.sync.state.ClientQuestStateFacade;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuController;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasViewport;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.CanvasPoint;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.QuestCardLayout;
import com.abo47.questsandstuff.client.tablet.quest.canvas.overlay.CanvasMiniNotificationController;
import com.abo47.questsandstuff.client.tablet.quest.canvas.selection.CanvasSelectionActions;
import com.abo47.questsandstuff.client.tablet.quest.editor.EditorCanvasCommandClient;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory;
import com.abo47.questsandstuff.client.tablet.ui.state.TabletStateQueries;
import com.abo47.questsandstuff.quest.model.canvas.CanvasExclusiveChoice;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;

public final class CanvasClipboardCopy {
    private CanvasClipboardCopy() {
    }

    public static boolean canCopyContext(CanvasViewport canvasViewport, TabletUiState state) {
        return canCopy(canvasViewport, state, contextSelection(state));
    }

    public static boolean copyContextToClipboard(CanvasViewport canvasViewport, TabletUiState state) {
        boolean copied = copyToClipboard(canvasViewport, state, contextSelection(state), "context");
        if (copied) {
            CanvasMiniNotificationController.show(state, "ui.questsandstuff.canvas_notifications.copied", state.contextMenu.contextLastClickX, state.contextMenu.contextLastClickY);
        }
        return copied;
    }

    public static boolean copySelectionToClipboard(CanvasViewport canvasViewport, TabletUiState state) {
        boolean copied = copyToClipboard(canvasViewport, state, currentSelection(state), "shortcut");
        if (copied) {
            CanvasMiniNotificationController.showAtPointer(state, canvasViewport, "ui.questsandstuff.canvas_notifications.copied");
        }
        return copied;
    }

    public static boolean hasClipboardContent(TabletUiState state) {
        return state != null && state.clipboard.canvasClipboard.hasContent();
    }

    private static boolean canCopy(CanvasViewport canvasViewport, TabletUiState state, ClipboardSelection selection) {
        if (canvasViewport == null || state == null || selection.isEmpty()) {
            return false;
        }
        Map<String, QuestCardLayout> byQuestId = canvasViewport.cardLookup();
        for (String questId : selection.questIds()) {
            if (byQuestId.containsKey(questId) || ClientQuestStateFacade.containsQuest(questId)) {
                return true;
            }
        }
        String chapter = TabletStateQueries.selectedChapterName(state);
        for (CanvasImageLayer image : state.canvas.canvasImagesByChapter.getOrDefault(chapter, List.of())) {
            if (selection.imageIds().contains(image.id())) {
                return true;
            }
        }
        for (CanvasTextLayer text : state.canvas.canvasTextsByChapter.getOrDefault(chapter, List.of())) {
            if (selection.textIds().contains(text.id())) {
                return true;
            }
        }
        for (CanvasExclusiveChoice ec : state.canvas.canvasExclusiveChoicesByChapter.getOrDefault(chapter, List.of())) {
            if (selection.ecIds().contains(ec.id())) {
                return true;
            }
        }
        return false;
    }

    private static ClipboardSelection contextSelection(TabletUiState state) {
        ClipboardSelection current = currentSelection(state);
        return switch (state.contextMenu.contextMenuTarget) {
            case SELECTION -> current;
            case QUEST -> {
                if (current.questIds().contains(state.contextMenu.contextQuestId) && !current.isEmpty()) {
                    yield current;
                }
                yield ClipboardSelection.ofQuest(state.contextMenu.contextQuestId);
            }
            case IMAGE -> {
                if (current.imageIds().contains(state.contextMenu.contextCanvasImageId) && !current.isEmpty()) {
                    yield current;
                }
                yield ClipboardSelection.ofImage(state.contextMenu.contextCanvasImageId);
            }
            case TEXT -> {
                if (current.textIds().contains(state.contextMenu.contextCanvasTextId) && !current.isEmpty()) {
                    yield current;
                }
                yield ClipboardSelection.ofText(state.contextMenu.contextCanvasTextId);
            }
            case EXCLUSIVE_CHOICE -> {
                if (current.ecIds().contains(state.contextMenu.contextCanvasExclusiveChoiceId) && !current.isEmpty()) {
                    yield current;
                }
                yield ClipboardSelection.ofExclusiveChoice(state.contextMenu.contextCanvasExclusiveChoiceId);
            }
            default -> ClipboardSelection.empty();
        };
    }

    private static ClipboardSelection currentSelection(TabletUiState state) {
        return new ClipboardSelection(
                new LinkedHashSet<>(state.canvas.canvasSelection.questIds()),
                CanvasSelectionActions.selectedImageIds(state),
                CanvasSelectionActions.selectedTextIds(state),
                CanvasSelectionActions.selectedEcIds(state)
        );
    }

    private static boolean copyToClipboard(CanvasViewport canvasViewport, TabletUiState state, ClipboardSelection selection, String source) {
        if (state == null || canvasViewport == null || selection.isEmpty()) {
            return false;
        }
        String chapter = TabletStateQueries.selectedChapterName(state);
        Set<String> copiedQuestIds = copiedQuestIds(canvasViewport, selection.questIds());
        List<CanvasImageLayer> copiedImages = copiedImages(state, chapter, selection.imageIds());
        List<CanvasTextLayer> copiedTexts = copiedTexts(state, chapter, selection.textIds());
        List<CanvasExclusiveChoice> copiedEcs = copiedExclusiveChoices(state, chapter, selection.ecIds());
        if (copiedQuestIds.isEmpty() && copiedImages.isEmpty() && copiedTexts.isEmpty() && copiedEcs.isEmpty()) {
            QuestsAndStuffMod.debugLog("[QnS:UI:Clipboard] copy skipped source={} chapter={} reason=no_canvas_selection", source, chapter);
            return false;
        }

        CanvasPoint origin = clipboardOrigin(state, canvasViewport, copiedQuestIds, copiedImages, copiedTexts, copiedEcs);
        state.clipboard.canvasClipboard.store(!copiedQuestIds.isEmpty(), copiedImages, copiedTexts, copiedEcs, origin.x, origin.y);
        ContextMenuController.clearDeleteConfirm(state);

        if (!copiedQuestIds.isEmpty()) {
            EditorCanvasCommandClient.runCanvasCopyAction(canvasViewport.player(), chapter, copiedQuestIds);
        }
        QuestsAndStuffMod.debugLog("[QnS:UI:Clipboard] copy stored source={} chapter={} quests={} images={} texts={} ecs={} origin={},{}",
                source, chapter, copiedQuestIds.size(), copiedImages.size(), copiedTexts.size(), copiedEcs.size(), origin.x, origin.y);
        return true;
    }

    private static Set<String> copiedQuestIds(CanvasViewport canvasViewport, Set<String> ids) {
        Set<String> copiedIds = new LinkedHashSet<>();
        Map<String, QuestCardLayout> byQuestId = canvasViewport.cardLookup();
        for (String questId : ids) {
            String normalized = questId == null ? "" : questId.trim();
            if (normalized.isBlank()) {
                continue;
            }
            if (byQuestId.containsKey(normalized) || ClientQuestStateFacade.containsQuest(normalized)) {
                copiedIds.add(normalized);
            }
        }
        return copiedIds;
    }

    private static List<CanvasImageLayer> copiedImages(TabletUiState state, String chapter, Set<String> imageIds) {
        if (imageIds.isEmpty()) {
            return List.of();
        }
        List<CanvasImageLayer> copied = new ArrayList<>();
        for (CanvasImageLayer image : state.canvas.canvasImagesByChapter.getOrDefault(chapter, List.of())) {
            if (imageIds.contains(image.id())) {
                copied.add(image);
            }
        }
        return copied;
    }

    private static List<CanvasTextLayer> copiedTexts(TabletUiState state, String chapter, Set<String> textIds) {
        if (textIds.isEmpty()) {
            return List.of();
        }
        List<CanvasTextLayer> copied = new ArrayList<>();
        for (CanvasTextLayer text : state.canvas.canvasTextsByChapter.getOrDefault(chapter, List.of())) {
            if (textIds.contains(text.id())) {
                copied.add(text);
            }
        }
        return copied;
    }

    private static List<CanvasExclusiveChoice> copiedExclusiveChoices(TabletUiState state, String chapter, Set<String> ecIds) {
        if (ecIds.isEmpty()) {
            return List.of();
        }
        List<CanvasExclusiveChoice> copied = new ArrayList<>();
        for (CanvasExclusiveChoice ec : state.canvas.canvasExclusiveChoicesByChapter.getOrDefault(chapter, List.of())) {
            if (ecIds.contains(ec.id())) {
                copied.add(ec);
            }
        }
        return copied;
    }

    private static CanvasPoint clipboardOrigin(
            TabletUiState state,
            CanvasViewport canvasViewport,
            Set<String> questIds,
            List<CanvasImageLayer> images,
            List<CanvasTextLayer> texts,
            List<CanvasExclusiveChoice> ecs
    ) {
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        for (String questId : questIds) {
            QuestCardLayout card = canvasViewport.cardLookup().get(questId);
            if (card == null) {
                continue;
            }
            minX = Math.min(minX, card.logicalX());
            minY = Math.min(minY, card.logicalY());
        }
        if (minX == Integer.MAX_VALUE) {
            for (CanvasImageLayer image : images) {
                minX = Math.min(minX, image.x());
                minY = Math.min(minY, image.y());
            }
            for (CanvasTextLayer text : texts) {
                minX = Math.min(minX, text.x());
                minY = Math.min(minY, text.y());
            }
            for (CanvasExclusiveChoice ec : ecs) {
                minX = Math.min(minX, ec.x());
                minY = Math.min(minY, ec.y());
            }
        }
        if (minX == Integer.MAX_VALUE) {
            return new CanvasPoint(TabletUiFactory.snapToGrid(state, state.contextMenu.contextLogicalX), TabletUiFactory.snapToGrid(state, state.contextMenu.contextLogicalY));
        }
        return new CanvasPoint(minX, minY);
    }

    private record ClipboardSelection(Set<String> questIds, Set<String> imageIds, Set<String> textIds, Set<String> ecIds) {
        private static ClipboardSelection empty() {
            return new ClipboardSelection(Set.of(), Set.of(), Set.of(), Set.of());
        }

        private static ClipboardSelection ofQuest(String questId) {
            return new ClipboardSelection(single(questId), Set.of(), Set.of(), Set.of());
        }

        private static ClipboardSelection ofImage(String imageId) {
            return new ClipboardSelection(Set.of(), single(imageId), Set.of(), Set.of());
        }

        private static ClipboardSelection ofText(String textId) {
            return new ClipboardSelection(Set.of(), Set.of(), single(textId), Set.of());
        }

        private static ClipboardSelection ofExclusiveChoice(String ecId) {
            return new ClipboardSelection(Set.of(), Set.of(), Set.of(), single(ecId));
        }

        private boolean isEmpty() {
            return questIds.isEmpty() && imageIds.isEmpty() && textIds.isEmpty() && ecIds.isEmpty();
        }

        private static Set<String> single(String value) {
            String normalized = value == null ? "" : value.trim();
            return normalized.isBlank() ? Set.of() : Set.of(normalized);
        }
    }
}
