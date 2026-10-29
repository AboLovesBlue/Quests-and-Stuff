package com.abo47.questsandstuff.client.tablet.quest.canvas.render.connection;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import com.abo47.questsandstuff.client.tablet.quest.canvas.render.geometry.CanvasElementGeometry;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.geometry.CanvasLayerOrdering;
import com.abo47.questsandstuff.client.tablet.quest.canvas.layer.CanvasLayerMutations;
import com.abo47.questsandstuff.client.tablet.quest.canvas.geometry.CanvasLayoutService;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.QuestCardLayout;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.tokens.TabletColors;
import com.abo47.questsandstuff.quest.model.QuestDefinition;
import com.abo47.questsandstuff.quest.model.QuestSettings;
import com.abo47.questsandstuff.quest.model.canvas.CanvasExclusiveChoice;

import static com.abo47.questsandstuff.client.tablet.ui.state.TabletStateQueries.selectedChapterName;

final class ConnectionLayout {
    private ConnectionLayout() {
    }

    static List<ConnectionLine> prerequisiteAndPendingConnectionLines(
            TabletUiState state,
            List<QuestCardLayout> cards,
            Map<String, QuestCardLayout> byQuestId,
            int viewportW,
            int viewportH
    ) {
        List<ConnectionLine> lines = new ArrayList<>(prerequisiteConnectionLines(state, cards, byQuestId, viewportW, viewportH));
        lines.addAll(pendingConnectionLines(state, byQuestId, viewportW, viewportH));
        return lines;
    }

    static List<ConnectionLine> prerequisiteConnectionLines(
            TabletUiState state,
            List<QuestCardLayout> cards,
            Map<String, QuestCardLayout> byQuestId,
            int viewportW,
            int viewportH
    ) {
        List<ConnectionLine> lines = new ArrayList<>();
        Set<String> rendered = new HashSet<>();
        String chapter = selectedChapterName(state);

        for (QuestCardLayout quest : cards) {
            CompoundTag questTag = quest.tag();
            if (!questTag.getBoolean(QuestSettings.SHOW_PREREQUISITE_ARROW_FIELD)) {
                continue;
            }
            ListTag prerequisites = questTag.getList(QuestDefinition.PREREQUISITES_FIELD, Tag.TAG_STRING);
            for (int i = 0; i < prerequisites.size(); i++) {
                String prerequisiteId = prerequisites.getString(i);
                QuestCardLayout prerequisite = byQuestId.get(prerequisiteId);
                if (prerequisite == null) {
                    continue;
                }

                ConnectionRenderStyle style = ConnectionStyleResolver.style(state, chapter, prerequisiteId, quest.questId(), questTag);
                if (!rendered.add(style.connectionId())) {
                    continue;
                }
                if (!CanvasLayoutService.intersectsPanRenderWindow(prerequisite, viewportW, viewportH)
                        && !CanvasLayoutService.intersectsPanRenderWindow(quest, viewportW, viewportH)) {
                    continue;
                }
                lines.add(lineFromStyle(style, prerequisite, quest, false));
            }
        }
        List<CanvasExclusiveChoice> ecs = state.canvas.canvasExclusiveChoicesByChapter.getOrDefault(chapter, List.of());
        for (CanvasExclusiveChoice ec : ecs) {
            CanvasExclusiveChoice drawExclusiveChoice = CanvasLayerMutations.effectiveCanvasExclusiveChoice(state, ec);
            CanvasElementGeometry.Box exclusiveChoiceBox = CanvasElementGeometry.screenBoxAtPivot(state, drawExclusiveChoice.x(), drawExclusiveChoice.y(), drawExclusiveChoice.w(), drawExclusiveChoice.h(), 0, 0, drawExclusiveChoice.rotation());
            int exclusiveChoiceBoxLeft = (int) Math.floor(exclusiveChoiceBox.centerX() + exclusiveChoiceBox.left());
            int exclusiveChoiceBoxTop = (int) Math.floor(exclusiveChoiceBox.centerY() + exclusiveChoiceBox.top());
            int exclusiveChoiceBoxRight = (int) Math.ceil(exclusiveChoiceBox.centerX() + exclusiveChoiceBox.right());
            int exclusiveChoiceBoxBottom = (int) Math.ceil(exclusiveChoiceBox.centerY() + exclusiveChoiceBox.bottom());
            int exclusiveChoiceScreenW = Math.max(1, exclusiveChoiceBoxRight - exclusiveChoiceBoxLeft);
            int exclusiveChoiceScreenH = Math.max(1, exclusiveChoiceBoxBottom - exclusiveChoiceBoxTop);
            double exclusiveChoiceRot = Math.toRadians(drawExclusiveChoice.rotation());
            double exclusiveChoiceCos = Math.cos(exclusiveChoiceRot);
            double exclusiveChoiceSin = Math.sin(exclusiveChoiceRot);
            int exclusiveChoiceCenterX = (int) (exclusiveChoiceBox.centerX() + exclusiveChoiceBox.width() / 2.0 * exclusiveChoiceCos - exclusiveChoiceBox.height() / 2.0 * exclusiveChoiceSin);
            int exclusiveChoiceCenterY = (int) (exclusiveChoiceBox.centerY() + exclusiveChoiceBox.width() / 2.0 * exclusiveChoiceSin + exclusiveChoiceBox.height() / 2.0 * exclusiveChoiceCos);
            for (String connectedQuestId : drawExclusiveChoice.connectionQuestIds()) {
                QuestCardLayout connectedQuest = byQuestId.get(connectedQuestId);
                if (connectedQuest == null) {
                    continue;
                }
                addExclusiveChoiceConnectionLine(lines, rendered, state, chapter, ec.id(), connectedQuestId, connectedQuest,
                        true, viewportW, viewportH, exclusiveChoiceBoxLeft, exclusiveChoiceBoxTop, exclusiveChoiceScreenW, exclusiveChoiceScreenH, exclusiveChoiceCenterX, exclusiveChoiceCenterY);
            }
            for (String prerequisiteQuestId : drawExclusiveChoice.prerequisiteQuestIds()) {
                QuestCardLayout prerequisiteQuest = byQuestId.get(prerequisiteQuestId);
                if (prerequisiteQuest == null) {
                    continue;
                }
                addExclusiveChoiceConnectionLine(lines, rendered, state, chapter, ec.id(), prerequisiteQuestId, prerequisiteQuest,
                        false, viewportW, viewportH, exclusiveChoiceBoxLeft, exclusiveChoiceBoxTop, exclusiveChoiceScreenW, exclusiveChoiceScreenH, exclusiveChoiceCenterX, exclusiveChoiceCenterY);
            }
        }
        return lines;
    }

