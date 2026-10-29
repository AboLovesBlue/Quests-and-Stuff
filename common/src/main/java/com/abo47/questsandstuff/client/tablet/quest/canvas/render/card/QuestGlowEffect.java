package com.abo47.questsandstuff.client.tablet.quest.canvas.render.card;

import javax.annotation.Nonnull;

import net.minecraft.client.gui.GuiGraphics;

import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import com.abo47.questsandstuff.client.tablet.quest.canvas.interaction.CanvasInteractionGate;
import com.abo47.questsandstuff.client.tablet.state.UiState;
import com.abo47.questsandstuff.client.tablet.theme.render.GlowShaderHelper;

public final class QuestGlowEffect {
    public static WidgetGroup overlay(int x, int y, int w, int h, UiState state) {
        return new WidgetGroup(x, y, w, h) {
            @Override
            public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
                int px = getPositionX();
                int py = getPositionY();
                if (mouseX < px || mouseX > px + w || mouseY < py || mouseY > py + h) {
                    return;
                }
                if (!CanvasInteractionGate.hoverAllowed(state, this, mouseX, mouseY)) {
                    return;
                }
                GlowShaderHelper.drawGlow(graphics, mouseX, mouseY, px, py, w, h);
            }
        };
    }
}
