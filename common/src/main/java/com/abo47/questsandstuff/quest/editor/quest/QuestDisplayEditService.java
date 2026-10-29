package com.abo47.questsandstuff.quest.editor.quest;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.minecraft.server.level.ServerPlayer;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.quest.editor.session.EditorSessionService;
import com.abo47.questsandstuff.quest.model.QuestDefinition;
import com.abo47.questsandstuff.quest.model.QuestDisplay;
import com.abo47.questsandstuff.quest.sync.SyncKeys;

public final class QuestDisplayEditService {
    private static final int MAX_DESCRIPTION_LINES = 256;

    private final EditorSessionService service;

    public QuestDisplayEditService(EditorSessionService service) {
        this.service = service;
    }

    public void updateQuestDisplay(ServerPlayer player, String questId, String title, String subtitle) {
        String normalizedQuestId = EditorSessionService.normalizeQuestId(questId);
        QuestDefinition source = service.definitionStore().quests().get(normalizedQuestId);
        if (source == null) {
            return;
        }

        QuestDisplay old = source.display();
        String newTitle = title == null || title.isBlank() ? old.title() : title;
        String newSubtitle = subtitle == null ? old.subtitle() : subtitle;
        if (newTitle.equals(old.title()) && newSubtitle.equals(old.subtitle())) {
            return;
        }

        EditorSessionService.EditorSession session = service.session(player);
        service.captureUndo(session);
        QuestContentEditService.updateQuest(service, player, session, QuestDefinitionEdits.withDisplay(source, old.withTitle(newTitle).withSubtitle(newSubtitle)));
    }

    public void updateQuestDescription(ServerPlayer player, String questId, List<String> description) {
        String normalizedQuestId = EditorSessionService.normalizeQuestId(questId);
        QuestDefinition source = service.definitionStore().quests().get(normalizedQuestId);
        if (source == null) {
            return;
        }

        List<String> newDescription = description == null ? List.of() : description.stream()
                .filter(line -> line != null)
                .map(QuestDisplayEditService::limitDescriptionLine)
                .limit(MAX_DESCRIPTION_LINES)
                .toList();
        if (source.display().description().equals(newDescription)) {
            return;
        }

        EditorSessionService.EditorSession session = service.session(player);
        service.captureUndo(session);
        QuestContentEditService.updateQuest(service, player, session, QuestDefinitionEdits.withDisplay(source, source.display().withDescription(newDescription)));
    }

    public void setQuestIcon(ServerPlayer player, String questId, String icon) {
        String normalizedQuestId = EditorSessionService.normalizeQuestId(questId);
        QuestDefinition source = service.definitionStore().quests().get(normalizedQuestId);
        if (source == null) {
            return;
        }

        String normalizedIcon = QuestDisplay.normalizeIcon(icon);
        if (source.display().icon().equals(normalizedIcon)) {
            return;
        }

        EditorSessionService.EditorSession session = service.session(player);
        service.captureUndo(session);
        QuestsAndStuffMod.debugLog("[QnS:Editor] quest icon quest={} icon={}", normalizedQuestId, normalizedIcon);
        QuestContentEditService.updateQuest(service, player, session, QuestDefinitionEdits.withDisplay(source, source.display().withIcon(normalizedIcon)));
    }

    public void setQuestVisualHidden(ServerPlayer player, String questId, boolean hidden) {
        String normalizedQuestId = EditorSessionService.normalizeQuestId(questId);
        QuestDefinition quest = service.definitionStore().quests().get(normalizedQuestId);
        if (quest == null || quest.display().visualHidden() == hidden) {
            return;
        }

        EditorSessionService.EditorSession session = service.session(player);
        service.captureUndo(session);
        QuestContentEditService.updateQuest(service, player, session, QuestDefinitionEdits.withDisplay(quest, quest.display().withVisualHidden(hidden)));
    }

    public void setQuestCompletionSound(ServerPlayer player, String questId, String sound) {
        String normalizedQuestId = EditorSessionService.normalizeQuestId(questId);
        QuestDefinition quest = service.definitionStore().quests().get(normalizedQuestId);
        if (quest == null) {
            return;
        }
        String normalizedSound = sound == null || sound.isBlank() ? QuestDisplay.DEFAULT_COMPLETION_SOUND : sound.trim();
        if (quest.display().completionSound().equals(normalizedSound)) {
            return;
        }

        EditorSessionService.EditorSession session = service.session(player);
        service.captureUndo(session);
        QuestContentEditService.updateQuest(service, player, session, QuestDefinitionEdits.withDisplay(quest, quest.display().withCompletionSound(normalizedSound)));
    }

    public void setQuestCompletionSound(ServerPlayer player, Set<String> questIds, String sound) {
        String normalizedSound = sound == null || sound.isBlank() ? QuestDisplay.DEFAULT_COMPLETION_SOUND : sound.trim();
        List<QuestDefinition> updated = new ArrayList<>();
        for (QuestDefinition quest : selectedQuests(questIds)) {
            if (quest.display().completionSound().equals(normalizedSound)) {
                continue;
            }
            updated.add(QuestDefinitionEdits.withDisplay(quest, quest.display().withCompletionSound(normalizedSound)));
        }
        updateQuests(player, updated);
    }

