package com.abo47.questsandstuff.client.tablet.root;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuController;
import com.abo47.questsandstuff.client.tablet.quest.canvas.text.CanvasTextStyleSession;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsWindow;
import com.abo47.questsandstuff.client.tablet.quest.editor.EditorQuestCommandClient;
import com.abo47.questsandstuff.client.tablet.state.UiState;
import com.abo47.questsandstuff.client.tablet.ui.factory.UiFactory;
import com.abo47.questsandstuff.client.tablet.ui.widget.WidgetCoordinates;

final class RootDismissals {
    private RootDismissals() {
    }

    static ClickDismissState capture(RootWidget root, UiState state, double mouseX, double mouseY, Runnable refresh) {
        int rootX = WidgetCoordinates.rootX(root);
        int rootY = WidgetCoordinates.rootY(root);
        boolean chapterMenuWasOpen = state.chapterPanel.chapterMenuOpen;
        boolean contextMenuWasOpen = state.contextMenu.contextMenuOpen;
        boolean questDetailsWasOpen = state.questDetails.questDetailsOpen;
        boolean chapterTextMenuWasOpen = state.chapterPanel.chapterTextMenuOpen;
        boolean canvasTextMenuWasOpen = state.canvas.canvasTextMenuOpen;
        boolean chapterMenuHit = chapterMenuWasOpen && RootHitTest.isChapterMenuHit(state, rootX, rootY, mouseX, mouseY, root.resolvePlayer(), refresh);
        boolean contextMenuHit = contextMenuWasOpen && RootHitTest.isCanvasContextMenuHit(state, rootX, rootY, mouseX, mouseY);
        boolean chapterTextMenuHit = chapterTextMenuWasOpen && RootHitTest.isChapterTextMenuHit(state, rootX, rootY, mouseX, mouseY);
        boolean canvasTextMenuHit = canvasTextMenuWasOpen && RootHitTest.isCanvasTextMenuHit(state, rootX, rootY, mouseX, mouseY);
        boolean assetContextHit = state.pickers.assetContextOpen && RootHitTest.isAssetContextHit(state, rootX, rootY, mouseX, mouseY);
        return new ClickDismissState(chapterMenuWasOpen, contextMenuWasOpen, questDetailsWasOpen, chapterTextMenuWasOpen, canvasTextMenuWasOpen, chapterMenuHit, contextMenuHit, chapterTextMenuHit, canvasTextMenuHit, assetContextHit);
    }

    static boolean handleAfterClick(RootWidget root, UiState state, Runnable refresher, ClickDismissState clickState, double mouseX, double mouseY, int button, boolean handled) {
        int rootX = WidgetCoordinates.rootX(root);
        int rootY = WidgetCoordinates.rootY(root);
        if (clickState.questDetailsWasOpen()) {
            return true;
        }
        boolean chapterMenuOpenedByThisClick = state.chapterPanel.chapterMenuOpenedByClick;
        state.chapterPanel.chapterSelectionJustChanged = false;
        state.chapterPanel.chapterMenuOpenedByClick = false;

        if (button != 0 && button != 1) {
            return handled;
        }
        boolean changed = false;
        if (UiFactory.DRAFT_CHAPTER.equals(state.canvas.pendingChapterRename)
                && !RootHitTest.isInsideChapterPanel(state, rootX, rootY, mouseX, mouseY)) {
            state.canvas.pendingChapterRename = "";
            changed = true;
        }
        if (!state.questDetails.pendingQuestTitleChangeId.isBlank()
                && !QuestDetailsWindow.isInside(state, mouseX, mouseY)
                && !RootHitTest.isInsideCanvasViewport(state, rootX, rootY, mouseX, mouseY)) {
            EditorQuestCommandClient.cancelQuestTitleChange(state);
            changed = true;
        }
        if (clickState.chapterMenuWasOpen() && state.chapterPanel.chapterMenuOpen && !chapterMenuOpenedByThisClick && !clickState.chapterMenuHit() && !state.chapterPanel.modeEditorOpen) {
            state.chapterPanel.chapterMenuOpen = false;
            ContextMenuController.clearDeleteConfirm(state);
            changed = true;
        }
        if (clickState.contextMenuWasOpen() && state.contextMenu.contextMenuOpen && !clickState.contextMenuHit() && !state.contextMenu.modeEditorOpen) {
            ContextMenuController.close(state);
            changed = true;
        }
        if (clickState.chapterTextMenuWasOpen() && state.chapterPanel.chapterTextMenuOpen && !clickState.chapterTextMenuHit()) {
            state.chapterPanel.chapterTextMenuOpen = false;
            state.chapterPanel.chapterTextMenuTarget = "";
            state.chapterPanel.chapterTextFontSizeFieldTarget = "";
            changed = true;
        }
        if (clickState.canvasTextMenuWasOpen() && state.canvas.canvasTextMenuOpen && !clickState.canvasTextMenuHit()) {
            CanvasTextStyleSession.closeMainCanvas(state);
            changed = true;
        }
        if (state.pickers.assetContextOpen && !clickState.assetContextHit()) {
            state.pickers.assetContextOpen = false;
            state.pickers.assetRenameOpen = false;
            ContextMenuController.clearDeleteConfirm(state);
            changed = true;
        }
        boolean insideCanvasViewport = RootHitTest.isInsideCanvasViewport(state, rootX, rootY, mouseX, mouseY);
        boolean insideChapterPanel = RootHitTest.isInsideChapterPanel(state, rootX, rootY, mouseX, mouseY);
        if (!insideCanvasViewport
                && !RootHitTest.isToolsMenuHit(state, rootX, rootY, mouseX, mouseY)
                && !state.canvas.canvasSelection.questIds().isEmpty()) {
            state.canvas.canvasSelection.questIds().clear();
            if (!insideChapterPanel) {
                state.canvas.connectSourceQuestId = "";
                state.canvas.connectSourceQuestIds.clear();
            }
            state.canvas.selectionBoundsVisible = false;
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas selection cleared reason=outside_canvas x={} y={}", Math.round(mouseX - rootX), Math.round(mouseY - rootY));
            changed = true;
        }
        if (changed) {
            refresher.run();
            return true;
        }
        return handled;
    }

    record ClickDismissState(
            boolean chapterMenuWasOpen,
            boolean contextMenuWasOpen,
            boolean questDetailsWasOpen,
            boolean chapterTextMenuWasOpen,
            boolean canvasTextMenuWasOpen,
            boolean chapterMenuHit,
            boolean contextMenuHit,
            boolean chapterTextMenuHit,
            boolean canvasTextMenuHit,
            boolean assetContextHit
    ) {
    }
}
