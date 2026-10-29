package com.abo47.questsandstuff.client.tablet.quest.canvas.blueprint;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joml.Quaternionf;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.GuiGraphics;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;

import com.abo47.questsandstuff.client.tablet.quest.canvas.geometry.CanvasGeometry;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.CanvasPoint;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.CanvasImageLayerRenderer;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.geometry.CanvasElementGeometry;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.geometry.CanvasLayerOrdering;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.card.QuestCardBackgroundRenderer;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.card.QuestMiniCardRenderer;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.connection.ConnectionRenderer;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.text.CanvasTextRenderer;
import com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory;
import com.abo47.questsandstuff.client.tablet.theme.tokens.ThemeColors;
import com.abo47.questsandstuff.quest.editor.blueprint.CanvasBlueprint;
import com.abo47.questsandstuff.quest.model.QuestDefinition;
import com.abo47.questsandstuff.quest.model.QuestDisplay;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextStyleSpan;

import static com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory.withAlpha;
import static com.abo47.questsandstuff.client.tablet.ui.factory.UiFactory.chapterBackgroundTexture;

final class CanvasBlueprintMiniPainter {
    private static final int GRID_CONNECTION_STEP = 16;

    private CanvasBlueprintMiniPainter() {
    }

    static void drawPreview(GuiGraphics graphics, int mouseX, int mouseY, int x, int y, int w, int h, CanvasBlueprint blueprint, Set<String> highlightedQuestIds, Set<String> highlightedConnectionKeys, float partialTicks) {
        if (blueprint == null || blueprint.isEmpty()) {
            drawCenteredTextPlaceholder(graphics, x, y, w, h);
            return;
        }
        CanvasBlueprintMiniGeometry.BlueprintBounds bounds = CanvasBlueprintMiniGeometry.bounds(blueprint);
        int pad = 2;
        int drawW = Math.max(1, w - pad * 2);
        int drawH = Math.max(1, h - pad * 2);
        float scale = Math.min(drawW / (float) bounds.width(), drawH / (float) bounds.height());
        scale = Math.max(0.01f, scale);
        int offsetX = x + pad + Math.max(0, Math.round((drawW - bounds.width() * scale) / 2.0f));
        int offsetY = y + pad + Math.max(0, Math.round((drawH - bounds.height() * scale) / 2.0f));
        drawBlueprint(
                graphics,
                mouseX,
                mouseY,
                Math.round(offsetX - bounds.minX() * scale),
                Math.round(offsetY - bounds.minY() * scale),
                scale,
                blueprint,
                highlightedQuestIds,
                highlightedConnectionKeys,
                partialTicks,
                255
        );
    }

    static void drawBlueprint(GuiGraphics graphics, int mouseX, int mouseY, int x, int y, float scale, CanvasBlueprint blueprint, float partialTicks, int alpha) {
        drawBlueprint(graphics, mouseX, mouseY, x, y, scale, blueprint, Set.of(), Set.of(), partialTicks, alpha);
    }

