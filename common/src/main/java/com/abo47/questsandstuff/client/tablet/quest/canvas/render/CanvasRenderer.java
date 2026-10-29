package com.abo47.questsandstuff.client.tablet.quest.canvas.render;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;

import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasViewport;
import com.abo47.questsandstuff.client.tablet.quest.canvas.geometry.CanvasGeometry;
import com.abo47.questsandstuff.client.tablet.quest.canvas.geometry.CanvasLayoutService;
import com.abo47.questsandstuff.client.tablet.quest.canvas.hit.CanvasTextMenuPlacement;
import com.abo47.questsandstuff.client.sync.state.ClientQuestStateFacade;
import com.abo47.questsandstuff.client.tablet.quest.canvas.blueprint.CanvasBlueprintController;
import com.abo47.questsandstuff.client.tablet.quest.canvas.hit.CanvasHitTester;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.ConnectionHit;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.QuestCardLayout;
import com.abo47.questsandstuff.client.tablet.quest.canvas.overlay.CanvasMiniNotificationController;
import com.abo47.questsandstuff.client.tablet.quest.canvas.overlay.CanvasOverlayController;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.CanvasChapterSwitchAnimation;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.selection.CanvasSelectionRenderer;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.text.CanvasTextRenderer;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.connection.ConnectionRenderer;
import com.abo47.questsandstuff.client.tablet.quest.canvas.viewport.CanvasCameraController;
import com.abo47.questsandstuff.client.tablet.state.UiState;
import com.abo47.questsandstuff.client.tablet.theme.tokens.ThemeColors;
import com.abo47.questsandstuff.quest.model.canvas.CanvasExclusiveChoice;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;

import static com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory.withAlpha;
import static com.abo47.questsandstuff.client.tablet.ui.factory.UiFactory.BODY_Y;
import static com.abo47.questsandstuff.client.tablet.ui.factory.UiFactory.CANVAS_LIMIT_HEIGHT;
import static com.abo47.questsandstuff.client.tablet.ui.factory.UiFactory.CANVAS_LIMIT_WIDTH;
import static com.abo47.questsandstuff.client.tablet.ui.factory.UiFactory.CHAPTER_W;
import static com.abo47.questsandstuff.client.tablet.ui.factory.UiFactory.GAP;
import static com.abo47.questsandstuff.client.tablet.ui.factory.UiFactory.PAD;
import static com.abo47.questsandstuff.client.tablet.ui.factory.UiFactory.PAD_Y;
import static com.abo47.questsandstuff.client.tablet.ui.factory.UiFactory.ROOT_H;
import static com.abo47.questsandstuff.client.tablet.ui.factory.UiFactory.ROOT_W;
import static com.abo47.questsandstuff.client.tablet.ui.factory.UiFactory.canvasHeight;
import static com.abo47.questsandstuff.client.tablet.ui.factory.UiFactory.canvasPanelWidth;
import static com.abo47.questsandstuff.client.tablet.ui.factory.UiFactory.panel;
import static com.abo47.questsandstuff.client.tablet.ui.state.StateQueries.selectedChapterName;

public final class CanvasRenderer {
    public static final float MIN_CANVAS_ZOOM = 0.5f;
    public static final float MAX_CANVAS_ZOOM = 3.0f;

    private static final int WINDOWED_CANVAS_PANEL_W = ROOT_W - PAD * 2 - CHAPTER_W - GAP;
    private static final int WINDOWED_CANVAS_PANEL_H = ROOT_H - BODY_Y - PAD_Y;

