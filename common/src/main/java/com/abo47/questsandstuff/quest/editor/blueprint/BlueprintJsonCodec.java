package com.abo47.questsandstuff.quest.editor.blueprint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.quest.model.QuestDefinition;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasLayerNbtCodec;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;
import com.abo47.questsandstuff.quest.persistence.GsonProvider;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

final class BlueprintJsonCodec {
    private static final Gson GSON = GsonProvider.GSON;

    private BlueprintJsonCodec() {
    }

    static String toJson(CanvasBlueprint blueprint) {
        JsonObject root = new JsonObject();
        root.addProperty("schema", blueprint.schema());
        root.addProperty("name", blueprint.name());
        root.addProperty("origin_x", blueprint.originX());
        root.addProperty("origin_y", blueprint.originY());
        JsonArray questArray = new JsonArray();
        for (CanvasBlueprint.QuestEntry quest : blueprint.quests()) {
            JsonObject entry = new JsonObject();
            entry.addProperty("source_id", quest.sourceId());
            entry.addProperty("source_chapter", quest.sourceChapter());
            entry.addProperty("source_x", quest.sourceX());
            entry.addProperty("source_y", quest.sourceY());
            entry.addProperty("scale", quest.scale());
            entry.add("definition", QuestDefinition.CODEC.encodeStart(JsonOps.INSTANCE, quest.definition())
                    .resultOrPartial(diagnostic -> QuestsAndStuffMod.LOGGER.warn(
                            "[QnS:Blueprint] Failed encoding quest definition for json source={} diagnostic={}",
                            quest.sourceId(),
                            diagnostic
                    ))
                    .orElseGet(JsonObject::new));
            questArray.add(entry);
        }
        root.add("quests", questArray);
        root.add("images", imagesToJson(blueprint.images()));
        root.add("texts", textsToJson(blueprint.texts()));
        root.add("exclusive_choices", exclusiveChoicesToJson(blueprint.exclusiveChoices()));
        JsonArray order = new JsonArray();
        for (String key : blueprint.layerOrder()) {
            if (key != null && !key.isBlank()) {
                order.add(key);
            }
        }
        root.add("layer_order", order);
        return GSON.toJson(root);
    }

    static CanvasBlueprint fromJson(String raw) {
        try {
            JsonObject root = JsonParser.parseString(raw).getAsJsonObject();
            List<CanvasBlueprint.QuestEntry> quests = new ArrayList<>();
            JsonArray questArray = array(root, "quests");
            for (JsonElement element : questArray) {
                if (!element.isJsonObject()) {
                    continue;
                }
                JsonObject entry = element.getAsJsonObject();
                String sourceId = string(entry, "source_id");
                QuestDefinition definition = QuestDefinition.CODEC.parse(JsonOps.INSTANCE, entry.get("definition"))
                        .resultOrPartial(diagnostic -> QuestsAndStuffMod.LOGGER.warn(
                                "[QnS:Blueprint] Failed parsing quest definition from json source={} diagnostic={}",
                                sourceId,
                                diagnostic
                        ))
                        .orElse(null);
                if (definition == null) {
                    continue;
                }
                quests.add(new CanvasBlueprint.QuestEntry(
                        sourceId,
                        string(entry, "source_chapter"),
                        integer(entry, "source_x", 0, "quest:" + sourceId),
                        integer(entry, "source_y", 0, "quest:" + sourceId),
                        floating(entry, "scale", 1.0f, "quest:" + sourceId),
                        definition
                ));
            }

            List<String> order = new ArrayList<>();
            JsonArray orderArray = array(root, "layer_order");
            for (JsonElement element : orderArray) {
                String value = stringValue(element, "layer_order");
                if (!value.isBlank()) {
                    order.add(value);
                }
            }
            return new CanvasBlueprint(
                    integer(root, "schema", CanvasBlueprint.CURRENT_SCHEMA, "root"),
                    string(root, "name"),
                    integer(root, "origin_x", 0, "root"),
                    integer(root, "origin_y", 0, "root"),
                    quests,
                    imagesFromJson(array(root, "images")),
                    textsFromJson(array(root, "texts")),
                    order,
                    exclusiveChoicesFromJson(array(root, "exclusive_choices"))
            );
        } catch (RuntimeException exception) {
            QuestsAndStuffMod.LOGGER.warn(
                    "[QnS:Blueprint] Failed parsing blueprint json length={}",
                    raw.length(),
                    exception
            );
            return CanvasBlueprint.empty();
        }
    }

