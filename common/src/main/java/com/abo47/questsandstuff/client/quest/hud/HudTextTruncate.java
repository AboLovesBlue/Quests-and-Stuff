package com.abo47.questsandstuff.client.quest.hud;

import net.minecraft.client.gui.Font;

final class HudTextTruncate {
    private static final String ELLIPSIS = "...";

    private HudTextTruncate() {
    }

    static String cropToWidth(Font font, String value, int width) {
        String safe = value == null ? "" : value;
        if (width <= 0 || safe.isBlank()) {
            return "";
        }
        if (font.width(safe) <= width) {
            return safe;
        }
        if (font.width(ELLIPSIS) > width) {
            return safe.substring(0, largestFittingLength(font, safe, "", width));
        }
        int cut = largestFittingLength(font, safe, ELLIPSIS, width);
        return cut == 0 ? ELLIPSIS : safe.substring(0, cut) + ELLIPSIS;
    }

    private static int largestFittingLength(Font font, String value, String suffix, int width) {
        int lo = 0;
        int hi = value.length();
        while (lo < hi) {
            int mid = (lo + hi + 1) / 2;
            if (font.width(value.substring(0, mid) + suffix) <= width) {
                lo = mid;
            } else {
                hi = mid - 1;
            }
        }
        return lo;
    }
}
