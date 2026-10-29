package com.abo47.questsandstuff.client.tablet.quest.canvas.interaction;

import java.util.List;

import net.minecraft.world.entity.player.Player;

import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasViewport;
import com.abo47.questsandstuff.client.tablet.quest.canvas.layer.CanvasLayerMutations;
import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.bootstrap.TabletKeybindings;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.QuestCardLayout;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.connection.ConnectionAnimation;
import com.abo47.questsandstuff.client.tablet.quest.editor.EditorChapterCommandClient;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory;
import com.abo47.questsandstuff.quest.model.canvas.CanvasExclusiveChoice;

public final class CanvasConnectionClickActions {
    private CanvasConnectionClickActions() {
    }

    public static boolean handleQuickConnect(TabletUiState state, Player player, Runnable refresher, QuestCardLayout hit, CanvasExclusiveChoice exclusiveChoiceHit, int button) {
        if (shouldClearQuickConnect(state)) {
            clearQuickConnect(state);
        }
        if (button != 0 || !isQuickConnectActive(state)) {
            return false;
        }
        if (hit != null && !state.canvas.quickConnectExclusiveChoiceId.isBlank()) {
            return connectExclusiveChoiceToQuest(state, refresher, hit);
        }
        if (!state.canvas.quickConnectExclusiveChoiceId.isBlank()) {
            return true;
        }
        if (hit == null && exclusiveChoiceHit != null) {
            return connectQuestToExclusiveChoice(state, refresher, exclusiveChoiceHit);
        }
        if (hit == null) {
            return true;
        }
        if (state.canvas.quickConnectSourceQuestId.isBlank() || state.canvas.quickConnectSourceQuestId.equals(hit.questId())) {
            return setQuickConnectSource(state, refresher, hit);
        }
        return finalizeQuickConnect(state, player, refresher, hit);
    }

    public static boolean handlePendingConnect(TabletUiState state, Player player, Runnable refresher, QuestCardLayout hit, CanvasExclusiveChoice exclusiveChoiceHit, int button) {
        if (button != 0 || !hasPendingConnectSource(state)) {
            return false;
        }
        if (!state.canvas.connectExclusiveChoiceId.isBlank()) {
            return handlePendingExclusiveChoiceConnect(state, refresher, hit);
        }
        if (!state.canvas.connectSourceQuestId.isBlank() && exclusiveChoiceHit != null && hit == null) {
            return handlePendingExclusiveChoicePrerequisite(state, refresher, exclusiveChoiceHit);
        }
        if (hit == null) {
            return cancelPendingConnect(state, refresher);
        }
        return createPendingConnections(state, player, refresher, hit);
    }

    public static boolean handleConnectMode(CanvasViewport canvasViewport, TabletUiState state, Player player, Runnable refresher, QuestCardLayout hit, int button) {
        if (hit == null) {
            return true;
        }
        if (state.canvas.connectSourceQuestId.isBlank() || state.canvas.connectSourceQuestId.equals(hit.questId())) {
            state.canvas.connectSourceQuestId = hit.questId();
            state.canvas.canvasSelection.selectOnlyQuest(hit.questId());
            refresher.run();
            return true;
        }
        return finalizeConnectMode(canvasViewport, state, player, refresher, hit, button);
    }

    private static boolean shouldClearQuickConnect(TabletUiState state) {
        return !isQuickConnectActive(state) && !state.canvas.quickConnectSourceQuestId.isBlank() && !state.canvas.quickConnectExclusiveChoiceId.isBlank();
    }

    private static void clearQuickConnect(TabletUiState state) {
        state.canvas.quickConnectSourceQuestId = "";
        state.canvas.quickConnectExclusiveChoiceId = "";
    }

    private static boolean isQuickConnectActive(TabletUiState state) {
        return state.canvas.quickConnectHeld || TabletKeybindings.quickConnectDown();
    }

    private static boolean hasPendingConnectSource(TabletUiState state) {
        return !state.canvas.connectSourceQuestId.isBlank()
                || !state.canvas.connectSourceQuestIds.isEmpty()
                || !state.canvas.connectExclusiveChoiceId.isBlank();
    }

    private static boolean connectExclusiveChoiceToQuest(TabletUiState state, Runnable refresher, QuestCardLayout hit) {
        String chapter = EditorChapterCommandClient.selectedChapterName(state);
        CanvasExclusiveChoice ec = CanvasLayerMutations.findCanvasExclusiveChoice(state, chapter, state.canvas.quickConnectExclusiveChoiceId);
        if (ec != null && !ec.connectionQuestIds().contains(hit.questId())) {
            CanvasExclusiveChoice updated = ec.addConnection(hit.questId());
            CanvasLayerMutations.putCanvasExclusiveChoice(state, chapter, updated);
            CanvasLayerMutations.persistCanvasExclusiveChoice(state, chapter, updated.id());
            ConnectionAnimation.startIfNew(state, hit.questId(), state.canvas.quickConnectExclusiveChoiceId);
        }
        state.canvas.canvasSelection.selectOnlyQuest(hit.questId());
        state.chapterPanel.lastJumpQuest = hit.questId();
        QuestsAndStuffMod.debugLog("[QnS:UI] canvas quick-connect exclusive_choice={} quest={}", state.canvas.quickConnectExclusiveChoiceId, hit.questId());
        refresher.run();
        return true;
    }

