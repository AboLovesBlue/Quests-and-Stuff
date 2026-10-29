package com.abo47.questsandstuff.client.tablet.quest.canvas.hit;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import com.abo47.questsandstuff.client.tablet.quest.canvas.render.gizmo.CanvasTransformGizmoAxis;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.gizmo.CanvasTransformGizmoHit;
import com.abo47.questsandstuff.client.tablet.quest.canvas.layer.CanvasLayerMutations;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.CanvasPoint;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.ConnectionHit;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.QuestCardLayout;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.geometry.CanvasElementGeometry;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.card.QuestElementSelectionSlot;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.geometry.CanvasLayerOrdering;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.gizmo.CanvasTransformGizmo;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.connection.ConnectionRenderer;
import com.abo47.questsandstuff.client.tablet.quest.canvas.selection.CanvasSelectionActions;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.quest.model.QuestDefinition;
import com.abo47.questsandstuff.quest.model.QuestSettings;
import com.abo47.questsandstuff.quest.model.canvas.CanvasExclusiveChoice;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;

import static com.abo47.questsandstuff.client.tablet.ui.state.TabletStateQueries.selectedChapterName;

public final class CanvasHitTester {
    private CanvasHitTester() {
    }

    public static QuestCardLayout hitTestCard(List<QuestCardLayout> cards, int x, int y) {
        for (int i = cards.size() - 1; i >= 0; i--) {
            QuestCardLayout card = cards.get(i);
            if (card.containsScreen(x, y)) {
                return card;
            }
        }
        return null;
    }

    public static CanvasImageLayer hitTestCanvasImage(TabletUiState state, int x, int y) {
        String chapter = selectedChapterName(state);
        List<CanvasImageLayer> images = orderedCanvasImages(state, chapter);
        for (int i = images.size() - 1; i >= 0; i--) {
            CanvasImageLayer image = CanvasLayerMutations.effectiveCanvasImage(state, images.get(i));
            CanvasElementGeometry.Box box = CanvasElementGeometry.screenBoxAtPivot(state, image.x(), image.y(), image.w(), image.h(), image.pivotX(), image.pivotY(), image.rotation());
            double[] local = canvasImageLocalScreenPoint(state, image, x, y);
            if (local[0] >= 0 && local[0] <= box.width() && local[1] >= 0 && local[1] <= box.height()) {
                return image;
            }
        }
        return null;
    }

    public static CanvasImageLayer hitTestSelectedCanvasImageControls(TabletUiState state, int x, int y) {
        if (state.canvas.canvasSelection.primaryImageId().isBlank()) {
            return null;
        }
        CanvasImageLayer image = state.canvas.canvasImagesByChapter.getOrDefault(selectedChapterName(state), List.of()).stream()
                .filter(entry -> entry.id().equals(state.canvas.canvasSelection.primaryImageId()))
                .findFirst()
                .orElse(null);
        if (image == null) {
            return null;
        }
        image = CanvasLayerMutations.effectiveCanvasImage(state, image);
        boolean gizmoSupported = CanvasTransformGizmoAxis.supports(image.asset());
        if (gizmoSupported && CanvasTransformGizmoHit.controlHitAtPivot(state, image.x(), image.y(), image.w(), image.h(), image.pivotX(), image.pivotY(), image.rotation(), image.entityYaw(), image.modelPitch(), x, y)) {
            return image;
        }
        if (!gizmoSupported && (isCanvasImageResizeHandleHit(state, image, x, y) || isCanvasImageRotateHandleHit(state, image, x, y))) {
            return image;
        }
        CanvasElementGeometry.Box box = CanvasElementGeometry.screenBoxAtPivot(state, image.x(), image.y(), image.w(), image.h(), image.pivotX(), image.pivotY(), image.rotation());
        double[] local = canvasImageLocalScreenPoint(state, image, x, y);
        return local[0] >= -3 && local[0] <= box.width() + 3 && local[1] >= -3 && local[1] <= box.height() + 3 ? image : null;
    }

