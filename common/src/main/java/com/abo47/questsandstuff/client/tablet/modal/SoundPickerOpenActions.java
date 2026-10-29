package com.abo47.questsandstuff.client.tablet.modal;

import java.util.Collection;
import java.util.Set;

import com.abo47.questsandstuff.client.quest.sound.QuestCompletionSoundPlayer;
import com.abo47.questsandstuff.client.sync.state.ClientQuestStateFacade;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuController;
import com.abo47.questsandstuff.client.tablet.modal.target.ModalPickerStates;
import com.abo47.questsandstuff.client.tablet.state.UiState;
import com.abo47.questsandstuff.quest.model.QuestDisplay;

public final class SoundPickerOpenActions {
    private SoundPickerOpenActions() {
    }

    public static void openQuestCompletionSoundPicker(UiState state, String questId, String currentSound) {
        openQuestCustomCompletionSoundPicker(state, questId, currentSound);
    }

    public static void openQuestGameSoundPicker(UiState state, String questId, String currentSound) {
        openSoundPickerSession(state, currentSound, questId, () -> state.modal.modalQuestCompletionSoundTarget = ModalOpenSupport.clean(questId));
    }

    public static void openQuestCustomCompletionSoundPicker(UiState state, String questId, String currentSound) {
        openAssetSoundPickerSession(state, currentSound, questId, () -> state.modal.modalQuestCompletionSoundTarget = ModalOpenSupport.clean(questId));
    }

    public static void openBatchQuestGameSoundPicker(UiState state, Collection<String> questIds, String currentSound) {
        Set<String> targets = ModalOpenSupport.normalizedTargets(questIds);
        openSoundPickerSession(state, currentSound, ModalOpenSupport.firstTarget(targets), () -> state.modal.modalQuestCompletionSoundTargets.addAll(targets));
    }

    public static void openBatchQuestCustomCompletionSoundPicker(UiState state, Collection<String> questIds, String currentSound) {
        Set<String> targets = ModalOpenSupport.normalizedTargets(questIds);
        openAssetSoundPickerSession(state, currentSound, ModalOpenSupport.firstTarget(targets), () -> state.modal.modalQuestCompletionSoundTargets.addAll(targets));
    }

    static void resetSoundPicker(UiState state) {
        ModalPickerStates.sound(state).reset();
    }

    static int completionSoundVolume(String questId) {
        return QuestDisplay.normalizeCompletionSoundVolume(ClientQuestStateFacade.quest(questId));
    }

    static void openSoundPickerSession(UiState state, String currentSound, String volumeQuestId, Runnable configureTargets) {
        ModalOpenSupport.openPickerModal(state, ModalWindowManager.ModalType.SOUND_PICKER, () -> {
            resetSoundPicker(state);
            configureTargets.run();
            ContextMenuController.clearDeleteConfirm(state);
            state.pickers.soundVolumeDraft = completionSoundVolume(volumeQuestId);
            state.pickers.soundVolumeDragging = false;
            state.pickers.soundSelected = ModalOpenSupport.clean(currentSound).isBlank() || QuestCompletionSoundPlayer.isAssetSoundId(currentSound) ? "" : currentSound;
        });
    }

    static void openAssetSoundPickerSession(UiState state, String currentSound, String volumeQuestId, Runnable configureTargets) {
        AssetPickerOpenActions.openAssetPickerSession(state, ModalOpenSupport.clean(currentSound).isBlank() || !QuestCompletionSoundPlayer.isAssetSoundId(currentSound) ? "" : currentSound, () -> {
            configureTargets.run();
            ContextMenuController.clearDeleteConfirm(state);
            state.pickers.soundVolumeDraft = completionSoundVolume(volumeQuestId);
            state.pickers.soundVolumeDragging = false;
            state.pickers.assetBrowseDir = "sounds";
        });
    }
}
