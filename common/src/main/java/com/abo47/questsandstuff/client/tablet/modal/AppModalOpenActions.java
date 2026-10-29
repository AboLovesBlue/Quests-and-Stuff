package com.abo47.questsandstuff.client.tablet.modal;

import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuController;
import com.abo47.questsandstuff.client.tablet.controls.SearchNormalizer;
import com.abo47.questsandstuff.client.tablet.modal.target.ModalPickerStates;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;

public final class AppModalOpenActions {
    private AppModalOpenActions() {
    }

    public static void openColorPicker(TabletUiState state, String target, int color) {
        PickerOpenSupport.openPickerModal(state, ModalWindowManager.ModalType.COLOR_PICKER, () -> {
            state.pickers.colorPickerTarget = PickerOpenSupport.clean(target);
            state.pickers.colorDraft = color;
            state.pickers.colorHexDraft = SearchNormalizer.toHexColor(color);
            state.pickers.colorPaletteContextOpen = false;
            state.pickers.colorPaletteContextValue = Integer.MIN_VALUE;
            state.pickers.colorPaletteScrollDragging = false;
            ContextMenuController.clearDeleteConfirm(state);
        });
    }

    public static void openThemePicker(TabletUiState state) {
        PickerOpenSupport.openPickerModal(state, ModalWindowManager.ModalType.THEME_PICKER, () -> {
            state.modal.themeScrollDragging = false;
            ContextMenuController.clearDeleteConfirm(state);
        });
    }

    public static void openPrerequisitesManager(TabletUiState state, String questId) {
        PickerOpenSupport.openPickerModal(state, ModalWindowManager.ModalType.PREREQUISITES_MANAGER, () -> {
            state.modal.prerequisitesManagerQuestId = PickerOpenSupport.clean(questId).trim();
            state.modal.prerequisitesManagerEcMode = false;
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

    public static void openPrerequisitesManagerForEc(TabletUiState state, String ecId) {
        PickerOpenSupport.openPickerModal(state, ModalWindowManager.ModalType.PREREQUISITES_MANAGER, () -> {
            state.modal.prerequisitesManagerQuestId = PickerOpenSupport.clean(ecId).trim();
            state.modal.prerequisitesManagerEcMode = true;
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