    public static ConnectionHit hitTestConnection(TabletUiState state, List<QuestCardLayout> cards, Map<String, QuestCardLayout> byQuestId, int x, int y) {
        String chapter = selectedChapterName(state);
        int tolerance = 4;
        for (QuestCardLayout quest : cards) {
            if (!quest.tag().getBoolean(QuestSettings.SHOW_PREREQUISITE_ARROW_FIELD)) {
                continue;
            }
            ListTag prerequisites = quest.tag().getList(QuestDefinition.PREREQUISITES_FIELD, Tag.TAG_STRING);
            for (int i = 0; i < prerequisites.size(); i++) {
                String prerequisiteId = prerequisites.getString(i);
                QuestCardLayout prerequisite = byQuestId.get(prerequisiteId);
                if (prerequisite == null) {
                    continue;
                }
                if (ConnectionRenderer.isConnectionHidden(state, chapter, prerequisiteId, quest.questId(), quest.tag()) && !state.root.canEdit) {
                    continue;
                }

                List<CanvasPoint> path = ConnectionRenderer.connectionPath(
                        state,
                        0,
                        0,
                        prerequisite.centerX(),
                        prerequisite.centerY(),
                        quest.centerX(),
                        quest.centerY(),
                        ConnectionRenderer.isConnectionDirect(state, chapter, prerequisiteId, quest.questId(), quest.tag())
                );
                if (nearPath(x, y, path, tolerance)) {
                    return new ConnectionHit(prerequisiteId, quest.questId());
                }
            }
        }
        List<CanvasExclusiveChoice> ecs = state.canvas.canvasExclusiveChoicesByChapter.getOrDefault(chapter, List.of());
        for (CanvasExclusiveChoice ec : ecs) {
            CanvasExclusiveChoice drawExclusiveChoice = CanvasLayerMutations.effectiveCanvasExclusiveChoice(state, ec);
            CanvasElementGeometry.Box exclusiveChoiceBox = CanvasElementGeometry.screenBoxAtPivot(state, drawExclusiveChoice.x(), drawExclusiveChoice.y(), drawExclusiveChoice.w(), drawExclusiveChoice.h(), 0, 0, drawExclusiveChoice.rotation());
            int exclusiveChoiceCenterX = (int) Math.round(exclusiveChoiceBox.centerX() + exclusiveChoiceBox.width() / 2.0);
            int exclusiveChoiceCenterY = (int) Math.round(exclusiveChoiceBox.centerY() + exclusiveChoiceBox.height() / 2.0);
            for (String connectedQuestId : drawExclusiveChoice.connectionQuestIds()) {
                QuestCardLayout connectedQuest = byQuestId.get(connectedQuestId);
                if (connectedQuest == null) {
                    continue;
                }
                List<CanvasPoint> path = ConnectionRenderer.connectionPath(
                        state,
                        0,
                        0,
                        exclusiveChoiceCenterX,
                        exclusiveChoiceCenterY,
                        connectedQuest.centerX(),
                        connectedQuest.centerY(),
                        ConnectionRenderer.exclusiveChoiceIsConnectionDirect(state, chapter, ec.id(), connectedQuest.questId())
                );
                if (nearPath(x, y, path, tolerance)) {
                    return new ConnectionHit(ec.id(), connectedQuest.questId());
                }
            }
            for (String prerequisiteQuestId : drawExclusiveChoice.prerequisiteQuestIds()) {
                QuestCardLayout prerequisiteQuest = byQuestId.get(prerequisiteQuestId);
                if (prerequisiteQuest == null) {
                    continue;
                }
                List<CanvasPoint> path = ConnectionRenderer.connectionPath(
                        state,
                        0,
                        0,
                        prerequisiteQuest.centerX(),
                        prerequisiteQuest.centerY(),
                        exclusiveChoiceCenterX,
                        exclusiveChoiceCenterY,
                        ConnectionRenderer.exclusiveChoiceIsConnectionDirect(state, chapter, prerequisiteQuest.questId(), ec.id())
                );
                if (nearPath(x, y, path, tolerance)) {
                    return new ConnectionHit(prerequisiteQuest.questId(), ec.id());
                }
            }
        }
        return null;
    }

