package com.abo47.questsandstuff.client.tablet.quest.tools;

import java.util.function.Consumer;

import net.minecraft.network.chat.Component;

import com.abo47.questsandstuff.client.tablet.theme.tokens.ThemeColors;

final class CanvasToolRows {
    private CanvasToolRows() {
    }

    static void grid(ToolMenuRows rows, boolean enabled, Runnable action) {
        rows.toggle("grid",
                enabled ? ThemeColors.SUCCESS : ThemeColors.ERROR,
                enabled,
                new Component[]{
                        Component.translatable("ui.questsandstuff.tools.grid_toggle"),
                        Component.translatable(enabled ? "ui.questsandstuff.tools.grid_state_on" : "ui.questsandstuff.tools.grid_state_off")
                },
                action);
    }

    static void snap(ToolMenuRows rows, boolean enabled, Runnable action) {
        rows.toggle("magnet",
                enabled ? ThemeColors.SUCCESS : ThemeColors.ERROR,
                enabled,
                new Component[]{
                        Component.translatable("ui.questsandstuff.tools.snap_to_grid"),
                        Component.translatable(enabled ? "ui.questsandstuff.tools.snap_state_on" : "ui.questsandstuff.tools.snap_state_off")
                },
                action);
    }

    static void centerX(ToolMenuRows rows, boolean enabled, Runnable action) {
        rows.toggle("align-center-vertical",
                enabled ? ThemeColors.SUCCESS : ThemeColors.ERROR,
                enabled,
                new Component[]{
                        Component.translatable("ui.questsandstuff.tools.center_snap_vertical"),
                        Component.translatable(enabled ? "ui.questsandstuff.tools.center_snap_vertical_on" : "ui.questsandstuff.tools.center_snap_vertical_off")
                },
                action);
    }

    static void centerY(ToolMenuRows rows, boolean enabled, Runnable action) {
        rows.toggle("align-center-horizontal",
                enabled ? ThemeColors.SUCCESS : ThemeColors.ERROR,
                enabled,
                new Component[]{
                        Component.translatable("ui.questsandstuff.tools.center_snap_horizontal"),
                        Component.translatable(enabled ? "ui.questsandstuff.tools.center_snap_horizontal_on" : "ui.questsandstuff.tools.center_snap_horizontal_off")
                },
                action);
    }

    static void objectSnap(ToolMenuRows rows, boolean enabled, Runnable action) {
        rows.toggle("objects",
                enabled ? ThemeColors.SUCCESS : ThemeColors.ERROR,
                enabled,
                new Component[]{
                        Component.translatable("ui.questsandstuff.tools.object_snap"),
                        Component.translatable(enabled ? "ui.questsandstuff.tools.object_snap_on" : "ui.questsandstuff.tools.object_snap_off")
                },
                action);
    }

    static void gridOpacity(ToolMenuRows rows, int percent, Consumer<Boolean> action) {
        rows.opacity("opacity",
                new Component[]{
                        Component.translatable("ui.questsandstuff.tools.grid_opacity"),
                        Component.translatable("ui.questsandstuff.tools.grid_opacity_value", percent),
                        Component.translatable("ui.questsandstuff.tools.cycle_click_hint")
                },
                action);
    }

    static void backgroundOpacity(ToolMenuRows rows, int percent, Consumer<Boolean> action) {
        rows.opacity("background_opacity",
                new Component[]{
                        Component.translatable("ui.questsandstuff.tools.canvas_bg_opacity"),
                        Component.translatable("ui.questsandstuff.tools.canvas_bg_opacity_value", percent),
                        Component.translatable("ui.questsandstuff.tools.cycle_click_hint")
                },
                action);
    }

    static void canvasLock(ToolMenuRows rows, boolean locked, Runnable action) {
        rows.toggle(locked ? "lock_canvas" : "unlock_canvas",
                locked ? ThemeColors.ERROR : ThemeColors.SUCCESS,
                !locked,
                new Component[]{
                        Component.translatable("ui.questsandstuff.tools.lock_canvas"),
                        Component.translatable(locked ? "ui.questsandstuff.tools.lock_state_locked" : "ui.questsandstuff.tools.lock_state_unlocked")
                },
                action);
    }

    static void splitterLock(ToolMenuRows rows, boolean locked, Runnable action) {
        rows.toggle(locked ? "lock_separator" : "unlock_separator",
                locked ? ThemeColors.ERROR : ThemeColors.SUCCESS,
                !locked,
                new Component[]{
                        Component.translatable("ui.questsandstuff.tools.lock_separator"),
                        Component.translatable(locked ? "ui.questsandstuff.tools.separator_state_locked" : "ui.questsandstuff.tools.separator_state_unlocked")
                },
                action);
    }
}
