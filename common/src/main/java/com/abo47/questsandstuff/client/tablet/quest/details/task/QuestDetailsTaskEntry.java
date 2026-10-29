package com.abo47.questsandstuff.client.tablet.quest.details.task;
import com.abo47.questsandstuff.client.tablet.quest.details.task.model.TaskJsonFactory;

import net.minecraft.nbt.CompoundTag;

import com.google.gson.JsonObject;

public record QuestDetailsTaskEntry(String id, CompoundTag tag, JsonObject json) {
    public String type() {
        String tagType = tag.getString("type");
        return tagType == null || tagType.isBlank() ? TaskJsonFactory.asString(json, "type", "") : tagType;
    }
}
