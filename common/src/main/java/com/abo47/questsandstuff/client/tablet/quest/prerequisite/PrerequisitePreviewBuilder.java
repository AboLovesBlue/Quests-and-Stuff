package com.abo47.questsandstuff.client.tablet.quest.prerequisite;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.abo47.questsandstuff.client.sync.state.ClientQuestStateFacade;
import com.abo47.questsandstuff.client.tablet.quest.canvas.blueprint.CanvasBlueprintSnapshots;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.geometry.CanvasLayerOrdering;
import com.abo47.questsandstuff.quest.editor.blueprint.CanvasBlueprint;
import com.abo47.questsandstuff.quest.model.ChapterDef;
import com.abo47.questsandstuff.quest.model.QuestDefinition;
import com.abo47.questsandstuff.quest.model.canvas.CanvasExclusiveChoice;

final class PrerequisitePreviewBuilder {
    private static final int COMPACT_X_OFFSET = 128;
    private static final int COMPACT_Y_STEP = 70;

    private PrerequisitePreviewBuilder() {
    }

    static CanvasBlueprint build(String chapter, PrerequisiteConnectionModel model, boolean externalMode) {
        if (model == null) {
            return CanvasBlueprint.empty();
        }
        QuestDefinition focus = CanvasBlueprintSnapshots.fromClientTag(model.questId(), model.questTag());
        if (focus == null) {
            return CanvasBlueprint.empty();
        }

        boolean exclusiveChoiceMode = model.isExclusiveChoice();
        Set<String> exclusiveChoiceIds = exclusiveChoiceMode ? Set.of(model.questId()) : exclusiveChoiceIdsFromRows(model.rows(), model.questId());
        Map<String, QuestDefinition> definitions = definitionsForPreview(model.questId(), focus, model.rows());
        Map<String, QuestPlacement> placements = placementsForPreview(chapter, model.questId(), definitions, model.rows(), externalMode);
        Map<String, Set<String>> prerequisitesByTarget = prerequisitesByTarget(model.rows(), exclusiveChoiceIds);
        List<CanvasBlueprint.QuestEntry> entries = new ArrayList<>();
        List<CanvasBlueprint.ExclusiveChoiceEntry> exclusiveChoiceEntries = new ArrayList<>();
        List<String> order = new ArrayList<>();
        for (Map.Entry<String, QuestDefinition> entry : definitions.entrySet()) {
            String id = entry.getKey();
            if (model.questId().equals(id)) {
                continue;
            }
            QuestPlacement placement = placements.get(id);
            if (placement == null) {
                continue;
            }
            entries.add(new CanvasBlueprint.QuestEntry(id, placement.chapter(), placement.x(), placement.y(), placement.scale(), withPrerequisites(entry.getValue(), prerequisitesByTarget.getOrDefault(id, Set.of()))));
            order.add(CanvasLayerOrdering.questKey(id));
        }
        if (exclusiveChoiceMode) {
            CanvasExclusiveChoice focusExclusiveChoice = findExclusiveChoiceById(model.questId(), chapter);
            if (focusExclusiveChoice != null) {
                exclusiveChoiceEntries.add(new CanvasBlueprint.ExclusiveChoiceEntry(
                        model.questId(), chapter, model.exclusiveChoiceX(), model.exclusiveChoiceY(),
                        model.exclusiveChoiceW(), model.exclusiveChoiceH(), focusExclusiveChoice.rotation(),
                        focusExclusiveChoice.background(), focusExclusiveChoice.connectionQuestIds(),
                        prerequisitesByTarget.getOrDefault(model.questId(), Set.of()),
                        focusExclusiveChoice.connectionColors(), focusExclusiveChoice.connectionModes(),
                        focusExclusiveChoice.connectionTextures(), focusExclusiveChoice.connectionTextureSpacings(),
                        focusExclusiveChoice.hiddenConnections()));
            } else {
                exclusiveChoiceEntries.add(new CanvasBlueprint.ExclusiveChoiceEntry(
                        model.questId(), chapter, model.exclusiveChoiceX(), model.exclusiveChoiceY(),
                        model.exclusiveChoiceW(), model.exclusiveChoiceH(), 0,
                        focus.display().questBackground(), List.of(),
                        prerequisitesByTarget.getOrDefault(model.questId(), Set.of()),
                        Map.of(), Map.of(), Map.of(), Map.of(), Set.of()));
            }
            order.add(CanvasLayerOrdering.exclusiveChoiceKey(model.questId()));
        } else {
            for (String exclusiveChoiceId : exclusiveChoiceIds) {
                CanvasExclusiveChoice ec = findExclusiveChoiceById(exclusiveChoiceId, chapter);
                if (ec == null) {
                    continue;
                }
                QuestPlacement exclusiveChoicePlacement = placements.get(exclusiveChoiceId);
                int exclusiveChoiceX = exclusiveChoicePlacement != null ? exclusiveChoicePlacement.x() : ec.x();
                int exclusiveChoiceY = exclusiveChoicePlacement != null ? exclusiveChoicePlacement.y() : ec.y();
                exclusiveChoiceEntries.add(new CanvasBlueprint.ExclusiveChoiceEntry(
                        exclusiveChoiceId, chapter, exclusiveChoiceX, exclusiveChoiceY, ec.w(), ec.h(), ec.rotation(),
                        ec.background(), ec.connectionQuestIds(),
                        prerequisitesByTarget.getOrDefault(exclusiveChoiceId, Set.of()),
                        ec.connectionColors(), ec.connectionModes(),
                        ec.connectionTextures(), ec.connectionTextureSpacings(),
                        ec.hiddenConnections()));
                order.add(CanvasLayerOrdering.exclusiveChoiceKey(exclusiveChoiceId));
            }
            QuestPlacement focusPlacement = placements.get(model.questId());
            if (focusPlacement != null) {
                entries.add(new CanvasBlueprint.QuestEntry(model.questId(), focusPlacement.chapter(), focusPlacement.x(), focusPlacement.y(), focusPlacement.scale(), withPrerequisites(focus, prerequisitesByTarget.getOrDefault(model.questId(), Set.of()))));
                order.add(CanvasLayerOrdering.questKey(model.questId()));
            }
        }
        Origin origin = origin(entries, exclusiveChoiceEntries);
        return new CanvasBlueprint(CanvasBlueprint.CURRENT_SCHEMA, focus.display().title(), origin.x(), origin.y(), entries, List.of(), List.of(), order, exclusiveChoiceEntries);
    }

