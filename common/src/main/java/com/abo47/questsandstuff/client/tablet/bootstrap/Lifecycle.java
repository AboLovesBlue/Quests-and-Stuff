package com.abo47.questsandstuff.client.tablet.bootstrap;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;

import com.lowdragmc.lowdraglib.gui.modular.IUIHolder;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import com.abo47.questsandstuff.QuestsAndStuffConfig;
import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.quest.hud.QuestHudLayoutEditScreen;
import com.abo47.questsandstuff.client.compat.recipeviewer.LockViewerSync;
import com.abo47.questsandstuff.client.sync.state.ClientQuestStateFacade;
import com.abo47.questsandstuff.client.tablet.app.TabletAppDescriptor;
import com.abo47.questsandstuff.client.tablet.app.TabletAppRegistry;
import com.abo47.questsandstuff.client.tablet.icons.DisplayIconProvider;
import com.abo47.questsandstuff.client.tablet.modal.pickers.BlockPickerModal;
import com.abo47.questsandstuff.client.tablet.modal.sound.SoundPickerModal;
import com.abo47.questsandstuff.client.tablet.modal.pickers.StatPickerModal;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsWindow;
import com.abo47.questsandstuff.client.tablet.state.UiState;
import com.abo47.questsandstuff.client.tablet.state.UiStatePersistence;
import com.abo47.questsandstuff.client.tablet.ui.GuiContainer;
import com.abo47.questsandstuff.client.tablet.ui.factory.UiFactory;
import com.abo47.questsandstuff.client.tablet.ui.state.ActiveState;

public final class Lifecycle {
    private static boolean uiPrewarmed;
    private static boolean suppressNextOpenClick;
    private static boolean rememberedQuestDetailsOpen;
    private static String rememberedQuestDetailsQuestId = "";

    private Lifecycle() {
    }

    public static void prewarmClientAtGameLaunch() {
        DisplayIconProvider.prewarm();
        StatPickerModal.prewarm();
        SoundPickerModal.prewarm();
        BlockPickerModal.prewarm();
    }

    public static void onClientLogin() {
        resetSessionLocalState();
    }

    public static void onClientLogout() {
        resetSessionLocalState();
    }

