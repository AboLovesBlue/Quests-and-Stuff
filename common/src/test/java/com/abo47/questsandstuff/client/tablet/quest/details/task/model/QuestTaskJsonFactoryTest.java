package com.abo47.questsandstuff.client.tablet.quest.details.task.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuestTaskJsonFactoryTest {
    @Test
    void readResultReportsMalformedJsonWithoutThrowing() {
        QuestTaskJsonFactory.ParseResult result = QuestTaskJsonFactory.readResult("{bad json");

        assertFalse(result.valid());
        assertTrue(result.value().entrySet().isEmpty());
        assertFalse(result.diagnostic().isBlank());
    }

    @Test
    void readResultRejectsNonObjectJson() {
        QuestTaskJsonFactory.ParseResult result = QuestTaskJsonFactory.readResult("\"string\"");

        assertFalse(result.valid());
        assertTrue(result.value().entrySet().isEmpty());
        assertEquals("expected JSON object", result.diagnostic());
    }

    @Test
    void readForEditKeepsSafeFallbackForMalformedTaskJson() {
        QuestTaskJsonFactory.ParseResult result = QuestTaskJsonFactory.readForEdit("quest_a", "task_a", true, "{bad json");

        assertFalse(result.valid());
        assertTrue(result.value().entrySet().isEmpty());
        assertFalse(result.diagnostic().isBlank());
    }

    @Test
    void readResultAcceptsValidTaskObjects() {
        QuestTaskJsonFactory.ParseResult result = QuestTaskJsonFactory.readResult("{\"id\":\"task_a\",\"type\":\"questsandstuff:check\"}");

        assertTrue(result.valid());
        assertEquals("task_a", QuestTaskJsonFactory.asString(result.value(), "id", ""));
        assertEquals("questsandstuff:check", QuestTaskJsonFactory.asString(result.value(), "type", ""));
        assertEquals("", result.diagnostic());
    }

    @Test
    void fieldAccessorsFallbackForWrongJsonTypes() {
        QuestTaskJsonFactory.ParseResult result = QuestTaskJsonFactory.readResult("{\"id\":{\"nested\":true},\"manual\":{\"nested\":true}}");

        assertTrue(result.valid());
        assertEquals("fallback", QuestTaskJsonFactory.asString(result.value(), "id", "fallback"));
        assertTrue(QuestTaskJsonFactory.asBoolean(result.value(), "manual", true));
    }
}
