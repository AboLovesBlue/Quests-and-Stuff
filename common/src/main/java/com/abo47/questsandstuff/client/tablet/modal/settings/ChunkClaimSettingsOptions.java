package com.abo47.questsandstuff.client.tablet.modal.settings;

import java.util.List;

import com.abo47.questsandstuff.QuestsAndStuffConfig;
import com.abo47.questsandstuff.client.tablet.state.UiState;
import com.abo47.questsandstuff.client.tablet.text.ChunkClaimTranslationKeys;
import com.abo47.questsandstuff.network.ModNetwork;
import com.abo47.questsandstuff.network.chunkclaim.C2SChunkClaimConfigPacket;

final class ChunkClaimSettingsOptions {
    private ChunkClaimSettingsOptions() {
    }

    static List<SettingsOptionDescriptor> options(UiState state) {
        int capMin = QuestsAndStuffConfig.minChunkClaimCap();
        int capMax = QuestsAndStuffConfig.maxChunkClaimCap();
        return List.of(
                new SettingsOptionDescriptor(
                        "protectBreakPlace",
                        ChunkClaimTranslationKeys.SETTING_PROTECT_BREAK_PLACE,
                        ChunkClaimTranslationKeys.SETTING_PROTECT_BREAK_PLACE + "_desc",
                        QuestsAndStuffConfig::chunkClaimProtectBreakPlace,
                        enabled -> {
                            QuestsAndStuffConfig.setChunkClaimProtectBreakPlace(enabled);
                            sendChunkClaimsConfig();
                        },
                        false,
                        false
                ),
                new SettingsOptionDescriptor(
                        "protectInteraction",
                        ChunkClaimTranslationKeys.SETTING_PROTECT_INTERACTION,
                        ChunkClaimTranslationKeys.SETTING_PROTECT_INTERACTION + "_desc",
                        QuestsAndStuffConfig::chunkClaimProtectInteraction,
                        enabled -> {
                            QuestsAndStuffConfig.setChunkClaimProtectInteraction(enabled);
                            sendChunkClaimsConfig();
                        },
                        false,
                        false
                ),
                new SettingsOptionDescriptor(
                        "protectExplosions",
                        ChunkClaimTranslationKeys.SETTING_PROTECT_EXPLOSIONS,
                        ChunkClaimTranslationKeys.SETTING_PROTECT_EXPLOSIONS + "_desc",
                        QuestsAndStuffConfig::chunkClaimProtectExplosions,
                        enabled -> {
                            QuestsAndStuffConfig.setChunkClaimProtectExplosions(enabled);
                            sendChunkClaimsConfig();
                        },
                        false,
                        false
                ),
                new SettingsOptionDescriptor(
                        "protectMobGriefing",
                        ChunkClaimTranslationKeys.SETTING_PROTECT_MOB_GRIEFING,
                        ChunkClaimTranslationKeys.SETTING_PROTECT_MOB_GRIEFING + "_desc",
                        QuestsAndStuffConfig::chunkClaimProtectMobGriefing,
                        enabled -> {
                            QuestsAndStuffConfig.setChunkClaimProtectMobGriefing(enabled);
                            sendChunkClaimsConfig();
                        },
                        false,
                        false
                ),
                new SettingsOptionDescriptor(
                        "protectPvp",
                        ChunkClaimTranslationKeys.SETTING_PROTECT_PVP,
                        ChunkClaimTranslationKeys.SETTING_PROTECT_PVP + "_desc",
                        QuestsAndStuffConfig::chunkClaimProtectPvp,
                        enabled -> {
                            QuestsAndStuffConfig.setChunkClaimProtectPvp(enabled);
                            sendChunkClaimsConfig();
                        },
                        false,
                        false
                ),
                new SettingsOptionDescriptor(
                        "protectFire",
                        ChunkClaimTranslationKeys.SETTING_PROTECT_FIRE,
                        ChunkClaimTranslationKeys.SETTING_PROTECT_FIRE + "_desc",
                        QuestsAndStuffConfig::chunkClaimProtectFire,
                        enabled -> {
                            QuestsAndStuffConfig.setChunkClaimProtectFire(enabled);
                            sendChunkClaimsConfig();
                        },
                        false,
                        false
                ),
                new SettingsOptionDescriptor(
                        "maxClaimedChunks",
                        ChunkClaimTranslationKeys.SETTING_MAX_CLAIMED,
                        ChunkClaimTranslationKeys.SETTING_MAX_CLAIMED + "_desc",
                        QuestsAndStuffConfig::chunkClaimMaxClaimedChunks,
                        value -> {
                            QuestsAndStuffConfig.setChunkClaimMaxClaimedChunks(value);
                            sendChunkClaimsConfig();
                        },
                        capMin,
                        capMax,
                        10,
                        false,
                        "ui.questsandstuff.settings.chunk_unit"
                ),
                new SettingsOptionDescriptor(
                        "maxForceLoadedChunks",
                        ChunkClaimTranslationKeys.SETTING_MAX_FORCE_LOADED,
                        ChunkClaimTranslationKeys.SETTING_MAX_FORCE_LOADED + "_desc",
                        QuestsAndStuffConfig::chunkClaimMaxForceLoadedChunks,
                        value -> {
                            QuestsAndStuffConfig.setChunkClaimMaxForceLoadedChunks(value);
                            sendChunkClaimsConfig();
                        },
                        capMin,
                        capMax,
                        10,
                        false,
                        "ui.questsandstuff.settings.chunk_unit"
                )
        );
    }

    private static void sendChunkClaimsConfig() {
        ModNetwork.sendToServer(new C2SChunkClaimConfigPacket(
                QuestsAndStuffConfig.chunkClaimProtectBreakPlace(),
                QuestsAndStuffConfig.chunkClaimProtectInteraction(),
                QuestsAndStuffConfig.chunkClaimProtectExplosions(),
                QuestsAndStuffConfig.chunkClaimProtectMobGriefing(),
                QuestsAndStuffConfig.chunkClaimProtectPvp(),
                QuestsAndStuffConfig.chunkClaimProtectFire(),
                QuestsAndStuffConfig.chunkClaimMaxClaimedChunks(),
                QuestsAndStuffConfig.chunkClaimMaxForceLoadedChunks()
        ));
    }
}
