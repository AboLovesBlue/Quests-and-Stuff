package com.abo47.questsandstuff.client.tablet.quest.canvas.contextmenu;

import com.abo47.questsandstuff.client.tablet.quest.canvas.render.gizmo.CanvasTransformGizmoAxis;
import com.abo47.questsandstuff.client.tablet.modal.AssetPickerOpenActions;
import com.abo47.questsandstuff.client.tablet.modal.AppOpenActions;
import com.abo47.questsandstuff.client.tablet.modal.RegistryPickerOpenActions;
import com.abo47.questsandstuff.client.tablet.modal.IconPickerOpenActions;
import java.util.List;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextAction;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextActionFactory;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuController;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuSection;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuSections;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuTarget;
import com.abo47.questsandstuff.client.tablet.controls.EntityIconControls;
import com.abo47.questsandstuff.client.tablet.entity.EntityPreviewRenderer;
import com.abo47.questsandstuff.client.tablet.entity.motion.EntityMotionEditor;
import com.abo47.questsandstuff.client.tablet.modal.target.ModalTargets;
import com.abo47.questsandstuff.client.tablet.preview.ModelAssetPreviewRenderer;
import com.abo47.questsandstuff.client.tablet.quest.canvas.geometry.CanvasGridFitController;
import com.abo47.questsandstuff.client.tablet.quest.canvas.layer.CanvasLayerMutations;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasViewport;
import com.abo47.questsandstuff.client.tablet.quest.canvas.recipe.CanvasRecipeCardAsset;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.geometry.CanvasLayerOrdering;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.gizmo.CanvasTransformGizmo;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.gizmo.CanvasTransformGizmoMenus;
import com.abo47.questsandstuff.client.tablet.quest.canvas.selection.CanvasSelectionActions;
import com.abo47.questsandstuff.client.tablet.quest.canvas.text.CanvasTextEditSession;
import com.abo47.questsandstuff.client.tablet.quest.canvas.text.CanvasTextStyleSession;
import com.abo47.questsandstuff.client.tablet.state.UiState;
import com.abo47.questsandstuff.client.tablet.text.QuestTranslationKeys;
import com.abo47.questsandstuff.client.tablet.text.TranslationKeys;
import com.abo47.questsandstuff.client.tablet.theme.tokens.ThemeColors;
import com.abo47.questsandstuff.quest.model.canvas.CanvasExclusiveChoice;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;

final class CanvasContextElementActions {
    private CanvasContextElementActions() {
    }

