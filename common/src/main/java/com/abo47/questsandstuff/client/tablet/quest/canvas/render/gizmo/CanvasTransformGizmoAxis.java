package com.abo47.questsandstuff.client.tablet.quest.canvas.render.gizmo;

import com.abo47.questsandstuff.client.tablet.entity.EntityPreviewRenderer;
import com.abo47.questsandstuff.client.tablet.preview.ModelAssetPreviewRenderer;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.geometry.CanvasTransformMode;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;

public final class CanvasTransformGizmoAxis {
    public static final String AXIS_YAW = "yaw";
    public static final String AXIS_PITCH = "pitch";
    public static final String AXIS_ROLL = "roll";
    public static final String AXIS_MOVE_X = "move_x";
    public static final String AXIS_MOVE_Y = "move_y";
    public static final String AXIS_MOVE_FREE = "move_free";
    public static final String AXIS_RESIZE_NW = "resize_nw";
    public static final String AXIS_RESIZE_NE = "resize_ne";
    public static final String AXIS_RESIZE_SW = "resize_sw";
    public static final String AXIS_RESIZE_SE = "resize_se";

    private CanvasTransformGizmoAxis() {
    }

    public static boolean supports(String asset) {
        return EntityPreviewRenderer.isEntityAsset(asset) || ModelAssetPreviewRenderer.isBlockModelAsset(asset);
    }

    public static CanvasTransformMode activeMode(TabletUiState state) {
        return CanvasTransformMode.fromId(state == null ? "" : state.canvas.transformGizmoMode);
    }

    public static void setMode(TabletUiState state, CanvasTransformMode mode) {
        if (state != null && mode != null) {
            boolean changed = !mode.id.equals(state.canvas.transformGizmoMode);
            state.canvas.transformGizmoMode = mode.id;
            if (changed) {
                state.canvas.canvasImageTransformAxis = "";
                state.questDetails.questDetailsTransformAxis = "";
            }
        }
    }

    public static String moveAxisOrFree(String axis) {
        if (AXIS_MOVE_X.equals(axis) || AXIS_MOVE_Y.equals(axis) || AXIS_MOVE_FREE.equals(axis)) {
            return axis;
        }
        return AXIS_MOVE_FREE;
    }

    public static int resizeCornerX(String axis) {
        return AXIS_RESIZE_NW.equals(axis) || AXIS_RESIZE_SW.equals(axis) ? -1 : 1;
    }

    public static int resizeCornerY(String axis) {
        return AXIS_RESIZE_NW.equals(axis) || AXIS_RESIZE_NE.equals(axis) ? -1 : 1;
    }
}
