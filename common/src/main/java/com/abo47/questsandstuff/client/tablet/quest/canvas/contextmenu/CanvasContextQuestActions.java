package com.abo47.questsandstuff.client.tablet.quest.canvas.contextmenu;
import com.abo47.questsandstuff.client.tablet.modal.SoundPickerOpenActions;
import com.abo47.questsandstuff.client.tablet.modal.AssetPickerOpenActions;
import com.abo47.questsandstuff.client.tablet.modal.AppOpenActions;
import com.abo47.questsandstuff.client.tablet.modal.IconPickerOpenActions;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.sync.state.ClientQuestStateFacade;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextAction;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextActionFactory;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuController;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuSection;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuSections;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuTarget;
import com.abo47.questsandstuff.client.tablet.controls.EntityIconControls;
import com.abo47.questsandstuff.client.tablet.entity.motion.EntityMotionEditor;
import com.abo47.questsandstuff.client.tablet.modal.target.ModalTargets;
import com.abo47.questsandstuff.client.tablet.quest.canvas.geometry.CanvasGridFitController;
import com.abo47.questsandstuff.client.tablet.quest.canvas.layer.CanvasLayerMutations;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasViewport;
import com.abo47.questsandstuff.client.tablet.quest.canvas.blueprint.CanvasBlueprintController;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.QuestCardLayout;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.geometry.CanvasLayerOrdering;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsWindow;
import com.abo47.questsandstuff.client.tablet.quest.editor.EditorQuestCommandClient;
import com.abo47.questsandstuff.client.tablet.state.UiState;
import com.abo47.questsandstuff.client.tablet.text.QuestTranslationKeys;
import com.abo47.questsandstuff.client.tablet.text.TranslationKeys;
import com.abo47.questsandstuff.client.tablet.theme.tokens.ThemeColors;
import com.abo47.questsandstuff.client.tablet.ui.widget.WidgetCoordinates;
import com.abo47.questsandstuff.quest.model.QuestDisplay;
import com.abo47.questsandstuff.quest.model.task.QuestVisibilityMode;

final class CanvasContextQuestActions {
    private CanvasContextQuestActions() {
    }

