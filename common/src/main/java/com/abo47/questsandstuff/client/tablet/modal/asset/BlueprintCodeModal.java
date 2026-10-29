package com.abo47.questsandstuff.client.tablet.modal.asset;

import java.util.List;

import net.minecraft.client.Minecraft;

import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.codeeditor.CodeEditorWidget;

import com.abo47.questsandstuff.QuestsAndStuffConfig;
import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.animation.SourceOriginRevealWidget;
import com.abo47.questsandstuff.client.tablet.quest.canvas.blueprint.CanvasBlueprintStore;
import com.abo47.questsandstuff.client.tablet.state.UiState;
import com.abo47.questsandstuff.client.tablet.ui.state.ModalState;
import com.abo47.questsandstuff.client.tablet.theme.codec.UiThemeManager;
import com.abo47.questsandstuff.client.tablet.theme.render.ChromeFactory;
import com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory;
import com.abo47.questsandstuff.client.tablet.theme.tokens.ThemeColors;
import com.abo47.questsandstuff.quest.editor.blueprint.CanvasBlueprint;
import com.abo47.questsandstuff.quest.editor.blueprint.CanvasBlueprintCodec;

import static com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory.withAlpha;
import static com.abo47.questsandstuff.client.tablet.theme.tokens.UiThemeTokens.BUTTON_18;
import static com.abo47.questsandstuff.client.tablet.theme.tokens.UiThemeTokens.GRID_3;
import static com.abo47.questsandstuff.client.tablet.theme.tokens.UiThemeTokens.PAD_8;
import static com.abo47.questsandstuff.client.tablet.ui.factory.UiFactory.label;
import com.abo47.questsandstuff.client.tablet.modal.ModalPanel;

public final class BlueprintCodeModal {
    private static final int PANEL_W = 360;
    private static final int PANEL_H = 122;
    private static final int PAD = PAD_8;
    private static final int BTN = BUTTON_18;
    private static final int GAP = GRID_3;
    private static final int CODE_Y = 32;
    private static final int CODE_H = 44;

    private BlueprintCodeModal() {
    }

    public static void openExport(UiState state, String relativePath) {
        CanvasBlueprint blueprint = CanvasBlueprintStore.read(relativePath);
        String code = CanvasBlueprintCodec.encode(blueprint);
        state.modal.blueprintCodeOpen = true;
        state.modal.blueprintCodeImportMode = false;
        state.modal.blueprintCodeTarget = relativePath == null ? "" : relativePath;
        state.modal.blueprintCodeDraft = code;
        state.modal.blueprintCodeMessage = code.isBlank()
                ? ModalPanel.tr("ui.questsandstuff.blueprints.code_select_export")
                : "";
        state.modal.blueprintCodeAnimationStartMs = System.currentTimeMillis();
        state.pickers.assetContextOpen = false;
        QuestsAndStuffMod.debugLog("[QnS:UI:Blueprint] export code open path={} empty={}", state.modal.blueprintCodeTarget, code.isBlank());
    }

    public static void openImport(UiState state) {
        state.modal.blueprintCodeOpen = true;
        state.modal.blueprintCodeImportMode = true;
        state.modal.blueprintCodeTarget = "";
        state.modal.blueprintCodeDraft = "";
        state.modal.blueprintCodeMessage = "";
        state.modal.blueprintCodeAnimationStartMs = System.currentTimeMillis();
        state.pickers.assetContextOpen = false;
        QuestsAndStuffMod.debugLog("[QnS:UI:Blueprint] import code open");
    }

    public static void close(UiState state) {
        ModalState.clearBlueprintCodeState(state);
    }

    public static void add(WidgetGroup modal, UiState state, Runnable refresh, int modalW, int modalH) {
        int panelW = Math.min(PANEL_W, Math.max(220, modalW - 32));
        int panelH = PANEL_H;
        int x = Math.max(PAD, (modalW - panelW) / 2);
        int y = Math.max(24, (modalH - panelH) / 2);
        WidgetGroup panel = SurfaceFactory.panel(x, y, panelW, panelH, withAlpha(ThemeColors.elevatedSurface(), 245), ThemeColors.BORDER_ACCENT);
        panel.addWidget(label(PAD, 6, ModalPanel.tr(state.modal.blueprintCodeImportMode
                ? "ui.questsandstuff.blueprints.import_code"
                : "ui.questsandstuff.blueprints.export_code"), ThemeColors.TEXT_PRIMARY));

        addHeaderButtons(panel, state, refresh, panelW);
        addCodeField(panel, state, panelW);
        if (!state.modal.blueprintCodeMessage.isBlank()) {
            panel.addWidget(label(PAD, CODE_Y + CODE_H + 8, state.modal.blueprintCodeMessage, ThemeColors.WARNING));
        }
        modal.addWidget(QuestsAndStuffConfig.popupWindowAnimationsEnabled()
                ? SourceOriginRevealWidget.windowNoShadow(panel, () -> state.modal.blueprintCodeAnimationStartMs, () -> true, () -> null)
                : panel);
    }