    private static List<CanvasBlueprint.ExclusiveChoiceEntry> exclusiveChoicesFromJson(JsonArray array) {
        List<CanvasBlueprint.ExclusiveChoiceEntry> entries = new ArrayList<>();
        for (JsonElement element : array) {
            if (!element.isJsonObject()) {
                continue;
            }
            JsonObject obj = element.getAsJsonObject();

            JsonArray connArray = array(obj, "connections");
            List<String> connections = new ArrayList<>();
            for (JsonElement c : connArray) {
                String value = stringValue(c, "connections");
                if (!value.isBlank()) {
                    connections.add(value);
                }
            }

            JsonArray prereqArray = array(obj, "prerequisites");
            Set<String> prerequisiteSet = new LinkedHashSet<>();
            for (JsonElement p : prereqArray) {
                String value = stringValue(p, "prerequisites");
                if (!value.isBlank()) {
                    prerequisiteSet.add(value);
                }
            }

            Map<String, Integer> connectionColors = intMap(obj, "connection_colors");
            Map<String, String> connectionModes = stringMap(obj, "connection_modes");
            Map<String, String> connectionTextures = stringMap(obj, "connection_textures");
            Map<String, Integer> connectionTextureSpacings = intMap(obj, "connection_texture_spacings");

            JsonArray hiddenArray = obj.getAsJsonArray("hidden_connections");
            Set<String> hiddenConnections = new LinkedHashSet<>();
            if (hiddenArray != null) {
                for (JsonElement h : hiddenArray) {
                    String value = stringValue(h, "hidden_connections");
                    if (!value.isBlank()) {
                        hiddenConnections.add(value);
                    }
                }
            }

            String sourceId = string(obj, "source_id");
            entries.add(new CanvasBlueprint.ExclusiveChoiceEntry(
                    sourceId,
                    string(obj, "source_chapter"),
                    integer(obj, "source_x", 0, "ec:" + sourceId),
                    integer(obj, "source_y", 0, "ec:" + sourceId),
                    integer(obj, "source_w", 79, "ec:" + sourceId),
                    integer(obj, "source_h", 79, "ec:" + sourceId),
                    integer(obj, "rotation", 0, "ec:" + sourceId),
                    string(obj, "background"),
                    connections,
                    prerequisiteSet,
                    connectionColors,
                    connectionModes,
                    connectionTextures,
                    connectionTextureSpacings,
                    hiddenConnections
            ));
        }
        return entries;
    }

    private static JsonArray exclusiveChoicesToJson(List<CanvasBlueprint.ExclusiveChoiceEntry> entries) {
        JsonArray array = new JsonArray();
        for (CanvasBlueprint.ExclusiveChoiceEntry entry : entries) {
            JsonObject obj = new JsonObject();
            obj.addProperty("source_id", entry.sourceId());
            obj.addProperty("source_chapter", entry.sourceChapter());
            obj.addProperty("source_x", entry.sourceX());
            obj.addProperty("source_y", entry.sourceY());
            obj.addProperty("source_w", entry.sourceW());
            obj.addProperty("source_h", entry.sourceH());
            obj.addProperty("rotation", entry.rotation());
            obj.addProperty("background", entry.background());
            JsonArray conns = new JsonArray();
            for (String c : entry.connections()) {
                conns.add(c);
            }
            obj.add("connections", conns);
            JsonArray prereqs = new JsonArray();
            for (String p : entry.prerequisites()) {
                prereqs.add(p);
            }
            obj.add("prerequisites", prereqs);
            putInts(obj, "connection_colors", entry.connectionColors());
            putStrings(obj, "connection_modes", entry.connectionModes());
            putStrings(obj, "connection_textures", entry.connectionTextures());
            putInts(obj, "connection_texture_spacings", entry.connectionTextureSpacings());
            if (!entry.hiddenConnections().isEmpty()) {
                JsonArray hidden = new JsonArray();
                for (String h : entry.hiddenConnections()) {
                    hidden.add(h);
                }
                obj.add("hidden_connections", hidden);
            }
            array.add(obj);
        }
        return array;
    }