    static void addQuestActions(ContextMenuSections sections, CanvasViewport canvasViewport, UiState state, Player player, String selectedChapter) {
        if (state.contextMenu.contextMenuTarget != ContextMenuTarget.QUEST || state.contextMenu.contextQuestId.isBlank()) {
            return;
        }
        CompoundTag questTag = ClientQuestStateFacade.quest(state.contextMenu.contextQuestId);
        ContextMenuController.closeExclusiveSubmenus(state);
        QuestCardLayout contextQuest = canvasViewport.cardLookup().get(state.contextMenu.contextQuestId);

        sections.add(ContextMenuSection.PRIMARY, ContextActionFactory.action(CanvasContextMenuController.tr("ui.questsandstuff.context.open_quest"), "open", ThemeColors.INTERACTIVE, withCleanup(canvasViewport, state, () -> {
            openQuestDetails(canvasViewport, state);
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=open_quest quest={}", state.contextMenu.contextQuestId);
        })));
        if (CanvasContextMenuSupport.hasOtherQuest(canvasViewport, state.contextMenu.contextQuestId)) {
            sections.add(ContextMenuSection.PRIMARY, ContextActionFactory.promoted(CanvasContextMenuController.tr("ui.questsandstuff.context.connect_to"), "connect", ThemeColors.SUCCESS, withCleanup(canvasViewport, state, () -> {
                state.canvas.connectSourceQuestId = state.contextMenu.contextQuestId;
                state.canvas.connectSourceQuestIds.clear();
                if (state.canvas.canvasSelection.questIds().contains(state.contextMenu.contextQuestId) && state.canvas.canvasSelection.questIds().size() > 1) {
                    state.canvas.connectSourceQuestIds.addAll(state.canvas.canvasSelection.questIds());
                } else {
                    state.canvas.canvasSelection.questIds().clear();
                    state.canvas.canvasSelection.questIds().add(state.contextMenu.contextQuestId);
                    state.canvas.connectSourceQuestIds.add(state.contextMenu.contextQuestId);
                }
                QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=connect_to sources={}", state.canvas.connectSourceQuestIds);
            })));
        }
        sections.add(ContextMenuSection.PRIMARY, ContextActionFactory.promoted(CanvasContextMenuController.tr(QuestTranslationKeys.CONTEXT_CHANGE_TITLE), "rename", ThemeColors.INTERACTIVE, withCleanup(canvasViewport, state, () -> {
            EditorQuestCommandClient.beginQuestTitleChange(state, state.contextMenu.contextQuestId);
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=change_title quest={}", state.contextMenu.contextQuestId);
        })));
        sections.add(ContextMenuSection.PRIMARY, ContextActionFactory.submenu(CanvasContextMenuController.tr("ui.questsandstuff.menu.change_icon"), "icon", ThemeColors.INTERACTIVE, List.of(
                ContextActionFactory.action(TranslationKeys.text(QuestTranslationKeys.CONTEXT_USE_BUILTIN_ICON), "icon", ThemeColors.INTERACTIVE, () -> {
                    EntityIconControls.openIconPicker(state, EntityIconControls.IconPickerTarget.quest(state.contextMenu.contextQuestId));
                    ContextMenuController.closeExclusiveSubmenus(state);
                    QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=change_icon quest={}", state.contextMenu.contextQuestId);
                    canvasViewport.refresh();
                }),
                ContextActionFactory.action(TranslationKeys.text(QuestTranslationKeys.CONTEXT_USE_ASSET_IMAGE), "image", ThemeColors.INTERACTIVE, () -> {
                    AssetPickerOpenActions.openQuestAssetIconPicker(state, state.contextMenu.contextQuestId);
                    ContextMenuController.closeExclusiveSubmenus(state);
                    QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=change_icon_asset quest={}", state.contextMenu.contextQuestId);
                    canvasViewport.refresh();
                })
        )));

        addQuestPrerequisiteActions(sections, canvasViewport, state, questTag);
        addQuestBehaviorActions(sections, canvasViewport, state, player, questTag);
        addCompletionSoundActions(sections, canvasViewport, state, questTag);

        addQuestBackgroundActions(sections, canvasViewport, state, player, questTag);

        if (CanvasGridFitController.canFitQuestToGrid(state, contextQuest)) {
            sections.add(ContextMenuSection.ARRANGE, new ContextAction(CanvasContextMenuController.tr("ui.questsandstuff.context.fit_to_grid"), "fit_grid", ThemeColors.INTERACTIVE, withCleanup(canvasViewport, state, () -> {
                boolean changed = CanvasGridFitController.fitQuestToGrid(player, state, contextQuest);
                QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=fit_to_grid target=quest id={} changed={}", state.contextMenu.contextQuestId, changed);
            })));
        }
        addQuestArrangeActions(sections, canvasViewport, state, selectedChapter);

        EntityIconControls.addEntityVariantAndMotionActions(
                sections,
                ContextMenuSection.APPEARANCE,
                state,
                questTag.getString("icon"),
                ModalTargets.questIcon(state.contextMenu.contextQuestId),
                () -> ContextMenuController.close(state),
                () -> {
                    EntityMotionEditor.openQuestIcon(state, state.contextMenu.contextQuestId, state.contextMenu.contextMenuX, state.contextMenu.contextMenuY);
                    QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=edit_entity_icon_motion quest={}", state.contextMenu.contextQuestId);
                },
                canvasViewport::refresh
        );
        if (questTag.getBoolean("completed") || questTag.getBoolean("claimed") || questTag.getFloat("progress") > 0.0f) {
            String resetKey = "quest_reset:" + state.contextMenu.contextQuestId;
            sections.add(ContextMenuSection.DANGER, ContextActionFactory.warningDelete(state, resetKey, CanvasContextMenuController.tr("ui.questsandstuff.context.reset_quest"), withCleanup(canvasViewport, state, () -> {
                EditorQuestCommandClient.resetQuestProgress(player, state.contextMenu.contextQuestId);
                QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=reset_quest quest={}", state.contextMenu.contextQuestId);
            })));
        }
        addQuestCopyAndDeleteActions(sections, canvasViewport, state, player);
        sections.add(ContextMenuSection.PRIMARY, ContextActionFactory.promoted(CanvasContextMenuController.tr("ui.questsandstuff.context.save_as_blueprint"), "scroll", ThemeColors.INTERACTIVE, () -> {
            ContextMenuController.close(state);
            ContextMenuController.clearDeleteConfirm(state);
            boolean saved = CanvasBlueprintController.saveQuestWithNotice(canvasViewport, state, state.contextMenu.contextQuestId, state.contextMenu.contextMenuX, state.contextMenu.contextMenuY);
            QuestsAndStuffMod.debugLog("[QnS:UI:Blueprint] context save_as_blueprint target=quest id={} saved={}", state.contextMenu.contextQuestId, saved);
            canvasViewport.refresh();
        }));
    }