    private static void drawBlueprint(GuiGraphics graphics, int mouseX, int mouseY, int x, int y, float scale, CanvasBlueprint blueprint, Set<String> highlightedQuestIds, Set<String> highlightedConnectionKeys, float partialTicks, int alpha) {
        int safeAlpha = Math.max(0, Math.min(255, alpha));
        if (safeAlpha <= 0 || scale <= 0.0f) {
            return;
        }
        Map<String, CanvasBlueprintMiniGeometry.BlueprintRect> questBoxes = CanvasBlueprintMiniGeometry.questBoxes(blueprint);
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0.0f);
        graphics.pose().scale(scale, scale, 1.0f);
        drawConnections(graphics, blueprint, questBoxes, highlightedConnectionKeys, safeAlpha);
        for (String key : CanvasBlueprintMiniGeometry.layerOrder(blueprint)) {
            if (key.startsWith(CanvasLayerOrdering.IMAGE_PREFIX)) {
                CanvasImageLayer image = CanvasBlueprintMiniGeometry.imageById(blueprint, key.substring(CanvasLayerOrdering.IMAGE_PREFIX.length()));
                if (image != null) {
                    drawImage(graphics, mouseX, mouseY, blueprint, image, safeAlpha);
                }
                continue;
            }
            if (key.startsWith(CanvasLayerOrdering.TEXT_PREFIX)) {
                CanvasTextLayer text = CanvasBlueprintMiniGeometry.textById(blueprint, key.substring(CanvasLayerOrdering.TEXT_PREFIX.length()));
                if (text != null) {
                    drawText(graphics, blueprint, text, safeAlpha);
                }
                continue;
            }
            if (key.startsWith(CanvasLayerOrdering.QUEST_PREFIX)) {
                CanvasBlueprint.QuestEntry quest = CanvasBlueprintMiniGeometry.questById(blueprint, key.substring(CanvasLayerOrdering.QUEST_PREFIX.length()));
                if (quest != null) {
                    CanvasBlueprintMiniGeometry.BlueprintRect rect = questBoxes.get(quest.sourceId());
                    if (rect != null) {
                        drawQuest(graphics, mouseX, mouseY, quest, rect, highlightedQuestIds.contains(quest.sourceId()), partialTicks, safeAlpha);
                    }
                }
                continue;
            }
            if (key.startsWith(CanvasLayerOrdering.EXCLUSIVE_CHOICE_PREFIX)) {
                CanvasBlueprint.ExclusiveChoiceEntry ec = CanvasBlueprintMiniGeometry.exclusiveChoiceById(blueprint, key.substring(CanvasLayerOrdering.EXCLUSIVE_CHOICE_PREFIX.length()));
                if (ec != null) {
                    CanvasBlueprintMiniGeometry.BlueprintRect rect = questBoxes.get(ec.sourceId());
                    if (rect != null) {
                        drawExclusiveChoice(graphics, mouseX, mouseY, ec, rect, highlightedQuestIds.contains(ec.sourceId()), safeAlpha);
                    }
                }
            }
        }
        graphics.pose().popPose();
    }

    private static void drawConnections(GuiGraphics graphics, CanvasBlueprint blueprint, Map<String, CanvasBlueprintMiniGeometry.BlueprintRect> questBoxes, Set<String> highlightedConnectionKeys, int alpha) {
        int connectionAlpha = Math.min(alpha, 210);
        for (CanvasBlueprint.QuestEntry target : blueprint.quests()) {
            drawEntryConnections(graphics, questBoxes, highlightedConnectionKeys, connectionAlpha, target.sourceId(), target.definition(), alpha);
        }
        for (CanvasBlueprint.ExclusiveChoiceEntry target : blueprint.exclusiveChoices()) {
            drawExclusiveChoiceConnections(graphics, questBoxes, highlightedConnectionKeys, connectionAlpha, target, alpha);
        }
    }

    private static void drawEntryConnections(GuiGraphics graphics, Map<String, CanvasBlueprintMiniGeometry.BlueprintRect> questBoxes, Set<String> highlightedConnectionKeys, int connectionAlpha, String sourceId, QuestDefinition definition, int alpha) {
        if (definition == null || !definition.settings().showPrerequisiteArrow()) {
            return;
        }
        CanvasBlueprintMiniGeometry.BlueprintRect targetBox = questBoxes.get(sourceId);
        if (targetBox == null) {
            return;
        }
        for (String prerequisiteId : definition.prerequisites()) {
            CanvasBlueprintMiniGeometry.BlueprintRect sourceBox = questBoxes.get(prerequisiteId);
            if (sourceBox == null) {
                continue;
            }
            boolean direct = !"grid".equals(definition.connectionModes().get(prerequisiteId));
            boolean highlighted = highlightedConnectionKeys.contains(connectionKey(prerequisiteId, sourceId));
            int color = highlighted ? ThemeColors.BORDER_ACCENT : definition.connectionColors().getOrDefault(prerequisiteId, ThemeColors.TEXT_SECONDARY);
            int drawAlpha = highlighted ? Math.min(255, alpha) : connectionAlpha;
            String texture = definition.connectionTextures().getOrDefault(prerequisiteId, "");
            List<CanvasPoint> path = connectionPath(sourceBox, targetBox, direct);
            if (texture.isBlank()) {
                ConnectionRenderer.drawStaticChevrons(graphics, path, color, drawAlpha, -4096, -4096, 8192, 8192);
            } else {
                ConnectionRenderer.drawTexturedChevrons(graphics, path, color, drawAlpha, 1.0f, texture, -4096, -4096, 8192, 8192);
            }
        }
    }

    private static void drawExclusiveChoiceConnections(GuiGraphics graphics, Map<String, CanvasBlueprintMiniGeometry.BlueprintRect> questBoxes, Set<String> highlightedConnectionKeys, int connectionAlpha, CanvasBlueprint.ExclusiveChoiceEntry target, int alpha) {
        CanvasBlueprintMiniGeometry.BlueprintRect targetBox = questBoxes.get(target.sourceId());
        if (targetBox == null) {
            return;
        }
        for (String prerequisiteId : target.prerequisites()) {
            CanvasBlueprintMiniGeometry.BlueprintRect sourceBox = questBoxes.get(prerequisiteId);
            if (sourceBox == null) {
                continue;
            }
            drawOneExclusiveChoiceConnection(graphics, highlightedConnectionKeys, connectionAlpha, target,
                    prerequisiteId, sourceBox, targetBox, connectionKey(prerequisiteId, target.sourceId()), alpha);
        }
        for (String connectedId : target.connections()) {
            CanvasBlueprintMiniGeometry.BlueprintRect connectedBox = questBoxes.get(connectedId);
            if (connectedBox == null) {
                continue;
            }
            drawOneExclusiveChoiceConnection(graphics, highlightedConnectionKeys, connectionAlpha, target,
                    connectedId, targetBox, connectedBox, connectionKey(target.sourceId(), connectedId), alpha);
        }
    }

    private static void drawOneExclusiveChoiceConnection(GuiGraphics graphics, Set<String> highlightedConnectionKeys, int connectionAlpha, CanvasBlueprint.ExclusiveChoiceEntry target, String otherId, CanvasBlueprintMiniGeometry.BlueprintRect sourceBox, CanvasBlueprintMiniGeometry.BlueprintRect targetBox, String key, int alpha) {
        boolean highlighted = highlightedConnectionKeys.contains(key);
        int color = highlighted ? ThemeColors.BORDER_ACCENT : target.connectionColors().getOrDefault(otherId, ThemeColors.TEXT_SECONDARY);
        int drawAlpha = highlighted ? Math.min(255, alpha) : connectionAlpha;
        boolean direct = !"grid".equals(target.connectionModes().get(otherId));
        String texture = target.connectionTextures().getOrDefault(otherId, "");
        List<CanvasPoint> path = connectionPath(sourceBox, targetBox, direct);
        if (texture.isBlank()) {
            ConnectionRenderer.drawStaticChevrons(graphics, path, color, drawAlpha, -4096, -4096, 8192, 8192);
        } else {
            ConnectionRenderer.drawTexturedChevrons(graphics, path, color, drawAlpha, 1.0f, texture, -4096, -4096, 8192, 8192);
        }
    }

    private static String connectionKey(String sourceQuestId, String targetQuestId) {
        return sourceQuestId + "->" + targetQuestId;
    }

    private static List<CanvasPoint> connectionPath(CanvasBlueprintMiniGeometry.BlueprintRect source, CanvasBlueprintMiniGeometry.BlueprintRect target, boolean direct) {
        int sourceX = source.centerX();
        int sourceY = source.centerY();
        int targetX = target.centerX();
        int targetY = target.centerY();
        if (direct) {
            return List.of(new CanvasPoint(sourceX, sourceY), new CanvasPoint(targetX, targetY));
        }
        int midX = CanvasGeometry.snapValueToGrid((sourceX + targetX) / 2, GRID_CONNECTION_STEP);
        if (Math.abs(midX - sourceX) < GRID_CONNECTION_STEP / 2) {
            midX += targetX >= sourceX ? GRID_CONNECTION_STEP : -GRID_CONNECTION_STEP;
        }
        return List.of(
                new CanvasPoint(sourceX, sourceY),
                new CanvasPoint(midX, sourceY),
                new CanvasPoint(midX, targetY),
                new CanvasPoint(targetX, targetY)
        );
    }

    private static void drawQuest(GuiGraphics graphics, int mouseX, int mouseY, CanvasBlueprint.QuestEntry quest, CanvasBlueprintMiniGeometry.BlueprintRect rect, boolean highlighted, float partialTicks, int alpha) {
        QuestDefinition definition = quest.definition();
        QuestDisplay display = definition == null ? QuestDisplay.DEFAULT : definition.display();
        boolean gated = definition != null && !definition.prerequisites().isEmpty();
        QuestMiniCardRenderer.drawDisplayCard(graphics, display, gated, rect.x(), rect.y(), rect.w(), rect.h(), mouseX, mouseY, partialTicks, alpha, highlighted);
    }

    private static void drawExclusiveChoice(GuiGraphics graphics, int mouseX, int mouseY, CanvasBlueprint.ExclusiveChoiceEntry ec, CanvasBlueprintMiniGeometry.BlueprintRect rect, boolean highlighted, int alpha) {
        String bg = QuestDisplay.normalizeQuestBackground(ec.background());
        if (!QuestDisplay.DEFAULT_QUEST_BACKGROUND.equals(bg)) {
            IGuiTexture bgTexture = chapterBackgroundTexture(bg, false);
            if (bgTexture != null) {
                withShaderAlpha(alpha, () -> bgTexture.draw(graphics, mouseX, mouseY, rect.x(), rect.y(), rect.w(), rect.h()));
            }
        } else {
            withShaderAlpha(alpha, () -> QuestCardBackgroundRenderer.EXCLUSIVE_CHOICE_TEXTURE.draw(graphics, mouseX, mouseY, rect.x(), rect.y(), rect.w(), rect.h()));
        }
        if (highlighted) {
            QuestMiniCardRenderer.drawHighlightBorder(graphics, rect.x(), rect.y(), rect.w(), rect.h(), alpha);
        }
    }

    private static void drawImage(GuiGraphics graphics, int mouseX, int mouseY, CanvasBlueprint blueprint, CanvasImageLayer image, int alpha) {
        int x = image.x() - blueprint.originX();
        int y = image.y() - blueprint.originY();
        withShaderAlpha(alpha, () -> CanvasImageLayerRenderer.draw(graphics, mouseX, mouseY, image, x, y, image.w(), image.h(), image.pivotX(), image.pivotY()));
    }

    private static void drawText(GuiGraphics graphics, CanvasBlueprint blueprint, CanvasTextLayer text, int alpha) {
        CanvasTextLayer drawText = textWithAlpha(text, alpha);
        int x = drawText.x() - blueprint.originX();
        int y = drawText.y() - blueprint.originY();
        graphics.pose().pushPose();
        graphics.pose().translate(
                x + CanvasElementGeometry.defaultPivot(drawText.w()),
                y + CanvasElementGeometry.defaultPivot(drawText.h()),
                0.0f
        );
        graphics.pose().mulPose(new Quaternionf().rotationXYZ(0.0f, 0.0f, (float) Math.toRadians(drawText.rotation())));
        CanvasTextRenderer.drawTextLayer(graphics, null, drawText, drawText.w(), drawText.h(), false);
        graphics.pose().popPose();
    }

    private static CanvasTextLayer textWithAlpha(CanvasTextLayer text, int alpha) {
        if (alpha >= 255) {
            return text;
        }
        List<CanvasTextStyleSpan> spans = new ArrayList<>();
        for (CanvasTextStyleSpan span : text.spans()) {
            spans.add(new CanvasTextStyleSpan(span.start(), span.end(), span.style(), withAlpha(span.color(), alpha)));
        }
        return new CanvasTextLayer(
                text.id(),
                text.text(),
                text.x(),
                text.y(),
                text.w(),
                text.h(),
                text.rotation(),
                text.align(),
                text.style(),
                withAlpha(text.color(), alpha),
                text.fontSize(),
                spans
        );
    }

    private static void withShaderAlpha(int alpha, Runnable draw) {
        int safeAlpha = Math.max(0, Math.min(255, alpha));
        if (safeAlpha <= 0) {
            return;
        }
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, safeAlpha / 255.0f);
        try {
            draw.run();
        } finally {
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        }
    }

    private static void drawCenteredTextPlaceholder(GuiGraphics graphics, int x, int y, int w, int h) {
        int cx = x + w / 2;
        int cy = y + h / 2;
        SurfaceFactory.fill(withAlpha(ThemeColors.TEXT_MUTED, 100)).draw(graphics, 0, 0, cx - 12, cy - 1, 24, 2);
    }
}
