package com.abo47.questsandstuff.client.tablet.quest.canvas.blueprint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.abo47.questsandstuff.client.tablet.quest.canvas.geometry.CanvasGeometry;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.geometry.CanvasElementGeometry;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.geometry.CanvasLayerOrdering;
import com.abo47.questsandstuff.quest.editor.blueprint.CanvasBlueprint;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;

final class CanvasBlueprintMiniGeometry {
    private CanvasBlueprintMiniGeometry() {
    }

    static BlueprintBounds bounds(CanvasBlueprint blueprint) {
        if (blueprint == null || blueprint.isEmpty()) {
            return new BlueprintBounds(0, 0, 1, 1);
        }
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        for (CanvasBlueprint.QuestEntry quest : blueprint.quests()) {
            int x = quest.sourceX() - blueprint.originX();
            int y = quest.sourceY() - blueprint.originY();
            int w = CanvasGeometry.slotSpanForVisualSize(CanvasGeometry.visualLogicalWidth(quest.scale()));
            int h = CanvasGeometry.slotSpanForVisualSize(CanvasGeometry.visualLogicalHeight(quest.scale()));
            minX = Math.min(minX, x);
            minY = Math.min(minY, y);
            maxX = Math.max(maxX, x + w);
            maxY = Math.max(maxY, y + h);
        }
        for (CanvasImageLayer image : blueprint.images()) {
            int[] rotated = CanvasElementGeometry.logicalBoundsAtPivot(
                    image.x() - blueprint.originX(),
                    image.y() - blueprint.originY(),
                    image.w(),
                    image.h(),
                    image.pivotX(),
                    image.pivotY(),
                    image.rotation()
            );
            minX = Math.min(minX, rotated[0]);
            minY = Math.min(minY, rotated[1]);
            maxX = Math.max(maxX, rotated[2]);
            maxY = Math.max(maxY, rotated[3]);
        }
        for (CanvasTextLayer text : blueprint.texts()) {
            int[] rotated = CanvasElementGeometry.logicalBounds(text.x() - blueprint.originX(), text.y() - blueprint.originY(), text.w(), text.h(), text.rotation());
            minX = Math.min(minX, rotated[0]);
            minY = Math.min(minY, rotated[1]);
            maxX = Math.max(maxX, rotated[2]);
            maxY = Math.max(maxY, rotated[3]);
        }
        for (CanvasBlueprint.ExclusiveChoiceEntry ec : blueprint.exclusiveChoices()) {
            BlueprintRect rect = exclusiveChoiceRect(ec.sourceId(), ec.sourceX(), ec.sourceY(), ec.sourceW(), ec.sourceH(), blueprint.originX(), blueprint.originY());
            minX = Math.min(minX, rect.x());
            minY = Math.min(minY, rect.y());
            maxX = Math.max(maxX, rect.x() + rect.w());
            maxY = Math.max(maxY, rect.y() + rect.h());
        }
        if (minX == Integer.MAX_VALUE) {
            return new BlueprintBounds(0, 0, 1, 1);
        }
        return new BlueprintBounds(minX, minY, Math.max(1, maxX - minX), Math.max(1, maxY - minY));
    }

    static Map<String, BlueprintRect> questBoxes(CanvasBlueprint blueprint) {
        Map<String, BlueprintRect> questBoxes = new HashMap<>();
        for (CanvasBlueprint.QuestEntry quest : blueprint.quests()) {
            BlueprintRect rect = questRect(quest.sourceId(), quest.sourceX(), quest.sourceY(), quest.scale(), blueprint.originX(), blueprint.originY());
            questBoxes.put(quest.sourceId(), rect);
        }
        for (CanvasBlueprint.ExclusiveChoiceEntry ec : blueprint.exclusiveChoices()) {
            BlueprintRect rect = exclusiveChoiceRect(ec.sourceId(), ec.sourceX(), ec.sourceY(), ec.sourceW(), ec.sourceH(), blueprint.originX(), blueprint.originY());
            questBoxes.put(ec.sourceId(), rect);
        }
        return questBoxes;
    }

    private static BlueprintRect questRect(String id, int sourceX, int sourceY, float scale, int originX, int originY) {
        int visualW = CanvasGeometry.visualLogicalWidth(scale);
        int visualH = CanvasGeometry.visualLogicalHeight(scale);
        int slotW = CanvasGeometry.slotSpanForVisualSize(visualW);
        int slotH = CanvasGeometry.slotSpanForVisualSize(visualH);
        int x = sourceX - originX + CanvasGeometry.visualInsetForSlot(slotW, visualW);
        int y = sourceY - originY + CanvasGeometry.visualInsetForSlot(slotH, visualH);
        return new BlueprintRect(x, y, visualW, visualH);
    }

    private static BlueprintRect exclusiveChoiceRect(String id, int sourceX, int sourceY, int w, int h, int originX, int originY) {
        return new BlueprintRect(sourceX - originX, sourceY - originY, w, h);
    }

    static List<String> layerOrder(CanvasBlueprint blueprint) {
        if (blueprint.layerOrder().isEmpty()) {
            return defaultOrder(blueprint);
        }
        return blueprint.layerOrder();
    }

    private static List<String> defaultOrder(CanvasBlueprint blueprint) {
        List<String> order = new ArrayList<>();
        for (CanvasImageLayer image : blueprint.images()) {
            order.add(CanvasLayerOrdering.imageKey(image.id()));
        }
        for (CanvasTextLayer text : blueprint.texts()) {
            order.add(CanvasLayerOrdering.textKey(text.id()));
        }
        for (CanvasBlueprint.QuestEntry quest : blueprint.quests()) {
            order.add(CanvasLayerOrdering.questKey(quest.sourceId()));
        }
        for (CanvasBlueprint.ExclusiveChoiceEntry ec : blueprint.exclusiveChoices()) {
            order.add(CanvasLayerOrdering.exclusiveChoiceKey(ec.sourceId()));
        }
        return order;
    }

    static CanvasBlueprint.QuestEntry questById(CanvasBlueprint blueprint, String id) {
        for (CanvasBlueprint.QuestEntry quest : blueprint.quests()) {
            if (quest.sourceId().equals(id)) {
                return quest;
            }
        }
        return null;
    }

    static CanvasImageLayer imageById(CanvasBlueprint blueprint, String id) {
        for (CanvasImageLayer image : blueprint.images()) {
            if (image.id().equals(id)) {
                return image;
            }
        }
        return null;
    }

    static CanvasTextLayer textById(CanvasBlueprint blueprint, String id) {
        for (CanvasTextLayer text : blueprint.texts()) {
            if (text.id().equals(id)) {
                return text;
            }
        }
        return null;
    }

    static CanvasBlueprint.ExclusiveChoiceEntry exclusiveChoiceById(CanvasBlueprint blueprint, String id) {
        for (CanvasBlueprint.ExclusiveChoiceEntry entry : blueprint.exclusiveChoices()) {
            if (entry.sourceId().equals(id)) {
                return entry;
            }
        }
        return null;
    }

    public record BlueprintBounds(int minX, int minY, int width, int height) {
    }

    record BlueprintRect(int x, int y, int w, int h) {
        int centerX() {
            return x + w / 2;
        }

        int centerY() {
            return y + h / 2;
        }
    }
}