    private static boolean connectQuestToExclusiveChoice(TabletUiState state, Runnable refresher, CanvasExclusiveChoice exclusiveChoiceHit) {
        if (!state.canvas.quickConnectSourceQuestId.isBlank()) {
            String chapter = EditorChapterCommandClient.selectedChapterName(state);
            CanvasExclusiveChoice ec = CanvasLayerMutations.findCanvasExclusiveChoice(state, chapter, exclusiveChoiceHit.id());
            if (ec != null && !ec.prerequisiteQuestIds().contains(state.canvas.quickConnectSourceQuestId)) {
                CanvasExclusiveChoice updated = ec.addPrerequisite(state.canvas.quickConnectSourceQuestId);
                CanvasLayerMutations.putCanvasExclusiveChoice(state, chapter, updated);
                CanvasLayerMutations.persistCanvasExclusiveChoice(state, chapter, updated.id());
                ConnectionAnimation.startIfNew(state, exclusiveChoiceHit.id(), state.canvas.quickConnectSourceQuestId);
            }
            state.canvas.quickConnectExclusiveChoiceId = exclusiveChoiceHit.id();
            state.canvas.quickConnectSourceQuestId = "";
            state.canvas.canvasSelection.selectOnlyExclusiveChoice(exclusiveChoiceHit.id());
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas quick-connect quest={} exclusive_choice={}", state.canvas.quickConnectSourceQuestId, exclusiveChoiceHit.id());
            refresher.run();
            return true;
        }
        state.canvas.quickConnectExclusiveChoiceId = exclusiveChoiceHit.id();
        state.canvas.quickConnectSourceQuestId = "";
        QuestsAndStuffMod.debugLog("[QnS:UI] canvas quick-connect exclusive_choice source={}", exclusiveChoiceHit.id());
        refresher.run();
        return true;
    }

    private static boolean setQuickConnectSource(TabletUiState state, Runnable refresher, QuestCardLayout hit) {
        state.canvas.quickConnectSourceQuestId = hit.questId();
        state.canvas.canvasSelection.questIds().clear();
        state.canvas.canvasSelection.questIds().add(hit.questId());
        state.chapterPanel.lastJumpQuest = hit.questId();
        QuestsAndStuffMod.debugLog("[QnS:UI] canvas quick-connect source quest={}", hit.questId());
        refresher.run();
        return true;
    }

    private static boolean finalizeQuickConnect(TabletUiState state, Player player, Runnable refresher, QuestCardLayout hit) {
        String sourceQuestId = state.canvas.quickConnectSourceQuestId;
        ConnectionAnimation.startIfNew(state, hit.questId(), sourceQuestId);
        TabletUiFactory.runPrerequisiteAction(player, hit.questId(), sourceQuestId, true);
        state.canvas.quickConnectSourceQuestId = hit.questId();
        state.canvas.canvasSelection.questIds().clear();
        state.canvas.canvasSelection.questIds().add(hit.questId());
        state.chapterPanel.lastJumpQuest = hit.questId();
        state.canvas.connectSourceQuestId = "";
        state.canvas.connectSourceQuestIds.clear();
        QuestsAndStuffMod.debugLog("[QnS:UI] canvas quick-connect prerequisite={} quest={}", sourceQuestId, hit.questId());
        refresher.run();
        return true;
    }

    private static boolean handlePendingExclusiveChoiceConnect(TabletUiState state, Runnable refresher, QuestCardLayout hit) {
        if (hit == null) {
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas ec connect cancelled id={}", state.canvas.connectExclusiveChoiceId);
            state.canvas.connectExclusiveChoiceId = "";
            refresher.run();
            return true;
        }
        String chapter = EditorChapterCommandClient.selectedChapterName(state);
        CanvasExclusiveChoice ec = CanvasLayerMutations.findCanvasExclusiveChoice(state, chapter, state.canvas.connectExclusiveChoiceId);
        if (ec != null && !ec.connectionQuestIds().contains(hit.questId())) {
            CanvasExclusiveChoice updated = ec.addConnection(hit.questId());
            CanvasLayerMutations.putCanvasExclusiveChoice(state, chapter, updated);
            CanvasLayerMutations.persistCanvasExclusiveChoice(state, chapter, updated.id());
            ConnectionAnimation.startIfNew(state, hit.questId(), state.canvas.connectExclusiveChoiceId);
        }
        state.canvas.connectExclusiveChoiceId = "";
        state.canvas.canvasSelection.selectOnlyQuest(hit.questId());
        state.chapterPanel.lastJumpQuest = hit.questId();
        QuestsAndStuffMod.debugLog("[QnS:UI] canvas ec connect exclusive_choice={} quest={}", state.canvas.connectExclusiveChoiceId, hit.questId());
        refresher.run();
        return true;
    }

