package com.abo47.questsandstuff.client.tablet.root;

import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import com.abo47.questsandstuff.client.tablet.entity.motion.EntityMotionEditor;
import com.abo47.questsandstuff.client.tablet.quest.chapter.ChapterDragController;
import com.abo47.questsandstuff.client.tablet.quest.chapter.ChapterPanel;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsWindow;
import com.abo47.questsandstuff.client.tablet.quest.details.task.interaction.QuestTaskDragDispatcher;
import com.abo47.questsandstuff.client.tablet.state.UiState;
import com.abo47.questsandstuff.client.tablet.ui.factory.UiFactory;
import com.abo47.questsandstuff.client.tablet.ui.state.ModalState;
import com.abo47.questsandstuff.client.tablet.ui.widget.WidgetCoordinates;

final class RootPointerRouter {
    private RootPointerRouter() {
    }

    static boolean mouseClicked(RootWidget root, UiState state, WidgetGroup modalLayer, WidgetGroup frontWindowLayer, Runnable refresher, MouseClickDelegate selfClick, double mouseX, double mouseY, int button) {
        ModalState.rememberPointerSource(state, localRootX(root, mouseX), localRootY(root, mouseY));
        if (root.isAnyModalOpen()) {
            if (modalLayer != null) {
                modalLayer.mouseClicked(mouseX, mouseY, button);
            }
            return true;
        }
        if (root.isFrontWindowOpen()) {
            if (frontWindowLayer != null) {
                QuestDetailsWindow.syncScreenOrigin(frontWindowLayer, state);
                frontWindowLayer.mouseClicked(mouseX, mouseY, button);
            }
            return true;
        }
        RootDismissals.ClickDismissState dismissState = RootDismissals.capture(root, state, mouseX, mouseY, refresher);
        if (button == 0 && beginChapterScrollDrag(root, state, refresher, mouseX, mouseY)) {
            return RootDismissals.handleAfterClick(root, state, refresher, dismissState, mouseX, mouseY, button, true);
        }
        if (button == 0 && dismissState.chapterMenuHit()) {
            boolean handled = ChapterPanel.clickChapterMenu(state, root.resolvePlayer(), refresher, localRootX(root, mouseX), localRootY(root, mouseY));
            return RootDismissals.handleAfterClick(root, state, refresher, dismissState, mouseX, mouseY, button, handled);
        }
        boolean handled = selfClick.invoke(mouseX, mouseY, button);
        return RootDismissals.handleAfterClick(root, state, refresher, dismissState, mouseX, mouseY, button, handled);
    }

    static boolean mouseDragged(RootWidget root, UiState state, WidgetGroup modalLayer, WidgetGroup frontWindowLayer, Runnable refresher, MouseDragDelegate selfDrag, double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (root.isAnyModalOpen()) {
            if (modalLayer != null) {
                modalLayer.mouseDragged(mouseX, mouseY, button, dragX, dragY);
            }
            return true;
        }
        if (root.isFrontWindowOpen()) {
            if (frontWindowLayer != null) {
                QuestDetailsWindow.syncScreenOrigin(frontWindowLayer, state);
            }
            if (QuestTaskDragDispatcher.handleDrag(root.resolvePlayer(), state, refresher, mouseX, mouseY, button)) {
                return true;
            }
            if (frontWindowLayer != null) {
                frontWindowLayer.mouseDragged(mouseX, mouseY, button, dragX, dragY);
            }
            return true;
        }
        if (state.chapterPanel.chapterScrollDragging) {
            updateChapterScrollDrag(root, state, refresher, mouseY);
            return true;
        }
        if (EntityMotionEditor.isDragging(state)) {
            selfDrag.invoke(mouseX, mouseY, button, dragX, dragY);
            return true;
        }
        if (ChapterDragController.handleDrag(state, root.resolvePlayer(), refresher, WidgetCoordinates.rootY(root), mouseX, mouseY, button)) {
            return true;
        }
        return selfDrag.invoke(mouseX, mouseY, button, dragX, dragY);
    }

