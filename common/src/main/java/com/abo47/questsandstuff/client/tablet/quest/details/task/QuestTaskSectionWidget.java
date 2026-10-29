package com.abo47.questsandstuff.client.tablet.quest.details.task;

import java.util.List;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuController;
import com.abo47.questsandstuff.client.tablet.controls.DragScrollBarWidget;
import com.abo47.questsandstuff.client.tablet.controls.IconOnlyButton;
import com.abo47.questsandstuff.client.tablet.controls.ScrollMath;
import com.abo47.questsandstuff.client.tablet.modal.target.ModalTargets;
import com.abo47.questsandstuff.client.tablet.quest.canvas.viewport.CanvasViewportScissor;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsCoordinates;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsEditController;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsWindow;
import com.abo47.questsandstuff.client.tablet.quest.details.task.interaction.QuestTaskListInteractions;
import com.abo47.questsandstuff.client.tablet.quest.details.task.model.QuestTaskSelectableRewards;
import com.abo47.questsandstuff.client.tablet.state.UiState;
import com.abo47.questsandstuff.client.tablet.text.QuestTranslationKeys;
import com.abo47.questsandstuff.client.tablet.text.TranslationKeys;
import com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory;
import com.abo47.questsandstuff.client.tablet.theme.skin.SkinAnchorRegistry;
import com.abo47.questsandstuff.client.tablet.theme.tokens.ThemeColors;

import static com.abo47.questsandstuff.client.tablet.ui.factory.UiFactory.label;

final class QuestTaskSectionWidget {
    private QuestTaskSectionWidget() {
    }

    static void renderTasks(WidgetGroup modal, UiState state, Player player, Runnable refresh, String questId, CompoundTag quest, int x, int y, int w, int h) {
        List<QuestTaskEntry> tasks = QuestTaskEntries.entries(quest.getCompound("tasks"), quest.getList("tasks_order", Tag.TAG_STRING));
        WidgetGroup section = sectionWidget(state, player, refresh, questId, x, y, w, h, "tasks", tasks, QuestTaskListPanel.TITLE_H, 4, false);
        section.addWidget(label(8, 6, QuestTranslationKeys.tasks(), ThemeColors.TEXT_PRIMARY));
        addChangeIconButton(section, state, refresh, questId, "tasks", w);
        QuestTaskCardList.renderCards(section, state, player, refresh, questId, tasks, w, h, QuestTaskListPanel.TITLE_H, true);
        modal.addWidget(section);
    }

    static void renderRewards(WidgetGroup modal, UiState state, Player player, Runnable refresh, String questId, CompoundTag quest, int x, int y, int w, int h) {
        List<QuestTaskEntry> rewards = QuestTaskEntries.entries(quest.getCompound("rewards"), quest.getList("rewards_order", Tag.TAG_STRING));
        List<QuestTaskEntry> displayRewards = QuestTaskSelectableRewards.displayEntries(rewards, QuestDetailsEditController.canEdit(state));
        boolean rewardsClaimed = quest.getBoolean("claimed") || questId.equals(state.questDetails.questDetailsClaimedOverrideQuestId);
        WidgetGroup section = sectionWidget(state, player, refresh, questId, x, y, w, h, "rewards", displayRewards, QuestTaskListPanel.TITLE_H, 4, rewardsClaimed);
        section.addWidget(label(8, 6, QuestTranslationKeys.rewards(), ThemeColors.TEXT_PRIMARY));
        addChangeIconButton(section, state, refresh, questId, "rewards", w);
        QuestTaskCardList.renderCards(section, state, player, refresh, questId, displayRewards, w, h, QuestTaskListPanel.TITLE_H, false, rewardsClaimed);
        modal.addWidget(section);
    }

