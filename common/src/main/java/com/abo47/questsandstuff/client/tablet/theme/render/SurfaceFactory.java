package com.abo47.questsandstuff.client.tablet.theme.render;

import com.lowdragmc.lowdraglib.gui.texture.ColorBorderTexture;
import com.lowdragmc.lowdraglib.gui.texture.ColorRectTexture;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import com.abo47.questsandstuff.client.tablet.theme.tokens.ThemeColors;
import com.abo47.questsandstuff.client.tablet.theme.tokens.UiThemeTokens;

public final class SurfaceFactory {
    private SurfaceFactory() {
    }

    public static ColorRectTexture fill(int color) {
        return new ColorRectTexture(color);
    }

    public static ColorRectTexture transparentFill() {
        return fill(ThemeColors.TRANSPARENT);
    }

    public static int withAlpha(int color, int alpha) {
        return UiThemeTokens.withAlpha(color, alpha);
    }

    public static GuiTextureGroup group(IGuiTexture... textures) {
        return new GuiTextureGroup(textures);
    }

    public static IGuiTexture transparent() {
        return IGuiTexture.EMPTY;
    }

    public static GuiTextureGroup bordered(int fillColor, int borderColor) {
        return group(fill(fillColor), new ColorBorderTexture(1, borderColor));
    }

    public static GuiTextureGroup transparentBorder(int borderColor) {
        return bordered(ThemeColors.TRANSPARENT, borderColor);
    }

    public static GuiTextureGroup panel() {
        return bordered(ThemeColors.SURFACE_PANEL, ThemeColors.BORDER_BASE);
    }

    public static GuiTextureGroup insetPanel() {
        return bordered(ThemeColors.recessedSurface(), ThemeColors.subtleBorder());
    }

    public static GuiTextureGroup raisedPanel() {
        return bordered(ThemeColors.elevatedSurface(), ThemeColors.BORDER_BASE);
    }

    public static GuiTextureGroup control() {
        return bordered(ThemeColors.SURFACE_PANEL_ALT, ThemeColors.BORDER_BASE);
    }

    public static GuiTextureGroup controlHover(int accentColor) {
        return bordered(ThemeColors.hoverFill(accentColor), ThemeColors.focusBorder());
    }

    public static GuiTextureGroup controlPressed(int accentColor) {
        return bordered(ThemeColors.pressedFill(accentColor), accentColor);
    }

    public static GuiTextureGroup card(boolean selected, int accentColor, boolean muted) {
        int fill = muted
                ? withAlpha(ThemeColors.TEXT_MUTED, 34)
                : (selected ? withAlpha(accentColor, 180) : ThemeColors.elevatedSurface());
        int border = muted ? ThemeColors.subtleBorder() : (selected ? accentColor : ThemeColors.subtleBorder());
        return bordered(fill, border);
    }

    public static WidgetGroup panel(int x, int y, int w, int h, int fillColor, int borderColor) {
        WidgetGroup panel = new WidgetGroup(x, y, w, h);
        panel.setBackground(bordered(fillColor, borderColor));
        return panel;
    }
}
