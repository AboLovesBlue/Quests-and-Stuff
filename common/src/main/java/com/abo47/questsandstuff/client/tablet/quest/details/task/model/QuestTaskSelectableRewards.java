package com.abo47.questsandstuff.client.tablet.quest.details.task.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.sync.state.ClientQuestStateFacade;
import com.abo47.questsandstuff.client.tablet.quest.details.task.QuestTaskEntry;
import com.abo47.questsandstuff.client.tablet.quest.editor.EditorQuestCommandClient;
import com.abo47.questsandstuff.client.tablet.state.UiState;
import com.abo47.questsandstuff.network.ModNetwork;
import com.abo47.questsandstuff.network.quest.runtime.C2SClaimSelectableRewardPacket;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public final class QuestTaskSelectableRewards {
    private static final String CHOICE_SEPARATOR = "\u001F";
    private static final String STANDALONE_SELECTION_KEY = CHOICE_SEPARATOR + "standalone";

    private QuestTaskSelectableRewards() {
    }

    public static boolean isSelectable(JsonObject json) {
        return QuestTaskJsonFactory.asBoolean(json, "selectable", false) || isSelectableWrapper(json);
    }

    private static boolean isSelectableWrapper(JsonObject json) {
        return "selectable".equals(QuestTaskJsonFactory.typePath(QuestTaskJsonFactory.asString(json, "type", "")));
    }

    public static JsonObject displayJson(JsonObject json) {
        if (!isSelectableWrapper(json)) {
            return json;
        }
        JsonElement rewards = json.get("rewards");
        if (rewards == null || !rewards.isJsonObject()) {
            return json;
        }
        for (Map.Entry<String, JsonElement> entry : rewards.getAsJsonObject().entrySet()) {
            if (entry.getValue().isJsonObject()) {
                return entry.getValue().getAsJsonObject();
            }
        }
        return json;
    }

    public static List<QuestTaskEntry> displayEntries(List<QuestTaskEntry> entries, boolean editMode) {
        if (editMode) {
            return entries;
        }
        List<QuestTaskEntry> displayEntries = new ArrayList<>();
        for (QuestTaskEntry entry : entries) {
            if (!isSelectableWrapper(entry.json())) {
                displayEntries.add(entry);
                continue;
            }
            JsonElement rewards = entry.json().get("rewards");
            if (rewards == null || !rewards.isJsonObject()) {
                displayEntries.add(entry);
                continue;
            }
            int added = 0;
            for (Map.Entry<String, JsonElement> choice : rewards.getAsJsonObject().entrySet()) {
                if (!choice.getValue().isJsonObject()) {
                    continue;
                }
                JsonObject child = choice.getValue().getAsJsonObject();
                CompoundTag tag = entry.tag().copy();
                tag.putString("type", QuestTaskJsonFactory.asString(child, "type", ""));
                tag.putString("json", child.toString());
                displayEntries.add(new QuestTaskEntry(choiceEntryId(entry.id(), choice.getKey()), tag, child));
                added++;
            }
            if (added == 0) {
                displayEntries.add(entry);
            }
        }
        return displayEntries;
    }

    public static boolean isSelectableChoiceId(String id) {
        return id != null && id.contains(CHOICE_SEPARATOR);
    }

    public static boolean isClaimChoiceEntry(QuestTaskEntry entry) {
        if (entry == null) {
            return false;
        }
        if (isSelectableChoiceId(entry.id())) {
            return true;
        }
        return isSelectable(entry.json()) && !isSelectableWrapper(entry.json());
    }

    static String choiceGroupId(String id) {
        int separator = id == null ? -1 : id.indexOf(CHOICE_SEPARATOR);
        return separator < 0 ? "" : id.substring(0, separator);
    }

    static String choiceId(String id) {
        int separator = id == null ? -1 : id.indexOf(CHOICE_SEPARATOR);
        return separator < 0 ? "" : id.substring(separator + CHOICE_SEPARATOR.length());
    }

    public static void selectChoice(UiState state, String id) {
        if (state == null || id == null || id.isBlank()) {
            return;
        }
        if (isSelectableChoiceId(id)) {
            String groupId = choiceGroupId(id);
            String choiceId = choiceId(id);
            if (!groupId.isBlank() && !choiceId.isBlank()) {
                state.questDetails.questDetailsSelectableRewardChoices.put(groupId, choiceId);
            }
        } else {
            state.questDetails.questDetailsSelectableRewardChoices.put(STANDALONE_SELECTION_KEY, id);
        }
    }

    public static boolean isSelectedChoice(UiState state, String id) {
        if (state == null || id == null || id.isBlank()) {
            return false;
        }
        if (!isSelectableChoiceId(id)) {
            return id.equals(state.questDetails.questDetailsSelectableRewardChoices.get(STANDALONE_SELECTION_KEY));
        }
        String groupId = choiceGroupId(id);
        String choiceId = choiceId(id);
        if (groupId.isBlank() || choiceId.isBlank()) {
            return false;
        }
        return choiceId.equals(state.questDetails.questDetailsSelectableRewardChoices.get(groupId));
    }

    public static void makeSelectable(Player player, String questId, String rewardId) {
        CompoundTag rewards = ClientQuestStateFacade.quest(questId).getCompound("rewards");
        JsonObject reward = QuestTaskJsonFactory.readRewardForEdit(questId, rewardId, rewards.getCompound(rewardId).getString("json"));
        if (isSelectable(reward)) {
            return;
        }
        reward.addProperty("selectable", true);
        EditorQuestCommandClient.putQuestRewardJson(player, questId, reward.toString());
        QuestsAndStuffMod.debugLog("[QnS:UI] reward marked selectable quest={} reward={}", questId, rewardId);
    }

    public static boolean claimSelected(Player player, UiState state, String questId) {
        if (state == null) {
            return false;
        }
        CompoundTag rewards = ClientQuestStateFacade.quest(questId).getCompound("rewards");
        boolean claimedAny = false;
        for (String rewardId : rewards.getAllKeys()) {
            JsonObject json = QuestTaskJsonFactory.read(rewards.getCompound(rewardId).getString("json"));
            if (!isSelectable(json)) {
                continue;
            }
            if (!isSelectableWrapper(json)) {
                if (!isStandaloneSelected(state, rewardId)) {
                    continue;
                }
                ModNetwork.sendToServer(new C2SClaimSelectableRewardPacket(questId, rewardId, List.of()));
                QuestsAndStuffMod.debugLog("[QnS:UI] selectable reward claimed quest={} reward={}", questId, rewardId);
                claimedAny = true;
                continue;
            }
            String choiceId = selectedChoiceForGroup(state, rewardId);
            if (choiceId.isBlank() || !choiceExists(json, choiceId)) {
                continue;
            }
            ModNetwork.sendToServer(new C2SClaimSelectableRewardPacket(questId, rewardId, List.of(choiceId)));
            QuestsAndStuffMod.debugLog("[QnS:UI] selectable reward choice claimed quest={} reward={} choice={}", questId, rewardId, choiceId);
            claimedAny = true;
        }
        return claimedAny;
    }

    public static boolean allSelectableRewardsSelected(CompoundTag quest, UiState state) {
        if (quest == null || state == null) {
            return false;
        }
        CompoundTag rewards = quest.getCompound("rewards");
        boolean hasSelectable = false;
        boolean hasSelected = false;
        boolean hasStandaloneSelectable = false;
        boolean hasSelectedStandalone = false;
        for (String rewardId : rewards.getAllKeys()) {
            JsonObject json = QuestTaskJsonFactory.read(rewards.getCompound(rewardId).getString("json"));
            if (!isSelectable(json)) {
                continue;
            }
            hasSelectable = true;
            if (!isSelectableWrapper(json)) {
                hasStandaloneSelectable = true;
                if (isStandaloneSelected(state, rewardId)) {
                    hasSelected = true;
                    hasSelectedStandalone = true;
                }
                continue;
            }
            String choiceId = selectedChoiceForGroup(state, rewardId);
            if (choiceId.isBlank() || !choiceExists(json, choiceId)) {
                return false;
            }
            hasSelected = true;
        }
        if (hasStandaloneSelectable && !hasSelectedStandalone) {
            return false;
        }
        return hasSelectable && hasSelected;
    }

    public static boolean hasSelectableReward(CompoundTag quest) {
        if (quest == null) {
            return false;
        }
        CompoundTag rewards = quest.getCompound("rewards");
        for (String rewardId : rewards.getAllKeys()) {
            JsonObject json = QuestTaskJsonFactory.read(rewards.getCompound(rewardId).getString("json"));
            if (isSelectable(json)) {
                return true;
            }
        }
        return false;
    }

    private static String selectedChoiceForGroup(UiState state, String groupId) {
        if (state == null || groupId == null || groupId.isBlank()) {
            return "";
        }
        String selected = state.questDetails.questDetailsSelectableRewardChoices.get(groupId);
        if (selected != null && !selected.isBlank()) {
            return selected.trim();
        }
        return "";
    }

    private static boolean isStandaloneSelected(UiState state, String rewardId) {
        return state != null && rewardId != null && rewardId.equals(state.questDetails.questDetailsSelectableRewardChoices.get(STANDALONE_SELECTION_KEY));
    }

    private static boolean choiceExists(JsonObject selectable, String choiceId) {
        if (choiceId == null || choiceId.isBlank()) {
            return false;
        }
        JsonElement rewards = selectable.get("rewards");
        return rewards != null && rewards.isJsonObject() && rewards.getAsJsonObject().has(choiceId);
    }

    public static boolean commitDisplayAmount(Player player, String questId, String rewardId, int amount) {
        if (questId == null || questId.isBlank() || rewardId == null || rewardId.isBlank()) {
            return false;
        }
        CompoundTag rewards = ClientQuestStateFacade.quest(questId).getCompound("rewards");
        JsonObject selectable = QuestTaskJsonFactory.readRewardForEdit(questId, rewardId, rewards.getCompound(rewardId).getString("json"));
        if (!isSelectableWrapper(selectable)) {
            return false;
        }
        JsonElement choicesElement = selectable.get("rewards");
        if (choicesElement == null || !choicesElement.isJsonObject()) {
            return false;
        }
        for (Map.Entry<String, JsonElement> choice : choicesElement.getAsJsonObject().entrySet()) {
            if (!choice.getValue().isJsonObject()) {
                continue;
            }
            choice.getValue().getAsJsonObject().addProperty("amount", amount);
            EditorQuestCommandClient.putQuestRewardJson(player, questId, selectable.toString());
            QuestsAndStuffMod.debugLog("[QnS:UI] selectable reward display amount changed quest={} reward={} choice={} amount={}", questId, rewardId, choice.getKey(), amount);
            return true;
        }
        return false;
    }

    private static String choiceEntryId(String groupId, String choiceId) {
        return groupId + CHOICE_SEPARATOR + choiceId;
    }
}
