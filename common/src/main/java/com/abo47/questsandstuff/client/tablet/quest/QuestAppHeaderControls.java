package com.abo47.questsandstuff.client.tablet.quest;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuController;
import com.abo47.questsandstuff.client.tablet.controls.SearchFilter;
import com.abo47.questsandstuff.client.tablet.controls.StyledTextFields;
import com.abo47.questsandstuff.client.tablet.controls.IconTextButton;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.CanvasRenderer;
import com.abo47.questsandstuff.client.tablet.quest.canvas.blueprint.CanvasBlueprintController;
import com.abo47.questsandstuff.client.tablet.quest.tools.ToolMenuAnimation;
import com.abo47.questsandstuff.client.tablet.state.UiState;
import com.abo47.questsandstuff.client.tablet.text.QuestTranslationKeys;
import com.abo47.questsandstuff.client.tablet.text.TranslationKeys;
import com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory;
import com.abo47.questsandstuff.client.tablet.theme.tokens.ThemeColors;

import static com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory.withAlpha;
import static com.abo47.questsandstuff.client.tablet.ui.factory.UiFactory.persistUiState;

final class QuestAppHeaderControls {
    private final TextFieldWidget chapterSearchField;
    private final TextFieldWidget searchField;
    private final IconTextButton toolsButton;
    private final IconTextButton blueprintButton;
    private final IconTextButton claimAllButton;
    private final IconTextButton editorButton;
    private int toolsX;

    private QuestAppHeaderControls(
            TextFieldWidget chapterSearchField,
            TextFieldWidget searchField,
            IconTextButton toolsButton,
            IconTextButton blueprintButton,
            IconTextButton claimAllButton,
            IconTextButton editorButton
    ) {
        this.chapterSearchField = chapterSearchField;
        this.searchField = searchField;
        this.toolsButton = toolsButton;
        this.blueprintButton = blueprintButton;
        this.claimAllButton = claimAllButton;
        this.editorButton = editorButton;
    }

    static QuestAppHeaderControls create(Player player, UiState state, Runnable refresh, int chapterInset, int chapterTopY, int chapterHeaderH, int initialChapterW, int canvasHeaderX, int topY, int headerH) {
        TextFieldWidget chapterSearchField = StyledTextFields.search(
                chapterInset,
                chapterTopY,
                Math.max(24, initialChapterW - chapterInset * 2),
                chapterHeaderH,
                () -> state.chapterPanel.chapterSearch,
                Integer.MAX_VALUE,
                value -> {
                    state.chapterPanel.chapterSearch = SearchFilter.normalizeUserInput(value);
                    state.chapterPanel.chapterScroll = 0;
                    refresh.run();
                },
                focused -> state.chapterPanel.chapterSearchFocused = focused
        );

        TextFieldWidget searchField = StyledTextFields.search(
                canvasHeaderX,
                topY,
                60,
                headerH,
                () -> state.root.search,
                Integer.MAX_VALUE,
                value -> {
                    state.root.search = SearchFilter.normalizeUserInput(value);
                    if (!state.root.search.isBlank()) {
                        CanvasRenderer.jumpToBestMatch(state);
                    }
                    refresh.run();
                },
                focused -> state.root.searchFocused = focused
        );

        int toolsW = headerH;
        IconTextButton toolsButton = headerButton(0, 0, toolsW, headerH, "tools", ThemeColors.INTERACTIVE, click -> {
            ToolMenuAnimation.toggleMain(state);
            refresh.run();
        });

        IconTextButton blueprintButton = headerButton(0, 0, toolsW, headerH, "scroll", ThemeColors.WARNING, click -> {
            ToolMenuAnimation.closeMain(state);
            ContextMenuController.close(state);
            state.chapterPanel.chapterMenuOpen = false;
            state.pickers.assetContextOpen = false;
            CanvasBlueprintController.openBlueprintLibrary(state);
            QuestsAndStuffMod.debugLog("[QnS:UI:Blueprint] library opened from main header");
            refresh.run();
        });
        IconTextButton claimAllButton = headerButton(0, 0, toolsW, headerH, "claim_all", ThemeColors.INTERACTIVE, click -> {
            ToolMenuAnimation.closeMain(state);
            ContextMenuController.close(state);
            state.chapterPanel.chapterMenuOpen = false;
            state.pickers.assetContextOpen = false;
            RewardClaimHandler.claimAll(player, "");
            refresh.run();
        });
        IconTextButton editorButton = headerButton(0, 0, toolsW, headerH, "editor", ThemeColors.INTERACTIVE, click -> {
            if (!state.root.editorAvailable) {
                return;
            }
            state.root.editMode = !state.root.editMode;
            state.root.canEdit = state.root.editorAvailable && state.root.editMode;
            ContextMenuController.close(state);
            state.chapterPanel.chapterMenuOpen = false;
            state.pickers.assetContextOpen = false;
            persistUiState(state);
            QuestsAndStuffMod.debugLog("[QnS:UI] editor mode toggle enabled={}", state.root.editMode);
            refresh.run();
        });

        return new QuestAppHeaderControls(chapterSearchField, searchField, toolsButton, blueprintButton, claimAllButton, editorButton);
    }

    TextFieldWidget chapterSearchField() {
        return chapterSearchField;
    }

    TextFieldWidget searchField() {
        return searchField;
    }

    IconTextButton toolsButton() {
        return toolsButton;
    }

    IconTextButton blueprintButton() {
        return blueprintButton;
    }

    IconTextButton claimAllButton() {
        return claimAllButton;
    }

    IconTextButton editorButton() {
        return editorButton;
    }

    int toolsX() {
        return toolsX;
    }

