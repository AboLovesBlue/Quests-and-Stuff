package com.abo47.questsandstuff.client.tablet.quest.canvas.overlay;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import com.abo47.questsandstuff.QuestsAndStuffConfig;
import com.abo47.questsandstuff.client.tablet.animation.UiAnimationProgress;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasViewport;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.CanvasPoint;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.QuestCardLayout;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.ConnectionRenderer;
import com.abo47.questsandstuff.client.tablet.quest.canvas.viewport.CanvasMinimapController;
import com.abo47.questsandstuff.client.tablet.quest.canvas.viewport.CanvasMinimapGeometry;
import com.abo47.questsandstuff.client.tablet.quest.canvas.viewport.CanvasViewportScissor;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.tokens.TabletColors;
import com.abo47.questsandstuff.quest.model.QuestDefinition;
import com.abo47.questsandstuff.quest.model.QuestSettings;
import com.abo47.questsandstuff.quest.model.canvas.CanvasExclusiveChoice;

import static com.abo47.questsandstuff.client.tablet.ui.state.TabletStateQueries.selectedChapterName;
import static com.abo47.questsandstuff.util.MathUtils.clamp;

final class CanvasMinimapOverlay {
    private static final int MIN_QUEST_SIZE = 4;
    private static final int MIN_EC_SIZE = 2;
    private static final int EC_ALPHA = 200;
    private static final int LOGICAL_GRID_STEP = 16;

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
                        lastSnapshot = snapshot(state, chapter, canvasViewport.cardCache(), canvasViewport.cardLookup(), projection);
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

    private static CanvasMinimapSnapshot snapshot(
            TabletUiState state,
            String chapter,
            List<QuestCardLayout> cards,
            Map<String, QuestCardLayout> byQuestId,
            CanvasMinimapGeometry.Projection projection
    ) {
        Map<String, CanvasMinimapRect> boxes = new HashMap<>();
        Map<String, double[]> logicalCenters = new HashMap<>();
        Set<String> rendered = new HashSet<>();
        List<CanvasMinimapRect> rects = snapshotQuestBoxes(state, cards, projection, boxes, logicalCenters);
        List<CanvasMinimapConnection> connections = snapshotQuestConnections(state, chapter, cards, byQuestId, projection, boxes, logicalCenters, rendered);
        snapshotEcStage(state, chapter, projection, boxes, logicalCenters, rendered, rects, connections);
        return new CanvasMinimapSnapshot(List.copyOf(rects), List.copyOf(connections), projection);
    }

    private static List<CanvasMinimapRect> snapshotQuestBoxes(
            TabletUiState state,
            List<QuestCardLayout> cards,
            CanvasMinimapGeometry.Projection projection,
            Map<String, CanvasMinimapRect> boxes,
            Map<String, double[]> logicalCenters
    ) {
        List<CanvasMinimapRect> rects = new ArrayList<>();
        boolean visualMode = QuestsAndStuffConfig.visualMinimapEnabled();
        for (QuestCardLayout card : cards) {
            CanvasMinimapRect box = projectRect(
                    projection,
                    card.visualLogicalX(),
                    card.visualLogicalY(),
                    card.logicalWidth(),
                    card.logicalHeight(),
                    MIN_QUEST_SIZE,
                    questColor(state, card),
                    visualMode ? visualCardAlpha(state, card) : cardAlpha(state, card),
                    card.questId(),
                    card.tag()
            );
            logicalCenters.put(card.questId(), new double[]{card.visualLogicalX() + card.logicalWidth() / 2.0, card.visualLogicalY() + card.logicalHeight() / 2.0});
            boxes.put(card.questId(), box);
            rects.add(box);
        }
        return rects;
    }

