package com.abolovesblue.questsandstuff.client.tablet;

import net.minecraft.world.entity.player.Player;

import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import com.abolovesblue.questsandstuff.client.tablet.app.TabletAppRouter;
import com.abolovesblue.questsandstuff.client.tablet.home.HomeComposer;

public final class TabletComposer {
    private TabletComposer() {
    }

    public static WidgetGroup create(Player player) {
        return HomeComposer.create(player);
    }

    public static WidgetGroup create(Player player, int rootWidth, int rootHeight, boolean fullScreenMode) {
        return HomeComposer.create(player, rootWidth, rootHeight, fullScreenMode);
    }

    public static WidgetGroup createQuests(Player player, int rootWidth, int rootHeight, boolean fullScreenMode) {
        return TabletAppRouter.create("QUESTS", player, rootWidth, rootHeight, fullScreenMode);
    }

    public static WidgetGroup createTeams(Player player, int rootWidth, int rootHeight, boolean fullScreenMode) {
        return TabletAppRouter.create("TEAMS", player, rootWidth, rootHeight, fullScreenMode);
    }
}