    void syncFocus(UiState state) {
        if (state.root.searchFocused) {
            searchField.setFocus(true);
        }
        if (state.chapterPanel.chapterSearchFocused) {
            chapterSearchField.setFocus(true);
        }
    }

    void refreshSurfaceFactory(UiState state) {
        chapterSearchField.setBackground(SurfaceFactory.bordered(ThemeColors.SURFACE_BASE, ThemeColors.BORDER_BASE));
        chapterSearchField.setTextColor(ThemeColors.TEXT_PRIMARY);
        searchField.setBackground(SurfaceFactory.bordered(ThemeColors.SURFACE_BASE, ThemeColors.BORDER_BASE));
        searchField.setTextColor(ThemeColors.TEXT_PRIMARY);
        toolsButton.visuals(headerVisuals(ThemeColors.SURFACE_PANEL_ALT, ThemeColors.BORDER_BASE, ThemeColors.INTERACTIVE));
        blueprintButton.visuals(headerVisuals(ThemeColors.SURFACE_PANEL_ALT, ThemeColors.BORDER_BASE, ThemeColors.INTERACTIVE));
        claimAllButton.visuals(headerVisuals(ThemeColors.SURFACE_PANEL_ALT, ThemeColors.BORDER_BASE, ThemeColors.INTERACTIVE));
        editorButton.visuals(editorVisuals(state.root.editMode));
    }

    void layoutChapter(boolean chapterCollapsed, int dynamicListX, int dynamicListW, int chapterTopY, int chapterHeaderH) {
        chapterSearchField.setVisible(!chapterCollapsed);
        chapterSearchField.setSelfPosition(dynamicListX, chapterTopY);
        chapterSearchField.setSize(dynamicListW, chapterHeaderH);
    }

    void layoutCanvas(UiState state, int headerX, int headerW, int topY, int headerH, int toolsW, int topGap) {
        toolsX = headerX + headerW - toolsW;
        boolean showEditorToggle = state.root.editorAvailable;
        boolean showBlueprintButton = state.root.canEdit && state.root.editMode;
        boolean showToolsButton = true;
        int editorX = showEditorToggle ? toolsX - topGap - toolsW : toolsX;
        int claimAllX = (showEditorToggle ? editorX : toolsX) - topGap - toolsW;
        int blueprintX = showBlueprintButton ? claimAllX - topGap - toolsW : claimAllX;
        int searchX = headerX;
        int searchEnd = (showBlueprintButton ? blueprintX : claimAllX) - topGap;
        int searchW = Math.max(60, searchEnd - searchX);
        searchField.setSelfPosition(searchX, topY);
        searchField.setSize(searchW, headerH);
        layoutHeaderButton(editorButton, editorX, topY, toolsW, headerH, showEditorToggle, new Component[]{
                Component.translatable("ui.questsandstuff.tools.editor_toggle"),
                Component.translatable(state.root.editMode
                        ? "ui.questsandstuff.tools.editor_state_on"
                        : "ui.questsandstuff.tools.editor_state_off")
        });

        layoutHeaderButton(claimAllButton, claimAllX, topY, toolsW, headerH, true, new Component[]{
                TranslationKeys.component(QuestTranslationKeys.CLAIM_ALL_REWARDS)
        });

        layoutHeaderButton(blueprintButton, blueprintX, topY, toolsW, headerH, showBlueprintButton, new Component[]{
                Component.translatable("ui.questsandstuff.blueprints.button"),
                Component.translatable("ui.questsandstuff.blueprints.button_tooltip")
        });

        layoutHeaderButton(toolsButton, toolsX, topY, toolsW, headerH, showToolsButton, new Component[]{
                Component.translatable("ui.questsandstuff.tools.menu")
        });
    }

    void addToCanvas(WidgetGroup canvasPanel) {
        canvasPanel.addWidget(searchField);
        canvasPanel.addWidget(blueprintButton);
        canvasPanel.addWidget(claimAllButton);
        canvasPanel.addWidget(editorButton);
        canvasPanel.addWidget(toolsButton);
    }

    private static IconTextButton headerButton(int x, int y, int width, int height, String icon, int accentColor, java.util.function.Consumer<com.lowdragmc.lowdraglib.gui.util.ClickData> callback) {
        return IconTextButton.icon(x, y, width, height, icon, headerVisuals(ThemeColors.SURFACE_PANEL_ALT, ThemeColors.BORDER_BASE, accentColor), callback);
    }

    private static IconTextButton.Visuals headerVisuals(int fill, int border, int accentColor) {
        return new IconTextButton.Visuals(
                IconTextButton.State.of(fill, border, accentColor),
                IconTextButton.State.of(withAlpha(accentColor, 66), ThemeColors.BORDER_ACCENT, accentColor),
                IconTextButton.State.of(withAlpha(accentColor, 90), accentColor, ThemeColors.TEXT_PRIMARY)
        );
    }

    private static IconTextButton.Visuals editorVisuals(boolean editMode) {
        int accent = editMode ? ThemeColors.SUCCESS : ThemeColors.ERROR;
        return new IconTextButton.Visuals(
                IconTextButton.State.of(ThemeColors.SURFACE_PANEL_ALT, accent, accent),
                IconTextButton.State.of(ThemeColors.hoverFill(accent), ThemeColors.BORDER_ACCENT, accent),
                IconTextButton.State.of(ThemeColors.pressedFill(accent), accent, ThemeColors.TEXT_PRIMARY),
                editMode ? ThemeColors.SUCCESS : -1
        );
    }

    private static void layoutHeaderButton(IconTextButton button, int x, int y, int width, int height, boolean visible, Component[] tooltips) {
        button.setSelfPosition(x, y);
        button.setSize(width, height);
        button.tooltips(tooltips);
        button.setVisible(visible);
        button.setActive(visible);
    }

}
