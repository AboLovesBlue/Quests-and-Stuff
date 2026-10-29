package com.abo47.questsandstuff.client.tablet.modal;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import com.abo47.questsandstuff.client.tablet.modal.recipe.RecipePickerChoiceIndex;

class RecipeChoiceIndexTest {
    @Test
    void outputSearchMatchesValueDisplayNameAndRecipeIds() {
        RecipePickerChoiceIndex.RecipeChoice iron = choice("minecraft:iron_ingot", "Iron Ingot", "minecraft:smelting/iron_ingot", false);
        RecipePickerChoiceIndex.RecipeChoice bread = choice("minecraft:bread", "Bread", "minecraft:crafting/bread", false);
        RecipePickerChoiceIndex.RecipeChoices choices = new RecipePickerChoiceIndex.RecipeChoices(List.of(iron, bread), List.of());

        assertSame(iron, RecipePickerChoiceIndex.filterChoices(choices, "iron", false).get(0));
        assertSame(iron, RecipePickerChoiceIndex.filterChoices(choices, "smelting", false).get(0));
        assertSame(bread, RecipePickerChoiceIndex.filterChoices(choices, "minecraft:bread", false).get(0));
    }

    @Test
    void tagSearchUsesTagsForTagModeOrHashPrefix() {
        RecipePickerChoiceIndex.RecipeChoice output = choice("minecraft:iron_ingot", "Iron Ingot", "minecraft:smelting/iron_ingot", false);
        RecipePickerChoiceIndex.RecipeChoice tag = choice("#forge:ingots/iron", "Iron Ingots", "", true);
        RecipePickerChoiceIndex.RecipeChoices choices = new RecipePickerChoiceIndex.RecipeChoices(List.of(output), List.of(tag));

        assertSame(tag, RecipePickerChoiceIndex.filterChoices(choices, "iron", true).get(0));
        assertSame(tag, RecipePickerChoiceIndex.filterChoices(choices, "#forge:ingots", false).get(0));
        assertEquals(List.of(output), RecipePickerChoiceIndex.filterChoices(choices, "", false));
    }

    @Test
    void nullPreviewsBecomeEmptyArray() {
        RecipePickerChoiceIndex.RecipeChoice choice = RecipePickerChoiceIndex.RecipeChoice.of("minecraft:diamond", "Diamond", null, List.of(), false);

        assertEquals(0, choice.previews().length);
    }

    private static RecipePickerChoiceIndex.RecipeChoice choice(String value, String displayName, String recipeId, boolean tag) {
        List<String> recipes = recipeId == null || recipeId.isBlank() ? List.of() : List.of(recipeId);
        return RecipePickerChoiceIndex.RecipeChoice.of(value, displayName, null, recipes, tag);
    }
}