    public static void onClientTick() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            uiPrewarmed = false;
            return;
        }
        if (!uiPrewarmed) {
            UiFactory.prewarmClientUiAssets();
            uiPrewarmed = true;
            QuestsAndStuffMod.debugLog("[QnS:UI] prewarmed tablet ui caches");
        }
        LockViewerSync.tick();
        if (minecraft.gameMode == null || minecraft.screen != null) {
            return;
        }
        if (suppressNextOpenClick && !Keybindings.OPEN_UI.isDown() && !Keybindings.OPEN_QUESTS_UI.isDown() && !Keybindings.OPEN_TEAMS_UI.isDown() && !Keybindings.OPEN_CHUNKCLAIMER_UI.isDown()) {
            suppressNextOpenClick = false;
        }
        while (Keybindings.OPEN_UI.consumeClick()) {
            if (suppressNextOpenClick) {
                continue;
            }
            openTabletUi(minecraft, minecraft.player);
            break;
        }
        while (Keybindings.OPEN_QUESTS_UI.consumeClick()) {
            if (suppressNextOpenClick) {
                continue;
            }
            openApp("QUESTS");
            break;
        }
        while (Keybindings.OPEN_TEAMS_UI.consumeClick()) {
            if (suppressNextOpenClick) {
                continue;
            }
            openApp("TEAMS");
            break;
        }
        while (Keybindings.OPEN_CHUNKCLAIMER_UI.consumeClick()) {
            if (suppressNextOpenClick) {
                continue;
            }
            openApp("CHUNKCLAIMER");
            break;
        }
        while (Keybindings.EDIT_HUD.consumeClick()) {
            minecraft.setScreen(new QuestHudLayoutEditScreen());
            QuestsAndStuffMod.debugLog("[QnS:UI] hud layout editor opened from keybind");
            break;
        }
    }

    public static void openTabletUiFromItem(Player player) {
        if (player instanceof LocalPlayer localPlayer) {
            openTabletUi(Minecraft.getInstance(), localPlayer);
        }
    }

    public static void openTabletUiHome(Player player) {
        if (player instanceof LocalPlayer) {
            openApp("home");
        }
    }

    public static void openApp(String appId) {
        Minecraft minecraft = Minecraft.getInstance();
        Player mcPlayer = minecraft.player;
        if (!(mcPlayer instanceof LocalPlayer player)) {
            return;
        }
        UiState currentState = ActiveState.getActiveTabletState();
        if (currentState != null) {
            UiFactory.persistUiState(currentState);
        }
        Bootstrap.ensureAppsRegistered();
        TabletAppDescriptor app = TabletAppRegistry.get(appId);
        if (app == null) {
            QuestsAndStuffMod.debugLog("[QnS:UI] unknown app id={}", appId);
            return;
        }
        saveLastApp("home".equals(appId) ? "" : appId);
        boolean fullScreen = QuestsAndStuffConfig.fullScreenModeEnabled();
        int rootW = ScreenManager.targetRootWidth(minecraft, fullScreen);
        int rootH = ScreenManager.targetRootHeight(minecraft, fullScreen);
        WidgetGroup content = app.composer().create(player, rootW, rootH, fullScreen);
        ModularUI uiTemplate = new ModularUI(content, IUIHolder.EMPTY, player);
        uiTemplate.initWidgets();
        GuiContainer modularUiGui = new GuiContainer(uiTemplate, player.containerMenu.containerId);
        minecraft.setScreen(modularUiGui);
        player.containerMenu = modularUiGui.getMenu();
        QuestsAndStuffMod.debugLog("[QnS:UI] open app={}", appId);
    }

    public static void rememberActiveWindow(UiState state) {
        rememberedQuestDetailsOpen = state != null && state.questDetails.questDetailsOpen && state.questDetails.questDetailsQuestId != null && !state.questDetails.questDetailsQuestId.isBlank();
        rememberedQuestDetailsQuestId = rememberedQuestDetailsOpen ? state.questDetails.questDetailsQuestId.trim() : "";
    }

    public static void rememberMainWindow() {
        rememberedQuestDetailsOpen = false;
        rememberedQuestDetailsQuestId = "";
    }

    public static void restoreRememberedWindow(UiState state) {
        if (state == null || !rememberedQuestDetailsOpen || rememberedQuestDetailsQuestId.isBlank()) {
            return;
        }
        if (ClientQuestStateFacade.containsQuest(rememberedQuestDetailsQuestId)) {
            if (state.questDetails.questDetailsOpen) {
                QuestDetailsWindow.swapQuest(state, rememberedQuestDetailsQuestId);
            } else {
                QuestDetailsWindow.open(state, rememberedQuestDetailsQuestId);
            }
        } else {
            rememberedQuestDetailsOpen = false;
            rememberedQuestDetailsQuestId = "";
        }
    }

    public static void suppressNextOpenClick() {
        suppressNextOpenClick = true;
    }

    public static void closeTabletUi(UiState state, boolean suppressOpenClick, String reason) {
        rememberActiveWindow(state);
        if (state != null) {
            UiFactory.persistUiState(state);
            String saveKey = TabletAppRegistry.saveKeyOf(state.root.currentApp);
            if (saveKey != null) {
                saveLastApp(saveKey);
            }
            if (state.root.skinEditMode) {
                state.root.skinEditMode = false;
                state.root.skinEditSelectedTarget = "";
                UiFactory.persistSkinState(state);
                QuestsAndStuffMod.debugLog("[QnS:UI] skin edit mode cleared on ui close");
            }
        }
        if (suppressOpenClick) {
            suppressNextOpenClick();
        }
        Minecraft.getInstance().setScreen(null);
        QuestsAndStuffMod.debugLog("[QnS:UI] close ui reason={}", reason == null || reason.isBlank() ? "unknown" : reason);
    }

    private static void resetSessionLocalState() {
        ClientQuestStateFacade.resetStateForTests();
        LockViewerSync.reset();
        rememberedQuestDetailsOpen = false;
        rememberedQuestDetailsQuestId = "";
        suppressNextOpenClick = false;
        uiPrewarmed = false;
        QuestsAndStuffMod.debugLog("[QnS:UI] reset client quest cache/window state for world session");
    }

    private static void openTabletUi(Minecraft minecraft, LocalPlayer player) {
        if (player == null) {
            return;
        }
        String lastApp = readLastApp();
        if ("TEAMS".equals(lastApp)) {
            openApp("TEAMS");
        } else if ("QUESTS".equals(lastApp)) {
            openApp("QUESTS");
        } else if ("CHUNKCLAIMER".equals(lastApp)) {
            openApp("CHUNKCLAIMER");
        } else if ("SETTINGS".equals(lastApp)) {
            openApp("SETTINGS");
        } else {
            openApp("home");
        }
    }

    private static String readLastApp() {
        UiState temp = new UiState();
        UiStatePersistence.read(temp);
        return temp.root.lastApp;
    }

    private static void saveLastApp(String appId) {
        UiState temp = new UiState();
        UiStatePersistence.read(temp);
        temp.root.lastApp = appId;
        UiStatePersistence.write(temp);
    }
}
