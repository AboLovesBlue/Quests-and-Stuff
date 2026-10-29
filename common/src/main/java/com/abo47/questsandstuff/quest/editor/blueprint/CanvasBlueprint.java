package com.abo47.questsandstuff.quest.editor.blueprint;

import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.nbt.CompoundTag;

import com.abo47.questsandstuff.quest.model.QuestDefinition;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;

public record CanvasBlueprint(
        int schema,
        String name,
        int originX,
        int originY,
        List<QuestEntry> quests,
        List<CanvasImageLayer> images,
        List<CanvasTextLayer> texts,
        List<String> layerOrder,
        List<ExclusiveChoiceEntry> exclusiveChoices
) {
    public static final int CURRENT_SCHEMA = 1;

    public CanvasBlueprint {
        schema = schema <= 0 ? CURRENT_SCHEMA : schema;
        name = name == null ? "" : name.trim();
        quests = quests == null ? List.of() : List.copyOf(quests);
        images = images == null ? List.of() : List.copyOf(images);
        texts = texts == null ? List.of() : List.copyOf(texts);
        layerOrder = layerOrder == null ? List.of() : List.copyOf(layerOrder);
        exclusiveChoices = exclusiveChoices == null ? List.of() : List.copyOf(exclusiveChoices);
    }

    public CanvasBlueprint(String name, int originX, int originY, List<QuestEntry> quests, List<CanvasImageLayer> images, List<CanvasTextLayer> texts, List<String> layerOrder) {
        this(CURRENT_SCHEMA, name, originX, originY, quests, images, texts, layerOrder, List.of());
    }

    public CanvasBlueprint(String name, int originX, int originY, List<QuestEntry> quests, List<CanvasImageLayer> images, List<CanvasTextLayer> texts, List<String> layerOrder, List<ExclusiveChoiceEntry> exclusiveChoices) {
        this(CURRENT_SCHEMA, name, originX, originY, quests, images, texts, layerOrder, exclusiveChoices);
    }

    public boolean isEmpty() {
        return quests.isEmpty() && images.isEmpty() && texts.isEmpty() && exclusiveChoices.isEmpty();
    }

    public int contentCount() {
        return quests.size() + images.size() + texts.size() + exclusiveChoices.size();
    }

    public CompoundTag toPacketTag() {
        return BlueprintPacketCodec.toPacketTag(this);
    }

    public static CanvasBlueprint fromPacketTag(CompoundTag tag) {
        return tag == null ? empty() : BlueprintPacketCodec.fromPacketTag(tag);
    }

    public String toJson() {
        return BlueprintJsonCodec.toJson(this);
    }

    public static CanvasBlueprint fromJson(String raw) {
        if (raw == null || raw.isBlank()) {
            return empty();
        }
        return BlueprintJsonCodec.fromJson(raw);
    }

    public static CanvasBlueprint empty() {
        return new CanvasBlueprint(CURRENT_SCHEMA, "", 0, 0, List.of(), List.of(), List.of(), List.of(), List.of());
    }

    public record QuestEntry(
            String sourceId,
            String sourceChapter,
            int sourceX,
            int sourceY,
            float scale,
            QuestDefinition definition
    ) {
        public QuestEntry {
            sourceId = sourceId == null ? "" : sourceId.trim();
            sourceChapter = sourceChapter == null ? "" : sourceChapter.trim();
            if (Float.isNaN(scale) || Float.isInfinite(scale)) {
                scale = 1.0f;
            }
            scale = Math.max(0.5f, scale);
        }
    }

    public record ExclusiveChoiceEntry(
            String sourceId,
            String sourceChapter,
            int sourceX,
            int sourceY,
            int sourceW,
            int sourceH,
            int rotation,
            String background,
            List<String> connections,
            Set<String> prerequisites,
            Map<String, Integer> connectionColors,
            Map<String, String> connectionModes,
            Map<String, String> connectionTextures,
            Map<String, Integer> connectionTextureSpacings,
            Set<String> hiddenConnections
    ) {
        public ExclusiveChoiceEntry {
            sourceId = sourceId == null ? "" : sourceId.trim();
            sourceChapter = sourceChapter == null ? "" : sourceChapter.trim();
            sourceW = Math.max(1, sourceW);
            sourceH = Math.max(1, sourceH);
            connections = connections == null ? List.of() : List.copyOf(connections);
            prerequisites = prerequisites == null ? Set.of() : Set.copyOf(prerequisites);
            connectionColors = connectionColors == null ? Map.of() : Map.copyOf(connectionColors);
            connectionModes = connectionModes == null ? Map.of() : Map.copyOf(connectionModes);
            connectionTextures = connectionTextures == null ? Map.of() : Map.copyOf(connectionTextures);
            connectionTextureSpacings = connectionTextureSpacings == null ? Map.of() : Map.copyOf(connectionTextureSpacings);
            hiddenConnections = hiddenConnections == null ? Set.of() : Set.copyOf(hiddenConnections);
        }
    }
}
