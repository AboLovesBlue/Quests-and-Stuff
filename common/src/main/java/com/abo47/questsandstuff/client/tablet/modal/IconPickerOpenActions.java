package com.abo47.questsandstuff.client.tablet.modal;

import java.util.Collection;
import java.util.Set;

import com.abo47.questsandstuff.client.tablet.modal.target.IconPickerMode;
import com.abo47.questsandstuff.client.tablet.modal.target.ModalTargets;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;

public final class IconPickerOpenActions {
    private IconPickerOpenActions() {
    }

    public static void openQuestDetailsIconPicker(TabletUiState state, String target) {
        PickerOpenSupport.openPickerModal(state, ModalWindowManager.ModalType.ICON_PICKER, () -> {
            state.questDetails.questDetailsPickTarget = PickerOpenSupport.clean(target);
            PickerOpenSupport.resetIconPicker(state);
            IconPickerMode.resetForTarget(state, state.questDetails.questDetailsPickTarget);
        });
    }

    public static void openChapterIconPicker(TabletUiState state, String chapter) {
        PickerOpenSupport.openPickerModal(state, ModalWindowManager.ModalType.ICON_PICKER, () -> {
            state.modal.modalChapterTarget = PickerOpenSupport.clean(chapter);
            PickerOpenSupport.resetIconPicker(state);
        });
    }

    public static void openQuestIconPicker(TabletUiState state, String questId) {
        PickerOpenSupport.openPickerModal(state, ModalWindowManager.ModalType.ICON_PICKER, () -> {
            state.modal.modalQuestTarget = PickerOpenSupport.clean(questId);
            PickerOpenSupport.resetIconPicker(state);
        });
    }

    public static void openQuestDetailsGameTexturePicker(TabletUiState state, String target) {
        openGameTexturePickerSession(state, () -> state.questDetails.questDetailsAssetPickTarget = PickerOpenSupport.clean(target));
    }

    public static void openQuestGameTexturePicker(TabletUiState state, String questId, boolean grayscale) {
        openGameTexturePickerSession(state, () -> {
            state.modal.modalQuestBackgroundTarget = PickerOpenSupport.clean(questId);
            state.modal.modalQuestBackgroundGrayscale = grayscale;
        });
    }

    public static void openBatchQuestGameTexturePicker(TabletUiState state, Collection<String> questIds, boolean grayscale) {
        Set<String> targets = PickerOpenSupport.normalizedTargets(questIds);
        openGameTexturePickerSession(state, () -> {
            state.modal.modalQuestBackgroundTargets.addAll(targets);
            state.modal.modalQuestBackgroundGrayscale = grayscale;
        });
    }

    public static void openQuestCompletionHudGameTexturePicker(TabletUiState state, String questId) {
        openGameTexturePickerSession(state, () -> state.modal.modalQuestCompletionHudBackgroundTarget = PickerOpenSupport.clean(questId));
    }

    public static void openBatchQuestCompletionHudGameTexturePicker(TabletUiState state, Collection<String> questIds) {
        Set<String> targets = PickerOpenSupport.normalizedTargets(questIds);
        openGameTexturePickerSession(state, () -> state.modal.modalQuestCompletionHudBackgroundTargets.addAll(targets));
    }

    public static void openChapterGameTexturePicker(TabletUiState state, String chapter) {
        openGameTexturePickerSession(state, () -> state.modal.modalChapterTarget = PickerOpenSupport.clean(chapter));
    }

    public static void openCanvasGameTexturePicker(TabletUiState state, String chapter) {
        openGameTexturePickerSession(state, () -> state.modal.modalCanvasBackgroundTarget = PickerOpenSupport.clean(chapter));
    }

    public static void openEcGameTexturePicker(TabletUiState state, String chapter, String ecId) {
        openGameTexturePickerSession(state, () -> state.modal.modalEcBackgroundTarget = chapter + ":" + ecId);
    }

    public static void openCanvasImageGameTexturePicker(TabletUiState state, String chapter, int logicalX, int logicalY) {
        openGameTexturePickerSession(state, () -> {
            state.modal.modalCanvasImageTarget = PickerOpenSupport.clean(chapter);
            PickerOpenSupport.setCanvasPickPoint(state, logicalX, logicalY);
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
        PickerOpenSupport.openPickerModal(state, ModalWindowManager.ModalType.ICON_PICKER, () -> {
            PickerOpenSupport.resetIconPicker(state);
            IconPickerMode.normalizeForBackground(state);
            configure.run();
            state.modal.modalGameTexturePick = true;
        });
    }

    public static void openCanvasEntityPicker(TabletUiState state, String target, int logicalX, int logicalY) {
        PickerOpenSupport.openPickerModal(state, ModalWindowManager.ModalType.ICON_PICKER, () -> {
            state.modal.modalCanvasEntityTarget = PickerOpenSupport.clean(target);
            PickerOpenSupport.setCanvasPickPoint(state, logicalX, logicalY);
            PickerOpenSupport.resetIconPicker(state);
            IconPickerMode.resetTo(state, IconPickerMode.ENTITIES);
        });
    }

    public static void openCanvasItemPicker(TabletUiState state, String target, int logicalX, int logicalY) {
        PickerOpenSupport.openPickerModal(state, ModalWindowManager.ModalType.ICON_PICKER, () -> {
            state.modal.modalCanvasModelTarget = PickerOpenSupport.clean(target);
            PickerOpenSupport.setCanvasPickPoint(state, logicalX, logicalY);
            PickerOpenSupport.resetIconPicker(state);
            IconPickerMode.resetTo(state, IconPickerMode.ITEMS);
        });
    }
}
