package com.abo47.questsandstuff.client.tablet.quest.details.task;

import java.util.List;
import javax.annotation.Nonnull;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;

import com.lowdragmc.lowdraglib.gui.texture.ColorBorderTexture;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsCoordinates;
import com.abo47.questsandstuff.client.tablet.quest.details.task.interaction.QuestTaskListInteractions;
import com.abo47.questsandstuff.client.tablet.quest.details.task.model.QuestTaskSelectableRewards;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.render.GlowShaderHelper;
import com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory;
import com.abo47.questsandstuff.client.tablet.theme.skin.SkinAnchorRegistry;
import com.abo47.questsandstuff.client.tablet.theme.skin.SkinFillOverride;
import com.abo47.questsandstuff.client.tablet.theme.skin.SkinOverrideKey;
import com.abo47.questsandstuff.client.tablet.theme.tokens.TabletColors;

import static com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory.withAlpha;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.flatHitButton;

final class QuestTaskCardPanel {
    private QuestTaskCardPanel() {
    }

    static WidgetGroup cardPanel(TabletUiState state, Player player, Runnable refresh, String questId, List<QuestDetailsTaskEntry> entries, String kind, String id, int x, int y, int w, float progress, int listY, int listBottom, boolean selectableReward, boolean claimedReward, boolean claimChoiceEntry, boolean itemLocked) {
        WidgetGroup card = new WidgetGroup(x, y, w, QuestDetailsTasksPanel.CARD_H) {
            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                return false;
            }

            @Override
            public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
                int localY = listY + y + QuestDetailsCoordinates.localY(state, mouseY, getPositionY(), QuestDetailsTasksPanel.CARD_H);
                if (QuestTaskListInteractions.handleDrag(player, state, refresh, questId, entries, kind, listY, listBottom, localY, mouseX, mouseY, button)) {
                    return true;
                }
                return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
            }

            @Override
            public boolean mouseReleased(double mouseX, double mouseY, int button) {
                if (QuestTaskListInteractions.handleRelease(player, state, refresh, questId, entries, kind)) {
                    return true;
                }
                return super.mouseReleased(mouseX, mouseY, button);
            }

            @Override
            public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
                super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
                if (isMouseOverElement(mouseX, mouseY)) {
                    GlowShaderHelper.drawGlow(graphics, mouseX, mouseY, getPositionX(), getPositionY(), getSizeWidth(), getSizeHeight());
                }
            }
        };
        if (claimChoiceEntry && !claimedReward) {
            card.addWidget(flatHitButton(0, 0, w, QuestDetailsTasksPanel.CARD_H, click -> {
                QuestTaskSelectableRewards.selectChoice(state, id);
                state.questDetails.questDetailsTaskDragRefreshQueued = true;
            }));
        }
        boolean editSelected = TaskSections.normalize(kind).equals(state.questDetails.questDetailsSelectedTaskKind) && id.equals(state.questDetails.questDetailsSelectedTaskId);
        boolean claimSelected = claimChoiceEntry && QuestTaskSelectableRewards.isSelectedChoice(state, id);
        boolean selected = editSelected || claimSelected;
        int accent = selectableReward ? (claimSelected ? TabletColors.SUCCESS : TabletColors.WARNING)
                : itemLocked ? TabletColors.LOCKED : TabletColors.INTERACTIVE;

        String skinKey = TaskSections.isTask(kind) ? "quests_task_cards" : "quests_reward_cards";
        SkinAnchorRegistry.registerShared(skinKey, card);
        String rawOverride = SkinOverrideKey.resolveOverride(state, skinKey);
        if (rawOverride == null) {
            rawOverride = fallbackOverride(state, skinKey);
        }
        boolean skinned = rawOverride != null;
        if (skinned) {
            SkinFillOverride parsed = SkinFillOverride.parse(rawOverride);
            if (parsed != null) {
                IGuiTexture tex = parsed.createTexture();
                if (tex != null) {
                    card.addWidget(new ImageWidget(-1, -1, w + 2, QuestDetailsTasksPanel.CARD_H + 2, tex));
                }
            }
            card.setBackground(SurfaceFactory.transparentFill());
        } else {
            card.setBackground(SurfaceFactory.fill(TabletColors.elevatedSurface()));
        }

        if (claimedReward) {
            addCardFill(card, w, withAlpha(TabletColors.TEXT_MUTED, 34));
        } else if (selected || selectableReward) {
            addCardFill(card, w, withAlpha(accent, 82));
        } else if (itemLocked) {
            addCardFill(card, w, withAlpha(TabletColors.LOCKED, 46));
        }

        int borderColor = claimedReward ? TabletColors.subtleBorder() : (selected ? accent : TabletColors.subtleBorder());
        if (!skinned) {
            card.addWidget(borderQuad(w, borderColor));
        }

        int fillW = Math.round(w * Math.max(0.0f, Math.min(1.0f, progress)));
        if (fillW > 0) {
            WidgetGroup fill = new WidgetGroup(0, 0, Math.max(1, fillW), QuestDetailsTasksPanel.CARD_H);
            fill.setBackground(SurfaceFactory.fill(withAlpha(TabletColors.SUCCESS, 80)));
            card.addWidget(fill);
        }
        return card;
    }

    private static String fallbackOverride(TabletUiState state, String skinKey) {
        for (var entry : state.root.skinFillOverrides.entrySet()) {
            String entryKey = entry.getKey();
            if (skinKey.equals(entryKey)) return entry.getValue();
            String effective = entryKey.contains(":") ? entryKey.substring(entryKey.indexOf(':') + 1) : entryKey;
            if (skinKey.equals(effective)) return entry.getValue();
        }
        return null;
    }

    private static void addCardFill(WidgetGroup card, int w, int fill) {
        WidgetGroup g = new WidgetGroup(0, 0, w, QuestDetailsTasksPanel.CARD_H);
        g.setBackground(SurfaceFactory.fill(fill));
        card.addWidget(g);
    }

    private static WidgetGroup borderQuad(int w, int color) {
        WidgetGroup g = new WidgetGroup(0, 0, w, QuestDetailsTasksPanel.CARD_H);
        g.setBackground(new ColorBorderTexture(1, color));
        return g;
    }
}
