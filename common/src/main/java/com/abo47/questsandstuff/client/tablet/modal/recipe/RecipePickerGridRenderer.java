package com.abo47.questsandstuff.client.tablet.modal.recipe;

import java.util.List;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.SlotWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.controls.ScrollState;
import com.abo47.questsandstuff.client.tablet.controls.picker.TiledPickerPanel;
import com.abo47.questsandstuff.client.tablet.icons.DisplayIconProvider;
import com.abo47.questsandstuff.client.tablet.icons.DisplayIconWidget;
import com.abo47.questsandstuff.client.tablet.icons.ItemStackIconCodec;
import com.abo47.questsandstuff.client.tablet.icons.ScopedItemStackTexture;
import com.abo47.questsandstuff.client.tablet.state.UiState;
import com.abo47.questsandstuff.client.tablet.text.QuestTranslationKeys;
import com.abo47.questsandstuff.client.tablet.text.TranslationKeys;
import com.abo47.questsandstuff.client.tablet.theme.render.GlowShaderHelper;
import com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory;
import com.abo47.questsandstuff.client.tablet.theme.tokens.ThemeColors;

import static com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory.withAlpha;
import static com.abo47.questsandstuff.client.tablet.theme.tokens.UiThemeTokens.*;
import com.abo47.questsandstuff.client.tablet.modal.ModalPanel;
import com.abo47.questsandstuff.client.tablet.modal.pickers.ItemInventoryPickerModal;

final class RecipePickerGridRenderer {
    private static final int TILE = GRID_18;

    private RecipePickerGridRenderer() {
    }

    static void add(WidgetGroup modal, UiState state, Player player, Runnable refresh, int gridX, int gridY, int gridW, int gridH) {
        RecipePickerMode mode = RecipePickerModeController.mode(state);
        switch (RecipePickerModeController.contentKind(mode)) {
            case INVENTORY -> addInventoryGrid(modal, state, player, refresh, gridX, gridY, gridW, gridH);
            case FLUIDS -> addFluidGrid(modal, state, player, refresh, gridX, gridY, gridW, gridH);
            case RECIPES -> addRecipeGrid(modal, state, player, refresh, gridX, gridY, gridW, gridH, mode);
        }
    }

    private static void addInventoryGrid(WidgetGroup modal, UiState state, Player player, Runnable refresh, int gridX, int gridY, int gridW, int gridH) {
        List<ItemStack> entries = ItemInventoryPickerModal.inventoryEntries(player, state.pickers.recipeSearch);
        TiledPickerPanel.add(
                modal,
                gridX,
                gridY,
                gridW,
                gridH,
                TILE,
                TILE,
                0,
                6,
                6,
                entries,
                TranslationKeys.text(QuestTranslationKeys.NO_INVENTORY_ITEMS),
                scrollState(state),
                null,
                (surface, stack, index, x, y, tileW, tileH, layout) -> ItemInventoryPickerModal.renderStackTile(surface, stack, x, y, picked -> applyInventoryRecipePick(player, state, picked, refresh), hovered -> RecipePickerApplyActions.trackRecipeHover(state, ItemStackIconCodec.iconFromStack(hovered)))
        );
    }

    private static void addFluidGrid(WidgetGroup modal, UiState state, Player player, Runnable refresh, int gridX, int gridY, int gridW, int gridH) {
        List<String> entries = DisplayIconProvider.searchableFluidEntries(state.pickers.recipeSearch);
        TiledPickerPanel.add(
                modal,
                gridX,
                gridY,
                gridW,
                gridH,
                TILE,
                TILE,
                0,
                6,
                6,
                entries,
                TranslationKeys.text(QuestTranslationKeys.NO_FLUIDS),
                scrollState(state),
                null,
                (surface, entry, index, x, y, tileW, tileH, layout) -> renderFluidTile(surface, player, state, refresh, entry, x, y)
        );
    }