    private static void addHeaderButtons(WidgetGroup panel, UiState state, Runnable refresh, int panelW) {
        int closeX = panelW - PAD - BTN;
        panel.addWidget(ChromeFactory.closeIconButton(closeX, 4, BTN, BTN, click -> {
            close(state);
            refresh.run();
        }));

        int x = closeX - GAP - BTN;
        if (state.modal.blueprintCodeImportMode) {
            panel.addWidget(ChromeFactory.iconButton(x, 4, BTN, BTN, "manual_check", () -> UiThemeManager.colorForRole(UiThemeManager.ROLE_ICON_SUCCESS), click -> {
                applyImport(state);
                refresh.run();
            }));
            x -= GAP + BTN;
            panel.addWidget(ChromeFactory.iconButton(x, 4, BTN, BTN, "paste", () -> UiThemeManager.colorForRole(UiThemeManager.ROLE_ICON_DEFAULT), click -> {
                state.modal.blueprintCodeDraft = Minecraft.getInstance().keyboardHandler.getClipboard();
                state.modal.blueprintCodeMessage = "";
                refresh.run();
            }));
            return;
        }
        panel.addWidget(ChromeFactory.iconButton(x, 4, BTN, BTN, "copy", () -> UiThemeManager.colorForRole(UiThemeManager.ROLE_ICON_DEFAULT), click -> {
            Minecraft.getInstance().keyboardHandler.setClipboard(state.modal.blueprintCodeDraft == null ? "" : state.modal.blueprintCodeDraft);
            state.modal.blueprintCodeMessage = ModalPanel.tr("ui.questsandstuff.blueprints.code_copied");
            refresh.run();
        }));
    }

    private static void addCodeField(WidgetGroup panel, UiState state, int panelW) {
        CodeEditorWidget editor = new CodeEditorWidget(PAD, CODE_Y, panelW - PAD * 2, CODE_H);
        editor.codeEditor.setLanguageDefinitionUnformatted();
        editor.setBackground(SurfaceFactory.bordered(ThemeColors.recessedSurface(), ThemeColors.BORDER_BASE));
        editor.setLines(editorLines(state.modal.blueprintCodeDraft));
        editor.setOnTextChanged(lines -> state.modal.blueprintCodeDraft = rawCode(lines));
        editor.setFocus(true);
        panel.addWidget(editor);
    }

    private static void applyImport(UiState state) {
        CanvasBlueprint blueprint = CanvasBlueprintCodec.decode(state.modal.blueprintCodeDraft);
        if (blueprint.isEmpty()) {
            state.modal.blueprintCodeMessage = ModalPanel.tr("ui.questsandstuff.blueprints.code_invalid");
            return;
        }
        String saved = CanvasBlueprintStore.save(blueprint, blueprint.name());
        if (saved.isBlank()) {
            state.modal.blueprintCodeMessage = ModalPanel.tr("ui.questsandstuff.blueprints.code_save_failed");
            return;
        }
        state.pickers.assetBrowseDir = CanvasBlueprintStore.BLUEPRINTS_DIR;
        state.pickers.assetSelected = saved;
        state.pickers.assetContextFile = saved;
        state.pickers.assetGridScroll = 0;
        close(state);
        QuestsAndStuffMod.debugLog("[QnS:UI:Blueprint] imported code saved path={} entries={}", saved, blueprint.contentCount());
    }

    public static List<String> editorLines(String value) {
        String raw = value == null ? "" : value;
        if (raw.isEmpty()) {
            return List.of("");
        }
        return List.of(raw.split("\\R", -1));
    }

    public static String rawCode(List<String> lines) {
        if (lines == null || lines.isEmpty()) {
            return "";
        }
        return String.join("\n", lines);
    }
}
