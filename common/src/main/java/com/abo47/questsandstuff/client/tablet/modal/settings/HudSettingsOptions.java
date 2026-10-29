package com.abo47.questsandstuff.client.tablet.modal.settings;

import java.util.List;

import net.minecraft.client.Minecraft;

import com.abo47.questsandstuff.QuestsAndStuffConfig;
import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.quest.hud.QuestHudLayoutEditScreen;
import com.abo47.questsandstuff.client.tablet.state.UiState;

final class HudSettingsOptions {
    private HudSettingsOptions() {
    }

    static List<SettingsOptionDescriptor> options(UiState state) {
        return List.of(
                new SettingsOptionDescriptor(
                        "editHudLayout",
                        "ui.questsandstuff.hud.layout.button",
                        "ui.questsandstuff.hud.layout.button_tooltip",
                        () -> {
                            Minecraft.getInstance().setScreen(new QuestHudLayoutEditScreen());
                            QuestsAndStuffMod.debugLog("[QnS:UI] hud layout editor opened from settings");
                        }
                ),
                new SettingsOptionDescriptor(
                        "completionHud",
                        "ui.questsandstuff.settings.completion_hud",
                        "ui.questsandstuff.settings.completion_hud_desc",
                        QuestsAndStuffConfig::completionHudEnabled,
                        QuestsAndStuffConfig::setCompletionHudEnabled,
                        false,
                        false
                ),
                new SettingsOptionDescriptor(
                        "completionHudSound",
                        "ui.questsandstuff.settings.completion_hud_sound",
                        "ui.questsandstuff.settings.completion_hud_sound_desc",
                        QuestsAndStuffConfig::completionHudSoundEnabled,
                        QuestsAndStuffConfig::setCompletionHudSoundEnabled,
                        false,
                        false
                ),
                new SettingsOptionDescriptor(
                        "completionHudDurationMs",
                        "ui.questsandstuff.settings.completion_hud_duration",
                        "ui.questsandstuff.settings.completion_hud_duration_desc",
                        QuestsAndStuffConfig::completionHudDurationMs,
                        QuestsAndStuffConfig::setCompletionHudDurationMs,
                        QuestsAndStuffConfig.MIN_COMPLETION_HUD_DURATION_MS,
                        QuestsAndStuffConfig.MAX_COMPLETION_HUD_DURATION_MS,
                        5,
                        false,
                        "ui.questsandstuff.settings.duration_unit_ms"
                )
        );
    }
}
