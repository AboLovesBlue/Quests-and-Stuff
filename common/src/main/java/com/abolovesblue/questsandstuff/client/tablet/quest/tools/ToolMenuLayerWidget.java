package com.abolovesblue.questsandstuff.client.tablet.quest.tools;

import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import com.abolovesblue.questsandstuff.client.tablet.state.UiState;

public final class ToolMenuLayerWidget extends WidgetGroup {
    private final UiState state;
    private final Runnable refresh;

    public ToolMenuLayerWidget(int x, int y, int width, int height, UiState state, Runnable refresh) {
        super(x, y, width, height);
        this.state = state;
        this.refresh = refresh == null ? () -> {
        } : refresh;
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if (ToolMenuAnimation.finishClosingIfDone(state)) {
            refresh.run();
        }
    }
}