    private static Map<String, QuestDefinition> definitionsForPreview(String questId, QuestDefinition focus, List<PrerequisiteConnectionRow> rows) {
        Map<String, QuestDefinition> definitions = new LinkedHashMap<>();
        definitions.put(questId, focus);
        for (PrerequisiteConnectionRow row : rows) {
            addDefinition(definitions, row.sourceId());
            addDefinition(definitions, row.targetId());
        }
        return definitions;
    }

    private static void addDefinition(Map<String, QuestDefinition> definitions, String questId) {
        if (definitions.containsKey(questId)) {
            return;
        }
        QuestDefinition definition = CanvasBlueprintSnapshots.fromClientTag(questId, ClientQuestStateFacade.quest(questId));
        if (definition != null) {
            definitions.put(questId, definition);
        }
    }

    private static Map<String, QuestPlacement> placementsForPreview(String chapter, String focusId, Map<String, QuestDefinition> definitions, List<PrerequisiteConnectionRow> rows, boolean externalMode) {
        if (externalMode) {
            return compactPlacements(chapter, focusId, definitions, rows);
        }
        Map<String, QuestPlacement> placements = new LinkedHashMap<>();
        for (Map.Entry<String, QuestDefinition> entry : definitions.entrySet()) {
            placements.put(entry.getKey(), actualPlacement(entry.getValue(), chapter));
        }
        return placements;
    }

    private static Map<String, QuestPlacement> compactPlacements(String chapter, String focusId, Map<String, QuestDefinition> definitions, List<PrerequisiteConnectionRow> rows) {
        Map<String, QuestPlacement> placements = new LinkedHashMap<>();
        placements.put(focusId, new QuestPlacement(chapter, 0, 0, 1.0f));
        List<String> incoming = uniqueOtherIds(rows, focusId, PrerequisiteConnectionKind.INCOMING);
        List<String> outgoing = uniqueOtherIds(rows, focusId, PrerequisiteConnectionKind.OUTGOING);
        addCompactColumn(placements, chapter, incoming, -COMPACT_X_OFFSET);
        addCompactColumn(placements, chapter, outgoing, COMPACT_X_OFFSET);
        for (String id : definitions.keySet()) {
            placements.putIfAbsent(id, new QuestPlacement(chapter, 0, (placements.size() + 1) * COMPACT_Y_STEP, 1.0f));
        }
        return placements;
    }

