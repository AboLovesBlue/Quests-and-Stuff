package com.abo47.questsandstuff.client.tablet.layout;

import javax.annotation.Nonnull;

import net.minecraft.client.gui.GuiGraphics;

import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import com.abo47.questsandstuff.client.tablet.state.UiState;

import static com.abo47.questsandstuff.client.tablet.layout.PanelChrome.drawCanvasPanelChrome;
import static com.abo47.questsandstuff.client.tablet.layout.PanelChrome.drawCanvasPanelOutlines;
import static com.abo47.questsandstuff.client.tablet.layout.PanelChrome.drawPanelOutline;
import static com.abo47.questsandstuff.client.tablet.ui.factory.UiFactory.GAP;
import static com.abo47.questsandstuff.client.tablet.ui.factory.UiFactory.SPLITTER_W;

public final class SplitPanelLayout {
    private SplitPanelLayout() {
    }

    public static int splitterX(int leftX, int leftWidth) {
        return leftX + leftWidth + Math.max(0, (GAP - SPLITTER_W) / 2);
    }

    public static int rightPanelX(int leftX, int leftWidth) {
        return leftX + leftWidth + GAP;
    }

    public static WidgetGroup leftPanel(int x, int y, int w, int h, UiState state) {
        return new WidgetGroup(x, y, w, h) {
            @Override
            public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
                PanelChrome.resolveFill(this).draw(graphics, mouseX, mouseY, getPositionX(), getPositionY(), getSizeWidth(), getSizeHeight());
                drawWidgetsBackground(graphics, mouseX, mouseY, partialTicks);
                drawPanelOutline(graphics, this, state);
            }
        };
    }

    public static WidgetGroup rightPanel(int x, int y, int w, int h, UiState state) {
        return new WidgetGroup(x, y, w, h) {
            @Override
            public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
                drawCanvasPanelChrome(graphics, this, state);
                drawWidgetsBackground(graphics, mouseX, mouseY, partialTicks);
                drawCanvasPanelOutlines(graphics, this, state);
            }
        };
    }

    public static WidgetGroup rightPanel(int x, int y, int w, int h,
                                          int viewportX, int viewportY, int viewportW, int viewportH,
                                          boolean canEdit, boolean showGrid,
                                          int gridOpacity, int gridColor,
                                          @javax.annotation.Nullable UiState state) {
        return new WidgetGroup(x, y, w, h) {
            @Override
            public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
                drawCanvasPanelChrome(graphics, this, viewportX, viewportY, viewportW, viewportH, state);
                drawWidgetsBackground(graphics, mouseX, mouseY, partialTicks);
                drawCanvasPanelOutlines(graphics, this, state, viewportX, viewportY, viewportW, viewportH, canEdit, showGrid, gridOpacity, gridColor);
            }
        };
    }
}
