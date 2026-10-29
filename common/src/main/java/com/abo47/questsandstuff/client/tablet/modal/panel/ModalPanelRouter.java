package com.abo47.questsandstuff.client.tablet.modal.panel;

import java.util.List;
import javax.annotation.Nonnull;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;

import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import com.abo47.questsandstuff.QuestsAndStuffConfig;
import com.abo47.questsandstuff.client.tablet.animation.SourceOriginRevealWidget;
import com.abo47.questsandstuff.client.tablet.modal.ModalStateQueries;
import com.abo47.questsandstuff.client.tablet.modal.ModalShell;
import com.abo47.questsandstuff.client.tablet.modal.ModalWindowManager;
import com.abo47.questsandstuff.client.tablet.modal.pickers.AdvancementPickerModal;
import com.abo47.questsandstuff.client.tablet.modal.asset.AssetPickerModal;
import com.abo47.questsandstuff.client.tablet.modal.pickers.BiomePickerModal;
import com.abo47.questsandstuff.client.tablet.modal.pickers.BlockPickerModal;
import com.abo47.questsandstuff.client.tablet.modal.pickers.ColorPickerModal;
import com.abo47.questsandstuff.client.tablet.modal.pickers.DimensionPickerModal;
import com.abo47.questsandstuff.client.tablet.modal.entity.EntityVariantModal;
import com.abo47.questsandstuff.client.tablet.modal.pickers.IconPickerModal;
import com.abo47.questsandstuff.client.tablet.modal.pickers.ItemInventoryPickerModal;
import com.abo47.questsandstuff.client.tablet.modal.pickers.ItemLockPickerModal;
import com.abo47.questsandstuff.client.tablet.modal.pickers.LootTablePickerModal;
import com.abo47.questsandstuff.client.tablet.modal.recipe.RecipePickerModal;
import com.abo47.questsandstuff.client.tablet.modal.sound.SoundPickerModal;
import com.abo47.questsandstuff.client.tablet.modal.pickers.StatPickerModal;
import com.abo47.questsandstuff.client.tablet.modal.pickers.StructurePickerModal;
import com.abo47.questsandstuff.client.tablet.modal.pickers.ThemePickerModal;
import com.abo47.questsandstuff.client.tablet.quest.prerequisite.QuestPrerequisitesModal;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory;
import com.abo47.questsandstuff.client.tablet.theme.tokens.TabletColors;

import static com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory.withAlpha;

public final class ModalPanelRouter {
    private static final List<ModalPanelDescriptor> MODAL_DESCRIPTORS = List.of(
            new ModalPanelDescriptor(ModalWindowManager.ModalType.ICON_PICKER, IconPickerModal::rebuild, state -> state.pickers.iconSearchFocused),
            new ModalPanelDescriptor(ModalWindowManager.ModalType.ASSET_PICKER, AssetPickerModal::rebuild, state -> state.pickers.assetSearchFocused),
            new ModalPanelDescriptor(ModalWindowManager.ModalType.BIOME_PICKER, BiomePickerModal::rebuild, state -> state.pickers.biomeSearchFocused),
            new ModalPanelDescriptor(ModalWindowManager.ModalType.ADVANCEMENT_PICKER, AdvancementPickerModal::rebuild, state -> state.pickers.advancementSearchFocused),
            new ModalPanelDescriptor(ModalWindowManager.ModalType.RECIPE_PICKER, RecipePickerModal::rebuild, state -> state.pickers.recipeSearchFocused),
            new ModalPanelDescriptor(ModalWindowManager.ModalType.STRUCTURE_PICKER, StructurePickerModal::rebuild, state -> state.pickers.structureSearchFocused),
            new ModalPanelDescriptor(ModalWindowManager.ModalType.BLOCK_PICKER, BlockPickerModal::rebuild, state -> state.pickers.blockSearchFocused),
            new ModalPanelDescriptor(ModalWindowManager.ModalType.STAT_PICKER, StatPickerModal::rebuild, state -> state.pickers.statSearchFocused),
            new ModalPanelDescriptor(ModalWindowManager.ModalType.DIMENSION_PICKER, DimensionPickerModal::rebuild, state -> state.pickers.dimensionSearchFocused),
            new ModalPanelDescriptor(ModalWindowManager.ModalType.LOOT_TABLE_PICKER, LootTablePickerModal::rebuild, state -> state.pickers.lootTableSearchFocused),
            new ModalPanelDescriptor(ModalWindowManager.ModalType.ITEM_INVENTORY_PICKER, ItemInventoryPickerModal::rebuild, state -> state.pickers.itemInventorySearchFocused),
            new ModalPanelDescriptor(ModalWindowManager.ModalType.SOUND_PICKER, SoundPickerModal::rebuild, state -> state.pickers.soundSearchFocused),
            new ModalPanelDescriptor(ModalWindowManager.ModalType.COLOR_PICKER, (modal, state, player, refresh, w, h) -> {
                ColorPickerModal.rebuild(modal, state, player, refresh, w, h);
                return null;
            }, state -> false),
            new ModalPanelDescriptor(ModalWindowManager.ModalType.THEME_PICKER, (modal, state, player, refresh, w, h) -> {
                ThemePickerModal.rebuild(modal, state, refresh, w, h);
                return null;
            }, state -> false),
            new ModalPanelDescriptor(ModalWindowManager.ModalType.ENTITY_VARIANT_PICKER, EntityVariantModal::rebuild, state -> state.pickers.entityVariantSearchFocused),
            new ModalPanelDescriptor(ModalWindowManager.ModalType.PREREQUISITES_MANAGER, QuestPrerequisitesModal::rebuild, state -> state.modal.prerequisitesManagerSearchFocused),
            new ModalPanelDescriptor(ModalWindowManager.ModalType.ITEM_LOCK_PICKER, ItemLockPickerModal::rebuild, state -> state.pickers.itemLockSearchFocused)
    );