    private static List<String> uniqueOtherIds(List<PrerequisiteConnectionRow> rows, String focusId, PrerequisiteConnectionKind kind) {
        Set<String> values = new LinkedHashSet<>();
        for (PrerequisiteConnectionRow row : rows) {
            if (row.kind() != kind) {
                continue;
            }
            values.add(focusId.equals(row.sourceId()) ? row.targetId() : row.sourceId());
        }
        return List.copyOf(values);
    }

    private static void addCompactColumn(Map<String, QuestPlacement> placements, String chapter, List<String> ids, int x) {
        int count = ids.size();
        for (int i = 0; i < ids.size(); i++) {
            int y = Math.round((i - (count - 1) / 2.0f) * COMPACT_Y_STEP);
            placements.put(ids.get(i), new QuestPlacement(chapter, x, y, 1.0f));
        }
    }

    private static QuestPlacement actualPlacement(QuestDefinition definition, String preferredGroup) {
        ChapterDef preferred = definition.display().chapters().get(preferredGroup);
        if (preferred != null) {
            preferred = new ChapterDef(preferred.visible(), preferred.x(), preferred.y(), preferred.scale());
        }
        for (Map.Entry<String, ChapterDef> entry : definition.display().chapters().entrySet()) {
            ChapterDef view = entry.getValue();
            return new QuestPlacement(entry.getKey(), view.x(), view.y(), view.scale());
        }
        return new QuestPlacement(preferredGroup, 0, 0, 1.0f);
    }

    private static Map<String, Set<String>> prerequisitesByTarget(List<PrerequisiteConnectionRow> rows, Set<String> exclusiveChoiceIds) {
        Map<String, Set<String>> prerequisites = new LinkedHashMap<>();
        for (PrerequisiteConnectionRow row : rows) {
            if (row.exclusiveChoice() && !exclusiveChoiceIds.contains(row.targetId())) {
                continue;
            }
            prerequisites.computeIfAbsent(row.targetId(), ignored -> new LinkedHashSet<>()).add(row.sourceId());
        }
        return prerequisites;
    }

    private static QuestDefinition withPrerequisites(QuestDefinition definition, Set<String> prerequisites) {
        return new QuestDefinition(
                definition.schema(),
                definition.id(),
                definition.display(),
                definition.settings(),
                prerequisites,
                definition.connectionColors(),
                definition.connectionModes(),
                definition.hiddenConnections(),
                definition.connectionTextures(),
                definition.connectionTextureSpacings(),
                definition.tasksOrder(),
                definition.rewardsOrder(),
                definition.tasks(),
                definition.rewards()
        );
    }

    private static Origin origin(List<CanvasBlueprint.QuestEntry> entries, List<CanvasBlueprint.ExclusiveChoiceEntry> exclusiveChoiceEntries) {
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        for (CanvasBlueprint.QuestEntry entry : entries) {
            minX = Math.min(minX, entry.sourceX());
            minY = Math.min(minY, entry.sourceY());
        }
        for (CanvasBlueprint.ExclusiveChoiceEntry entry : exclusiveChoiceEntries) {
            minX = Math.min(minX, entry.sourceX());
            minY = Math.min(minY, entry.sourceY());
        }
        return minX == Integer.MAX_VALUE ? new Origin(0, 0) : new Origin(minX, minY);
    }

    private static Set<String> exclusiveChoiceIdsFromRows(List<PrerequisiteConnectionRow> rows, String focusId) {
        Set<String> exclusiveChoiceIds = new LinkedHashSet<>();
        for (PrerequisiteConnectionRow row : rows) {
            if (row.exclusiveChoice()) {
                if (!row.sourceId().equals(focusId)) {
                    exclusiveChoiceIds.add(row.sourceId());
                }
                if (!row.targetId().equals(focusId)) {
                    exclusiveChoiceIds.add(row.targetId());
                }
            }
        }
        return exclusiveChoiceIds;
    }

    private static CanvasExclusiveChoice findExclusiveChoiceById(String id, String chapter) {
        if (chapter == null || chapter.isBlank()) {
            return null;
        }
        List<CanvasExclusiveChoice> ecs = ClientQuestStateFacade.canvasExclusiveChoicesByChapter().get(chapter);
        if (ecs == null) {
            return null;
        }
        for (CanvasExclusiveChoice ec : ecs) {
            if (ec.id().equals(id)) {
                return ec;
            }
        }
        return null;
    }

    private record QuestPlacement(String chapter, int x, int y, float scale) {
    }

    private record Origin(int x, int y) {
    }
}
