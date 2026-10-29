package com.abo47.questsandstuff.client.tablet.quest.canvas.render;

import java.util.List;
import java.util.function.BiConsumer;
import javax.annotation.Nonnull;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasViewport;
import com.abo47.questsandstuff.client.sync.state.ClientQuestStateFacade;
import com.abo47.questsandstuff.client.tablet.controls.InlineRenameField;
import com.abo47.questsandstuff.client.tablet.icons.DisplayIconWidget;
import com.abo47.questsandstuff.client.tablet.quest.canvas.geometry.CanvasGeometry;
import com.abo47.questsandstuff.client.tablet.quest.canvas.geometry.CanvasLayoutService;
import com.abo47.questsandstuff.client.tablet.quest.canvas.interaction.CanvasInteractionGate;
import com.abo47.questsandstuff.client.tablet.quest.canvas.layer.CanvasLayerMutations;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.QuestCardLayout;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.card.QuestElementSelectionSlot;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.card.QuestGlowEffect;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.card.QuestCardBackgroundRenderer;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.card.QuestHoverExpand;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.card.QuestEffectBadges;
import com.abo47.questsandstuff.client.tablet.quest.canvas.selection.CanvasSelectionActions;
import com.abo47.questsandstuff.client.tablet.quest.editor.EditorQuestCommandClient;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory;
import com.abo47.questsandstuff.client.tablet.theme.tokens.TabletColors;
import com.abo47.questsandstuff.quest.model.canvas.CanvasExclusiveChoice;
import com.abo47.questsandstuff.util.naming.QuestIdentity;

import static com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory.withAlpha;
import static com.abo47.questsandstuff.client.tablet.ui.state.TabletStateQueries.selectedChapterName;

final class CanvasSceneQuestCardPainter {
    private CanvasSceneQuestCardPainter() {
    }