    private CanvasRenderer() {
    }
    public static void rebuildQuestCanvas(CanvasViewport canvasViewport, UiState state) {
        CanvasCameraController.beforeCanvasRebuild(state);
        canvasViewport.clearAllWidgets();
        String selectedChapter = CanvasRenderStateController.prepareRebuild(state);
        CanvasChapterSwitchAnimation.trackSelectedChapter(state, selectedChapter);
        CanvasSceneRenderer.applyCanvasBackground(canvasViewport);
        List<Map.Entry<String, CompoundTag>> quests = new ArrayList<>(ClientQuestStateFacade.questEntries());
        quests.sort(Comparator.comparing(Map.Entry::getKey));

        int viewportW = canvasViewport.getSize().width;
        int viewportH = canvasViewport.getSize().height;
        int usableW = Math.max(1, viewportW - 1);
        int usableH = Math.max(1, viewportH - 1);
        if (state.canvas.canvasLimitEnabled && !state.root.fullScreenMode) {
            double scaleX = (double) canvasPanelWidth(state) / WINDOWED_CANVAS_PANEL_W;
            double scaleY = (double) canvasHeight(state) / WINDOWED_CANVAS_PANEL_H;
            int limitW = (int) Math.round(CANVAS_LIMIT_WIDTH[state.canvas.canvasLimitIndex] * scaleX);
            int limitH = (int) Math.round(CANVAS_LIMIT_HEIGHT[state.canvas.canvasLimitIndex] * scaleY);
            usableW = Math.min(usableW, limitW);
            usableH = Math.min(usableH, limitH);
        }
        int cell = CanvasGeometry.gridSize(state);
        int contentW;
        int contentH;
        int contentX;
        int contentY;
        if (state.root.fullScreenMode) {
            contentX = 0;
            contentY = 0;
            contentW = ((usableW + cell - 1) / cell) * cell;
            contentH = ((usableH + cell - 1) / cell) * cell;
        } else {
            contentW = CanvasSceneRenderer.snapCanvasContentSize(usableW, cell);
            contentH = CanvasSceneRenderer.snapCanvasContentSize(usableH, cell);
            contentX = Math.max(0, (usableW - contentW) / 2);
            contentY = Math.max(0, (usableH - contentH) / 2);
        }
        CanvasRenderStateController.setContentBounds(state, contentX, contentY, contentW, contentH);
        CanvasCameraController.afterCanvasLayout(state, selectedChapter);
        CanvasSceneRenderer.renderCanvasSurfaceFactory(canvasViewport, state, contentX, contentY, contentW, contentH, viewportW, viewportH);

        if (state.canvas.canvasLimitEnabled && !state.root.fullScreenMode && (contentW < viewportW - 12 || contentH < viewportH - 12)) {
            WidgetGroup bounds = panel(4, 4, contentW + 4, contentH + 4, withAlpha(ThemeColors.SURFACE_PANEL_ALT, 36), ThemeColors.BORDER_ACCENT);
            canvasViewport.addWidget(bounds);
        }

        List<QuestCardLayout> visibleCards = CanvasLayoutService.layoutVisibleCards(quests, state);
        CanvasLayoutService.clampCanvasOffset(state, visibleCards, contentW, contentH);
        visibleCards = CanvasLayoutService.layoutVisibleCards(quests, state);
        if (CanvasCameraController.consumePendingQuestFocus(state, visibleCards, selectedChapter)) {
            visibleCards = CanvasLayoutService.layoutVisibleCards(quests, state);
        }
        CanvasCameraController.rememberCurrentGroup(state);

        Map<String, QuestCardLayout> byQuestId = new HashMap<>();
        for (QuestCardLayout card : visibleCards) {
            byQuestId.put(card.questId(), card);
        }
        CanvasRenderStateController.pruneStaleInteractiveState(state, byQuestId.keySet());
        WidgetGroup canvasContent = new WidgetGroup(0, 0, viewportW, viewportH);
        if (state.root.canEdit && state.canvas.gridEnabled) {
            CanvasSceneRenderer.renderGridOverlay(canvasContent, state, contentX, contentY, contentW, contentH);
        }
        CanvasSceneRenderer.renderCanvasElements(
                canvasContent,
                state,
                canvasViewport.player(),
                canvasViewport::refresh,
                visibleCards,
                viewportW,
                viewportH,
                canvasViewport::registerQuestCardLayer
        );
        ConnectionRenderer.renderPendingConnections(canvasContent, state, byQuestId, viewportW, viewportH);
        CanvasSelectionRenderer.renderAlignmentGuides(canvasContent, state);
        CanvasSelectionRenderer.updateSelectionBounds(state, visibleCards);
        CanvasRenderStateController.closeEditOnlyStateWhenReadOnly(state);
        CanvasSelectionRenderer.renderSelectionOverlay(canvasContent, state, visibleCards);
        WidgetGroup canvasContentLayer = CanvasChapterSwitchAnimation.wrap(state, canvasContent);
        canvasViewport.setCanvasContentLayer(canvasContentLayer);
        canvasViewport.addWidget(canvasContentLayer);
        WidgetGroup blueprintGhost = CanvasBlueprintController.placementGhost(canvasViewport, state);
        if (blueprintGhost != null) {
            canvasViewport.addWidget(blueprintGhost);
        }
        renderCanvasMetaPanels(canvasViewport, state, contentX, contentY, contentW, contentH);
        WidgetGroup miniNotification = CanvasMiniNotificationController.render(canvasViewport, state);
        if (miniNotification != null) {
            canvasViewport.addWidget(miniNotification);
        }
        canvasViewport.updateCardCache(visibleCards, byQuestId);
    }

