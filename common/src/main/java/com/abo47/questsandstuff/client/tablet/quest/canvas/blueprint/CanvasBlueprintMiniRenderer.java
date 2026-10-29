package com.abo47.questsandstuff.client.tablet.quest.canvas.blueprint;

import java.util.Set;
import java.util.function.Supplier;
import javax.annotation.Nonnull;

import net.minecraft.client.gui.GuiGraphics;

import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasViewport;
import com.abo47.questsandstuff.client.tablet.quest.canvas.geometry.CanvasGeometry;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.CanvasRenderer;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.quest.editor.blueprint.CanvasBlueprint;

public final class CanvasBlueprintMiniRenderer {
    private CanvasBlueprintMiniRenderer() {
    }

    public static WidgetGroup previewWidget(int x, int y, int w, int h, CanvasBlueprint blueprint) {
        return previewWidget(x, y, w, h, blueprint, Set.of(), Set.of());
    }

    public static WidgetGroup previewWidget(int x, int y, int w, int h, CanvasBlueprint blueprint, Set<String> highlightedQuestIds, Set<String> highlightedConnectionKeys) {
        Set<String> safeQuestIds = safeSet(highlightedQuestIds);
        Set<String> safeConnectionKeys = safeSet(highlightedConnectionKeys);
        return previewWidget(x, y, w, h, blueprint, () -> safeQuestIds, () -> safeConnectionKeys);
    }

    public static WidgetGroup previewWidget(int x, int y, int w, int h, CanvasBlueprint blueprint, Supplier<Set<String>> highlightedQuestIds, Supplier<Set<String>> highlightedConnectionKeys) {
        return new WidgetGroup(x, y, w, h) {
            @Override
            public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
                CanvasBlueprintMiniPainter.drawPreview(
                        graphics,
                        mouseX,
                        mouseY,
                        getPositionX(),
                        getPositionY(),
                        getSizeWidth(),
                        getSizeHeight(),
                        blueprint,
                        safeSet(highlightedQuestIds == null ? null : highlightedQuestIds.get()),
                        safeSet(highlightedConnectionKeys == null ? null : highlightedConnectionKeys.get()),
                        partialTicks
                );
            }
        };
    }

    private static Set<String> safeSet(Set<String> values) {
        return values == null || values.isEmpty() ? Set.of() : Set.copyOf(values);
    }

    public static WidgetGroup placementGhost(CanvasViewport canvasViewport, TabletUiState state) {
        return new WidgetGroup(0, 0, canvasViewport.getSizeWidth(), canvasViewport.getSizeHeight()) {
            @Override
            public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
                CanvasBlueprint blueprint = CanvasBlueprintStore.read(state.canvas.blueprintPlacement.asset());
                if (blueprint.isEmpty()) {
                    return;
                }
                int originX = getPositionX();
                int originY = getPositionY();
                int localX = mouseX - originX;
                int localY = mouseY - originY;
                CanvasBlueprintController.PlacementAnchor anchor = CanvasBlueprintController.placementAnchor(state, blueprint, localX, localY);
                int anchorScreenX = originX + CanvasGeometry.screenX(state, anchor.x());
                int anchorScreenY = originY + CanvasGeometry.screenY(state, anchor.y());
                CanvasBlueprintMiniPainter.drawBlueprint(graphics, mouseX, mouseY, anchorScreenX, anchorScreenY, CanvasRenderer.clampZoom(state.canvas.canvasZoom), blueprint, partialTicks, 150);
            }
        };
    }
}
