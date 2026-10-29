package com.abo47.questsandstuff.client.tablet.modal;

import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuController;
import com.abo47.questsandstuff.client.tablet.controls.SearchNormalizer;
import com.abo47.questsandstuff.client.tablet.modal.target.ModalPickerStates;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;

public final class AppOpenActions {
    private AppOpenActions() {
    }

    public static void openColorPicker(TabletUiState state, String target, int color) {
        ModalOpenSupport.openPickerModal(state, ModalWindowManager.ModalType.COLOR_PICKER, () -> {
            state.pickers.colorPickerTarget = ModalOpenSupport.clean(target);
            state.pickers.colorDraft = color;
            state.pickers.colorHexDraft = SearchNormalizer.toHexColor(color);
            state.pickers.colorPaletteContextOpen = false;
            state.pickers.colorPaletteContextValue = Integer.MIN_VALUE;
            state.pickers.colorPaletteScrollDragging = false;
            ContextMenuController.clearDeleteConfirm(state);
        });
    }

    public static void openThemePicker(TabletUiState state) {
        ModalOpenSupport.openPickerModal(state, ModalWindowManager.ModalType.THEME_PICKER, () -> {
            state.modal.themeScrollDragging = false;
            ContextMenuController.clearDeleteConfirm(state);
        });
    }

    public static void openPrerequisitesManager(TabletUiState state, String questId) {
        ModalOpenSupport.openPickerModal(state, ModalWindowManager.ModalType.PREREQUISITES_MANAGER, () -> {
            state.modal.prerequisitesManagerQuestId = ModalOpenSupport.clean(questId).trim();
            state.modal.prerequisitesManagerExclusiveChoiceMode = false;
            ModalPickerStates.prerequisitesManager(state).reset();
            state.modal.prerequisitesManagerExternalMode = false;
            state.modal.prerequisitesManagerContextOpen = false;
            state.modal.prerequisitesManagerContextPrerequisiteId = "";
            state.modal.prerequisitesManagerSelectedConnectionKey = "";
            state.modal.prerequisitesManagerHoveredConnectionKey = "";
            ContextMenuController.clearDeleteConfirm(state);
            ContextMenuController.close(state);
        });
    }

    public static void openPrerequisitesManagerForExclusiveChoice(TabletUiState state, String exclusiveChoiceId) {
        ModalOpenSupport.openPickerModal(state, ModalWindowManager.ModalType.PREREQUISITES_MANAGER, () -> {
            state.modal.prerequisitesManagerQuestId = ModalOpenSupport.clean(exclusiveChoiceId).trim();
            state.modal.prerequisitesManagerExclusiveChoiceMode = true;
            ModalPickerStates.prerequisitesManager(state).reset();
            state.modal.prerequisitesManagerExternalMode = false;
            state.modal.prerequisitesManagerContextOpen = false;
            state.modal.prerequisitesManagerContextPrerequisiteId = "";
            state.modal.prerequisitesManagerSelectedConnectionKey = "";
            state.modal.prerequisitesManagerHoveredConnectionKey = "";
            ContextMenuController.clearDeleteConfirm(state);
            ContextMenuController.close(state);
        });
    }
}