    private static void addQuestBackgroundActions(ContextMenuSections sections, CanvasViewport canvasViewport, UiState state, Player player, CompoundTag questTag) {
        sections.add(ContextMenuSection.APPEARANCE, ContextActionFactory.submenu(CanvasContextMenuController.tr(QuestTranslationKeys.CONTEXT_CHANGE_QUEST_BACKGROUND), "background", ThemeColors.INTERACTIVE, List.of(
                ContextActionFactory.action(CanvasContextMenuController.tr(QuestTranslationKeys.CONTEXT_USE_ASSET_TEXTURE), "image", ThemeColors.INTERACTIVE, withCleanup(canvasViewport, state, () -> {
                    AssetPickerOpenActions.openQuestBackgroundPicker(
                            state,
                            state.contextMenu.contextQuestId,
                            questTag.getString("quest_background"),
                            questTag.getBoolean("quest_background_grayscale")
                    );
                    QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=change_quest_background quest={}", state.contextMenu.contextQuestId);
                })),
                ContextActionFactory.action(CanvasContextMenuController.tr(QuestTranslationKeys.CONTEXT_USE_BUILTIN_TEXTURE), "brick-wall", ThemeColors.INTERACTIVE, () -> {
                    IconPickerOpenActions.openQuestGameTexturePicker(
                            state,
                            state.contextMenu.contextQuestId,
                            questTag.getBoolean("quest_background_grayscale")
                    );
                    ContextMenuController.closeExclusiveSubmenus(state);
                    QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=change_quest_background_game quest={}", state.contextMenu.contextQuestId);
                    canvasViewport.refresh();
                })
        )));
        if (!QuestDisplay.DEFAULT_QUEST_BACKGROUND.equals(QuestDisplay.normalizeQuestBackground(questTag.getString("quest_background")))) {
            String removeBgKey = "quest_remove_bg:" + state.contextMenu.contextQuestId;
            sections.add(ContextMenuSection.DANGER, ContextActionFactory.warningDelete(state, removeBgKey, CanvasContextMenuController.tr(QuestTranslationKeys.CONTEXT_REMOVE_QUEST_TEXTURE), withCleanup(canvasViewport, state, () -> {
                EditorQuestCommandClient.setQuestBackground(player, state.contextMenu.contextQuestId, QuestDisplay.DEFAULT_QUEST_BACKGROUND, false);
                QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=remove_quest_background quest={}", state.contextMenu.contextQuestId);
            })));
        }
        addCompletionHudBackgroundActions(sections, canvasViewport, state, player, questTag);
    }

