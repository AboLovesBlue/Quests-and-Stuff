package com.abo47.questsandstuff.client.tablet.quest.details;

import javax.annotation.Nonnull;

import net.minecraft.client.gui.GuiGraphics;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import com.abo47.questsandstuff.client.tablet.quest.canvas.render.CanvasWorldPortalCapture;
import com.abo47.questsandstuff.client.tablet.state.UiState;
import com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory;
import com.abo47.questsandstuff.client.tablet.theme.tokens.ThemeColors;
import com.abo47.questsandstuff.client.tablet.ui.factory.UiFactory;
import com.abo47.questsandstuff.client.tablet.ui.state.ActiveState;

public final class QuestDetailsRootWidget extends WidgetGroup {
    public QuestDetailsRootWidget(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    @Override
    public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        CanvasWorldPortalCapture.captureMainCanvas(ActiveState.getActiveTabletState());
        UiState state = ActiveState.getActiveTabletState();
        int rx = getPositionX(), ry = getPositionY(), rw = getSizeWidth(), rh = getSizeHeight();
        int bx, by, bw, bh;
        if (state.root.fullScreenMode) {
            bw = UiFactory.ROOT_W;
            bh = UiFactory.ROOT_H;
            bx = rx + (rw - bw) / 2;
            by = ry + (rh - bh) / 2;
        } else {
            bx = rx;
            by = ry;
            bw = rw;
            bh = rh;
        }
        IGuiTexture bg = getBackgroundTexture();
        if (bg != null && !bg.equals(IGuiTexture.EMPTY)) {
            bg.draw(graphics, 0, 0, bx, by, bw, bh);
        } else {
            SurfaceFactory.bordered(ThemeColors.SURFACE_BASE, ThemeColors.BORDER_BASE)
                    .draw(graphics, mouseX, mouseY, bx, by, bw, bh);
        }
        drawWidgetsBackground(graphics, mouseX, mouseY, partialTicks);
    }
}
