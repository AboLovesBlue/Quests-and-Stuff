package com.abo47.questsandstuff.client.tablet.controls;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import com.abo47.questsandstuff.client.tablet.icons.DisplayIconWidget;
import com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory;
import com.abo47.questsandstuff.client.tablet.theme.tokens.ThemeColors;
import com.abo47.questsandstuff.client.tablet.ui.factory.UiFactory;

import static com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory.withAlpha;
import static com.abo47.questsandstuff.client.tablet.theme.tokens.UiThemeTokens.*;

public final class CardDragGhosts {
    private CardDragGhosts() {
    }

    public static void renderChapter(WidgetGroup parent, int x, int y, int w, IGuiTexture background, String icon, String title) {
        renderBase(parent, x, y, w, UiFactory.CHAPTER_CARD_H, background, 0.0f);
        if (icon != null && !icon.isBlank()) {
            parent.addWidget(new DisplayIconWidget(x + GRID_4, y + GRID_8, UiFactory.CONTENT_ICON_SIZE, UiFactory.CONTENT_ICON_SIZE, icon));
        }
        parent.addWidget(UiFactory.label(x + 26, y + GRID_12, SearchFilter.crop(title, Math.max(4, (w - 34) / 7)), withAlpha(ThemeColors.TEXT_PRIMARY, 225)));
        renderVeil(parent, x, y, w, UiFactory.CHAPTER_CARD_H);
    }

    public static void renderTask(WidgetGroup parent, int x, int y, int w, int h, String icon, String title, String amount, float progress) {
        renderBase(parent, x, y, w, h, null, progress);
        if (icon != null && !icon.isBlank()) {
            parent.addWidget(new DisplayIconWidget(x + GRID_8, y + GRID_8, UiFactory.CONTENT_ICON_SIZE, UiFactory.CONTENT_ICON_SIZE, icon));
        }
        int textRight = amount == null || amount.isBlank() ? x + w - 8 : x + w - 38;
        int fieldW = Math.max(18, textRight - (x + 30));
        parent.addWidget(UiFactory.label(x + 30, y + 11, SearchFilter.crop(title, Math.max(4, fieldW / 7)), withAlpha(ThemeColors.TEXT_PRIMARY, 210)));
        if (amount != null && !amount.isBlank()) {
            parent.addWidget(UiFactory.label(x + w - 34, y + GRID_12, SearchFilter.crop(amount, 5), withAlpha(ThemeColors.TEXT_MUTED, 210)));
        }
        renderVeil(parent, x, y, w, h);
    }

    private static void renderBase(WidgetGroup parent, int x, int y, int w, int h, IGuiTexture background, float progress) {
        WidgetGroup card = UiFactory.panel(x, y, w, h, withAlpha(ThemeColors.SUCCESS, 58), ThemeColors.SUCCESS);
        parent.addWidget(card);
        if (background != null) {
            parent.addWidget(new ImageWidget(x + GRID_1, y + GRID_1, Math.max(1, w - GRID_2), Math.max(1, h - GRID_2), background));
        }
        int fillW = Math.round((w - GRID_2) * Math.max(0.0f, Math.min(1.0f, progress)));
        if (fillW > 0) {
            parent.addWidget(UiFactory.panel(x + GRID_1, y + GRID_1, Math.max(1, fillW), h - GRID_2, withAlpha(ThemeColors.SUCCESS, 62), ThemeColors.TRANSPARENT));
        }
    }

    private static void renderVeil(WidgetGroup parent, int x, int y, int w, int h) {
        WidgetGroup veil = new WidgetGroup(x, y, w, h);
        veil.setBackground(SurfaceFactory.bordered(withAlpha(ThemeColors.SUCCESS, 36), ThemeColors.SUCCESS));
        parent.addWidget(veil);
    }
}
