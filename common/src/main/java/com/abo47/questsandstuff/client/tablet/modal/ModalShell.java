package com.abo47.questsandstuff.client.tablet.modal;

import java.util.function.Consumer;

import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import com.abo47.questsandstuff.client.tablet.controls.StyledTextFields;
import com.abo47.questsandstuff.client.tablet.state.UiState;
import com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory;
import com.abo47.questsandstuff.client.tablet.theme.tokens.ThemeColors;

import static com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory.withAlpha;
import static com.abo47.questsandstuff.client.tablet.ui.factory.UiFactory.label;

public final class ModalShell {
    public static final int SEARCH_MAX_LENGTH = 80;
    public static final int SEARCH_MED = 96;
    public static final int SEARCH_LONG = 120;
    public static final int PICKER_HEADER_Y = 24;
    public static final int PICKER_SEARCH_H = 16;
    public static final int PICKER_LIST_Y = 46;
    public static final int MODAL_MAX_W = 432;
    public static final int MODAL_MAX_H = 260;
    public static final int MODAL_MARGIN = 32;

    private ModalShell() {
    }

    public static void addTitleAndClose(WidgetGroup modal, String title, int w, UiState state, Runnable refresh) {
        modal.addWidget(label(8, 6, title, ThemeColors.TEXT_PRIMARY));
        ModalPanel.addModalClose(modal, w - 26, 4, 18, state, refresh);
    }

    public static TextFieldWidget addSearchField(
            WidgetGroup modal,
            int x,
            int y,
            int w,
            int h,
            String current,
            int maxLength,
            Consumer<String> responder,
            Consumer<Boolean> focusResponder
    ) {
        TextFieldWidget field = StyledTextFields.search(x, y, w, h, current, maxLength, responder, focusResponder);
        modal.addWidget(field);
        return field;
    }

    public static WidgetGroup bodyPanel(int x, int y, int w, int h) {
        return SurfaceFactory.panel(x, y, w, h, withAlpha(ThemeColors.elevatedSurface(), 190), ThemeColors.subtleBorder());
    }
}
