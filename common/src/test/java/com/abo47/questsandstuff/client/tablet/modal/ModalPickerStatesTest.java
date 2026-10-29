package com.abo47.questsandstuff.client.tablet.modal;

import org.junit.jupiter.api.Test;

import com.abo47.questsandstuff.client.tablet.controls.SearchScrollState;
import com.abo47.questsandstuff.client.tablet.state.UiState;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.abo47.questsandstuff.client.tablet.modal.recipe.RecipePickerMode;
import com.abo47.questsandstuff.client.tablet.modal.target.ModalIconPickerMode;
import com.abo47.questsandstuff.client.tablet.modal.target.ModalPickerStates;

class ModalPickerStatesTest {
    @Test
    void recipeBindingResetsSearchFocusScrollDraggingAndKeepsModeSeparate() {
        UiState state = new UiState();
        state.pickers.recipeSearch = "minecraft:stone";
        state.pickers.recipeSearchFocused = true;
        state.pickers.recipeScroll = 42;
        state.pickers.recipeScrollDragging = true;
        state.pickers.recipeMode = RecipePickerMode.TAGS;

        ModalPickerStates.recipe(state).reset();

        assertEquals("", state.pickers.recipeSearch);
        assertFalse(state.pickers.recipeSearchFocused);
        assertEquals(0, state.pickers.recipeScroll);
        assertFalse(state.pickers.recipeScrollDragging);
        assertEquals(RecipePickerMode.TAGS, state.pickers.recipeMode);
    }

    @Test
    void assetBindingTargetsGridScrollFields() {
        UiState state = new UiState();
        state.pickers.assetSearch = "blueprints";
        state.pickers.assetSearchFocused = true;
        state.pickers.assetGridScroll = 16;
        state.pickers.assetGridScrollDragging = true;

        SearchScrollState picker = ModalPickerStates.asset(state);
        picker.clearInteraction();

        assertEquals("blueprints", state.pickers.assetSearch);
        assertEquals(16, state.pickers.assetGridScroll);
        assertFalse(state.pickers.assetSearchFocused);
        assertFalse(state.pickers.assetGridScrollDragging);
    }

    @Test
    void typeLookupBindsEverySimpleResourceListPicker() {
        UiState state = new UiState();

        ModalPickerStates.forType(state, ModalWindowManager.ModalType.BIOME_PICKER).setSearch("plains");
        ModalPickerStates.forType(state, ModalWindowManager.ModalType.ADVANCEMENT_PICKER).setSearch("story");
        ModalPickerStates.forType(state, ModalWindowManager.ModalType.STRUCTURE_PICKER).setSearch("village");
        ModalPickerStates.forType(state, ModalWindowManager.ModalType.STAT_PICKER).setSearch("jump");
        ModalPickerStates.forType(state, ModalWindowManager.ModalType.DIMENSION_PICKER).setSearch("nether");
        ModalPickerStates.forType(state, ModalWindowManager.ModalType.LOOT_TABLE_PICKER).setSearch("chests");

        assertEquals("plains", state.pickers.biomeSearch);
        assertEquals("story", state.pickers.advancementSearch);
        assertEquals("village", state.pickers.structureSearch);
        assertEquals("jump", state.pickers.statSearch);
        assertEquals("nether", state.pickers.dimensionSearch);
        assertEquals("chests", state.pickers.lootTableSearch);
    }

    @Test
    void typeLookupRejectsModalsWithoutPickerState() {
        UiState state = new UiState();

        assertThrows(IllegalArgumentException.class,
                () -> ModalPickerStates.forType(state, ModalWindowManager.ModalType.THEME_PICKER));
    }

    @Test
    void activePickerBindingUpdatesModalSessionPayload() {
        UiState state = new UiState();
        RegistryPickerOpenActions.openRecipePicker(state, "task_recipe|quest|task|questsandstuff:recipe");

        SearchScrollState picker = ModalPickerStates.recipe(state);
        picker.setSearch(" Stone\n");
        picker.setFocused(true);
        picker.setScrollValue(18);
        picker.setDragging(true);

        assertEquals(" stone ", state.pickers.recipeSearch);
        assertEquals(" stone ", state.modal.modalSession.picker().search());
        assertTrue(state.modal.modalSession.picker().focused());
        assertEquals(18, state.pickers.recipeScroll);
        assertEquals(18, state.modal.modalSession.picker().scroll());
        assertTrue(state.modal.modalSession.picker().dragging());
    }

    @Test
    void switchingPickerTypeCreatesFreshSessionPayload() {
        UiState state = new UiState();
        RegistryPickerOpenActions.openRecipePicker(state, "task_recipe|quest|task|questsandstuff:recipe");
        ModalPickerStates.recipe(state).setSearch("stone");
        ModalPickerStates.recipe(state).setScrollValue(22);

        AssetPickerOpenActions.openAssetPicker(state, "reward_icon|quest|reward|icon", "icons/new.png");

        assertEquals(ModalWindowManager.ModalType.ASSET_PICKER, state.modal.modalSession.type());
        assertEquals("", state.modal.modalSession.picker().search());
        assertEquals(0, state.modal.modalSession.picker().scroll());
        assertEquals("icons/new.png", state.modal.modalSession.selectedValue());
        assertEquals("", state.modal.modalSession.target(ModalSession.TargetSlot.QUEST_DETAILS_PICK));
        assertEquals("reward_icon|quest|reward|icon", state.modal.modalSession.target(ModalSession.TargetSlot.QUEST_DETAILS_ASSET_PICK));
    }

