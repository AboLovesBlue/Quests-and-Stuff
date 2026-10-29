package com.abo47.questsandstuff.client.tablet.quest.chapter;

import java.util.function.Consumer;
import java.util.function.Supplier;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import com.abo47.questsandstuff.client.tablet.controls.InlineRenameField;
import com.abo47.questsandstuff.client.tablet.icons.DisplayIconWidget;
import com.abo47.questsandstuff.client.tablet.icons.IconAtlas;
import com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory;
import com.abo47.questsandstuff.client.tablet.theme.tokens.ThemeColors;
import com.abo47.questsandstuff.client.tablet.ui.factory.UiFactory;
import com.abo47.questsandstuff.util.naming.QuestIdentity;

import static com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory.withAlpha;

final class ChapterInlineRenameRows {
    private ChapterInlineRenameRows() {
    }

    static void add(
            WidgetGroup chapterList,
            int cardX,
            int y,
            int cardW,
            int iconX,
            String icon,
            Supplier<String> value,
            Consumer<String> updateValue,
            Consumer<String> commit,
            Runnable cancel
    ) {
        chapterList.addWidget(UiFactory.panel(
                cardX,
                y,
                cardW,
                UiFactory.CHAPTER_CARD_H,
                withAlpha(ThemeColors.INTERACTIVE, 108),
                ThemeColors.BORDER_ACCENT
        ));
        if (icon != null && !icon.isBlank()) {
            chapterList.addWidget(new DisplayIconWidget(iconX, y + 8, UiFactory.CONTENT_ICON_SIZE, UiFactory.CONTENT_ICON_SIZE, icon));
        }

        int doneW = 24;
        int doneX = cardX + cardW - doneW - 2;
        int fieldX = cardX + 24;
        int fieldW = Math.max(18, doneX - fieldX - 3);
        InlineRenameField field = new InlineRenameField(
                fieldX,
                y + 8,
                fieldW,
                16,
                value,
                updateValue,
                () -> commit.accept(value.get()),
                cancel,
                null,
                null
        );
        field.setClientSideWidget();
        field.setMaxStringLength(QuestIdentity.UI_CHAPTER_NAME_MAX_LENGTH);
        field.setBordered(false);
        field.setBackground(SurfaceFactory.bordered(ThemeColors.SURFACE_BASE, ThemeColors.INTERACTIVE));
        field.setTextColor(ThemeColors.TEXT_PRIMARY);
        chapterList.addWidget(field);
        addFlatIconAction(chapterList, doneX + 4, y + 10, UiFactory.ACTION_ICON_SIZE, "add.png", click -> commit.accept(field.getCurrentString()));
    }

    private static void addFlatIconAction(WidgetGroup parent, int x, int y, int size, String iconName, java.util.function.Consumer<com.lowdragmc.lowdraglib.gui.util.ClickData> callback) {
        IGuiTexture texture = IconAtlas.iconTexture(iconName);
        if (texture != null) {
            parent.addWidget(new ImageWidget(x, y, size, size, texture));
        }
        parent.addWidget(UiFactory.flatHitButton(x, y, size, size, callback));
    }
}
