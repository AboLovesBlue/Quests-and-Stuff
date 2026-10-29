package com.abo47.questsandstuff.client.tablet.quest.canvas.overlay;

import javax.annotation.Nonnull;

import net.minecraft.client.gui.GuiGraphics;

import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import com.abo47.questsandstuff.QuestsAndStuffConfig;
import com.abo47.questsandstuff.client.tablet.animation.UiAnimationProgress;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasViewport;
import com.abo47.questsandstuff.client.tablet.quest.canvas.viewport.CanvasMinimapController;
import com.abo47.questsandstuff.client.tablet.quest.canvas.viewport.CanvasMinimapGeometry;
import com.abo47.questsandstuff.client.tablet.quest.canvas.viewport.CanvasViewportScissor;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;

import static com.abo47.questsandstuff.client.tablet.ui.state.TabletStateQueries.selectedChapterName;

final class CanvasMinimapOverlay {
    private CanvasMinimapOverlay() {
    }

    static void render(
            CanvasViewport canvasViewport,
            TabletUiState state
    ) {
        clearState(state);
        if (!QuestsAndStuffConfig.minimapEnabled()) {
            return;
        }
        CanvasMinimapGeometry.Layout hitLayout = CanvasMinimapGeometry.layout(canvasViewport.getSizeWidth(), canvasViewport.getSizeHeight(), state.canvas.minimapCollapsed);
        applyLayout(state, hitLayout);
        if (hitLayout.panelW() <= 0 || hitLayout.panelH() <= 0) {
            return;
        }

        boolean animationsEnabled = QuestsAndStuffConfig.minimapAnimationsEnabled();
        boolean closing = CanvasMinimapController.isClosingAnimationRunning(state);
        if (hitLayout.collapsed() && !closing) {
            canvasViewport.addWidget(minimapWidget(canvasViewport, state, animationsEnabled, null, hitLayout, hitLayout));
            return;
        }

        CanvasMinimapGeometry.Layout layout = hitLayout.collapsed()
                ? CanvasMinimapGeometry.layout(canvasViewport.getSizeWidth(), canvasViewport.getSizeHeight(), false)
                : hitLayout;
        String chapter = selectedChapterName(state);
        CanvasMinimapGeometry.WorldBounds world = CanvasMinimapGeometry.worldBounds(state, chapter, canvasViewport.cardCache());
        CanvasMinimapGeometry.Projection projection = CanvasMinimapGeometry.projection(layout, world);
        if (!state.canvas.minimapCollapsed) {
            applyProjection(state, projection);
        }

        CanvasMinimapGeometry.Layout collapsedLayout = CanvasMinimapGeometry.layout(canvasViewport.getSizeWidth(), canvasViewport.getSizeHeight(), true);
        canvasViewport.addWidget(minimapWidget(canvasViewport, state, animationsEnabled, chapter, layout, collapsedLayout));
    }

    private static WidgetGroup minimapWidget(
            CanvasViewport canvasViewport,
            TabletUiState state,
            boolean animationsEnabled,
            String chapter,
            CanvasMinimapGeometry.Layout layout,
            CanvasMinimapGeometry.Layout collapsedLayout
    ) {
        return new WidgetGroup(0, 0, canvasViewport.getSizeWidth(), canvasViewport.getSizeHeight()) {
            private CanvasMinimapSnapshot lastSnapshot;

            @Override
            public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
                int originX = getPositionX();
                int originY = getPositionY();
                float openProgress = minimapOpenProgress(state, animationsEnabled);
                float holderProgress = CanvasMinimapPainter.stagedProgress(openProgress, 0.0f, CanvasMinimapPainter.BODY_REVEAL_START);
                float bodyProgress = CanvasMinimapPainter.stagedProgress(openProgress, CanvasMinimapPainter.BODY_REVEAL_START, 1.0f);
                CanvasMinimapPainter.drawPanel(graphics, state, originX, originY, layout, collapsedLayout, holderProgress, bodyProgress, mouseX, mouseY);
                if (bodyProgress > 0.02f) {
                    if (chapter != null && layout != null) {
                        CanvasMinimapGeometry.WorldBounds world = CanvasMinimapGeometry.worldBounds(state, chapter, canvasViewport.cardCache());
                        CanvasMinimapGeometry.Projection projection = CanvasMinimapGeometry.projection(layout, world);
                        lastSnapshot = CanvasMinimapSnapshotBuilder.snapshot(state, chapter, canvasViewport.cardCache(), canvasViewport.cardLookup(), projection);
                    }
                    if (lastSnapshot != null) {
                        int clipW = Math.max(1, Math.round((layout.panelW() - layout.toggleW()) * bodyProgress));
                        int clipX = layout.toggleX() - clipW;
                        CanvasViewportScissor.draw(
                                graphics,
                                originX + clipX,
                                originY + layout.panelY(),
                                clipW,
                                layout.panelH(),
                                () -> CanvasMinimapPainter.drawSnapshot(graphics, state, lastSnapshot, originX, originY, mouseX, mouseY, partialTicks)
                        );
                    }
                }
            }
        };
    }

    private static float minimapOpenProgress(TabletUiState state, boolean animationsEnabled) {
        if (!animationsEnabled) {
            return state.canvas.minimapCollapsed ? 0.0f : 1.0f;
        }
        return UiAnimationProgress.openProgress(
                !state.canvas.minimapCollapsed,
                state.canvas.minimapAnimationFromCollapsed,
                state.canvas.minimapAnimationStartMs,
                CanvasMinimapController.ANIMATION_MS
        );
    }

    private static void clearState(TabletUiState state) {
        state.canvas.minimapPanelX = 0;
        state.canvas.minimapPanelY = 0;
        state.canvas.minimapPanelW = 0;
        state.canvas.minimapPanelH = 0;
        state.canvas.minimapToggleX = 0;
        state.canvas.minimapToggleY = 0;
        state.canvas.minimapToggleW = 0;
        state.canvas.minimapToggleH = 0;
        state.canvas.minimapX = 0;
        state.canvas.minimapY = 0;
        state.canvas.minimapW = 0;
        state.canvas.minimapH = 0;
    }

    private static void applyLayout(TabletUiState state, CanvasMinimapGeometry.Layout layout) {
        state.canvas.minimapPanelX = layout.panelX();
        state.canvas.minimapPanelY = layout.panelY();
        state.canvas.minimapPanelW = layout.panelW();
        state.canvas.minimapPanelH = layout.panelH();
        state.canvas.minimapToggleX = layout.toggleX();
        state.canvas.minimapToggleY = layout.toggleY();
        state.canvas.minimapToggleW = layout.toggleW();
        state.canvas.minimapToggleH = layout.toggleH();
    }

    private static void applyProjection(TabletUiState state, CanvasMinimapGeometry.Projection projection) {
        state.canvas.minimapX = projection.drawX();
        state.canvas.minimapY = projection.drawY();
        state.canvas.minimapW = projection.drawW();
        state.canvas.minimapH = projection.drawH();
        state.canvas.minimapWorldMinX = projection.world().minX();
        state.canvas.minimapWorldMinY = projection.world().minY();
        state.canvas.minimapWorldWidth = projection.world().width();
        state.canvas.minimapWorldHeight = projection.world().height();
    }
}
