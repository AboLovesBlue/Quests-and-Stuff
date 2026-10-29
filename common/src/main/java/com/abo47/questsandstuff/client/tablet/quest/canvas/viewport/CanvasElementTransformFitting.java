package com.abo47.questsandstuff.client.tablet.quest.canvas.viewport;

import java.util.List;
import java.util.Set;

import com.abo47.questsandstuff.client.tablet.quest.canvas.geometry.CanvasGeometry;
import com.abo47.questsandstuff.client.tablet.quest.canvas.geometry.CanvasGridFitController;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.CanvasPoint;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.QuestCardLayout;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.geometry.CanvasElementGeometry;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.gizmo.CanvasTransformGizmoAxis;
import com.abo47.questsandstuff.client.tablet.quest.canvas.snap.CanvasSnapEngine;
import com.abo47.questsandstuff.client.tablet.quest.canvas.transform.CanvasTransformEngine;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.ui.TabletModifierKeys;
import com.abo47.questsandstuff.quest.model.canvas.CanvasExclusiveChoice;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;

final class CanvasElementTransformFitting {
    private CanvasElementTransformFitting() {
    }

    static CanvasImageLayer applySmartSnapToImage(TabletUiState state, CanvasImageLayer image, List<QuestCardLayout> cards, String chapter) {
        CanvasSnapEngine.SnapResult snap = CanvasSmartSnapper.snap(
                state,
                CanvasSmartSnapper.boundsForImage(state, image),
                cards,
                chapter,
                Set.of(),
                Set.of(image.id()),
                Set.of()
        );
        if (!snap.hasOffset()) {
            return image;
        }
        CanvasPoint offset = constrainedSnapOffset(state, snap.offsetX(), snap.offsetY(), image.rotation());
        int offsetX = offset.x;
        int offsetY = offset.y;
        if (!CanvasTransformGizmoAxis.AXIS_MOVE_FREE.equals(CanvasTransformGizmoAxis.moveAxisOrFree(state.canvas.canvasImageTransformAxis))) {
            state.canvas.snapGuideXVisible = state.canvas.snapGuideXVisible && offsetX != 0;
            state.canvas.snapGuideYVisible = state.canvas.snapGuideYVisible && offsetY != 0;
        }
        int requestedX = image.x() + offsetX;
        int requestedY = image.y() + offsetY;
        var clamped = CanvasGeometry.clampRotatedAnchorToCanvas(state, requestedX, requestedY, image.w(), image.h(), image.pivotX(), image.pivotY(), image.rotation());
        if (clamped.x != requestedX || clamped.y != requestedY) {
            clearSnapGuides(state);
        }
        return image.moveTo(clamped.x, clamped.y);
    }

    static CanvasExclusiveChoice applySmartSnapToExclusiveChoice(TabletUiState state, CanvasExclusiveChoice ec, List<QuestCardLayout> cards, String chapter) {
        CanvasSnapEngine.SnapResult snap = CanvasSmartSnapper.snap(
                state,
                CanvasSmartSnapper.boundsForExclusiveChoice(state, ec),
                cards,
                chapter,
                Set.of(),
                Set.of(),
                Set.of(),
                Set.of(ec.id())
        );
        if (!snap.hasOffset()) {
            return ec;
        }
        int requestedX = ec.x() + snap.offsetX();
        int requestedY = ec.y() + snap.offsetY();
        var clamped = CanvasGeometry.clampRotatedAnchorToCanvas(state, requestedX, requestedY, ec.w(), ec.h(), 0, 0, ec.rotation());
        if (clamped.x != requestedX || clamped.y != requestedY) {
            clearSnapGuides(state);
        }
        return ec.moveTo(clamped.x, clamped.y);
    }

    static CanvasTextLayer applySmartSnapToText(TabletUiState state, CanvasTextLayer text, List<QuestCardLayout> cards, String chapter) {
        CanvasSnapEngine.SnapResult snap = CanvasSmartSnapper.snap(
                state,
                CanvasSmartSnapper.boundsForText(state, text),
                cards,
                chapter,
                Set.of(),
                Set.of(),
                Set.of(text.id())
        );
        if (!snap.hasOffset()) {
            return text;
        }
        int requestedX = text.x() + snap.offsetX();
        int requestedY = text.y() + snap.offsetY();
        var clamped = CanvasGeometry.clampRotatedAnchorToCanvas(state, requestedX, requestedY, text.w(), text.h(), CanvasElementGeometry.defaultPivot(text.w()), CanvasElementGeometry.defaultPivot(text.h()), text.rotation());
        if (clamped.x != requestedX || clamped.y != requestedY) {
            clearSnapGuides(state);
        }
        return text.moveTo(clamped.x, clamped.y);
    }

