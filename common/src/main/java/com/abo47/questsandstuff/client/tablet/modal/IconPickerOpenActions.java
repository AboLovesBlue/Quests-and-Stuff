package com.abo47.questsandstuff.client.tablet.modal;

import java.util.Collection;
import java.util.Set;

import com.abo47.questsandstuff.client.tablet.modal.target.ModalIconPickerMode;
import com.abo47.questsandstuff.client.tablet.modal.target.ModalTargets;
import com.abo47.questsandstuff.client.tablet.state.UiState;

public final class IconPickerOpenActions {
    private IconPickerOpenActions() {
    }

    public static void openQuestDetailsIconPicker(UiState state, String target) {
        ModalOpenSupport.openPickerModal(state, ModalWindowManager.ModalType.ICON_PICKER, () -> {
            state.questDetails.questDetailsPickTarget = ModalOpenSupport.clean(target);
            ModalOpenSupport.resetIconPicker(state);
            ModalIconPickerMode.resetForTarget(state, state.questDetails.questDetailsPickTarget);
        });
    }

    public static void openChapterIconPicker(UiState state, String chapter) {
        ModalOpenSupport.openPickerModal(state, ModalWindowManager.ModalType.ICON_PICKER, () -> {
            state.modal.modalChapterTarget = ModalOpenSupport.clean(chapter);
            ModalOpenSupport.resetIconPicker(state);
        });
    }

    public static void openQuestIconPicker(UiState state, String questId) {
        ModalOpenSupport.openPickerModal(state, ModalWindowManager.ModalType.ICON_PICKER, () -> {
            state.modal.modalQuestTarget = ModalOpenSupport.clean(questId);
            ModalOpenSupport.resetIconPicker(state);
        });
    }

    public static void openQuestDetailsGameTexturePicker(UiState state, String target) {
        openGameTexturePickerSession(state, () -> state.questDetails.questDetailsAssetPickTarget = ModalOpenSupport.clean(target));
    }

    public static void openQuestGameTexturePicker(UiState state, String questId, boolean grayscale) {
        openGameTexturePickerSession(state, () -> {
            state.modal.modalQuestBackgroundTarget = ModalOpenSupport.clean(questId);
            state.modal.modalQuestBackgroundGrayscale = grayscale;
        });
    }

    public static void openBatchQuestGameTexturePicker(UiState state, Collection<String> questIds, boolean grayscale) {
        Set<String> targets = ModalOpenSupport.normalizedTargets(questIds);
        openGameTexturePickerSession(state, () -> {
            state.modal.modalQuestBackgroundTargets.addAll(targets);
            state.modal.modalQuestBackgroundGrayscale = grayscale;
        });
    }

    public static void openQuestCompletionHudGameTexturePicker(UiState state, String questId) {
        openGameTexturePickerSession(state, () -> state.modal.modalQuestCompletionHudBackgroundTarget = ModalOpenSupport.clean(questId));
    }

    public static void openBatchQuestCompletionHudGameTexturePicker(UiState state, Collection<String> questIds) {
        Set<String> targets = ModalOpenSupport.normalizedTargets(questIds);
        openGameTexturePickerSession(state, () -> state.modal.modalQuestCompletionHudBackgroundTargets.addAll(targets));
    }

    public static void openChapterGameTexturePicker(UiState state, String chapter) {
        openGameTexturePickerSession(state, () -> state.modal.modalChapterTarget = ModalOpenSupport.clean(chapter));
    }

    public static void openCanvasGameTexturePicker(UiState state, String chapter) {
        openGameTexturePickerSession(state, () -> state.modal.modalCanvasBackgroundTarget = ModalOpenSupport.clean(chapter));
    }

    public static void openExclusiveChoiceGameTexturePicker(UiState state, String chapter, String exclusiveChoiceId) {
        openGameTexturePickerSession(state, () -> state.modal.modalExclusiveChoiceBackgroundTarget = chapter + ":" + exclusiveChoiceId);
    }

    public static void openCanvasImageGameTexturePicker(UiState state, String chapter, int logicalX, int logicalY) {
        openGameTexturePickerSession(state, () -> {
            state.modal.modalCanvasImageTarget = ModalOpenSupport.clean(chapter);
            ModalOpenSupport.setCanvasPickPoint(state, logicalX, logicalY);
        });
    }

    public static void openConnectionGameTexturePicker(UiState state, String chapter, String sourceQuestId, String targetQuestId) {
        openGameTexturePickerSession(state, () -> {
            state.modal.modalConnectionTextureTarget = ModalTargets.connection(chapter, sourceQuestId, targetQuestId);
        });
    }

    public static void openConnectionGameTexturePicker(UiState state, String target) {
        openGameTexturePickerSession(state, () -> state.modal.modalConnectionTextureTarget = target);
    }

    public static void openChapterConnectionGameTexturePicker(UiState state, String chapter, java.util.Collection<String> questIds) {
        openGameTexturePickerSession(state, () -> {
            state.modal.modalConnectionTextureTarget = ModalTargets.of(ModalTargets.CONNECTION, chapter, "", "");
            state.modal.modalConnectionTextureChapterTargets.addAll(questIds);
        });
    }

    static void openGameTexturePickerSession(UiState state, Runnable configure) {
        ModalOpenSupport.openPickerModal(state, ModalWindowManager.ModalType.ICON_PICKER, () -> {
            ModalOpenSupport.resetIconPicker(state);
            ModalIconPickerMode.normalizeForBackground(state);
            configure.run();
            state.modal.modalGameTexturePick = true;
        });
    }

    public static void openCanvasEntityPicker(UiState state, String target, int logicalX, int logicalY) {
        ModalOpenSupport.openPickerModal(state, ModalWindowManager.ModalType.ICON_PICKER, () -> {
            state.modal.modalCanvasEntityTarget = ModalOpenSupport.clean(target);
            ModalOpenSupport.setCanvasPickPoint(state, logicalX, logicalY);
            ModalOpenSupport.resetIconPicker(state);
            ModalIconPickerMode.resetTo(state, ModalIconPickerMode.ENTITIES);
        });
    }

    public static void openCanvasItemPicker(UiState state, String target, int logicalX, int logicalY) {
        ModalOpenSupport.openPickerModal(state, ModalWindowManager.ModalType.ICON_PICKER, () -> {
            state.modal.modalCanvasModelTarget = ModalOpenSupport.clean(target);
            ModalOpenSupport.setCanvasPickPoint(state, logicalX, logicalY);
            ModalOpenSupport.resetIconPicker(state);
            ModalIconPickerMode.resetTo(state, ModalIconPickerMode.ITEMS);
        });
    }
}
