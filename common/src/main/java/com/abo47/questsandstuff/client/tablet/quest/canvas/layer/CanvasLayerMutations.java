package com.abo47.questsandstuff.client.tablet.quest.canvas.layer;

import java.util.List;
import java.util.function.UnaryOperator;

import com.abo47.questsandstuff.client.tablet.quest.canvas.layer.CanvasElementStore;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.geometry.CanvasLayerOrdering;
import com.abo47.questsandstuff.client.tablet.quest.canvas.selection.CanvasSelectionActions;
import com.abo47.questsandstuff.client.tablet.state.UiState;
import com.abo47.questsandstuff.quest.model.canvas.CanvasExclusiveChoice;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;
import com.abo47.questsandstuff.quest.model.connection.QuestConnectionMetadata;

public final class CanvasLayerMutations {
    private CanvasLayerMutations() {
    }

    public static void moveQuestLayer(UiState state, String chapter, String questId, boolean front) {
        CanvasLayerOrdering.moveQuestLayer(state, chapter, questId, front);
        CanvasElementStore.persistLayerOrder(state, chapter);
    }

    public static void moveImageLayer(UiState state, String chapter, String imageId, boolean front) {
        CanvasLayerOrdering.moveImageLayer(state, chapter, imageId, front);
        CanvasElementStore.persistLayerOrder(state, chapter);
    }

    public static void moveTextLayer(UiState state, String chapter, String textId, boolean front) {
        CanvasLayerOrdering.moveTextLayer(state, chapter, textId, front);
        CanvasElementStore.persistLayerOrder(state, chapter);
    }

    public static void moveConnectionLayer(UiState state, String chapter, String sourceQuestId, String targetQuestId, boolean front) {
        CanvasLayerOrdering.moveConnectionLayer(state, chapter, QuestConnectionMetadata.connectionKey(sourceQuestId, targetQuestId), front);
        CanvasElementStore.persistLayerOrder(state, chapter);
    }

    public static void moveCanvasLayers(UiState state, String chapter, List<String> layerKeys, boolean front) {
        CanvasLayerOrdering.moveLayers(state, chapter, layerKeys, front);
        CanvasElementStore.persistLayerOrder(state, chapter);
    }

    public static void moveExclusiveChoiceLayer(UiState state, String chapter, String exclusiveChoiceId, boolean front) {
        CanvasLayerOrdering.moveExclusiveChoiceLayer(state, chapter, exclusiveChoiceId, front);
        CanvasElementStore.persistLayerOrder(state, chapter);
    }

    public static void putCanvasExclusiveChoice(UiState state, String chapter, CanvasExclusiveChoice ec) {
        CanvasElementStore.putCanvasExclusiveChoice(state, chapter, ec);
    }

    public static void putCanvasExclusiveChoice(UiState state, String chapter, CanvasExclusiveChoice ec, boolean syncServer) {
        CanvasElementStore.putCanvasExclusiveChoice(state, chapter, ec, syncServer);
    }

    public static void putCanvasExclusiveChoices(UiState state, String chapter, List<CanvasExclusiveChoice> ecs, boolean syncServer) {
        CanvasElementStore.putCanvasExclusiveChoices(state, chapter, ecs, syncServer);
    }

    public static boolean removeCanvasExclusiveChoice(UiState state, String chapter, String exclusiveChoiceId) {
        return CanvasElementStore.removeCanvasExclusiveChoice(state, chapter, exclusiveChoiceId);
    }

    public static CanvasExclusiveChoice findCanvasExclusiveChoice(UiState state, String chapter, String exclusiveChoiceId) {
        return CanvasElementStore.findCanvasExclusiveChoice(state, chapter, exclusiveChoiceId);
    }

    public static void putCanvasImage(UiState state, String chapter, CanvasImageLayer image) {
        CanvasElementStore.putCanvasImage(state, chapter, image);
    }

    public static void putCanvasImage(UiState state, String chapter, CanvasImageLayer image, boolean syncServer) {
        CanvasElementStore.putCanvasImage(state, chapter, image, syncServer);
    }

    public static boolean removeCanvasImage(UiState state, String chapter, String imageId) {
        return CanvasElementStore.removeCanvasImage(state, chapter, imageId);
    }

    public static void putCanvasText(UiState state, String chapter, CanvasTextLayer text) {
        CanvasElementStore.putCanvasText(state, chapter, text);
    }

    public static void putCanvasText(UiState state, String chapter, CanvasTextLayer text, boolean syncServer) {
        CanvasElementStore.putCanvasText(state, chapter, text, syncServer);
    }

    public static boolean removeCanvasText(UiState state, String chapter, String textId) {
        return CanvasElementStore.removeCanvasText(state, chapter, textId);
    }

    public static CanvasTextLayer findCanvasText(UiState state, String chapter, String textId) {
        return CanvasElementStore.findCanvasText(state, chapter, textId);
    }

    public static CanvasImageLayer findCanvasImage(UiState state, String chapter, String imageId) {
        return CanvasElementStore.findCanvasImage(state, chapter, imageId);
    }

    public static void updateCanvasText(UiState state, String chapter, String textId, UnaryOperator<CanvasTextLayer> updater) {
        CanvasElementStore.updateCanvasText(state, chapter, textId, updater);
    }

