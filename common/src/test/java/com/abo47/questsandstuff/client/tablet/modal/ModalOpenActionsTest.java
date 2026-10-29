package com.abo47.questsandstuff.client.tablet.modal;

import org.junit.jupiter.api.Test;

import com.abo47.questsandstuff.client.tablet.quest.canvas.model.CanvasPoint;
import com.abo47.questsandstuff.client.tablet.state.UiState;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.abo47.questsandstuff.client.tablet.modal.recipe.RecipePickerMode;
import com.abo47.questsandstuff.client.tablet.modal.target.ModalTargets;
import com.abo47.questsandstuff.client.tablet.modal.target.ModalIconPickerMode;

class ModalOpenActionsTest {
    @Test
    void assetPickerOpenPreservesSelectedValueAndResetsSearchSession() {
        UiState state = new UiState();
        state.pickers.assetSearch = "old query";
        state.pickers.assetSearchFocused = true;
        state.pickers.assetGridScroll = 32;
        state.pickers.assetGridScrollDragging = true;
        state.pickers.assetSelected = "old.png";
        state.pickers.assetContextOpen = true;
        state.pickers.assetRenameOpen = true;

        AssetPickerOpenActions.openAssetPicker(state, "reward_icon|quest|reward|icon", "icons/new.png");

        assertTrue(ModalStateQueries.isOpen(state, ModalWindowManager.ModalType.ASSET_PICKER));
        assertEquals(ModalWindowManager.ModalType.ASSET_PICKER, state.modal.modalSession.type());
        assertEquals("reward_icon|quest|reward|icon", state.questDetails.questDetailsAssetPickTarget);
        assertEquals("reward_icon|quest|reward|icon", state.modal.modalSession.target(ModalSession.TargetSlot.QUEST_DETAILS_ASSET_PICK));
        assertEquals("icons/new.png", state.pickers.assetSelected);
        assertEquals("icons/new.png", state.modal.modalSession.selectedValue());
        assertEquals("", state.pickers.assetSearch);
        assertEquals("", state.modal.modalSession.picker().search());
        assertFalse(state.pickers.assetSearchFocused);
        assertEquals(0, state.pickers.assetGridScroll);
        assertEquals(0, state.modal.modalSession.picker().scroll());
        assertFalse(state.pickers.assetGridScrollDragging);
        assertFalse(state.pickers.assetContextOpen);
        assertFalse(state.pickers.assetRenameOpen);
    }

    @Test
    void recipePickerOpenAssignsTargetCoordinatesAndResetsModeState() {
        UiState state = new UiState();
        state.pickers.recipeSearch = "stone";
        state.pickers.recipeSearchFocused = true;
        state.pickers.recipeScroll = 44;
        state.pickers.recipeScrollDragging = true;
        state.pickers.recipeMode = RecipePickerMode.INVENTORY;

        RegistryPickerOpenActions.openCanvasRecipePicker(state, "task_recipe|quest|task|questsandstuff:recipe", 12, 34);

        assertTrue(ModalStateQueries.isOpen(state, ModalWindowManager.ModalType.RECIPE_PICKER));
        assertEquals(ModalWindowManager.ModalType.RECIPE_PICKER, state.modal.modalSession.type());
        assertEquals("task_recipe|quest|task|questsandstuff:recipe", state.questDetails.questDetailsPickTarget);
        assertEquals("task_recipe|quest|task|questsandstuff:recipe", state.modal.modalSession.target(ModalSession.TargetSlot.QUEST_DETAILS_PICK));
        assertEquals(12, state.canvas.canvasImageLogicalX);
        assertEquals(34, state.canvas.canvasImageLogicalY);
        assertEquals("", state.pickers.recipeSearch);
        assertEquals("", state.modal.modalSession.picker().search());
        assertFalse(state.pickers.recipeSearchFocused);
        assertEquals(0, state.pickers.recipeScroll);
        assertEquals(0, state.modal.modalSession.picker().scroll());
        assertFalse(state.pickers.recipeScrollDragging);
        assertEquals(RecipePickerMode.ITEMS, state.pickers.recipeMode);
        assertEquals(RecipePickerMode.ITEMS.name(), state.modal.modalSession.mode());
    }

    @Test
    void questDetailsIconPickerUsesUsableItemsModeForItemUseTargets() {
        UiState state = new UiState();
        state.pickers.iconSearch = "old";
        state.pickers.iconSearchFocused = true;
        state.pickers.iconScroll = 12;
        state.pickers.iconScrollDragging = true;
        state.pickers.iconMode = ModalIconPickerMode.INVENTORY;

        IconPickerOpenActions.openQuestDetailsIconPicker(state, ModalTargets.taskSimpleIcon("quest", "task", "questsandstuff:item_use"));

        assertTrue(ModalStateQueries.isOpen(state, ModalWindowManager.ModalType.ICON_PICKER));
        assertEquals(ModalWindowManager.ModalType.ICON_PICKER, state.modal.modalSession.type());
        assertEquals(ModalTargets.taskSimpleIcon("quest", "task", "questsandstuff:item_use"), state.questDetails.questDetailsPickTarget);
        assertEquals(ModalTargets.taskSimpleIcon("quest", "task", "questsandstuff:item_use"), state.modal.modalSession.target(ModalSession.TargetSlot.QUEST_DETAILS_PICK));
        assertEquals("", state.pickers.iconSearch);
        assertFalse(state.pickers.iconSearchFocused);
        assertEquals(0, state.pickers.iconScroll);
        assertFalse(state.pickers.iconScrollDragging);
        assertEquals(ModalIconPickerMode.USABLE_ITEMS, state.pickers.iconMode);
        assertEquals(ModalIconPickerMode.USABLE_ITEMS.name(), state.modal.modalSession.mode());
    }

