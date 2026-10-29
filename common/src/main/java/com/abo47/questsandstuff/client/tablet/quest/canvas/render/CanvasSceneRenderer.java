package com.abo47.questsandstuff.client.tablet.quest.canvas.render;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import javax.annotation.Nonnull;

import org.joml.Quaternionf;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasViewport;
import com.abo47.questsandstuff.client.sync.state.ClientQuestStateFacade;
import com.abo47.questsandstuff.client.tablet.layout.TabletGridControls;
import com.abo47.questsandstuff.client.tablet.quest.canvas.geometry.CanvasGeometry;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.QuestCardLayout;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.card.CanvasElementSelectionSlot;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.card.QuestCardBackgroundRenderer;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.connection.ConnectionLine;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.connection.ConnectionRenderer;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.geometry.CanvasElementGeometry;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.geometry.CanvasLayerOrdering;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.gizmo.CanvasTransformGizmo;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.gizmo.CanvasTransformGizmoAxis;
import com.abo47.questsandstuff.client.tablet.quest.canvas.layer.CanvasLayerMutations;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.text.CanvasTextRenderer;
import com.abo47.questsandstuff.client.tablet.quest.canvas.selection.CanvasSelectionActions;
import com.abo47.questsandstuff.client.tablet.quest.canvas.viewport.CanvasCameraController;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.BackgroundModes;
import com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory;
import com.abo47.questsandstuff.client.tablet.theme.tokens.TabletColors;
import com.abo47.questsandstuff.quest.model.QuestDisplay;
import com.abo47.questsandstuff.quest.model.canvas.CanvasExclusiveChoice;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;

import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.chapterBackgroundTexture;
import static com.abo47.questsandstuff.client.tablet.ui.state.TabletStateQueries.selectedChapterName;

final class CanvasSceneRenderer {
    private static final ResourceTexture EXCLUSIVE_CHOICE_TEXTURE = QuestCardBackgroundRenderer.EXCLUSIVE_CHOICE_TEXTURE;

    private CanvasSceneRenderer() {
    }

    static int snapCanvasContentSize(int available, int cell) {
        int grid = Math.max(1, cell);
        if (available <= grid) {
            return Math.max(1, available);
        }
        return Math.max(grid, (available / grid) * grid);
    }

    static void applyCanvasBackground(WidgetGroup canvasViewport) {
        canvasViewport.setBackground(SurfaceFactory.transparentFill());
    }