    private static void putInts(JsonObject obj, String key, Map<String, Integer> values) {
        if (!values.isEmpty()) {
            JsonObject compound = new JsonObject();
            for (Map.Entry<String, Integer> e : values.entrySet()) {
                compound.addProperty(e.getKey(), e.getValue());
            }
            obj.add(key, compound);
        }
    }

    private static void putStrings(JsonObject obj, String key, Map<String, String> values) {
        if (!values.isEmpty()) {
            JsonObject compound = new JsonObject();
            for (Map.Entry<String, String> e : values.entrySet()) {
                compound.addProperty(e.getKey(), e.getValue());
            }
            obj.add(key, compound);
        }
    }

    private static Map<String, Integer> intMap(JsonObject obj, String key) {
        Map<String, Integer> map = new HashMap<>();
        JsonObject values = obj.getAsJsonObject(key);
        if (values != null) {
            for (var e : values.entrySet()) {
                if (e.getValue().isJsonPrimitive() && e.getValue().getAsJsonPrimitive().isNumber()) {
                    map.put(e.getKey(), e.getValue().getAsInt());
                }
            }
        }
        return map;
    }

    private static Map<String, String> stringMap(JsonObject obj, String key) {
        Map<String, String> map = new HashMap<>();
        JsonObject values = obj.getAsJsonObject(key);
        if (values != null) {
            for (var e : values.entrySet()) {
                if (e.getValue().isJsonPrimitive() && e.getValue().getAsJsonPrimitive().isString()) {
                    map.put(e.getKey(), e.getValue().getAsString());
                }
            }
        }
        return map;
    }

    private static JsonArray imagesToJson(List<CanvasImageLayer> images) {
        JsonArray array = new JsonArray();
        for (CanvasImageLayer image : images) {
            JsonObject entry = new JsonObject();
            entry.addProperty("id", image.id());
            entry.addProperty("asset", image.asset());
            entry.addProperty("x", image.x());
            entry.addProperty("y", image.y());
            entry.addProperty("w", image.w());
            entry.addProperty("h", image.h());
            entry.addProperty("rotation", image.rotation());
            entry.addProperty("entity_yaw", image.entityYaw());
            entry.addProperty("entity_spin_speed", image.entitySpinSpeed());
            entry.addProperty("model_pitch", image.modelPitch());
            entry.addProperty("pivot_x", image.pivotX());
            entry.addProperty("pivot_y", image.pivotY());
            array.add(entry);
        }
        return array;
    }

    private static List<CanvasImageLayer> imagesFromJson(JsonArray array) {
        List<CanvasImageLayer> images = new ArrayList<>();
        for (JsonElement element : array) {
            if (!element.isJsonObject()) {
                continue;
            }
            JsonObject entry = element.getAsJsonObject();
            String id = string(entry, "id");
            if (id.isBlank()) {
                continue;
            }
            int width = integer(entry, "w", 80, "image:" + id);
            int height = integer(entry, "h", 80, "image:" + id);
            images.add(new CanvasImageLayer(
                    id,
                    string(entry, "asset"),
                    integer(entry, "x", 0, "image:" + id),
                    integer(entry, "y", 0, "image:" + id),
                    width,
                    height,
                    integer(entry, "rotation", 0, "image:" + id),
                    integer(entry, "entity_yaw", CanvasImageLayer.DEFAULT_ENTITY_YAW, "image:" + id),
                    integer(entry, "entity_spin_speed", CanvasImageLayer.DEFAULT_ENTITY_SPIN_SPEED, "image:" + id),
                    integer(entry, "model_pitch", CanvasImageLayer.DEFAULT_MODEL_PITCH, "image:" + id),
                    integer(entry, "pivot_x", width / 2, "image:" + id),
                    integer(entry, "pivot_y", height / 2, "image:" + id)
            ));
        }
        return images;
    }