    public static CanvasTextLayer hitTestCanvasText(TabletUiState state, int x, int y) {
        String chapter = selectedChapterName(state);
        List<CanvasTextLayer> texts = orderedCanvasTexts(state, chapter);
        for (int i = texts.size() - 1; i >= 0; i--) {
            CanvasTextLayer text = CanvasLayerMutations.effectiveCanvasText(state, texts.get(i));
            CanvasElementGeometry.Box box = CanvasElementGeometry.screenBox(state, text.x(), text.y(), text.w(), text.h(), text.rotation());
            double[] local = canvasTextLocalScreenPoint(state, text, x, y);
            if (local[0] >= 0 && local[0] <= box.width() && local[1] >= 0 && local[1] <= box.height()) {
                return text;
            }
        }
        return null;
    }

    public static CanvasExclusiveChoice hitTestCanvasExclusiveChoice(TabletUiState state, int x, int y) {
        String chapter = selectedChapterName(state);
        List<CanvasExclusiveChoice> ecs = orderedCanvasExclusiveChoices(state, chapter);
        for (int i = ecs.size() - 1; i >= 0; i--) {
            CanvasExclusiveChoice ec = CanvasLayerMutations.effectiveCanvasExclusiveChoice(state, ecs.get(i));
            CanvasElementGeometry.Box box = CanvasElementGeometry.screenBoxAtPivot(state, ec.x(), ec.y(), ec.w(), ec.h(), 0, 0, ec.rotation());
            double[] local = canvasExclusiveChoiceLocalScreenPoint(state, ec, x, y);
            if (local[0] >= 0 && local[0] <= box.width() && local[1] >= 0 && local[1] <= box.height()) {
                return ec;
            }
        }
        return null;
    }

    public static CanvasExclusiveChoice hitTestSelectedCanvasExclusiveChoiceControls(TabletUiState state, int x, int y) {
        if (state.canvas.canvasSelection.primaryExclusiveChoiceId().isBlank()) {
            return null;
        }
        CanvasExclusiveChoice ec = state.canvas.canvasExclusiveChoicesByChapter.getOrDefault(selectedChapterName(state), List.of()).stream()
                .filter(entry -> entry.id().equals(state.canvas.canvasSelection.primaryExclusiveChoiceId()))
                .findFirst()
                .orElse(null);
        if (ec == null) {
            return null;
        }
        ec = CanvasLayerMutations.effectiveCanvasExclusiveChoice(state, ec);
        CanvasElementGeometry.Box box = CanvasElementGeometry.screenBoxAtPivot(state, ec.x(), ec.y(), ec.w(), ec.h(), 0, 0, ec.rotation());
        double[] local = canvasExclusiveChoiceLocalScreenPoint(state, ec, x, y);
        return local[0] >= -3 && local[0] <= box.width() + 3 && local[1] >= -3 && local[1] <= box.height() + 3 ? ec : null;
    }

    public static boolean isCanvasExclusiveChoiceResizeHandleHit(TabletUiState state, CanvasExclusiveChoice ec, int x, int y) {
        return QuestElementSelectionSlot.resizeHandleHitAtPivot(state, ec.x(), ec.y(), ec.w(), ec.h(), ec.pivotX(), ec.pivotY(), ec.rotation(), x, y);
    }

    public static boolean isCanvasExclusiveChoiceRotateHandleHit(TabletUiState state, CanvasExclusiveChoice ec, int x, int y) {
        if (CanvasSelectionActions.totalCanvasSelectionCount(state) <= 1) {
            return false;
        }
        return QuestElementSelectionSlot.rotateHandleHitAtPivot(state, ec.x(), ec.y(), ec.w(), ec.h(), ec.pivotX(), ec.pivotY(), ec.rotation(), x, y);
    }

