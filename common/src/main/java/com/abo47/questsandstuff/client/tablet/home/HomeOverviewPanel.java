package com.abo47.questsandstuff.client.tablet.home;

import java.util.Comparator;
import java.util.List;
import javax.annotation.Nonnull;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import com.abo47.questsandstuff.client.tablet.app.TabletAppDescriptor;
import com.abo47.questsandstuff.client.tablet.app.TabletAppRegistry;
import com.abo47.questsandstuff.client.tablet.bootstrap.Lifecycle;
import com.abo47.questsandstuff.client.tablet.icons.IconAtlas;
import com.abo47.questsandstuff.client.tablet.layout.PanelChrome;
import com.abo47.questsandstuff.client.tablet.theme.render.GlowShaderHelper;
import com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory;
import com.abo47.questsandstuff.client.tablet.theme.tokens.ThemeColors;

import static com.abo47.questsandstuff.client.tablet.theme.tokens.UiThemeTokens.*;
import static com.abo47.questsandstuff.client.tablet.ui.factory.UiFactory.ROOT_PAD_X;
import static com.abo47.questsandstuff.client.tablet.ui.factory.UiFactory.ROOT_PAD_Y;

public class HomeOverviewPanel extends WidgetGroup {
    private static final int HOME_BTN_SIZE = GRID_10;
    private static final int APP_ICON_SIZE = GRID_48;
    private final WidgetGroup innerContainer;

    private final ButtonWidget homeBtn;

    WidgetGroup getInnerContainer() {
        return innerContainer;
    }

    ButtonWidget getHomeBtn() {
        return homeBtn;
    }

    HomeOverviewPanel(int x, int y, int width, int height) {
        super(x, y, width, height);
        int innerW = Math.max(1, width - ROOT_PAD_X * 2);
        int innerH = Math.max(1, height - ROOT_PAD_Y * 2);
        int innerX = ROOT_PAD_X;
        int innerY = ROOT_PAD_Y;

        innerContainer = new WidgetGroup(innerX, innerY, innerW, innerH) {
            @Override
            public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
                PanelChrome.drawPanelChrome(graphics, this);
                drawWidgetsBackground(graphics, mouseX, mouseY, partialTicks);
                PanelChrome.drawPanelOutline(graphics, this);
            }
        };
        addWidget(innerContainer);

        int gutterX = innerX + innerW;
        int btnX = gutterX + (ROOT_PAD_X - HOME_BTN_SIZE) / 2;
        int btnY = innerY + (innerH - HOME_BTN_SIZE) / 2;
        homeBtn = new ButtonWidget(btnX, btnY, HOME_BTN_SIZE, HOME_BTN_SIZE,
                SurfaceFactory.bordered(ThemeColors.SURFACE_PANEL_ALT, ThemeColors.subtleBorder()),
                cd -> Lifecycle.closeTabletUi(null, false, "home_button"));
        homeBtn.setClientSideWidget();
        GlowShaderHelper.glowHit(homeBtn, Component.translatable("ui.questsandstuff.common.close"));
        homeBtn.setClickedTexture(SurfaceFactory.bordered(ThemeColors.SURFACE_PANEL_ALT, ThemeColors.BORDER_ACCENT));
        addWidget(homeBtn);

        List<TabletAppDescriptor> apps = TabletAppRegistry.all().values().stream()
                .filter(a -> !"home".equals(a.id()))
                .sorted(Comparator.comparingInt(HomeOverviewPanel::appAnchorOrder)
                        .thenComparing(TabletAppDescriptor::id))
                .toList();
        int appCount = apps.size();
        int iconPairW = APP_ICON_SIZE * appCount + Math.max(0, appCount - 1) * GRID_16;
        int iconsStartX = innerX + (innerW - iconPairW) / 2;
        int iconY = innerY + (innerH - APP_ICON_SIZE) / 2;

        int col = 0;
        for (TabletAppDescriptor app : apps) {
            int ix = iconsStartX + col * (APP_ICON_SIZE + GRID_16);
            IGuiTexture tex = app.iconKey() != null ? IconAtlas.iconTexture(app.iconKey()) : new ResourceTexture(app.iconTexture());
            ButtonWidget appBtn = new ButtonWidget(ix, iconY, APP_ICON_SIZE, APP_ICON_SIZE,
                    tex,
                    cd -> Lifecycle.openApp(app.id()));
            appBtn.setClientSideWidget();
            GlowShaderHelper.glowHit(appBtn, Component.translatable(app.translationKey()));
            appBtn.setClickedTexture(SurfaceFactory.group(tex, SurfaceFactory.fill(ThemeColors.pressedFill(ThemeColors.INTERACTIVE))));
            addWidget(appBtn);
            col++;
        }
    }

    private static int appAnchorOrder(TabletAppDescriptor app) {
        if ("QUESTS".equals(app.id())) return 0;
        if ("TEAMS".equals(app.id())) return 1;
        if ("CHUNKCLAIMER".equals(app.id())) return 2;
        if ("SETTINGS".equals(app.id())) return 3;
        return 99;
    }

    @Override
    public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        PanelChrome.drawRootChromeNoShadow(graphics, this);
        drawWidgetsBackground(graphics, mouseX, mouseY, partialTicks);
    }
}
