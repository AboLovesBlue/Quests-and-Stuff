package com.abolovesblue.questsandstuff.quest.runtime;

import com.abolovesblue.questsandstuff.quest.persistence.quest.QuestDefinitionStore;
import com.abolovesblue.questsandstuff.quest.persistence.quest.QuestProgressSavedData;
import com.abolovesblue.questsandstuff.quest.sync.SyncService;

record RuntimeContext(
    QuestDefinitionStore definitionStore,
    QuestProgressSavedData progressData,
    SyncService syncService,
    RuntimeEngine engine
) {
}
