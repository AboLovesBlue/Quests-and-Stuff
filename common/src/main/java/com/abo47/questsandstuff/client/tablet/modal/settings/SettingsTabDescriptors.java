package com.abo47.questsandstuff.client.tablet.modal.settings;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.resources.language.I18n;

import com.abo47.questsandstuff.client.tablet.controls.SearchFilter;
import com.abo47.questsandstuff.client.tablet.state.UiState;
import com.abo47.questsandstuff.client.tablet.text.ChunkClaimTranslationKeys;

public final class SettingsTabDescriptors {
    public static final int THEMES = 0;
    public static final int CANVAS = 1;
    public static final int HUD = 2;
    public static final int ANIMATIONS = 3;
    public static final int DEBUG = 4;
    public static final int SKIN = 5;
    public static final int CHUNK_CLAIMS = 6;

    private static final List<SettingsTabDescriptor> TABS = List.of(
            new SettingsTabDescriptor(THEMES, "themes", "ui.questsandstuff.settings.tab_themes", ThemeSettingsOptions::options),
            new SettingsTabDescriptor(CANVAS, "canvas", "ui.questsandstuff.settings.tab_canvas", CanvasSettingsOptions::options),
            new SettingsTabDescriptor(HUD, "hud", "ui.questsandstuff.settings.tab_hud", HudSettingsOptions::options),
            new SettingsTabDescriptor(ANIMATIONS, "animations", "ui.questsandstuff.settings.tab_animations", AnimationSettingsOptions::options),
            new SettingsTabDescriptor(DEBUG, "debug", "ui.questsandstuff.settings.tab_debug", DebugSettingsOptions::options),
            new SettingsTabDescriptor(SKIN, "skin", "ui.questsandstuff.settings.tab_skin", SkinSettingsOptions::options),
            new SettingsTabDescriptor(CHUNK_CLAIMS, "chunkClaims", ChunkClaimTranslationKeys.SETTINGS_TAB, ChunkClaimSettingsOptions::options)
    );

    private SettingsTabDescriptors() {
    }

    public static List<SettingsTabDescriptor> all() {
        return TABS;
    }

    public static int activeTab(int tab) {
        return descriptor(tab).id();
    }

    static SettingsTabDescriptor active(UiState state) {
        return descriptor(state.settings.currentTab);
    }

    public static SettingsTabDescriptor descriptor(int tab) {
        for (SettingsTabDescriptor descriptor : TABS) {
            if (descriptor.id() == tab) {
                return descriptor;
            }
        }
        return TABS.get(0);
    }

    public static List<SettingsOptionDescriptor> search(UiState state, String query) {
        String q = SearchFilter.normalizeUserInput(query);
        List<SettingsOptionDescriptor> matches = new ArrayList<>();
        for (SettingsTabDescriptor tab : TABS) {
            for (SettingsOptionDescriptor option : tab.options(state)) {
                if (optionMatches(option, q)) {
                    matches.add(option);
                }
            }
        }
        return matches;
    }

    private static boolean optionMatches(SettingsOptionDescriptor option, String q) {
        if (q.isBlank()) {
            return true;
        }
        String label = SearchFilter.normalizeUserInput(I18n.get(option.labelKey()));
        if (label.contains(q)) {
            return true;
        }
        String description = SearchFilter.normalizeUserInput(I18n.get(option.descriptionKey()));
        return description.contains(q);
    }
}
