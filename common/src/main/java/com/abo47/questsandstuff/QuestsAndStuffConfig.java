package com.abo47.questsandstuff;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import com.abo47.questsandstuff.client.tablet.quest.canvas.render.card.QuestHoverExpand;
import com.abo47.questsandstuff.platform.Services;
import com.abo47.questsandstuff.quest.persistence.GsonProvider;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public final class QuestsAndStuffConfig {
    private static final Gson GSON = GsonProvider.GSON;
    public static final int DEFAULT_COMPLETION_HUD_DURATION_MS = QuestsAndStuffConfigSections.Hud.DEFAULT_DURATION_MS;
    public static final int MIN_COMPLETION_HUD_DURATION_MS = QuestsAndStuffConfigSections.Hud.MIN_DURATION_MS;
    public static final int MAX_COMPLETION_HUD_DURATION_MS = QuestsAndStuffConfigSections.Hud.MAX_DURATION_MS;
    public static final int MIN_QUEST_HOVER_EXPAND_PERCENT = QuestsAndStuffConfigSections.Animations.MIN_HOVER_EXPAND_PERCENT;
    public static final int MAX_QUEST_HOVER_EXPAND_PERCENT = QuestsAndStuffConfigSections.Animations.MAX_HOVER_EXPAND_PERCENT;
    public static final int DEFAULT_QUEST_HOVER_EXPAND_PERCENT = QuestsAndStuffConfigSections.Animations.DEFAULT_HOVER_EXPAND_PERCENT;

    private static boolean loaded;
    private static final QuestsAndStuffConfigSections.Debug DEBUG = new QuestsAndStuffConfigSections.Debug();
    private static final QuestsAndStuffConfigSections.Animations ANIMATIONS = new QuestsAndStuffConfigSections.Animations();
    private static final QuestsAndStuffConfigSections.Canvas CANVAS = new QuestsAndStuffConfigSections.Canvas();
    private static final QuestsAndStuffConfigSections.Rewards REWARDS = new QuestsAndStuffConfigSections.Rewards();
    private static final QuestsAndStuffConfigSections.Hud HUD = new QuestsAndStuffConfigSections.Hud();
    private static final QuestsAndStuffConfigSections.Security SECURITY = new QuestsAndStuffConfigSections.Security();
    private static final QuestsAndStuffConfigSections.ChunkClaims CHUNK_CLAIMS = new QuestsAndStuffConfigSections.ChunkClaims();
    private static final QuestsAndStuffConfigSections.ItemLocks ITEM_LOCKS = new QuestsAndStuffConfigSections.ItemLocks();

    private QuestsAndStuffConfig() {
    }

    public static synchronized void load() {
        if (loaded) {
            return;
        }
        loaded = true;
        Path file = configFile();
        if (Files.isRegularFile(file)) {
            try {
                JsonElement parsed = JsonParser.parseString(Files.readString(file, StandardCharsets.UTF_8));
                if (parsed.isJsonObject()) {
                    read(parsed.getAsJsonObject());
                }
            } catch (Exception e) {
                QuestsAndStuffMod.LOGGER.warn("Failed reading Quests and Stuff config {}, keeping defaults", file, e);
            }
        }
        save();
    }

    private static boolean current(BooleanSupplier flag) {
        load();
        return flag.getAsBoolean();
    }

    private static boolean animations(BooleanSupplier flag) {
        load();
        return ANIMATIONS.ui && flag.getAsBoolean();
    }

    private static boolean setIfChanged(BooleanSupplier current, Consumer<Boolean> apply, boolean value) {
        load();
        if (current.getAsBoolean() != value) {
            apply.accept(value);
            save();
        }
        return true;
    }

    public static boolean debugLoggingEnabled() {
        return current(() -> DEBUG.debugLogging);
    }

    public static void setDebugLoggingEnabled(boolean enabled) {
        setIfChanged(() -> DEBUG.debugLogging, v -> DEBUG.debugLogging = v, enabled);
    }

    public static boolean uiAnimationsEnabled() {
        return current(() -> ANIMATIONS.ui);
    }

    public static void setUiAnimationsEnabled(boolean enabled) {
        setIfChanged(() -> ANIMATIONS.ui, v -> ANIMATIONS.ui = v, enabled);
    }

    public static boolean contextMenuAnimationSettingEnabled() {
        return current(() -> ANIMATIONS.contextMenu);
    }

    public static boolean contextMenuAnimationsEnabled() {
        return animations(() -> ANIMATIONS.contextMenu);
    }

    public static void setContextMenuAnimationsEnabled(boolean enabled) {
        setIfChanged(() -> ANIMATIONS.contextMenu, v -> ANIMATIONS.contextMenu = v, enabled);
    }

    public static boolean toolsMenuAnimationSettingEnabled() {
        return current(() -> ANIMATIONS.toolsMenu);
    }

    public static boolean toolsMenuAnimationsEnabled() {
        return animations(() -> ANIMATIONS.toolsMenu);
    }

    public static void setToolsMenuAnimationsEnabled(boolean enabled) {
        setIfChanged(() -> ANIMATIONS.toolsMenu, v -> ANIMATIONS.toolsMenu = v, enabled);
    }

    public static boolean minimapAnimationSettingEnabled() {
        return current(() -> ANIMATIONS.minimap);
    }

    public static boolean minimapAnimationsEnabled() {
        return animations(() -> ANIMATIONS.minimap);
    }

    public static void setMinimapAnimationsEnabled(boolean enabled) {
        setIfChanged(() -> ANIMATIONS.minimap, v -> ANIMATIONS.minimap = v, enabled);
    }

    public static boolean questWindowAnimationSettingEnabled() {
        return current(() -> ANIMATIONS.questWindow);
    }

    public static boolean questWindowAnimationsEnabled() {
        return animations(() -> ANIMATIONS.questWindow);
    }

    public static void setQuestWindowAnimationsEnabled(boolean enabled) {
        setIfChanged(() -> ANIMATIONS.questWindow, v -> ANIMATIONS.questWindow = v, enabled);
    }

    public static boolean popupWindowAnimationSettingEnabled() {
        return current(() -> ANIMATIONS.popupWindow);
    }

    public static boolean popupWindowAnimationsEnabled() {
        return animations(() -> ANIMATIONS.popupWindow);
    }

    public static void setPopupWindowAnimationsEnabled(boolean enabled) {
        setIfChanged(() -> ANIMATIONS.popupWindow, v -> ANIMATIONS.popupWindow = v, enabled);
    }

    public static boolean connectionAnimationSettingEnabled() {
        return current(() -> ANIMATIONS.connection);
    }

    public static boolean connectionAnimationsEnabled() {
        return animations(() -> ANIMATIONS.connection);
    }

    public static void setConnectionAnimationsEnabled(boolean enabled) {
        setIfChanged(() -> ANIMATIONS.connection, v -> ANIMATIONS.connection = v, enabled);
    }

    public static boolean chapterSwitchAnimationSettingEnabled() {
        return current(() -> ANIMATIONS.chapterSwitch);
    }

    public static boolean chapterSwitchAnimationsEnabled() {
        return animations(() -> ANIMATIONS.chapterSwitch);
    }

    public static void setChapterSwitchAnimationsEnabled(boolean enabled) {
        setIfChanged(() -> ANIMATIONS.chapterSwitch, v -> ANIMATIONS.chapterSwitch = v, enabled);
    }

    public static boolean questHoverExpandAnimationSettingEnabled() {
        return current(() -> ANIMATIONS.questHoverExpand);
    }

    public static boolean questHoverExpandAnimationsEnabled() {
        return animations(() -> ANIMATIONS.questHoverExpand);
    }

    public static void setQuestHoverExpandAnimationsEnabled(boolean enabled) {
        setIfChanged(() -> ANIMATIONS.questHoverExpand, v -> ANIMATIONS.questHoverExpand = v, enabled);
    }

    public static int questHoverExpandPercent() {
        load();
        return ANIMATIONS.questHoverExpandPercent;
    }

    public static void setQuestHoverExpandPercent(int percent) {
        load();
        int normalized = normalizeQuestHoverExpandPercent(percent);
        if (ANIMATIONS.questHoverExpandPercent != normalized) {
            ANIMATIONS.questHoverExpandPercent = normalized;
            save();
        }
    }

    public static int normalizeQuestHoverExpandPercent(int percent) {
        return QuestsAndStuffConfigSections.Animations.normalizeHoverExpandPercent(percent);
    }

    public static boolean fullScreenModeEnabled() {
        return current(() -> CANVAS.fullScreenMode);
    }

    public static void setFullScreenModeEnabled(boolean enabled) {
        setIfChanged(() -> CANVAS.fullScreenMode, v -> CANVAS.fullScreenMode = v, enabled);
    }

    public static boolean minimapEnabled() {
        return current(() -> CANVAS.minimap);
    }

    public static void setMinimapEnabled(boolean enabled) {
        setIfChanged(() -> CANVAS.minimap, v -> CANVAS.minimap = v, enabled);
    }

    public static boolean visualMinimapEnabled() {
        return current(() -> CANVAS.visualMinimap);
    }

    public static void setVisualMinimapEnabled(boolean enabled) {
        setIfChanged(() -> CANVAS.visualMinimap, v -> CANVAS.visualMinimap = v, enabled);
    }

    public static boolean readOnlyCanvasFocusEnabled() {
        return current(() -> CANVAS.readOnlyFocus);
    }

    public static void setReadOnlyCanvasFocusEnabled(boolean enabled) {
        setIfChanged(() -> CANVAS.readOnlyFocus, v -> CANVAS.readOnlyFocus = v, enabled);
    }

    public static boolean questEffectIconsEnabled() {
        return current(() -> CANVAS.questEffectIcons);
    }

    public static void setQuestEffectIconsEnabled(boolean enabled) {
        setIfChanged(() -> CANVAS.questEffectIcons, v -> CANVAS.questEffectIcons = v, enabled);
    }

    public static boolean canvasMiniNotificationsEnabled() {
        return current(() -> CANVAS.miniNotifications);
    }

    public static void setCanvasMiniNotificationsEnabled(boolean enabled) {
        setIfChanged(() -> CANVAS.miniNotifications, v -> CANVAS.miniNotifications = v, enabled);
    }

    public static boolean autoClaimRewardsEnabled() {
        return current(() -> REWARDS.autoClaim);
    }

    public static void setAutoClaimRewardsEnabled(boolean enabled) {
        setIfChanged(() -> REWARDS.autoClaim, v -> REWARDS.autoClaim = v, enabled);
    }

    public static boolean commandRewardsEnabled() {
        return current(() -> SECURITY.commandRewards);
    }

    public static void setCommandRewardsEnabled(boolean enabled) {
        setIfChanged(() -> SECURITY.commandRewards, v -> SECURITY.commandRewards = v, enabled);
    }

    public static boolean completionHudEnabled() {
        return current(() -> HUD.enabled);
    }

    public static void setCompletionHudEnabled(boolean enabled) {
        setIfChanged(() -> HUD.enabled, v -> HUD.enabled = v, enabled);
    }

    public static boolean completionHudSoundEnabled() {
        return current(() -> HUD.sound);
    }

    public static void setCompletionHudSoundEnabled(boolean enabled) {
        setIfChanged(() -> HUD.sound, v -> HUD.sound = v, enabled);
    }

    public static int completionHudDurationMs() {
        load();
        return HUD.durationMs;
    }

    public static void setCompletionHudDurationMs(int durationMs) {
        load();
        int normalized = normalizeCompletionHudDurationMs(durationMs);
        if (HUD.durationMs != normalized) {
            HUD.durationMs = normalized;
            save();
        }
    }

    public static int normalizeCompletionHudDurationMs(int durationMs) {
        return QuestsAndStuffConfigSections.Hud.normalizeDurationMs(durationMs);
    }

    public static boolean chunkClaimProtectBreakPlace() {
        return current(() -> CHUNK_CLAIMS.protectBreakPlace);
    }

    public static boolean chunkClaimProtectInteraction() {
        return current(() -> CHUNK_CLAIMS.protectInteraction);
    }

    public static boolean chunkClaimProtectExplosions() {
        return current(() -> CHUNK_CLAIMS.protectExplosions);
    }

    public static boolean chunkClaimProtectMobGriefing() {
        return current(() -> CHUNK_CLAIMS.protectMobGriefing);
    }

    public static boolean chunkClaimProtectPvp() {
        return current(() -> CHUNK_CLAIMS.protectPvp);
    }

    public static boolean chunkClaimProtectFire() {
        return current(() -> CHUNK_CLAIMS.protectFire);
    }

    public static int chunkClaimMaxClaimedChunks() {
        load();
        return CHUNK_CLAIMS.maxClaimedChunks;
    }

    public static int chunkClaimMaxForceLoadedChunks() {
        load();
        return CHUNK_CLAIMS.maxForceLoadedChunks;
    }

    public static int minChunkClaimCap() {
        return QuestsAndStuffConfigSections.ChunkClaims.MIN_CAP;
    }

    public static int maxChunkClaimCap() {
        return QuestsAndStuffConfigSections.ChunkClaims.MAX_CAP;
    }

    public static boolean setChunkClaimProtectBreakPlace(boolean value) {
        return setIfChanged(() -> CHUNK_CLAIMS.protectBreakPlace, v -> CHUNK_CLAIMS.protectBreakPlace = v, value);
    }

    public static boolean setChunkClaimProtectInteraction(boolean value) {
        return setIfChanged(() -> CHUNK_CLAIMS.protectInteraction, v -> CHUNK_CLAIMS.protectInteraction = v, value);
    }

    public static boolean setChunkClaimProtectExplosions(boolean value) {
        return setIfChanged(() -> CHUNK_CLAIMS.protectExplosions, v -> CHUNK_CLAIMS.protectExplosions = v, value);
    }

    public static boolean setChunkClaimProtectMobGriefing(boolean value) {
        return setIfChanged(() -> CHUNK_CLAIMS.protectMobGriefing, v -> CHUNK_CLAIMS.protectMobGriefing = v, value);
    }

    public static boolean setChunkClaimProtectPvp(boolean value) {
        return setIfChanged(() -> CHUNK_CLAIMS.protectPvp, v -> CHUNK_CLAIMS.protectPvp = v, value);
    }

    public static boolean setChunkClaimProtectFire(boolean value) {
        return setIfChanged(() -> CHUNK_CLAIMS.protectFire, v -> CHUNK_CLAIMS.protectFire = v, value);
    }

    public static boolean setChunkClaimMaxClaimedChunks(int value) {
        load();
        int normalized = QuestsAndStuffConfigSections.ChunkClaims.normalizeCap(value);
        if (CHUNK_CLAIMS.maxClaimedChunks != normalized) {
            CHUNK_CLAIMS.maxClaimedChunks = normalized;
            save();
        }
        return true;
    }

    public static boolean setChunkClaimMaxForceLoadedChunks(int value) {
        load();
        int normalized = QuestsAndStuffConfigSections.ChunkClaims.normalizeCap(value);
        if (CHUNK_CLAIMS.maxForceLoadedChunks != normalized) {
            CHUNK_CLAIMS.maxForceLoadedChunks = normalized;
            save();
        }
        return true;
    }

    public static boolean updateChunkClaims(boolean protectBreakPlace, boolean protectInteraction,
                                            boolean protectExplosions, boolean protectMobGriefing, boolean protectPvp,
                                            boolean protectFire,
                                            int maxClaimedChunks, int maxForceLoadedChunks) {
        load();
        CHUNK_CLAIMS.protectBreakPlace = protectBreakPlace;
        CHUNK_CLAIMS.protectInteraction = protectInteraction;
        CHUNK_CLAIMS.protectExplosions = protectExplosions;
        CHUNK_CLAIMS.protectMobGriefing = protectMobGriefing;
        CHUNK_CLAIMS.protectPvp = protectPvp;
        CHUNK_CLAIMS.protectFire = protectFire;
        CHUNK_CLAIMS.maxClaimedChunks = QuestsAndStuffConfigSections.ChunkClaims.normalizeCap(maxClaimedChunks);
        CHUNK_CLAIMS.maxForceLoadedChunks = QuestsAndStuffConfigSections.ChunkClaims.normalizeCap(maxForceLoadedChunks);
        save();
        return true;
    }

    public static boolean itemLockBlockUse() {
        return current(() -> ITEM_LOCKS.blockUse);
    }

    public static boolean itemLockBlockPickup() {
        return current(() -> ITEM_LOCKS.blockPickup);
    }

    public static boolean itemLockBlockEquip() {
        return current(() -> ITEM_LOCKS.blockEquip);
    }

    public static boolean itemLockLockedTooltips() {
        return current(() -> ITEM_LOCKS.lockedTooltips);
    }

    public static boolean updateItemLocks(boolean blockUse, boolean blockPickup, boolean blockEquip, boolean lockedTooltips) {
        load();
        ITEM_LOCKS.blockUse = blockUse;
        ITEM_LOCKS.blockPickup = blockPickup;
        ITEM_LOCKS.blockEquip = blockEquip;
        ITEM_LOCKS.lockedTooltips = lockedTooltips;
        save();
        return true;
    }

    private static void read(JsonObject root) {
        DEBUG.read(QuestsAndStuffConfigSections.object(root, "debug"));
        ANIMATIONS.read(QuestsAndStuffConfigSections.object(root, "animations"));
        CANVAS.read(QuestsAndStuffConfigSections.object(root, "canvas"));
        REWARDS.read(QuestsAndStuffConfigSections.object(root, "rewards"));
        HUD.read(QuestsAndStuffConfigSections.object(root, "hud"));
        SECURITY.read(QuestsAndStuffConfigSections.object(root, "security"));
        CHUNK_CLAIMS.read(QuestsAndStuffConfigSections.object(root, "chunkClaims"));
        ITEM_LOCKS.read(QuestsAndStuffConfigSections.object(root, "itemLocks"));
    }

    private static synchronized void save() {
        JsonObject root = new JsonObject();
        root.add("debug", DEBUG.write());
        root.add("animations", ANIMATIONS.write());
        root.add("canvas", CANVAS.write());
        root.add("rewards", REWARDS.write());
        root.add("hud", HUD.write());
        root.add("security", SECURITY.write());
        root.add("chunkClaims", CHUNK_CLAIMS.write());
        root.add("itemLocks", ITEM_LOCKS.write());

        Path file = configFile();
        try {
            Path parent = file.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.writeString(file, GSON.toJson(root), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (Exception e) {
            QuestsAndStuffMod.LOGGER.warn("Failed writing Quests and Stuff config {}", file, e);
        }
    }

    private static Path configFile() {
        return Services.platform().configDir().resolve(QuestsAndStuffMod.MODID).resolve("config.json");
    }
}
