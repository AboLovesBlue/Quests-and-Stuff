package com.abo47.questsandstuff.client.tablet.modal.recipe;

import net.minecraft.world.entity.player.Player;

import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.controls.TabletCycleButton;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.text.QuestTranslationKeys;
import com.abo47.questsandstuff.client.tablet.text.TabletTranslationKeys;
import com.abo47.questsandstuff.client.tablet.modal.ModalShell;

public final class RecipePickerModal {
    private RecipePickerModal() {
    }

    public static TextFieldWidget rebuild(WidgetGroup modal, TabletUiState state, Player player, Runnable refresh, int w, int h) {
        ModalShell.addTitleAndClose(modal, TabletTranslationKeys.text(QuestTranslationKeys.CHOOSE_RECIPE), w, state, refresh);
        int sidePad = 8;
        int headY = ModalShell.PICKER_HEADER_Y;
        int headH = 18;
        int modeW = headH;
        int gap = 4;
        int gridX = sidePad;
        int gridW = w - sidePad * 2;
        int searchX = gridX + modeW + gap;
        int searchW = gridW - modeW - gap;
        int gridY = headY + headH + 4;
        int gridH = h - gridY - 8;

        TextFieldWidget search = ModalShell.addSearchField(modal, searchX, headY, Math.max(24, searchW), headH, state.pickers.recipeSearch, ModalShell.SEARCH_MED, value -> {
            String query = RecipePickerModeController.setSearch(state, value);
            RecipePickerMode mode = RecipePickerModeController.mode(state);
            QuestsAndStuffMod.debugLog("[QnS:UI] recipe search mode={} query='{}'", mode.logName(query), query);
            refresh.run();
        }, focused -> state.pickers.recipeSearchFocused = focused);

        TabletCycleButton.addIconModeButton(
                modal,
                gridX,
                headY,
                modeW,
                headH,
                RecipePickerModeController.cycleSize(),
                () -> RecipePickerModeController.cycleIndex(state),
                RecipePickerModeController::iconAt,
                RecipePickerModeController.tooltip(state),
                direction -> {
                    RecipePickerMode nextMode = RecipePickerModeController.cycle(state, direction);
                    QuestsAndStuffMod.debugLog("[QnS:UI] recipe picker mode={} direction={}", nextMode.logName(state.pickers.recipeSearch), direction < 0 ? "backward" : "forward");
                    refresh.run();
                });
        RecipePickerApplyActions.addRecipeViewerKeyHandler(modal, state, player, refresh);
        RecipePickerGridRenderer.add(modal, state, player, refresh, gridX, gridY, gridW, gridH);
        return search;
    }
}