    @Test
    void canvasEntityIconPickerStartsInEntityMode() {
        UiState state = new UiState();
        state.pickers.iconMode = ModalIconPickerMode.FLUIDS;

        IconPickerOpenActions.openCanvasEntityPicker(state, ModalTargets.canvasEntityNew("chapter"), 5, 9);

        assertTrue(ModalStateQueries.isOpen(state, ModalWindowManager.ModalType.ICON_PICKER));
        assertEquals(ModalWindowManager.ModalType.ICON_PICKER, state.modal.modalSession.type());
        assertEquals(ModalTargets.canvasEntityNew("chapter"), state.modal.modalCanvasEntityTarget);
        assertEquals(ModalTargets.canvasEntityNew("chapter"), state.modal.modalSession.target(ModalSession.TargetSlot.CANVAS_ENTITY));
        assertEquals(5, state.canvas.canvasImageLogicalX);
        assertEquals(9, state.canvas.canvasImageLogicalY);
        assertEquals(ModalIconPickerMode.ENTITIES, state.pickers.iconMode);
    }

    @Test
    void questBackgroundPickerClearsStaleTargetsAndCapturesSessionPayload() {
        UiState state = new UiState();
        state.questDetails.questDetailsAssetPickTarget = "old_details";
        state.modal.modalCanvasImageTarget = "old_image";
        state.modal.modalQuestCompletionSoundTargets.add("old_sound");

        AssetPickerOpenActions.openQuestBackgroundPicker(state, "quest_a", "backgrounds/stone.png", true);

        assertTrue(ModalStateQueries.isOpen(state, ModalWindowManager.ModalType.ASSET_PICKER));
        assertEquals("quest_a", state.modal.modalQuestBackgroundTarget);
        assertEquals("quest_a", state.modal.modalSession.target(ModalSession.TargetSlot.QUEST_BACKGROUND));
        assertEquals("backgrounds/stone.png", state.modal.modalSession.selectedValue());
        assertTrue(state.modal.modalQuestBackgroundGrayscale);
        assertEquals("", state.questDetails.questDetailsAssetPickTarget);
        assertEquals("", state.modal.modalCanvasImageTarget);
        assertTrue(state.modal.modalQuestCompletionSoundTargets.isEmpty());
    }

    @Test
    void gameSoundPickerClearsAssetTargetsAndCapturesSessionPayload() {
        UiState state = new UiState();
        state.questDetails.questDetailsAssetPickTarget = "old_asset";
        state.modal.modalCanvasBackgroundTarget = "old_canvas";
        state.pickers.assetSelected = "sounds/old.ogg";

        SoundPickerOpenActions.openQuestGameSoundPicker(state, "quest_a", "minecraft:block.note_block.pling");

        assertTrue(ModalStateQueries.isOpen(state, ModalWindowManager.ModalType.SOUND_PICKER));
        assertEquals("quest_a", state.modal.modalQuestCompletionSoundTarget);
        assertEquals("quest_a", state.modal.modalSession.target(ModalSession.TargetSlot.QUEST_COMPLETION_SOUND));
        assertEquals("minecraft:block.note_block.pling", state.pickers.soundSelected);
        assertEquals("minecraft:block.note_block.pling", state.modal.modalSession.selectedValue());
        assertEquals("", state.questDetails.questDetailsAssetPickTarget);
        assertEquals("", state.modal.modalCanvasBackgroundTarget);
    }

    @Test
    void openingModalClearsActiveTransformSessions() {
        UiState state = new UiState();
        state.canvas.draggingCanvasImage = true;
        state.canvas.transientCanvasImages.put("image:a", image("image:a"));
        state.canvas.transientQuestPositions.put("quest/a", new CanvasPoint(10, 20));
        state.questDetails.questDetailsTransformKind = "desc_image";
        state.questDetails.questDetailsTransformId = "image:b";
        state.questDetails.questDetailsTransientImages.put("image:b", image("image:b"));
        state.canvas.snapGuideXVisible = true;
        state.canvas.snapGuideYVisible = true;

        AppOpenActions.openColorPicker(state, "theme", 0xFF00AA);

        assertTrue(ModalStateQueries.isOpen(state, ModalWindowManager.ModalType.COLOR_PICKER));
        assertFalse(state.canvas.draggingCanvasImage);
        assertTrue(state.canvas.transientCanvasImages.isEmpty());
        assertTrue(state.canvas.transientQuestPositions.isEmpty());
        assertTrue(state.questDetails.questDetailsTransformKind.isBlank());
        assertTrue(state.questDetails.questDetailsTransformId.isBlank());
        assertTrue(state.questDetails.questDetailsTransientImages.isEmpty());
        assertFalse(state.canvas.snapGuideXVisible);
        assertFalse(state.canvas.snapGuideYVisible);
    }

    private static CanvasImageLayer image(String id) {
        return new CanvasImageLayer(id, "item:minecraft:diamond", 10, 20, 40, 50, 0);
    }
}
