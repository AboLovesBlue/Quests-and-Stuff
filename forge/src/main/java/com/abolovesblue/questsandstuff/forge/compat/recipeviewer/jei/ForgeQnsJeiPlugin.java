package com.abolovesblue.questsandstuff.forge.compat.recipeviewer.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.runtime.IJeiRuntime;

import net.minecraft.resources.ResourceLocation;

import com.abolovesblue.questsandstuff.QuestsAndStuffMod;
import com.abolovesblue.questsandstuff.client.compat.recipeviewer.LockViewerSync;
import com.abolovesblue.questsandstuff.client.compat.recipeviewer.LockViewerBridges;

@JeiPlugin
public final class ForgeQnsJeiPlugin implements IModPlugin {
    private final ForgeJeiGateBridge bridge = new ForgeJeiGateBridge();

    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.tryBuild(QuestsAndStuffMod.MODID, "lock_sync");
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime runtime) {
        bridge.onRuntimeAvailable(runtime);
        LockViewerBridges.setJei(bridge);
        LockViewerSync.requestRefresh();
        LockViewerSync.ensureSubscribed();
    }

    @Override
    public void onRuntimeUnavailable() {
        LockViewerBridges.clearJei();
        bridge.onRuntimeUnavailable();
    }
}
