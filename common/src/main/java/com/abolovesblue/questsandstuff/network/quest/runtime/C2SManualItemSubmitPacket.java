package com.abolovesblue.questsandstuff.network.quest.runtime;

import net.minecraft.network.FriendlyByteBuf;

import com.abolovesblue.questsandstuff.network.ModPacketContext;
import com.abolovesblue.questsandstuff.network.PacketBufHelper;
import com.abolovesblue.questsandstuff.network.PacketHandlerHelper;
import com.abolovesblue.questsandstuff.quest.QuestServiceRegistry;

public record C2SManualItemSubmitPacket(String questId, String taskId) {
    public static C2SManualItemSubmitPacket decode(FriendlyByteBuf buf) {
        return new C2SManualItemSubmitPacket(buf.readUtf(), buf.readUtf());
    }

    public void encode(FriendlyByteBuf buf) {
        PacketBufHelper.writeUtfSafe(buf, questId);
        PacketBufHelper.writeUtfSafe(buf, taskId);
    }

    public void handle(ModPacketContext context) {
        PacketHandlerHelper.onServer(context, player ->
                QuestServiceRegistry.engine(player.server).submitManualItemTask(player, questId, taskId));
    }
}
