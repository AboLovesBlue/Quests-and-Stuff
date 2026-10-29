package com.abo47.questsandstuff.client.tablet.modal.asset;

import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;

import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import com.abo47.questsandstuff.client.tablet.quest.canvas.render.connection.ConnectionOps;
import com.abo47.questsandstuff.client.quest.hud.QuestHudLayoutManager;
import com.abo47.questsandstuff.client.sync.state.ClientQuestStateFacade;
import com.abo47.questsandstuff.client.tablet.controls.PercentSliderControls;
import com.abo47.questsandstuff.client.tablet.controls.ToggleSwitchWidget;
import com.abo47.questsandstuff.client.tablet.quest.editor.EditorCanvasCommandClient;
import com.abo47.questsandstuff.client.tablet.state.UiState;
import com.abo47.questsandstuff.client.tablet.text.QuestTranslationKeys;
import com.abo47.questsandstuff.client.tablet.theme.tokens.ThemeColors;
import com.abo47.questsandstuff.client.tablet.modal.ModalSession;
import com.abo47.questsandstuff.client.tablet.modal.ModalPanel;
import com.abo47.questsandstuff.client.tablet.modal.target.ModalTargetState;

import static com.abo47.questsandstuff.client.tablet.modal.ModalSession.TargetSetSlot.QUEST_BACKGROUND;
import static com.abo47.questsandstuff.client.tablet.modal.ModalSession.TargetSlot.HUD_BACKGROUND;
import static com.abo47.questsandstuff.client.tablet.ui.factory.UiFactory.button;
import static com.abo47.questsandstuff.client.tablet.ui.factory.UiFactory.label;

final class AssetPickerPreviewControls {
    private AssetPickerPreviewControls() {
    }

    static boolean isQuestBackgroundPicker(UiState state) {
        return !ModalTargetState.target(state, ModalSession.TargetSlot.QUEST_BACKGROUND, state.modal.modalQuestBackgroundTarget).isBlank()
                || !ModalTargetState.targetSet(state, QUEST_BACKGROUND, state.modal.modalQuestBackgroundTargets).isEmpty();
    }

    static void addQuestBackgroundOptions(WidgetGroup preview, UiState state, Runnable refresh, int leftW, int previewH) {
        if (!isQuestBackgroundPicker(state)) {
            return;
        }
        String target = ModalTargetState.targetSet(state, QUEST_BACKGROUND, state.modal.modalQuestBackgroundTargets).isEmpty()
                ? ModalTargetState.target(state, ModalSession.TargetSlot.QUEST_BACKGROUND, state.modal.modalQuestBackgroundTarget)
                : "batch";
        int rowY = Math.max(48, previewH - 24);
        preview.addWidget(label(8, rowY + 3, ModalPanel.tr(QuestTranslationKeys.QUEST_BACKGROUND_GRAYSCALE), ThemeColors.TEXT_SECONDARY));
        preview.addWidget(new ToggleSwitchWidget(
                "quest_background_grayscale:" + target,
                Math.max(8, leftW - 8 - ToggleSwitchWidget.DEFAULT_WIDTH),
                rowY,
                ToggleSwitchWidget.DEFAULT_WIDTH,
                ToggleSwitchWidget.DEFAULT_HEIGHT,
                () -> state.modal.modalQuestBackgroundGrayscale,
                enabled -> state.modal.modalQuestBackgroundGrayscale = enabled,
                refresh,
                new Component[]{
                        Component.translatable(QuestTranslationKeys.QUEST_BACKGROUND_GRAYSCALE_TOOLTIP)
                }
        ));
    }

    static boolean isHudBackgroundPicker(UiState state) {
        return hudElement(state) != null;
    }

    static void addHudBackgroundOptions(WidgetGroup preview, UiState state, Runnable refresh, int leftW, int previewH) {
        QuestHudLayoutManager.Element element = hudElement(state);
        if (element == null) {
            return;
        }
        int rowY = Math.max(58, previewH - 56);
        preview.addWidget(label(8, rowY, ModalPanel.tr("ui.questsandstuff.hud.opacity"), ThemeColors.TEXT_SECONDARY));
        PercentSliderControls.add(
                preview,
                8,
                rowY + 12,
                leftW - 16,
                QuestHudLayoutManager.opacityPercent(element),
                next -> {
                    QuestHudLayoutManager.setOpacityPercent(element, next);
                    state.modal.modalHudBackgroundOpacityDraft = QuestHudLayoutManager.opacityPercent(element);
                    refresh.run();
                },
                refresh,
                () -> state.modal.modalHudBackgroundOpacityDragging,
                dragging -> state.modal.modalHudBackgroundOpacityDragging = dragging,
                new Component[]{Component.translatable("ui.questsandstuff.hud.opacity")}
        );
        boolean hudRemoveConfirming = state.modal.hudRemoveConfirmArmed;
        String hudRemoveLabel = hudRemoveConfirming ? "Sure?" : ModalPanel.tr("ui.questsandstuff.hud.remove_background");
        preview.addWidget(button(8, rowY + 32, leftW - 16, 14, hudRemoveLabel, ThemeColors.SURFACE_PANEL_ALT, ThemeColors.WARNING, click -> {
            if (hudRemoveConfirming) {
                state.modal.hudRemoveConfirmArmed = false;
                QuestHudLayoutManager.setBackground(element, "");
                state.pickers.assetSelected = "";
                refresh.run();
            } else {
                state.modal.hudRemoveConfirmArmed = true;
                refresh.run();
            }
        }));
    }

