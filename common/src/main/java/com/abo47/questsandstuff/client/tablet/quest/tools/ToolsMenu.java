package com.abo47.questsandstuff.client.tablet.quest.tools;

import net.minecraft.world.entity.player.Player;

import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import com.abo47.questsandstuff.client.tablet.state.UiState;

public final class ToolsMenu {
    private ToolsMenu() {
    }

    public static void rebuild(WidgetGroup toolsMenu, UiState state, Player player, Runnable refresh, int canvasX, int toolsX, int topY, int headerH, int toolsW) {
        CanvasToolsMenu.rebuild(toolsMenu, state, player, refresh, canvasX, toolsX, topY, headerH, toolsW);
    }

    public static void rebuildQuestDetails(WidgetGroup toolsMenu, UiState state, Player player, Runnable refresh, String questId, int buttonX, int buttonY, int headerH, int toolSlot) {
        QuestDetailsToolsMenu.rebuild(toolsMenu, state, player, refresh, questId, buttonX, buttonY, headerH, toolSlot);
    }
}
