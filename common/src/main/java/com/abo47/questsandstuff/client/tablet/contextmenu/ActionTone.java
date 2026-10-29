package com.abo47.questsandstuff.client.tablet.contextmenu;

import com.abo47.questsandstuff.client.tablet.theme.tokens.ThemeColors;

public enum ActionTone {
    NEUTRAL,
    PRIMARY,
    SUCCESS,
    WARNING,
    DANGER;

    public int accentColor(int fallback) {
        return switch (this) {
            case NEUTRAL -> fallback == 0 ? ThemeColors.TEXT_MUTED : fallback;
            case PRIMARY -> ThemeColors.INTERACTIVE;
            case SUCCESS -> ThemeColors.SUCCESS;
            case WARNING -> ThemeColors.WARNING;
            case DANGER -> ThemeColors.ERROR;
        };
    }

    public int defaultAccentColor() {
        return accentColor(0);
    }

    public boolean destructive() {
        return this == WARNING || this == DANGER;
    }

    public static ActionTone fromLegacyColor(int color) {
        if (color == ThemeColors.ERROR) {
            return DANGER;
        }
        if (color == ThemeColors.WARNING) {
            return WARNING;
        }
        if (color == ThemeColors.SUCCESS) {
            return SUCCESS;
        }
        if (color == ThemeColors.INTERACTIVE) {
            return PRIMARY;
        }
        return NEUTRAL;
    }
}
