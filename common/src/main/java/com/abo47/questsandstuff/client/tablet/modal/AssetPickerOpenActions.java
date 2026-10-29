package com.abo47.questsandstuff.client.tablet.modal;

import java.util.Collection;
import java.util.Set;

import com.abo47.questsandstuff.client.tablet.modal.target.ModalPickerStates;
import com.abo47.questsandstuff.client.tablet.modal.target.ModalTargets;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;

import static com.abo47.questsandstuff.client.tablet.ui.state.TabletModalState.clearBlueprintCodeState;

public final class AssetPickerOpenActions {
    private AssetPickerOpenActions() {
    }

    public static void openQuestDetailsAssetIconPicker(TabletUiState state, String target) {
        openAssetPickerSession(state, "", () -> state.questDetails.questDetailsPickTarget = PickerOpenSupport.clean(target));
    }

    public static void openChapterAssetIconPicker(TabletUiState state, String chapter) {
        openAssetPickerSession(state, "", () -> state.modal.modalAssetIconTarget = ModalTargets.chapterIcon(chapter));
    }

    public static void openQuestAssetIconPicker(TabletUiState state, String questId) {
        openAssetPickerSession(state, "", () -> state.modal.modalAssetIconTarget = ModalTargets.questIcon(questId));
    }

    public static void openAssetPicker(TabletUiState state, String target) {
        openAssetPicker(state, target, "");
    }

    public static void openAssetPicker(TabletUiState state, String target, String selectedAsset) {
        openAssetPickerSession(state, selectedAsset, () -> state.questDetails.questDetailsAssetPickTarget = PickerOpenSupport.clean(target));
    }

    public static void openBlueprintPicker(TabletUiState state, String selectedBlueprint) {
        openAssetPickerSession(state, selectedBlueprint, () -> {
            state.modal.modalBlueprintTarget = "canvas";
            state.pickers.assetBrowseDir = "blueprints";
        });
    }

    public static void openChapterBackgroundPicker(TabletUiState state, String chapter, String currentBackground) {
        openAssetPickerSession(state, currentBackground, () -> state.modal.modalChapterTarget = PickerOpenSupport.clean(chapter));
    }

    public static void openQuestBackgroundPicker(TabletUiState state, String questId, String currentBackground, boolean grayscale) {
        openAssetPickerSession(state, currentBackground, () -> {
            state.modal.modalQuestBackgroundTarget = PickerOpenSupport.clean(questId);
            state.modal.modalQuestBackgroundGrayscale = grayscale;
        });
    }

    public static void openEcBackgroundPicker(TabletUiState state, String chapter, String ecId, String currentBackground) {
        openAssetPickerSession(state, currentBackground, () -> {
            state.modal.modalEcBackgroundTarget = chapter + ":" + ecId;
        });
    }

    public static void openBatchQuestBackgroundPicker(TabletUiState state, Collection<String> questIds, String currentBackground, boolean grayscale) {
        Set<String> targets = PickerOpenSupport.normalizedTargets(questIds);
        openAssetPickerSession(state, currentBackground, () -> {
            state.modal.modalQuestBackgroundTargets.addAll(targets);
            state.modal.modalQuestBackgroundGrayscale = grayscale;
        });
    }

    public static void openQuestCompletionHudBackgroundPicker(TabletUiState state, String questId, String currentBackground) {
        openAssetPickerSession(state, currentBackground, () -> state.modal.modalQuestCompletionHudBackgroundTarget = PickerOpenSupport.clean(questId));
    }

    public static void openBatchQuestCompletionHudBackgroundPicker(TabletUiState state, Collection<String> questIds, String currentBackground) {
        Set<String> targets = PickerOpenSupport.normalizedTargets(questIds);
        openAssetPickerSession(state, currentBackground, () -> state.modal.modalQuestCompletionHudBackgroundTargets.addAll(targets));
    }

    public static void openCanvasBackgroundPicker(TabletUiState state, String chapter, String currentBackground) {
        openAssetPickerSession(state, currentBackground, () -> state.modal.modalCanvasBackgroundTarget = PickerOpenSupport.clean(chapter));
    }

    public static void openHudBackgroundPicker(TabletUiState state, String target, String currentBackground, int currentOpacity) {
        openAssetPickerSession(state, currentBackground, () -> {
            state.modal.modalHudBackgroundTarget = PickerOpenSupport.clean(target).trim();
            state.modal.modalHudBackgroundOpacityDraft = Math.max(0, Math.min(100, currentOpacity));
            state.modal.modalHudBackgroundOpacityDragging = false;
        });
    }

    public static void openCanvasImagePicker(TabletUiState state, String chapter, int logicalX, int logicalY) {
        openAssetPickerSession(state, "", () -> {
            state.modal.modalCanvasImageTarget = PickerOpenSupport.clean(chapter);
            PickerOpenSupport.setCanvasPickPoint(state, logicalX, logicalY);
        });
    }

    public static void openConnectionTexturePicker(TabletUiState state, String chapter, String sourceQuestId, String targetQuestId) {
        openAssetPickerSession(state, "", () -> {
            state.modal.modalConnectionTextureTarget = ModalTargets.connection(chapter, sourceQuestId, targetQuestId);
        });
    }

    public static void openConnectionTexturePicker(TabletUiState state, String target) {
        openAssetPickerSession(state, "", () -> {
            state.modal.modalConnectionTextureTarget = target;
        });
    }

    public static void openChapterConnectionTexturePicker(TabletUiState state, String chapter, java.util.Collection<String> questIds) {
        openAssetPickerSession(state, "", () -> {
            state.modal.modalConnectionTextureTarget = ModalTargets.of(ModalTargets.CONNECTION, chapter, "", "");
            state.modal.modalConnectionTextureChapterTargets.addAll(questIds);
        });
    }

    static void resetAssetPicker(TabletUiState state) {
        state.pickers.assetContextOpen = false;
        state.pickers.assetRenameOpen = false;
        clearBlueprintCodeState(state);
        state.pickers.assetBrowseDir = "";
        state.pickers.assetPickerSessionFresh = true;
        ModalPickerStates.asset(state).reset();
    }

    static void openAssetPickerSession(TabletUiState state, String selectedAsset, Runnable configure) {
        PickerOpenSupport.openPickerModal(state, ModalWindowManager.ModalType.ASSET_PICKER, () -> {
            resetAssetPicker(state);
            configure.run();
            state.pickers.assetSelected = PickerOpenSupport.clean(selectedAsset);
        });
    }
}
