package com.abolovesblue.questsandstuff.client.tablet.quest.canvas.render.geometry;

import com.abolovesblue.questsandstuff.client.tablet.quest.canvas.model.QuestCardLayout;
import com.abolovesblue.questsandstuff.quest.model.canvas.CanvasExclusiveChoice;
import com.abolovesblue.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abolovesblue.questsandstuff.quest.model.canvas.CanvasTextLayer;

public record CanvasLayerHit(QuestCardLayout quest, CanvasImageLayer image, CanvasTextLayer text, CanvasExclusiveChoice exclusiveChoice) {
    public static final CanvasLayerHit EMPTY = new CanvasLayerHit(null, null, null, null);
}
