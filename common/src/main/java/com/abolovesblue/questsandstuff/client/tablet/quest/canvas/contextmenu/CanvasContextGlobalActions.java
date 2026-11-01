package com.abolovesblue.questsandstuff.client.tablet.quest.canvas.contextmenu;

import net.minecraft.world.entity.player.Player;

import com.abolovesblue.questsandstuff.client.tablet.quest.canvas.clipboard.CanvasClipboardCopy;
import com.abolovesblue.questsandstuff.QuestsAndStuffMod;
import com.abolovesblue.questsandstuff.client.tablet.contextmenu.ContextActionFactory;
import com.abolovesblue.questsandstuff.client.tablet.contextmenu.ContextMenuController;
import com.abolovesblue.questsandstuff.client.tablet.contextmenu.ContextMenuSection;
import com.abolovesblue.questsandstuff.client.tablet.contextmenu.ContextMenuSections;
import com.abolovesblue.questsandstuff.client.tablet.quest.canvas.CanvasViewport;
import com.abolovesblue.questsandstuff.client.tablet.quest.canvas.viewport.CanvasCameraController;
import com.abolovesblue.questsandstuff.client.tablet.state.UiState;
import com.abolovesblue.questsandstuff.client.tablet.theme.tokens.ThemeColors;

final class CanvasContextGlobalActions {
    private CanvasContextGlobalActions() {
    }

    static void addGlobalActions(ContextMenuSections sections, CanvasViewport canvasViewport, UiState state, Player player, String selectedChapter) {
        if (state.canvas.canvasZoom != 1.0f) {
            sections.add(ContextMenuSection.ARRANGE, ContextActionFactory.action(CanvasContextMenuController.tr("ui.questsandstuff.context.reset_zoom"), "reset_zoom", ThemeColors.INTERACTIVE, () -> {
                CanvasCameraController.resetZoom(state, true);
                ContextMenuController.clearDeleteConfirm(state);
                QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=reset_zoom");
                canvasViewport.refresh();
            }));
        }
        if (!selectedChapter.isBlank() && CanvasClipboardCopy.hasClipboardContent(state)) {
            sections.add(ContextMenuSection.CLIPBOARD, ContextActionFactory.promoted(CanvasContextMenuController.tr("ui.questsandstuff.context.paste"), "paste", ThemeColors.SUCCESS, () -> {
                CanvasContextMenuSupport.pasteClipboard(player, state);
                QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=paste target={}", state.contextMenu.contextMenuTarget);
                canvasViewport.refresh();
            }));
        }
    }
}
