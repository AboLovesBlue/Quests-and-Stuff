package com.abo47.questsandstuff.client.tablet.quest.details.task.menus;

import com.abo47.questsandstuff.client.tablet.modal.AssetPickerOpenActions;
import java.util.List;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextAction;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextActionFactory;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuController;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuSection;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuSections;
import com.abo47.questsandstuff.client.tablet.controls.EntityIconControls;
import com.abo47.questsandstuff.client.tablet.entity.motion.EntityMotionEditor;
import com.abo47.questsandstuff.client.tablet.modal.target.ModalTargets;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsTransientManager;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsWindow;
import com.abo47.questsandstuff.client.tablet.quest.details.task.QuestTaskEditActions;
import com.abo47.questsandstuff.client.tablet.quest.details.task.model.QuestTaskJsonFactory;
import com.abo47.questsandstuff.client.tablet.state.UiState;
import com.abo47.questsandstuff.client.tablet.text.QuestTranslationKeys;
import com.abo47.questsandstuff.client.tablet.text.TranslationKeys;
import com.abo47.questsandstuff.client.tablet.theme.tokens.ThemeColors;

import com.google.gson.JsonObject;

final class QuestTaskMenuSupport {
    private QuestTaskMenuSupport() {
    }

    static ContextAction editSubmenu(List<ContextAction> editActions) {
        return ContextActionFactory.submenu(TranslationKeys.text(QuestTranslationKeys.CONTEXT_EDIT), "rename", ThemeColors.INTERACTIVE, editActions);
    }

    static void addMoveActions(List<ContextAction> actions, Runnable moveUp, Runnable moveDown) {
        actions.add(ContextActionFactory.moveUp(moveUp));
        actions.add(ContextActionFactory.moveDown(moveDown));
    }

    static void addVisualActions(List<ContextAction> actions, UiState state, String questId, String taskId, boolean task) {
        actions.add(ContextActionFactory.submenu(TranslationKeys.text(QuestTranslationKeys.CONTEXT_CHANGE_ICON), "square_pen", ThemeColors.INTERACTIVE, List.of(
                ContextActionFactory.action(TranslationKeys.text(QuestTranslationKeys.CONTEXT_USE_BUILTIN_ICON), "icon", ThemeColors.INTERACTIVE, () -> {
                    ContextMenuController.clearDeleteConfirm(state);
                    QuestDetailsWindow.openIconPicker(state, task ? ModalTargets.taskIcon(questId, taskId) : ModalTargets.rewardIcon(questId, taskId));
                }),
                ContextActionFactory.action(TranslationKeys.text(QuestTranslationKeys.CONTEXT_USE_ASSET_IMAGE), "image", ThemeColors.INTERACTIVE, () -> {
                    ContextMenuController.clearDeleteConfirm(state);
                    AssetPickerOpenActions.openQuestDetailsAssetIconPicker(state, task ? ModalTargets.taskIcon(questId, taskId) : ModalTargets.rewardIcon(questId, taskId));
                })
        )));
        addEntityIconActions(actions, state, questId, taskId, task);
    }

    static JsonObject parseTaskJson(String value) {
        QuestTaskJsonFactory.ParseResult result = QuestTaskJsonFactory.readResult(value);
        if (!result.valid()) {
            QuestsAndStuffMod.debugLog(
                    "[QnS:UI] task menu json fallback diagnostic={}",
                    result.diagnostic()
            );
        }
        return result.value();
    }

    static void addEntityIconActions(List<ContextAction> actions, UiState state, String questId, String taskId, boolean task) {
        String icon = QuestTaskEditActions.taskIcon(questId, taskId, task);
        ContextMenuSections variantSections = new ContextMenuSections();
        EntityIconControls.addEntityVariantAndMotionActions(
                variantSections,
                ContextMenuSection.APPEARANCE,
                state,
                icon,
                task ? ModalTargets.taskTask(questId, taskId) : ModalTargets.taskReward(questId, taskId),
                () -> QuestDetailsTransientManager.closeContext(state),
                () -> EntityMotionEditor.openTaskIcon(state, questId, taskId, task, state.questDetails.questDetailsContextX, state.questDetails.questDetailsContextY),
                () -> {
                }
        );
        actions.addAll(variantSections.build());
    }
}