    private static JsonArray textsToJson(List<CanvasTextLayer> texts) {
        JsonArray array = new JsonArray();
        for (CanvasTextLayer text : texts) {
            array.add(nbtToJson(CanvasLayerNbtCodec.textToTag(text)));
        }
        return array;
    }

    private static List<CanvasTextLayer> textsFromJson(JsonArray array) {
        List<CanvasTextLayer> texts = new ArrayList<>();
        for (JsonElement element : array) {
            if (!element.isJsonObject()) {
                continue;
            }
            CanvasTextLayer text = CanvasLayerNbtCodec.textFromTag(jsonToNbt(element.getAsJsonObject()));
            if (text != null) {
                texts.add(text);
            }
        }
        return texts;
    }

    private static JsonObject nbtToJson(CompoundTag tag) {
        JsonObject object = new JsonObject();
        for (String key : tag.getAllKeys()) {
            Tag value = tag.get(key);
            if (value instanceof ListTag list) {
                JsonArray array = new JsonArray();
                for (int i = 0; i < list.size(); i++) {
                    Tag child = list.get(i);
                    if (child instanceof CompoundTag compound) {
                        array.add(nbtToJson(compound));
                    }
                }
                object.add(key, array);
            } else if (value != null) {
                if (tag.contains(key, Tag.TAG_INT)) {
                    object.addProperty(key, tag.getInt(key));
                } else {
                    object.addProperty(key, tag.getString(key));
                }
            }
        }
        return object;
    }

    private static CompoundTag jsonToNbt(JsonObject object) {
        CompoundTag tag = new CompoundTag();
        for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
            JsonElement value = entry.getValue();
            if (value == null || value.isJsonNull()) {
                continue;
            }
            if (value.isJsonArray()) {
                ListTag list = new ListTag();
                for (JsonElement child : value.getAsJsonArray()) {
                    if (child.isJsonObject()) {
                        list.add(jsonToNbt(child.getAsJsonObject()));
                    }
                }
                tag.put(entry.getKey(), list);
            } else if (value.isJsonPrimitive() && value.getAsJsonPrimitive().isNumber()) {
                tag.putInt(entry.getKey(), value.getAsInt());
            } else {
                tag.putString(entry.getKey(), value.getAsString());
            }
        }
        return tag;
    }

    private static JsonArray array(JsonObject object, String key) {
        JsonElement element = object == null ? null : object.get(key);
        return element != null && element.isJsonArray() ? element.getAsJsonArray() : new JsonArray();
    }

    private static String string(JsonObject object, String key) {
        JsonElement element = object == null ? null : object.get(key);
        return element == null || element.isJsonNull() ? "" : element.getAsString();
    }

    private static int integer(JsonObject object, String key, int fallback, String context) {
        try {
            JsonElement element = object == null ? null : object.get(key);
            return element == null || element.isJsonNull() ? fallback : element.getAsInt();
        } catch (RuntimeException exception) {
            QuestsAndStuffMod.LOGGER.warn(
                    "[QnS:Blueprint] Failed reading integer field context={} key={} fallback={}",
                    context,
                    key,
                    fallback,
                    exception
            );
            return fallback;
        }
    }

    private static float floating(JsonObject object, String key, float fallback, String context) {
        try {
            JsonElement element = object == null ? null : object.get(key);
            float value = element == null || element.isJsonNull() ? fallback : element.getAsFloat();
            return Float.isNaN(value) || Float.isInfinite(value) ? fallback : value;
        } catch (RuntimeException exception) {
            QuestsAndStuffMod.LOGGER.warn(
                    "[QnS:Blueprint] Failed reading float field context={} key={} fallback={}",
                    context,
                    key,
                    fallback,
                    exception
            );
            return fallback;
        }
    }

    private static String stringValue(JsonElement element, String context) {
        try {
            return element == null || element.isJsonNull() ? "" : element.getAsString();
        } catch (RuntimeException exception) {
            QuestsAndStuffMod.LOGGER.warn("[QnS:Blueprint] Failed reading string value context={}", context, exception);
            return "";
        }
    }
}
