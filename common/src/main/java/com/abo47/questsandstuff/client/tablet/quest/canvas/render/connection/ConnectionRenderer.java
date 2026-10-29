package com.abo47.questsandstuff.client.tablet.quest.canvas.render.connection;

import java.util.List;
import java.util.Map;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;

import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasViewport;
import com.abo47.questsandstuff.client.sync.state.ClientQuestStateFacade;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.CanvasPoint;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.QuestCardLayout;
import com.abo47.questsandstuff.client.tablet.state.UiState;
import com.abo47.questsandstuff.quest.model.canvas.CanvasExclusiveChoice;
import com.abo47.questsandstuff.quest.model.connection.QuestConnectionMetadata;

public final class ConnectionRenderer {
    private ConnectionRenderer() {
    }

    public static String connectionKey(String sourceQuestId, String targetQuestId) {
        return QuestConnectionMetadata.connectionKey(sourceQuestId, targetQuestId);
    }

    public static int connectionColor(UiState state, String chapter, String sourceQuestId, String targetQuestId) {
        return ConnectionStyleResolver.connectionColor(state, chapter, sourceQuestId, targetQuestId, ClientQuestStateFacade.quest(targetQuestId));
    }

    public static void setConnectionColor(UiState state, String chapter, String sourceQuestId, String targetQuestId, int color) {
        ConnectionStateMutations.setConnectionColor(state, chapter, sourceQuestId, targetQuestId, color);
    }

    public static boolean isConnectionHidden(UiState state, String chapter, String sourceQuestId, String targetQuestId) {
        return ConnectionStyleResolver.isConnectionHidden(state, chapter, sourceQuestId, targetQuestId, ClientQuestStateFacade.quest(targetQuestId));
    }

    public static boolean isConnectionHidden(UiState state, String chapter, String sourceQuestId, String targetQuestId, CompoundTag target) {
        return ConnectionStyleResolver.isConnectionHidden(state, chapter, sourceQuestId, targetQuestId, target);
    }

    public static void setConnectionHidden(UiState state, String chapter, String sourceQuestId, String targetQuestId, boolean hidden) {
        ConnectionStateMutations.setConnectionHidden(state, chapter, sourceQuestId, targetQuestId, hidden);
    }

    public static void toggleConnectionHidden(UiState state, String chapter, String sourceQuestId, String targetQuestId) {
        ConnectionStateMutations.toggleConnectionHidden(state, chapter, sourceQuestId, targetQuestId);
    }

    public static boolean isConnectionDirect(UiState state, String chapter, String sourceQuestId, String targetQuestId) {
        return ConnectionStyleResolver.isConnectionDirect(state, chapter, sourceQuestId, targetQuestId, ClientQuestStateFacade.quest(targetQuestId));
    }

    public static boolean isConnectionDirect(UiState state, String chapter, String sourceQuestId, String targetQuestId, CompoundTag target) {
        return ConnectionStyleResolver.isConnectionDirect(state, chapter, sourceQuestId, targetQuestId, target);
    }

    public static void toggleConnectionMode(UiState state, String chapter, String sourceQuestId, String targetQuestId) {
        ConnectionStateMutations.toggleConnectionMode(state, chapter, sourceQuestId, targetQuestId);
    }

    public static QuestConnectionMetadata connectionMetadata(UiState state, String chapter, String sourceQuestId, String targetQuestId) {
        return ConnectionStyleResolver.metadata(state, chapter, sourceQuestId, targetQuestId, ClientQuestStateFacade.quest(targetQuestId));
    }

    public static void renderPrerequisiteConnections(
            WidgetGroup canvasViewport,
            UiState state,
            List<QuestCardLayout> cards,
            Map<String, QuestCardLayout> byQuestId,
            int viewportW,
            int viewportH
    ) {
        List<ConnectionLine> lines = ConnectionLayout.prerequisiteAndPendingConnectionLines(state, cards, byQuestId, viewportW, viewportH);
        ConnectionPainter.renderConnectionLines(canvasViewport, state, lines);
    }

    public static List<ConnectionLine> prerequisiteConnectionLines(
            UiState state,
            List<QuestCardLayout> cards,
            Map<String, QuestCardLayout> byQuestId,
            int viewportW,
            int viewportH
    ) {
        return ConnectionLayout.prerequisiteConnectionLines(state, cards, byQuestId, viewportW, viewportH);
    }

    public static List<String> prerequisiteConnectionLayerKeys(
            UiState state,
            List<QuestCardLayout> cards,
            Map<String, QuestCardLayout> byQuestId,
            int viewportW,
            int viewportH
    ) {
        return ConnectionLayout.prerequisiteConnectionLayerKeys(state, cards, byQuestId, viewportW, viewportH);
    }

    public static void renderConnectionLayer(WidgetGroup canvasViewport, UiState state, ConnectionLine line) {
        ConnectionPainter.renderConnectionLines(canvasViewport, state, line == null ? List.of() : List.of(line));
    }

    public static void renderPendingConnections(
            WidgetGroup canvasViewport,
            UiState state,
            Map<String, QuestCardLayout> byQuestId,
            int viewportW,
            int viewportH
    ) {
        ConnectionPainter.renderConnectionLines(canvasViewport, state, ConnectionLayout.pendingConnectionLines(state, byQuestId, viewportW, viewportH));
    }

