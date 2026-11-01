package com.abolovesblue.questsandstuff.client.tablet.quest.details.task.menus;

import com.abolovesblue.questsandstuff.client.tablet.contextmenu.ContextActionFactory;
import com.abolovesblue.questsandstuff.client.tablet.contextmenu.ContextMenuController;
import com.abolovesblue.questsandstuff.client.tablet.contextmenu.ContextMenuSection;
import com.abolovesblue.questsandstuff.client.tablet.contextmenu.ContextMenuSections;
import com.abolovesblue.questsandstuff.client.tablet.quest.details.QuestDetailsTransientManager;
import com.abolovesblue.questsandstuff.client.tablet.quest.details.task.QuestTaskSections;
import com.abolovesblue.questsandstuff.client.tablet.state.UiState;
import com.abolovesblue.questsandstuff.client.tablet.text.QuestTranslationKeys;
import com.abolovesblue.questsandstuff.client.tablet.text.TranslationKeys;

final class QuestTaskCreateMenuActions {
    private QuestTaskCreateMenuActions() {
    }

    static void addSections(ContextMenuSections sections, UiState state, String kind) {
        if (QuestTaskSections.isTask(kind)) {
            sections.add(ContextMenuSection.PRIMARY, ContextActionFactory.add(TranslationKeys.text(QuestTranslationKeys.ADD_TASK), () -> {
                ContextMenuController.clearDeleteConfirm(state);
                QuestDetailsTransientManager.openTypePicker(state, "task", "");
            }));
        }
        if (QuestTaskSections.isReward(kind)) {
            sections.add(ContextMenuSection.PRIMARY, ContextActionFactory.add(TranslationKeys.text(QuestTranslationKeys.ADD_REWARD), () -> {
                ContextMenuController.clearDeleteConfirm(state);
                QuestDetailsTransientManager.openTypePicker(state, "reward", "");
            }));
        }
    }
}