    static void renderGridOverlay(WidgetGroup canvasViewport, TabletUiState state, int contentX, int contentY, int contentW, int contentH) {
        if (contentW <= 0 || contentH <= 0) {
            return;
        }
        canvasViewport.addWidget(new WidgetGroup(0, 0, canvasViewport.getSizeWidth(), canvasViewport.getSizeHeight()) {
            @Override
            public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
                int lineColor = TabletGridControls.gridAlpha(state.canvas.gridOpacityPercent, TabletGridControls.defaultGridColor(state));
                int cell = CanvasGeometry.gridSize(state);
                int originX = getPositionX();
                int originY = getPositionY();
                int visibleLeft = contentX - state.canvas.canvasLivePanX;
                int visibleTop = contentY - state.canvas.canvasLivePanY;
                int visibleRight = contentX + contentW - state.canvas.canvasLivePanX;
                int visibleBottom = contentY + contentH - state.canvas.canvasLivePanY;

                int firstCol = (int) Math.floor(CanvasCameraController.screenToLogicalX(state, contentX, true) / cell) - 1;
                int lastCol = (int) Math.ceil(CanvasCameraController.screenToLogicalX(state, contentX + contentW, true) / cell) + 1;
                int firstRow = (int) Math.floor(CanvasCameraController.screenToLogicalY(state, contentY, true) / cell) - 1;
                int lastRow = (int) Math.ceil(CanvasCameraController.screenToLogicalY(state, contentY + contentH, true) / cell) + 1;

                for (int col = firstCol; col <= lastCol; col++) {
                    int x = CanvasGeometry.screenX(state, col * cell);
                    if (x < visibleLeft || x > visibleRight) {
                        continue;
                    }
                    SurfaceFactory.fill(lineColor).draw(graphics, 0, 0, originX + x, originY + visibleTop, 1, visibleBottom + 1 - visibleTop);
                }
                for (int row = firstRow; row <= lastRow; row++) {
                    int y = CanvasGeometry.screenY(state, row * cell);
                    if (y < visibleTop || y > visibleBottom) {
                        continue;
                    }
                    SurfaceFactory.fill(lineColor).draw(graphics, 0, 0, originX + visibleLeft, originY + y, visibleRight + 1 - visibleLeft, 1);
                }
            }
        });
    }

    static void renderCanvasSurfaceFactory(WidgetGroup canvasViewport, TabletUiState state, int contentX, int contentY, int contentW, int contentH, int viewportW, int viewportH) {
        int paintW = contentW + 1;
        int paintH = contentH + 1;
        IGuiTexture canvasBackground = BackgroundModes.createTexture(ClientQuestStateFacade.chapterCanvasBackground(selectedChapterName(state)));
        if (canvasViewport instanceof CanvasViewport cv) {
            cv.setExtendedBackgroundTexture(canvasBackground);
        }
        canvasViewport.addWidget(new WidgetGroup(0, 0, viewportW, viewportH) {
            @Override
            public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
                boolean portal = WorldPortalCapture.shouldCapture(state) && WorldPortalCapture.hasTexture();
                if (portal) {
                    WorldPortalCapture.drawInto(graphics, canvasViewport, state);
                }
                int percent = Math.max(0, Math.min(100, state.canvas.canvasBgOpacityPercent));
                if (percent == 0) {
                    return;
                }
                int fill = CanvasBackgroundOpacity.color(TabletColors.SURFACE_BASE, percent);
                if ((fill >>> 24) == 0) {
                    return;
                }
                int originX = getPositionX();
                int originY = getPositionY();
                if (portal) {
                    SurfaceFactory.fill(fill).draw(graphics, 0, 0, originX, originY, viewportW, viewportH);
                    if (canvasBackground != null) {
                        CanvasBackgroundOpacity.drawTexture(graphics, canvasBackground, mouseX, mouseY, originX + contentX, originY + contentY, paintW, paintH, percent);
                    }
                    return;
                }
                SurfaceFactory.fill(fill).draw(graphics, 0, 0, originX, originY, viewportW, contentY);
                SurfaceFactory.fill(fill).draw(graphics, 0, 0, originX, originY + contentY + paintH, viewportW, viewportH - contentY - paintH);
                SurfaceFactory.fill(fill).draw(graphics, 0, 0, originX, originY + contentY, contentX, paintH);
                SurfaceFactory.fill(fill).draw(graphics, 0, 0, originX + contentX + paintW, originY + contentY, viewportW - contentX - paintW, paintH);
                if (canvasBackground == null) {
                    SurfaceFactory.fill(fill).draw(graphics, 0, 0, originX + contentX, originY + contentY, paintW, paintH);
                }
            }
        });
    }

    static void renderCanvasElements(
            WidgetGroup canvasViewport,
            TabletUiState state,
            Player player,
            Runnable refresh,
            List<QuestCardLayout> visibleCards,
            int viewportW,
            int viewportH,
            BiConsumer<String, WidgetGroup> questCardLayerSink
    ) {
        String chapter = selectedChapterName(state);
        List<CanvasExclusiveChoice> exclusiveChoices = state.canvas.canvasExclusiveChoicesByChapter.getOrDefault(chapter, List.of());
        List<CanvasImageLayer> images = state.canvas.canvasImagesByChapter.getOrDefault(chapter, List.of());
        List<CanvasTextLayer> texts = state.canvas.canvasTextsByChapter.getOrDefault(chapter, List.of());
        if (exclusiveChoices.isEmpty() && images.isEmpty() && texts.isEmpty() && visibleCards.isEmpty()) {
            return;
        }
        Map<String, CanvasExclusiveChoice> exclusiveChoicesById = new HashMap<>();
        for (CanvasExclusiveChoice ec : exclusiveChoices) {
            exclusiveChoicesById.put(ec.id(), ec);
        }
        Map<String, CanvasImageLayer> imagesById = new HashMap<>();
        for (CanvasImageLayer image : images) {
            imagesById.put(image.id(), image);
        }
        Map<String, CanvasTextLayer> textsById = new HashMap<>();
        for (CanvasTextLayer text : texts) {
            textsById.put(text.id(), text);
        }
        Map<String, QuestCardLayout> cardsById = new HashMap<>();
        for (QuestCardLayout card : visibleCards) {
            cardsById.put(card.questId(), card);
        }
        List<ConnectionLine> connections = ConnectionRenderer.prerequisiteConnectionLines(state, visibleCards, cardsById, viewportW, viewportH);
        Map<String, ConnectionLine> connectionsByKey = new HashMap<>();
        List<String> connectionKeys = new ArrayList<>();
        for (ConnectionLine connection : connections) {
            String key = CanvasLayerOrdering.connectionKey(connection.connectionId());
            connectionsByKey.put(key, connection);
            connectionKeys.add(key);
        }
        List<String> layerOrder = CanvasLayerOrdering.normalize(state, chapter, visibleCards, images, texts, connectionKeys, exclusiveChoices);
        for (String key : layerOrder) {
            if (key.startsWith(CanvasLayerOrdering.CONNECTION_PREFIX)) {
                ConnectionLine connection = connectionsByKey.get(key);
                if (connection != null) {
                    ConnectionRenderer.renderConnectionLayer(canvasViewport, state, connection);
                }
                continue;
            }
            if (key.startsWith(CanvasLayerOrdering.EXCLUSIVE_CHOICE_PREFIX)) {
                CanvasExclusiveChoice ec = exclusiveChoicesById.get(key.substring(CanvasLayerOrdering.EXCLUSIVE_CHOICE_PREFIX.length()));
                if (ec != null) {
                    renderCanvasExclusiveChoice(canvasViewport, state, ec);
                }
                continue;
            }
            if (key.startsWith(CanvasLayerOrdering.IMAGE_PREFIX)) {
                CanvasImageLayer image = imagesById.get(key.substring(CanvasLayerOrdering.IMAGE_PREFIX.length()));
                if (image != null) {
                    renderCanvasImage(canvasViewport, state, image);
                }
                continue;
            }
            if (key.startsWith(CanvasLayerOrdering.TEXT_PREFIX)) {
                CanvasTextLayer text = textsById.get(key.substring(CanvasLayerOrdering.TEXT_PREFIX.length()));
                if (text != null) {
                    CanvasTextRenderer.renderCanvasText(canvasViewport, state, text);
                }
                continue;
            }
            if (key.startsWith(CanvasLayerOrdering.QUEST_PREFIX)) {
                QuestCardLayout card = cardsById.get(key.substring(CanvasLayerOrdering.QUEST_PREFIX.length()));
                if (card != null) {
                    CanvasSceneQuestCardPainter.renderQuestCard(canvasViewport, state, player, refresh, card, viewportW, viewportH, questCardLayerSink);
                }
            }
        }
    }

    private static void renderCanvasImage(WidgetGroup canvasViewport, TabletUiState state, CanvasImageLayer image) {
        canvasViewport.addWidget(new WidgetGroup(0, 0, canvasViewport.getSizeWidth(), canvasViewport.getSizeHeight()) {
            @Override
            public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
                CanvasImageLayer drawImage = CanvasLayerMutations.effectiveCanvasImage(state, image);
                int originX = getPositionX();
                int originY = getPositionY();
                CanvasElementGeometry.Box box = CanvasElementGeometry.screenBoxAtPivot(state, drawImage.x(), drawImage.y(), drawImage.w(), drawImage.h(), drawImage.pivotX(), drawImage.pivotY(), drawImage.rotation());
                int w = box.width();
                int h = box.height();
                int pivotX = -box.left();
                int pivotY = -box.top();
                CanvasImageLayerRenderer.drawAtPivot(graphics, mouseX, mouseY, drawImage, originX + box.centerX(), originY + box.centerY(), w, h, pivotX, pivotY);
                if (state.root.canEdit && CanvasSelectionActions.isImageSelected(state, drawImage.id())) {
                    if (CanvasSelectionActions.totalCanvasSelectionCount(state) > 1) {
                        CanvasElementSelectionSlot.drawFillAndOutlineAtPivot(graphics, state, originX, originY, drawImage.x(), drawImage.y(), drawImage.w(), drawImage.h(), drawImage.pivotX(), drawImage.pivotY(), drawImage.rotation());
                    } else if (CanvasTransformGizmoAxis.supports(drawImage.asset())) {
                        CanvasTransformGizmo.drawAtPivot(graphics, state, originX, originY, drawImage.x(), drawImage.y(), drawImage.w(), drawImage.h(), drawImage.pivotX(), drawImage.pivotY(), drawImage.rotation(), drawImage.entityYaw(), drawImage.modelPitch());
                    } else {
                        CanvasElementSelectionSlot.drawAtPivot(graphics, state, originX, originY, drawImage.x(), drawImage.y(), drawImage.w(), drawImage.h(), drawImage.pivotX(), drawImage.pivotY(), drawImage.rotation());
                    }
                }
            }
        });
    }

    private static void renderCanvasExclusiveChoice(
            WidgetGroup canvasViewport,
            TabletUiState state,
            CanvasExclusiveChoice ec
    ) {
        WidgetGroup ecLayer = new WidgetGroup(0, 0, canvasViewport.getSizeWidth(), canvasViewport.getSizeHeight()) {
            @Override
            public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
                super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
                CanvasExclusiveChoice drawEc = CanvasLayerMutations.effectiveCanvasExclusiveChoice(state, ec);
                int originX = getPositionX();
                int originY = getPositionY();
                CanvasElementGeometry.Box box = CanvasElementGeometry.screenBoxAtPivot(state, drawEc.x(), drawEc.y(), drawEc.w(), drawEc.h(), 0, 0, drawEc.rotation());
                int w = box.width();
                int h = box.height();
                graphics.pose().pushPose();
                graphics.pose().translate(originX + box.centerX(), originY + box.centerY(), 0.0f);
                graphics.pose().mulPose(new Quaternionf().rotationXYZ(0.0f, 0.0f, (float) Math.toRadians(drawEc.rotation())));
                String bg = QuestDisplay.normalizeQuestBackground(drawEc.background());
                if (!QuestDisplay.DEFAULT_QUEST_BACKGROUND.equals(bg)) {
                    IGuiTexture bgTexture = chapterBackgroundTexture(bg, false);
                    if (bgTexture != null) {
                        bgTexture.draw(graphics, mouseX, mouseY, -box.left(), -box.top(), w, h);
                    }
                } else {
                    EXCLUSIVE_CHOICE_TEXTURE.draw(graphics, mouseX, mouseY, -box.left(), -box.top(), w, h);
                }
                graphics.pose().popPose();
                if (state.root.canEdit && CanvasSelectionActions.isExclusiveChoiceSelected(state, drawEc.id())) {
                    if (CanvasSelectionActions.totalCanvasSelectionCount(state) > 1) {
                        CanvasElementSelectionSlot.drawFillAndOutlineAtPivot(graphics, state, originX, originY, drawEc.x(), drawEc.y(), drawEc.w(), drawEc.h(), 0, 0, drawEc.rotation());
                    } else {
                        CanvasElementSelectionSlot.drawResizeOnlyAtPivot(graphics, state, originX, originY, drawEc.x(), drawEc.y(), drawEc.w(), drawEc.h(), 0, 0, drawEc.rotation());
                    }
                }
            }
        };
        canvasViewport.addWidget(ecLayer);
    }
}
