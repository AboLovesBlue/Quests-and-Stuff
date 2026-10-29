package com.abo47.questsandstuff.client.tablet.quest.canvas.render.gizmo;

import org.joml.Quaternionf;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;

import com.abo47.questsandstuff.client.tablet.quest.canvas.geometry.CanvasRotationMath;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.geometry.CanvasElementGeometry;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.geometry.CanvasTransformMode;
import com.abo47.questsandstuff.client.tablet.state.UiState;
import com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory;
import com.abo47.questsandstuff.client.tablet.theme.tokens.ThemeColors;

import static com.abo47.questsandstuff.client.tablet.layout.PanelChrome.drawRectOutline;
import static com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory.withAlpha;
import static com.abo47.questsandstuff.client.tablet.theme.tokens.UiThemeTokens.*;

public final class CanvasTransformGizmo {
    static final int HANDLE = GRID_7;
    static final int HIT_PAD = GRID_7;
    private static final int MOVE_HANDLE = GRID_7;
    private static final int RING_STEP = GRID_7;
    private static final int RING_THICKNESS = GRID_3;
    private static final int RING_ALPHA_IDLE = 155;
    private static final int RING_ALPHA_ACTIVE = 255;
    private static final int RING_SEGMENTS = 256;
    private static final int AXIS_X_COLOR = ThemeColors.ERROR;
    private static final int AXIS_Y_COLOR = ThemeColors.SUCCESS;
    private static final int AXIS_Z_COLOR = ThemeColors.INTERACTIVE;

    private CanvasTransformGizmo() {
    }

    public static void draw(GuiGraphics graphics, UiState state, int originX, int originY, int x, int y, int width, int height, int rotationDegrees) {
        draw(graphics, state, originX, originY, x, y, width, height, rotationDegrees, 0, 0);
    }

    public static void draw(GuiGraphics graphics, UiState state, int originX, int originY, int x, int y, int width, int height, int rotationDegrees, int yawDegrees, int pitchDegrees) {
        drawAtPivot(graphics, state, originX, originY, x, y, width, height, width / 2, height / 2, rotationDegrees, yawDegrees, pitchDegrees);
    }

    public static void drawAtPivot(GuiGraphics graphics, UiState state, int originX, int originY, int x, int y, int width, int height, int pivotX, int pivotY, int rotationDegrees, int yawDegrees, int pitchDegrees) {
        Geometry geometry = geometry(state, x, y, width, height, pivotX, pivotY, rotationDegrees);
        if (geometry.width() <= 0 || geometry.height() <= 0) {
            return;
        }
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        CanvasTransformMode mode = CanvasTransformGizmoAxis.activeMode(state);
        RotateAxes rotateAxes = null;
        int moveAxis = moveAxis(geometry.width(), geometry.height());
        graphics.pose().pushPose();
        graphics.pose().translate(originX + geometry.centerX(), originY + geometry.centerY(), 0.0F);
        if (mode == CanvasTransformMode.ROTATE) {
            rotateAxes = rotateAxes(rotationRadius(geometry.width(), geometry.height()), rotationDegrees, yawDegrees, pitchDegrees);
            drawRotateGizmo(graphics, state, rotateAxes, rotationDegrees, yawDegrees, pitchDegrees);
        } else {
            graphics.pose().mulPose(new Quaternionf().rotationXYZ(0.0F, 0.0F, (float) Math.toRadians(CanvasRotationMath.normalizeDegrees(rotationDegrees))));
            if (mode == CanvasTransformMode.RESIZE) {
                drawResizeGizmo(graphics, geometry.left(), geometry.top(), geometry.right(), geometry.bottom());
            } else {
                drawMoveGizmo(graphics, moveAxis);
            }
        }
        graphics.pose().popPose();
        if (mode == CanvasTransformMode.ROTATE && rotateAxes != null) {
            drawActiveRotateLabel(graphics, state, originX, originY, geometry, rotateAxes, rotationDegrees, yawDegrees, pitchDegrees);
        } else if (mode == CanvasTransformMode.RESIZE) {
            drawActiveResizeLabel(graphics, state, originX, originY, geometry, rotationDegrees, width, height);
        } else if (mode == CanvasTransformMode.MOVE) {
            drawActiveMoveLabel(graphics, state, originX, originY, geometry, rotationDegrees, moveAxis, x, y);
        }
        RenderSystem.depthMask(true);
    }