    private static boolean handlePendingExclusiveChoicePrerequisite(TabletUiState state, Runnable refresher, CanvasExclusiveChoice exclusiveChoiceHit) {
        String chapter = EditorChapterCommandClient.selectedChapterName(state);
        CanvasExclusiveChoice ec = CanvasLayerMutations.findCanvasExclusiveChoice(state, chapter, exclusiveChoiceHit.id());
        if (ec != null && !ec.prerequisiteQuestIds().contains(state.canvas.connectSourceQuestId)) {
            CanvasExclusiveChoice updated = ec.addPrerequisite(state.canvas.connectSourceQuestId);
            CanvasLayerMutations.putCanvasExclusiveChoice(state, chapter, updated);
            CanvasLayerMutations.persistCanvasExclusiveChoice(state, chapter, updated.id());
            ConnectionAnimation.startIfNew(state, exclusiveChoiceHit.id(), state.canvas.connectSourceQuestId);
        }
        state.canvas.connectSourceQuestId = "";
        state.canvas.connectSourceQuestIds.clear();
        state.canvas.canvasSelection.selectOnlyExclusiveChoice(exclusiveChoiceHit.id());
        QuestsAndStuffMod.debugLog("[QnS:UI] canvas ec prerequisite connect quest={} ec={}", state.canvas.connectSourceQuestId, exclusiveChoiceHit.id());
        refresher.run();
        return true;
    }

    private static boolean cancelPendingConnect(TabletUiState state, Runnable refresher) {
        if (!state.canvas.connectSourceQuestIds.isEmpty() || !state.canvas.connectSourceQuestId.isBlank()) {
            state.canvas.connectSourceQuestIds.add(state.canvas.connectSourceQuestId);
        }
        QuestsAndStuffMod.debugLog("[QnS:UI] canvas connect cancelled sources={}", state.canvas.connectSourceQuestIds);
        state.canvas.connectSourceQuestId = "";
        state.canvas.connectSourceQuestIds.clear();
        refresher.run();
        return true;
    }

    private static boolean createPendingConnections(TabletUiState state, Player player, Runnable refresher, QuestCardLayout hit) {
        if (state.canvas.connectSourceQuestIds.isEmpty() && !state.canvas.connectSourceQuestId.isBlank()) {
            state.canvas.connectSourceQuestIds.add(state.canvas.connectSourceQuestId);
        }
        int connected = 0;
        for (String sourceQuestId : List.copyOf(state.canvas.connectSourceQuestIds)) {
            if (sourceQuestId == null || sourceQuestId.isBlank() || sourceQuestId.equals(hit.questId())) {
                continue;
            }
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas connect prerequisite={} quest={}", sourceQuestId, hit.questId());
            ConnectionAnimation.startIfNew(state, hit.questId(), sourceQuestId);
            TabletUiFactory.runPrerequisiteAction(player, hit.questId(), sourceQuestId, true);
            connected++;
        }
        List<String> attemptedSources = List.copyOf(state.canvas.connectSourceQuestIds);
        state.canvas.connectSourceQuestId = "";
        state.canvas.connectSourceQuestIds.clear();
        if (connected > 0) {
            state.canvas.canvasSelection.selectOnlyQuest(hit.questId());
            state.chapterPanel.lastJumpQuest = hit.questId();
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas connect completed sources={} target={}", attemptedSources, hit.questId());
        } else {
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas connect ended without new connection sources={} target={}", attemptedSources, hit.questId());
        }
        refresher.run();
        return true;
    }

    private static boolean finalizeConnectMode(CanvasViewport canvasViewport, TabletUiState state, Player player, Runnable refresher, QuestCardLayout hit, int button) {
        boolean addPrerequisite = !(canvasViewport.shiftDown() || canvasViewport.ctrlDown() || button == 1);
        if (state.canvas.connectSourceQuestIds.isEmpty()) {
            state.canvas.connectSourceQuestIds.add(state.canvas.connectSourceQuestId);
        }
        for (String sourceQuestId : List.copyOf(state.canvas.connectSourceQuestIds)) {
            if (!sourceQuestId.equals(hit.questId())) {
                if (addPrerequisite) {
                    ConnectionAnimation.startIfNew(state, hit.questId(), sourceQuestId);
                }
                TabletUiFactory.runPrerequisiteAction(player, hit.questId(), sourceQuestId, addPrerequisite);
            }
        }
        state.canvas.canvasSelection.selectOnlyQuest(hit.questId());
        state.canvas.connectSourceQuestId = "";
        state.canvas.connectSourceQuestIds.clear();
        refresher.run();
        return true;
    }
}
