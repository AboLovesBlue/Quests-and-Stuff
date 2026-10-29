package com.abo47.questsandstuff.client.tablet.theme.skin;

import java.awt.Rectangle;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import com.lowdragmc.lowdraglib.gui.texture.ColorRectTexture;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import com.abo47.questsandstuff.client.tablet.modal.ModalStateQueries;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasViewport;
import com.abo47.questsandstuff.client.tablet.root.TabletRootWidget;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.render.GlowShaderHelper;
import com.abo47.questsandstuff.client.tablet.theme.tokens.TabletColors;

public final class SkinEditRenderer {
    private static final int DIM_COLOR = TabletColors.DIM_OVERLAY;
    private static final ColorRectTexture DIM_BG = new ColorRectTexture(DIM_COLOR);
    private static final int GLOW_COLOR = TabletColors.INTERACTIVE;
    private static final int SELECTED_GLOW_COLOR = TabletColors.SUCCESS;

    private SkinEditRenderer() {
    }

    public static void draw(GuiGraphics graphics, TabletRootWidget root, TabletUiState state, int mouseX, int mouseY) {
        if (ModalStateQueries.anyOpen(state)) return;
        Minecraft mc = Minecraft.getInstance();
        int screenW = mc.getWindow().getGuiScaledWidth();
        int screenH = mc.getWindow().getGuiScaledHeight();

        DIM_BG.draw(graphics, 0, 0, 0, 0, screenW, screenH);

        String selectedKey = state.root.skinEditSelectedTarget;
        if (!selectedKey.isBlank()) {
            drawSelectionGlow(graphics, root, state, selectedKey, SELECTED_GLOW_COLOR, mouseX, mouseY);
        } else {
            drawHoverGlow(graphics, root, state, mouseX, mouseY);
        }

        if (root.isContextMenuOpen()) {
            root.getContextMenuRoot().drawInBackground(graphics, mouseX, mouseY, mc.getFrameTime());
        }

        String tooltipKey = !selectedKey.isBlank() ? selectedKey : hoverKeyAt(root, state, mouseX, mouseY);
        if (tooltipKey != null) {
            int dw = 0, dh = 0;
            if (SkinTargetKeys.MINIMAP_BODY.equals(tooltipKey)) {
                dw = state.canvas.minimapPanelW + 1;
                dh = state.canvas.minimapPanelH + 1;
            } else if (SkinTargetKeys.MINIMAP_TOGGLE.equals(tooltipKey)) {
                dw = state.canvas.minimapToggleW + 1;
                dh = state.canvas.minimapToggleH + 1;
            } else {
                Widget tw = SkinEditTargetResolver.widgetForKey(root, tooltipKey);
                if (tw != null) {
                    dw = tw.getSizeWidth() + 1;
                    dh = tw.getSizeHeight() + 1;
                }
            }
            if (dw > 0 && dh > 0) {
                graphics.renderTooltip(mc.font, Component.literal(dw + " x " + dh), mouseX, mouseY);
            }
        }
    }

    private static void drawSelectionGlow(GuiGraphics graphics, TabletRootWidget root, TabletUiState state, String key, int glowColor, int mouseX, int mouseY) {
        if (SkinEditManager.isMinimapKey(key)) {
            drawMinimapGlow(graphics, root, state, key, glowColor, mouseX, mouseY);
            return;
        }
        Widget widget = SkinEditManager.selectedTargetWidget(root);
        if (widget == null && !SkinOverrideKey.isSharedKey(SkinOverrideKey.resolveTargetKey(state, key))) {
            widget = SkinEditTargetResolver.widgetForKey(root, key);
        }
        if (widget != null) {
            drawGlow(graphics, root, widget, glowColor, mouseX, mouseY);
        }
    }

    private static void drawHoverGlow(GuiGraphics graphics, TabletRootWidget root, TabletUiState state, int mouseX, int mouseY) {
        if (SkinEditManager.isMinimapHit(state, root, mouseX, mouseY)) {
            drawMinimapGlow(graphics, root, state, SkinEditManager.minimapHitKey(state, root, mouseX, mouseY), GLOW_COLOR, mouseX, mouseY);
            return;
        }
        Widget hoverWidget = SkinEditTargetResolver.findTargetWidgetAt(root, mouseX, mouseY);
        if (hoverWidget != null) {
            drawGlow(graphics, root, hoverWidget, GLOW_COLOR, mouseX, mouseY);
        }
    }

    private static void drawMinimapGlow(GuiGraphics graphics, TabletRootWidget root, TabletUiState state, String key, int glowColor, int mouseX, int mouseY) {
        int[] rect = SkinTargetKeys.MINIMAP_TOGGLE.equals(key)
                ? SkinMinimapRects.toggleRect(state, viewport(root))
                : SkinMinimapRects.bodyRect(state, viewport(root));
        if (rect == null) return;
        GlowShaderHelper.drawGlow(graphics, mouseX, mouseY, rect[0], rect[1], rect[2], rect[3], glowColor);
    }

    private static String hoverKeyAt(TabletRootWidget root, TabletUiState state, int mouseX, int mouseY) {
        if (SkinEditManager.isMinimapHit(state, root, mouseX, mouseY)) {
            return SkinEditManager.minimapHitKey(state, root, mouseX, mouseY);
        }
        return SkinEditTargetResolver.findTargetKeyAt(root, mouseX, mouseY);
    }

    private static void drawGlow(GuiGraphics graphics, WidgetGroup root, Widget widget, int glowColor, int mouseX, int mouseY) {
        List<Rectangle> ancestors = SkinEditTargetResolver.ancestorBounds(widget, root);
        List<Rectangle> nestedTargets = SkinEditTargetResolver.nestedTargetBounds(widget, root);
        GlowShaderHelper.drawGlowOccluded(graphics, mouseX, mouseY,
                widget.getPositionX(), widget.getPositionY(),
                widget.getSizeWidth(), widget.getSizeHeight(),
                glowColor, ancestors, nestedTargets);
    }

    private static CanvasViewport viewport(WidgetGroup root) {
        return root instanceof TabletRootWidget trw ? trw.getCanvasViewport() : null;
    }
}
