package com.abo47.questsandstuff.client.tablet.quest.details.task;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuController;
import com.abo47.questsandstuff.client.tablet.controls.EntityIconControls;
import com.abo47.questsandstuff.client.tablet.icons.DisplayIconWidget;
import com.abo47.questsandstuff.client.tablet.icons.ItemStackIconCodec;
import com.abo47.questsandstuff.client.tablet.modal.target.ModalTargets;
import com.abo47.questsandstuff.client.tablet.modal.ModalPanel;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsEditController;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsWindow;
import com.abo47.questsandstuff.client.tablet.quest.details.task.edit.QuestTaskInlineFields;
import com.abo47.questsandstuff.client.tablet.quest.details.task.edit.QuestTaskLootTableRewardEditor;
import com.abo47.questsandstuff.client.tablet.quest.details.task.interaction.QuestTaskListInteractions;
import com.abo47.questsandstuff.client.tablet.quest.details.task.model.QuestTaskLockJson;
import com.abo47.questsandstuff.client.tablet.quest.details.task.model.QuestTaskSelectableRewards;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.tokens.TabletColors;

import com.google.gson.JsonObject;

import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.flatHitButton;

final class QuestTaskCardRenderer {
    private QuestTaskCardRenderer() {
    }

    static void renderTaskCard(WidgetGroup parent, TabletUiState state, Player player, Runnable refresh, String questId, QuestTaskEntry entry, int x, int y, int w, List<QuestTaskEntry> entries, int listY, int listBottom) {
        parent.addWidget(QuestTaskCardPanel.cardPanel(state, player, refresh, questId, entries, "tasks", entry.id(), x, y, w, entry.tag().getFloat("progress"), listY, listBottom, false, false, false, QuestTaskLockJson.hasLocks(entry.json())));
        String icon = QuestTaskDisplayText.taskIcon(entry.json());
        addTaskIcon(parent, entry.json(), icon, x + 8, y + 8);
        addIconHoverHit(parent, state, refresh, questId, entry.id(), true, entry.json(), icon, x + 8, y + 8);
        boolean renaming = QuestTaskInlineFields.isRenamingTask(state, questId, entry.id(), true);
        QuestTaskInlineFields.renderTaskTitle(parent, state, player, refresh, questId, entry, true, x + 30, y + 8, renaming ? x + w - 8 : taskTitleRightX(entry, x, w));
        if (!renaming) {
            QuestTaskInlineFields.renderAmountField(parent, state, player, refresh, questId, entry, x + w - 34, y + 9, 30, true);
        }
    }

    static void renderRewardCard(WidgetGroup parent, TabletUiState state, Player player, Runnable refresh, String questId, QuestTaskEntry entry, int x, int y, int w, List<QuestTaskEntry> entries, int listY, int listBottom, boolean rewardsClaimed) {
        boolean selectableWrapper = QuestTaskSelectableRewards.isSelectable(entry.json());
        boolean selectableChoice = QuestTaskSelectableRewards.isSelectableChoiceId(entry.id());
        boolean selectableReward = selectableWrapper || selectableChoice;
        boolean claimChoiceEntry = QuestTaskSelectableRewards.isClaimChoiceEntry(entry);
        JsonObject displayJson = selectableWrapper ? QuestTaskSelectableRewards.displayJson(entry.json()) : entry.json();
        QuestTaskEntry displayEntry = selectableWrapper ? new QuestTaskEntry(entry.id(), entry.tag(), displayJson) : entry;
        parent.addWidget(QuestTaskCardPanel.cardPanel(state, player, refresh, questId, entries, "rewards", entry.id(), x, y, w, 0.0f, listY, listBottom, selectableReward, rewardsClaimed, claimChoiceEntry, false));
        String icon = QuestTaskDisplayText.rewardIcon(displayJson);
        addTaskIcon(parent, displayJson, icon, x + 8, y + 8);
        addIconHoverHit(parent, state, refresh, questId, entry.id(), false, displayJson, icon, x + 8, y + 8);
        boolean renaming = QuestTaskInlineFields.isRenamingTask(state, questId, entry.id(), false);
        int titleX = x + 30;
        if (!renaming && !selectableReward && QuestTaskLootTableRewardEditor.isLootTable(entry.json())) {
            QuestTaskLootTableRewardEditor.render(parent, state, player, refresh, questId, entry, titleX, y + 8, rewardTitleRightX(entry, x, w));
        } else {
            QuestTaskInlineFields.renderTaskTitle(parent, state, player, refresh, questId, displayEntry, false, titleX, y + 8, renaming ? x + w - 8 : rewardTitleRightX(displayEntry, x, w), rewardsClaimed ? TabletColors.TEXT_MUTED : TabletColors.TEXT_PRIMARY);
        }
        if (!renaming) {
            QuestTaskInlineFields.renderAmountField(parent, state, player, refresh, questId, displayEntry, x + w - 34, y + 9, 30, false);
        }
    }

    private static void addTaskIcon(WidgetGroup parent, JsonObject json, String icon, int x, int y) {
        ItemStack stack = QuestTaskItemStacks.iconStack(json);
        if (!stack.isEmpty()) {
            parent.addWidget(new DisplayIconWidget(x, y, QuestTaskListPanel.ICON, QuestTaskListPanel.ICON, stack));
            return;
        }
        parent.addWidget(new DisplayIconWidget(x, y, QuestTaskListPanel.ICON, QuestTaskListPanel.ICON, icon));
    }

    private static int taskTitleRightX(QuestTaskEntry entry, int x, int w) {
        if (!QuestTaskDisplayText.usesAmountField(entry.json(), true)) {
            return x + w - 8;
        }
        if (QuestTaskDisplayText.isManualTask(entry.json())) {
            return x + w - 58;
        }
        int count = Math.max(0, entry.tag().getInt("count"));
        int countTextW = Minecraft.getInstance().font.width(count + " /");
        return x + w - 34 - countTextW - 6;
    }

    private static int rewardTitleRightX(QuestTaskEntry entry, int x, int w) {
        return QuestTaskDisplayText.usesAmountField(entry.json(), false) ? x + w - 40 : x + w - 8;
    }

    private static void addIconHoverHit(WidgetGroup parent, TabletUiState state, Runnable refresh, String questId, String id, boolean task, JsonObject json, String icon, int x, int y) {
        if (!QuestDetailsEditController.canEdit(state)) {
            return;
        }
        var hit = flatHitButton(x, y, QuestTaskListPanel.ICON, QuestTaskListPanel.ICON, click -> {
            ContextMenuController.clearDeleteConfirm(state);
            QuestDetailsWindow.openIconPicker(state, task ? ModalTargets.taskIcon(questId, id) : ModalTargets.rewardIcon(questId, id));
            refresh.run();
        });
        hit.setHoverTexture(EntityIconControls.iconHoverTexture());
        hit.setHoverTooltips(iconTooltip(json, icon));
        parent.addWidget(hit);
    }

    private static Component[] iconTooltip(JsonObject json, String icon) {
        ItemStack stack = QuestTaskItemStacks.iconStack(json);
        if (stack.isEmpty()) {
            return ModalPanel.iconTooltip(icon);
        }
        List<Component> lines = new ArrayList<>(Screen.getTooltipFromItem(Minecraft.getInstance(), stack));
        String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        lines.add(Component.literal(itemId).withStyle(ChatFormatting.DARK_GRAY));
        String summary = ItemStackIconCodec.nbtSummary(stack);
        if (!summary.isBlank()) {
            lines.add(Component.literal("NBT: " + summary).withStyle(ChatFormatting.GOLD));
        }
        return lines.toArray(Component[]::new);
    }
}
