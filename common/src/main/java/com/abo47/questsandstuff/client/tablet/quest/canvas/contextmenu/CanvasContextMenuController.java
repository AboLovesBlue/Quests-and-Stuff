package com.abo47.questsandstuff.client.tablet.quest.canvas.contextmenu;

import java.util.List;

import net.minecraft.client.resources.language.I18n;

import com.abo47.questsandstuff.client.tablet.contextmenu.ContextAction;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuSections;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasViewport;
import com.abo47.questsandstuff.client.tablet.state.UiState;

import static com.abo47.questsandstuff.client.tablet.ui.state.StateQueries.selectedChapterName;

public final class CanvasContextMenuController {
    private CanvasContextMenuController() {
    }

    public static void renderCanvasContextMenu(CanvasViewport canvasViewport, UiState state) {
        CanvasContextMenuRenderer.renderCanvasContextMenu(canvasViewport, state);
    }

    public static List<ContextAction> buildContextActions(CanvasViewport canvasViewport, UiState state) {
        ContextMenuSections sections = new ContextMenuSections();
        String selectedChapter = selectedChapterName(state);
        CanvasContextCanvasActions.addCanvasEmptyActions(sections, canvasViewport, state, canvasViewport.player(), selectedChapter);
        CanvasContextSelectionActions.addSelectionActions(sections, canvasViewport, state, canvasViewport.player(), selectedChapter);
        CanvasContextQuestActions.addQuestActions(sections, canvasViewport, state, canvasViewport.player(), selectedChapter);
        CanvasContextElementActions.addImageActions(sections, canvasViewport, state, selectedChapter);
        CanvasContextElementActions.addTextActions(sections, canvasViewport, state, selectedChapter);
        CanvasContextElementActions.addExclusiveChoiceActions(sections, canvasViewport, state, selectedChapter);
        CanvasContextConnectionActions.addConnectionActions(sections, canvasViewport, state, canvasViewport.player(), selectedChapter);
        if (!exclusiveSubmenuOpen(state)) {
            CanvasContextGlobalActions.addGlobalActions(sections, canvasViewport, state, canvasViewport.player(), selectedChapter);
        }
        return sections.build();
    }

    private static boolean exclusiveSubmenuOpen(UiState state) {
        return state.contextMenu.contextQuestCompletionSoundMenuOpen;
    }

    public static List<ConnectionRef> selectedConnections(UiState state, String chapter) {
        return CanvasConnectionSelection.selectedConnections(state, chapter);
    }

    public record ConnectionRef(String prerequisiteId, String questId) {
    }

    public static boolean isContextMenuHit(UiState state, int x, int y) {
        return CanvasContextMenuSupport.isContextMenuHit(state, x, y);
    }

    public static boolean clickContextMenu(CanvasViewport canvasViewport, UiState state, int x, int y) {
        return CanvasContextMenuSupport.clickContextMenu(canvasViewport, state, x, y);
    }

    public static void scrollContextMenu(UiState state, double wheelDelta) {
        CanvasContextMenuSupport.scrollContextMenu(state, wheelDelta);
    }

    static String tr(String key, Object... args) {
        return I18n.get(key, args);
    }
}