    private static List<CanvasMinimapConnection> snapshotQuestConnections(
            TabletUiState state,
            String chapter,
            List<QuestCardLayout> cards,
            Map<String, QuestCardLayout> byQuestId,
            CanvasMinimapGeometry.Projection projection,
            Map<String, CanvasMinimapRect> boxes,
            Map<String, double[]> logicalCenters,
            Set<String> rendered
    ) {
        List<CanvasMinimapConnection> connections = new ArrayList<>();
        for (QuestCardLayout target : cards) {
            CompoundTag targetTag = target.tag();
            if (!targetTag.getBoolean(QuestSettings.SHOW_PREREQUISITE_ARROW_FIELD)) {
                continue;
            }
            ListTag prerequisites = targetTag.getList(QuestDefinition.PREREQUISITES_FIELD, Tag.TAG_STRING);
            for (int i = 0; i < prerequisites.size(); i++) {
                String sourceId = prerequisites.getString(i);
                QuestCardLayout source = byQuestId.get(sourceId);
                if (source == null) {
                    continue;
                }
                String connectionKey = ConnectionRenderer.connectionKey(sourceId, target.questId());
                if (!rendered.add(connectionKey)) {
                    continue;
                }
                boolean hidden = ConnectionRenderer.isConnectionHidden(state, chapter, sourceId, target.questId());
                if (hidden && !state.root.canEdit) {
                    continue;
                }
                boolean direct = ConnectionRenderer.isConnectionDirect(state, chapter, sourceId, target.questId());
                double[] srcCenter = logicalCenters.get(sourceId);
                double[] tgtCenter = logicalCenters.get(target.questId());
                if (srcCenter == null || tgtCenter == null) continue;
                CanvasMinimapRect srcBox = boxes.get(sourceId);
                CanvasMinimapRect tgtBox = boxes.get(target.questId());
                float sourceCenterX = srcBox.x() + srcBox.w() / 2.0f;
                float sourceCenterY = srcBox.y() + srcBox.h() / 2.0f;
                float targetCenterX = tgtBox.x() + tgtBox.w() / 2.0f;
                float targetCenterY = tgtBox.y() + tgtBox.h() / 2.0f;
                String tex = ConnectionRenderer.connectionTexture(state, chapter, sourceId, target.questId());
                connections.add(new CanvasMinimapConnection(
                        sourceCenterX,
                        sourceCenterY,
                        targetCenterX,
                        targetCenterY,
                        ConnectionRenderer.connectionColor(state, chapter, sourceId, target.questId()),
                        hidden ? 70 : 190,
                        direct,
                        direct ? null : computeGridPath(projection, srcCenter[0], srcCenter[1], tgtCenter[0], tgtCenter[1]),
                        tex.isBlank() ? null : tex
                ));
            }
        }
        return connections;
    }

    private static void snapshotEcStage(
            TabletUiState state,
            String chapter,
            CanvasMinimapGeometry.Projection projection,
            Map<String, CanvasMinimapRect> boxes,
            Map<String, double[]> logicalCenters,
            Set<String> rendered,
            List<CanvasMinimapRect> rects,
            List<CanvasMinimapConnection> connections
    ) {
        List<CanvasExclusiveChoice> ecs = state.canvas.canvasExclusiveChoicesByChapter.getOrDefault(chapter, List.of());
        for (CanvasExclusiveChoice ec : ecs) {
            net.minecraft.nbt.CompoundTag ecTag = null;
            if (!ec.background().isBlank()) {
                ecTag = new CompoundTag();
                ecTag.putString("ec_background", ec.background());
            }
            CanvasMinimapRect ecBox = projectRect(
                    projection,
                    ec.x(),
                    ec.y(),
                    ec.w(),
                    ec.h(),
                    MIN_EC_SIZE,
                    TabletColors.WARNING,
                    EC_ALPHA,
                    ec.id(),
                    ecTag
            );
            double[] ecLogicalCenter = new double[]{ec.x() + ec.w() / 2.0, ec.y() + ec.h() / 2.0};
            logicalCenters.put(ec.id(), ecLogicalCenter);
            boxes.put(ec.id(), ecBox);
            rects.add(ecBox);

            float ecCenterX = (float) CanvasMinimapGeometry.mapX(projection, ecLogicalCenter[0]);
            float ecCenterY = (float) CanvasMinimapGeometry.mapY(projection, ecLogicalCenter[1]);
            for (String connectedId : ec.connectionQuestIds()) {
                addEcConnection(connections, rendered, projection, ec, connectedId, boxes.get(connectedId), logicalCenters.get(connectedId), ecLogicalCenter, ecCenterX, ecCenterY, true);
            }
            for (String prerequisiteId : ec.prerequisiteQuestIds()) {
                addEcConnection(connections, rendered, projection, ec, prerequisiteId, boxes.get(prerequisiteId), logicalCenters.get(prerequisiteId), ecLogicalCenter, ecCenterX, ecCenterY, false);
            }
        }
    }