    public static double[] canvasExclusiveChoiceLocalScreenPoint(TabletUiState state, CanvasExclusiveChoice ec, int x, int y) {
        CanvasElementGeometry.Box box = CanvasElementGeometry.screenBoxAtPivot(state, ec.x(), ec.y(), ec.w(), ec.h(), 0, 0, ec.rotation());
        return CanvasElementGeometry.localScreenPoint(box, ec.rotation(), x, y);
    }

    public static CanvasTextLayer hitTestSelectedCanvasTextControls(TabletUiState state, int x, int y) {
        if (state.canvas.canvasSelection.primaryTextId().isBlank()) {
            return null;
        }
        CanvasTextLayer text = state.canvas.canvasTextsByChapter.getOrDefault(selectedChapterName(state), List.of()).stream()
                .filter(entry -> entry.id().equals(state.canvas.canvasSelection.primaryTextId()))
                .findFirst()
                .orElse(null);
        if (text == null) {
            return null;
        }
        text = CanvasLayerMutations.effectiveCanvasText(state, text);
        if (isCanvasTextResizeHandleHit(state, text, x, y) || isCanvasTextRotateHandleHit(state, text, x, y)) {
            return text;
        }
        CanvasElementGeometry.Box box = CanvasElementGeometry.screenBox(state, text.x(), text.y(), text.w(), text.h(), text.rotation());
        double[] local = canvasTextLocalScreenPoint(state, text, x, y);
        return local[0] >= -3 && local[0] <= box.width() + 3 && local[1] >= -3 && local[1] <= box.height() + 3 ? text : null;
    }

    public static boolean isCanvasTextResizeHandleHit(TabletUiState state, CanvasTextLayer text, int x, int y) {
        return QuestElementSelectionSlot.resizeHandleHit(state, text.x(), text.y(), text.w(), text.h(), text.rotation(), x, y);
    }

    public static boolean isCanvasTextRotateHandleHit(TabletUiState state, CanvasTextLayer text, int x, int y) {
        return QuestElementSelectionSlot.rotateHandleHit(state, text.x(), text.y(), text.w(), text.h(), text.rotation(), x, y);
    }

    public static double[] canvasTextLocalScreenPoint(TabletUiState state, CanvasTextLayer text, int x, int y) {
        CanvasElementGeometry.Box box = CanvasElementGeometry.screenBox(state, text.x(), text.y(), text.w(), text.h(), text.rotation());
        return CanvasElementGeometry.localScreenPoint(box, text.rotation(), x, y);
    }

    public static boolean isCanvasTextOwnerHit(TabletUiState state, CanvasTextLayer text, int x, int y) {
        if (state == null || text == null) {
            return false;
        }
        CanvasTextLayer drawText = CanvasLayerMutations.effectiveCanvasText(state, text);
        CanvasElementGeometry.Box box = CanvasElementGeometry.screenBox(state, drawText.x(), drawText.y(), drawText.w(), drawText.h(), drawText.rotation());
        double[] local = canvasTextLocalScreenPoint(state, drawText, x, y);
        return local[0] >= -3 && local[0] <= box.width() + 3 && local[1] >= -3 && local[1] <= box.height() + 3;
    }

    public static boolean isCanvasImageResizeHandleHit(TabletUiState state, CanvasImageLayer image, int x, int y) {
        return QuestElementSelectionSlot.resizeHandleHitAtPivot(state, image.x(), image.y(), image.w(), image.h(), image.pivotX(), image.pivotY(), image.rotation(), x, y);
    }

    public static boolean isCanvasImageRotateHandleHit(TabletUiState state, CanvasImageLayer image, int x, int y) {
        return QuestElementSelectionSlot.rotateHandleHitAtPivot(state, image.x(), image.y(), image.w(), image.h(), image.pivotX(), image.pivotY(), image.rotation(), x, y);
    }

    public static boolean isCanvasQuestResizeHandleHit(TabletUiState state, QuestCardLayout card, int x, int y) {
        return QuestElementSelectionSlot.resizeHandleHitAtScreenRect(card.x(), card.y(), card.width(), card.height(), x, y);
    }

