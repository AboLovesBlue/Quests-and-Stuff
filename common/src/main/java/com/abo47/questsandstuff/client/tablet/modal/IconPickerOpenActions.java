package com.abo47.questsandstuff.client.tablet.modal;

import java.util.Collection;
import java.util.Set;

import com.abo47.questsandstuff.client.tablet.modal.target.ModalIconPickerMode;
import com.abo47.questsandstuff.client.tablet.modal.target.ModalTargets;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;

public final class IconPickerOpenActions {
    private IconPickerOpenActions() {
    }

    public static void openQuestDetailsIconPicker(TabletUiState state, String target) {
        ModalOpenSupport.openPickerModal(state, ModalWindowManager.ModalType.ICON_PICKER, () -> {
            state.questDetails.questDetailsPickTarget = ModalOpenSupport.clean(target);
            ModalOpenSupport.resetIconPicker(state);
            ModalIconPickerMode.resetForTarget(state, state.questDetails.questDetailsPickTarget);
        });
    }

    public static void openChapterIconPicker(TabletUiState state, String chapter) {
        ModalOpenSupport.openPickerModal(state, ModalWindowManager.ModalType.ICON_PICKER, () -> {
            state.modal.modalChapterTarget = ModalOpenSupport.clean(chapter);
            ModalOpenSupport.resetIconPicker(state);
        });
    }

    public static void openQuestIconPicker(TabletUiState state, String questId) {
        ModalOpenSupport.openPickerModal(state, ModalWindowManager.ModalType.ICON_PICKER, () -> {
            state.modal.modalQuestTarget = ModalOpenSupport.clean(questId);
            ModalOpenSupport.resetIconPicker(state);
        });
    }

    public static void openQuestDetailsGameTexturePicker(TabletUiState state, String target) {
        openGameTexturePickerSession(state, () -> state.questDetails.questDetailsAssetPickTarget = ModalOpenSupport.clean(target));
    }

    public static void openQuestGameTexturePicker(TabletUiState state, String questId, boolean grayscale) {
        openGameTexturePickerSession(state, () -> {
            state.modal.modalQuestBackgroundTarget = ModalOpenSupport.clean(questId);
            state.modal.modalQuestBackgroundGrayscale = grayscale;
        });
    }

    public static void openBatchQuestGameTexturePicker(TabletUiState state, Collection<String> questIds, boolean grayscale) {
        Set<String> targets = ModalOpenSupport.normalizedTargets(questIds);
        openGameTexturePickerSession(state, () -> {
            state.modal.modalQuestBackgroundTargets.addAll(targets);
            state.modal.modalQuestBackgroundGrayscale = grayscale;
        });
    }

    public static void openQuestCompletionHudGameTexturePicker(TabletUiState state, String questId) {
        openGameTexturePickerSession(state, () -> state.modal.modalQuestCompletionHudBackgroundTarget = ModalOpenSupport.clean(questId));
    }

    public static void openBatchQuestCompletionHudGameTexturePicker(TabletUiState state, Collection<String> questIds) {
        Set<String> targets = ModalOpenSupport.normalizedTargets(questIds);
        openGameTexturePickerSession(state, () -> state.modal.modalQuestCompletionHudBackgroundTargets.addAll(targets));
    }

    public static void openChapterGameTexturePicker(TabletUiState state, String chapter) {
        openGameTexturePickerSession(state, () -> state.modal.modalChapterTarget = ModalOpenSupport.clean(chapter));
    }

    public static void openCanvasGameTexturePicker(TabletUiState state, String chapter) {
        openGameTexturePickerSession(state, () -> state.modal.modalCanvasBackgroundTarget = ModalOpenSupport.clean(chapter));
    }

    public static void openExclusiveChoiceGameTexturePicker(TabletUiState state, String chapter, String exclusiveChoiceId) {
        openGameTexturePickerSession(state, () -> state.modal.modalExclusiveChoiceBackgroundTarget = chapter + ":" + exclusiveChoiceId);
    }

    public static void openCanvasImageGameTexturePicker(TabletUiState state, String chapter, int logicalX, int logicalY) {
        openGameTexturePickerSession(state, () -> {
            state.modal.modalCanvasImageTarget = ModalOpenSupport.clean(chapter);
            ModalOpenSupport.setCanvasPickPoint(state, logicalX, logicalY);
        });
    }

    public static void openConnectionGameTexturePicker(TabletUiState state, String chapter, String sourceQuestId, String targetQuestId) {
        openGameTexturePickerSession(state, () -> {
            state.modal.modalConnectionTextureTarget = ModalTargets.connection(chapter, sourceQuestId, targetQuestId);
        });
    }

    public static void openConnectionGameTexturePicker(TabletUiState state, String target) {
        openGameTexturePickerSession(state, () -> state.modal.modalConnectionTextureTarget = target);
    }

    public static void openChapterConnectionGameTexturePicker(TabletUiState state, String chapter, java.util.Collection<String> questIds) {
        openGameTexturePickerSession(state, () -> {
            state.modal.modalConnectionTextureTarget = ModalTargets.of(ModalTargets.CONNECTION, chapter, "", "");
            state.modal.modalConnectionTextureChapterTargets.addAll(questIds);
        });
    }

    static void openGameTexturePickerSession(TabletUiState state, Runnable configure) {
        ModalOpenSupport.openPickerModal(state, ModalWindowManager.ModalType.ICON_PICKER, () -> {
            ModalOpenSupport.resetIconPicker(state);
            ModalIconPickerMode.normalizeForBackground(state);
            configure.run();
            state.modal.modalGameTexturePick = true;
        });
    }

    public static void openCanvasEntityPicker(TabletUiState state, String target, int logicalX, int logicalY) {
        ModalOpenSupport.openPickerModal(state, ModalWindowManager.ModalType.ICON_PICKER, () -> {
            state.modal.modalCanvasEntityTarget = ModalOpenSupport.clean(target);
            ModalOpenSupport.setCanvasPickPoint(state, logicalX, logicalY);
            ModalOpenSupport.resetIconPicker(state);
            ModalIconPickerMode.resetTo(state, ModalIconPickerMode.ENTITIES);
        });
    }

    public static void openCanvasItemPicker(TabletUiState state, String target, int logicalX, int logicalY) {
        ModalOpenSupport.openPickerModal(state, ModalWindowManager.ModalType.ICON_PICKER, () -> {
            state.modal.modalCanvasModelTarget = ModalOpenSupport.clean(target);
            ModalOpenSupport.setCanvasPickPoint(state, logicalX, logicalY);
            ModalOpenSupport.resetIconPicker(state);
            ModalIconPickerMode.resetTo(state, ModalIconPickerMode.ITEMS);
        });
    }
}