    private static void drawMoveGizmo(GuiGraphics graphics, int axis) {
        int xColor = AXIS_X_COLOR;
        int yColor = AXIS_Y_COLOR;
        line(graphics, -axis + MOVE_HANDLE / 2, 0, axis - MOVE_HANDLE / 2, 0, xColor, 220);
        line(graphics, 0, -axis + MOVE_HANDLE / 2, 0, axis - MOVE_HANDLE / 2, yColor, 220);
        drawBoxHandle(graphics, axis, 0, xColor);
        drawBoxHandle(graphics, -axis, 0, xColor);
        drawBoxHandle(graphics, 0, -axis, yColor);
        drawBoxHandle(graphics, 0, axis, yColor);
        drawTransparentBoxHandle(graphics, 0, 0, AXIS_Z_COLOR);
    }

    private static void drawResizeGizmo(GuiGraphics graphics, int boxLeft, int boxTop, int boxRight, int boxBottom) {
        int left = boxLeft;
        int top = boxTop;
        int right = boxRight;
        int bottom = boxBottom;
        SurfaceFactory.fill(withAlpha(ThemeColors.INTERACTIVE, 18)).draw(graphics, 0, 0, left, top, right - left, bottom - top);
        drawRectOutline(graphics, left, top, Math.max(1, right - left), Math.max(1, bottom - top), withAlpha(ThemeColors.SUCCESS, 185));
        drawInsideHandle(graphics, left, top, ThemeColors.SUCCESS);
        drawInsideHandle(graphics, right - HANDLE, top, ThemeColors.SUCCESS);
        drawInsideHandle(graphics, left, bottom - HANDLE, ThemeColors.SUCCESS);
        drawInsideHandle(graphics, right - HANDLE, bottom - HANDLE, ThemeColors.SUCCESS);
    }

    private static void drawRotateGizmo(GuiGraphics graphics, UiState state, RotateAxes axes, int rotationDegrees, int yawDegrees, int pitchDegrees) {
        String active = activeAxis(state);
        boolean dragging = isRotating(state);
        int pitchAlpha = CanvasTransformGizmoAxis.AXIS_PITCH.equals(active) && dragging ? RING_ALPHA_ACTIVE : RING_ALPHA_IDLE;
        int yawAlpha = CanvasTransformGizmoAxis.AXIS_YAW.equals(active) && dragging ? RING_ALPHA_ACTIVE : RING_ALPHA_IDLE;
        int rollAlpha = CanvasTransformGizmoAxis.AXIS_ROLL.equals(active) && dragging ? RING_ALPHA_ACTIVE : RING_ALPHA_IDLE;
        drawCircleRing(graphics, axes.pitchRadius(), RING_THICKNESS, AXIS_X_COLOR, pitchAlpha);
        drawCircleRing(graphics, axes.yawRadius(), RING_THICKNESS, AXIS_Y_COLOR, yawAlpha);
        drawCircleRing(graphics, axes.rollRadius(), RING_THICKNESS, AXIS_Z_COLOR, rollAlpha);
        drawBoxHandle(graphics, axes.pitchHandleX(), axes.pitchHandleY(), AXIS_X_COLOR);
        drawBoxHandle(graphics, axes.yawHandleX(), axes.yawHandleY(), AXIS_Y_COLOR);
        drawBoxHandle(graphics, axes.rollHandleX(), axes.rollHandleY(), AXIS_Z_COLOR);
        drawTransparentBoxHandle(graphics, 0, 0, AXIS_Z_COLOR);
    }

    private static void line(GuiGraphics graphics, int x1, int y1, int x2, int y2, int color, int alpha) {
        int c = withAlpha(color, alpha);
        if (y1 == y2) {
            SurfaceFactory.fill(c).draw(graphics, 0, 0, Math.min(x1, x2), y1 - 1, Math.max(x1, x2) + 1 - Math.min(x1, x2), 3);
        } else if (x1 == x2) {
            SurfaceFactory.fill(c).draw(graphics, 0, 0, x1 - 1, Math.min(y1, y2), 3, Math.max(y1, y2) + 1 - Math.min(y1, y2));
        }
    }