    public static double[] canvasImageLocalScreenPoint(TabletUiState state, CanvasImageLayer image, int x, int y) {
        CanvasElementGeometry.Box box = CanvasElementGeometry.screenBoxAtPivot(state, image.x(), image.y(), image.w(), image.h(), image.pivotX(), image.pivotY(), image.rotation());
        return CanvasElementGeometry.localScreenPoint(box, image.rotation(), x, y);
    }

    private static <T> List<T> orderedLayers(TabletUiState state, String chapter, List<T> source, java.util.function.Function<T, String> keyFunction) {
        List<T> list = new ArrayList<>(source);
        List<String> order = state.canvas.canvasLayerOrderByChapter.getOrDefault(chapter, List.of());
        Map<String, Integer> indexes = CanvasLayerOrdering.indexMap(order);
        list.sort(Comparator.comparingInt(e -> CanvasLayerOrdering.layerIndex(indexes, keyFunction.apply(e))));
        return list;
    }

    private static List<CanvasImageLayer> orderedCanvasImages(TabletUiState state, String chapter) {
        return orderedLayers(state, chapter, state.canvas.canvasImagesByChapter.getOrDefault(chapter, List.of()), image -> CanvasLayerOrdering.imageKey(image.id()));
    }

    private static List<CanvasExclusiveChoice> orderedCanvasExclusiveChoices(TabletUiState state, String chapter) {
        return orderedLayers(state, chapter, state.canvas.canvasExclusiveChoicesByChapter.getOrDefault(chapter, List.of()), ec -> CanvasLayerOrdering.exclusiveChoiceKey(ec.id()));
    }

    private static List<CanvasTextLayer> orderedCanvasTexts(TabletUiState state, String chapter) {
        return orderedLayers(state, chapter, state.canvas.canvasTextsByChapter.getOrDefault(chapter, List.of()), text -> CanvasLayerOrdering.textKey(text.id()));
    }

    private static boolean nearHorizontal(int x, int y, int lineY, int x1, int x2, int tolerance) {
        int minX = Math.min(x1, x2);
        int maxX = Math.max(x1, x2);
        return x >= minX - tolerance && x <= maxX + tolerance && Math.abs(y - lineY) <= tolerance;
    }

    private static boolean nearVertical(int x, int y, int lineX, int y1, int y2, int tolerance) {
        int minY = Math.min(y1, y2);
        int maxY = Math.max(y1, y2);
        return y >= minY - tolerance && y <= maxY + tolerance && Math.abs(x - lineX) <= tolerance;
    }

    private static boolean nearPath(int x, int y, List<CanvasPoint> path, int tolerance) {
        for (int i = 0; i + 1 < path.size(); i++) {
            CanvasPoint a = path.get(i);
            CanvasPoint b = path.get(i + 1);
            if (a.y == b.y && nearHorizontal(x, y, a.y, a.x, b.x, tolerance)) {
                return true;
            }
            if (a.x == b.x && nearVertical(x, y, a.x, a.y, b.y, tolerance)) {
                return true;
            }
            if (a.x != b.x && a.y != b.y && nearSegment(x, y, a.x, a.y, b.x, b.y, tolerance)) {
                return true;
            }
        }
        return false;
    }

    private static boolean nearSegment(int px, int py, int x1, int y1, int x2, int y2, int tolerance) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        double lenSq = dx * dx + dy * dy;
        if (lenSq <= 0.0) {
            return Math.abs(px - x1) <= tolerance && Math.abs(py - y1) <= tolerance;
        }
        double t = ((px - x1) * dx + (py - y1) * dy) / lenSq;
        t = Math.max(0.0, Math.min(1.0, t));
        double closestX = x1 + t * dx;
        double closestY = y1 + t * dy;
        double distX = px - closestX;
        double distY = py - closestY;
        return distX * distX + distY * distY <= tolerance * tolerance;
    }
}
