package com.abolovesblue.questsandstuff.network.quest.editor;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import com.abolovesblue.questsandstuff.network.ModPacketContext;
import com.abolovesblue.questsandstuff.network.PacketBufHelper;
import com.abolovesblue.questsandstuff.quest.QuestServiceRegistry;

public record C2SEditorUpdateQuestPacket(String questId, String title, String subtitle) {
    public static C2SEditorUpdateQuestPacket decode(FriendlyByteBuf buf) {
        return new C2SEditorUpdateQuestPacket(buf.readUtf(), buf.readUtf(), buf.readUtf());
    }

    public void encode(FriendlyByteBuf buf) {
        PacketBufHelper.writeUtfSafe(buf, questId);
        PacketBufHelper.writeUtfSafe(buf, title);
        PacketBufHelper.writeUtfSafe(buf, subtitle);
    }

    public void handle(ModPacketContext context) {
        ServerPlayer player = context.sender();
        if (EditorPacketGuard.canEdit(player)) {
            context.enqueueWork(() -> QuestServiceRegistry.editor(player.server)
                    .updateQuestDisplay(player, questId, title, subtitle));
        }
    }
}
