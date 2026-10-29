package com.abo47.questsandstuff.client.tablet.quest.details.task.interaction;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ProgressTexture;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.ProgressWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import com.abo47.questsandstuff.client.sync.state.ClientQuestStateFacade;
import com.abo47.questsandstuff.client.tablet.animation.ProgressAnimations;
import com.abo47.questsandstuff.client.tablet.controls.IconOnlyButton;
import com.abo47.questsandstuff.client.tablet.controls.TextTextures;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsWindow;
import com.abo47.questsandstuff.client.tablet.quest.details.task.QuestTaskEntry;
import com.abo47.questsandstuff.client.tablet.quest.details.task.model.QuestTaskSelectableRewards;
import com.abo47.questsandstuff.client.tablet.state.UiState;
import com.abo47.questsandstuff.client.tablet.text.QuestTranslationKeys;
import com.abo47.questsandstuff.client.tablet.text.TranslationKeys;
import com.abo47.questsandstuff.client.tablet.theme.render.GlowShaderHelper;
import com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory;
import com.abo47.questsandstuff.client.tablet.theme.skin.SkinAnchorRegistry;
import com.abo47.questsandstuff.client.tablet.theme.tokens.ThemeColors;
import com.abo47.questsandstuff.client.tablet.ui.factory.UiFactory;
import com.abo47.questsandstuff.network.ModNetwork;
import com.abo47.questsandstuff.network.quest.runtime.C2SManualTaskPacket;
import com.abo47.questsandstuff.network.quest.runtime.C2SManualXpSubmitPacket;

import static com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory.withAlpha;
import static com.abo47.questsandstuff.client.tablet.theme.tokens.UiThemeTokens.*;

public final class QuestTaskActionWidgets {
    private QuestTaskActionWidgets() {
    }

    public static void renderManualDoneButton(WidgetGroup parent, Player player, Runnable refresh, String questId, QuestTaskEntry entry, int x, int y, int w, boolean done) {
        int iconSize = 16;
        int iconX = x + Math.max(0, (w - iconSize) / 2);
        int iconY = y - GRID_1;
        if (done) {
            parent.addWidget(IconOnlyButton.icon(iconX, iconY, iconSize, "send-horizontal", ThemeColors.TEXT_MUTED));
            return;
        }
        var hit = IconOnlyButton.create(iconX, iconY, iconSize, "send-horizontal", ThemeColors.SUCCESS, click -> {
            ModNetwork.sendToServer(new C2SManualTaskPacket(questId, entry.id()));
            refresh.run();
        });
        parent.addWidget(hit.tooltips(new Component[]{TranslationKeys.component(QuestTranslationKeys.MARK_TASK_DONE)}));
    }

    public static void renderManualXpButton(WidgetGroup parent, Player player, Runnable refresh, String questId, QuestTaskEntry entry, int x, int y, int w, int count, int amount) {
        parent.addWidget(TextTextures.flatLiteral(x - 46, y, 42, 16, count + " / " + amount, ThemeColors.TEXT_PRIMARY, TextTexture.TextType.RIGHT_HIDE));
        boolean done = count >= amount;
        int iconSize = 16;
        int iconX = x + Math.max(0, (w - iconSize) / 2);
        int iconY = y - GRID_1;
        if (done) {
            parent.addWidget(IconOnlyButton.icon(iconX, iconY, iconSize, "send-horizontal", ThemeColors.TEXT_MUTED));
            return;
        }
        var hit = IconOnlyButton.create(iconX, iconY, iconSize, "send-horizontal", ThemeColors.SUCCESS, click -> {
            ModNetwork.sendToServer(new C2SManualXpSubmitPacket(questId, entry.id()));
            refresh.run();
        });
        parent.addWidget(hit.tooltips(new Component[]{TranslationKeys.component(QuestTranslationKeys.SUBMIT_XP_TASK)}));
    }

    public static void renderProgress(WidgetGroup section, UiState state, Player player, Runnable refresh, String questId, CompoundTag quest, int x, int y, int w, int h) {
        int barW = w;
        float progressValue = Math.max(0.0f, Math.min(1.0f, quest.getFloat("progress")));
        boolean locallyClaimed = state != null && questId.equals(state.questDetails.questDetailsClaimedOverrideQuestId);
        boolean claimed = quest.getBoolean("claimed") || locallyClaimed;
        boolean claimable = quest.getBoolean("completed") && !claimed;
        int fillColor = claimed ? ThemeColors.TEXT_MUTED : (claimable ? ThemeColors.WARNING : ThemeColors.SUCCESS);
        String progressKey = ProgressAnimations.key("details", questId);
        ProgressTexture texture = new ProgressTexture(
                IGuiTexture.EMPTY,
                SurfaceFactory.fill(withAlpha(fillColor, claimed ? 95 : 180))
        ).setFillDirection(ProgressTexture.FillDirection.LEFT_TO_RIGHT);
        ProgressWidget progressFill = new ProgressWidget(() -> ProgressAnimations.value(progressKey, progressValue), x, y, Math.max(1, barW), Math.max(1, h), texture);
        progressFill.setBackground(SurfaceFactory.bordered(ThemeColors.SURFACE_BASE, ThemeColors.BORDER_BASE));
        progressFill.setClientSideWidget();
        SkinAnchorRegistry.register("quest_details_progress", progressFill);
        section.addWidget(progressFill);
        String label = claimable
                ? TranslationKeys.text(QuestTranslationKeys.CLAIM)
                : (claimed ? TranslationKeys.text(QuestTranslationKeys.CLAIMED) : Math.round(progressValue * 100.0f) + "%");
        section.addWidget(TextTextures.flatLiteral(x, y, barW, h, label, claimed ? ThemeColors.TEXT_MUTED : ThemeColors.TEXT_PRIMARY, TextTexture.TextType.HIDE));
        if (claimable) {
            var hit = UiFactory.flatHitButton(x, y, barW, h, click -> {
                boolean hasSelectableReward = QuestTaskSelectableRewards.hasSelectableReward(quest);
                boolean canClaimNow = true;
                if (hasSelectableReward) {
                    canClaimNow = QuestTaskSelectableRewards.allSelectableRewardsSelected(quest, state)
                            && QuestTaskSelectableRewards.claimSelected(player, state, questId);
                } else {
                    QuestDetailsWindow.claimAll(player, questId);
                }
                if (canClaimNow) {
                    if (state != null) {
                        state.questDetails.questDetailsClaimedOverrideQuestId = questId;
                    }
                    ClientQuestStateFacade.setQuestClaimedLocal(questId, true);
                }
                refresh.run();
            });
            GlowShaderHelper.glowHit(hit, TranslationKeys.component(QuestTranslationKeys.CLAIM_ALL_REWARDS));
            hit.setClickedTexture(SurfaceFactory.fill(withAlpha(ThemeColors.SUCCESS, 80)));
            section.addWidget(hit);
        }
    }


}
