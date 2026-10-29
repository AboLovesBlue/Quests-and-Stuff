package com.abo47.questsandstuff.client.tablet.teams;

import net.minecraft.world.entity.player.Player;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import com.abo47.questsandstuff.client.tablet.app.TabletAppRegistry;
import com.abo47.questsandstuff.client.tablet.bootstrap.Bootstrap;
import com.abo47.questsandstuff.client.tablet.bootstrap.Lifecycle;
import com.abo47.questsandstuff.client.tablet.layout.SplitPanelLayout;
import com.abo47.questsandstuff.client.tablet.modal.ModalDismissGuard;
import com.abo47.questsandstuff.client.tablet.modal.ModalStateQueries;
import com.abo47.questsandstuff.client.tablet.modal.panel.ModalPanelRouter;
import com.abo47.questsandstuff.client.tablet.root.RootWidget;
import com.abo47.questsandstuff.client.tablet.state.UiState;
import com.abo47.questsandstuff.client.tablet.theme.render.GlowShaderHelper;
import com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory;
import com.abo47.questsandstuff.client.tablet.theme.skin.SkinAnchorRegistry;
import com.abo47.questsandstuff.client.tablet.theme.skin.SkinEditManager;
import com.abo47.questsandstuff.client.tablet.theme.tokens.ThemeColors;
import com.abo47.questsandstuff.network.ModNetwork;
import com.abo47.questsandstuff.network.team.C2STeamCreatePacket;

import static com.abo47.questsandstuff.client.tablet.theme.tokens.UiThemeTokens.*;
import static com.abo47.questsandstuff.client.tablet.ui.factory.UiFactory.BODY_X;
import static com.abo47.questsandstuff.client.tablet.ui.factory.UiFactory.BODY_Y;
import static com.abo47.questsandstuff.client.tablet.ui.factory.UiFactory.HEADER_H;
import static com.abo47.questsandstuff.client.tablet.ui.factory.UiFactory.ROOT_H;
import static com.abo47.questsandstuff.client.tablet.ui.factory.UiFactory.ROOT_PAD_X;
import static com.abo47.questsandstuff.client.tablet.ui.factory.UiFactory.ROOT_PAD_Y;
import static com.abo47.questsandstuff.client.tablet.ui.factory.UiFactory.ROOT_W;
import static com.abo47.questsandstuff.client.tablet.ui.factory.UiFactory.applyRootSize;
import static com.abo47.questsandstuff.client.tablet.ui.factory.UiFactory.chapterHeight;
import static com.abo47.questsandstuff.client.tablet.ui.factory.UiFactory.refreshActiveTablet;
import static com.abo47.questsandstuff.client.tablet.ui.factory.UiFactory.setActiveTabletRefresh;
import static com.abo47.questsandstuff.client.tablet.ui.factory.UiFactory.setActiveTabletState;
import static com.abo47.questsandstuff.client.tablet.ui.state.StateQueries.rootHeight;
import static com.abo47.questsandstuff.client.tablet.ui.state.StateQueries.rootWidth;

public final class TeamsAppComposer {
    private static final int HOME_BTN_SIZE = GRID_10;
    private static final int CONTENT_INSET = 6;
    private static final int LIST_INSET = 9;
    private static final int GUTTER = 6;
    private static final int HEADER_LIST_GAP = 5;

    private TeamsAppComposer() {
    }

    public static WidgetGroup create(Player player) {
        return create(player, ROOT_W, ROOT_H, false);
    }

