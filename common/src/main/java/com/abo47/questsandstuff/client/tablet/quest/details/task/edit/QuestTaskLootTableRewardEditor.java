package com.abo47.questsandstuff.client.tablet.quest.details.task.edit;

import net.minecraft.world.entity.player.Player;

import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import com.abo47.questsandstuff.client.sync.state.ClientQuestStateFacade;
import com.abo47.questsandstuff.client.tablet.quest.details.task.QuestTaskEntry;
import com.abo47.questsandstuff.client.tablet.quest.details.task.model.QuestTaskJsonFactory;
import com.abo47.questsandstuff.client.tablet.state.UiState;
import com.abo47.questsandstuff.client.tablet.text.QuestTranslationKeys;
import com.abo47.questsandstuff.client.tablet.text.TranslationKeys;
import com.abo47.questsandstuff.client.tablet.text.format.DisplayNameFormatter;
import com.abo47.questsandstuff.client.tablet.theme.tokens.ThemeColors;

import com.google.gson.JsonObject;

public final class QuestTaskLootTableRewardEditor {
    private QuestTaskLootTableRewardEditor() {
    }

    public static boolean isLootTable(JsonObject json) {
        String path = QuestTaskJsonFactory.typePath(QuestTaskJsonFactory.asString(json, "type", ""));
        return "loot_table".equals(path) || "loot".equals(path);
    }

    public static void render(WidgetGroup parent, UiState state, Player player, Runnable refresh, String questId, QuestTaskEntry entry, int x, int y, int rightX) {
        renderSummary(parent, entry.json(), x, y, Math.max(18, rightX - x));
    }

    public static String displayName(String lootTable) {
        return DisplayNameFormatter.lootTable(lootTable, ClientQuestStateFacade.lootTableDisplays());
    }

    private static void renderSummary(WidgetGroup parent, JsonObject json, int x, int y, int w) {
        String lootTable = lootTable(json);
        String naturalName = displayName(lootTable);
        String title = QuestTaskJsonFactory.asString(json, "title", "");
        String lootTableLabel = TranslationKeys.text(QuestTranslationKeys.TYPE_LOOT_TABLE);
        if (title.isBlank() || "Loot table".equalsIgnoreCase(title) || lootTableLabel.equalsIgnoreCase(title)) {
            title = naturalName;
        }
        if (title.isBlank()) {
            title = TranslationKeys.text(QuestTranslationKeys.CHOOSE_LOOT_TABLE);
        }
        QuestTaskInlineFields.renderDisplayText(parent, x, y, w, title, ThemeColors.TEXT_PRIMARY, TextTexture.TextType.LEFT_HIDE);
    }

    private static String lootTable(JsonObject json) {
        return QuestTaskJsonFactory.asString(json, "loot_table", "");
    }
}
