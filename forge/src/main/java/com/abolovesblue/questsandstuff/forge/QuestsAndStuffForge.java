package com.abolovesblue.questsandstuff.forge;

import com.abolovesblue.questsandstuff.QuestsAndStuffMod;
import com.abolovesblue.questsandstuff.command.QuestCommands;
import com.abolovesblue.questsandstuff.forge.runtime.signal.ForgeQuestEventBridge;
import com.abolovesblue.questsandstuff.platform.Services;
import com.abolovesblue.questsandstuff.quest.QuestServiceRegistry;
import com.abolovesblue.questsandstuff.quest.persistence.quest.QuestServerReloadListener;
import com.abolovesblue.questsandstuff.quest.runtime.lock.ServerRecipeWrap;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import org.spongepowered.asm.mixin.Mixins;

@Mod(QuestsAndStuffMod.MODID)
public final class QuestsAndStuffForge {
    static {
        Mixins.addConfiguration("questsandstuff.forge.mixins.json");
    }

    public QuestsAndStuffForge(FMLJavaModLoadingContext modLoadingContext) {
        Services.setPlatform(new ForgePlatformService());

        var modBus = modLoadingContext.getModEventBus();
        ForgeContent.register(modBus);
        modBus.addListener(this::onCommonSetup);

        MinecraftForge.EVENT_BUS.register(new ForgeQuestEventBridge());
        MinecraftForge.EVENT_BUS.addListener(this::onRegisterCommands);
        MinecraftForge.EVENT_BUS.addListener(this::onAddReloadListener);
        MinecraftForge.EVENT_BUS.addListener(this::onServerStarted);
        MinecraftForge.EVENT_BUS.addListener(this::onServerStopping);
    }

    private void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(QuestsAndStuffMod::bootstrapCommon);
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        QuestCommands.register(event.getDispatcher());
    }

    private void onAddReloadListener(AddReloadListenerEvent event) {
        event.addListener(new QuestServerReloadListener());
    }

    private void onServerStarted(ServerStartedEvent event) {
        QuestsAndStuffMod.SERVER_REF = event.getServer();
        QuestsAndStuffMod.prepareAssetsDirectory();
        QuestServiceRegistry.start(event.getServer());
        ServerRecipeWrap.wrapAll(event.getServer().getRecipeManager());
        GameStagesBridge.install();
    }

    private void onServerStopping(ServerStoppingEvent event) {
        QuestServiceRegistry.stop(event.getServer());
        if (QuestsAndStuffMod.SERVER_REF == event.getServer()) {
            QuestsAndStuffMod.SERVER_REF = null;
        }
    }
}
