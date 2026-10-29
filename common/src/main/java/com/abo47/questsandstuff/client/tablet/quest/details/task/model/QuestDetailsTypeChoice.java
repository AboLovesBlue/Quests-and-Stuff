package com.abo47.questsandstuff.client.tablet.quest.details.task.model;

import java.util.List;
import java.util.function.Function;

import com.abo47.questsandstuff.client.tablet.quest.details.task.edit.QuestTaskEditFlow;
import com.abo47.questsandstuff.client.tablet.text.QuestTranslationKeys;

import com.google.gson.JsonObject;

public record QuestDetailsTypeChoice(
        String labelKey,
        String type,
        String icon,
        QuestTaskEditFlow editFlow,
        Function<String, JsonObject> defaultJsonFactory,
        List<String> requiredJsonFields
) {
    public QuestDetailsTypeChoice {
        requiredJsonFields = List.copyOf(requiredJsonFields);
    }

    public String label() {
        return QuestTranslationKeys.text(labelKey);
    }

    public String fullType() {
        return TaskJsonFactory.MOD + type;
    }

    public JsonObject defaultJson(String id) {
        return defaultJsonFactory.apply(id);
    }
}
