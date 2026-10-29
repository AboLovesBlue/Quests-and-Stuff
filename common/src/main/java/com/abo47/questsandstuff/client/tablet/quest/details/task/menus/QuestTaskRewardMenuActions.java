package com.abo47.questsandstuff.client.tablet.quest.details.task.menus;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

import com.abo47.questsandstuff.client.sync.state.ClientQuestStateFacade;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextAction;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextActionFactory;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuController;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuSection;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuSections;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsTransientManager;
import com.abo47.questsandstuff.client.tablet.quest.details.task.QuestTaskEditActions;
import com.abo47.questsandstuff.client.tablet.quest.details.task.edit.QuestTaskXpEditor;
import com.abo47.questsandstuff.client.tablet.quest.details.task.model.QuestTaskSelectableRewards;
import com.abo47.questsandstuff.client.tablet.quest.details.task.model.QuestTaskJsonFactory;
import com.abo47.questsandstuff.client.tablet.quest.editor.EditorQuestCommandClient;
import com.abo47.questsandstuff.client.tablet.state.UiState;
import com.abo47.questsandstuff.client.tablet.text.QuestTranslationKeys;
import com.abo47.questsandstuff.client.tablet.text.TranslationKeys;
import com.abo47.questsandstuff.client.tablet.theme.tokens.ThemeColors;

import com.google.gson.JsonObject;

final class QuestTaskRewardMenuActions {
    private QuestTaskRewardMenuActions() {
    }

    static void addSections(ContextMenuSections sections, UiState state, Player player, String questId, String contextId) {
        CompoundTag rewardTag = ClientQuestStateFacade.quest(questId)
                .getCompound("rewards")
                .getCompound(contextId);
        JsonObject rewardJson = QuestTaskMenuSupport.parseTaskJson(rewardTag.getString("json"));
        boolean selectable = QuestTaskSelectableRewards.isSelectable(rewardJson);
        if ("command".equals(QuestTaskJsonFactory.typePath(rewardJson.has("type") ? rewardJson.get("type").getAsString() : ""))) {
            sections.add(ContextMenuSection.PRIMARY, ContextActionFactory.rename(TranslationKeys.text(QuestTranslationKeys.EDIT_COMMAND_REWARD), () -> {
                ContextMenuController.clearDeleteConfirm(state);
                QuestTaskEditActions.openExistingCommandRewardEditor(state, questId, contextId);
            }));
        }
        if (QuestTaskXpEditor.isXp(rewardJson)) {
            sections.add(ContextMenuSection.PRIMARY, ContextActionFactory.rename(TranslationKeys.text(QuestTranslationKeys.EDIT_XP), () -> {
                ContextMenuController.clearDeleteConfirm(state);
                QuestDetailsTransientManager.openXpPicker(state, questId, contextId, false);
            }));
        }
        if (!selectable) {
            sections.add(ContextMenuSection.PRIMARY, ContextActionFactory.action(TranslationKeys.text(QuestTranslationKeys.MAKE_SELECTABLE_REWARD), "selectable", ThemeColors.INTERACTIVE, () -> {
                ContextMenuController.clearDeleteConfirm(state);
                QuestTaskSelectableRewards.makeSelectable(player, questId, contextId);
            }));
        }
        sections.add(ContextMenuSection.PRIMARY, ContextActionFactory.promoted(TranslationKeys.text(QuestTranslationKeys.CHANGE_REWARD), "square_pen", ThemeColors.INTERACTIVE, () -> {
            ContextMenuController.clearDeleteConfirm(state);
            QuestDetailsTransientManager.openTypePicker(state, "reward_change", contextId);
        }));
        sections.add(ContextMenuSection.PRIMARY, ContextActionFactory.promotedRename(TranslationKeys.text(QuestTranslationKeys.RENAME_REWARD), () -> {
            ContextMenuController.clearDeleteConfirm(state);
            QuestTaskEditActions.openTaskRenameEditor(state, questId, contextId, false);
        }));
        List<ContextAction> moveActions = new ArrayList<>();
        QuestTaskMenuSupport.addMoveActions(moveActions, () -> {
            ContextMenuController.clearDeleteConfirm(state);
            EditorQuestCommandClient.moveQuestReward(player, questId, contextId, -1);
        }, () -> {
            ContextMenuController.clearDeleteConfirm(state);
            EditorQuestCommandClient.moveQuestReward(player, questId, contextId, 1);
        });
        sections.addAll(ContextMenuSection.ARRANGE, moveActions);
        List<ContextAction> visualActions = new ArrayList<>();
        QuestTaskMenuSupport.addVisualActions(visualActions, state, questId, contextId, false);
        sections.addAll(ContextMenuSection.APPEARANCE, visualActions);
        String deleteKey = "quest_details_reward:" + questId + ":" + contextId;
        sections.add(ContextMenuSection.DANGER, ContextActionFactory.delete(state, deleteKey, TranslationKeys.text(TranslationKeys.COMMON_REMOVE), () -> {
            EditorQuestCommandClient.removeQuestReward(player, questId, contextId);
        }));
    }
}
