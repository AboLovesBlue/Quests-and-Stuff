package com.abolovesblue.questsandstuff.network.quest.runtime;

import net.minecraft.network.FriendlyByteBuf;

import com.abolovesblue.questsandstuff.network.ModPacketContext;
import com.abolovesblue.questsandstuff.network.PacketBufHelper;
import com.abolovesblue.questsandstuff.network.PacketHandlerHelper;
import com.abolovesblue.questsandstuff.quest.QuestServiceRegistry;

public record C2STogglePinPacket(String questId) {
    public static C2STogglePinPacket decode(FriendlyByteBuf buf) {
        return new C2STogglePinPacket(buf.readUtf());
    }

    public void encode(FriendlyByteBuf buf) {
        PacketBufHelper.writeUtfSafe(buf, questId);
    }

    public void handle(ModPacketContext context) {
        String normalizedQuestId = questId == null ? "" : questId.trim();
        if (normalizedQuestId.isBlank()) {
            return;
        }
        PacketHandlerHelper.onServer(context, player ->
                QuestServiceRegistry.engine(player.server).togglePin(player, normalizedQuestId));
    }
}
