package com.abo47.questsandstuff.client.tablet.quest.canvas.interaction;

import org.lwjgl.glfw.GLFW;

import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasViewport;
import com.abo47.questsandstuff.client.tablet.quest.canvas.clipboard.CanvasClipboardCopy;
import com.abo47.questsandstuff.client.tablet.quest.canvas.clipboard.CanvasClipboardPaste;
import com.abo47.questsandstuff.client.tablet.entity.motion.EntityMotionEditor;
import com.abo47.questsandstuff.client.tablet.quest.canvas.viewport.CanvasInlineTextEditor;
import com.abo47.questsandstuff.client.tablet.state.UiState;

public final class CanvasViewportKeyboardController {
    private CanvasViewportKeyboardController() {
    }

    public static boolean keyPressed(
            CanvasViewport viewport,
            UiState state,
            Runnable refresher,
            CanvasInlineTextEditor textEditor,
            int keyCode,
            int scanCode,
            int modifiers
    ) {
        if (EntityMotionEditor.isMainCanvasOpen(state) && viewport.callSuperKeyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        if (textEditor.handleKeyPressed(keyCode)) {
            return true;
        }
        if (state.root.canEdit && viewport.ctrlDown() && keyCode == GLFW.GLFW_KEY_C) {
            if (CanvasClipboardCopy.copySelectionToClipboard(viewport, state)) {
                refresher.run();
            }
            return true;
        }
        if (state.root.canEdit && viewport.ctrlDown() && keyCode == GLFW.GLFW_KEY_V) {
            if (CanvasClipboardPaste.pasteNearSelectionOrViewportCenter(viewport.player(), state, viewport)) {
                refresher.run();
            }
            return true;
        }
        return viewport.callSuperKeyPressed(keyCode, scanCode, modifiers);
    }

    public static boolean charTyped(
            CanvasViewport viewport,
            UiState state,
            CanvasInlineTextEditor textEditor,
            char codePoint,
            int modifiers
    ) {
        if (EntityMotionEditor.isMainCanvasOpen(state) && viewport.callSuperCharTyped(codePoint, modifiers)) {
            return true;
        }
        if (textEditor.handleCharTyped(codePoint)) {
            return true;
        }
        return viewport.callSuperCharTyped(codePoint, modifiers);
    }
}
