package com.abolovesblue.questsandstuff.network.quest.sync;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

import com.abolovesblue.questsandstuff.network.ModPacketContext;

public record S2CDisplayCacheSyncPacket(long sequence, CompoundTag payload) {
    public static S2CDisplayCacheSyncPacket decode(FriendlyByteBuf buf) {
        long sequence = buf.readLong();
        return new S2CDisplayCacheSyncPacket(sequence, SyncPacketPayloadLimits.readNbt(buf));
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeLong(sequence);
        buf.writeNbt(payload);
    }

    public void handle(ModPacketContext context) {
        context.enqueueWork(() -> S2CSyncPacketDispatch.handleDisplayCache(sequence, payload));
    }
}
