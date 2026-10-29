package com.abo47.questsandstuff.client.tablet.theme.skin;

enum SkinFillMode {
    STRETCH("stretch", "size", "ui.questsandstuff.skin.mode_stretch", 0, 0, 0, 0),
    TILE("tile", "brick-wall", "ui.questsandstuff.skin.mode_tile", 0, 0, 0, 0,
            "ui.questsandstuff.skin.tile_size_w", "ui.questsandstuff.skin.tile_size_h"),
    CENTER("center", "original_size", "ui.questsandstuff.skin.mode_original_size", 0, 0, 0, 0),
    DYNAMIC("dynamic", "dynamic", "ui.questsandstuff.skin.mode_dynamic", 1, 1, 1, 1,
            "ui.questsandstuff.skin.dynamic_left", "ui.questsandstuff.skin.dynamic_right",
            "ui.questsandstuff.skin.dynamic_top", "ui.questsandstuff.skin.dynamic_bottom"),
    HRSTRETCH("hrstretch", "repeat", "ui.questsandstuff.skin.mode_hrstretch", 0, 0, 0, 0,
            "ui.questsandstuff.skin.hrstretch_left", "ui.questsandstuff.skin.hrstretch_right");

    final String mode;
    final String icon;
    final String titleKey;
    final int defaultLeft;
    final int defaultRight;
    final int defaultTop;
    final int defaultBottom;
    final String leftLabelKey;
    final String rightLabelKey;
    final String topLabelKey;
    final String bottomLabelKey;

    SkinFillMode(String mode, String icon, String titleKey, int dl, int dr, int dt, int db, String... labelKeys) {
        this.mode = mode;
        this.icon = icon;
        this.titleKey = titleKey;
        this.defaultLeft = dl;
        this.defaultRight = dr;
        this.defaultTop = dt;
        this.defaultBottom = db;
        this.leftLabelKey = labelKeys.length >= 2 ? labelKeys[0] : null;
        this.rightLabelKey = labelKeys.length >= 2 ? labelKeys[1] : null;
        this.topLabelKey = labelKeys.length >= 4 ? labelKeys[2] : null;
        this.bottomLabelKey = labelKeys.length >= 4 ? labelKeys[3] : null;
    }
}
