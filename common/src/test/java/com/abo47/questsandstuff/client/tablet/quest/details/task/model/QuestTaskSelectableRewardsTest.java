package com.abo47.questsandstuff.client.tablet.quest.details.task.model;

import org.junit.jupiter.api.Test;

import net.minecraft.nbt.CompoundTag;

import com.abo47.questsandstuff.client.tablet.state.UiState;
import com.abo47.questsandstuff.client.tablet.quest.details.task.QuestTaskEntry;
import com.abo47.questsandstuff.client.tablet.quest.details.task.interaction.QuestTaskListInteractions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuestTaskSelectableRewardsTest {
    @Test
    void standaloneSelectableChoiceDoesNotUseEditSelection() {
        UiState state = new UiState();

        QuestTaskSelectableRewards.selectChoice(state, "reward_item_0002");

        assertEquals("", state.questDetails.questDetailsSelectedTaskKind);
        assertEquals("", state.questDetails.questDetailsSelectedTaskId);
        assertTrue(QuestTaskSelectableRewards.isSelectedChoice(state, "reward_item_0002"));
    }

    @Test
    void oneStandaloneSelectableRewardChoiceCompletesStandaloneSelection() {
        UiState state = new UiState();
        CompoundTag quest = questWithRewards(
                reward("reward_item_0001", true),
                reward("reward_item_0002", true),
                reward("reward_item_0003", true)
        );

        QuestTaskSelectableRewards.selectChoice(state, "reward_item_0002");

        assertTrue(QuestTaskSelectableRewards.allSelectableRewardsSelected(quest, state));
    }

    @Test
    void standaloneSelectableRewardsActAsOneChoiceGroup() {
        UiState state = new UiState();

        QuestTaskSelectableRewards.selectChoice(state, "reward_item_0001");
        QuestTaskSelectableRewards.selectChoice(state, "reward_item_0002");

        assertTrue(QuestTaskSelectableRewards.isSelectedChoice(state, "reward_item_0002"));
        assertFalse(QuestTaskSelectableRewards.isSelectedChoice(state, "reward_item_0001"));
    }

    @Test
    void wrapperSelectableRewardChoiceUsesGroupChoiceState() {
        UiState state = new UiState();
        CompoundTag quest = questWithRewards(wrapperReward("reward_selectable_0001"));

        QuestTaskSelectableRewards.selectChoice(state, "reward_selectable_0001\u001Fchoice_a");

        assertEquals("", state.questDetails.questDetailsSelectedTaskKind);
        assertEquals("", state.questDetails.questDetailsSelectedTaskId);
        assertTrue(QuestTaskSelectableRewards.isSelectedChoice(state, "reward_selectable_0001\u001Fchoice_a"));
        assertTrue(QuestTaskSelectableRewards.allSelectableRewardsSelected(quest, state));
    }

    @Test
    void claimChoiceEntriesIncludeStandaloneAndDisplayedWrapperChoicesOnly() {
        QuestTaskEntry standalone = entry("reward_item_0001", reward("reward_item_0001", true));
        QuestTaskEntry wrapper = entry("reward_selectable_0001", wrapperReward("reward_selectable_0001"));
        QuestTaskEntry wrapperChoice = entry("reward_selectable_0001\u001Fchoice_a", reward("choice_a", false));
        QuestTaskEntry normal = entry("reward_item_0002", reward("reward_item_0002", false));

        assertTrue(QuestTaskSelectableRewards.isClaimChoiceEntry(standalone));
        assertTrue(QuestTaskSelectableRewards.isClaimChoiceEntry(wrapperChoice));
        assertFalse(QuestTaskSelectableRewards.isClaimChoiceEntry(wrapper));
        assertFalse(QuestTaskSelectableRewards.isClaimChoiceEntry(normal));
    }

    @Test
    void clearingEditSelectionKeepsSelectableRewardChoice() {
        UiState state = new UiState();
        QuestTaskSelectableRewards.selectChoice(state, "reward_item_0002");
        QuestTaskListInteractions.select(state, "rewards", "reward_item_0002");

        assertTrue(QuestTaskListInteractions.clearSelection(state, "outside_card_click"));

        assertEquals("", state.questDetails.questDetailsSelectedTaskKind);
        assertEquals("", state.questDetails.questDetailsSelectedTaskId);
        assertTrue(QuestTaskSelectableRewards.isSelectedChoice(state, "reward_item_0002"));
    }

    private static CompoundTag questWithRewards(CompoundTag... rewardTags) {
        CompoundTag quest = new CompoundTag();
        CompoundTag rewards = new CompoundTag();
        for (CompoundTag reward : rewardTags) {
            rewards.put(reward.getString("id"), reward);
        }
        quest.put("rewards", rewards);
        return quest;
    }

    private static CompoundTag reward(String id, boolean selectable) {
        String json = """
                {"id":"%s","type":"questsandstuff:item","item":"minecraft:diamond","amount":1,"nbt":"","selectable":%s}
                """.formatted(id, selectable);
        return rewardTag(id, json);
    }

    private static CompoundTag wrapperReward(String id) {
        String json = """
                {"id":"%s","type":"questsandstuff:selectable","amount":1,"rewards":{"choice_a":{"id":"choice_a","type":"questsandstuff:item","item":"minecraft:diamond","amount":1,"nbt":""}}}
                """.formatted(id);
        return rewardTag(id, json);
    }

    private static CompoundTag rewardTag(String id, String json) {
        CompoundTag reward = new CompoundTag();
        reward.putString("id", id);
        reward.putString("json", json);
        return reward;
    }

    private static QuestTaskEntry entry(String id, CompoundTag tag) {
        return new QuestTaskEntry(id, tag, QuestTaskJsonFactory.read(tag.getString("json")));
    }
}