    public static boolean matchesFilters(CompoundTag questTag, UiState state) {
        String selectedChapter = selectedChapterName(state);
        CompoundTag groups = questTag.getCompound("chapters");
        if (selectedChapter.isBlank()) {
            return false;
        }
        if (!state.root.canEdit && isVisualHiddenOutsideEdit(questTag)) {
            return false;
        }
        return groups.contains(selectedChapter);
    }

    private static boolean isVisualHiddenOutsideEdit(CompoundTag questTag) {
        return ClientQuestStateFacade.questHiddenPreview(questTag);
    }

    public static String connectionKey(String sourceQuestId, String targetQuestId) {
        return ConnectionRenderer.connectionKey(sourceQuestId, targetQuestId);
    }

    public static int connectionColor(UiState state, String chapter, String sourceQuestId, String targetQuestId) {
        return ConnectionRenderer.connectionColor(state, chapter, sourceQuestId, targetQuestId);
    }

    public static boolean isConnectionHidden(UiState state, String chapter, String sourceQuestId, String targetQuestId) {
        return ConnectionRenderer.isConnectionHidden(state, chapter, sourceQuestId, targetQuestId);
    }

    public static boolean isConnectionDirect(UiState state, String chapter, String sourceQuestId, String targetQuestId) {
        return ConnectionRenderer.isConnectionDirect(state, chapter, sourceQuestId, targetQuestId);
    }

    public static int snapToGrid(UiState state, int value) {
        return CanvasGeometry.snapToGrid(state, value);
    }

    public static QuestCardLayout hitTestCard(List<QuestCardLayout> cards, int x, int y) {
        return CanvasHitTester.hitTestCard(cards, x, y);
    }

    public static CanvasImageLayer hitTestCanvasImage(UiState state, int x, int y) {
        return CanvasHitTester.hitTestCanvasImage(state, x, y);
    }

    public static CanvasImageLayer hitTestSelectedCanvasImageControls(UiState state, int x, int y) {
        return CanvasHitTester.hitTestSelectedCanvasImageControls(state, x, y);
    }

    public static CanvasTextLayer hitTestCanvasText(UiState state, int x, int y) {
        return CanvasHitTester.hitTestCanvasText(state, x, y);
    }

    public static CanvasTextLayer hitTestSelectedCanvasTextControls(UiState state, int x, int y) {
        return CanvasHitTester.hitTestSelectedCanvasTextControls(state, x, y);
    }

    public static CanvasExclusiveChoice hitTestCanvasExclusiveChoice(UiState state, int x, int y) {
        return CanvasHitTester.hitTestCanvasExclusiveChoice(state, x, y);
    }

    public static CanvasExclusiveChoice hitTestSelectedCanvasExclusiveChoiceControls(UiState state, int x, int y) {
        return CanvasHitTester.hitTestSelectedCanvasExclusiveChoiceControls(state, x, y);
    }

    public static double[] canvasExclusiveChoiceLocalScreenPoint(UiState state, CanvasExclusiveChoice ec, int x, int y) {
        return CanvasHitTester.canvasExclusiveChoiceLocalScreenPoint(state, ec, x, y);
    }

    public static boolean isCanvasExclusiveChoiceResizeHandleHit(UiState state, CanvasExclusiveChoice ec, int x, int y) {
        return CanvasHitTester.isCanvasExclusiveChoiceResizeHandleHit(state, ec, x, y);
    }

    public static boolean isCanvasExclusiveChoiceRotateHandleHit(UiState state, CanvasExclusiveChoice ec, int x, int y) {
        return CanvasHitTester.isCanvasExclusiveChoiceRotateHandleHit(state, ec, x, y);
    }

    public static boolean isCanvasTextResizeHandleHit(UiState state, CanvasTextLayer text, int x, int y) {
        return CanvasHitTester.isCanvasTextResizeHandleHit(state, text, x, y);
    }

    public static boolean isCanvasTextRotateHandleHit(UiState state, CanvasTextLayer text, int x, int y) {
        return CanvasHitTester.isCanvasTextRotateHandleHit(state, text, x, y);
    }

    public static double[] canvasTextLocalScreenPoint(UiState state, CanvasTextLayer text, int x, int y) {
        return CanvasHitTester.canvasTextLocalScreenPoint(state, text, x, y);
    }