    public static WidgetGroup create(Player player, int requestedRootW, int requestedRootH, boolean fullScreenMode) {
        UiState state = Bootstrap.prepare(player);
        Lifecycle.rememberMainWindow();
        state.root.currentApp = TabletAppRegistry.APP_TEAMS;
        applyRootSize(state, requestedRootW, requestedRootH, fullScreenMode);

        int initialRootW = rootWidth(state);
        int initialRootH = rootHeight(state);

        RootWidget root = new RootWidget(0, 0, initialRootW, initialRootH, state);
        root.setBackground(fullScreenMode
                ? SurfaceFactory.transparent()
                : SurfaceFactory.transparentBorder(ThemeColors.BORDER_BASE));

        int bodyH = chapterHeight(state);
        int bodyW = initialRootW - ROOT_PAD_X * 2;
        WidgetGroup mainPanel = SplitPanelLayout.leftPanel(BODY_X, BODY_Y, bodyW, bodyH, state);

        int homeBtnX = initialRootW - ROOT_PAD_X + (ROOT_PAD_X - HOME_BTN_SIZE) / 2;
        int homeBtnY = ROOT_PAD_Y + ((initialRootH - 2 * ROOT_PAD_Y) - HOME_BTN_SIZE) / 2;
        ButtonWidget homeBtn = new ButtonWidget(homeBtnX, homeBtnY, HOME_BTN_SIZE, HOME_BTN_SIZE,
                SurfaceFactory.bordered(ThemeColors.SURFACE_PANEL_ALT, ThemeColors.subtleBorder()),
                cd -> Lifecycle.openTabletUiHome(player));
        homeBtn.setClientSideWidget();
        GlowShaderHelper.glowHit(homeBtn);
        homeBtn.setClickedTexture(SurfaceFactory.bordered(ThemeColors.SURFACE_PANEL_ALT, ThemeColors.BORDER_ACCENT));
        root.addWidget(homeBtn);
        root.setHomeButton(homeBtn);

        final int headerY = CONTENT_INSET;
        TeamsAppHeaderControls headers = TeamsAppHeaderControls.create(state, () -> refreshActiveTablet(), headerY, bodyW);

        int listY = headerY + HEADER_H + HEADER_LIST_GAP;
        int listH = Math.max(1, bodyH - listY - GUTTER);
        WidgetGroup memberListPanel = new WidgetGroup(LIST_INSET, listY, bodyW - LIST_INSET * 2, listH);
        memberListPanel.setBackground(SurfaceFactory.bordered(ThemeColors.SURFACE_BASE, ThemeColors.BORDER_BASE));

        Runnable[] refresh = new Runnable[1];
        WidgetGroup modalLayer = new ModalDismissGuard(0, 0, initialRootW, initialRootH, state, () -> refresh[0].run());

        if (ClientTeamCache.INSTANCE.getTeam() == null) {
            ModNetwork.sendToServer(new C2STeamCreatePacket());
        }

        refresh[0] = () -> {
            SkinAnchorRegistry.clear();
            int crw = rootWidth(state);
            int crh = rootHeight(state);
            root.setSize(crw, crh);
            RootWidget.refreshRootBackground(root, state);

            int cbw = crw - ROOT_PAD_X * 2;
            int cbh = chapterHeight(state);
            mainPanel.setSize(cbw, cbh);

            int hbx = crw - ROOT_PAD_X + (ROOT_PAD_X - HOME_BTN_SIZE) / 2;
            int hby = ROOT_PAD_Y + ((crh - 2 * ROOT_PAD_Y) - HOME_BTN_SIZE) / 2;
            homeBtn.setSelfPosition(hbx, hby);
            homeBtn.setBackground(SurfaceFactory.bordered(ThemeColors.SURFACE_PANEL_ALT, ThemeColors.subtleBorder()));

            headers.layout(headerY, cbw);

            int clY = headerY + HEADER_H + HEADER_LIST_GAP;
            int clH = Math.max(1, cbh - clY - GUTTER);
            memberListPanel.setSize(cbw - LIST_INSET * 2, clH);
memberListPanel.setBackground(SurfaceFactory.bordered(ThemeColors.SURFACE_BASE, ThemeColors.BORDER_BASE));
            mainPanel.setBackground((IGuiTexture) null);

            ClientTeamCache.JoinResult joinResult = ClientTeamCache.INSTANCE.takePendingJoinResult();
            if (joinResult != null) {
                state.teams.inviteCodeMessage = joinResult.message();
                state.teams.inviteCodeMessageSuccess = joinResult.success();
                if (joinResult.success()) {
                    state.teams.inviteCodeModalOpen = false;
                }
            }

            ModalPanelRouter.rebuildChapterModal(modalLayer, state, player, refresh[0]);
            if (!ModalStateQueries.anyOpen(state) && !state.modal.modalWindowClosing) {
                TeamsInviteCodeModal.rebuild(modalLayer, state, refresh[0], crw, crh);
                TeamsConfirmModal.rebuild(modalLayer, state, refresh[0], crw, crh);
            }
            TeamsMemberCardRenderer.rebuildMemberList(memberListPanel, state, refresh[0]);
            SkinAnchorRegistry.registerRootChrome(root, homeBtn);
            SkinAnchorRegistry.register("teams_main_panel", mainPanel);
            SkinAnchorRegistry.register("teams_member_list", memberListPanel);
            SkinAnchorRegistry.register("teams_header_leave", headers.leaveBtn());
            SkinAnchorRegistry.register("teams_header_join", headers.joinBtn());
            SkinAnchorRegistry.register("teams_header_invite", headers.inviteBtn());
            SkinAnchorRegistry.register("teams_header_search", headers.searchField());
            SkinEditManager.reapplyOverrides(state, root);
        };

        setActiveTabletState(state);
        setActiveTabletRefresh(refresh[0]);
        root.setRefresher(refresh[0]);
        root.setModalLayer(modalLayer);

        root.addWidget(mainPanel);
        headers.addTo(mainPanel);
        mainPanel.addWidget(memberListPanel);
        root.addWidget(modalLayer);

        refresh[0].run();
        return root;
    }
}