    public static void persistCanvasImage(UiState state, String chapter, String imageId) {
        CanvasElementStore.persistCanvasImage(state, chapter, imageId);
    }

    public static void persistCanvasText(UiState state, String chapter, String textId) {
        CanvasElementStore.persistCanvasText(state, chapter, textId);
    }

    public static void persistCanvasExclusiveChoice(UiState state, String chapter, String exclusiveChoiceId) {
        CanvasElementStore.persistCanvasExclusiveChoice(state, chapter, exclusiveChoiceId);
    }

    public static CanvasImageLayer effectiveCanvasImage(UiState state, CanvasImageLayer image) {
        if (state == null || image == null) {
            return image;
        }
        return state.canvas.transientCanvasImages.getOrDefault(image.id(), image);
    }

    public static CanvasTextLayer effectiveCanvasText(UiState state, CanvasTextLayer text) {
        if (state == null || text == null) {
            return text;
        }
        return state.canvas.transientCanvasTexts.getOrDefault(text.id(), text);
    }

    public static CanvasExclusiveChoice effectiveCanvasExclusiveChoice(UiState state, CanvasExclusiveChoice ec) {
        if (state == null || ec == null) {
            return ec;
        }
        return state.canvas.transientCanvasExclusiveChoices.getOrDefault(ec.id(), ec);
    }

    public static CanvasImageLayer effectiveQuestDetailsImage(UiState state, CanvasImageLayer image) {
        if (state == null || image == null) {
            return image;
        }
        return state.questDetails.questDetailsTransientImages.getOrDefault(image.id(), image);
    }

    public static CanvasTextLayer effectiveQuestDetailsText(UiState state, CanvasTextLayer text) {
        if (state == null || text == null) {
            return text;
        }
        return state.questDetails.questDetailsTransientTexts.getOrDefault(text.id(), text);
    }

    public static void putTransientCanvasImage(UiState state, CanvasImageLayer image) {
        if (state == null || image == null || image.id().isBlank()) {
            return;
        }
        state.canvas.transientCanvasImages.put(image.id(), image);
    }

    public static void putTransientCanvasText(UiState state, CanvasTextLayer text) {
        if (state == null || text == null || text.id().isBlank()) {
            return;
        }
        state.canvas.transientCanvasTexts.put(text.id(), text);
    }

    public static void putTransientCanvasExclusiveChoice(UiState state, CanvasExclusiveChoice ec) {
        if (state == null || ec == null || ec.id().isBlank()) {
            return;
        }
        state.canvas.transientCanvasExclusiveChoices.put(ec.id(), ec);
    }

    public static void putTransientQuestDetailsImage(UiState state, CanvasImageLayer image) {
        if (state == null || image == null || image.id().isBlank()) {
            return;
        }
        state.questDetails.questDetailsTransientImages.put(image.id(), image);
    }

    public static void putTransientQuestDetailsText(UiState state, CanvasTextLayer text) {
        if (state == null || text == null || text.id().isBlank()) {
            return;
        }
        state.questDetails.questDetailsTransientTexts.put(text.id(), text);
    }

    public static boolean commitTransientCanvasImage(UiState state, String chapter, String imageId) {
        if (state == null || chapter == null || chapter.isBlank() || imageId == null || imageId.isBlank()) {
            return false;
        }
        CanvasImageLayer preview = state.canvas.transientCanvasImages.remove(imageId);
        if (preview == null) {
            return false;
        }
        CanvasElementStore.putCanvasImage(state, chapter, preview, false);
        return true;
    }

    public static boolean commitTransientCanvasText(UiState state, String chapter, String textId) {
        if (state == null || chapter == null || chapter.isBlank() || textId == null || textId.isBlank()) {
            return false;
        }
        CanvasTextLayer preview = state.canvas.transientCanvasTexts.remove(textId);
        if (preview == null) {
            return false;
        }
        CanvasElementStore.putCanvasText(state, chapter, preview, false);
        return true;
    }

    public static boolean commitTransientCanvasExclusiveChoice(UiState state, String chapter, String exclusiveChoiceId) {
        if (state == null || chapter == null || chapter.isBlank() || exclusiveChoiceId == null || exclusiveChoiceId.isBlank()) {
            return false;
        }
        CanvasExclusiveChoice preview = state.canvas.transientCanvasExclusiveChoices.remove(exclusiveChoiceId);
        if (preview == null) {
            return false;
        }
        CanvasElementStore.putCanvasExclusiveChoice(state, chapter, preview, false);
        return true;
    }

    public static void commitSelectedTransientCanvasLayers(UiState state, String chapter) {
        for (String imageId : CanvasSelectionActions.selectedImageIds(state)) {
            commitTransientCanvasImage(state, chapter, imageId);
        }
        for (String textId : CanvasSelectionActions.selectedTextIds(state)) {
            commitTransientCanvasText(state, chapter, textId);
        }
        for (String exclusiveChoiceId : CanvasSelectionActions.selectedExclusiveChoiceIds(state)) {
            commitTransientCanvasExclusiveChoice(state, chapter, exclusiveChoiceId);
        }
    }
}