    static boolean isConnectionTexturePicker(UiState state) {
        return !state.modal.modalConnectionTextureTarget.isBlank()
                || !state.modal.modalConnectionTextureChapterTargets.isEmpty();
    }

    static void addConnectionTextureControls(WidgetGroup preview, UiState state, Player player, Runnable refresh, int leftW, int previewH) {
        if (!isConnectionTexturePicker(state)) {
            return;
        }
        int rowY = Math.max(58, previewH - 56);
        preview.addWidget(label(8, rowY, ModalPanel.tr("ui.questsandstuff.context.connection_texture_spacing"), ThemeColors.TEXT_SECONDARY));
        PercentSliderControls.addRanged(
                preview,
                8,
                rowY + 12,
                leftW - 16,
                -100,
                100,
                state.pickers.connectionTextureSpacingDraft,
                next -> {
                    state.pickers.connectionTextureSpacingDraft = Math.max(-100, Math.min(100, next));
                    refresh.run();
                },
                () -> commitConnectionTextureSpacing(player, state, refresh),
                () -> state.pickers.connectionTextureSpacingDragging,
                dragging -> state.pickers.connectionTextureSpacingDragging = dragging,
                new Component[]{Component.translatable("ui.questsandstuff.context.connection_texture_spacing")}
        );
    }

    static void commitConnectionTextureSpacing(Player player, UiState state, Runnable refresh) {
        int spacing = state.pickers.connectionTextureSpacingDraft;
        String target = ModalTargetState.target(state, ModalSession.TargetSlot.CONNECTION_TEXTURE, state.modal.modalConnectionTextureTarget);
        java.util.Set<String> chapterTargets = ModalTargetState.targetSet(state, ModalSession.TargetSetSlot.CONNECTION_TEXTURE_CHAPTERS, state.modal.modalConnectionTextureChapterTargets);
        if (!chapterTargets.isEmpty()) {
            String[] parts = target.split("\\|");
            String chapter = parts.length >= 2 ? parts[1] : "";
            for (String questId : chapterTargets) {
                CompoundTag questTag = ClientQuestStateFacade.quest(questId);
                if (questTag == null) continue;
                ListTag prereqs = questTag.getList("prerequisites", Tag.TAG_STRING);
                for (int i = 0; i < prereqs.size(); i++) {
                    String prerequisiteId = prereqs.getString(i);
                    EditorCanvasCommandClient.runConnectionTextureSpacingAction(player, questId, prerequisiteId, spacing);
                }
            }
            if (!chapter.isBlank()) {
                for (com.abo47.questsandstuff.quest.model.canvas.CanvasExclusiveChoice ec : state.canvas.canvasExclusiveChoicesByChapter.getOrDefault(chapter, java.util.List.of())) {
                    for (String connectedId : ec.connectionQuestIds()) {
                        if (chapterTargets.contains(connectedId) || chapterTargets.contains(ec.id())) {
                            EditorCanvasCommandClient.runExclusiveChoiceConnectionTextureSpacingAction(state, ec.id(), connectedId, spacing);
                        }
                    }
                    for (String prerequisiteId : ec.prerequisiteQuestIds()) {
                        if (chapterTargets.contains(prerequisiteId) || chapterTargets.contains(ec.id())) {
                            EditorCanvasCommandClient.runExclusiveChoiceConnectionTextureSpacingAction(state, prerequisiteId, ec.id(), spacing);
                        }
                    }
                }
            }
            return;
        }
        if (!target.isBlank()) {
            String[] parts = target.split("\\|");
            if (parts.length >= 4) {
                String chapter = parts[1];
                String prerequisiteId = parts[2];
                String questId = parts[3];
                com.abo47.questsandstuff.client.tablet.quest.canvas.render.connection.ConnectionOps.applyTextureSpacing(state, player, chapter, prerequisiteId, questId, spacing);
            }
        }
        refresh.run();
    }

    static QuestHudLayoutManager.Element hudElement(UiState state) {
        String target = ModalTargetState.target(state, HUD_BACKGROUND, state.modal.modalHudBackgroundTarget);
        if ("completion".equalsIgnoreCase(target)) {
            return QuestHudLayoutManager.Element.COMPLETION;
        }
        if ("pinned".equalsIgnoreCase(target)) {
            return QuestHudLayoutManager.Element.PINNED;
        }
        return null;
    }
}
