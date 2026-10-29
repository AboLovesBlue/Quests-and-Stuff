package com.abo47.questsandstuff.client.tablet.quest.canvas.overlay;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import com.abo47.questsandstuff.QuestsAndStuffConfig;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.CanvasPoint;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.QuestCardLayout;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.connection.ConnectionRenderer;
import com.abo47.questsandstuff.client.tablet.quest.canvas.viewport.CanvasMinimapGeometry;
import com.abo47.questsandstuff.client.tablet.state.UiState;
import com.abo47.questsandstuff.client.tablet.theme.tokens.ThemeColors;
import com.abo47.questsandstuff.quest.model.QuestDefinition;
import com.abo47.questsandstuff.quest.model.QuestSettings;
import com.abo47.questsandstuff.quest.model.canvas.CanvasExclusiveChoice;

import static com.abo47.questsandstuff.util.MathUtils.clamp;

final class CanvasMinimapSnapshotBuilder {
    private static final int MIN_QUEST_SIZE = 4;
    private static final int MIN_EC_SIZE = 2;
    private static final int EXCLUSIVE_CHOICE_ALPHA = 200;
    private static final int LOGICAL_GRID_STEP = 16;

    private CanvasMinimapSnapshotBuilder() {
    }

    static CanvasMinimapSnapshot snapshot(
            UiState state,
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
        snapshotExclusiveChoiceStage(state, chapter, projection, boxes, logicalCenters, rendered, rects, connections);
        return new CanvasMinimapSnapshot(List.copyOf(rects), List.copyOf(connections), projection);
    }

    private static List<CanvasMinimapRect> snapshotQuestBoxes(
            UiState state,
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
            UiState state,
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

    private static void snapshotExclusiveChoiceStage(
            UiState state,
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
            net.minecraft.nbt.CompoundTag exclusiveChoiceTag = null;
            if (!ec.background().isBlank()) {
                exclusiveChoiceTag = new CompoundTag();
                exclusiveChoiceTag.putString("ec_background", ec.background());
            }
            CanvasMinimapRect exclusiveChoiceBox = projectRect(
                    projection,
                    ec.x(),
                    ec.y(),
                    ec.w(),
                    ec.h(),
                    MIN_EC_SIZE,
                    ThemeColors.WARNING,
                    EXCLUSIVE_CHOICE_ALPHA,
                    ec.id(),
                    exclusiveChoiceTag
            );
            double[] exclusiveChoiceLogicalCenter = new double[]{ec.x() + ec.w() / 2.0, ec.y() + ec.h() / 2.0};
            logicalCenters.put(ec.id(), exclusiveChoiceLogicalCenter);
            boxes.put(ec.id(), exclusiveChoiceBox);
            rects.add(exclusiveChoiceBox);

            float exclusiveChoiceCenterX = (float) CanvasMinimapGeometry.mapX(projection, exclusiveChoiceLogicalCenter[0]);
            float exclusiveChoiceCenterY = (float) CanvasMinimapGeometry.mapY(projection, exclusiveChoiceLogicalCenter[1]);
            for (String connectedId : ec.connectionQuestIds()) {
                addExclusiveChoiceConnection(connections, rendered, projection, ec, connectedId, boxes.get(connectedId), logicalCenters.get(connectedId), exclusiveChoiceLogicalCenter, exclusiveChoiceCenterX, exclusiveChoiceCenterY, true);
            }
            for (String prerequisiteId : ec.prerequisiteQuestIds()) {
                addExclusiveChoiceConnection(connections, rendered, projection, ec, prerequisiteId, boxes.get(prerequisiteId), logicalCenters.get(prerequisiteId), exclusiveChoiceLogicalCenter, exclusiveChoiceCenterX, exclusiveChoiceCenterY, false);
            }
        }
    }

    private static void addExclusiveChoiceConnection(
            List<CanvasMinimapConnection> connections,
            Set<String> rendered,
            CanvasMinimapGeometry.Projection projection,
            CanvasExclusiveChoice ec,
            String otherId,
            CanvasMinimapRect otherBox,
            double[] otherCenter,
            double[] exclusiveChoiceLogicalCenter,
            float exclusiveChoiceCenterX,
            float exclusiveChoiceCenterY,
            boolean exclusiveChoiceIsSource
    ) {
        if (otherBox == null) {
            return;
        }
        String sourceId = exclusiveChoiceIsSource ? ec.id() : otherId;
        String targetId = exclusiveChoiceIsSource ? otherId : ec.id();
        if (!rendered.add("ec:" + sourceId + "->" + targetId)) return;
        float otherCX = otherCenter == null ? otherBox.x() + otherBox.w() / 2.0f : (float) CanvasMinimapGeometry.mapX(projection, otherCenter[0]);
        float otherCY = otherCenter == null ? otherBox.y() + otherBox.h() / 2.0f : (float) CanvasMinimapGeometry.mapY(projection, otherCenter[1]);
        float srcCX = exclusiveChoiceIsSource ? exclusiveChoiceCenterX : otherCX;
        float srcCY = exclusiveChoiceIsSource ? exclusiveChoiceCenterY : otherCY;
        float tgtCX = exclusiveChoiceIsSource ? otherCX : exclusiveChoiceCenterX;
        float tgtCY = exclusiveChoiceIsSource ? otherCY : exclusiveChoiceCenterY;
        double[] srcCenter = exclusiveChoiceIsSource ? exclusiveChoiceLogicalCenter : otherCenter;
        double[] tgtCenter = exclusiveChoiceIsSource ? otherCenter : exclusiveChoiceLogicalCenter;
        String tex = ec.connectionTextures().getOrDefault(otherId, "");
        connections.add(new CanvasMinimapConnection(
                srcCX, srcCY, tgtCX, tgtCY,
                ec.connectionColors().getOrDefault(otherId, ThemeColors.TEXT_SECONDARY),
                EXCLUSIVE_CHOICE_ALPHA, isExclusiveChoiceDirect(ec, otherId),
                isExclusiveChoiceDirect(ec, otherId) || otherCenter == null ? null : computeGridPath(projection, srcCenter[0], srcCenter[1], tgtCenter[0], tgtCenter[1]),
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

    private static boolean isExclusiveChoiceDirect(CanvasExclusiveChoice ec, String targetId) {
        String mode = ec.connectionModes().get(targetId);
        return mode == null || !"grid".equals(mode);
    }

    private static int questColor(UiState state, QuestCardLayout card) {
        if (state.canvas.canvasSelection.questIds().contains(card.questId())) {
            return ThemeColors.WARNING;
        }
        CompoundTag tag = card.tag();
        if (tag.getBoolean("claimed") || tag.getBoolean("completed")) {
            return ThemeColors.SUCCESS;
        }
        if (tag.getBoolean("unlocked")) {
            return ThemeColors.INTERACTIVE;
        }
        return ThemeColors.TEXT_MUTED;
    }

    private static int cardAlpha(UiState state, QuestCardLayout card) {
        CompoundTag tag = card.tag();
        if (state.root.canEdit && tag.getBoolean("visual_hidden") && !tag.getBoolean("completed")) {
            return 115;
        }
        return 220;
    }

    private static int visualCardAlpha(UiState state, QuestCardLayout card) {
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
}