    @Test
    void prerequisitesManagerBindingNormalizesQuestSearch() {
        UiState state = new UiState();

        SearchScrollState picker = ModalPickerStates.prerequisitesManager(state);
        picker.setSearch(" Quest_A\n");

        assertEquals(" quest_a ", state.modal.prerequisitesManagerSearch);
        assertEquals("quest_a", picker.normalizedSearch());
        assertEquals("questa", picker.normalizedKey());
    }

    @Test
    void recipeModeCycleKeepsOneModeActiveAndResetsScroll() {
        UiState state = new UiState();
        state.pickers.recipeScroll = 24;

        RecipePickerMode.cycle(state, 1);

        assertEquals(RecipePickerMode.TAGS, state.pickers.recipeMode);
        assertEquals(0, state.pickers.recipeScroll);
        assertEquals("mode_tags", state.pickers.recipeMode.icon());
        assertEquals("tags", state.pickers.recipeMode.logName(state.pickers.recipeSearch));
        assertEquals(1, RecipePickerMode.cycleIndex(state.pickers.recipeMode));
        assertEquals("mode_tags", RecipePickerMode.iconAt(1));

        state.pickers.recipeScroll = 18;
        RecipePickerMode.cycle(state, 1);

        assertEquals(RecipePickerMode.FLUIDS, state.pickers.recipeMode);
        assertEquals(0, state.pickers.recipeScroll);
        assertEquals("mode_fluids", state.pickers.recipeMode.icon());
        assertEquals("fluids", state.pickers.recipeMode.logName(state.pickers.recipeSearch));
        assertEquals(4, RecipePickerMode.cycleSize());

        state.pickers.recipeScroll = 9;
        RecipePickerMode.cycle(state, -1);

        assertEquals(RecipePickerMode.TAGS, state.pickers.recipeMode);
        assertEquals(0, state.pickers.recipeScroll);
    }

    @Test
    void hashRecipeSearchUsesTagModeNameWithoutChangingModeFlags() {
        UiState state = new UiState();
        state.pickers.recipeSearch = " #forge:ingots ";

        assertEquals("tags", state.pickers.recipeMode.logName(state.pickers.recipeSearch));
        assertEquals("mode_items", state.pickers.recipeMode.icon());
        assertEquals(RecipePickerMode.ITEMS, state.pickers.recipeMode);
    }

    @Test
    void iconModeCycleKeepsOneModeActiveAndResetsScroll() {
        UiState state = new UiState();
        state.pickers.iconMode = ModalIconPickerMode.ITEMS;
        state.pickers.iconScroll = 24;

        ModalIconPickerMode.cycle(state, true, true, false, 1);

        assertEquals(ModalIconPickerMode.TAGS, state.pickers.iconMode);
        assertEquals(0, state.pickers.iconScroll);
        assertEquals("mode_tags", state.pickers.iconMode.icon());
        assertEquals("tags", state.pickers.iconMode.logName());

        state.pickers.iconScroll = 18;
        ModalIconPickerMode.cycle(state, true, true, false, 1);

        assertEquals(ModalIconPickerMode.FLUIDS, state.pickers.iconMode);
        assertEquals(0, state.pickers.iconScroll);
        assertEquals("mode_fluids", state.pickers.iconMode.icon());

        ModalIconPickerMode.cycle(state, true, true, false, 1);

        assertEquals(ModalIconPickerMode.ENTITIES, state.pickers.iconMode);
        assertEquals("entity", state.pickers.iconMode.icon());

        ModalIconPickerMode.cycle(state, true, true, false, 1);

        assertEquals(ModalIconPickerMode.INVENTORY, state.pickers.iconMode);
        assertEquals("mode_inventory", state.pickers.iconMode.icon());
        ModalIconPickerMode[] cycle = ModalIconPickerMode.cycleForContext(true, true, false);
        assertEquals(4, ModalIconPickerMode.cycleIndex(state.pickers.iconMode, cycle));
        assertEquals("mode_inventory", ModalIconPickerMode.iconAt(cycle, 4));

        ModalIconPickerMode.cycle(state, true, true, false, -1);

        assertEquals(ModalIconPickerMode.ENTITIES, state.pickers.iconMode);
    }

    @Test
    void useItemIconModeCyclesUsableItemsBeforeAllItems() {
        UiState state = new UiState();
        state.pickers.iconMode = ModalIconPickerMode.USABLE_ITEMS;

        ModalIconPickerMode.cycle(state, false, true, true, 1);

        assertEquals(ModalIconPickerMode.ITEMS, state.pickers.iconMode);
        assertEquals("items", state.pickers.iconMode.logName());

        ModalIconPickerMode.cycle(state, false, true, true, -1);

        assertEquals(ModalIconPickerMode.USABLE_ITEMS, state.pickers.iconMode);
        assertEquals("usable_items", state.pickers.iconMode.logName());
        assertEquals("send-horizontal", state.pickers.iconMode.icon());
    }

    @Test
    void modelItemIconModeOnlyCyclesItemsAndTags() {
        UiState state = new UiState();
        state.pickers.iconMode = ModalIconPickerMode.ENTITIES;

        ModalIconPickerMode.cycleModelItems(state, 1);

        assertEquals(ModalIconPickerMode.TAGS, state.pickers.iconMode);
        assertEquals(1, ModalIconPickerMode.cycleIndex(state.pickers.iconMode, ModalIconPickerMode.modelItemCycle()));
        assertEquals("mode_tags", ModalIconPickerMode.iconAt(ModalIconPickerMode.modelItemCycle(), 1));

        ModalIconPickerMode.cycleModelItems(state, 1);

        assertEquals(ModalIconPickerMode.ITEMS, state.pickers.iconMode);
    }
}
