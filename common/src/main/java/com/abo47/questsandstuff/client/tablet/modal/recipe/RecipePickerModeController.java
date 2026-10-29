package com.abo47.questsandstuff.client.tablet.modal.recipe;

import net.minecraft.network.chat.Component;

import com.abo47.questsandstuff.client.tablet.controls.SearchFilter;
import com.abo47.questsandstuff.client.tablet.state.UiState;

public final class RecipePickerModeController {
    private RecipePickerModeController() {
    }

    public static RecipePickerMode mode(UiState state) {
        return state == null ? RecipePickerMode.ITEMS : RecipePickerMode.safe(state.pickers.recipeMode);
    }

    public static ContentKind contentKind(UiState state) {
        return contentKind(mode(state));
    }

    public static ContentKind contentKind(RecipePickerMode mode) {
        return switch (RecipePickerMode.safe(mode)) {
            case INVENTORY -> ContentKind.INVENTORY;
            case FLUIDS -> ContentKind.FLUIDS;
            case ITEMS, TAGS -> ContentKind.RECIPES;
        };
    }

    public static String setSearch(UiState state, String value) {
        if (state == null) {
            return "";
        }
        state.pickers.recipeSearch = SearchFilter.normalizeUserInput(value);
        state.pickers.recipeScroll = 0;
        return state.pickers.recipeSearch;
    }

    public static RecipePickerMode cycle(UiState state, int direction) {
        RecipePickerMode.cycle(state, direction);
        return mode(state);
    }

    public static int cycleIndex(UiState state) {
        return RecipePickerMode.cycleIndex(mode(state));
    }

    public static int cycleSize() {
        return RecipePickerMode.cycleSize();
    }

    public static String iconAt(int index) {
        return RecipePickerMode.iconAt(index);
    }

    public static Component[] tooltip(UiState state) {
        return mode(state).tooltip(state == null ? "" : state.pickers.recipeSearch);
    }

    public enum ContentKind {
        RECIPES,
        FLUIDS,
        INVENTORY
    }
}