    static void addImageActions(ContextMenuSections sections, CanvasViewport canvasViewport, UiState state, String selectedChapter) {
        if (state.contextMenu.contextMenuTarget != ContextMenuTarget.IMAGE || state.contextMenu.contextCanvasImageId.isBlank()) {
            return;
        }
        CanvasImageLayer contextImage = CanvasLayerMutations.findCanvasImage(state, selectedChapter, state.contextMenu.contextCanvasImageId);
        if (contextImage != null && CanvasRecipeCardAsset.isRecipeCardAsset(contextImage.asset())) {
            sections.add(ContextMenuSection.PRIMARY, ContextActionFactory.promoted(CanvasContextMenuController.tr("ui.questsandstuff.context.change_recipe_card"), "recipe", ThemeColors.INTERACTIVE, () -> {
                RegistryPickerOpenActions.openCanvasRecipePicker(state, ModalTargets.canvasRecipeChange(selectedChapter, state.contextMenu.contextCanvasImageId), state.canvas.canvasImageLogicalX, state.canvas.canvasImageLogicalY);
                ContextMenuController.clearDeleteConfirm(state);
                ContextMenuController.close(state);
                QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=change_recipe_card chapter={} image={}", selectedChapter, state.contextMenu.contextCanvasImageId);
                canvasViewport.refresh();
            }));
        } else if (contextImage != null && EntityPreviewRenderer.isEntityAsset(contextImage.asset())) {
            sections.add(ContextMenuSection.PRIMARY, ContextActionFactory.promoted(CanvasContextMenuController.tr("ui.questsandstuff.context.change_entity"), "entity", ThemeColors.INTERACTIVE, () -> {
                IconPickerOpenActions.openCanvasEntityPicker(state, ModalTargets.canvasEntityChange(selectedChapter, state.contextMenu.contextCanvasImageId), state.canvas.canvasImageLogicalX, state.canvas.canvasImageLogicalY);
                ContextMenuController.clearDeleteConfirm(state);
                ContextMenuController.close(state);
                QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=change_entity chapter={} image={}", selectedChapter, state.contextMenu.contextCanvasImageId);
                canvasViewport.refresh();
            }));
            EntityIconControls.addEntityVariantAndMotionActions(
                    sections,
                    ContextMenuSection.APPEARANCE,
                    state,
                    contextImage.asset(),
                    ModalTargets.canvasImage(selectedChapter, state.contextMenu.contextCanvasImageId),
                    () -> ContextMenuController.close(state),
                    () -> {
                        state.questDetails.entityMotionEditorBatchImageIds = "";
                        EntityMotionEditor.openMainCanvas(state, selectedChapter, state.contextMenu.contextCanvasImageId, state.contextMenu.contextMenuX, state.contextMenu.contextMenuY);
                        QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=edit_entity_motion chapter={} image={}", selectedChapter, state.contextMenu.contextCanvasImageId);
                    },
                    canvasViewport::refresh
            );
        } else if (contextImage != null && (ModelAssetPreviewRenderer.isItemAsset(contextImage.asset()) || ModelAssetPreviewRenderer.isItemTagAsset(contextImage.asset()))) {
            sections.add(ContextMenuSection.PRIMARY, ContextActionFactory.promoted(CanvasContextMenuController.tr("ui.questsandstuff.context.change_item"), "icon", ThemeColors.INTERACTIVE, () -> {
                IconPickerOpenActions.openCanvasItemPicker(state, ModalTargets.canvasItemChange(selectedChapter, state.contextMenu.contextCanvasImageId), state.canvas.canvasImageLogicalX, state.canvas.canvasImageLogicalY);
                ContextMenuController.clearDeleteConfirm(state);
                ContextMenuController.close(state);
                QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=change_item_model chapter={} image={}", selectedChapter, state.contextMenu.contextCanvasImageId);
                canvasViewport.refresh();
            }));
        } else if (contextImage != null && ModelAssetPreviewRenderer.isBlockModelAsset(contextImage.asset())) {
            sections.add(ContextMenuSection.PRIMARY, ContextActionFactory.promoted(CanvasContextMenuController.tr("ui.questsandstuff.context.change_block"), "box", ThemeColors.INTERACTIVE, () -> {
                RegistryPickerOpenActions.openCanvasBlockPicker(state, ModalTargets.canvasBlockChange(selectedChapter, state.contextMenu.contextCanvasImageId), state.canvas.canvasImageLogicalX, state.canvas.canvasImageLogicalY);
                ContextMenuController.clearDeleteConfirm(state);
                ContextMenuController.close(state);
                QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=change_block_model chapter={} image={}", selectedChapter, state.contextMenu.contextCanvasImageId);
                canvasViewport.refresh();
            }));
        }
        if (contextImage != null && CanvasTransformGizmoAxis.supports(contextImage.asset())
                && CanvasSelectionActions.totalCanvasSelectionCount(state) == 1
                && CanvasSelectionActions.isImageSelected(state, contextImage.id())) {
            CanvasTransformGizmoMenus.addModeActions(sections, ContextMenuSection.ARRANGE, state, canvasViewport::refresh);
            CanvasTransformGizmoMenus.addCenterPivotAction(sections, ContextMenuSection.ARRANGE, state, () -> {
                CanvasImageLayer image = CanvasLayerMutations.findCanvasImage(state, selectedChapter, state.contextMenu.contextCanvasImageId);
                if (image == null) {
                    return;
                }
                CanvasLayerMutations.putCanvasImage(state, selectedChapter, image.withCenteredPivot());
                state.canvas.canvasSelection.setPrimaryImageId(image.id());
                state.canvas.canvasSelection.imageIds().clear();
                state.canvas.canvasSelection.imageIds().add(image.id());
                state.canvas.canvasSelection.setPrimaryTextId("");
                state.canvas.canvasSelection.textIds().clear();
            }, canvasViewport::refresh);
        }
        if (CanvasGridFitController.canFitImageToGrid(state, selectedChapter, state.contextMenu.contextCanvasImageId)) {
            sections.add(ContextMenuSection.ARRANGE, new ContextAction(CanvasContextMenuController.tr("ui.questsandstuff.context.fit_to_grid"), "fit_grid", ThemeColors.INTERACTIVE, () -> {
                boolean changed = CanvasGridFitController.fitImageToGrid(state, selectedChapter, state.contextMenu.contextCanvasImageId);
                ContextMenuController.clearDeleteConfirm(state);
                QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=fit_to_grid target=image id={} changed={}", state.contextMenu.contextCanvasImageId, changed);
                canvasViewport.refresh();
            }));
        }
        addLayerActions(sections, canvasViewport, state, selectedChapter, CanvasLayerOrdering.imageKey(state.contextMenu.contextCanvasImageId), "image", state.contextMenu.contextCanvasImageId);
        addCopyAndDeleteActions(sections, canvasViewport, state);
    }