    private static void addCompletionSoundActions(ContextMenuSections sections, CanvasViewport canvasViewport, UiState state, CompoundTag questTag) {
        sections.add(ContextMenuSection.BEHAVIOR, ContextActionFactory.submenu(TranslationKeys.text(QuestTranslationKeys.CONTEXT_CHANGE_COMPLETION_SOUND), "audio-lines", ThemeColors.INTERACTIVE, List.of(
                ContextActionFactory.action(CanvasContextMenuController.tr(QuestTranslationKeys.CONTEXT_USE_GAME_SOUND), "audio-lines", ThemeColors.INTERACTIVE, () -> {
                    String sound = questTag.getString("completion_sound");
                    SoundPickerOpenActions.openQuestGameSoundPicker(state, state.contextMenu.contextQuestId, sound);
                    ContextMenuController.closeExclusiveSubmenus(state);
                    QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=completion_sound_game quest={} sound={}", state.contextMenu.contextQuestId, sound);
                    canvasViewport.refresh();
                }),
                ContextActionFactory.action(CanvasContextMenuController.tr(QuestTranslationKeys.CONTEXT_USE_CUSTOM_SOUND), "audio-lines", ThemeColors.INTERACTIVE, () -> {
                    String sound = questTag.getString("completion_sound");
                    SoundPickerOpenActions.openQuestCustomCompletionSoundPicker(state, state.contextMenu.contextQuestId, sound);
                    ContextMenuController.closeExclusiveSubmenus(state);
                    QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=completion_sound_custom quest={} sound={}", state.contextMenu.contextQuestId, sound);
                    canvasViewport.refresh();
                })
        )));
    }

    private static void addCompletionHudBackgroundActions(ContextMenuSections sections, CanvasViewport canvasViewport, UiState state, Player player, CompoundTag questTag) {
        String currentBackground = questTag == null ? "" : questTag.getString("completion_hud_background");
        sections.add(ContextMenuSection.APPEARANCE, ContextActionFactory.submenu(CanvasContextMenuController.tr(QuestTranslationKeys.CONTEXT_CHANGE_COMPLETION_HUD_BACKGROUND), "completion_hud_background", ThemeColors.INTERACTIVE, List.of(
                ContextActionFactory.action(CanvasContextMenuController.tr(QuestTranslationKeys.CONTEXT_USE_ASSET_TEXTURE), "image", ThemeColors.INTERACTIVE, withCleanup(canvasViewport, state, () -> {
                    AssetPickerOpenActions.openQuestCompletionHudBackgroundPicker(state, state.contextMenu.contextQuestId, currentBackground);
                    QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=change_completion_hud_background quest={}", state.contextMenu.contextQuestId);
                })),
                ContextActionFactory.action(CanvasContextMenuController.tr(QuestTranslationKeys.CONTEXT_USE_BUILTIN_TEXTURE), "brick-wall", ThemeColors.INTERACTIVE, () -> {
                    IconPickerOpenActions.openQuestCompletionHudGameTexturePicker(state, state.contextMenu.contextQuestId);
                    ContextMenuController.closeExclusiveSubmenus(state);
                    QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=change_completion_hud_background_game quest={}", state.contextMenu.contextQuestId);
                    canvasViewport.refresh();
                })
        )));
        if (!QuestDisplay.DEFAULT_COMPLETION_HUD_BACKGROUND.equals(QuestDisplay.normalizeCompletionHudBackground(currentBackground))) {
            String removeHudBgKey = "quest_remove_hud_bg:" + state.contextMenu.contextQuestId;
            sections.add(ContextMenuSection.DANGER, ContextActionFactory.warningDelete(state, removeHudBgKey, CanvasContextMenuController.tr(QuestTranslationKeys.CONTEXT_REMOVE_COMPLETION_HUD_BACKGROUND), withCleanup(canvasViewport, state, () -> {
                EditorQuestCommandClient.setQuestCompletionHudBackground(player, state.contextMenu.contextQuestId, QuestDisplay.DEFAULT_COMPLETION_HUD_BACKGROUND);
                QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=remove_completion_hud_background quest={}", state.contextMenu.contextQuestId);
            })));
        }
    }

