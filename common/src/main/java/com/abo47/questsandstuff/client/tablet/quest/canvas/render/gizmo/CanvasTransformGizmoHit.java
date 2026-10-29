package com.abo47.questsandstuff.client.tablet.quest.canvas.render.gizmo;

import com.abo47.questsandstuff.client.tablet.quest.canvas.render.geometry.CanvasTransformMode;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;

public final class CanvasTransformGizmoHit {
    private CanvasTransformGizmoHit() {
    }

    public static boolean controlHit(TabletUiState state, int x, int y, int width, int height, int rotationDegrees, int hitX, int hitY) {
        return modeAt(state, x, y, width, height, rotationDegrees, 0, 0, hitX, hitY) != null;
    }

    public static boolean controlHit(TabletUiState state, int x, int y, int width, int height, int rotationDegrees, int yawDegrees, int pitchDegrees, int hitX, int hitY) {
        return controlHitAtPivot(state, x, y, width, height, width / 2, height / 2, rotationDegrees, yawDegrees, pitchDegrees, hitX, hitY);
    }

    public static boolean controlHitAtPivot(TabletUiState state, int x, int y, int width, int height, int pivotX, int pivotY, int rotationDegrees, int yawDegrees, int pitchDegrees, int hitX, int hitY) {
        return modeAtPivot(state, x, y, width, height, pivotX, pivotY, rotationDegrees, yawDegrees, pitchDegrees, hitX, hitY) != null;
    }

    public static CanvasTransformMode modeAt(TabletUiState state, int x, int y, int width, int height, int rotationDegrees, int hitX, int hitY) {
        return modeAt(state, x, y, width, height, rotationDegrees, 0, 0, hitX, hitY);
    }

    public static CanvasTransformMode modeAt(TabletUiState state, int x, int y, int width, int height, int rotationDegrees, int yawDegrees, int pitchDegrees, int hitX, int hitY) {
        return modeAtPivot(state, x, y, width, height, width / 2, height / 2, rotationDegrees, yawDegrees, pitchDegrees, hitX, hitY);
    }

    public static CanvasTransformMode modeAtPivot(TabletUiState state, int x, int y, int width, int height, int pivotX, int pivotY, int rotationDegrees, int yawDegrees, int pitchDegrees, int hitX, int hitY) {
        Hit hit = hitAt(state, x, y, width, height, pivotX, pivotY, rotationDegrees, yawDegrees, pitchDegrees, hitX, hitY);
        return hit == null ? null : hit.mode();
    }

    public static String axisAt(TabletUiState state, int x, int y, int width, int height, int rotationDegrees, int yawDegrees, int pitchDegrees, int hitX, int hitY) {
        return axisAtPivot(state, x, y, width, height, width / 2, height / 2, rotationDegrees, yawDegrees, pitchDegrees, hitX, hitY);
    }

    public static String axisAtPivot(TabletUiState state, int x, int y, int width, int height, int pivotX, int pivotY, int rotationDegrees, int yawDegrees, int pitchDegrees, int hitX, int hitY) {
        Hit hit = hitAt(state, x, y, width, height, pivotX, pivotY, rotationDegrees, yawDegrees, pitchDegrees, hitX, hitY);
        return hit == null ? "" : hit.axis();
    }

    static Hit hitAt(TabletUiState state, int x, int y, int width, int height, int pivotX, int pivotY, int rotationDegrees, int yawDegrees, int pitchDegrees, int hitX, int hitY) {
        CanvasTransformGizmo.Geometry geometry = CanvasTransformGizmo.geometry(state, x, y, width, height, pivotX, pivotY, rotationDegrees);
        if (geometry.width() <= 0 || geometry.height() <= 0) {
            return null;
        }
        CanvasTransformGizmo.LocalPoint point = CanvasTransformGizmo.toLocalPoint(geometry, rotationDegrees, hitX, hitY);
        CanvasTransformGizmo.LocalPoint screenPoint = CanvasTransformGizmo.toLocalPoint(geometry, 0, hitX, hitY);
        int radius = CanvasTransformGizmo.rotationRadius(geometry.width(), geometry.height());
        CanvasTransformMode active = CanvasTransformGizmoAxis.activeMode(state);
        if (active == CanvasTransformMode.ROTATE) {
            CanvasTransformGizmo.RotateAxes axes = CanvasTransformGizmo.rotateAxes(radius, rotationDegrees, yawDegrees, pitchDegrees);
            String handleAxis = rotateHandleAxis(screenPoint, axes);
            if (!handleAxis.isBlank()) {
                return new Hit(CanvasTransformMode.ROTATE, handleAxis);
            }
            String ringAxis = rotateRingAxis(screenPoint, axes);
            if (!ringAxis.isBlank()) {
                return new Hit(CanvasTransformMode.ROTATE, ringAxis);
            }
        }
        if (active == CanvasTransformMode.RESIZE) {
            String resizeAxis = resizeAxisAt(point, geometry.left(), geometry.top(), geometry.right(), geometry.bottom());
            if (!resizeAxis.isBlank()) {
                return new Hit(CanvasTransformMode.RESIZE, resizeAxis);
            }
        }
        int axis = CanvasTransformGizmo.moveAxis(geometry.width(), geometry.height());
        if (active == CanvasTransformMode.MOVE) {
            String moveAxis = moveAxisAt(point, axis);
            if (!moveAxis.isBlank()) {
                return new Hit(CanvasTransformMode.MOVE, moveAxis);
            }
        }
        return null;
    }

    public static boolean boundsHit(TabletUiState state, int x, int y, int width, int height, int rotationDegrees, int hitX, int hitY) {
        return boundsHitAtPivot(state, x, y, width, height, width / 2, height / 2, rotationDegrees, hitX, hitY);
    }

