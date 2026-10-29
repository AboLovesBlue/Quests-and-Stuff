package com.abo47.questsandstuff.client.tablet.quest.canvas.interaction;

import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuController;
import com.abo47.questsandstuff.client.tablet.root.RootHitTest;
import com.abo47.questsandstuff.client.tablet.state.UiState;
import com.abo47.questsandstuff.client.tablet.ui.widget.WidgetCoordinates;
import com.lowdragmc.lowdraglib.gui.widget.Widget;

public final class CanvasInteractionGate {
    private CanvasInteractionGate() {
    }

    public static boolean hoverAllowed(UiState state, Widget view, double mouseX, double mouseY) {
        if (state == null || view == null) {
            return true;
        }
        if (ContextMenuController.isOpen(state)) {
            return false;
        }
        return !RootHitTest.isInsideChapterPanel(state, WidgetCoordinates.rootX(view),
                WidgetCoordinates.rootY(view), mouseX, mouseY);
    }
}