    public static boolean isCanvasTextOwnerHit(UiState state, CanvasTextLayer text, int x, int y) {
        return CanvasHitTester.isCanvasTextOwnerHit(state, text, x, y);
    }

    public static int canvasTextCursorAt(UiState state, CanvasTextLayer text, int x, int y) {
        return CanvasTextRenderer.canvasTextCursorAt(state, text, x, y);
    }

    public static int[] canvasTextMenuBounds(UiState state, CanvasTextLayer text, int viewportW, int viewportH, int toolCount) {
        return CanvasTextMenuPlacement.canvasTextMenuBounds(state, text, viewportW, viewportH, toolCount);
    }

    public static boolean isCanvasImageResizeHandleHit(UiState state, CanvasImageLayer image, int x, int y) {
        return CanvasHitTester.isCanvasImageResizeHandleHit(state, image, x, y);
    }

    public static boolean isCanvasImageRotateHandleHit(UiState state, CanvasImageLayer image, int x, int y) {
        return CanvasHitTester.isCanvasImageRotateHandleHit(state, image, x, y);
    }


    public static ConnectionHit hitTestConnection(UiState state, List<QuestCardLayout> cards, Map<String, QuestCardLayout> byQuestId, int x, int y) {
        return CanvasHitTester.hitTestConnection(state, cards, byQuestId, x, y);
    }

    public static boolean jumpToBestMatch(UiState state) {
        return CanvasLayoutService.jumpToBestMatch(state);
    }

    public static boolean matchesSearchOnly(CompoundTag questTag, String search) {
        return CanvasLayoutService.matchesSearchOnly(questTag, search);
    }


    public static int textSelectionStart(UiState state) {
        return CanvasTextRenderer.textSelectionStart(state);
    }

    public static int textSelectionEnd(UiState state) {
        return CanvasTextRenderer.textSelectionEnd(state);
    }

    public static boolean hasTextSelection(UiState state) {
        return CanvasTextRenderer.hasTextSelection(state);
    }

    public static CanvasTextLayer applyTextStyleSelection(UiState state, CanvasTextLayer text, String style) {
        return CanvasTextRenderer.applyTextStyleSelection(state, text, style);
    }

    public static CanvasTextLayer toggleTextStyleSelection(UiState state, CanvasTextLayer text, String flag) {
        return CanvasTextRenderer.toggleTextStyleSelection(state, text, flag);
    }

    public static boolean isTextStyleFlagActive(UiState state, CanvasTextLayer text, String flag) {
        return CanvasTextRenderer.isTextStyleFlagActive(state, text, flag);
    }

    public static int activeTextColor(UiState state, CanvasTextLayer text) {
        return CanvasTextRenderer.activeTextColor(state, text);
    }

    public static CanvasTextLayer applyTextColorSelection(UiState state, CanvasTextLayer text, int color) {
        return CanvasTextRenderer.applyTextColorSelection(state, text, color);
    }

    private static void renderCanvasMetaPanels(
            CanvasViewport canvasViewport,
            UiState state,
            int contentX,
            int contentY,
            int contentW,
            int contentH
    ) {
        CanvasOverlayController.renderCanvasMetaPanels(canvasViewport, state, contentX, contentY, contentW, contentH);
    }

    public static boolean isContextMenuHit(UiState state, int x, int y) {
        return CanvasOverlayController.isContextMenuHit(state, x, y);
    }

    public static boolean isSelectionBoundsHit(UiState state, int x, int y) {
        return CanvasSelectionRenderer.isSelectionBoundsHit(state, x, y);
    }

    public static boolean isSelectionResizeHandleHit(UiState state, int x, int y) {
        return CanvasSelectionRenderer.isSelectionResizeHandleHit(state, x, y);
    }

    public static boolean isSelectionRotateHandleHit(UiState state, int x, int y) {
        return CanvasSelectionRenderer.isSelectionRotateHandleHit(state, x, y);
    }

    public static void drawSelectionForeground(
            GuiGraphics graphics,
            UiState state,
            List<QuestCardLayout> cards,
            int originX,
            int originY,
            int maxW,
            int maxH
    ) {
        CanvasSelectionRenderer.drawSelectionForeground(graphics, state, cards, originX, originY, maxW, maxH);
    }

    public static float clampZoom(float zoom) {
        if (Float.isNaN(zoom) || Float.isInfinite(zoom)) {
            return 1.0f;
        }
        return Math.max(MIN_CANVAS_ZOOM, Math.min(MAX_CANVAS_ZOOM, zoom));
    }

}
