package com.abo47.questsandstuff.client.tablet.quest.chapter;

import net.minecraft.world.entity.player.Player;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.sync.state.ClientQuestStateFacade;
import com.abo47.questsandstuff.client.tablet.controls.CardDragSortUtil;
import com.abo47.questsandstuff.client.tablet.state.UiState;
import com.abo47.questsandstuff.client.tablet.ui.factory.UiFactory;

public final class ChapterDragController {
    private ChapterDragController() {
    }

    public static boolean handleDrag(UiState state, Player player, Runnable refresh, int rootY, double mouseX, double mouseY, int button) {
        if (state.chapterPanel.chapterDragActive) {
            int localY = (int) Math.round(mouseY - rootY - UiFactory.CHAPTER_Y);
            int nextTarget = UiFactory.chapterInsertIndexAtY(localY, state);
            if (nextTarget != state.chapterPanel.chapterDragTargetIndex) {
                state.chapterPanel.chapterDragTargetIndex = nextTarget;
                QuestsAndStuffMod.debugLog("[QnS:UI] chapter drag preview moving={} targetIndex={}", state.chapterPanel.chapterDragName, nextTarget);
                refresh.run();
            }
            return true;
        }
        if (state.chapterPanel.chapterDragPending && button == 0) {
            if (!CardDragSortUtil.pastDragThreshold(mouseX, mouseY, state.chapterPanel.chapterDragStartX, state.chapterPanel.chapterDragStartY)) {
                return true;
            }
            state.chapterPanel.chapterDragPending = false;
            state.chapterPanel.chapterDragActive = true;
            int localY = (int) Math.round(mouseY - rootY - UiFactory.CHAPTER_Y);
            state.chapterPanel.chapterDragTargetIndex = UiFactory.chapterInsertIndexAtY(localY, state);
            QuestsAndStuffMod.debugLog("[QnS:UI] chapter drag start moving={} targetIndex={}", state.chapterPanel.chapterDragName, state.chapterPanel.chapterDragTargetIndex);
            refresh.run();
            return true;
        }
        return false;
    }

    public static boolean finish(UiState state, Player player, Runnable refresh) {
        if (state.chapterPanel.chapterDragActive) {
            finishActive(state, player);
            refresh.run();
            return true;
        }
        if (state.chapterPanel.chapterDragPending) {
            state.chapterPanel.chapterDragPending = false;
            state.chapterPanel.chapterDragName = "";
            state.chapterPanel.chapterDragTargetIndex = -1;
            refresh.run();
            return true;
        }
        return false;
    }

    private static void finishActive(UiState state, Player player) {
        String moving = state.chapterPanel.chapterDragName;
        int target = Math.max(0, state.chapterPanel.chapterDragTargetIndex);
        state.chapterPanel.chapterDragActive = false;
        state.chapterPanel.chapterDragPending = false;
        state.chapterPanel.chapterDragName = "";
        state.chapterPanel.chapterDragTargetIndex = -1;
        if (moving.isBlank()) {
            return;
        }
        int fromIndex = ClientQuestStateFacade.chapterOrder().indexOf(moving);
        int size = ClientQuestStateFacade.chapterOrder().size();
        target = CardDragSortUtil.targetIndexAfterDrop(fromIndex, target, size);
        QuestsAndStuffMod.debugLog("[QnS:UI] chapter drag drop moving={} fromIndex={} targetIndex={}", moving, fromIndex, target);
        if (fromIndex >= 0 && target >= 0 && target != fromIndex) {
            UiFactory.runChapterAction(player, state, "move_to", moving, "", target);
        }
        state.root.selectedChapter = moving;
        UiFactory.persistUiState(state);
    }
}
