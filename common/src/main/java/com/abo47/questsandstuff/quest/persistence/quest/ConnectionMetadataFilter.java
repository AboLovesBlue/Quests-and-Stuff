package com.abo47.questsandstuff.quest.persistence.quest;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.abo47.questsandstuff.util.naming.QuestIdentity;

public final class ConnectionMetadataFilter {
    private ConnectionMetadataFilter() {
    }

    public static Map<String, Integer> filterConnectionColors(Map<String, Integer> colors, Set<String> prerequisites) {
        if (colors == null || colors.isEmpty() || prerequisites == null || prerequisites.isEmpty()) {
            return Map.of();
        }
        Map<String, Integer> filtered = new HashMap<>();
        for (Map.Entry<String, Integer> entry : colors.entrySet()) {
            if (entry.getKey() != null && prerequisites.contains(normalizeQuestId(entry.getKey())) && entry.getValue() != null) {
                filtered.put(normalizeQuestId(entry.getKey()), entry.getValue());
            }
        }
        return Map.copyOf(filtered);
    }

    public static Map<String, String> filterConnectionModes(Map<String, String> modes, Set<String> prerequisites) {
        if (modes == null || modes.isEmpty() || prerequisites == null || prerequisites.isEmpty()) {
            return Map.of();
        }
        Map<String, String> filtered = new HashMap<>();
        for (Map.Entry<String, String> entry : modes.entrySet()) {
            String key = normalizeQuestId(entry.getKey());
            String mode = entry.getValue() == null ? "" : entry.getValue().trim();
            if (!key.isBlank() && prerequisites.contains(key) && "grid".equals(mode)) {
                filtered.put(key, mode);
            }
        }
        return Map.copyOf(filtered);
    }

    public static Set<String> filterHiddenConnections(Set<String> hidden, Set<String> prerequisites) {
        if (hidden == null || hidden.isEmpty() || prerequisites == null || prerequisites.isEmpty()) {
            return Set.of();
        }
        Set<String> filtered = new HashSet<>();
        for (String entry : hidden) {
            String key = normalizeQuestId(entry);
            if (!key.isBlank() && prerequisites.contains(key)) {
                filtered.add(key);
            }
        }
        return Set.copyOf(filtered);
    }

    public static Map<String, String> filterConnectionTextures(Map<String, String> textures, Set<String> prerequisites) {
        if (textures == null || textures.isEmpty() || prerequisites == null || prerequisites.isEmpty()) {
            return Map.of();
        }
        Map<String, String> filtered = new HashMap<>();
        for (Map.Entry<String, String> entry : textures.entrySet()) {
            if (entry.getKey() != null && prerequisites.contains(normalizeQuestId(entry.getKey())) && entry.getValue() != null && !entry.getValue().isBlank()) {
                filtered.put(normalizeQuestId(entry.getKey()), entry.getValue());
            }
        }
        return Map.copyOf(filtered);
    }

    public static Map<String, Integer> filterConnectionTextureSpacings(Map<String, Integer> spacings, Set<String> prerequisites) {
        if (spacings == null || spacings.isEmpty() || prerequisites == null || prerequisites.isEmpty()) {
            return Map.of();
        }
        Map<String, Integer> filtered = new HashMap<>();
        for (Map.Entry<String, Integer> entry : spacings.entrySet()) {
            if (entry.getKey() != null && prerequisites.contains(normalizeQuestId(entry.getKey())) && entry.getValue() != null && entry.getValue() > 0) {
                filtered.put(normalizeQuestId(entry.getKey()), entry.getValue());
            }
        }
        return Map.copyOf(filtered);
    }

    static String normalizeQuestId(String questId) {
        return QuestIdentity.questIdOrDefault(questId);
    }
}
