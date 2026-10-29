package com.abo47.questsandstuff.client.tablet.modal;

import java.util.Collection;
import java.util.Set;

import com.abo47.questsandstuff.client.quest.sound.QuestCompletionSoundPlayer;
import com.abo47.questsandstuff.client.sync.state.ClientQuestStateFacade;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuController;
import com.abo47.questsandstuff.client.tablet.modal.target.ModalPickerStates;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.quest.model.QuestDisplay;

public final class SoundPickerOpenActions {
    private SoundPickerOpenActions() {
    }

    public static void openQuestCompletionSoundPicker(TabletUiState state, String questId, String currentSound) {
        openQuestCustomCompletionSoundPicker(state, questId, currentSound);
    }

    public static void openQuestGameSoundPicker(TabletUiState state, String questId, String currentSound) {
        openSoundPickerSession(state, currentSound, questId, () -> state.modal.modalQuestCompletionSoundTarget = ModalOpenSupport.clean(questId));
    }

    public static void openQuestCustomCompletionSoundPicker(TabletUiState state, String questId, String currentSound) {
        openAssetSoundPickerSession(state, currentSound, questId, () -> state.modal.modalQuestCompletionSoundTarget = ModalOpenSupport.clean(questId));
    }

    public static void openBatchQuestGameSoundPicker(TabletUiState state, Collection<String> questIds, String currentSound) {
        Set<String> targets = ModalOpenSupport.normalizedTargets(questIds);
        openSoundPickerSession(state, currentSound, ModalOpenSupport.firstTarget(targets), () -> state.modal.modalQuestCompletionSoundTargets.addAll(targets));
    }

    public static void openBatchQuestCustomCompletionSoundPicker(TabletUiState state, Collection<String> questIds, String currentSound) {
        Set<String> targets = ModalOpenSupport.normalizedTargets(questIds);
        openAssetSoundPickerSession(state, currentSound, ModalOpenSupport.firstTarget(targets), () -> state.modal.modalQuestCompletionSoundTargets.addAll(targets));
    }

    static void resetSoundPicker(TabletUiState state) {
        ModalPickerStates.sound(state).reset();
    }

    static int completionSoundVolume(String questId) {
        return QuestDisplay.normalizeCompletionSoundVolume(ClientQuestStateFacade.quest(questId));
    }

    static void openSoundPickerSession(TabletUiState state, String currentSound, String volumeQuestId, Runnable configureTargets) {
        ModalOpenSupport.openPickerModal(state, ModalWindowManager.ModalType.SOUND_PICKER, () -> {
            resetSoundPicker(state);
            configureTargets.run();
            ContextMenuController.clearDeleteConfirm(state);
            state.pickers.soundVolumeDraft = completionSoundVolume(volumeQuestId);
            state.pickers.soundVolumeDragging = false;
            state.pickers.soundSelected = ModalOpenSupport.clean(currentSound).isBlank() || QuestCompletionSoundPlayer.isAssetSoundId(currentSound) ? "" : currentSound;
        });
    }

    static void openAssetSoundPickerSession(TabletUiState state, String currentSound, String volumeQuestId, Runnable configureTargets) {
        AssetPickerOpenActions.openAssetPickerSession(state, ModalOpenSupport.clean(currentSound).isBlank() || !QuestCompletionSoundPlayer.isAssetSoundId(currentSound) ? "" : currentSound, () -> {
            configureTargets.run();
            ContextMenuController.clearDeleteConfirm(state);
            state.pickers.soundVolumeDraft = completionSoundVolume(volumeQuestId);
            state.pickers.soundVolumeDragging = false;
            state.pickers.assetBrowseDir = "sounds";
        });
    }
}
