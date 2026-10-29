package com.abo47.questsandstuff.client.tablet.home;

import net.minecraft.world.entity.player.Player;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import com.abo47.questsandstuff.client.tablet.app.TabletAppRegistry;
import com.abo47.questsandstuff.client.tablet.modal.ModalDismissGuard;
import com.abo47.questsandstuff.client.tablet.modal.panel.ModalPanelRouter;
import com.abo47.questsandstuff.client.tablet.root.RootWidget;
import com.abo47.questsandstuff.client.tablet.state.UiState;
import com.abo47.questsandstuff.client.tablet.theme.codec.UiThemeManager;
import com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory;
import com.abo47.questsandstuff.client.tablet.theme.skin.SkinAnchorRegistry;
import com.abo47.questsandstuff.client.tablet.theme.skin.SkinEditManager;
import com.abo47.questsandstuff.client.tablet.theme.tokens.ThemeColors;
import com.abo47.questsandstuff.client.tablet.ui.factory.UiFactory;

import static com.abo47.questsandstuff.client.tablet.ui.factory.UiFactory.ROOT_H;
import static com.abo47.questsandstuff.client.tablet.ui.factory.UiFactory.ROOT_W;
import static com.abo47.questsandstuff.client.tablet.ui.factory.UiFactory.readPersistedSkinState;
import static com.abo47.questsandstuff.client.tablet.ui.factory.UiFactory.readPersistedUiState;

public final class HomeComposer {
    private HomeComposer() {
    }

    public static WidgetGroup create(Player player) {
        return create(player, ROOT_W, ROOT_H, false);
    }

    public static WidgetGroup create(Player player, int rootWidth, int rootHeight, boolean fullScreenMode) {
        UiThemeManager.activeThemeName();
        UiState state = new UiState();
        state.root.currentApp = TabletAppRegistry.APP_HOME;
        UiFactory.setActiveTabletState(state);
        int safeRootW = Math.max(1, rootWidth);
        int safeRootH = Math.max(1, rootHeight);
        UiFactory.applyRootSize(state, safeRootW, safeRootH, fullScreenMode);

        RootWidget root = new RootWidget(0, 0, safeRootW, safeRootH, state);
        root.setBackground(fullScreenMode
                ? SurfaceFactory.transparent()
                : SurfaceFactory.transparentBorder(ThemeColors.BORDER_BASE));

        Runnable[] refresh = new Runnable[1];
        WidgetGroup modalLayer = new ModalDismissGuard(0, 0, safeRootW, safeRootH, state, () -> refresh[0].run());
        HomeOverviewPanel homePanel = new HomeOverviewPanel(0, 0, safeRootW, safeRootH);
        WidgetGroup innerContainer = homePanel.getInnerContainer();

        refresh[0] = () -> {
            SkinAnchorRegistry.clear();
            ModalPanelRouter.rebuildChapterModal(modalLayer, state, player, refresh[0]);
            RootWidget.refreshRootBackground(root, state);
            homePanel.setBackground(SurfaceFactory.bordered(ThemeColors.SURFACE_BASE, ThemeColors.BORDER_BASE));
            innerContainer.setBackground((IGuiTexture) null);
            IGuiTexture rootOverrideTex = RootWidget.resolveRootFill(state);
            if (rootOverrideTex != null) {
                homePanel.setBackground(rootOverrideTex);
            }
            homePanel.getHomeBtn().setBackground(SurfaceFactory.bordered(ThemeColors.SURFACE_PANEL_ALT, ThemeColors.subtleBorder()));
            SkinAnchorRegistry.registerRootChrome(root, homePanel.getHomeBtn());
            SkinAnchorRegistry.register("home_close_btn", homePanel.getHomeBtn());
            SkinAnchorRegistry.register("home_inner", innerContainer);
            SkinEditManager.reapplyOverrides(state, root);
        };
        UiFactory.setActiveTabletRefresh(refresh[0]);
        root.setRefresher(refresh[0]);
        root.setModalLayer(modalLayer);

        root.addWidget(homePanel);
        root.addWidget(modalLayer);
        readPersistedUiState(state);
        readPersistedSkinState(state);
        refresh[0].run();
        return root;
    }
}
