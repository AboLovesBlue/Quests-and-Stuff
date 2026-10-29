package com.abo47.questsandstuff.client.tablet.quest.details.task;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import com.abo47.questsandstuff.client.tablet.quest.details.task.edit.QuestTaskPickerApplyActions;
import com.abo47.questsandstuff.client.tablet.quest.details.task.edit.QuestTaskRewardEditActions;
import com.abo47.questsandstuff.client.tablet.quest.details.task.edit.QuestTaskTypeEditActions;
import com.abo47.questsandstuff.client.tablet.quest.details.task.interaction.QuestTaskIconActions;
import com.abo47.questsandstuff.client.tablet.quest.details.task.interaction.QuestTaskRenameActions;
import com.abo47.questsandstuff.client.tablet.state.UiState;

public final class QuestTaskEditActions {
    private QuestTaskEditActions() {
    }

    public static void applyIconPick(Player player, UiState state, String entry) {
        QuestTaskPickerApplyActions.applyIconPick(player, state, entry);
    }

    public static void applyInventoryItemPick(Player player, UiState state, ItemStack stack) {
        QuestTaskPickerApplyActions.applyInventoryItemPick(player, state, stack);
    }

    public static void applyBiomePick(Player player, UiState state, String biome) {
        QuestTaskPickerApplyActions.applyBiomePick(player, state, biome);
    }

    public static void applyAdvancementPick(Player player, UiState state, String advancement) {
        QuestTaskPickerApplyActions.applyAdvancementPick(player, state, advancement);
    }

    public static void applyRecipePick(Player player, UiState state, String recipe) {
        QuestTaskPickerApplyActions.applyRecipePick(player, state, recipe);
    }

    public static void applyStructurePick(Player player, UiState state, String structure) {
        QuestTaskPickerApplyActions.applyStructurePick(player, state, structure);
    }

    public static void applyBlockPick(Player player, UiState state, String block) {
        QuestTaskPickerApplyActions.applyBlockPick(player, state, block);
    }

    public static void applyStatPick(Player player, UiState state, String stat) {
        QuestTaskPickerApplyActions.applyStatPick(player, state, stat);
    }

    public static void applyDimensionPick(Player player, UiState state, String dimension) {
        QuestTaskPickerApplyActions.applyDimensionPick(player, state, dimension);
    }

    public static void applyLootTablePick(Player player, UiState state, String lootTable) {
        QuestTaskPickerApplyActions.applyLootTablePick(player, state, lootTable);
    }

    public static void applyItemLockPick(Player player, UiState state, String entry) {
        QuestTaskPickerApplyActions.applyItemLockPick(player, state, entry);
    }

    public static void removeItemLock(Player player, UiState state, String questId, String taskId, String entry) {
        QuestTaskPickerApplyActions.removeItemLock(player, state, questId, taskId, entry);
    }

    public static void beginTaskAdd(Player player, UiState state, String questId, CompoundTag quest, String typePath) {
        QuestTaskTypeEditActions.beginTaskAdd(player, state, questId, quest, typePath);
    }

    public static void beginTaskChange(Player player, UiState state, String questId, String id, String typePath) {
        QuestTaskTypeEditActions.beginTaskChange(player, state, questId, id, typePath);
    }

    public static void beginRewardAdd(Player player, UiState state, String questId, CompoundTag quest, String typePath) {
        QuestTaskRewardEditActions.beginRewardAdd(player, state, questId, quest, typePath);
    }

    public static void beginRewardChange(Player player, UiState state, String questId, String id, String typePath) {
        QuestTaskRewardEditActions.beginRewardChange(player, state, questId, id, typePath);
    }

    public static void openCommandRewardEditor(UiState state, String questId, String id, String command, String title, String icon) {
        QuestTaskRewardEditActions.openCommandRewardEditor(state, questId, id, command, title, icon);
    }

    public static void openExistingCommandRewardEditor(UiState state, String questId, String id) {
        QuestTaskRewardEditActions.openExistingCommandRewardEditor(state, questId, id);
    }

    public static void openTaskRenameEditor(UiState state, String questId, String id, boolean task) {
        QuestTaskRenameActions.openTaskRenameEditor(state, questId, id, task);
    }

    public static void putTaskTitle(Player player, String questId, String id, String title, boolean task) {
        QuestTaskRenameActions.putTaskTitle(player, questId, id, title, task);
    }

    public static String taskIcon(String questId, String id, boolean task) {
        return QuestTaskIconActions.taskIcon(questId, id, task);
    }

    public static boolean isEntityTaskIcon(String questId, String id, boolean task) {
        return QuestTaskIconActions.isEntityTaskIcon(questId, id, task);
    }

    public static void putTaskIcon(Player player, String questId, String id, String icon, boolean task) {
        QuestTaskIconActions.putTaskIcon(player, questId, id, icon, task);
    }

    public static void putTaskIcon(Player player, String questId, String id, String icon, boolean task, boolean sync) {
        QuestTaskIconActions.putTaskIcon(player, questId, id, icon, task, sync);
    }
}