    private static void addEcConnection(
            List<CanvasMinimapConnection> connections,
            Set<String> rendered,
            CanvasMinimapGeometry.Projection projection,
            CanvasExclusiveChoice ec,
            String otherId,
            CanvasMinimapRect otherBox,
            double[] otherCenter,
            double[] ecLogicalCenter,
            float ecCenterX,
            float ecCenterY,
            boolean ecIsSource
    ) {
        if (otherBox == null) {
            return;
        }
        String sourceId = ecIsSource ? ec.id() : otherId;
        String targetId = ecIsSource ? otherId : ec.id();
        if (!rendered.add("ec:" + sourceId + "->" + targetId)) return;
        float otherCX = otherCenter == null ? otherBox.x() + otherBox.w() / 2.0f : (float) CanvasMinimapGeometry.mapX(projection, otherCenter[0]);
        float otherCY = otherCenter == null ? otherBox.y() + otherBox.h() / 2.0f : (float) CanvasMinimapGeometry.mapY(projection, otherCenter[1]);
        float srcCX = ecIsSource ? ecCenterX : otherCX;
        float srcCY = ecIsSource ? ecCenterY : otherCY;
        float tgtCX = ecIsSource ? otherCX : ecCenterX;
        float tgtCY = ecIsSource ? otherCY : ecCenterY;
        double[] srcCenter = ecIsSource ? ecLogicalCenter : otherCenter;
        double[] tgtCenter = ecIsSource ? otherCenter : ecLogicalCenter;
        String tex = ec.connectionTextures().getOrDefault(otherId, "");
        connections.add(new CanvasMinimapConnection(
                srcCX, srcCY, tgtCX, tgtCY,
                ec.connectionColors().getOrDefault(otherId, TabletColors.TEXT_SECONDARY),
                EC_ALPHA, isEcDirect(ec, otherId),
                isEcDirect(ec, otherId) || otherCenter == null ? null : computeGridPath(projection, srcCenter[0], srcCenter[1], tgtCenter[0], tgtCenter[1]),
                tex.isBlank() ? null : tex
        ));
    }

    private static List<CanvasPoint> computeGridPath(CanvasMinimapGeometry.Projection projection, double sx, double sy, double tx, double ty) {
        int midX = snapToStep((int) ((sx + tx) / 2), LOGICAL_GRID_STEP);
        if (Math.abs(midX - sx) < LOGICAL_GRID_STEP / 2.0) {
            midX += tx >= sx ? LOGICAL_GRID_STEP : -LOGICAL_GRID_STEP;
        }
        return List.of(
                new CanvasPoint(CanvasMinimapGeometry.mapX(projection, sx), CanvasMinimapGeometry.mapY(projection, sy)),
                new CanvasPoint(CanvasMinimapGeometry.mapX(projection, midX), CanvasMinimapGeometry.mapY(projection, sy)),
                new CanvasPoint(CanvasMinimapGeometry.mapX(projection, midX), CanvasMinimapGeometry.mapY(projection, ty)),
                new CanvasPoint(CanvasMinimapGeometry.mapX(projection, tx), CanvasMinimapGeometry.mapY(projection, ty))
        );
    }

    private static int snapToStep(int value, int step) {
        return Math.round(value / (float) Math.max(1, step)) * Math.max(1, step);
    }

    private static boolean isEcDirect(CanvasExclusiveChoice ec, String targetId) {
        String mode = ec.connectionModes().get(targetId);
        return mode == null || !"grid".equals(mode);
    }

    private static int questColor(TabletUiState state, QuestCardLayout card) {
        if (state.canvas.canvasSelection.questIds().contains(card.questId())) {
            return TabletColors.WARNING;
        }
        CompoundTag tag = card.tag();
        if (tag.getBoolean("claimed") || tag.getBoolean("completed")) {
            return TabletColors.SUCCESS;
        }
        if (tag.getBoolean("unlocked")) {
            return TabletColors.INTERACTIVE;
        }
        return TabletColors.TEXT_MUTED;
    }

    private static int cardAlpha(TabletUiState state, QuestCardLayout card) {
        CompoundTag tag = card.tag();
        if (state.root.canEdit && tag.getBoolean("visual_hidden") && !tag.getBoolean("completed")) {
            return 115;
        }
        return 220;
    }

    private static int visualCardAlpha(TabletUiState state, QuestCardLayout card) {
        CompoundTag tag = card.tag();
        if (state.root.canEdit && tag.getBoolean("visual_hidden") && !tag.getBoolean("completed")) {
            return 130;
        }
        return 255;
    }

    private static CanvasMinimapRect projectRect(
            CanvasMinimapGeometry.Projection projection,
            int logicalX,
            int logicalY,
            int logicalW,
            int logicalH,
            int minSize,
            int color,
            int alpha,
            String id,
            CompoundTag tag
    ) {
        int x = clamp(CanvasMinimapGeometry.mapX(projection, logicalX), projection.drawX(), projection.drawX() + projection.drawW() - 1);
        int y = clamp(CanvasMinimapGeometry.mapY(projection, logicalY), projection.drawY(), projection.drawY() + projection.drawH() - 1);
        int w = Math.max(minSize, Math.round(Math.max(1, logicalW) * projection.scale()));
        int h = Math.max(minSize, Math.round(Math.max(1, logicalH) * projection.scale()));
        w = Math.max(1, Math.min(w, projection.drawX() + projection.drawW() - x));
        h = Math.max(1, Math.min(h, projection.drawY() + projection.drawH() - y));
        return new CanvasMinimapRect(x, y, w, h, color, alpha, id, tag);
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