    static void addTextActions(ContextMenuSections sections, CanvasViewport canvasViewport, UiState state, String selectedChapter) {
        if (state.contextMenu.contextMenuTarget != ContextMenuTarget.TEXT || state.contextMenu.contextCanvasTextId.isBlank()) {
            return;
        }
        sections.add(ContextMenuSection.PRIMARY, ContextActionFactory.promoted(CanvasContextMenuController.tr("ui.questsandstuff.context.edit_text"), "rename", ThemeColors.INTERACTIVE, () -> {
            CanvasTextLayer text = CanvasLayerMutations.findCanvasText(state, selectedChapter, state.contextMenu.contextCanvasTextId);
            CanvasTextEditSession.beginMainCanvas(state, state.contextMenu.contextCanvasTextId, text == null ? "" : text.text());
            state.canvas.canvasSelection.setPrimaryTextId(state.contextMenu.contextCanvasTextId);
            canvasViewport.setFocus(true);
            ContextMenuController.clearDeleteConfirm(state);
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=edit_text id={}", state.contextMenu.contextCanvasTextId);
            canvasViewport.refresh();
        }));
        sections.add(ContextMenuSection.APPEARANCE, new ContextAction(CanvasContextMenuController.tr("ui.questsandstuff.menu.text_style"), "style", ThemeColors.INTERACTIVE, false, () -> {
            CanvasTextStyleSession.openMainCanvas(state, state.contextMenu.contextCanvasTextId);
            state.canvas.canvasSelection.setPrimaryTextId(state.contextMenu.contextCanvasTextId);
            ContextMenuController.clearDeleteConfirm(state);
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=text_style id={}", state.contextMenu.contextCanvasTextId);
            canvasViewport.refresh();
        }));
        if (CanvasGridFitController.canFitTextToGrid(state, selectedChapter, state.contextMenu.contextCanvasTextId)) {
            sections.add(ContextMenuSection.ARRANGE, new ContextAction(CanvasContextMenuController.tr("ui.questsandstuff.context.fit_to_grid"), "fit_grid", ThemeColors.INTERACTIVE, () -> {
                boolean changed = CanvasGridFitController.fitTextToGrid(state, selectedChapter, state.contextMenu.contextCanvasTextId);
                ContextMenuController.clearDeleteConfirm(state);
                QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=fit_to_grid target=text id={} changed={}", state.contextMenu.contextCanvasTextId, changed);
                canvasViewport.refresh();
            }));
        }
        addLayerActions(sections, canvasViewport, state, selectedChapter, CanvasLayerOrdering.textKey(state.contextMenu.contextCanvasTextId), "text", state.contextMenu.contextCanvasTextId);
        addCopyAndDeleteActions(sections, canvasViewport, state);
    }

