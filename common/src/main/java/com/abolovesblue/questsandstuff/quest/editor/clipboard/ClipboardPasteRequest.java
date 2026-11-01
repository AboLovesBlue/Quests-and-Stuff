package com.abolovesblue.questsandstuff.quest.editor.clipboard;

public record ClipboardPasteRequest(String targetChapter, int anchorX, int anchorY) {
    public ClipboardPasteRequest {
        targetChapter = targetChapter == null ? "" : targetChapter.trim();
    }
}
