package com.abo47.questsandstuff.client.tablet.quest.tools;

import net.minecraft.network.chat.Component;

import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import com.abo47.questsandstuff.client.tablet.controls.IconTextButton;
import com.abo47.questsandstuff.client.tablet.theme.tokens.ThemeColors;

import static com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory.withAlpha;

final class ToolButtons {
    private ToolButtons() {
    }

    static void addToggle(WidgetGroup menu, int x, int y, int size, int border, String icon, int iconColor, boolean positiveFill, Component[] tooltips, Runnable action) {
        int fill = positiveFill ? withAlpha(ThemeColors.SUCCESS, 62) : withAlpha(ThemeColors.ERROR, 62);
        menu.addWidget(IconTextButton.icon(x, y, size, size, icon, visuals(fill, border, iconColor), click -> action.run())
                .tooltips(tooltips));
    }

    static void addAction(WidgetGroup menu, int x, int y, int size, int border, String icon, int iconColor, Component[] tooltips, Runnable action) {
        menu.addWidget(IconTextButton.icon(x, y, size, size, icon, visuals(withAlpha(ThemeColors.SURFACE_PANEL_ALT, 164), border, iconColor), click -> action.run())
                .tooltips(tooltips));
    }

    static void addOpacity(WidgetGroup menu, int x, int y, int size, int border, String icon, Component[] tooltips, java.util.function.Consumer<Boolean> action) {
        menu.addWidget(IconTextButton.icon(x, y, size, size, icon, visuals(withAlpha(ThemeColors.SURFACE_PANEL_ALT, 164), border, ThemeColors.TEXT_PRIMARY), click -> action.accept(click.button == 1))
                .tooltips(tooltips));
    }

    private static IconTextButton.Visuals visuals(int fill, int border, int iconColor) {
        return new IconTextButton.Visuals(
                IconTextButton.State.of(fill, border, iconColor),
                IconTextButton.State.of(withAlpha(ThemeColors.INTERACTIVE, 66), ThemeColors.BORDER_ACCENT, iconColor),
                IconTextButton.State.of(withAlpha(ThemeColors.INTERACTIVE, 90), ThemeColors.BORDER_ACCENT, iconColor)
        );
    }
}