    static boolean mouseReleased(RootWidget root, UiState state, WidgetGroup modalLayer, WidgetGroup frontWindowLayer, Runnable refresher, MouseClickDelegate selfRelease, double mouseX, double mouseY, int button) {
        if (root.isAnyModalOpen()) {
            if (modalLayer != null) {
                modalLayer.mouseReleased(mouseX, mouseY, button);
            }
            return true;
        }
        if (root.isFrontWindowOpen()) {
            if (frontWindowLayer != null) {
                QuestDetailsWindow.syncScreenOrigin(frontWindowLayer, state);
            }
            if (QuestTaskDragDispatcher.handleRelease(root.resolvePlayer(), state, refresher)) {
                return true;
            }
            if (frontWindowLayer != null) {
                frontWindowLayer.mouseReleased(mouseX, mouseY, button);
            }
            return true;
        }
        if (state.chapterPanel.chapterScrollDragging) {
            state.chapterPanel.chapterScrollDragging = false;
            refresher.run();
            return true;
        }
        if (EntityMotionEditor.isDragging(state)) {
            selfRelease.invoke(mouseX, mouseY, button);
            return true;
        }
        if (ChapterDragController.finish(state, root.resolvePlayer(), refresher)) {
            return true;
        }
        return selfRelease.invoke(mouseX, mouseY, button);
    }

    static boolean mouseWheelMove(RootWidget root, UiState state, WidgetGroup modalLayer, WidgetGroup frontWindowLayer, MouseWheelDelegate selfWheel, double mouseX, double mouseY, double wheelDelta) {
        if (root.isAnyModalOpen()) {
            if (modalLayer != null) {
                modalLayer.mouseWheelMove(mouseX, mouseY, wheelDelta);
            }
            return true;
        }
        if (root.isFrontWindowOpen()) {
            if (frontWindowLayer != null) {
                QuestDetailsWindow.syncScreenOrigin(frontWindowLayer, state);
                frontWindowLayer.mouseWheelMove(mouseX, mouseY, wheelDelta);
            }
            return true;
        }
        return selfWheel.invoke(mouseX, mouseY, wheelDelta);
    }

    private static boolean beginChapterScrollDrag(RootWidget root, UiState state, Runnable refresher, double mouseX, double mouseY) {
        if (!RootHitTest.isInsideChapterPanel(state, WidgetCoordinates.rootX(root), WidgetCoordinates.rootY(root), mouseX, mouseY)) {
            return false;
        }
        int localX = localChapterX(root, mouseX);
        int localY = localChapterY(root, mouseY);
        if (!UiFactory.isChapterScrollBarHit(localX, localY, state)) {
            return false;
        }
        state.chapterPanel.chapterDragPending = false;
        state.chapterPanel.chapterDragActive = false;
        state.chapterPanel.chapterDragName = "";
        state.chapterPanel.chapterDragTargetIndex = -1;
        state.chapterPanel.chapterScrollDragging = true;
        int previous = state.chapterPanel.chapterScroll;
        UiFactory.updateChapterScrollByMouse(localY, state);
        if (state.chapterPanel.chapterScroll != previous) {
            refresher.run();
        }
        return true;
    }

    private static void updateChapterScrollDrag(RootWidget root, UiState state, Runnable refresher, double mouseY) {
        int previous = state.chapterPanel.chapterScroll;
        UiFactory.updateChapterScrollByMouse(localChapterY(root, mouseY), state);
        if (state.chapterPanel.chapterScroll != previous) {
            refresher.run();
        }
    }

    private static int localRootX(RootWidget root, double mouseX) {
        return WidgetCoordinates.localX(root, 0, mouseX);
    }

    private static int localRootY(RootWidget root, double mouseY) {
        return WidgetCoordinates.localY(root, 0, mouseY);
    }

    private static int localChapterX(RootWidget root, double mouseX) {
        return WidgetCoordinates.localX(root, UiFactory.CHAPTER_X, mouseX);
    }

    private static int localChapterY(RootWidget root, double mouseY) {
        return WidgetCoordinates.localY(root, UiFactory.CHAPTER_Y, mouseY);
    }

    @FunctionalInterface
    interface MouseClickDelegate {
        boolean invoke(double mouseX, double mouseY, int button);
    }

    @FunctionalInterface
    interface MouseDragDelegate {
        boolean invoke(double mouseX, double mouseY, int button, double dragX, double dragY);
    }

    @FunctionalInterface
    interface MouseWheelDelegate {
        boolean invoke(double mouseX, double mouseY, double wheelDelta);
    }
}