    public static List<CanvasPoint> connectionPath(UiState state, int originX, int originY, int sourceX, int sourceY, int targetX, int targetY, boolean direct) {
        return ConnectionPainter.connectionPath(state, originX, originY, sourceX, sourceY, targetX, targetY, direct);
    }

    public static void drawStaticChevrons(GuiGraphics graphics, List<CanvasPoint> path, int color, int alpha, int clipMinX, int clipMinY, int clipMaxX, int clipMaxY) {
        ConnectionPainter.drawStaticChevrons(graphics, path, color, alpha, clipMinX, clipMinY, clipMaxX, clipMaxY);
    }

    public static void drawStaticChevrons(GuiGraphics graphics, List<CanvasPoint> path, int color, int alpha, float scale, int clipMinX, int clipMinY, int clipMaxX, int clipMaxY) {
        ConnectionPainter.drawStaticChevrons(graphics, path, color, alpha, scale, clipMinX, clipMinY, clipMaxX, clipMaxY);
    }

    public static void drawTexturedChevrons(GuiGraphics graphics, List<CanvasPoint> path, int color, int alpha, float scale, String textureStr, int clipMinX, int clipMinY, int clipMaxX, int clipMaxY) {
        ConnectionPainter.drawTexturedChevrons(graphics, path, color, alpha, scale, textureStr, clipMinX, clipMinY, clipMaxX, clipMaxY);
    }

    public static CanvasExclusiveChoice findExclusiveChoice(UiState state, String chapter, String id) {
        return ConnectionStyleResolver.findExclusiveChoice(state, chapter, id);
    }

    public static boolean isExclusiveChoiceId(UiState state, String chapter, String id) {
        return ConnectionStyleResolver.isExclusiveChoiceId(state, chapter, id);
    }

    public static int exclusiveChoiceConnectionColor(UiState state, String chapter, String sourceQuestId, String targetQuestId) {
        return ConnectionStyleResolver.exclusiveChoiceConnectionColor(state, chapter, sourceQuestId, targetQuestId);
    }

    public static boolean exclusiveChoiceIsConnectionDirect(UiState state, String chapter, String sourceQuestId, String targetQuestId) {
        return ConnectionStyleResolver.exclusiveChoiceIsConnectionDirect(state, chapter, sourceQuestId, targetQuestId);
    }

    public static void setExclusiveChoiceConnectionColor(UiState state, String chapter, String exclusiveChoiceId, String questId, int color) {
        ConnectionStateMutations.setExclusiveChoiceConnectionColor(state, chapter, exclusiveChoiceId, questId, color);
    }

    public static void setExclusiveChoiceConnectionTexture(UiState state, String chapter, String exclusiveChoiceId, String questId, String texture) {
        ConnectionStateMutations.setExclusiveChoiceConnectionTexture(state, chapter, exclusiveChoiceId, questId, texture);
    }

    public static void setExclusiveChoiceConnectionTextureSpacing(UiState state, String chapter, String exclusiveChoiceId, String questId, int spacing) {
        ConnectionStateMutations.setExclusiveChoiceConnectionTextureSpacing(state, chapter, exclusiveChoiceId, questId, spacing);
    }

    public static void setExclusiveChoiceConnectionMode(UiState state, String chapter, String exclusiveChoiceId, String questId, boolean direct) {
        ConnectionStateMutations.setExclusiveChoiceConnectionMode(state, chapter, exclusiveChoiceId, questId, direct);
    }

    public static void setExclusiveChoiceConnectionHidden(UiState state, String chapter, String exclusiveChoiceId, String questId, boolean hidden) {
        ConnectionStateMutations.setExclusiveChoiceConnectionHidden(state, chapter, exclusiveChoiceId, questId, hidden);
    }

    public static void setConnectionTexture(UiState state, String chapter, String sourceQuestId, String targetQuestId, String texture) {
        ConnectionStateMutations.setConnectionTexture(state, chapter, sourceQuestId, targetQuestId, texture);
    }

    public static void setConnectionTextureSpacing(UiState state, String chapter, String sourceQuestId, String targetQuestId, int spacing) {
        ConnectionStateMutations.setConnectionTextureSpacing(state, chapter, sourceQuestId, targetQuestId, spacing);
    }

    public static String connectionTexture(UiState state, String chapter, String sourceQuestId, String targetQuestId) {
        return ConnectionStyleResolver.connectionTexture(state, chapter, sourceQuestId, targetQuestId, ClientQuestStateFacade.quest(targetQuestId));
    }

    public static String exclusiveChoiceConnectionTexture(UiState state, String chapter, String sourceQuestId, String targetQuestId) {
        return ConnectionStyleResolver.exclusiveChoiceConnectionTexture(state, chapter, sourceQuestId, targetQuestId);
    }

    public static int exclusiveChoiceConnectionTextureSpacing(UiState state, String chapter, String sourceQuestId, String targetQuestId) {
        return ConnectionStyleResolver.exclusiveChoiceConnectionTextureSpacing(state, chapter, sourceQuestId, targetQuestId);
    }

    public static void removeConnectionTransientState(UiState state, String chapter, String sourceQuestId, String targetQuestId) {
        ConnectionStateMutations.removeConnectionTransientState(state, chapter, sourceQuestId, targetQuestId);
    }
}
