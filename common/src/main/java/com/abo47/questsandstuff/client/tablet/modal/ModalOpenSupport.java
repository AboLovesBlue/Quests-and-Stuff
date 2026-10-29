package com.abo47.questsandstuff.client.tablet.modal;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuController;
import com.abo47.questsandstuff.client.tablet.modal.recipe.RecipePickerMode;
import com.abo47.questsandstuff.client.tablet.modal.target.ModalIconPickerMode;
import com.abo47.questsandstuff.client.tablet.modal.target.ModalPickerStates;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;

import static com.abo47.questsandstuff.client.tablet.ui.state.TabletModalState.clearAllModalTargets;
import static com.abo47.questsandstuff.client.tablet.ui.state.TabletModalState.openModal;

final class ModalOpenSupport {
    private ModalOpenSupport() {
    }

    static void openPickerModal(TabletUiState state, ModalWindowManager.ModalType type, Runnable configure) {
        closeBeforeOpen(state);
        clearAllModalTargets(state);
        configure.run();
        openModal(state, type);
    }

    static void closeBeforeOpen(TabletUiState state) {
        ModalCloseActions.closeAllImmediately(state);
        ContextMenuController.close(state);
        state.questDetails.questDetailsContextOpen = false;
        state.questDetails.questDetailsContextKind = "";
        state.questDetails.questDetailsContextId = "";
        state.questDetails.questDetailsContextScroll = 0;
        state.questDetails.questDetailsContextScrollMax = 0;
        state.modal.prerequisitesManagerContextOpen = false;
    }

    static void setCanvasPickPoint(TabletUiState state, int logicalX, int logicalY) {
        state.canvas.canvasImageLogicalX = logicalX;
        state.canvas.canvasImageLogicalY = logicalY;
    }

    static String clean(String value) {
        return value == null ? "" : value;
    }

    static Set<String> normalizedTargets(Collection<String> questIds) {
        Set<String> targets = new LinkedHashSet<>();
        if (questIds == null) {
            return targets;
        }
        for (String questId : questIds) {
            String target = questId == null ? "" : questId.trim();
            if (!target.isBlank()) {
                targets.add(target);
            }
        }
        return targets;
    }

    static String firstTarget(Set<String> targets) {
        return targets.isEmpty() ? "" : targets.iterator().next();
    }

    static void resetIconPicker(TabletUiState state) {
        ModalPickerStates.icon(state).reset();
        ModalIconPickerMode.reset(state);
    }

    static void resetBlockPicker(TabletUiState state) {
        ModalPickerStates.block(state).reset();
        state.pickers.blockTagMode = false;
    }

    static void resetRecipePicker(TabletUiState state) {
        ModalPickerStates.recipe(state).reset();
        RecipePickerMode.reset(state);
    }
}
