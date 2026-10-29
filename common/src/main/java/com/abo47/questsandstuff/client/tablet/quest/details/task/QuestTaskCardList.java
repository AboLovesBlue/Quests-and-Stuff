package com.abo47.questsandstuff.client.tablet.quest.details.task;

import java.util.List;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;

import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import com.abo47.questsandstuff.client.tablet.controls.DragScrollBarWidget;
import com.abo47.questsandstuff.client.tablet.controls.ScrollMath;
import com.abo47.questsandstuff.client.tablet.quest.canvas.viewport.CanvasViewportScissor;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsCoordinates;
import com.abo47.questsandstuff.client.tablet.quest.details.task.interaction.QuestTaskListInteractions;
import com.abo47.questsandstuff.client.tablet.quest.details.task.model.QuestTaskSelectableRewards;
import com.abo47.questsandstuff.client.tablet.state.UiState;
import com.abo47.questsandstuff.client.tablet.theme.skin.SkinAnchorRegistry;

final class QuestTaskCardList {
    private QuestTaskCardList() {
    }

    static boolean isTaskScrollDragging(UiState state, String kind) {
        return "tasks".equals(kind) ? state.questDetails.questDetailsTaskScrollDragging : state.questDetails.questDetailsRewardScrollDragging;
    }

    static int scrollbarX(int sectionW) {
        return Math.max(0, sectionW - DragScrollBarWidget.RESERVED_WIDTH / 2);
    }

    static void renderCards(WidgetGroup section, UiState state, Player player, Runnable refresh, String questId, List<QuestTaskEntry> entries, int w, int h, int listY, boolean tasks) {
        renderCards(section, state, player, refresh, questId, entries, w, h, listY, tasks, false);
    }

    static void renderCards(WidgetGroup section, UiState state, Player player, Runnable refresh, String questId, List<QuestTaskEntry> entries, int w, int h, int listY, boolean tasks, boolean rewardsClaimed) {
        int visibleH = Math.max(1, h - listY - 4);
        int maxStart = scrollMax(entries, visibleH);
        if (tasks) {
            state.questDetails.questDetailsTaskScroll = ScrollMath.clamp(state.questDetails.questDetailsTaskScroll, maxStart);
        } else {
            state.questDetails.questDetailsRewardScroll = ScrollMath.clamp(state.questDetails.questDetailsRewardScroll, maxStart);
        }
        String kind = tasks ? "tasks" : "rewards";
        String cardsKey = tasks ? "quests_task_cards" : "quests_reward_cards";
        SkinAnchorRegistry.clearShared(cardsKey);
        int listW = maxStart > 0 ? w - DragScrollBarWidget.RESERVED_WIDTH : w;
        WidgetGroup list = clippedList(0, listY, listW, visibleH, state, player, refresh, questId, entries, kind, listY, h - 4);
        section.addWidget(list);
        int scroll = tasks ? state.questDetails.questDetailsTaskScroll : state.questDetails.questDetailsRewardScroll;
        if (entries.isEmpty()) {
            return;
        }
        int cardW = maxStart > 0 ? w - 12 - DragScrollBarWidget.RESERVED_WIDTH : w - 12;
        int y = QuestTaskListPanel.LIST_PAD - scroll;
        int ghostIndex = Math.max(0, Math.min(entries.size(), state.questDetails.questDetailsTaskDragTargetIndex));
        boolean showGhost = state.questDetails.questDetailsTaskDragActive
                && kind.equals(state.questDetails.questDetailsTaskDragKind)
                && !state.questDetails.questDetailsTaskDragId.isBlank();
        QuestTaskEntry ghostEntry = draggedEntry(entries, state.questDetails.questDetailsTaskDragId);
        for (int index = 0; index < entries.size(); index++) {
            if (showGhost && index == ghostIndex) {
                QuestTaskGhostCards.render(list, ghostEntry, tasks, 6, y, cardW);
                y += QuestTaskListPanel.CARD_H + QuestTaskListPanel.CARD_GAP;
            }
            QuestTaskEntry entry = entries.get(index);
            if (y < visibleH && y + QuestTaskListPanel.CARD_H > 0) {
                if (tasks) {
                    QuestTaskCardRenderer.renderTaskCard(list, state, player, refresh, questId, entry, 6, y, cardW, entries, listY, h - 4);
                } else {
                    QuestTaskCardRenderer.renderRewardCard(list, state, player, refresh, questId, entry, 6, y, cardW, entries, listY, h - 4, rewardsClaimed);
                }
            }
            y += QuestTaskListPanel.CARD_H + QuestTaskListPanel.CARD_GAP;
        }
        if (showGhost && ghostIndex == entries.size()) {
            QuestTaskGhostCards.render(list, ghostEntry, tasks, 6, y, cardW);
        }
        renderScrollbar(section, state, refresh, tasks, w, listY, visibleH, maxStart);
    }

    private static QuestTaskEntry draggedEntry(List<QuestTaskEntry> entries, String id) {
        for (QuestTaskEntry entry : entries) {
            if (entry.id().equals(id)) {
                return entry;
            }
        }
        return null;
    }

