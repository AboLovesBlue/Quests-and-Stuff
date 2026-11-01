package com.abolovesblue.questsandstuff.network.quest.editor;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import com.abolovesblue.questsandstuff.network.ModPacketContext;
import com.abolovesblue.questsandstuff.network.PacketBufHelper;
import com.abolovesblue.questsandstuff.quest.QuestServiceRegistry;

public record C2SEditorControlPacket(String action) {
    public static C2SEditorControlPacket decode(FriendlyByteBuf buf) {
        return new C2SEditorControlPacket(buf.readUtf());
    }

    public void encode(FriendlyByteBuf buf) {
        PacketBufHelper.writeUtfSafe(buf, action);
    }

    public void handle(ModPacketContext context) {
        ServerPlayer player = context.sender();
        if (EditorPacketGuard.canEdit(player)) {
            context.enqueueWork(() -> {
                var editor = QuestServiceRegistry.editor(player.server);
                String op = action == null ? "" : action;
                if ("undo".equals(op)) {
                    editor.undo(player);
                } else if ("redo".equals(op)) {
                    editor.redo(player);
                }
            });
        }
    }
}