    private static WidgetGroup sectionWidget(UiState state, Player player, Runnable refresh, String questId, int x, int y, int w, int h, String kind, List<QuestTaskEntry> entries, int listY, int bottomPad, boolean rewardsClaimed) {
        WidgetGroup section = new WidgetGroup(x, y, w, h) {
            @Override
            public boolean mouseWheelMove(double mouseX, double mouseY, double wheelDelta) {
                if (!isMouseOverElement(mouseX, mouseY)) {
                    return super.mouseWheelMove(mouseX, mouseY, wheelDelta);
                }
                int delta = wheelDelta < 0 ? 16 : -16;
                if ("tasks".equals(kind)) {
                    state.questDetails.questDetailsTaskScroll = Math.max(0, state.questDetails.questDetailsTaskScroll + delta);
                } else {
                    state.questDetails.questDetailsRewardScroll = Math.max(0, state.questDetails.questDetailsRewardScroll + delta);
                }
                refresh.run();
                return true;
            }

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                if (!isMouseOverElement(mouseX, mouseY)) {
                    return super.mouseClicked(mouseX, mouseY, button);
                }
                if (button == 0 && super.mouseClicked(mouseX, mouseY, button)) {
                    return true;
                }
                int lx = QuestDetailsCoordinates.localX(state, mouseX, getPositionX(), w);
                int ly = QuestDetailsCoordinates.localY(state, mouseY, getPositionY(), h);
                QuestTaskEntry hitEntry = QuestTaskCardList.hitEntry(state, entries, kind, listY, h - bottomPad, ly);
                String id = hitEntry == null ? "" : hitEntry.id();
                boolean editMode = QuestDetailsEditController.canEdit(state);
                boolean cardBodyHit = isCardBodyHit(lx, w);
                boolean claimChoice = "rewards".equals(kind)
                        && !rewardsClaimed
                        && QuestTaskSelectableRewards.isClaimChoiceEntry(hitEntry);
                if (button == 0 && !id.isBlank()) {
                    if (claimChoice) {
                        QuestTaskSelectableRewards.selectChoice(state, id);
                        state.questDetails.questDetailsTaskDragRefreshQueued = true;
                        return true;
                    }
                    if (editMode) {
                        QuestTaskListInteractions.selectAndBeginDrag(state, kind, id, mouseX, mouseY);
                        state.questDetails.questDetailsTaskDragRefreshQueued = true;
                        return true;
                    }
                }
                if (button == 1 && editMode) {
                    QuestTaskListInteractions.select(state, kind, id);
                    QuestDetailsCoordinates.openContextAtPointer(state, "tasks".equals(kind) ? "task" : "reward", id, mouseX, mouseY, getPositionX(), getPositionY(), lx, ly);
                    refresh.run();
                    return true;
                }
                return super.mouseClicked(mouseX, mouseY, button);
            }

            @Override
            public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
                if (QuestTaskCardList.isTaskScrollDragging(state, kind)) {
                    return super.mouseDragged(QuestDetailsCoordinates.screenX(state, getPositionX()) + QuestTaskCardList.scrollbarX(w), mouseY, button, dragX, dragY);
                }
                if (super.mouseDragged(mouseX, mouseY, button, dragX, dragY)) {
                    return true;
                }
                int ly = QuestDetailsCoordinates.localY(state, mouseY, getPositionY(), h);
                if (QuestTaskListInteractions.handleDrag(player, state, refresh, questId, entries, kind, listY, h - bottomPad, ly, mouseX, mouseY, button)) {
                    return true;
                }
                return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
            }

            @Override
            public boolean mouseReleased(double mouseX, double mouseY, int button) {
                if (QuestTaskCardList.isTaskScrollDragging(state, kind)) {
                    return super.mouseReleased(QuestDetailsCoordinates.screenX(state, getPositionX()) + QuestTaskCardList.scrollbarX(w), mouseY, button);
                }
                if (super.mouseReleased(mouseX, mouseY, button)) {
                    return true;
                }
                if (QuestTaskListInteractions.handleRelease(player, state, refresh, questId, entries, kind)) {
                    return true;
                }
                return super.mouseReleased(mouseX, mouseY, button);
            }
        };
        String sectionKey = "tasks".equals(kind) ? "quest_details_task_section" : "quest_details_reward_section";
        SkinAnchorRegistry.register(sectionKey, section);
        section.setBackground(SurfaceFactory.insetPanel());
        return section;
    }

    private static void addChangeIconButton(WidgetGroup section, UiState state, Runnable refresh, String questId, String kind, int w) {
        if (!QuestDetailsEditController.canEdit(state)) return;
        String selectedKind = state.questDetails.questDetailsSelectedTaskKind;
        String selectedId = state.questDetails.questDetailsSelectedTaskId;
        if (!kind.equals(selectedKind) || selectedId == null || selectedId.isBlank()) return;
        boolean task = "tasks".equals(kind);
        String target = task ? ModalTargets.taskIcon(questId, selectedId) : ModalTargets.rewardIcon(questId, selectedId);
        var btn = IconOnlyButton.create(
                w - QuestTaskListPanel.HEADER_H,
                1,
                QuestTaskListPanel.HEADER_H - 2,
                "icon",
                ThemeColors.INTERACTIVE,
                click -> {
                    ContextMenuController.clearDeleteConfirm(state);
                    QuestDetailsWindow.openIconPicker(state, target);
                    refresh.run();
                }
        );
        btn.tooltips(new Component[]{TranslationKeys.component(QuestTranslationKeys.CONTEXT_CHANGE_ICON)});
        section.addWidget(btn);
    }

    private static boolean isCardBodyHit(int localX, int sectionW) {
        return localX >= 30 && localX <= sectionW - 44;
    }
}
