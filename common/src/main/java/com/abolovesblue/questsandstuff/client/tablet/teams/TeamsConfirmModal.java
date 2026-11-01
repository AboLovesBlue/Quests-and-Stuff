package com.abolovesblue.questsandstuff.client.tablet.teams;

import java.util.UUID;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;

import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import com.abolovesblue.questsandstuff.QuestsAndStuffConfig;
import com.abolovesblue.questsandstuff.client.tablet.animation.SourceOriginRevealWidget;
import com.abolovesblue.questsandstuff.client.tablet.controls.ActionButtons;
import com.abolovesblue.questsandstuff.client.tablet.state.UiState;
import com.abolovesblue.questsandstuff.client.tablet.text.TranslationKeys;
import com.abolovesblue.questsandstuff.client.tablet.theme.render.SurfaceFactory;
import com.abolovesblue.questsandstuff.client.tablet.theme.tokens.ThemeColors;
import com.abolovesblue.questsandstuff.network.ModNetwork;
import com.abolovesblue.questsandstuff.network.team.C2STeamActionPacket;
import com.abolovesblue.questsandstuff.network.team.C2STeamCreatePacket;

import static com.abolovesblue.questsandstuff.client.tablet.theme.render.SurfaceFactory.withAlpha;
import static com.abolovesblue.questsandstuff.client.tablet.theme.tokens.UiThemeTokens.*;
import static com.abolovesblue.questsandstuff.client.tablet.ui.factory.UiFactory.ROOT_H;
import static com.abolovesblue.questsandstuff.client.tablet.ui.factory.UiFactory.ROOT_W;
import static com.abolovesblue.questsandstuff.client.tablet.ui.factory.UiFactory.label;

final class TeamsConfirmModal {
    private static final int PAD = 8;
    private static final int BUTTON_W = 82;
    private static final int BUTTON_H = BUTTON_16;
    private static final int BUTTON_Y = 56;

    private TeamsConfirmModal() {
    }

    static void rebuild(WidgetGroup layer, UiState state, Runnable refresh, int layerW, int layerH) {
        if (!state.teams.confirmModalOpen) {
            return;
        }
        if (layerW <= 0) {
            layerW = state.root.tabletRootWidth > 0 ? state.root.tabletRootWidth : ROOT_W;
        }
        if (layerH <= 0) {
            layerH = state.root.tabletRootHeight > 0 ? state.root.tabletRootHeight : ROOT_H;
        }

        int w = 200;
        int h = 80;

        int px = Math.max(8, (layerW - w) / 2);
        int py = Math.max(24, (layerH - h) / 2);

        WidgetGroup panel = SurfaceFactory.panel(px, py, w, h,
                withAlpha(ThemeColors.SURFACE_BASE, 246), ThemeColors.BORDER_ACCENT);

        panel.addWidget(label(PAD, 10,
                I18n.get(state.teams.confirmTitleKey),
                ThemeColors.TEXT_PRIMARY));

        panel.addWidget(label(PAD, 28,
                I18n.get(state.teams.confirmMessageKey),
                ThemeColors.TEXT_MUTED));

        ActionButtons.iconAction(panel, PAD, BUTTON_Y, BUTTON_W, BUTTON_H,
                "close", TranslationKeys.text(TranslationKeys.COMMON_CANCEL), ThemeColors.ERROR, null, click -> {
            state.teams.confirmModalOpen = false;
            refresh.run();
        });

        ActionButtons.iconAction(panel, w - PAD - BUTTON_W, BUTTON_Y, BUTTON_W, BUTTON_H,
                "manual_check", TranslationKeys.text(TranslationKeys.COMMON_CONFIRM), ThemeColors.SUCCESS, null, click -> {
            state.teams.confirmModalOpen = false;
            UUID localUuid = Minecraft.getInstance().player == null
                    ? UUID.randomUUID() : Minecraft.getInstance().player.getUUID();
            switch (state.teams.confirmAction) {
                case "LEAVE":
                    ClientTeamCache.INSTANCE.clear();
                    ModNetwork.sendToServer(new C2STeamActionPacket(
                            C2STeamActionPacket.Action.LEAVE, localUuid));
                    ModNetwork.sendToServer(new C2STeamCreatePacket());
                    break;
                case "KICK":
                    ModNetwork.sendToServer(new C2STeamActionPacket(
                            C2STeamActionPacket.Action.KICK,
                            UUID.fromString(state.teams.confirmTargetUuid)));
                    break;
                case "TRANSFER":
                    ModNetwork.sendToServer(new C2STeamActionPacket(
                            C2STeamActionPacket.Action.TRANSFER,
                            UUID.fromString(state.teams.confirmTargetUuid)));
                    break;
            }
            refresh.run();
        });

        layer.addWidget(QuestsAndStuffConfig.popupWindowAnimationsEnabled()
                ? SourceOriginRevealWidget.windowNoShadow(panel,
                () -> state.teams.confirmAnimationStartMs, () -> true, () -> null)
                : panel);
    }
}
