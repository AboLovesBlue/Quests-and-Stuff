package com.abo47.questsandstuff.client.tablet.quest.details.task;
import com.abo47.questsandstuff.client.tablet.quest.details.task.model.QuestTaskJsonFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import com.google.gson.JsonObject;

public final class QuestTaskEntries {
    private QuestTaskEntries() {
    }

    public static List<QuestTaskEntry> entries(CompoundTag tag) {
        return entries(tag, new ListTag());
    }

    public static List<QuestTaskEntry> entries(CompoundTag tag, ListTag order) {
        List<QuestTaskEntry> entries = new ArrayList<>();
        Set<String> added = new HashSet<>();
        for (int i = 0; i < order.size(); i++) {
            String key = order.getString(i);
            addEntry(entries, added, tag, key);
        }
        for (String key : tag.getAllKeys()) {
            addEntry(entries, added, tag, key);
        }
        return entries;
    }

    private static boolean addEntry(List<QuestTaskEntry> entries, Set<String> added, CompoundTag tag, String key) {
        if (key == null || key.isBlank() || added.contains(key) || !tag.contains(key, Tag.TAG_COMPOUND)) {
            return false;
        }
        CompoundTag entry = tag.getCompound(key);
        JsonObject json = QuestTaskJsonFactory.read(entry.getString("json"));
        if (!json.has("id")) {
            json.addProperty("id", key);
        }
        entries.add(new QuestTaskEntry(key, entry, json));
        added.add(key);
        return true;
    }
}
