package com.abo47.questsandstuff.client.tablet.root;

import net.minecraft.world.entity.player.Player;

import com.abo47.questsandstuff.client.tablet.modal.ModalStateQueries;
import com.abo47.questsandstuff.client.tablet.modal.ModalShell;
import com.abo47.questsandstuff.client.tablet.modal.ModalWindowManager;
import com.abo47.questsandstuff.client.tablet.quest.canvas.layer.CanvasLayerMutations;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.CanvasRenderer;
import com.abo47.questsandstuff.client.tablet.quest.chapter.menu.ChapterContextMenuLayout;
import com.abo47.questsandstuff.client.tablet.state.UiState;
import com.abo47.questsandstuff.client.tablet.ui.factory.UiFactory;
import com.abo47.questsandstuff.client.tablet.ui.state.StateQueries;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;

public final class RootHitTest {
    private RootHitTest() {
    }

    public static boolean isChapterTextMenuHit(UiState state, int rootX, int rootY, double mouseX, double mouseY) {
        if (state.chapterPanel.chapterTextMenuTarget.isBlank()) {
            return false;
        }
        int listHeight = state.chapterPanel.chapterListHeight > 0 ? state.chapterPanel.chapterListHeight : UiFactory.chapterHeight(state) - 12;
        int fy = UiFactory.chapterTextMenuY(state, listHeight);
        int menuX = UiFactory.chapterTextMenuX(state);
        int absX = rootX + UiFactory.CHAPTER_X + state.chapterPanel.chapterListOriginX + menuX;
        int absY = rootY + UiFactory.CHAPTER_Y + state.chapterPanel.chapterListOriginY + fy;
        int w = UiFactory.chapterTextMenuWidth(state);
        int h = UiFactory.chapterTextMenuHeight(state);
        return mouseX >= absX && mouseX <= absX + w && mouseY >= absY && mouseY <= absY + h;
    }

    public static boolean isChapterMenuHit(UiState state, int rootX, int rootY, double mouseX, double mouseY, Player player, Runnable refresh) {
        if (!state.chapterPanel.chapterMenuOpen) {
            return false;
        }
        ChapterContextMenuLayout layout = ChapterContextMenuLayout.resolve(state, StateQueries.rootWidth(state), StateQueries.rootHeight(state), player, refresh);
        int absMenuX = rootX + layout.menuX();
        int absMenuY = rootY + layout.menuY();
        return mouseX >= absMenuX && mouseX <= absMenuX + layout.menuW()
                && mouseY >= absMenuY && mouseY <= absMenuY + layout.menuH();
    }

    public static boolean isCanvasContextMenuHit(UiState state, int rootX, int rootY, double mouseX, double mouseY) {
        if (!state.contextMenu.contextMenuOpen) {
            return false;
        }
        int viewportX = rootX + state.canvas.canvasPanelX + state.canvas.canvasViewportX;
        int viewportY = rootY + state.canvas.canvasPanelY + state.canvas.canvasViewportY;
        int localX = (int) Math.round(mouseX - viewportX);
        int localY = (int) Math.round(mouseY - viewportY);
        return CanvasRenderer.isContextMenuHit(state, localX, localY);
    }

    public static boolean isCanvasTextMenuHit(UiState state, int rootX, int rootY, double mouseX, double mouseY) {
        if (!state.canvas.canvasTextMenuOpen || state.canvas.canvasTextMenuTarget.isBlank()) {
            return false;
        }
        CanvasTextLayer text = CanvasLayerMutations.findCanvasText(state, StateQueries.selectedChapterName(state), state.canvas.canvasTextMenuTarget);
        if (text == null) {
            return false;
        }
        int viewportX = rootX + state.canvas.canvasPanelX + state.canvas.canvasViewportX;
        int viewportY = rootY + state.canvas.canvasPanelY + state.canvas.canvasViewportY;
        int localX = (int) Math.round(mouseX - viewportX);
        int localY = (int) Math.round(mouseY - viewportY);
        if (inside(localX, localY, CanvasRenderer.canvasTextMenuBounds(state, text, state.canvas.canvasViewportW, state.canvas.canvasViewportH, 8))) {
            return true;
        }
        return CanvasRenderer.isCanvasTextOwnerHit(state, text, localX, localY);
    }

    public static boolean isToolsMenuHit(UiState state, int rootX, int rootY, double mouseX, double mouseY) {
        if ((!state.canvas.toolsMenuOpen && !state.canvas.toolsMenuClosing) || state.canvas.toolsMenuW <= 0 || state.canvas.toolsMenuH <= 0) {
            return false;
        }
        int localX = (int) Math.round(mouseX - rootX);
        int localY = (int) Math.round(mouseY - rootY);
        return localX >= state.canvas.toolsMenuX && localX <= state.canvas.toolsMenuX + state.canvas.toolsMenuW
                && localY >= state.canvas.toolsMenuY && localY <= state.canvas.toolsMenuY + state.canvas.toolsMenuH;
    }

    public static boolean isAssetContextHit(UiState state, int rootX, int rootY, double mouseX, double mouseY) {
        if (!state.pickers.assetContextOpen || !ModalStateQueries.isOpen(state, ModalWindowManager.ModalType.ASSET_PICKER)) {
            return false;
        }
        if (state.pickers.assetContextMenuW <= 0 || state.pickers.assetContextMenuH <= 0) {
            return false;
        }
        int rootW = StateQueries.rootWidth(state);
        int rootH = StateQueries.rootHeight(state);
        int w = Math.max(1, Math.min(ModalShell.MODAL_MAX_W, rootW - ModalShell.MODAL_MARGIN));
        int h = Math.max(1, Math.min(ModalShell.MODAL_MAX_H, rootH - ModalShell.MODAL_MARGIN));
        int mx = (rootW - w) / 2;
        int my = (rootH - h) / 2;
        int absX = rootX + mx + state.pickers.assetContextMenuX;
        int absY = rootY + my + state.pickers.assetContextMenuY;
        return mouseX >= absX && mouseX <= absX + state.pickers.assetContextMenuW
                && mouseY >= absY && mouseY <= absY + state.pickers.assetContextMenuH;
    }

    public static boolean isInsideCanvasViewport(UiState state, int rootX, int rootY, double mouseX, double mouseY) {
        int x = rootX + state.canvas.canvasPanelX + state.canvas.canvasViewportX;
        int y = rootY + state.canvas.canvasPanelY + state.canvas.canvasViewportY;
        int w = state.canvas.canvasViewportW;
        int h = state.canvas.canvasViewportH;
        return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
    }

    private static boolean inside(int x, int y, int[] bounds) {
        return bounds != null && bounds.length >= 4
                && x >= bounds[0] && x <= bounds[0] + bounds[2]
                && y >= bounds[1] && y <= bounds[1] + bounds[3];
    }

    public static boolean isInsideChapterPanel(UiState state, int rootX, int rootY, double mouseX, double mouseY) {
        int x = rootX + UiFactory.CHAPTER_X;
        int y = rootY + UiFactory.CHAPTER_Y;
        int w = UiFactory.chapterPanelWidth(state);
        int h = UiFactory.chapterHeight(state);
        return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
    }
}