    static CanvasPoint constrainedSnapOffset(TabletUiState state, int offsetX, int offsetY, int rotation) {
        String axis = CanvasTransformGizmoAxis.moveAxisOrFree(state.canvas.canvasImageTransformAxis);
        if (CanvasTransformGizmoAxis.AXIS_MOVE_FREE.equals(axis)) {
            return new CanvasPoint(offsetX, offsetY);
        }
        return CanvasTransformEngine.axisDelta(offsetX, offsetY, rotation, axis, new CanvasTransformEngine.SnapSettings(CanvasGeometry.gridSize(state), false, false));
    }

    static void clearSnapGuides(TabletUiState state) {
        state.canvas.snapGuideXVisible = false;
        state.canvas.snapGuideYVisible = false;
    }

    static CanvasImageLayer fittedImageIfGridLocked(TabletUiState state, CanvasImageLayer image) {
        return state.canvas.gridSnapLocked ? CanvasGridFitController.fittedImage(state, image) : image;
    }

    static CanvasTextLayer fittedTextIfGridLocked(TabletUiState state, CanvasTextLayer text) {
        return state.canvas.gridSnapLocked ? CanvasGridFitController.fittedText(state, text) : text;
    }

    static CanvasExclusiveChoice fittedExclusiveChoiceIfGridLocked(TabletUiState state, CanvasExclusiveChoice ec) {
        return state.canvas.gridSnapLocked ? CanvasGridFitController.fittedExclusiveChoice(state, ec) : ec;
    }

    static CanvasImageLayer fitAndClampImage(TabletUiState state, CanvasImageLayer image) {
        CanvasImageLayer fitted = fittedImageIfGridLocked(state, image);
        CanvasPoint clamped = CanvasGeometry.clampRotatedAnchorToCanvas(state, fitted.x(), fitted.y(), fitted.w(), fitted.h(), fitted.pivotX(), fitted.pivotY(), fitted.rotation());
        return fitted.moveTo(clamped.x, clamped.y);
    }

    static CanvasTextLayer fitAndClampText(TabletUiState state, CanvasTextLayer text) {
        CanvasTextLayer fitted = fittedTextIfGridLocked(state, text);
        CanvasPoint clamped = CanvasGeometry.clampRotatedAnchorToCanvas(state, fitted.x(), fitted.y(), fitted.w(), fitted.h(), CanvasElementGeometry.defaultPivot(fitted.w()), CanvasElementGeometry.defaultPivot(fitted.h()), fitted.rotation());
        return fitted.moveTo(clamped.x, clamped.y);
    }

    static CanvasExclusiveChoice fitAndClampExclusiveChoice(TabletUiState state, CanvasExclusiveChoice ec) {
        CanvasExclusiveChoice fitted = fittedExclusiveChoiceIfGridLocked(state, ec);
        CanvasPoint clamped = CanvasGeometry.clampRotatedAnchorToCanvas(state, fitted.x(), fitted.y(), fitted.w(), fitted.h(), CanvasElementGeometry.defaultPivot(fitted.w()), CanvasElementGeometry.defaultPivot(fitted.h()), fitted.rotation());
        return fitted.moveTo(clamped.x, clamped.y);
    }

    static CanvasImageLayer clampRotationPreviewImage(TabletUiState state, CanvasImageLayer image) {
        CanvasImageLayer preview = shouldFitRotatedPreview(state, image.rotation())
                ? fittedImageIfGridLocked(state, image)
                : image;
        CanvasPoint clamped = CanvasGeometry.clampRotatedAnchorToCanvas(state, preview.x(), preview.y(), preview.w(), preview.h(), preview.pivotX(), preview.pivotY(), preview.rotation());
        return preview.moveTo(clamped.x, clamped.y);
    }

    static CanvasTextLayer clampRotationPreviewText(TabletUiState state, CanvasTextLayer text) {
        CanvasTextLayer preview = shouldFitRotatedPreview(state, text.rotation())
                ? fittedTextIfGridLocked(state, text)
                : text;
        CanvasPoint clamped = CanvasGeometry.clampRotatedAnchorToCanvas(state, preview.x(), preview.y(), preview.w(), preview.h(), CanvasElementGeometry.defaultPivot(preview.w()), CanvasElementGeometry.defaultPivot(preview.h()), preview.rotation());
        return preview.moveTo(clamped.x, clamped.y);
    }

    static CanvasExclusiveChoice clampRotationPreviewExclusiveChoice(TabletUiState state, CanvasExclusiveChoice ec) {
        CanvasExclusiveChoice preview = shouldFitRotatedPreview(state, ec.rotation())
                ? fittedExclusiveChoiceIfGridLocked(state, ec)
                : ec;
        CanvasPoint clamped = CanvasGeometry.clampRotatedAnchorToCanvas(state, preview.x(), preview.y(), preview.w(), preview.h(), CanvasElementGeometry.defaultPivot(preview.w()), CanvasElementGeometry.defaultPivot(preview.h()), preview.rotation());
        return preview.moveTo(clamped.x, clamped.y);
    }

    private static boolean shouldFitRotatedPreview(TabletUiState state, int rotation) {
        return state.canvas.gridSnapLocked && TabletModifierKeys.shiftDown() && CanvasGeometry.isCardinalTurn(rotation);
    }
}
