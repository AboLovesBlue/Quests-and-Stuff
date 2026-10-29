package com.abo47.questsandstuff.quest.editor.quest;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.mojang.serialization.JsonOps;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.quest.model.ChapterDef;
import com.abo47.questsandstuff.quest.model.QuestDefinition;
import com.abo47.questsandstuff.quest.model.QuestDisplay;
import com.abo47.questsandstuff.quest.model.QuestSettings;
import com.abo47.questsandstuff.quest.model.connection.QuestConnectionMetadata;
import com.abo47.questsandstuff.quest.model.reward.QuestRewardDefinition;
import com.abo47.questsandstuff.quest.model.task.QuestTaskDefinition;
import com.abo47.questsandstuff.quest.persistence.quest.ConnectionMetadataFilter;

import com.google.gson.JsonElement;

import static com.abo47.questsandstuff.quest.persistence.quest.QuestDefinitionNormalizer.cloneDefinition;
import static com.abo47.questsandstuff.quest.persistence.quest.QuestDefinitionNormalizer.orderedCopy;

public final class QuestDefinitionEdits {
    private QuestDefinitionEdits() {
    }

    public static QuestDefinition deepCopyDefinition(QuestDefinition source) {
        if (source == null) {
            return null;
        }
        try {
            JsonElement encoded = QuestDefinition.CODEC.encodeStart(JsonOps.INSTANCE, source)
                    .getOrThrow(false, QuestsAndStuffMod.LOGGER::error);
            return QuestDefinition.CODEC.parse(JsonOps.INSTANCE, encoded)
                    .getOrThrow(false, QuestsAndStuffMod.LOGGER::error);
        } catch (Exception ignored) {
            return cloneDefinition(source);
        }
    }

    public static QuestDefinition withPrerequisites(QuestDefinition definition, Set<String> prerequisites) {
        Set<String> copiedPrerequisites = Set.copyOf(prerequisites);
        return new QuestDefinition(
                definition.schema(),
                definition.id(),
                definition.display(),
                definition.settings(),
                copiedPrerequisites,
                ConnectionMetadataFilter.filterConnectionColors(definition.connectionColors(), copiedPrerequisites),
                ConnectionMetadataFilter.filterConnectionModes(definition.connectionModes(), copiedPrerequisites),
                ConnectionMetadataFilter.filterHiddenConnections(definition.hiddenConnections(), copiedPrerequisites),
                ConnectionMetadataFilter.filterConnectionTextures(definition.connectionTextures(), copiedPrerequisites),
                ConnectionMetadataFilter.filterConnectionTextureSpacings(definition.connectionTextureSpacings(), copiedPrerequisites),
                definition.tasksOrder(),
                definition.rewardsOrder(),
                definition.tasks(),
                definition.rewards()
        );
    }

    public static QuestDefinition withoutPrerequisite(QuestDefinition definition, String prerequisiteId) {
        String normalizedPrerequisite = QuestConnectionMetadata.normalizeQuestId(prerequisiteId);
        if (definition == null || normalizedPrerequisite.isBlank() || !definition.prerequisites().contains(normalizedPrerequisite)) {
            return definition;
        }
        Set<String> prerequisites = new HashSet<>(definition.prerequisites());
        prerequisites.remove(normalizedPrerequisite);
        return withPrerequisites(definition, prerequisites);
    }

    public static QuestDefinition withDisplay(QuestDefinition definition, QuestDisplay display) {
        Set<String> prerequisites = definition.prerequisites();
        return new QuestDefinition(
                definition.schema(),
                definition.id(),
                display,
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

    public static QuestDefinition withSettings(QuestDefinition definition, QuestSettings settings) {
        return new QuestDefinition(
                definition.schema(),
                definition.id(),
                definition.display(),
                settings,
                definition.prerequisites(),
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

    public static QuestDefinition withConnectionColors(QuestDefinition definition, Map<String, Integer> colors) {
        Set<String> prerequisites = definition.prerequisites();
        return new QuestDefinition(
                definition.schema(),
                definition.id(),
                definition.display(),
                definition.settings(),
                prerequisites,
                ConnectionMetadataFilter.filterConnectionColors(colors, prerequisites),
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

    public static QuestDefinition withConnectionModes(QuestDefinition definition, Map<String, String> modes) {
        Set<String> prerequisites = definition.prerequisites();
        return new QuestDefinition(
                definition.schema(),
                definition.id(),
                definition.display(),
                definition.settings(),
                prerequisites,
                definition.connectionColors(),
                ConnectionMetadataFilter.filterConnectionModes(modes, prerequisites),
                definition.hiddenConnections(),
                definition.connectionTextures(),
                definition.connectionTextureSpacings(),
                definition.tasksOrder(),
                definition.rewardsOrder(),
                definition.tasks(),
                definition.rewards()
        );
    }

    public static QuestDefinition withHiddenConnections(QuestDefinition definition, Set<String> hiddenConnections) {
        Set<String> prerequisites = definition.prerequisites();
        return new QuestDefinition(
                definition.schema(),
                definition.id(),
                definition.display(),
                definition.settings(),
                prerequisites,
                definition.connectionColors(),
                definition.connectionModes(),
                ConnectionMetadataFilter.filterHiddenConnections(hiddenConnections, prerequisites),
                definition.connectionTextures(),
                definition.connectionTextureSpacings(),
                definition.tasksOrder(),
                definition.rewardsOrder(),
                definition.tasks(),
                definition.rewards()
        );
    }

    public static QuestDefinition withConnectionTextures(QuestDefinition definition, Map<String, String> textures) {
        Set<String> prerequisites = definition.prerequisites();
        return new QuestDefinition(
                definition.schema(),
                definition.id(),
                definition.display(),
                definition.settings(),
                prerequisites,
                definition.connectionColors(),
                definition.connectionModes(),
                definition.hiddenConnections(),
                ConnectionMetadataFilter.filterConnectionTextures(textures, prerequisites),
                definition.connectionTextureSpacings(),
                definition.tasksOrder(),
                definition.rewardsOrder(),
                definition.tasks(),
                definition.rewards()
        );
    }

    public static QuestDefinition withConnectionTextureSpacings(QuestDefinition definition, Map<String, Integer> spacings) {
        Set<String> prerequisites = definition.prerequisites();
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
                ConnectionMetadataFilter.filterConnectionTextureSpacings(spacings, prerequisites),
                definition.tasksOrder(),
                definition.rewardsOrder(),
                definition.tasks(),
                definition.rewards()
        );
    }

    public static QuestDefinition withChapters(QuestDefinition definition, Map<String, ChapterDef> groups) {
        Set<String> prerequisites = definition.prerequisites();
        QuestDisplay display = definition.display().withChapters(Map.copyOf(groups));
        return new QuestDefinition(
                definition.schema(),
                definition.id(),
                display,
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

    public static QuestDefinition withTasks(QuestDefinition definition, Map<String, QuestTaskDefinition> tasks) {
        Set<String> prerequisites = definition.prerequisites();
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
                new ArrayList<>(tasks.keySet()),
                definition.rewardsOrder(),
                orderedCopy(tasks),
                definition.rewards()
        );
    }

    public static QuestDefinition withRewards(QuestDefinition definition, Map<String, QuestRewardDefinition> rewards) {
        Set<String> prerequisites = definition.prerequisites();
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
                new ArrayList<>(rewards.keySet()),
                definition.tasks(),
                orderedCopy(rewards)
        );
    }
}