    private static void openQuestDetails(CanvasViewport canvasViewport, UiState state) {
        QuestCardLayout card = canvasViewport.cardLookup().get(state.contextMenu.contextQuestId);
        int viewportScreenX = WidgetCoordinates.screenX(canvasViewport, state.canvas.canvasPanelX + state.canvas.canvasViewportX);
        int viewportScreenY = WidgetCoordinates.screenY(canvasViewport, state.canvas.canvasPanelY + state.canvas.canvasViewportY);
        if (card == null) {
            QuestDetailsWindow.openAtSource(
                    state,
                    state.contextMenu.contextQuestId,
                    viewportScreenX + state.contextMenu.contextMenuX,
                    viewportScreenY + state.contextMenu.contextMenuY,
                    1,
                    1
            );
            return;
        }
        QuestDetailsWindow.openAtSource(
                state,
                state.contextMenu.contextQuestId,
                viewportScreenX + card.x(),
                viewportScreenY + card.y(),
                card.width(),
                card.height()
        );
    }

    private static void addQuestPrerequisiteActions(ContextMenuSections sections, CanvasViewport canvasViewport, UiState state, CompoundTag questTag) {
        sections.add(ContextMenuSection.PRIMARY, ContextActionFactory.action(CanvasContextMenuController.tr(QuestTranslationKeys.CONTEXT_PREREQUISITES_MANAGER), "share-2", ThemeColors.INTERACTIVE, withCleanup(canvasViewport, state, () -> {
            AppOpenActions.openPrerequisitesManager(state, state.contextMenu.contextQuestId);
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=prerequisites_manager quest={}", state.contextMenu.contextQuestId);
        })));
    }

    private static void addQuestBehaviorActions(ContextMenuSections sections, CanvasViewport canvasViewport, UiState state, Player player, CompoundTag questTag) {
        List<ContextAction> behaviorActions = new ArrayList<>();
        addQuestRepeatableAction(behaviorActions, canvasViewport, state, player, questTag);
        addQuestLockAction(behaviorActions, canvasViewport, state, player, questTag);
        addQuestHiddenAction(behaviorActions, canvasViewport, state, player, questTag);
        sections.add(ContextMenuSection.BEHAVIOR, ContextActionFactory.submenu(TranslationKeys.text(QuestTranslationKeys.CONTEXT_BEHAVIOR), "wrench", ThemeColors.INTERACTIVE, behaviorActions));
    }

    private static void addQuestRepeatableAction(List<ContextAction> actions, CanvasViewport canvasViewport, UiState state, Player player, CompoundTag questTag) {
        boolean repeatable = questTag.getBoolean("repeatable");
        actions.add(new ContextAction(
                CanvasContextMenuController.tr(repeatable ? QuestTranslationKeys.CONTEXT_MAKE_QUEST_NOT_REPEATABLE : QuestTranslationKeys.CONTEXT_MAKE_QUEST_REPEATABLE),
                repeatable ? "repeat-off" : "repeat",
                repeatable ? ThemeColors.SUCCESS : ThemeColors.INTERACTIVE,
                withCleanup(canvasViewport, state, () -> {
                    EditorQuestCommandClient.setQuestRepeatable(player, state.contextMenu.contextQuestId, !repeatable);
                    QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=quest_repeatable quest={} enabled={}", state.contextMenu.contextQuestId, !repeatable);
                })));
    }

