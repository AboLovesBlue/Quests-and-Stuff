package com.abo47.questsandstuff.quest.editor.blueprint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.quest.model.QuestDefinition;
import com.abo47.questsandstuff.quest.model.canvas.CanvasLayerNbtCodec;

final class BlueprintPacketCodec {
    private BlueprintPacketCodec() {
    }

    static CompoundTag toPacketTag(CanvasBlueprint blueprint) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("schema", blueprint.schema());
        tag.putString("name", blueprint.name());
        tag.putInt("origin_x", blueprint.originX());
        tag.putInt("origin_y", blueprint.originY());
        ListTag questTags = new ListTag();
        for (CanvasBlueprint.QuestEntry quest : blueprint.quests()) {
            CompoundTag entryTag = new CompoundTag();
            entryTag.putString("source_id", quest.sourceId());
            entryTag.putString("source_chapter", quest.sourceChapter());
            entryTag.putInt("source_x", quest.sourceX());
            entryTag.putInt("source_y", quest.sourceY());
            entryTag.putFloat("scale", quest.scale());
            Tag definitionTag = QuestDefinition.CODEC.encodeStart(NbtOps.INSTANCE, quest.definition())
                    .resultOrPartial(diagnostic -> QuestsAndStuffMod.LOGGER.warn(
                            "[QnS:Blueprint] Failed encoding quest definition for packet source={} diagnostic={}",
                            quest.sourceId(),
                            diagnostic
                    ))
                    .orElseGet(CompoundTag::new);
            if (definitionTag instanceof CompoundTag compound) {
                entryTag.put("definition", compound);
            }
            questTags.add(entryTag);
        }
        tag.put("quests", questTags);
        tag.put("images", CanvasLayerNbtCodec.imagesToListTag(blueprint.images()));
        tag.put("texts", CanvasLayerNbtCodec.textsToListTag(blueprint.texts()));
        tag.put("exclusive_choices", exclusiveChoicesToListTag(blueprint.exclusiveChoices()));
        tag.put("layer_order", CanvasLayerNbtCodec.stringsToListTag(blueprint.layerOrder()));
        return tag;
    }

    static CanvasBlueprint fromPacketTag(CompoundTag tag) {
        List<CanvasBlueprint.QuestEntry> quests = new ArrayList<>();
        ListTag questTags = tag.getList("quests", Tag.TAG_COMPOUND);
        for (int i = 0; i < questTags.size(); i++) {
            CompoundTag entryTag = questTags.getCompound(i);
            QuestDefinition definition = definitionFromNbt(entryTag.get("definition"));
            if (definition == null) {
                continue;
            }
            quests.add(new CanvasBlueprint.QuestEntry(
                    entryTag.getString("source_id"),
                    entryTag.getString("source_chapter"),
                    entryTag.getInt("source_x"),
                    entryTag.getInt("source_y"),
                    entryTag.getFloat("scale"),
                    definition
            ));
        }
        return new CanvasBlueprint(
                tag.contains("schema", Tag.TAG_INT) ? tag.getInt("schema") : CanvasBlueprint.CURRENT_SCHEMA,
                tag.getString("name"),
                tag.getInt("origin_x"),
                tag.getInt("origin_y"),
                quests,
                CanvasLayerNbtCodec.imagesFromListTag(tag.getList("images", Tag.TAG_COMPOUND)),
                CanvasLayerNbtCodec.textsFromListTag(tag.getList("texts", Tag.TAG_COMPOUND)),
                CanvasLayerNbtCodec.stringsFromListTag(tag.getList("layer_order", Tag.TAG_STRING)),
                exclusiveChoicesFromListTag(tag.getList("exclusive_choices", Tag.TAG_COMPOUND))
        );
    }

    private static QuestDefinition definitionFromNbt(Tag tag) {
        if (tag == null) {
            return null;
        }
        return QuestDefinition.CODEC.parse(NbtOps.INSTANCE, tag)
                .resultOrPartial(diagnostic -> QuestsAndStuffMod.LOGGER.warn(
                        "[QnS:Blueprint] Failed parsing quest definition from packet diagnostic={}",
                        diagnostic
                ))
                .orElse(null);
    }

    private static ListTag exclusiveChoicesToListTag(List<CanvasBlueprint.ExclusiveChoiceEntry> entries) {
        ListTag list = new ListTag();
        for (CanvasBlueprint.ExclusiveChoiceEntry entry : entries) {
            CompoundTag entryTag = new CompoundTag();
            entryTag.putString("source_id", entry.sourceId());
            entryTag.putString("source_chapter", entry.sourceChapter());
            entryTag.putInt("source_x", entry.sourceX());
            entryTag.putInt("source_y", entry.sourceY());
            entryTag.putInt("source_w", entry.sourceW());
            entryTag.putInt("source_h", entry.sourceH());
            entryTag.putInt("rotation", entry.rotation());
            entryTag.putString("background", entry.background());
            entryTag.put("connections", CanvasLayerNbtCodec.stringsToListTag(entry.connections()));
            entryTag.put("prerequisites", CanvasLayerNbtCodec.stringsToListTag(List.copyOf(entry.prerequisites())));
            putInts(entryTag, "connection_colors", entry.connectionColors());
            putStrings(entryTag, "connection_modes", entry.connectionModes());
            putStrings(entryTag, "connection_textures", entry.connectionTextures());
            putInts(entryTag, "connection_texture_spacings", entry.connectionTextureSpacings());
            if (!entry.hiddenConnections().isEmpty()) {
                entryTag.put("hidden_connections", CanvasLayerNbtCodec.stringsToListTag(new ArrayList<>(entry.hiddenConnections())));
            }
            list.add(entryTag);
        }
        return list;
    }

    private static void putInts(CompoundTag tag, String key, Map<String, Integer> values) {
        if (!values.isEmpty()) {
            CompoundTag compound = new CompoundTag();
            for (Map.Entry<String, Integer> e : values.entrySet()) {
                compound.putInt(e.getKey(), e.getValue());
            }
            tag.put(key, compound);
        }
    }

    private static void putStrings(CompoundTag tag, String key, Map<String, String> values) {
        if (!values.isEmpty()) {
            CompoundTag compound = new CompoundTag();
            for (Map.Entry<String, String> e : values.entrySet()) {
                compound.putString(e.getKey(), e.getValue());
            }
            tag.put(key, compound);
        }
    }

    private static List<CanvasBlueprint.ExclusiveChoiceEntry> exclusiveChoicesFromListTag(ListTag list) {
        List<CanvasBlueprint.ExclusiveChoiceEntry> entries = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entryTag = list.getCompound(i);
            Map<String, Integer> connectionColors = intMap(entryTag, "connection_colors");
            Map<String, String> connectionModes = stringMap(entryTag, "connection_modes");
            Map<String, String> connectionTextures = stringMap(entryTag, "connection_textures");
            Map<String, Integer> connectionTextureSpacings = intMap(entryTag, "connection_texture_spacings");
            Set<String> hiddenConnections = Set.copyOf(CanvasLayerNbtCodec.stringsFromListTag(entryTag.getList("hidden_connections", Tag.TAG_STRING)));
            entries.add(new CanvasBlueprint.ExclusiveChoiceEntry(
                    entryTag.getString("source_id"),
                    entryTag.getString("source_chapter"),
                    entryTag.getInt("source_x"),
                    entryTag.getInt("source_y"),
                    entryTag.getInt("source_w"),
                    entryTag.getInt("source_h"),
                    entryTag.contains("rotation", Tag.TAG_INT) ? entryTag.getInt("rotation") : 0,
                    entryTag.getString("background"),
                    CanvasLayerNbtCodec.stringsFromListTag(entryTag.getList("connections", Tag.TAG_STRING)),
                    Set.copyOf(CanvasLayerNbtCodec.stringsFromListTag(entryTag.getList("prerequisites", Tag.TAG_STRING))),
                    connectionColors,
                    connectionModes,
                    connectionTextures,
                    connectionTextureSpacings,
                    hiddenConnections
            ));
        }
        return entries;
    }

    private static Map<String, Integer> intMap(CompoundTag tag, String key) {
        Map<String, Integer> map = new HashMap<>();
        if (tag.contains(key, Tag.TAG_COMPOUND)) {
            CompoundTag compound = tag.getCompound(key);
            for (String mapKey : compound.getAllKeys()) {
                if (compound.contains(mapKey, Tag.TAG_INT)) {
                    map.put(mapKey, compound.getInt(mapKey));
                }
            }
        }
        return map;
    }

    private static Map<String, String> stringMap(CompoundTag tag, String key) {
        Map<String, String> map = new HashMap<>();
        if (tag.contains(key, Tag.TAG_COMPOUND)) {
            CompoundTag compound = tag.getCompound(key);
            for (String mapKey : compound.getAllKeys()) {
                if (compound.contains(mapKey, Tag.TAG_STRING)) {
                    map.put(mapKey, compound.getString(mapKey));
                }
            }
        }
        return map;
    }
}
