package com.abo47.questsandstuff.client.tablet.quest.canvas.render;

import net.minecraft.world.entity.player.Player;

import com.abo47.questsandstuff.client.tablet.quest.editor.EditorCanvasCommandClient;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;

public final class ConnectionOps {
    private ConnectionOps() {
    }

    public static void applyTexture(TabletUiState state, Player player, String chapter, String prerequisiteId, String questId, String texture) {
        if (ConnectionRenderer.isEcId(state, chapter, prerequisiteId) || ConnectionRenderer.isEcId(state, chapter, questId)) {
            EditorCanvasCommandClient.runEcConnectionTextureAction(state, prerequisiteId, questId, texture);
            return;
        }
        EditorCanvasCommandClient.runConnectionTextureAction(player, questId, prerequisiteId, texture);
        ConnectionRenderer.setConnectionTexture(state, chapter, prerequisiteId, questId, texture);
    }

    public static void applyTextureSpacing(TabletUiState state, Player player, String chapter, String prerequisiteId, String questId, int spacing) {
        if (ConnectionRenderer.isEcId(state, chapter, prerequisiteId) || ConnectionRenderer.isEcId(state, chapter, questId)) {
            EditorCanvasCommandClient.runEcConnectionTextureSpacingAction(state, prerequisiteId, questId, spacing);
            return;
        }
        EditorCanvasCommandClient.runConnectionTextureSpacingAction(player, questId, prerequisiteId, spacing);
        ConnectionRenderer.setConnectionTextureSpacing(state, chapter, prerequisiteId, questId, spacing);
    }
}
