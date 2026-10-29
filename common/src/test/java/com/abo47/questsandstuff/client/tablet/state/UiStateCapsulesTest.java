package com.abo47.questsandstuff.client.tablet.state;

import org.junit.jupiter.api.Test;

import com.abo47.questsandstuff.client.tablet.modal.ModalSession;
import com.abo47.questsandstuff.client.tablet.modal.ModalWindowManager;
import com.abo47.questsandstuff.client.tablet.quest.canvas.transform.CanvasTransformSessions;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.CanvasPoint;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UiStateCapsulesTest {
    @Test
    void rootExposesFeatureCapsulesInsteadOfLegacyFlatFields() {
        assertCapsuleField("root");
        assertCapsuleField("chapterPanel");
        assertCapsuleField("modal");
        assertCapsuleField("pickers");
        assertCapsuleField("canvas");
        assertCapsuleField("questDetails");
        assertCapsuleField("contextMenu");
        assertCapsuleField("clipboard");

        assertLegacyRootFieldMissing("selectedGroup");
        assertLegacyRootFieldMissing("modalSession");
        assertLegacyRootFieldMissing("canvasSelection");
        assertLegacyRootFieldMissing("canvasClipboard");
        assertLegacyRootFieldMissing("questDetailsQuestId");
        assertLegacyRootFieldMissing("contextMenuOpen");
    }

    @Test
    void mainCanvasResetLeavesUnrelatedCapsulesUntouched() {
        UiState state = new UiState();
        state.root.selectedChapter = "main";
        state.chapterPanel.lastJumpQuest = "quest/a";
        state.modal.modalSession = ModalSession.open(ModalWindowManager.ModalType.ICON_PICKER);
        state.pickers.assetSearch = "icons";
        state.questDetails.questDetailsQuestId = "quest/a";
        state.contextMenu.contextMenuOpen = true;
        state.clipboard.canvasClipboard.recordPastedImage("image:a");
        state.clipboard.canvasClipboard.recordPastedText("text:a");
        state.canvas.draggingSelection = true;
        state.canvas.transientQuestPositions.put("quest/a", new CanvasPoint(1, 2));

        CanvasTransformSessions.clearMainCanvasSession(state);

        assertFalse(state.canvas.draggingSelection);
        assertTrue(state.canvas.transientQuestPositions.isEmpty());
        assertEquals("main", state.root.selectedChapter);
        assertEquals("quest/a", state.chapterPanel.lastJumpQuest);
        assertEquals(ModalWindowManager.ModalType.ICON_PICKER, state.modal.modalSession.type());
        assertEquals("icons", state.pickers.assetSearch);
        assertEquals("quest/a", state.questDetails.questDetailsQuestId);
        assertTrue(state.contextMenu.contextMenuOpen);
        assertEquals("image:a", state.clipboard.canvasClipboard.lastPendingPastedImageId());
        assertEquals("text:a", state.clipboard.canvasClipboard.lastPendingPastedTextId());
    }

    private static void assertCapsuleField(String name) {
        try {
            UiState.class.getDeclaredField(name);
        } catch (NoSuchFieldException e) {
            throw new AssertionError("Missing capsule field: " + name, e);
        }
    }

    private static void assertLegacyRootFieldMissing(String name) {
        assertThrows(NoSuchFieldException.class, () -> UiState.class.getDeclaredField(name), name);
    }
}
