package com.abo47.questsandstuff.client.tablet.modal;

import java.util.Collection;
import java.util.Set;

import com.abo47.questsandstuff.client.tablet.modal.target.ModalPickerStates;
import com.abo47.questsandstuff.client.tablet.modal.target.ModalTargets;
import com.abo47.questsandstuff.client.tablet.state.UiState;

import static com.abo47.questsandstuff.client.tablet.ui.state.ModalState.clearBlueprintCodeState;

public final class AssetPickerOpenActions {
    private AssetPickerOpenActions() {
    }

    public static void openQuestDetailsAssetIconPicker(UiState state, String target) {
        openAssetPickerSession(state, "", () -> state.questDetails.questDetailsPickTarget = ModalOpenSupport.clean(target));
    }

    public static void openChapterAssetIconPicker(UiState state, String chapter) {
        openAssetPickerSession(state, "", () -> state.modal.modalAssetIconTarget = ModalTargets.chapterIcon(chapter));
    }

    public static void openQuestAssetIconPicker(UiState state, String questId) {
        openAssetPickerSession(state, "", () -> state.modal.modalAssetIconTarget = ModalTargets.questIcon(questId));
    }

    public static void openAssetPicker(UiState state, String target) {
        openAssetPicker(state, target, "");
    }

    public static void openAssetPicker(UiState state, String target, String selectedAsset) {
        openAssetPickerSession(state, selectedAsset, () -> state.questDetails.questDetailsAssetPickTarget = ModalOpenSupport.clean(target));
    }

    public static void openBlueprintPicker(UiState state, String selectedBlueprint) {
        openAssetPickerSession(state, selectedBlueprint, () -> {
            state.modal.modalBlueprintTarget = "canvas";
            state.pickers.assetBrowseDir = "blueprints";
        });
    }

    public static void openChapterBackgroundPicker(UiState state, String chapter, String currentBackground) {
        openAssetPickerSession(state, currentBackground, () -> state.modal.modalChapterTarget = ModalOpenSupport.clean(chapter));
    }

    public static void openQuestBackgroundPicker(UiState state, String questId, String currentBackground, boolean grayscale) {
        openAssetPickerSession(state, currentBackground, () -> {
            state.modal.modalQuestBackgroundTarget = ModalOpenSupport.clean(questId);
            state.modal.modalQuestBackgroundGrayscale = grayscale;
        });
    }

    public static void openExclusiveChoiceBackgroundPicker(UiState state, String chapter, String exclusiveChoiceId, String currentBackground) {
        openAssetPickerSession(state, currentBackground, () -> {
            state.modal.modalExclusiveChoiceBackgroundTarget = chapter + ":" + exclusiveChoiceId;
        });
    }

    public static void openBatchQuestBackgroundPicker(UiState state, Collection<String> questIds, String currentBackground, boolean grayscale) {
        Set<String> targets = ModalOpenSupport.normalizedTargets(questIds);
        openAssetPickerSession(state, currentBackground, () -> {
            state.modal.modalQuestBackgroundTargets.addAll(targets);
            state.modal.modalQuestBackgroundGrayscale = grayscale;
        });
    }

    public static void openQuestCompletionHudBackgroundPicker(UiState state, String questId, String currentBackground) {
        openAssetPickerSession(state, currentBackground, () -> state.modal.modalQuestCompletionHudBackgroundTarget = ModalOpenSupport.clean(questId));
    }

    public static void openBatchQuestCompletionHudBackgroundPicker(UiState state, Collection<String> questIds, String currentBackground) {
        Set<String> targets = ModalOpenSupport.normalizedTargets(questIds);
        openAssetPickerSession(state, currentBackground, () -> state.modal.modalQuestCompletionHudBackgroundTargets.addAll(targets));
    }

    public static void openCanvasBackgroundPicker(UiState state, String chapter, String currentBackground) {
        openAssetPickerSession(state, currentBackground, () -> state.modal.modalCanvasBackgroundTarget = ModalOpenSupport.clean(chapter));
    }

    public static void openHudBackgroundPicker(UiState state, String target, String currentBackground, int currentOpacity) {
        openAssetPickerSession(state, currentBackground, () -> {
            state.modal.modalHudBackgroundTarget = ModalOpenSupport.clean(target).trim();
            state.modal.modalHudBackgroundOpacityDraft = Math.max(0, Math.min(100, currentOpacity));
            state.modal.modalHudBackgroundOpacityDragging = false;
        });
    }

    public static void openCanvasImagePicker(UiState state, String chapter, int logicalX, int logicalY) {
        openAssetPickerSession(state, "", () -> {
            state.modal.modalCanvasImageTarget = ModalOpenSupport.clean(chapter);
            ModalOpenSupport.setCanvasPickPoint(state, logicalX, logicalY);
        });
    }

    public static void openConnectionTexturePicker(UiState state, String chapter, String sourceQuestId, String targetQuestId) {
        openAssetPickerSession(state, "", () -> {
            state.modal.modalConnectionTextureTarget = ModalTargets.connection(chapter, sourceQuestId, targetQuestId);
        });
    }

    public static void openConnectionTexturePicker(UiState state, String target) {
        openAssetPickerSession(state, "", () -> {
            state.modal.modalConnectionTextureTarget = target;
        });
    }

    public static void openChapterConnectionTexturePicker(UiState state, String chapter, java.util.Collection<String> questIds) {
        openAssetPickerSession(state, "", () -> {
            state.modal.modalConnectionTextureTarget = ModalTargets.of(ModalTargets.CONNECTION, chapter, "", "");
            state.modal.modalConnectionTextureChapterTargets.addAll(questIds);
        });
    }

    static void resetAssetPicker(UiState state) {
        state.pickers.assetContextOpen = false;
        state.pickers.assetRenameOpen = false;
        clearBlueprintCodeState(state);
        state.pickers.assetBrowseDir = "";
        state.pickers.assetPickerSessionFresh = true;
        ModalPickerStates.asset(state).reset();
    }

    static void openAssetPickerSession(UiState state, String selectedAsset, Runnable configure) {
        ModalOpenSupport.openPickerModal(state, ModalWindowManager.ModalType.ASSET_PICKER, () -> {
            resetAssetPicker(state);
            configure.run();
            state.pickers.assetSelected = ModalOpenSupport.clean(selectedAsset);
        });
    }
}
