package com.abo47.questsandstuff.client.tablet.quest.canvas.transform;

import org.junit.jupiter.api.Test;

import com.abo47.questsandstuff.client.tablet.quest.canvas.render.gizmo.CanvasTransformGizmoAxis;
import com.abo47.questsandstuff.client.tablet.quest.canvas.geometry.CanvasGeometry;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.CanvasPoint;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.gizmo.CanvasTransformGizmo;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CanvasTransformEngineTest {
    @Test
    void normalDragSnapsOnShiftButModelFreeDragDoesNot() {
        CanvasTransformEngine.SnapSettings shiftOnly = new CanvasTransformEngine.SnapSettings(16, false, true);
        CanvasTransformEngine.SnapSettings lockedGrid = new CanvasTransformEngine.SnapSettings(16, true, false);

        assertPoint(16, 16, CanvasTransformEngine.dragDelta(10, 22, shiftOnly));
        assertPoint(10, 22, CanvasTransformEngine.freeDelta(10, 22, shiftOnly));
        assertPoint(16, 16, CanvasTransformEngine.freeDelta(10, 22, lockedGrid));
        assertPoint(10, 22, CanvasTransformEngine.modelMoveDelta(new CanvasTransformEngine.ModelMoveRequest(
                10,
                22,
                0,
                CanvasTransformGizmoAxis.AXIS_MOVE_X,
                false,
                shiftOnly
        )));
    }

    @Test
    void layerRotationWrapsAndSnapsToSharedAngleStep() {
        assertEquals(90, CanvasTransformEngine.rotationDelta(0.0D, 1.0D, 0.0D, 0.0D, 0.0D));
        assertEquals(20, CanvasTransformEngine.rotationDelta(
                Math.cos(Math.toRadians(-170.0D)),
                Math.sin(Math.toRadians(-170.0D)),
                0.0D,
                0.0D,
                Math.toRadians(170.0D)
        ));
        assertEquals(15, CanvasTransformEngine.snapAngle(22));
        assertEquals(30, CanvasTransformEngine.snapAngle(23));
        assertEquals(105, CanvasTransformEngine.layerRotation(new CanvasTransformEngine.RotationRequest(
                10,
                0.0D,
                0.0D,
                0.0D,
                0.0D,
                1.0D,
                true
        )));
    }

    @Test
    void modelRotationSharesYawPitchAndSnapRules() {
        CanvasTransformEngine.ModelRotation yawRotation = CanvasTransformEngine.modelRotation(new CanvasTransformEngine.ModelRotationRequest(
                new CanvasTransformEngine.ModelRotation(12, 7),
                false,
                0.0D,
                0.0D,
                0.0D,
                0.0D,
                1.0D,
                true
        ));
        CanvasTransformEngine.ModelRotation pitchRotation = CanvasTransformEngine.modelRotation(new CanvasTransformEngine.ModelRotationRequest(
                new CanvasTransformEngine.ModelRotation(12, 7),
                true,
                0.0D,
                0.0D,
                0.0D,
                0.0D,
                1.0D,
                false
        ));

        assertAll(
                () -> assertEquals(new CanvasTransformEngine.ModelRotation(105, 0), yawRotation),
                () -> assertEquals(new CanvasTransformEngine.ModelRotation(12, 97), pitchRotation)
        );
    }

    @Test
    void resizeFromCornerUsesLayerRectAndSnapSettings() {
        CanvasGeometry.ResizedBox resized = CanvasTransformEngine.resizeFromCorner(new CanvasTransformEngine.ResizeRequest(
                new CanvasTransformEngine.LayerRect(10, 20, 40, 20, 20, 10, 0),
                58.0D,
                48.0D,
                8,
                8,
                new CanvasTransformEngine.SnapSettings(16, false, false),
                false,
                1,
                1
        ));

        assertEquals(new CanvasGeometry.ResizedBox(10, 20, 48, 28), resized);
    }

    @Test
    void moveAnchorAppliesClampAfterSharedDragMath() {
        CanvasPoint moved = CanvasTransformEngine.moveAnchor(
                new CanvasTransformEngine.MoveRequest(
                        new CanvasTransformEngine.LayerRect(10, 20, 40, 20, 20, 10, 0),
                        11,
                        22,
                        new CanvasTransformEngine.SnapSettings(16, false, true)
                ),
                (x, y, rect) -> new CanvasPoint(Math.min(24, x), Math.min(32, y))
        );

        assertPoint(24, 32, moved);
    }

    private static void assertPoint(int x, int y, CanvasPoint point) {
        assertAll(
                () -> assertEquals(x, point.x),
                () -> assertEquals(y, point.y)
        );
    }
}