    private ModalPanelRouter() {
    }

    public static void rebuildChapterModal(WidgetGroup overlay, TabletUiState state, Player player, Runnable refresh) {
        overlay.clearAllWidgets();
        if (!ModalStateQueries.anyOpen(state) && !state.modal.modalWindowClosing) {
            return;
        }
        ModalPanelDescriptor descriptor = descriptor(ModalStateQueries.activeType(state));
        if (descriptor == null) {
            return;
        }
        int w = Math.min(ModalShell.MODAL_MAX_W, overlay.getSize().width - ModalShell.MODAL_MARGIN);
        int h = Math.min(ModalShell.MODAL_MAX_H, overlay.getSize().height - ModalShell.MODAL_MARGIN);
        int mx = (overlay.getSize().width - w) / 2;
        int my = (overlay.getSize().height - h) / 2;
        WidgetGroup dim = new WidgetGroup(0, 0, overlay.getSize().width, overlay.getSize().height) {
            @Override
            public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
                int alpha = dimAlpha(state);
                if (alpha <= 0) {
                    return;
                }
                SurfaceFactory.fill(withAlpha(TabletColors.SURFACE_BASE, alpha)).draw(graphics, 0, 0, getPositionX(), getPositionY(), getSizeWidth(), getSizeHeight());
            }
        };
        overlay.addWidget(dim);
        WidgetGroup modal = new WidgetGroup(mx, my, w, h);
        modal.setBackground(SurfaceFactory.bordered(withAlpha(TabletColors.SURFACE_BASE, 252), TabletColors.BORDER_ACCENT));
        TextFieldWidget searchField = descriptor.rebuild(modal, state, player, refresh, w, h);
        modal.setActive(!state.modal.modalWindowClosing);
        if (QuestsAndStuffConfig.popupWindowAnimationsEnabled()) {
            overlay.addWidget(SourceOriginRevealWidget.windowNoShadow(
                    modal,
                    () -> state.modal.modalWindowAnimationStartMs,
                    () -> !state.modal.modalWindowClosing,
                    () -> sourceRect(state)
            ));
        } else {
            overlay.addWidget(modal);
        }
        if (!state.modal.modalWindowClosing) {
            restoreSearchFocus(state, descriptor, searchField);
        }
    }

    private static ModalPanelDescriptor descriptor(ModalWindowManager.ModalType type) {
        for (ModalPanelDescriptor descriptor : MODAL_DESCRIPTORS) {
            if (descriptor.type() == type) {
                return descriptor;
            }
        }
        return null;
    }

    private static int dimAlpha(TabletUiState state) {
        if (!QuestsAndStuffConfig.popupWindowAnimationsEnabled()) {
            return 140;
        }
        float amount = SourceOriginRevealWidget.windowOpenAmount(state.modal.modalWindowAnimationStartMs, !state.modal.modalWindowClosing);
        return Math.round(140 * amount);
    }

    private static SourceOriginRevealWidget.SourceRect sourceRect(TabletUiState state) {
        if (!state.modal.modalWindowAnimationHasSource) {
            return null;
        }
        return new SourceOriginRevealWidget.SourceRect(
                state.modal.modalWindowAnimationSourceX,
                state.modal.modalWindowAnimationSourceY,
                state.modal.modalWindowAnimationSourceW,
                state.modal.modalWindowAnimationSourceH
        );
    }

    private static void restoreSearchFocus(TabletUiState state, ModalPanelDescriptor descriptor, TextFieldWidget searchField) {
        if (searchField != null && descriptor.searchFocused(state)) {
            searchField.setFocus(true);
        }
    }

    private record ModalPanelDescriptor(
            ModalWindowManager.ModalType type,
            ModalPanelBuilder builder,
            ModalSearchFocus searchFocus
    ) {
        TextFieldWidget rebuild(WidgetGroup modal, TabletUiState state, Player player, Runnable refresh, int w, int h) {
            return builder.rebuild(modal, state, player, refresh, w, h);
        }

        boolean searchFocused(TabletUiState state) {
            return searchFocus.focused(state);
        }
    }

    private interface ModalPanelBuilder {
        TextFieldWidget rebuild(WidgetGroup modal, TabletUiState state, Player player, Runnable refresh, int w, int h);
    }

    private interface ModalSearchFocus {
        boolean focused(TabletUiState state);
    }
}
