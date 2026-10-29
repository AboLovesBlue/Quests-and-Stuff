package com.abo47.questsandstuff.client.tablet.quest.details.task.edit;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.modal.target.ModalTargets;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsTransientManager;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsWindow;
import com.abo47.questsandstuff.client.tablet.quest.details.task.model.QuestTaskTypeChoice;
import com.abo47.questsandstuff.client.tablet.quest.details.task.model.QuestTaskTypeCatalog;
import com.abo47.questsandstuff.client.tablet.quest.details.task.model.QuestTaskJsonDefaults;
import com.abo47.questsandstuff.client.tablet.quest.details.task.model.QuestTaskJsonFactory;
import com.abo47.questsandstuff.client.tablet.quest.editor.EditorQuestCommandClient;
import com.abo47.questsandstuff.client.tablet.state.UiState;

import com.google.gson.JsonObject;

public final class QuestTaskTypeEditActions {
    private QuestTaskTypeEditActions() {
    }

    public static void beginTaskAdd(Player player, UiState state, String questId, CompoundTag quest, String typePath) {
        QuestTaskTypeChoice choice = QuestTaskTypeCatalog.taskChoice(typePath);
        String type = choice == null ? QuestTaskJsonFactory.MOD + typePath : choice.fullType();
        String id = QuestTaskJsonFactory.nextId(quest.getCompound("tasks"), "task_" + typePath);
        beginTask(player, state, questId, id, type, typePath, choice, true);
    }

    public static void beginTaskChange(Player player, UiState state, String questId, String id, String typePath) {
        QuestTaskTypeChoice choice = QuestTaskTypeCatalog.taskChoice(typePath);
        String type = choice == null ? QuestTaskJsonFactory.MOD + typePath : choice.fullType();
        beginTask(player, state, questId, id, type, typePath, choice, false);
    }

    private static void beginTask(Player player, UiState state, String questId, String id, String type, String typePath, QuestTaskTypeChoice choice, boolean add) {
        QuestTaskEditFlow flow = choice == null ? QuestTaskEditFlow.DIRECT_JSON : choice.editFlow();
        if (flow == QuestTaskEditFlow.ITEM_SOURCE_PICKER) {
            QuestDetailsTransientManager.openItemSourcePicker(state, ModalTargets.taskItem(questId, id, type));
            return;
        }
        if (flow == QuestTaskEditFlow.BIOME_PICKER) {
            QuestDetailsWindow.openBiomePicker(state, ModalTargets.taskBiome(questId, id, type));
            return;
        }
        if (flow == QuestTaskEditFlow.ADVANCEMENT_PICKER) {
            QuestDetailsWindow.openAdvancementPicker(state, ModalTargets.taskAdvancement(questId, id, type));
            return;
        }
        if (flow == QuestTaskEditFlow.RECIPE_PICKER) {
            QuestDetailsWindow.openRecipePicker(state, ModalTargets.taskRecipe(questId, id, type));
            return;
        }
        if (flow == QuestTaskEditFlow.STRUCTURE_PICKER) {
            QuestDetailsWindow.openStructurePicker(state, ModalTargets.taskStructure(questId, id, type));
            return;
        }
        if (flow == QuestTaskEditFlow.BLOCK_PICKER) {
            QuestDetailsWindow.openBlockPicker(state, ModalTargets.taskBlock(questId, id, type));
            return;
        }
        if (flow == QuestTaskEditFlow.STAT_PICKER) {
            QuestDetailsWindow.openStatPicker(state, ModalTargets.taskStat(questId, id, type));
            return;
        }
        if (flow == QuestTaskEditFlow.DIMENSION_PICKER) {
            QuestDetailsWindow.openDimensionPicker(state, ModalTargets.taskDimension(questId, id, type));
            return;
        }
        if (flow == QuestTaskEditFlow.ENTITY_ICON_PICKER) {
            QuestDetailsWindow.openIconPicker(state, ModalTargets.taskEntity(questId, id, type));
            return;
        }
        if (flow == QuestTaskEditFlow.XP_PICKER) {
            QuestDetailsTransientManager.openXpPicker(state, questId, id, true);
            QuestsAndStuffMod.debugLog("[QnS:UI] quest details xp task picker open quest={} task={} add={}", questId, id, add);
            return;
        }
        if (flow == QuestTaskEditFlow.SIMPLE_ICON_PICKER) {
            QuestDetailsWindow.openIconPicker(state, ModalTargets.taskSimpleIcon(questId, id, type));
            return;
        }
        EditorQuestCommandClient.putQuestTaskJson(player, questId, defaultTaskJson(id, typePath, choice).toString());
        QuestsAndStuffMod.debugLog("[QnS:UI] quest details {} task quest={} task={} type={}", add ? "add" : "change", questId, id, typePath);
    }

    private static JsonObject defaultTaskJson(String id, String typePath, QuestTaskTypeChoice choice) {
        return choice == null ? QuestTaskJsonDefaults.defaultTask(id, typePath) : choice.defaultJson(id);
    }
}