    private static void drawCircleRing(GuiGraphics graphics, int radius, int thickness, int color, int alpha) {
        if (radius <= 0 || thickness <= 0) {
            return;
        }
        float outer = radius;
        float inner = Math.max(0.0F, radius - thickness);
        int argb = withAlpha(color, alpha);
        int r = (argb >> 16) & 0xFF;
        int g = (argb >> 8) & 0xFF;
        int b = argb & 0xFF;
        int a = (argb >> 24) & 0xFF;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        var tesselator = Tesselator.getInstance();
        var buffer = tesselator.getBuilder();
        buffer.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        var pose = graphics.pose().last().pose();
        for (int i = 0; i <= RING_SEGMENTS; i++) {
            double angle = 2.0 * Math.PI * i / RING_SEGMENTS;
            float cos = (float) Math.cos(angle);
            float sin = (float) Math.sin(angle);
            buffer.vertex(pose, cos * inner, sin * inner, 0).color(r, g, b, a).endVertex();
            buffer.vertex(pose, cos * outer, sin * outer, 0).color(r, g, b, a).endVertex();
        }
        tesselator.end();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    private static void drawActiveRotateLabel(GuiGraphics graphics, UiState state, int originX, int originY, Geometry geometry, RotateAxes axes, int rotationDegrees, int yawDegrees, int pitchDegrees) {
        if (state == null || !isRotating(state)) {
            return;
        }
        String axis = activeAxis(state);
        if (axis.isBlank()) {
            return;
        }
        int x;
        int y;
        int color;
        int value;
        if (CanvasTransformGizmoAxis.AXIS_PITCH.equals(axis)) {
            x = axes.pitchHandleX();
            y = axes.pitchHandleY();
            color = AXIS_X_COLOR;
            value = pitchDegrees;
        } else if (CanvasTransformGizmoAxis.AXIS_YAW.equals(axis)) {
            x = axes.yawHandleX();
            y = axes.yawHandleY();
            color = AXIS_Y_COLOR;
            value = yawDegrees;
        } else {
            x = axes.rollHandleX();
            y = axes.rollHandleY();
            color = AXIS_Z_COLOR;
            value = rotationDegrees;
        }
        ScreenPoint point = screenPoint(originX, originY, geometry, 0, x, y);
        drawValueLabel(graphics, CanvasRotationMath.normalizeDegrees(value) + "\u00B0", point.x() + HANDLE, point.y() - HANDLE, color);
    }

    private static void drawActiveMoveLabel(GuiGraphics graphics, UiState state, int originX, int originY, Geometry geometry, int rotationDegrees, int axisLength, int x, int y) {
        if (state == null || !isMoving(state)) {
            return;
        }
        String axis = activeMoveAxis(state);
        int labelX = 0;
        int labelY = 0;
        int color = AXIS_Z_COLOR;
        String text = x + ", " + y;
        if (CanvasTransformGizmoAxis.AXIS_MOVE_X.equals(axis)) {
            labelX = axisLength;
            color = AXIS_X_COLOR;
            text = String.valueOf(x);
        } else if (CanvasTransformGizmoAxis.AXIS_MOVE_Y.equals(axis)) {
            labelY = -axisLength;
            color = AXIS_Y_COLOR;
            text = String.valueOf(y);
        }
        ScreenPoint point = screenPoint(originX, originY, geometry, rotationDegrees, labelX, labelY);
        drawValueLabel(graphics, text, point.x() + HANDLE, point.y() - HANDLE, color);
    }

    private static void drawActiveResizeLabel(GuiGraphics graphics, UiState state, int originX, int originY, Geometry geometry, int rotationDegrees, int width, int height) {
        if (state == null || !isResizing(state)) {
            return;
        }
        CanvasTransformGizmo.LocalPoint point = CanvasTransformGizmoHit.resizeHandlePoint(activeResizeAxis(state), geometry.left(), geometry.top(), geometry.right(), geometry.bottom());
        ScreenPoint screenPoint = screenPoint(originX, originY, geometry, rotationDegrees, point.x(), point.y());
        drawValueLabel(graphics, width + " x " + height, screenPoint.x() + HANDLE, screenPoint.y() - HANDLE, AXIS_Y_COLOR);
    }

    private static void drawValueLabel(GuiGraphics graphics, String text, int x, int y, int color) {
        var font = Minecraft.getInstance().font;
        int width = font.width(text) + 6;
        int height = font.lineHeight + 4;
        SurfaceFactory.fill(withAlpha(ThemeColors.SURFACE_BASE, 205)).draw(graphics, 0, 0, x - GRID_3, y - GRID_2, width, height);
        drawRectOutline(graphics, x - GRID_3, y - GRID_2, width, height, withAlpha(color, 210));
        graphics.drawString(font, text, x, y, ThemeColors.TEXT_PRIMARY, false);
    }

    private static boolean isRotating(UiState state) {
        return state.canvas.rotatingCanvasImage || "rotate".equals(state.questDetails.questDetailsTransformMode);
    }

    private static boolean isMoving(UiState state) {
        return state.canvas.draggingCanvasImage || "move".equals(state.questDetails.questDetailsTransformMode);
    }

    private static boolean isResizing(UiState state) {
        return state.canvas.resizingCanvasImage || "resize".equals(state.questDetails.questDetailsTransformMode);
    }

    private static String activeAxis(UiState state) {
        if (state.canvas.rotatingCanvasImage && state.canvas.canvasImageTransformAxis != null && !state.canvas.canvasImageTransformAxis.isBlank()) {
            return state.canvas.canvasImageTransformAxis;
        }
        if ("rotate".equals(state.questDetails.questDetailsTransformMode) && state.questDetails.questDetailsTransformAxis != null && !state.questDetails.questDetailsTransformAxis.isBlank()) {
            return state.questDetails.questDetailsTransformAxis;
        }
        if (state.canvas.canvasImageTransformAxis != null && !state.canvas.canvasImageTransformAxis.isBlank()) {
            return state.canvas.canvasImageTransformAxis;
        }
        return state.questDetails.questDetailsTransformAxis == null ? "" : state.questDetails.questDetailsTransformAxis;
    }

    private static String activeMoveAxis(UiState state) {
        if (state.canvas.draggingCanvasImage) {
            return CanvasTransformGizmoAxis.moveAxisOrFree(state.canvas.canvasImageTransformAxis);
        }
        if ("move".equals(state.questDetails.questDetailsTransformMode)) {
            return CanvasTransformGizmoAxis.moveAxisOrFree(state.questDetails.questDetailsTransformAxis);
        }
        return CanvasTransformGizmoAxis.AXIS_MOVE_FREE;
    }

    private static String activeResizeAxis(UiState state) {
        if (state.canvas.resizingCanvasImage && state.canvas.canvasImageTransformAxis != null && !state.canvas.canvasImageTransformAxis.isBlank()) {
            return state.canvas.canvasImageTransformAxis;
        }
        if ("resize".equals(state.questDetails.questDetailsTransformMode) && state.questDetails.questDetailsTransformAxis != null && !state.questDetails.questDetailsTransformAxis.isBlank()) {
            return state.questDetails.questDetailsTransformAxis;
        }
        return CanvasTransformGizmoAxis.AXIS_RESIZE_SE;
    }

    private static void drawTransparentBoxHandle(GuiGraphics graphics, int centerX, int centerY, int color) {
        int half = HANDLE / 2;
        int left = centerX - half;
        int top = centerY - half;
        SurfaceFactory.fill(withAlpha(ThemeColors.SURFACE_BASE, 72)).draw(graphics, 0, 0, left, top, HANDLE, HANDLE);
        drawRectOutline(graphics, left, top, HANDLE, HANDLE, color);
    }

    private static void drawInsideHandle(GuiGraphics graphics, int left, int top, int color) {
        SurfaceFactory.fill(withAlpha(ThemeColors.SURFACE_BASE, 220)).draw(graphics, 0, 0, left, top, HANDLE, HANDLE);
        drawRectOutline(graphics, left, top, HANDLE, HANDLE, color);
    }

    private static void drawBoxHandle(GuiGraphics graphics, int centerX, int centerY, int color) {
        int half = HANDLE / 2;
        int left = centerX - half;
        int top = centerY - half;
        SurfaceFactory.fill(withAlpha(ThemeColors.SURFACE_BASE, 220)).draw(graphics, 0, 0, left, top, HANDLE, HANDLE);
        drawRectOutline(graphics, left, top, HANDLE, HANDLE, color);
    }

    static RotateAxes rotateAxes(int radius, int rotationDegrees, int yawDegrees, int pitchDegrees) {
        int pitchRadius = Math.max(GRID_12, radius - RING_STEP * 2);
        int yawRadius = Math.max(pitchRadius + RING_STEP, radius - RING_STEP);
        int rollRadius = Math.max(yawRadius + RING_STEP, radius);
        double pitchHandleR = Math.max(0, pitchRadius - RING_THICKNESS / 2.0);
        double yawHandleR = Math.max(0, yawRadius - RING_THICKNESS / 2.0);
        double rollHandleR = Math.max(0, rollRadius - RING_THICKNESS / 2.0);
        CanvasTransformGizmo.LocalPoint pitchHandle = pointOnCircle(pitchHandleR, pitchDegrees - 90);
        CanvasTransformGizmo.LocalPoint yawHandle = pointOnCircle(yawHandleR, yawDegrees - 90);
        CanvasTransformGizmo.LocalPoint rollHandle = pointOnCircle(rollHandleR, rotationDegrees - 90);
        return new RotateAxes(
                yawRadius,
                (int) Math.round(yawHandle.x()),
                (int) Math.round(yawHandle.y()),
                pitchRadius,
                (int) Math.round(pitchHandle.x()),
                (int) Math.round(pitchHandle.y()),
                rollRadius,
                (int) Math.round(rollHandle.x()),
                (int) Math.round(rollHandle.y())
        );
    }

    static CanvasTransformGizmo.LocalPoint pointOnCircle(double radius, int degrees) {
        double radians = Math.toRadians(CanvasRotationMath.normalizeDegrees(degrees));
        return new CanvasTransformGizmo.LocalPoint(Math.cos(radians) * radius, Math.sin(radians) * radius);
    }

    static int moveAxis(int width, int height) {
        return Math.max(GRID_18, Math.min(46, Math.min(width, height) / 2 + 10));
    }

    static int rotationRadius(int width, int height) {
        return Math.max(GRID_24, Math.min(70, Math.max(width, height) / 2 + GRID_12));
    }

    static Geometry geometry(UiState state, int x, int y, int width, int height, int pivotX, int pivotY, int rotationDegrees) {
        CanvasElementGeometry.Box box = CanvasElementGeometry.screenBoxAtPivot(state, x, y, width, height, pivotX, pivotY, rotationDegrees);
        return new Geometry(box.centerX(), box.centerY(), box.width(), box.height(), box.left(), box.top(), box.right(), box.bottom());
    }

    static ScreenPoint screenPoint(int originX, int originY, Geometry geometry, int rotationDegrees, double localX, double localY) {
        double radians = Math.toRadians(CanvasRotationMath.normalizeDegrees(rotationDegrees));
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        int x = (int) Math.round(originX + geometry.centerX() + localX * cos - localY * sin);
        int y = (int) Math.round(originY + geometry.centerY() + localX * sin + localY * cos);
        return new ScreenPoint(x, y);
    }

    static CanvasTransformGizmo.LocalPoint toLocalPoint(Geometry geometry, int rotationDegrees, int hitX, int hitY) {
        double dx = hitX - geometry.centerX();
        double dy = hitY - geometry.centerY();
        double radians = Math.toRadians(-CanvasRotationMath.normalizeDegrees(rotationDegrees));
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        return new CanvasTransformGizmo.LocalPoint(dx * cos - dy * sin, dx * sin + dy * cos);
    }

    record Geometry(double centerX, double centerY, int width, int height, int left, int top, int right, int bottom) {
    }

    record LocalPoint(double x, double y) {
    }

    record ScreenPoint(int x, int y) {
    }

    record RotateAxes(
            int yawRadius,
            int yawHandleX,
            int yawHandleY,
            int pitchRadius,
            int pitchHandleX,
            int pitchHandleY,
            int rollRadius,
            int rollHandleX,
            int rollHandleY
    ) {
    }
}
