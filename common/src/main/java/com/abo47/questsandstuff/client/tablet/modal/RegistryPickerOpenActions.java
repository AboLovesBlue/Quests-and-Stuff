package com.abo47.questsandstuff.client.tablet.modal;

import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuController;
import com.abo47.questsandstuff.client.tablet.entity.EntityPreviewRenderer;
import com.abo47.questsandstuff.client.tablet.modal.target.ModalPickerStates;
import com.abo47.questsandstuff.client.tablet.state.UiState;

public final class RegistryPickerOpenActions {
    private RegistryPickerOpenActions() {
    }

    public static void openBiomePicker(UiState state, String target) {
        openQuestDetailsPicker(state, target, ModalWindowManager.ModalType.BIOME_PICKER, () -> ModalPickerStates.biome(state).reset());
    }

    public static void openAdvancementPicker(UiState state, String target) {
        openQuestDetailsPicker(state, target, ModalWindowManager.ModalType.ADVANCEMENT_PICKER, () -> ModalPickerStates.advancement(state).reset());
    }

    public static void openRecipePicker(UiState state, String target) {
        openQuestDetailsPicker(state, target, ModalWindowManager.ModalType.RECIPE_PICKER, () -> ModalOpenSupport.resetRecipePicker(state));
    }

    public static void openStructurePicker(UiState state, String target) {
        openQuestDetailsPicker(state, target, ModalWindowManager.ModalType.STRUCTURE_PICKER, () -> ModalPickerStates.structure(state).reset());
    }

    public static void openBlockPicker(UiState state, String target) {
        openQuestDetailsPicker(state, target, ModalWindowManager.ModalType.BLOCK_PICKER, () -> ModalOpenSupport.resetBlockPicker(state));
    }

    public static void openStatPicker(UiState state, String target) {
        openQuestDetailsPicker(state, target, ModalWindowManager.ModalType.STAT_PICKER, () -> ModalPickerStates.stat(state).reset());
    }

    public static void openDimensionPicker(UiState state, String target) {
        openQuestDetailsPicker(state, target, ModalWindowManager.ModalType.DIMENSION_PICKER, () -> ModalPickerStates.dimension(state).reset());
    }

    public static void openLootTablePicker(UiState state, String target) {
        openQuestDetailsPicker(state, target, ModalWindowManager.ModalType.LOOT_TABLE_PICKER, () -> ModalPickerStates.lootTable(state).reset());
    }

    public static void openItemLockPicker(UiState state, String target) {
        openQuestDetailsPicker(state, target, ModalWindowManager.ModalType.ITEM_LOCK_PICKER, () -> ModalPickerStates.itemLock(state).reset());
    }

    public static void openItemInventoryPicker(UiState state, String target) {
        openQuestDetailsPicker(state, target, ModalWindowManager.ModalType.ITEM_INVENTORY_PICKER, () -> ModalPickerStates.itemInventory(state).reset());
    }

    public static void openCanvasBlockPicker(UiState state, String target, int logicalX, int logicalY) {
        ModalOpenSupport.openPickerModal(state, ModalWindowManager.ModalType.BLOCK_PICKER, () -> {
            state.modal.modalCanvasModelTarget = ModalOpenSupport.clean(target);
            ModalOpenSupport.setCanvasPickPoint(state, logicalX, logicalY);
            ModalOpenSupport.resetBlockPicker(state);
        });
    }

    public static void openCanvasRecipePicker(UiState state, String target, int logicalX, int logicalY) {
        ModalOpenSupport.openPickerModal(state, ModalWindowManager.ModalType.RECIPE_PICKER, () -> {
            state.questDetails.questDetailsPickTarget = ModalOpenSupport.clean(target);
            ModalOpenSupport.setCanvasPickPoint(state, logicalX, logicalY);
            ModalOpenSupport.resetRecipePicker(state);
        });
    }

    public static void openEntityVariantPicker(UiState state, String target, String icon) {
        ModalOpenSupport.openPickerModal(state, ModalWindowManager.ModalType.ENTITY_VARIANT_PICKER, () -> {
            state.pickers.entityVariantTarget = ModalOpenSupport.clean(target);
            state.pickers.entityVariantSelected = EntityPreviewRenderer.entityVariant(ModalOpenSupport.clean(icon));
            state.pickers.entityVariantFolder = "";
            ModalPickerStates.entityVariant(state).reset();
            ContextMenuController.clearDeleteConfirm(state);
        });
    }

    static void openQuestDetailsPicker(UiState state, String target, ModalWindowManager.ModalType type, Runnable reset) {
        ModalOpenSupport.openPickerModal(state, type, () -> {
            state.questDetails.questDetailsPickTarget = ModalOpenSupport.clean(target);
            reset.run();
        });
    }
}