    private static WidgetGroup clippedList(int x, int y, int w, int h, UiState state, Player player, Runnable refresh, String questId, List<QuestTaskEntry> entries, String kind, int listY, int listBottom) {
        return new WidgetGroup(x, y, w, h) {
            @Override
            public boolean isMouseOverElement(double mouseX, double mouseY) {
                return inside(mouseX, mouseY);
            }

            @Override
            public boolean mouseWheelMove(double mouseX, double mouseY, double wheelDelta) {
                return inside(mouseX, mouseY) && super.mouseWheelMove(mouseX, mouseY, wheelDelta);
            }

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                return inside(mouseX, mouseY) && super.mouseClicked(mouseX, mouseY, button);
            }

            @Override
            public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
                int localY = QuestDetailsCoordinates.localY(state, mouseY, getPositionY(), h) + listY;
                if (QuestTaskListInteractions.handleDrag(player, state, refresh, questId, entries, kind, listY, listBottom, localY, mouseX, mouseY, button)) {
                    return true;
                }
                return inside(mouseX, mouseY) && super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
            }

            @Override
            public boolean mouseReleased(double mouseX, double mouseY, int button) {
                if (QuestTaskListInteractions.handleRelease(player, state, refresh, questId, entries, kind)) {
                    return true;
                }
                return inside(mouseX, mouseY) && super.mouseReleased(mouseX, mouseY, button);
            }

            @Override
            public void drawInBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
                CanvasViewportScissor.draw(graphics, getPositionX(), getPositionY(), getSizeWidth(), getSizeHeight(),
                        () -> drawWidgetsBackground(graphics, mouseX, mouseY, partialTicks));
            }

            @Override
            public void drawInForeground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
                CanvasViewportScissor.draw(graphics, getPositionX(), getPositionY(), getSizeWidth(), getSizeHeight(),
                        () -> drawWidgetsForeground(graphics, mouseX, mouseY, partialTicks));
            }

            @Override
            public void drawOverlay(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
                CanvasViewportScissor.draw(graphics, getPositionX(), getPositionY(), getSizeWidth(), getSizeHeight(),
                        () -> super.drawOverlay(graphics, mouseX, mouseY, partialTicks));
            }

            private boolean inside(double mouseX, double mouseY) {
                int left = getPositionX();
                int top = getPositionY();
                return mouseX >= left && mouseX < left + getSizeWidth() && mouseY >= top && mouseY < top + getSizeHeight();
            }
        };
    }

    static int scrollMax(List<QuestTaskEntry> entries, int visibleH) {
        if (entries.isEmpty()) {
            return 0;
        }
        int contentH = QuestTaskListPanel.LIST_PAD * 2
                + entries.size() * QuestTaskListPanel.CARD_H
                + Math.max(0, entries.size() - 1) * QuestTaskListPanel.CARD_GAP;
        return Math.max(0, contentH - visibleH);
    }

    private static void renderScrollbar(WidgetGroup section, UiState state, Runnable refresh, boolean tasks, int w, int y, int h, int maxStart) {
        if (maxStart <= 0) {
            return;
        }
        int knobH = Math.max(12, Math.min(h, Math.round((float) h * ((float) h / (float) (h + maxStart)))));
        section.addWidget(new DragScrollBarWidget(
                w - DragScrollBarWidget.RESERVED_WIDTH,
                y,
                DragScrollBarWidget.RESERVED_WIDTH,
                h,
                () -> tasks ? state.questDetails.questDetailsTaskScroll : state.questDetails.questDetailsRewardScroll,
                () -> maxStart,
                () -> knobH,
                value -> {
                    if (tasks) {
                        state.questDetails.questDetailsTaskScroll = value;
                    } else {
                        state.questDetails.questDetailsRewardScroll = value;
                    }
                },
                () -> tasks ? state.questDetails.questDetailsTaskScrollDragging : state.questDetails.questDetailsRewardScrollDragging,
                dragging -> {
                    if (tasks) {
                        state.questDetails.questDetailsTaskScrollDragging = dragging;
                    } else {
                        state.questDetails.questDetailsRewardScrollDragging = dragging;
                    }
                },
                refresh,
                DragScrollBarWidget.WIDTH
        ));
    }

    static QuestTaskEntry hitEntry(UiState state, List<QuestTaskEntry> entries, String kind, int listY, int listBottom, int localY) {
        if (localY < listY || localY > listBottom || entries.isEmpty()) {
            return null;
        }
        int scroll = "tasks".equals(kind) ? state.questDetails.questDetailsTaskScroll : state.questDetails.questDetailsRewardScroll;
        int contentY = localY - listY + scroll - QuestTaskListPanel.LIST_PAD;
        if (contentY < 0) {
            return null;
        }
        int slot = contentY / (QuestTaskListPanel.CARD_H + QuestTaskListPanel.CARD_GAP);
        int inSlot = contentY % (QuestTaskListPanel.CARD_H + QuestTaskListPanel.CARD_GAP);
        if (slot < 0 || slot >= entries.size() || inSlot >= QuestTaskListPanel.CARD_H) {
            return null;
        }
        return entries.get(slot);
    }
}
