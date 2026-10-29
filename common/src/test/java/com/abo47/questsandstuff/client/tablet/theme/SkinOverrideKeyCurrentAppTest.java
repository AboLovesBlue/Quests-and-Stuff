package com.abo47.questsandstuff.client.tablet.theme;

import org.junit.jupiter.api.Test;

import com.abo47.questsandstuff.client.tablet.state.UiState;
import com.abo47.questsandstuff.client.tablet.theme.skin.SkinOverrideKey;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class SkinOverrideKeyCurrentAppTest {
    @Test
    void qualifiedEntryOnlyMatchesCurrentApp() {
        UiState state = new UiState();
        state.root.currentApp = "quest";
        assertEquals("quests_canvas", SkinOverrideKey.bareKeyForCurrentApp(state, "quest:quests_canvas"));
        assertNull(SkinOverrideKey.bareKeyForCurrentApp(state, "teams:quests_canvas"));
        assertEquals("home_inner", SkinOverrideKey.bareKeyForCurrentApp(state, "home_inner"));
    }

    @Test
    void blankAppRejectsQualifiedEntriesInsteadOfAcceptingAll() {
        UiState state = new UiState();
        assertNull(SkinOverrideKey.bareKeyForCurrentApp(state, "quest:quests_canvas"));
        assertEquals("home_inner", SkinOverrideKey.bareKeyForCurrentApp(state, "home_inner"));
    }
}
