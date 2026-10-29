package com.abo47.questsandstuff.client.tablet.quest.details.task.model;

import net.minecraft.nbt.CompoundTag;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.util.naming.StableIdAllocator;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public final class TaskJsonFactory {
    public static final String MOD = "questsandstuff:";
    public static final String XP_CARD_ICON = "minecraft:experience_bottle";

    private TaskJsonFactory() {
    }

    public static JsonObject read(String value) {
        return readResult(value).value();
    }

    public static ParseResult readResult(String value) {
        String source = value == null || value.isBlank() ? "{}" : value;
        try {
            JsonElement parsed = JsonParser.parseString(source);
            if (parsed == null || !parsed.isJsonObject()) {
                return ParseResult.invalid(new JsonObject(), "expected JSON object");
            }
            return ParseResult.valid(parsed.getAsJsonObject());
        } catch (Exception error) {
            return ParseResult.invalid(new JsonObject(), error.getClass().getSimpleName() + ": " + safeMessage(error));
        }
    }

    public static JsonObject readTaskForEdit(String questId, String taskId, String value) {
        return readForEdit(questId, taskId, true, value).value();
    }

    public static JsonObject readRewardForEdit(String questId, String rewardId, String value) {
        return readForEdit(questId, rewardId, false, value).value();
    }

    public static ParseResult readForEdit(String questId, String entryId, boolean task, String value) {
        ParseResult result = readResult(value);
        if (!result.valid()) {
            QuestsAndStuffMod.debugLog(
                    "[QnS:UI] malformed task json quest={} {}={} diagnostic={}",
                    safeId(questId),
                    task ? "task" : "reward",
                    safeId(entryId),
                    result.diagnostic()
            );
        }
        return result;
    }

    public static String asString(JsonObject json, String key, String fallback) {
        if (json == null || !json.has(key) || json.get(key).isJsonNull()) {
            return fallback;
        }
        try {
            return json.get(key).getAsString();
        } catch (RuntimeException exception) {
            QuestsAndStuffMod.debugLog(
                    "[QnS:UI] task json string fallback key={} fallback={} diagnostic={}",
                    key,
                    fallback,
                    exception.toString()
            );
            return fallback;
        }
    }

    public static boolean asBoolean(JsonObject json, String key, boolean fallback) {
        if (json == null || !json.has(key) || json.get(key).isJsonNull()) {
            return fallback;
        }
        try {
            return json.get(key).getAsBoolean();
        } catch (RuntimeException exception) {
            QuestsAndStuffMod.debugLog(
                    "[QnS:UI] task json boolean fallback key={} fallback={} diagnostic={}",
                    key,
                    fallback,
                    exception.toString()
            );
            return fallback;
        }
    }

    public static String firstPresent(JsonObject json, String... keys) {
        for (String key : keys) {
            String value = asString(json, key, "");
            if (!value.isBlank()) {
                return value;
            }
        }
        if (keys.length > 0 && keys[keys.length - 1].contains(":")) {
            return keys[keys.length - 1];
        }
        return "";
    }

    public static String typePath(String type) {
        String value = type == null ? "" : type;
        int colon = value.indexOf(':');
        return colon >= 0 ? value.substring(colon + 1) : value;
    }

    public static String nextId(CompoundTag existing, String prefix) {
        return StableIdAllocator.nextId(prefix == null || prefix.isBlank() ? "entry" : prefix, existing == null ? java.util.Set.of() : existing.getAllKeys());
    }

    private static String safeId(String value) {
        return value == null ? "" : value;
    }

    private static String safeMessage(Exception error) {
        String message = error.getMessage();
        return message == null || message.isBlank() ? "invalid JSON" : message;
    }

    public record ParseResult(boolean valid, JsonObject value, String diagnostic) {
        static ParseResult valid(JsonObject value) {
            return new ParseResult(true, value == null ? new JsonObject() : value, "");
        }

        static ParseResult invalid(JsonObject value, String diagnostic) {
            String safeDiagnostic = diagnostic == null || diagnostic.isBlank() ? "invalid JSON" : diagnostic;
            return new ParseResult(false, value == null ? new JsonObject() : value, safeDiagnostic);
        }
    }
}
