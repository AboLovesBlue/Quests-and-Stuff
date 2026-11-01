package com.abolovesblue.questsandstuff.client.tablet.quest;

import net.minecraft.world.entity.player.Player;

import com.abolovesblue.questsandstuff.QuestsAndStuffMod;
import com.abolovesblue.questsandstuff.client.tablet.ui.IntegratedServerActions;
import com.abolovesblue.questsandstuff.network.ModNetwork;
import com.abolovesblue.questsandstuff.network.quest.runtime.C2SClaimAllRewardsPacket;
import com.abolovesblue.questsandstuff.quest.QuestServiceRegistry;

public final class RewardClaimHandler {
    private RewardClaimHandler() {
    }

    public static void claimAll(Player player, String questId) {
        String targetQuestId = questId == null ? "" : questId.trim();
        IntegratedServerActions.run(
                player,
                serverPlayer -> QuestServiceRegistry.engine(serverPlayer.server).claimAllRewards(serverPlayer, targetQuestId),
                () -> ModNetwork.sendToServer(new C2SClaimAllRewardsPacket(targetQuestId)));
        QuestsAndStuffMod.debugLog("[QnS:UI] claim_all_rewards quest={}", targetQuestId.isBlank() ? "<all>" : targetQuestId);
    }
}
