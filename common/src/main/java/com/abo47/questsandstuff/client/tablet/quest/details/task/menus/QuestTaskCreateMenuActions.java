package com.abo47.questsandstuff.client.tablet.quest.details.task.menus;

import com.abo47.questsandstuff.client.tablet.contextmenu.ContextActionFactory;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuController;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuSection;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuSections;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsTransientManager;
import com.abo47.questsandstuff.client.tablet.quest.details.task.QuestTaskSections;
import com.abo47.questsandstuff.client.tablet.state.UiState;
import com.abo47.questsandstuff.client.tablet.text.QuestTranslationKeys;
import com.abo47.questsandstuff.client.tablet.text.TranslationKeys;

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