    public static boolean boundsHitAtPivot(TabletUiState state, int x, int y, int width, int height, int pivotX, int pivotY, int rotationDegrees, int hitX, int hitY) {
        CanvasTransformGizmo.Geometry geometry = CanvasTransformGizmo.geometry(state, x, y, width, height, pivotX, pivotY, rotationDegrees);
        CanvasTransformGizmo.LocalPoint point = CanvasTransformGizmo.toLocalPoint(geometry, rotationDegrees, hitX, hitY);
        return point.x() >= geometry.left() - CanvasTransformGizmo.HIT_PAD
                && point.x() <= geometry.right() + CanvasTransformGizmo.HIT_PAD
                && point.y() >= geometry.top() - CanvasTransformGizmo.HIT_PAD
                && point.y() <= geometry.bottom() + CanvasTransformGizmo.HIT_PAD;
    }

    static boolean near(CanvasTransformGizmo.LocalPoint point, int x, int y) {
        return Math.abs(point.x() - x) <= CanvasTransformGizmo.HIT_PAD && Math.abs(point.y() - y) <= CanvasTransformGizmo.HIT_PAD;
    }

    private static double circleDistance(CanvasTransformGizmo.LocalPoint point, int radius) {
        if (radius <= 0) {
            return Double.MAX_VALUE;
        }
        double distance = Math.sqrt(point.x() * point.x() + point.y() * point.y());
        return Math.abs(distance - radius);
    }

    static String moveAxisAt(CanvasTransformGizmo.LocalPoint point, int axis) {
        if (near(point, 0, 0)) {
            return CanvasTransformGizmoAxis.AXIS_MOVE_FREE;
        }
        if (near(point, axis, 0) || near(point, -axis, 0)) {
            return CanvasTransformGizmoAxis.AXIS_MOVE_X;
        }
        if (near(point, 0, -axis) || near(point, 0, axis)) {
            return CanvasTransformGizmoAxis.AXIS_MOVE_Y;
        }
        return "";
    }

    static String resizeAxisAt(CanvasTransformGizmo.LocalPoint point, int boxLeft, int boxTop, int boxRight, int boxBottom) {
        int left = boxLeft;
        int top = boxTop;
        int right = boxRight;
        int bottom = boxBottom;
        int inset = CanvasTransformGizmo.HANDLE / 2;
        if (near(point, left + inset, top + inset)) {
            return CanvasTransformGizmoAxis.AXIS_RESIZE_NW;
        }
        if (near(point, right - CanvasTransformGizmo.HANDLE + inset, top + inset)) {
            return CanvasTransformGizmoAxis.AXIS_RESIZE_NE;
        }
        if (near(point, left + inset, bottom - CanvasTransformGizmo.HANDLE + inset)) {
            return CanvasTransformGizmoAxis.AXIS_RESIZE_SW;
        }
        if (near(point, right - CanvasTransformGizmo.HANDLE + inset, bottom - CanvasTransformGizmo.HANDLE + inset)) {
            return CanvasTransformGizmoAxis.AXIS_RESIZE_SE;
        }
        return "";
    }

    static CanvasTransformGizmo.LocalPoint resizeHandlePoint(String axis, int boxLeft, int boxTop, int boxRight, int boxBottom) {
        int left = boxLeft;
        int top = boxTop;
        int right = boxRight;
        int bottom = boxBottom;
        int inset = CanvasTransformGizmo.HANDLE / 2;
        return switch (axis) {
            case CanvasTransformGizmoAxis.AXIS_RESIZE_NW -> new CanvasTransformGizmo.LocalPoint(left + inset, top + inset);
            case CanvasTransformGizmoAxis.AXIS_RESIZE_NE -> new CanvasTransformGizmo.LocalPoint(right - CanvasTransformGizmo.HANDLE + inset, top + inset);
            case CanvasTransformGizmoAxis.AXIS_RESIZE_SW -> new CanvasTransformGizmo.LocalPoint(left + inset, bottom - CanvasTransformGizmo.HANDLE + inset);
            default -> new CanvasTransformGizmo.LocalPoint(right - CanvasTransformGizmo.HANDLE + inset, bottom - CanvasTransformGizmo.HANDLE + inset);
        };
    }

    static String rotateHandleAxis(CanvasTransformGizmo.LocalPoint point, CanvasTransformGizmo.RotateAxes axes) {
        if (near(point, axes.yawHandleX(), axes.yawHandleY())) {
            return CanvasTransformGizmoAxis.AXIS_YAW;
        }
        if (near(point, axes.pitchHandleX(), axes.pitchHandleY())) {
            return CanvasTransformGizmoAxis.AXIS_PITCH;
        }
        if (near(point, axes.rollHandleX(), axes.rollHandleY())) {
            return CanvasTransformGizmoAxis.AXIS_ROLL;
        }
        return "";
    }

    static String rotateRingAxis(CanvasTransformGizmo.LocalPoint point, CanvasTransformGizmo.RotateAxes axes) {
        double yaw = circleDistance(point, axes.yawRadius());
        double pitch = circleDistance(point, axes.pitchRadius());
        double roll = circleDistance(point, axes.rollRadius());
        double best = Math.min(yaw, Math.min(pitch, roll));
        if (best > CanvasTransformGizmo.HIT_PAD + 2.0D) {
            return "";
        }
        if (best == yaw) {
            return CanvasTransformGizmoAxis.AXIS_YAW;
        }
        if (best == pitch) {
            return CanvasTransformGizmoAxis.AXIS_PITCH;
        }
        return CanvasTransformGizmoAxis.AXIS_ROLL;
    }

    record Hit(CanvasTransformMode mode, String axis) {
    }
}
