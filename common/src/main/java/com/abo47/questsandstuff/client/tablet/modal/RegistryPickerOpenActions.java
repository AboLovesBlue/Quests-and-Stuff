package com.abo47.questsandstuff.client.tablet.modal;

import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuController;
import com.abo47.questsandstuff.client.tablet.entity.EntityPreviewRenderer;
import com.abo47.questsandstuff.client.tablet.modal.target.ModalPickerStates;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;

public final class RegistryPickerOpenActions {
    private RegistryPickerOpenActions() {
    }

    public static void openBiomePicker(TabletUiState state, String target) {
        openQuestDetailsPicker(state, target, ModalWindowManager.ModalType.BIOME_PICKER, () -> ModalPickerStates.biome(state).reset());
    }

    public static void openAdvancementPicker(TabletUiState state, String target) {
        openQuestDetailsPicker(state, target, ModalWindowManager.ModalType.ADVANCEMENT_PICKER, () -> ModalPickerStates.advancement(state).reset());
    }

    public static void openRecipePicker(TabletUiState state, String target) {
        openQuestDetailsPicker(state, target, ModalWindowManager.ModalType.RECIPE_PICKER, () -> PickerOpenSupport.resetRecipePicker(state));
    }

    public static void openStructurePicker(TabletUiState state, String target) {
        openQuestDetailsPicker(state, target, ModalWindowManager.ModalType.STRUCTURE_PICKER, () -> ModalPickerStates.structure(state).reset());
    }

    public static void openBlockPicker(TabletUiState state, String target) {
        openQuestDetailsPicker(state, target, ModalWindowManager.ModalType.BLOCK_PICKER, () -> PickerOpenSupport.resetBlockPicker(state));
    }

    public static void openStatPicker(TabletUiState state, String target) {
        openQuestDetailsPicker(state, target, ModalWindowManager.ModalType.STAT_PICKER, () -> ModalPickerStates.stat(state).reset());
    }

    public static void openDimensionPicker(TabletUiState state, String target) {
        openQuestDetailsPicker(state, target, ModalWindowManager.ModalType.DIMENSION_PICKER, () -> ModalPickerStates.dimension(state).reset());
    }

    public static void openLootTablePicker(TabletUiState state, String target) {
        openQuestDetailsPicker(state, target, ModalWindowManager.ModalType.LOOT_TABLE_PICKER, () -> ModalPickerStates.lootTable(state).reset());
    }

    public static void openItemLockPicker(TabletUiState state, String target) {
        openQuestDetailsPicker(state, target, ModalWindowManager.ModalType.ITEM_LOCK_PICKER, () -> ModalPickerStates.itemLock(state).reset());
    }

    public static void openItemInventoryPicker(TabletUiState state, String target) {
        openQuestDetailsPicker(state, target, ModalWindowManager.ModalType.ITEM_INVENTORY_PICKER, () -> ModalPickerStates.itemInventory(state).reset());
    }

    public static void openCanvasBlockPicker(TabletUiState state, String target, int logicalX, int logicalY) {
        PickerOpenSupport.openPickerModal(state, ModalWindowManager.ModalType.BLOCK_PICKER, () -> {
            state.modal.modalCanvasModelTarget = PickerOpenSupport.clean(target);
            PickerOpenSupport.setCanvasPickPoint(state, logicalX, logicalY);
            PickerOpenSupport.resetBlockPicker(state);
        });
    }

    public static void openCanvasRecipePicker(TabletUiState state, String target, int logicalX, int logicalY) {
        PickerOpenSupport.openPickerModal(state, ModalWindowManager.ModalType.RECIPE_PICKER, () -> {
            state.questDetails.questDetailsPickTarget = PickerOpenSupport.clean(target);
            PickerOpenSupport.setCanvasPickPoint(state, logicalX, logicalY);
            PickerOpenSupport.resetRecipePicker(state);
        });
    }

    public static void openEntityVariantPicker(TabletUiState state, String target, String icon) {
        PickerOpenSupport.openPickerModal(state, ModalWindowManager.ModalType.ENTITY_VARIANT_PICKER, () -> {
            state.pickers.entityVariantTarget = PickerOpenSupport.clean(target);
            state.pickers.entityVariantSelected = EntityPreviewRenderer.entityVariant(PickerOpenSupport.clean(icon));
            state.pickers.entityVariantFolder = "";
            ModalPickerStates.entityVariant(state).reset();
            ContextMenuController.clearDeleteConfirm(state);
        });
    }

    static void openQuestDetailsPicker(TabletUiState state, String target, ModalWindowManager.ModalType type, Runnable reset) {
        PickerOpenSupport.openPickerModal(state, type, () -> {
            state.questDetails.questDetailsPickTarget = PickerOpenSupport.clean(target);
            reset.run();
        });
    }
}