    static void renderQuestCard(
            WidgetGroup canvasViewport,
            TabletUiState state,
            Player player,
            Runnable refresh,
            QuestCardLayout card,
            int viewportW,
            int viewportH,
            BiConsumer<String, WidgetGroup> questCardLayerSink
    ) {
        if (!CanvasLayoutService.intersectsPanRenderWindow(card, viewportW, viewportH)) {
            return;
        }
        CompoundTag tag = card.tag();
        WidgetGroup cardLayer = new QuestHoverExpand(card.x(), card.y(), card.width(), card.height(), card.questId(), state);
        QuestCardLayout localCard = localCard(card);
        float progress = QuestCardBackgroundRenderer.questProgress(tag);
        boolean customBackground = QuestCardBackgroundRenderer.renderWidgetBackground(cardLayer, localCard.x(), localCard.y(), localCard.width(), localCard.height(), tag, progress);
        if (!customBackground && QuestCardBackgroundRenderer.shouldShowProgressFill(tag, progress)) {
            QuestCardBackgroundRenderer.renderWidgetProgressFill(cardLayer, localCard.x(), localCard.y(), localCard.width(), localCard.height(), progress);
        }
        renderQuestIcon(cardLayer, localCard);
        renderLockedPreviewState(cardLayer, localCard);
        if (!ClientQuestStateFacade.questLockedPreview(card.tag())) {
            renderExclusiveChoiceConnectionLockedPreview(cardLayer, state, card);
        }
        renderSearchState(cardLayer, state, localCard);
        renderHiddenEditState(cardLayer, state, localCard);
        QuestEffectBadges.render(cardLayer, state, localCard);
        canvasViewport.addWidget(cardLayer);
        if (state.root.canEdit
                && !card.questId().equals(state.questDetails.pendingQuestTitleChangeId)) {
            canvasViewport.addWidget(new WidgetGroup(0, 0, canvasViewport.getSizeWidth(), canvasViewport.getSizeHeight()) {
                @Override
                public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
                    if (!state.canvas.canvasSelection.questIds().contains(card.questId())) {
                        return;
                    }
                    int originX = getPositionX();
                    int originY = getPositionY();
                    if (state.canvas.draggingSelection && state.canvas.canvasSelection.questIds().contains(card.questId())) {
                        originX += CanvasGeometry.screenX(state, state.canvas.dragStartBoundsLeft + state.canvas.dragSelectionDeltaX)
                                - CanvasGeometry.screenX(state, state.canvas.dragStartBoundsLeft);
                        originY += CanvasGeometry.screenY(state, state.canvas.dragStartBoundsTop + state.canvas.dragSelectionDeltaY)
                                - CanvasGeometry.screenY(state, state.canvas.dragStartBoundsTop);
                    }
                    if (CanvasSelectionActions.totalCanvasSelectionCount(state) <= 1) {
                        QuestElementSelectionSlot.drawScreenRectResizeOnly(graphics, originX, originY, card.x(), card.y(), card.width(), card.height());
                    } else {
                        QuestElementSelectionSlot.drawFillAndOutlineScreenRect(graphics, originX, originY, card.x(), card.y(), card.width(), card.height());
                    }
                }
            });
        }
        if (questCardLayerSink != null) {
            questCardLayerSink.accept(card.questId(), cardLayer);
        }
        if (state.root.canEdit && card.questId().equals(state.questDetails.pendingQuestTitleChangeId)) {
            renderQuestRenameField(canvasViewport, state, player, refresh, card, viewportW, viewportH);
            return;
        }
        cardLayer.addWidget(QuestGlowEffect.overlay(0, 0, card.width(), card.height(), state));
        addQuestTooltipHit(cardLayer, localCard, state);
    }

    private static QuestCardLayout localCard(QuestCardLayout card) {
        return new QuestCardLayout(
                card.questId(),
                card.tag(),
                card.logicalX(),
                card.logicalY(),
                card.logicalWidth(),
                card.logicalHeight(),
                card.slotLogicalWidth(),
                card.slotLogicalHeight(),
                card.visualLogicalX(),
                card.visualLogicalY(),
                card.scale(),
                0,
                0,
                card.width(),
                card.height()
        );
    }

    private static void renderQuestRenameField(WidgetGroup canvasViewport, TabletUiState state, Player player, Runnable refresh, QuestCardLayout card, int viewportW, int viewportH) {
        int fieldW = Math.max(64, Math.min(124, Math.max(card.width() + 28, card.width() * 2)));
        int fieldH = 16;
        int x = Math.max(4, Math.min(card.centerX() - fieldW / 2, Math.max(4, viewportW - fieldW - 4)));
        int belowY = card.y() + card.height() + 4;
        int aboveY = card.y() - fieldH - 4;
        int y = belowY + fieldH <= viewportH - 4 ? belowY : Math.max(4, Math.min(aboveY, viewportH - fieldH - 4));

        InlineRenameField field = new InlineRenameField(x, y, fieldW, fieldH, () -> state.questDetails.questTitleDraft, value -> {
            state.questDetails.questTitleDraft = value == null ? "" : value;
        }, () -> {
            EditorQuestCommandClient.commitQuestTitleChange(player, state);
            refresh.run();
        }, () -> {
            EditorQuestCommandClient.cancelQuestTitleChange(state);
            refresh.run();
        }, null, null);
        field.setClientSideWidget();
        field.setCurrentString(state.questDetails.questTitleDraft == null ? "" : state.questDetails.questTitleDraft);
        field.setMaxStringLength(QuestIdentity.TITLE_MAX_LENGTH);
        field.setBordered(false);
        field.setTextColor(TabletColors.TEXT_PRIMARY);
        field.setBackground(SurfaceFactory.bordered(withAlpha(TabletColors.SURFACE_BASE, 246), TabletColors.BORDER_ACCENT));
        field.setFocus(true);
        canvasViewport.addWidget(field);
    }

    private static void renderHiddenEditState(WidgetGroup canvasViewport, TabletUiState state, QuestCardLayout card) {
        if (!state.root.canEdit || card.tag().getBoolean("completed")) {
            return;
        }
        boolean hidden = card.tag().getBoolean("visual_hidden") && !card.tag().getBoolean("unlocked");
        if (hidden) {
            addSolidRect(canvasViewport, card.x(), card.y(), card.width(), card.height(), withAlpha(TabletColors.SURFACE_BASE, 190));
        }
    }

    private static void renderLockedPreviewState(WidgetGroup canvasViewport, QuestCardLayout card) {
        if (ClientQuestStateFacade.questLockedPreview(card.tag())) {
            addSolidRect(canvasViewport, card.x(), card.y(), card.width(), card.height(), withAlpha(TabletColors.SURFACE_BASE, 150));
        }
    }

    private static void renderExclusiveChoiceConnectionLockedPreview(WidgetGroup canvasViewport, TabletUiState state, QuestCardLayout card) {
        if (state.root.canEdit) {
            return;
        }
        String chapter = selectedChapterName(state);
        for (CanvasExclusiveChoice ec : state.canvas.canvasExclusiveChoicesByChapter.getOrDefault(chapter, List.of())) {
            CanvasExclusiveChoice drawExclusiveChoice = CanvasLayerMutations.effectiveCanvasExclusiveChoice(state, ec);
            if (drawExclusiveChoice.connectionQuestIds().contains(card.questId()) && !drawExclusiveChoice.prerequisiteQuestIds().isEmpty()) {
                addSolidRect(canvasViewport, card.x(), card.y(), card.width(), card.height(), withAlpha(TabletColors.SURFACE_BASE, 150));
                return;
            }
        }
    }

    private static void renderSearchState(WidgetGroup canvasViewport, TabletUiState state, QuestCardLayout card) {
        String query = state.root.search == null ? "" : state.root.search.trim();
        if (query.isBlank()) {
            return;
        }
        boolean matches = CanvasRenderer.matchesSearchOnly(card.tag(), query);
        if (!matches) {
            addSolidRect(canvasViewport, card.x(), card.y(), card.width(), card.height(), withAlpha(TabletColors.SURFACE_BASE, 150));
            return;
        }
        WidgetGroup highlight = new WidgetGroup(card.x() - 2, card.y() - 2, card.width() + 4, card.height() + 4);
        highlight.setBackground(SurfaceFactory.transparentBorder(TabletColors.WARNING));
        canvasViewport.addWidget(highlight);
    }

    private static void addSolidRect(WidgetGroup parent, int x, int y, int width, int height, int color) {
        if (width <= 0 || height <= 0 || (color >>> 24) == 0) {
            return;
        }
        WidgetGroup rect = new WidgetGroup(x, y, width, height);
        rect.setBackground(SurfaceFactory.fill(color));
        parent.addWidget(rect);
    }

    private static void renderQuestIcon(WidgetGroup canvasViewport, QuestCardLayout card) {
        String icon = card.tag().getString("icon");
        int min = Math.min(card.width(), card.height());
        int pad = Math.max(1, Math.round(min * 0.16f));
        int iconSize = Math.max(1, min - pad * 2);
        int iconX = card.x() + (card.width() - iconSize) / 2;
        int iconY = card.y() + (card.height() - iconSize) / 2;
        canvasViewport.addWidget(new DisplayIconWidget(iconX, iconY, iconSize, iconSize, icon));
    }

    private static void addQuestTooltipHit(WidgetGroup canvasViewport, QuestCardLayout card, TabletUiState state) {
        CompoundTag tag = card.tag();
        String title = tag.getString("title");
        if (title == null || title.isBlank()) {
            title = card.questId();
        }
        int progress = QuestCardBackgroundRenderer.progressPercent(tag);
        Component status = ClientQuestStateFacade.questLockedPreview(tag)
                ? Component.translatable("ui.questsandstuff.quest.locked")
                : Component.literal(progress + "%");
        ButtonWidget hit = new ButtonWidget(card.x(), card.y(), card.width(), card.height(), SurfaceFactory.transparentFill(), click -> {}) {
            @Override
            public boolean isMouseOverElement(double mouseX, double mouseY) {
                return super.isMouseOverElement(mouseX, mouseY)
                        && CanvasInteractionGate.hoverAllowed(state, this, mouseX, mouseY);
            }
        };
        hit.setClientSideWidget();
        hit.setHoverTexture(SurfaceFactory.transparentFill());
        hit.setClickedTexture(SurfaceFactory.transparentFill());
        hit.setHoverTooltips(new Component[]{
                Component.literal(title),
                status
        });
        canvasViewport.addWidget(hit);
    }
}
