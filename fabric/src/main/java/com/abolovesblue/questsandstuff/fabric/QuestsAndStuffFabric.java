package com.abolovesblue.questsandstuff.fabric;

import net.minecraft.server.packs.PackType;

import com.abolovesblue.questsandstuff.QuestsAndStuffMod;
import com.abolovesblue.questsandstuff.command.QuestCommands;
import com.abolovesblue.questsandstuff.platform.Services;
import com.abolovesblue.questsandstuff.quest.QuestServiceRegistry;
import com.abolovesblue.questsandstuff.quest.runtime.lock.ServerRecipeWrap;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;

public final class QuestsAndStuffFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        Services.setPlatform(new FabricPlatformService());
        FabricContent.register();
        QuestsAndStuffMod.bootstrapCommon();
        FabricQuestEventBridge.register();

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> QuestCommands.register(dispatcher));
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new FabricQuestServerReloadListener());

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            QuestsAndStuffMod.SERVER_REF = server;
            QuestsAndStuffMod.prepareAssetsDirectory();
            QuestServiceRegistry.start(server);
            ServerRecipeWrap.wrapAll(server.getRecipeManager());
        });
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            QuestServiceRegistry.stop(server);
            if (QuestsAndStuffMod.SERVER_REF == server) {
                QuestsAndStuffMod.SERVER_REF = null;
            }
        });
    }
}