    static void addExclusiveChoiceActions(ContextMenuSections sections, CanvasViewport canvasViewport, UiState state, String selectedChapter) {
        if (state.contextMenu.contextMenuTarget != ContextMenuTarget.EXCLUSIVE_CHOICE || state.contextMenu.contextCanvasExclusiveChoiceId.isBlank()) {
            return;
        }
        addExclusiveChoiceConnectedQuestActions(sections, canvasViewport, state, selectedChapter);
        addExclusiveChoicePrerequisiteActions(sections, canvasViewport, state, selectedChapter);
        if (CanvasGridFitController.canFitExclusiveChoiceToGrid(state, selectedChapter, state.contextMenu.contextCanvasExclusiveChoiceId)) {
            sections.add(ContextMenuSection.ARRANGE, new ContextAction(CanvasContextMenuController.tr("ui.questsandstuff.context.fit_to_grid"), "fit_grid", ThemeColors.INTERACTIVE, () -> {
                boolean changed = CanvasGridFitController.fitExclusiveChoiceToGrid(state, selectedChapter, state.contextMenu.contextCanvasExclusiveChoiceId);
                ContextMenuController.clearDeleteConfirm(state);
                QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=fit_to_grid target=exclusive_choice id={} changed={}", state.contextMenu.contextCanvasExclusiveChoiceId, changed);
                canvasViewport.refresh();
            }));
        }
        CanvasExclusiveChoice ec = CanvasLayerMutations.findCanvasExclusiveChoice(state, selectedChapter, state.contextMenu.contextCanvasExclusiveChoiceId);
        if (ec != null) {
            sections.add(ContextMenuSection.APPEARANCE, ContextActionFactory.submenu(
                    CanvasContextMenuController.tr(QuestTranslationKeys.CONTEXT_CHANGE_EXCLUSIVE_CHOICE_TEXTURE),
                    "background", ThemeColors.INTERACTIVE, List.of(
                            ContextActionFactory.action(
                                    CanvasContextMenuController.tr(QuestTranslationKeys.CONTEXT_USE_ASSET_TEXTURE),
                                    "image", ThemeColors.INTERACTIVE, () -> {
                                        AssetPickerOpenActions.openExclusiveChoiceBackgroundPicker(state, selectedChapter, state.contextMenu.contextCanvasExclusiveChoiceId, ec.background());
                                        ContextMenuController.close(state);
                                        QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=change_ec_background ec={}", state.contextMenu.contextCanvasExclusiveChoiceId);
                                        canvasViewport.refresh();
                                    }
                            ),
                            ContextActionFactory.action(
                                    CanvasContextMenuController.tr(QuestTranslationKeys.CONTEXT_USE_BUILTIN_TEXTURE),
                                    "brick-wall", ThemeColors.INTERACTIVE, () -> {
                                        IconPickerOpenActions.openExclusiveChoiceGameTexturePicker(state, selectedChapter, state.contextMenu.contextCanvasExclusiveChoiceId);
                                        ContextMenuController.close(state);
                                        QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=change_ec_background_game ec={}", state.contextMenu.contextCanvasExclusiveChoiceId);
                                        canvasViewport.refresh();
                                    }
                            )
                    )
            ));
            if (!ec.background().isBlank()) {
                String exclusiveChoiceBgKey = "ec_remove_bg:" + state.contextMenu.contextCanvasExclusiveChoiceId;
                sections.add(ContextMenuSection.DANGER, ContextActionFactory.warningDelete(state, exclusiveChoiceBgKey,
                        CanvasContextMenuController.tr(QuestTranslationKeys.CONTEXT_REMOVE_EXCLUSIVE_CHOICE_TEXTURE), () -> {
                            CanvasLayerMutations.putCanvasExclusiveChoice(state, selectedChapter, ec.withBackground(""));
                            CanvasLayerMutations.persistCanvasExclusiveChoice(state, selectedChapter, ec.id());
                            ContextMenuController.clearDeleteConfirm(state);
                            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=remove_ec_background ec={}", state.contextMenu.contextCanvasExclusiveChoiceId);
                            canvasViewport.refresh();
                        }
                ));
            }
        }
        addLayerActions(sections, canvasViewport, state, selectedChapter, CanvasLayerOrdering.exclusiveChoiceKey(state.contextMenu.contextCanvasExclusiveChoiceId), "exclusive_choice", state.contextMenu.contextCanvasExclusiveChoiceId);
        addCopyAndDeleteActions(sections, canvasViewport, state);
    }

    private static void addExclusiveChoiceConnectedQuestActions(ContextMenuSections sections, CanvasViewport canvasViewport, UiState state, String selectedChapter) {
        CanvasExclusiveChoice ec = CanvasLayerMutations.findCanvasExclusiveChoice(state, selectedChapter, state.contextMenu.contextCanvasExclusiveChoiceId);
        if (ec == null) {
            return;
        }
        sections.add(ContextMenuSection.PRIMARY, ContextActionFactory.promoted(
                CanvasContextMenuController.tr("ui.questsandstuff.context.connect_to"),
                "connect", ThemeColors.SUCCESS, () -> {
                    state.canvas.connectExclusiveChoiceId = state.contextMenu.contextCanvasExclusiveChoiceId;
                    ContextMenuController.close(state);
                    QuestsAndStuffMod.debugLog("[QnS:UI] canvas exclusive choice connect_to id={}", state.contextMenu.contextCanvasExclusiveChoiceId);
                    canvasViewport.refresh();
                }
        ));
    }

