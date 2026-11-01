package com.abolovesblue.questsandstuff.network.quest.editor;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

import com.abolovesblue.questsandstuff.quest.editor.session.EditorSessionService;

@FunctionalInterface
interface EditorCommandHandler {
    void apply(ServerPlayer player, EditorSessionService editor, CompoundTag payload);
}