    static List<String> prerequisiteConnectionLayerKeys(
            TabletUiState state,
            List<QuestCardLayout> cards,
            Map<String, QuestCardLayout> byQuestId,
            int viewportW,
            int viewportH
    ) {
        List<ConnectionLine> lines = prerequisiteConnectionLines(state, cards, byQuestId, viewportW, viewportH);
        List<String> keys = new ArrayList<>();
        for (ConnectionLine line : lines) {
            keys.add(CanvasLayerOrdering.connectionKey(line.connectionId()));
        }
        return keys;
    }

    static List<ConnectionLine> pendingConnectionLines(
            TabletUiState state,
            Map<String, QuestCardLayout> byQuestId,
            int viewportW,
            int viewportH
    ) {
        List<ConnectionLine> lines = new ArrayList<>();
        if (!state.root.canEdit) {
            return lines;
        }
        Set<String> pendingSources = new HashSet<>(state.canvas.connectSourceQuestIds);
        if (!state.canvas.connectSourceQuestId.isBlank()) {
            pendingSources.add(state.canvas.connectSourceQuestId);
        }
        for (String sourceQuestId : pendingSources) {
            QuestCardLayout source = byQuestId.get(sourceQuestId);
            if (source != null && CanvasLayoutService.intersectsPanRenderWindow(source, viewportW, viewportH)) {
                lines.add(new ConnectionLine(
                        "",
                        sourceQuestId,
                        sourceQuestId,
                        source.x(),
                        source.y(),
                        source.width(),
                        source.height(),
                        source.x(),
                        source.y(),
                        source.width(),
                        source.height(),
                        source.centerX(),
                        source.centerY(),
                        source.centerX(),
                        source.centerY(),
                        false,
                        true,
                        TabletColors.TEXT_SECONDARY,
                        false,
                        245, "", 0
                ));
            }
        }
        return lines;
    }