    private static void addQuestLockAction(List<ContextAction> actions, CanvasViewport canvasViewport, UiState state, Player player, CompoundTag questTag) {
        String hiddenMode = questTag.getString("hidden_mode");
        String locked = QuestVisibilityMode.LOCKED.serializedName();
        String prerequisitesVisible = QuestVisibilityMode.PREREQUISITES_VISIBLE.serializedName();
        boolean lockUntilUnlocked = locked.equals(hiddenMode);
        actions.add(new ContextAction(
                CanvasContextMenuController.tr(lockUntilUnlocked ? QuestTranslationKeys.CONTEXT_SHOW_QUEST_BEFORE_UNLOCKED : QuestTranslationKeys.CONTEXT_LOCK_QUEST_UNTIL_UNLOCKED),
                lockUntilUnlocked ? "unlock_quest" : "lock_quest",
                lockUntilUnlocked ? ThemeColors.SUCCESS : ThemeColors.INTERACTIVE,
                withCleanup(canvasViewport, state, () -> {
                    EditorQuestCommandClient.setQuestHiddenMode(player, state.contextMenu.contextQuestId, lockUntilUnlocked ? prerequisitesVisible : locked);
                    QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=quest_lock_until_unlocked quest={} enabled={}", state.contextMenu.contextQuestId, !lockUntilUnlocked);
                })));
    }

    private static void addQuestHiddenAction(List<ContextAction> actions, CanvasViewport canvasViewport, UiState state, Player player, CompoundTag questTag) {
        boolean hidden = questTag.getBoolean("visual_hidden");
        actions.add(new ContextAction(
                CanvasContextMenuController.tr(hidden ? QuestTranslationKeys.CONTEXT_REVEAL_QUEST : QuestTranslationKeys.CONTEXT_HIDE_QUEST_UNTIL_UNLOCKED),
                hidden ? "eye" : "eye-off",
                hidden ? ThemeColors.SUCCESS : ThemeColors.WARNING,
                withCleanup(canvasViewport, state, () -> {
                    EditorQuestCommandClient.setQuestVisualHidden(player, state.contextMenu.contextQuestId, !hidden);
                    QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=quest_hide_until_unlocked quest={} enabled={}", state.contextMenu.contextQuestId, !hidden);
                })));
    }

    private static void addQuestArrangeActions(ContextMenuSections sections, CanvasViewport canvasViewport, UiState state, String selectedChapter) {
        List<ContextAction> arrangeActions = new ArrayList<>();
        String layerKey = CanvasLayerOrdering.questKey(state.contextMenu.contextQuestId);
        if (CanvasContextMenuSupport.canMoveLayer(canvasViewport, state, selectedChapter, layerKey, true)) {
            arrangeActions.add(ContextActionFactory.action(CanvasContextMenuController.tr("ui.questsandstuff.context.bring_to_front"), "up", ThemeColors.INTERACTIVE, withCleanup(canvasViewport, state, () -> {
                CanvasLayerMutations.moveQuestLayer(state, selectedChapter, state.contextMenu.contextQuestId, true);
                QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=bring_to_front target=quest id={}", state.contextMenu.contextQuestId);
            })));
        }
        if (CanvasContextMenuSupport.canMoveLayer(canvasViewport, state, selectedChapter, layerKey, false)) {
            arrangeActions.add(ContextActionFactory.action(CanvasContextMenuController.tr("ui.questsandstuff.context.send_to_back"), "down", ThemeColors.TEXT_MUTED, withCleanup(canvasViewport, state, () -> {
                CanvasLayerMutations.moveQuestLayer(state, selectedChapter, state.contextMenu.contextQuestId, false);
                QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=send_to_back target=quest id={}", state.contextMenu.contextQuestId);
            })));
        }
        if (!arrangeActions.isEmpty()) {
            sections.add(ContextMenuSection.ARRANGE, ContextActionFactory.submenu(TranslationKeys.text(QuestTranslationKeys.CONTEXT_ORDER), "arrow-up-down", ThemeColors.INTERACTIVE, arrangeActions));
        }
    }

    private static void addQuestCopyAndDeleteActions(ContextMenuSections sections, CanvasViewport canvasViewport, UiState state, Player player) {
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
                CanvasContextDeleteController.runDeleteAction(player, state);
                canvasViewport.refresh();
            }));
        }
    }

    private static Runnable withCleanup(CanvasViewport canvasViewport, UiState state, Runnable action) {
        return () -> {
            action.run();
            ContextMenuController.clearDeleteConfirm(state);
            canvasViewport.refresh();
        };
    }
}
