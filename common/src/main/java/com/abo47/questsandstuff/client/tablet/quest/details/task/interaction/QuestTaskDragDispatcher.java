package com.abo47.questsandstuff.client.tablet.quest.details.task.interaction;

import java.util.List;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;

import com.abo47.questsandstuff.client.sync.state.ClientQuestStateFacade;
import com.abo47.questsandstuff.client.tablet.quest.details.task.QuestTaskEntry;
import com.abo47.questsandstuff.client.tablet.quest.details.task.QuestTaskListPanel;
import com.abo47.questsandstuff.client.tablet.quest.details.task.QuestTaskEntries;
import com.abo47.questsandstuff.client.tablet.state.UiState;
import com.abo47.questsandstuff.client.tablet.ui.factory.UiFactory;

public final class QuestTaskDragDispatcher {
    private static final int SECTION_GAP = UiFactory.PANEL_INSET;
    private static final int TITLE_H = 18;
    private static final int HEADER_H = UiFactory.HEADER_H;

    private QuestTaskDragDispatcher() {
    }

    public static boolean handleDrag(Player player, UiState state, Runnable refresh, double mouseX, double mouseY, int button) {
        if (state == null || (!state.questDetails.questDetailsTaskDragPending && !state.questDetails.questDetailsTaskDragActive)) {
            return false;
        }
        TaskDragScope scope = dragScope(state);
        if (!scope.valid()) {
            QuestTaskListInteractions.clearDrag(state);
            refresh.run();
            return true;
        }
        int localY = (int) Math.round(mouseY - state.questDetails.questDetailsScreenY - scope.sectionY());
        return QuestTaskListInteractions.handleDrag(
                player,
                state,
                refresh,
                state.questDetails.questDetailsQuestId,
                scope.entries(),
                scope.kind(),
                TITLE_H,
                scope.sectionH() - 4,
                localY,
                mouseX,
                mouseY,
                button
        );
    }

    public static boolean handleRelease(Player player, UiState state, Runnable refresh) {
        if (state == null || (!state.questDetails.questDetailsTaskDragPending && !state.questDetails.questDetailsTaskDragActive)) {
            return false;
        }
        TaskDragScope scope = dragScope(state);
        if (scope.valid()) {
            return QuestTaskListInteractions.handleRelease(player, state, refresh, state.questDetails.questDetailsQuestId, scope.entries(), scope.kind());
        }
        QuestTaskListInteractions.clearDrag(state);
        refresh.run();
        return true;
    }

    private static TaskDragScope dragScope(UiState state) {
        String questId = state.questDetails.questDetailsQuestId == null ? "" : state.questDetails.questDetailsQuestId.trim();
        CompoundTag quest = questId.isBlank() ? null : ClientQuestStateFacade.quest(questId);
        if (quest == null || quest.isEmpty()) {
            return TaskDragScope.invalid();
        }
        int panelY = UiFactory.CHAPTER_Y + QuestTaskListPanel.leftPanelContentY();
        int panelH = QuestTaskListPanel.leftPanelContentH();
        int sectionsY = HEADER_H + SECTION_GAP;
        int sectionsH = panelH - HEADER_H - SECTION_GAP;
        int sectionH = Math.max(20, (sectionsH - SECTION_GAP) / 2);
        if ("tasks".equals(state.questDetails.questDetailsTaskDragKind)) {
            return new TaskDragScope("tasks", QuestTaskEntries.entries(quest.getCompound("tasks"), quest.getList("tasks_order", Tag.TAG_STRING)), panelY + sectionsY, sectionH);
        }
        if ("rewards".equals(state.questDetails.questDetailsTaskDragKind)) {
            return new TaskDragScope("rewards", QuestTaskEntries.entries(quest.getCompound("rewards"), quest.getList("rewards_order", Tag.TAG_STRING)), panelY + sectionsY + sectionH + SECTION_GAP, sectionH);
        }
        return TaskDragScope.invalid();
    }

    private record TaskDragScope(String kind, List<QuestTaskEntry> entries, int sectionY, int sectionH) {
        private static TaskDragScope invalid() {
            return new TaskDragScope("", List.of(), 0, 0);
        }

        private boolean valid() {
            return !kind.isBlank();
        }
    }
}