    private static void addExclusiveChoicePrerequisiteActions(ContextMenuSections sections, CanvasViewport canvasViewport, UiState state, String selectedChapter) {
        CanvasExclusiveChoice ec = CanvasLayerMutations.findCanvasExclusiveChoice(state, selectedChapter, state.contextMenu.contextCanvasExclusiveChoiceId);
        if (ec == null) {
            return;
        }
        int connectionCount = ec.connectionQuestIds().size() + ec.prerequisiteQuestIds().size();
        if (connectionCount <= 0) {
            return;
        }
        sections.add(ContextMenuSection.PRIMARY, ContextActionFactory.action(CanvasContextMenuController.tr(QuestTranslationKeys.CONTEXT_PREREQUISITES_MANAGER), "share-2", ThemeColors.INTERACTIVE, () -> {
            AppOpenActions.openPrerequisitesManagerForExclusiveChoice(state, ec.id());
            ContextMenuController.clearDeleteConfirm(state);
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=prerequisites_manager ec={} connections={}", state.contextMenu.contextCanvasExclusiveChoiceId, connectionCount);
            canvasViewport.refresh();
        }));
    }

    private static void addCopyAndDeleteActions(ContextMenuSections sections, CanvasViewport canvasViewport, UiState state) {
        if (CanvasContextMenuSupport.canCopyContext(canvasViewport, state)) {
            sections.add(ContextMenuSection.CLIPBOARD, ContextActionFactory.copy(() -> {
                CanvasContextMenuSupport.copyContextToClipboard(canvasViewport, state);
                ContextMenuController.clearDeleteConfirm(state);
                canvasViewport.refresh();
            }));
        }
        if (CanvasContextDeleteController.canDeleteContext(state)) {
            String deleteKey = CanvasContextDeleteController.deleteConfirmKey(state);
            sections.add(ContextMenuSection.DANGER, ContextActionFactory.delete(state, deleteKey, TranslationKeys.text(TranslationKeys.COMMON_REMOVE), () -> {
                CanvasContextDeleteController.runDeleteAction(canvasViewport.player(), state);
                canvasViewport.refresh();
            }));
        }
    }

    private static void addLayerActions(ContextMenuSections sections, CanvasViewport canvasViewport, UiState state, String selectedChapter, String layerKey, String targetName, String targetId) {
        if (CanvasContextMenuSupport.canMoveLayer(canvasViewport, state, selectedChapter, layerKey, true)) {
            sections.add(ContextMenuSection.ARRANGE, ContextActionFactory.action(CanvasContextMenuController.tr("ui.questsandstuff.context.bring_to_front"), "up", ThemeColors.INTERACTIVE, () -> {
                switch (targetName) {
                    case "image" -> CanvasLayerMutations.moveImageLayer(state, selectedChapter, targetId, true);
                    case "text" -> CanvasLayerMutations.moveTextLayer(state, selectedChapter, targetId, true);
                    case "exclusive_choice" -> CanvasLayerMutations.moveExclusiveChoiceLayer(state, selectedChapter, targetId, true);
                }
                ContextMenuController.clearDeleteConfirm(state);
                QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=bring_to_front target={} id={}", targetName, targetId);
                canvasViewport.refresh();
            }));
        }
        if (CanvasContextMenuSupport.canMoveLayer(canvasViewport, state, selectedChapter, layerKey, false)) {
            sections.add(ContextMenuSection.ARRANGE, ContextActionFactory.action(CanvasContextMenuController.tr("ui.questsandstuff.context.send_to_back"), "down", ThemeColors.TEXT_MUTED, () -> {
                switch (targetName) {
                    case "image" -> CanvasLayerMutations.moveImageLayer(state, selectedChapter, targetId, false);
                    case "text" -> CanvasLayerMutations.moveTextLayer(state, selectedChapter, targetId, false);
                    case "exclusive_choice" -> CanvasLayerMutations.moveExclusiveChoiceLayer(state, selectedChapter, targetId, false);
                }
                ContextMenuController.clearDeleteConfirm(state);
                QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=send_to_back target={} id={}", targetName, targetId);
                canvasViewport.refresh();
            }));
        }
    }
}
