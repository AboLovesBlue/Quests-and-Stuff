package com.abolovesblue.questsandstuff.network.quest.runtime;

import net.minecraft.network.FriendlyByteBuf;

import com.abolovesblue.questsandstuff.network.ModPacketContext;
import com.abolovesblue.questsandstuff.network.PacketBufHelper;
import com.abolovesblue.questsandstuff.network.PacketHandlerHelper;
import com.abolovesblue.questsandstuff.quest.QuestServiceRegistry;

public record C2SClaimAllRewardsPacket(String questId) {
    public static C2SClaimAllRewardsPacket decode(FriendlyByteBuf buf) {
        return new C2SClaimAllRewardsPacket(buf.readUtf());
    }

    public void encode(FriendlyByteBuf buf) {
        PacketBufHelper.writeUtfSafe(buf, questId);
    }

    public void handle(ModPacketContext context) {
        PacketHandlerHelper.onServer(context, player ->
                QuestServiceRegistry.engine(player.server).claimAllRewards(player, questId == null ? "" : questId));
    }
}