    public void setQuestCompletionSoundVolume(ServerPlayer player, String questId, int volume) {
        String normalizedQuestId = EditorSessionService.normalizeQuestId(questId);
        QuestDefinition quest = service.definitionStore().quests().get(normalizedQuestId);
        if (quest == null) {
            return;
        }
        int normalizedVolume = QuestDisplay.normalizeCompletionSoundVolume(volume);
        if (quest.display().completionSoundVolume() == normalizedVolume) {
            return;
        }

        EditorSessionService.EditorSession session = service.session(player);
        service.captureUndo(session);
        QuestContentEditService.updateQuest(service, player, session, QuestDefinitionEdits.withDisplay(quest, quest.display().withCompletionSoundVolume(normalizedVolume)));
    }

    public void setQuestCompletionSoundVolume(ServerPlayer player, Set<String> questIds, int volume) {
        int normalizedVolume = QuestDisplay.normalizeCompletionSoundVolume(volume);
        List<QuestDefinition> updated = new ArrayList<>();
        for (QuestDefinition quest : selectedQuests(questIds)) {
            if (quest.display().completionSoundVolume() == normalizedVolume) {
                continue;
            }
            updated.add(QuestDefinitionEdits.withDisplay(quest, quest.display().withCompletionSoundVolume(normalizedVolume)));
        }
        updateQuests(player, updated);
    }

    public void setQuestCompletionHudBackground(ServerPlayer player, String questId, String background) {
        String normalizedQuestId = EditorSessionService.normalizeQuestId(questId);
        QuestDefinition quest = service.definitionStore().quests().get(normalizedQuestId);
        if (quest == null) {
            return;
        }
        String normalizedBackground = QuestDisplay.normalizeCompletionHudBackground(background);
        if (quest.display().completionHudBackground().equals(normalizedBackground)) {
            return;
        }

        EditorSessionService.EditorSession session = service.session(player);
        service.captureUndo(session);
        QuestContentEditService.updateQuest(service, player, session, QuestDefinitionEdits.withDisplay(quest, quest.display().withCompletionHudBackground(normalizedBackground)));
    }

    public void setQuestCompletionHudBackground(ServerPlayer player, Set<String> questIds, String background) {
        String normalizedBackground = QuestDisplay.normalizeCompletionHudBackground(background);
        List<QuestDefinition> updated = new ArrayList<>();
        for (QuestDefinition quest : selectedQuests(questIds)) {
            if (quest.display().completionHudBackground().equals(normalizedBackground)) {
                continue;
            }
            updated.add(QuestDefinitionEdits.withDisplay(quest, quest.display().withCompletionHudBackground(normalizedBackground)));
        }
        updateQuests(player, updated);
    }

    public void setQuestBackground(ServerPlayer player, String questId, String background, boolean grayscale) {
        String normalizedQuestId = EditorSessionService.normalizeQuestId(questId);
        QuestDefinition quest = service.definitionStore().quests().get(normalizedQuestId);
        if (quest == null) {
            return;
        }
        String normalizedBackground = QuestDisplay.normalizeQuestBackground(background);
        if (quest.display().questBackground().equals(normalizedBackground) && quest.display().questBackgroundGrayscale() == grayscale) {
            return;
        }

        EditorSessionService.EditorSession session = service.session(player);
        service.captureUndo(session);
        QuestDisplay display = quest.display().withQuestBackground(normalizedBackground).withQuestBackgroundGrayscale(grayscale);
        QuestContentEditService.updateQuest(service, player, session, QuestDefinitionEdits.withDisplay(quest, display));
    }

    public void setQuestBackground(ServerPlayer player, Set<String> questIds, String background, boolean grayscale) {
        String normalizedBackground = QuestDisplay.normalizeQuestBackground(background);
        List<QuestDefinition> updated = new ArrayList<>();
        for (QuestDefinition quest : selectedQuests(questIds)) {
            if (quest.display().questBackground().equals(normalizedBackground) && quest.display().questBackgroundGrayscale() == grayscale) {
                continue;
            }
            updated.add(QuestDefinitionEdits.withDisplay(quest, quest.display().withQuestBackground(normalizedBackground).withQuestBackgroundGrayscale(grayscale)));
        }
        updateQuests(player, updated);
    }

    private void updateQuests(ServerPlayer player, List<QuestDefinition> updated) {
        if (updated == null || updated.isEmpty()) {
            return;
        }
        EditorSessionService.EditorSession session = service.session(player);
        service.captureUndo(session);
        for (QuestDefinition definition : updated) {
            service.definitionStore().upsert(definition);
            service.definitionStore().saveNow(definition.id());
            session.currentQuest = definition.id();
        }
        service.postMutation(player);
        for (QuestDefinition definition : updated) {
            service.syncService().broadcastEditorMutation(player.server.getPlayerList().getPlayers(), SyncKeys.EditorAction.UPDATE, definition);
        }
    }

    private List<QuestDefinition> selectedQuests(Set<String> questIds) {
        if (questIds == null || questIds.isEmpty()) {
            return List.of();
        }
        List<QuestDefinition> quests = new ArrayList<>();
        for (String questId : questIds) {
            String normalizedQuestId = EditorSessionService.normalizeQuestId(questId);
            QuestDefinition quest = service.definitionStore().quests().get(normalizedQuestId);
            if (quest != null) {
                quests.add(quest);
            }
        }
        return quests;
    }

    private static String limitDescriptionLine(String line) {
        return line.length() > QuestDisplay.MAX_DESCRIPTION_LINE_LENGTH ? line.substring(0, QuestDisplay.MAX_DESCRIPTION_LINE_LENGTH) : line;
    }
}
