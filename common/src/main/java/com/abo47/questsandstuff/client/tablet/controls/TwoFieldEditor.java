package com.abo47.questsandstuff.client.tablet.controls;

import java.util.function.BiConsumer;

import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.state.UiState;
import com.abo47.questsandstuff.client.tablet.text.TranslationKeys;
import com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory;
import com.abo47.questsandstuff.client.tablet.theme.tokens.ThemeColors;

import static com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory.withAlpha;
import static com.abo47.questsandstuff.client.tablet.ui.factory.UiFactory.flatHitButton;
import static com.abo47.questsandstuff.client.tablet.ui.factory.UiFactory.label;
import static com.abo47.questsandstuff.client.tablet.ui.factory.UiFactory.panel;

public final class TwoFieldEditor {
    private TwoFieldEditor() {
    }

    public static WidgetGroup build(UiState state, int x, int y, int w, int h,
            String titleKey, String leftLabelKey, String rightLabelKey,
            int left, int right, BiConsumer<Integer, Integer> onApply, Runnable onCancel) {
        int pad = 8;
        int buttonW = 82;
        int buttonH = 16;
        int buttonY = h - pad - buttonH;
        int[] edges = { left, right };
        Runnable apply = () -> onApply.accept(edges[0], edges[1]);

        WidgetGroup popup = panel(x, y, w, h, withAlpha(ThemeColors.SURFACE_BASE, 246), ThemeColors.BORDER_ACCENT);
        popup.addWidget(flatHitButton(0, 0, w, h, click -> {}));
        popup.addWidget(label(pad, 6, TranslationKeys.text(titleKey), ThemeColors.TEXT_PRIMARY));

        popup.addWidget(label(pad, 26, TranslationKeys.text(leftLabelKey), ThemeColors.TEXT_SECONDARY));
        TextFieldWidget leftField = StyledTextFields.integerField(
                pad, 38, w - pad * 2, 16,
                 left, 0, Integer.MAX_VALUE, 4,
                v -> {
                    try {
                        edges[0] = Integer.parseInt(v);
                    } catch (NumberFormatException ignored) {
                    }
                },
                apply, onCancel, () -> {});
        leftField.setClientSideWidget();
        StyledTextFields.applyStandardStyle(leftField, ThemeColors.SURFACE_PANEL_ALT, ThemeColors.BORDER_BASE);
        popup.addWidget(leftField);

        popup.addWidget(label(pad, 60, TranslationKeys.text(rightLabelKey), ThemeColors.TEXT_SECONDARY));
        TextFieldWidget rightField = StyledTextFields.integerField(
                pad, 72, w - pad * 2, 16,
                 right, 0, Integer.MAX_VALUE, 4,
                v -> {
                    try {
                        edges[1] = Integer.parseInt(v);
                    } catch (NumberFormatException ignored) {
                    }
                },
                apply, onCancel, () -> {});
        rightField.setClientSideWidget();
        StyledTextFields.applyStandardStyle(rightField, ThemeColors.SURFACE_PANEL_ALT, ThemeColors.BORDER_BASE);
        popup.addWidget(rightField);

        ActionButtons.iconAction(popup, pad, buttonY, buttonW, buttonH, "add", TranslationKeys.text(TranslationKeys.COMMON_SAVE), ThemeColors.SUCCESS, null, click -> apply.run());
        ActionButtons.iconAction(popup, w - pad - buttonW, buttonY, buttonW, buttonH, "close", TranslationKeys.text(TranslationKeys.COMMON_CANCEL), ThemeColors.ERROR, null, click -> onCancel.run());

        leftField.setFocus(true);
        QuestsAndStuffMod.debugLog("[QnS:Skin] TwoFieldEditor opened: title={}, left={}, right={}", titleKey, left, right);
        return popup;
    }
}
