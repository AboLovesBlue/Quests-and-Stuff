package com.abo47.questsandstuff.client.tablet.contextmenu;

import org.junit.jupiter.api.Test;

import com.abo47.questsandstuff.client.tablet.state.UiState;
import com.abo47.questsandstuff.client.tablet.theme.tokens.ThemeColors;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ContextActionToneTest {
    @Test
    void factoryActionsCarryExplicitTones() {
        assertTone(ContextActionFactory.add("Add", () -> {
        }), ActionTone.SUCCESS, ThemeColors.SUCCESS);
        assertTone(ContextActionFactory.copy(() -> {
        }), ActionTone.PRIMARY, ThemeColors.INTERACTIVE);
        assertTone(ContextActionFactory.rename("Rename", () -> {
        }), ActionTone.PRIMARY, ThemeColors.INTERACTIVE);
    }

    @Test
    void destructiveFactoriesCarryDestructiveTones() {
        UiState state = new UiState();

        ContextAction delete = ContextActionFactory.delete(state, "quest", "Delete quest", () -> {
        });
        ContextAction warningDelete = ContextActionFactory.warningDelete(state, "quest", "Remove background", () -> {
        });

        assertTone(delete, ActionTone.DANGER, ThemeColors.ERROR);
        assertTone(warningDelete, ActionTone.WARNING, ThemeColors.WARNING);
    }

    @Test
    void legacyColorsMapToTonesWithoutLabelOrIconInference() {
        ContextAction neutralDeleteText = ContextActionFactory.action("Delete translated", "delete", ThemeColors.TEXT_MUTED, () -> {
        });
        ContextAction warningColor = ContextActionFactory.action("Plain label", "plain", ThemeColors.WARNING, () -> {
        });

        assertTone(neutralDeleteText, ActionTone.NEUTRAL, ThemeColors.TEXT_MUTED);
        assertTone(warningColor, ActionTone.WARNING, ThemeColors.WARNING);
    }

    @Test
    void explicitToneWinsOverTextAndIcon() {
        ContextAction successDeleteIcon = new ContextAction("Delete translated", "delete", ActionTone.SUCCESS, () -> {
        });

        assertTone(successDeleteIcon, ActionTone.SUCCESS, ThemeColors.SUCCESS);
    }

    private static void assertTone(ContextAction action, ActionTone tone, int accentColor) {
        assertEquals(tone, action.tone());
        assertEquals(accentColor, action.accentColor());
    }
}
