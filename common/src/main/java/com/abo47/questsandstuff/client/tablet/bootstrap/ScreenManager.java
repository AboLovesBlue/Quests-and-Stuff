package com.abo47.questsandstuff.client.tablet.bootstrap;

import net.minecraft.client.Minecraft;

import com.abo47.questsandstuff.QuestsAndStuffConfig;
import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.state.UiState;
import com.abo47.questsandstuff.client.tablet.ui.GuiContainer;
import com.abo47.questsandstuff.client.tablet.ui.factory.UiFactory;

public final class ScreenManager {
    private ScreenManager() {
    }

    public static void applyTabletLayoutMode(UiState state) {
        if (state == null) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        boolean fullScreen = QuestsAndStuffConfig.fullScreenModeEnabled();
        int rootW = targetRootWidth(minecraft, fullScreen);
        int rootH = targetRootHeight(minecraft, fullScreen);
        UiFactory.applyRootSize(state, rootW, rootH, fullScreen);
        if (minecraft.screen instanceof GuiContainer container) {
            container.modularUI.setSize(rootW, rootH);
        }
        QuestsAndStuffMod.debugLog("[QnS:UI] tablet layout mode fullscreen={} width={} height={}", fullScreen, rootW, rootH);
    }

    public static int targetRootWidth(Minecraft minecraft, boolean fullScreen) {
        if (!fullScreen || minecraft == null) {
            return UiFactory.ROOT_W;
        }
        return minecraft.getWindow().getGuiScaledWidth();
    }

    public static int targetRootHeight(Minecraft minecraft, boolean fullScreen) {
        if (!fullScreen || minecraft == null) {
            return UiFactory.ROOT_H;
        }
        return minecraft.getWindow().getGuiScaledHeight();
    }
}
