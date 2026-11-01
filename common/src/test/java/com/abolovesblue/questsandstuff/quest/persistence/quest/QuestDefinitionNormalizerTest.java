package com.abolovesblue.questsandstuff.quest.persistence.quest;

import org.junit.jupiter.api.Test;

import com.abolovesblue.questsandstuff.util.naming.QuestIdentity;

import static org.junit.jupiter.api.Assertions.assertEquals;

class QuestDefinitionNormalizerTest {
    @Test
    void questDefinitionPersistenceUsesDefaultingQuestIds() {
        assertEquals(QuestIdentity.DEFAULT_QUEST_ID, QuestDefinitionNormalizer.normalizeQuestId(" / "));
        assertEquals("Chapter/Quest", QuestDefinitionNormalizer.normalizeQuestId(" \\Chapter//Quest "));
    }

    @Test
    void groupFoldersUseFileSafeNamesNotDisplayNames() {
        assertEquals("main_chapter", QuestDefinitionNormalizer.chapterFolderName(" Main Chapter "));
        assertEquals("default", QuestDefinitionNormalizer.chapterFolderName("!!!"));
    }
}
