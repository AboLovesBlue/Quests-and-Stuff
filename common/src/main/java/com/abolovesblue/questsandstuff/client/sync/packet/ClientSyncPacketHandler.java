package com.abolovesblue.questsandstuff.client.sync.packet;

import java.util.List;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import com.abolovesblue.questsandstuff.QuestsAndStuffConfig;
import com.abolovesblue.questsandstuff.QuestsAndStuffMod;
import com.abolovesblue.questsandstuff.client.quest.hud.QuestCompletionNotificationOverlay;
import com.abolovesblue.questsandstuff.client.sync.state.ClientQuestStateFacade;
import com.abolovesblue.questsandstuff.network.ModNetwork;
import com.abolovesblue.questsandstuff.network.quest.runtime.C2SClaimAllRewardsPacket;
import com.abolovesblue.questsandstuff.quest.sync.SyncKeys;

public final class ClientSyncPacketHandler {
    private ClientSyncPacketHandler() {
    }

    public static void handleFull(long sequence, int chunkIndex, int chunkCount, CompoundTag payload) {
        ClientQuestStateFacade.acceptFullChunk(sequence, chunkIndex, chunkCount, payload);
        ClientSyncUiBridge.requestActiveTabletRefresh();
    }

    public static void handleDelta(long sequence, int chunkIndex, int chunkCount, CompoundTag payload) {
        ClientQuestStateFacade.acceptDeltaChunk(sequence, chunkIndex, chunkCount, payload);
        ClientSyncUiBridge.requestActiveTabletRefresh();
    }

    public static void handleDescription(long sequence, int chunkIndex, int chunkCount, CompoundTag payload) {
        ClientQuestStateFacade.acceptDescriptionChunk(sequence, chunkIndex, chunkCount, payload);
        ClientSyncUiBridge.requestActiveTabletRefresh();
    }

    public static void handleDisplayCache(long sequence, CompoundTag payload) {
        ClientQuestStateFacade.applyDisplayCacheSync(sequence, payload);
    }

    public static void handlePinned(long sequence, List<String> pinned) {
        ClientQuestStateFacade.applyPinnedSync(sequence, pinned);
    }

    public static void handleQuestEvent(long sequence, String eventType, String questId, String rewardId) {
        ClientQuestStateFacade.applyQuestEvent(sequence, eventType, questId, rewardId);
        if ("quest_completed".equals(eventType)) {
            ClientQuestStateFacade.noteQuestCompletedForChapterNotices(questId, ClientSyncUiBridge.activeSelectedChapter());
            QuestCompletionNotificationOverlay.push(questId);
            if (QuestsAndStuffConfig.autoClaimRewardsEnabled() && questId != null && !questId.isBlank()) {
                ModNetwork.sendToServer(new C2SClaimAllRewardsPacket(""));
                QuestsAndStuffMod.debugLog("[QnS:UI] global auto-claim triggered by quest={}", questId);
            }
            ClientSyncUiBridge.requestActiveTabletRefresh();
        }
    }

    public static void handleEditorMutation(long sequence, String action, String questId, CompoundTag questTag) {
        if (SyncKeys.EditorAction.PASTE_SELECT.equals(action)) {
            QuestsAndStuffMod.debugLog("[QnS:UI:Clipboard] received paste_select chapter={} ids={}", questTag.getString(SyncKeys.EditorSelection.CHAPTER), questTag.getList(SyncKeys.EditorSelection.QUESTS, Tag.TAG_STRING));
            ClientSyncUiBridge.selectPastedQuests(questTag);
            return;
        }
        if (SyncKeys.EditorAction.ADD.equals(action)) {
            QuestsAndStuffMod.debugLog("[QnS:UI:Clipboard] received editor add quest={} prerequisites={}", questId, questTag.getList(SyncKeys.Quest.PREREQUISITES, Tag.TAG_STRING));
        }
        ClientQuestStateFacade.applyEditorMutation(sequence, action, questId, questTag);
        ClientSyncUiBridge.requestActiveTabletRefresh();
    }
}