    private static void addExclusiveChoiceConnectionLine(List<ConnectionLine> lines, Set<String> rendered, TabletUiState state, String chapter,
            String exclusiveChoiceId, String questId, QuestCardLayout quest, boolean exclusiveChoiceSource, int viewportW, int viewportH,
            int exclusiveChoiceBoxLeft, int exclusiveChoiceBoxTop, int exclusiveChoiceScreenW, int exclusiveChoiceScreenH, int exclusiveChoiceCenterX, int exclusiveChoiceCenterY) {
        String fromId = exclusiveChoiceSource ? exclusiveChoiceId : questId;
        String toId = exclusiveChoiceSource ? questId : exclusiveChoiceId;
        String connectionId = ConnectionAnimation.connectionKey(fromId, toId);
        if (!rendered.add(connectionId)) {
            return;
        }
        if (!intersectsViewport(exclusiveChoiceBoxLeft, exclusiveChoiceBoxTop, exclusiveChoiceScreenW, exclusiveChoiceScreenH, viewportW, viewportH)
                && !CanvasLayoutService.intersectsPanRenderWindow(quest, viewportW, viewportH)) {
            return;
        }
        boolean direct = ConnectionStyleResolver.exclusiveChoiceIsConnectionDirect(state, chapter, fromId, toId);
        int color = ConnectionStyleResolver.exclusiveChoiceConnectionColor(state, chapter, fromId, toId);
        String texture = ConnectionStyleResolver.exclusiveChoiceConnectionTexture(state, chapter, fromId, toId);
        int textureSpacing = ConnectionStyleResolver.exclusiveChoiceConnectionTextureSpacing(state, chapter, fromId, toId);
        boolean hidden = ConnectionStyleResolver.exclusiveChoiceIsConnectionHidden(state, chapter, fromId, toId);
        int alpha = hidden ? ConnectionRenderStyle.HIDDEN_ALPHA : ConnectionRenderStyle.VISIBLE_ALPHA;
        int sourceX = exclusiveChoiceSource ? exclusiveChoiceBoxLeft : quest.x();
        int sourceY = exclusiveChoiceSource ? exclusiveChoiceBoxTop : quest.y();
        int sourceW = exclusiveChoiceSource ? exclusiveChoiceScreenW : quest.width();
        int sourceH = exclusiveChoiceSource ? exclusiveChoiceScreenH : quest.height();
        int sourceCenterX = exclusiveChoiceSource ? exclusiveChoiceCenterX : quest.centerX();
        int sourceCenterY = exclusiveChoiceSource ? exclusiveChoiceCenterY : quest.centerY();
        int targetX = exclusiveChoiceSource ? quest.x() : exclusiveChoiceBoxLeft;
        int targetY = exclusiveChoiceSource ? quest.y() : exclusiveChoiceBoxTop;
        int targetW = exclusiveChoiceSource ? quest.width() : exclusiveChoiceScreenW;
        int targetH = exclusiveChoiceSource ? quest.height() : exclusiveChoiceScreenH;
        int targetCenterX = exclusiveChoiceSource ? quest.centerX() : exclusiveChoiceCenterX;
        int targetCenterY = exclusiveChoiceSource ? quest.centerY() : exclusiveChoiceCenterY;
        lines.add(new ConnectionLine(
                connectionId, fromId, toId,
                sourceX, sourceY, sourceW, sourceH,
                targetX, targetY, targetW, targetH,
                sourceCenterX, sourceCenterY, targetCenterX, targetCenterY,
                direct, false, color, hidden, alpha, texture, textureSpacing
        ));
    }

    private static boolean intersectsViewport(int x, int y, int w, int h, int viewportW, int viewportH) {
        int marginX = CanvasLayoutService.panRenderOverscanX(viewportW);
        int marginY = CanvasLayoutService.panRenderOverscanY(viewportH);
        return x + w >= -marginX && y + h >= -marginY && x <= viewportW + marginX && y <= viewportH + marginY;
    }

    private static ConnectionLine lineFromStyle(ConnectionRenderStyle style, QuestCardLayout source, QuestCardLayout target, boolean pending) {
        return new ConnectionLine(
                style.connectionId(),
                style.sourceQuestId(),
                style.targetQuestId(),
                source.x(),
                source.y(),
                source.width(),
                source.height(),
                target.x(),
                target.y(),
                target.width(),
                target.height(),
                source.centerX(),
                source.centerY(),
                target.centerX(),
                target.centerY(),
                style.direct(),
                pending,
                style.color(),
                style.hidden(),
                style.alpha(),
                style.texture(),
                style.textureSpacing()
        );
    }
}