    private static void addRecipeGrid(WidgetGroup modal, UiState state, Player player, Runnable refresh, int gridX, int gridY, int gridW, int gridH, RecipePickerMode mode) {
        List<RecipePickerChoiceIndex.RecipeChoice> entries = RecipePickerChoiceIndex.recipes(state.pickers.recipeSearch, mode.showingTags(state.pickers.recipeSearch));
        TiledPickerPanel.add(
                modal,
                gridX,
                gridY,
                gridW,
                gridH,
                TILE,
                TILE,
                0,
                6,
                6,
                entries,
                TranslationKeys.text(QuestTranslationKeys.NO_RECIPES),
                scrollState(state),
                null,
                (surface, entry, index, x, y, tileW, tileH, layout) -> renderTile(surface, player, state, refresh, entry, x, y)
        );
    }

    private static ScrollState scrollState(UiState state) {
        return ScrollState.bind(
                () -> state.pickers.recipeScroll,
                value -> state.pickers.recipeScroll = value,
                () -> state.pickers.recipeScrollDragging,
                dragging -> state.pickers.recipeScrollDragging = dragging
        );
    }

    private static void renderTile(WidgetGroup surface, Player player, UiState state, Runnable refresh, RecipePickerChoiceIndex.RecipeChoice entry, int x, int y) {
        surface.addWidget(new ImageWidget(x, y, TILE, TILE, SlotWidget.ITEM_SLOT_TEXTURE));
        if (entry.previews().length == 0) {
            surface.addWidget(new DisplayIconWidget(x + GRID_1, y + GRID_1, GRID_16, GRID_16, entry.tag() ? "name_tag" : "recipe"));
        } else {
            surface.addWidget(new ImageWidget(x + GRID_1, y + GRID_1, GRID_16, GRID_16, new ScopedItemStackTexture(entry.previews())));
        }
        addHoverHit(surface, player, state, refresh, x, y, entry.value(), entry.tooltip(), entry.tag() ? "tag" : "output");
    }

    private static void renderFluidTile(WidgetGroup surface, Player player, UiState state, Runnable refresh, String entry, int x, int y) {
        surface.addWidget(new ImageWidget(x, y, TILE, TILE, SlotWidget.ITEM_SLOT_TEXTURE));
        surface.addWidget(new DisplayIconWidget(x + GRID_1, y + GRID_1, GRID_16, GRID_16, entry));
        addHoverHit(surface, player, state, refresh, x, y, entry, ModalPanel.iconTooltip(entry), "fluid");
    }

    private static void addHoverHit(WidgetGroup surface, Player player, UiState state, Runnable refresh, int x, int y, String value, Component[] tooltip, String logKind) {
        ButtonWidget hit = new ButtonWidget(x + GRID_1, y + GRID_1, GRID_16, GRID_16, SurfaceFactory.transparentFill(), click -> {
            if (value != null && !value.isBlank()) {
                RecipePickerApplyActions.applyRecipePick(player, state, value, refresh);
            }
            QuestsAndStuffMod.debugLog("[QnS:UI] recipe picked kind={} value={}", logKind, value);
        }) {
            @Override
            public void drawInBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
                super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
                if (isMouseOverElement(mouseX, mouseY)) {
                    RecipePickerApplyActions.trackRecipeHover(state, value);
                }
            }
        };
        GlowShaderHelper.glowHit(hit, tooltip);
        hit.setClickedTexture(SurfaceFactory.fill(withAlpha(ThemeColors.INTERACTIVE, 90)));
        hit.setClientSideWidget();
        surface.addWidget(hit);
    }

    private static void applyInventoryRecipePick(Player player, UiState state, ItemStack stack, Runnable refresh) {
        String pick = ItemStackIconCodec.iconFromStack(stack);
        if (!pick.isBlank()) {
            RecipePickerApplyActions.applyRecipePick(player, state, pick, refresh);
        }
        QuestsAndStuffMod.debugLog("[QnS:UI] recipe picked kind=inventory value={} hasNbt={}", pick, stack != null && stack.hasTag());
    }
}
