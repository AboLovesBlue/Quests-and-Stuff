package com.abo47.questsandstuff.client.tablet.quest.details.task;
import com.abo47.questsandstuff.client.tablet.quest.details.task.model.QuestTaskJsonFactory;

import net.minecraft.nbt.CompoundTag;

import com.google.gson.JsonObject;

public record QuestTaskEntry(String id, CompoundTag tag, JsonObject json) {
    public String type() {
        String tagType = tag.getString("type");
        return tagType == null || tagType.isBlank() ? QuestTaskJsonFactory.asString(json, "type", "") : tagType;
    }
}
