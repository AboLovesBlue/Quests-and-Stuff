package com.abo47.questsandstuff.client.tablet.modal.asset;

import java.nio.file.Path;
import java.util.List;

import org.lwjgl.glfw.GLFW;

import net.minecraft.Util;
import net.minecraft.world.entity.player.Player;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import com.abo47.questsandstuff.client.sync.state.ClientQuestStateFacade;
import com.abo47.questsandstuff.client.tablet.assets.AssetLibrary;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuAnimationBridge;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuController;
import com.abo47.questsandstuff.client.tablet.controls.DragScrollBarWidget;
import com.abo47.questsandstuff.client.tablet.controls.ScrollState;
import com.abo47.questsandstuff.client.tablet.controls.SearchFilter;
import com.abo47.questsandstuff.client.tablet.controls.picker.TiledPickerPanel;
import com.abo47.questsandstuff.client.tablet.icons.IconAtlas;
import com.abo47.questsandstuff.client.tablet.quest.canvas.blueprint.CanvasBlueprintMiniRenderer;
import com.abo47.questsandstuff.client.tablet.quest.canvas.blueprint.CanvasBlueprintStore;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.codec.UiThemeManager;
import com.abo47.questsandstuff.client.tablet.theme.render.ChromeFactory;
import com.abo47.questsandstuff.client.tablet.theme.render.GlowShaderHelper;
import com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory;
import com.abo47.questsandstuff.client.tablet.theme.tokens.TabletColors;
import com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory;
import com.abo47.questsandstuff.quest.editor.blueprint.CanvasBlueprint;
import com.abo47.questsandstuff.client.tablet.modal.sound.SoundPreviewPlayerWidget;
import com.abo47.questsandstuff.client.tablet.modal.pickers.PickerTileText;
import com.abo47.questsandstuff.client.tablet.modal.sound.SoundVolumeControls;
import com.abo47.questsandstuff.client.tablet.modal.ModalStateQueries;
import com.abo47.questsandstuff.client.tablet.modal.ModalPreviewLayout;
import com.abo47.questsandstuff.client.tablet.modal.ModalWindowManager;
import com.abo47.questsandstuff.client.tablet.modal.ModalCloseActions;
import com.abo47.questsandstuff.client.tablet.modal.ModalShell;
import com.abo47.questsandstuff.client.tablet.modal.ModalSession;
import com.abo47.questsandstuff.client.tablet.modal.TabletModalPanel;
import com.abo47.questsandstuff.client.tablet.modal.target.ModalTargets;
import com.abo47.questsandstuff.client.tablet.modal.target.ModalTargetState;

import static com.abo47.questsandstuff.client.tablet.controls.SearchFilter.crop;
import static com.abo47.questsandstuff.client.tablet.modal.ModalCloseActions.closeAll;
import static com.abo47.questsandstuff.client.tablet.modal.ModalSession.TargetSetSlot.QUEST_COMPLETION_SOUND;
import static com.abo47.questsandstuff.client.tablet.modal.ModalSession.TargetSlot.BLUEPRINT;
import static com.abo47.questsandstuff.client.tablet.theme.tokens.UiThemeTokens.*;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.assetDimensions;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.assetThumbnailTexture;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.chapterBackgroundTexture;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.flatHitButton;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.label;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.searchAssetEntries;

public final class TabletAssetPickerModal {
    private static final int HEADER_BUTTON_SIZE = BUTTON_18;
    private static final int HEADER_GAP = GRID_3;
    private static final int HEADER_BUTTON_Y = GRID_1;
    private static final int HEADER_CLOSE_ANCHOR_RIGHT_PAD = 26;
    private static final int HEADER_CLOSE_RENDER_X_OFFSET = 1;
    private static final int ASSET_TILE_GAP = GRID_8;
    private static final int ASSET_TILE_PAD = GRID_8;
    private static final int ASSET_TILE_MIN = 44;
    private static final int ASSET_TILE_MAX = GRID_96;

    private TabletAssetPickerModal() {
    }

    public static TextFieldWidget rebuild(WidgetGroup modal, TabletUiState state, Player player, Runnable refresh, int w, int h) {
        boolean soundPicker = !ModalTargetState.target(state, ModalSession.TargetSlot.QUEST_COMPLETION_SOUND, state.modal.modalQuestCompletionSoundTarget).isBlank()
                || !ModalTargetState.targetSet(state, QUEST_COMPLETION_SOUND, state.modal.modalQuestCompletionSoundTargets).isEmpty();
        boolean connectionTexturePicker = !state.modal.modalConnectionTextureTarget.isBlank()
                || !state.modal.modalConnectionTextureChapterTargets.isEmpty();
        boolean blueprintPicker = isBlueprintPicker(state);
        boolean hudPicker = AssetPickerPreviewControls.isHudBackgroundPicker(state);
        boolean bottomPreviewControls = AssetPickerPreviewControls.isQuestBackgroundPicker(state) || hudPicker || connectionTexturePicker;
        boolean imagePicker = !state.modal.modalEcBackgroundTarget.isBlank()
                || !state.modal.modalCanvasBackgroundTarget.isBlank()
                || !state.modal.modalQuestCompletionHudBackgroundTarget.isBlank()
                || !state.modal.modalCanvasImageTarget.isBlank()
                || !state.questDetails.questDetailsAssetPickTarget.isBlank()
                || !state.modal.modalChapterTarget.isBlank()
                || AssetPickerPreviewControls.isQuestBackgroundPicker(state)
                || AssetPickerPreviewControls.isHudBackgroundPicker(state)
                || connectionTexturePicker;
        String title = blueprintPicker
                ? "ui.questsandstuff.modal.blueprints"
                : soundPicker ? "ui.questsandstuff.modal.custom_sounds" : "ui.questsandstuff.modal.assets_library";
        ModalShell.addTitleAndClose(modal, TabletModalPanel.tr(title), w, state, refresh);
        if (blueprintPicker) {
            addBlueprintHeaderActions(modal, state, refresh, w);
        }
        String currentMode = soundPicker ? "sound" : blueprintPicker ? "blueprint" : "image";
        if (state.pickers.assetPickerSessionFresh) {
            String saved = switch (currentMode) {
                case "sound" -> state.pickers.assetBrowseDirSound;
                case "blueprint" -> state.pickers.assetBrowseDirBlueprint;
                default -> state.pickers.assetBrowseDirImage;
            };
            if (!saved.isEmpty()) {
                state.pickers.assetBrowseDir = saved;
            }
            state.pickers.assetPickerSessionFresh = false;
        }
        state.pickers.assetPickerMode = currentMode;
        String dir = state.pickers.assetBrowseDir == null ? "" : state.pickers.assetBrowseDir;
        modal.addWidget(ChromeFactory.iconButton(headerChainButtonX(w, blueprintPicker ? 3 : 1), HEADER_BUTTON_Y, HEADER_BUTTON_SIZE, HEADER_BUTTON_SIZE, "folder", () -> UiThemeManager.colorForRole(UiThemeManager.ROLE_ICON_INTERACTIVE), click -> {
            AssetLibrary.ensureAssetsDirs(TabletUiFactory.ASSETS_ROOT_DIR);
            Path folder = AssetLibrary.resolveDirectory(TabletUiFactory.ASSETS_ROOT_DIR, dir);
            if (folder != null) {
                Util.getPlatform().openFile(folder.toFile());
            }
        }));
        List<AssetLibrary.AssetEntry> assets = searchAssetEntries(dir, SearchFilter.normalizeUserInput(state.pickers.assetSearch));
        if (soundPicker) {
            assets = filterByKind(assets, AssetLibrary.AssetKind.SOUND);
        } else if (blueprintPicker) {
            assets = filterByKind(assets, AssetLibrary.AssetKind.BLUEPRINT);
        } else if (imagePicker) {
            assets = filterByKind(assets, AssetLibrary.AssetKind.IMAGE, AssetLibrary.AssetKind.GIF);
        }

        ModalPreviewLayout.Metrics libraryLayout = ModalPreviewLayout.calculate(w, h);
        int leftW = libraryLayout.leftW();
        int rightX = libraryLayout.rightX();
        int rightW = libraryLayout.rightW();
        int previewH = libraryLayout.bodyH();
        WidgetGroup preview = ModalPreviewLayout.previewPanel(libraryLayout);
        String selected = state.pickers.assetSelected == null ? "" : state.pickers.assetSelected;
        preview.addWidget(label(8, 8, crop(dir.isBlank() ? "/" : "/" + dir, 22), TabletColors.TEXT_SECONDARY));
        preview.addWidget(label(8, 20, selected.isBlank()
                ? TabletModalPanel.tr(blueprintPicker ? "ui.questsandstuff.blueprints.none_selected" : soundPicker ? "ui.questsandstuff.sound.none_selected" : "ui.questsandstuff.asset.none_selected")
                : crop(selected, 22), TabletColors.TEXT_SECONDARY));
        AssetLibrary.AssetKind selectedKind = selected.isBlank() ? AssetLibrary.AssetKind.UNKNOWN : AssetLibrary.assetKind(selected);
        AssetLibrary.AssetDimensions dims = selectedKind.hasImageThumbnail() ? assetDimensions(selected) : null;
        if (soundPicker || selectedKind == AssetLibrary.AssetKind.SOUND) {
            String previewSound = selectedKind == AssetLibrary.AssetKind.SOUND ? selected : "";
            if (!previewSound.isBlank()) {
                int volumeY = Math.max(46, previewH - 24);
                int playY = 34;
                int playH = Math.max(34, volumeY - playY - 8);
                preview.addWidget(new SoundPreviewPlayerWidget(8, playY, leftW - 16, playH, previewSound, () -> state.pickers.soundVolumeDraft));
                SoundVolumeControls.add(preview, state, player, refresh, 8, volumeY, leftW - 16, previewSound);
            }
        } else if (blueprintPicker || selectedKind == AssetLibrary.AssetKind.BLUEPRINT) {
            CanvasBlueprint blueprint = CanvasBlueprintStore.read(selected);
            preview.addWidget(label(8, 32, blueprint.isEmpty()
                    ? TabletModalPanel.tr("ui.questsandstuff.common.none_short")
                    : TabletModalPanel.tr("ui.questsandstuff.blueprints.item_count", blueprint.contentCount()), TabletColors.TEXT_MUTED));
            if (!blueprint.isEmpty()) {
                int previewW = leftW - 16;
                int previewAreaH = Math.max(24, h - 100);
                preview.addWidget(CanvasBlueprintMiniRenderer.previewWidget(8, 48, previewW, previewAreaH, blueprint));
            }
        } else {
            preview.addWidget(label(8, 32, dims == null ? TabletModalPanel.tr("ui.questsandstuff.common.none_short") : dims.width() + "x" + dims.height(), TabletColors.TEXT_MUTED));
            AssetPickerPreviewControls.addQuestBackgroundOptions(preview, state, refresh, leftW, previewH);
            AssetPickerPreviewControls.addHudBackgroundOptions(preview, state, refresh, leftW, previewH);
            AssetPickerPreviewControls.addConnectionTextureControls(preview, state, player, refresh, leftW, previewH);
        }
        if (!selected.isBlank() && dims != null) {
            boolean grayscale = AssetPickerPreviewControls.isQuestBackgroundPicker(state) && state.modal.modalQuestBackgroundGrayscale;
            IGuiTexture texture = chapterBackgroundTexture(selected, grayscale);
            if (texture != null) {
                int areaW = leftW - 16;
                int areaH = Math.max(1, h - (bottomPreviewControls ? 132 : 100));
                float scale = Math.min(1f, Math.min((float) areaW / Math.max(1, dims.width()), (float) areaH / Math.max(1, dims.height())));
                int drawW = Math.max(1, Math.round(dims.width() * scale));
                int drawH = Math.max(1, Math.round(dims.height() * scale));
                preview.addWidget(new ImageWidget(8 + Math.max(0, (areaW - drawW) / 2), 48, drawW, drawH, texture));
            }
        }
        modal.addWidget(preview);

        int controlsY = 2;
        int controlsH = 16;
        int backY = HEADER_BUTTON_Y;
        int backSize = HEADER_BUTTON_SIZE;
        int firstHeaderButtonX = headerChainButtonX(w, blueprintPicker ? 3 : 1);
        int backX = firstHeaderButtonX - HEADER_GAP - backSize;
        boolean canGoBack = !dir.isBlank();
        int searchW = canGoBack
                ? Math.max(40, backX - rightX - HEADER_GAP)
                : Math.max(40, firstHeaderButtonX - rightX - HEADER_GAP);
        TextFieldWidget search = ModalShell.addSearchField(modal, rightX, controlsY, searchW, controlsH, state.pickers.assetSearch, ModalShell.SEARCH_MAX_LENGTH, value -> {
            state.pickers.assetSearch = SearchFilter.normalizeUserInput(value);
            state.pickers.assetGridScroll = 0;
            refresh.run();
        }, focused -> state.pickers.assetSearchFocused = focused);

        if (canGoBack) {
            modal.addWidget(ChromeFactory.iconButton(backX, backY, backSize, backSize, "back", () -> UiThemeManager.colorForRole(UiThemeManager.ROLE_ICON_DEFAULT), click -> {
                state.pickers.assetBrowseDir = dir.contains("/") ? dir.substring(0, dir.lastIndexOf('/')) : "";
                state.pickers.saveBrowseDirForMode();
                state.pickers.assetGridScroll = 0;
                state.pickers.assetContextOpen = false;
                refresh.run();
            }));
        }

        int listY = libraryLayout.bodyY();
        int listH = libraryLayout.bodyH();
        int assetGridW = Math.max(1, rightW - ASSET_TILE_PAD * 2 - DragScrollBarWidget.RESERVED_WIDTH - ASSET_TILE_GAP);
        int assetCols = Math.max(3, (assetGridW + ASSET_TILE_GAP) / (ASSET_TILE_MIN + ASSET_TILE_GAP));
        int assetTile = Math.max(ASSET_TILE_MIN, (assetGridW - (assetCols - 1) * ASSET_TILE_GAP) / assetCols);
        assetTile = Math.min(ASSET_TILE_MAX, assetTile);
        TiledPickerPanel.add(
                modal,
                rightX,
                listY,
                rightW,
                listH,
                assetTile,
                assetTile,
                ASSET_TILE_GAP,
                ASSET_TILE_PAD,
                ASSET_TILE_PAD,
                assets,
                "No assets",
                ScrollState.bind(
                        () -> state.pickers.assetGridScroll,
                        value -> state.pickers.assetGridScroll = value,
                        () -> state.pickers.assetGridScrollDragging,
                        dragging -> state.pickers.assetGridScrollDragging = dragging
                ),
                () -> {
                    state.pickers.assetContextOpen = false;
                    state.pickers.assetRenameOpen = false;
                },
                (surface, entry, index, x, y, cellW, cellH, layout) -> {
            String relative = entry.relativePath();
            boolean renaming = state.pickers.assetRenameOpen && relative.equals(state.pickers.assetContextFile) && !entry.directory();
            WidgetGroup tile = new WidgetGroup(x, y, cellW, cellH);
            if (relative.equals(selected)) {
                tile.setBackground(SurfaceFactory.fill(withAlpha(TabletColors.INTERACTIVE, 54)));
            }
            int labelH = 14;
            int iconAreaH = Math.max(24, cellH - labelH - 8);
            int iconSize = Math.max(24, Math.min(96, Math.min(cellW - 24, iconAreaH - 12)));
            int iconX = Math.max(0, (cellW - iconSize) / 2);
            int iconY = Math.max(4, (iconAreaH - iconSize) / 2);
            if (entry.kind() == AssetLibrary.AssetKind.DIRECTORY) {
                var folderIcon = IconAtlas.iconTexture("folder");
                if (folderIcon != null) {
                    tile.addWidget(new ImageWidget(iconX, iconY, iconSize, iconSize, folderIcon));
                } else {
                    tile.addWidget(PickerTileText.centeredLabel(0, iconY + iconSize / 2 - 4, cellW, "[dir]", TabletColors.TEXT_MUTED));
                }
            } else if (entry.kind() == AssetLibrary.AssetKind.BLUEPRINT) {
                CanvasBlueprint blueprint = CanvasBlueprintStore.read(relative);
                if (!blueprint.isEmpty()) {
                    int thumbW = Math.max(12, cellW - 14);
                    int thumbH = Math.max(16, iconAreaH - 4);
                    tile.addWidget(CanvasBlueprintMiniRenderer.previewWidget((cellW - thumbW) / 2, 4, thumbW, thumbH, blueprint));
                } else {
                    var blueprintIcon = IconAtlas.iconTexture("scroll");
                    if (blueprintIcon != null) {
                        tile.addWidget(new ImageWidget(iconX, iconY, iconSize, iconSize, blueprintIcon));
                    }
                }
            } else if (entry.kind().hasImageThumbnail()) {
                IGuiTexture thumb = assetThumbnailTexture(relative);
                if (thumb != null) {
                    int thumbW = Math.max(12, cellW - 14);
                    int thumbH = Math.max(16, iconAreaH - 4);
                    tile.addWidget(new ImageWidget((cellW - thumbW) / 2, 4, thumbW, thumbH, thumb));
                }
            } else if (entry.kind() == AssetLibrary.AssetKind.SOUND) {
                var soundIcon = IconAtlas.iconTexture("audio-lines");
                if (soundIcon != null) {
                    tile.addWidget(new ImageWidget(iconX, iconY, iconSize, iconSize, soundIcon));
                }
            } else {
                var fileIcon = IconAtlas.iconTexture("file");
                if (fileIcon != null) {
                    tile.addWidget(new ImageWidget(iconX, iconY, iconSize, iconSize, fileIcon));
                }
            }
            if (renaming) {
                tile.addWidget(AssetPickerContextActions.assetInlineRenameField(state, refresh, relative, 2, cellH - labelH - 1, cellW - 4));
            } else {
                tile.addWidget(PickerTileText.centeredLabel(2, cellH - labelH, cellW - 4, entry.name(), TabletColors.TEXT_SECONDARY));
            }
            surface.addWidget(tile);
            if (renaming) {
                return;
            }
            ButtonWidget hit = flatHitButton(x, y, cellW, cellH, click -> {
                if (click.button == 1) {
                    state.pickers.assetContextOpen = true;
                    state.pickers.assetContextFile = relative;
                    AssetPickerContextActions.anchorAssetContextAtPointer(state, w, h);
                    state.pickers.assetRenameOpen = false;
                    ContextMenuController.clearDeleteConfirm(state);
                    ContextMenuAnimationBridge.start(state, ContextMenuAnimationBridge.DEFAULT_KEY);
                    refresh.run();
                    return;
                }
                boolean doubleClick = click.button == 0 && TabletModalPanel.acceptPickerDoubleClick(state, ModalTargets.doubleClickKey("asset", relative));
                if (entry.directory()) {
                    state.pickers.assetBrowseDir = relative;
                    state.pickers.saveBrowseDirForMode();
                    state.pickers.assetGridScroll = 0;
                    state.pickers.assetContextOpen = false;
                } else {
                    state.pickers.assetSelected = relative;
                    state.pickers.assetContextFile = relative;
                    if (doubleClick) {
                        TabletModalPanel.runAssetBackgroundAction(player, state, relative);
                        closeAll(state);
                    }
                }
                refresh.run();
            });
            GlowShaderHelper.glowHit(hit);
            surface.addWidget(hit);
                });

        if (state.pickers.assetContextOpen && !state.pickers.assetContextFile.isBlank()) {
            AssetPickerContextActions.addAssetContextDismissLayer(modal, state, refresh, w, h);
            AssetPickerContextActions.addContext(modal, state, player, refresh, w, h, rightW, assets);
        }
        if (state.modal.blueprintCodeOpen) {
            TabletBlueprintCodeModal.add(modal, state, refresh, w, h);
        }
        return search;
    }

    public static boolean handleKeyPressed(TabletUiState state, Runnable refresh, int keyCode) {
        if (!ModalStateQueries.isOpen(state, ModalWindowManager.ModalType.ASSET_PICKER) || keyCode != GLFW.GLFW_KEY_F3 || state.pickers.assetSelected == null || state.pickers.assetSelected.isBlank()) {
            return false;
        }
        AssetPickerContextActions.beginInlineRename(state, state.pickers.assetSelected);
        refresh.run();
        return true;
    }

    private static void addBlueprintHeaderActions(WidgetGroup modal, TabletUiState state, Runnable refresh, int w) {
        int importX = headerChainButtonX(w, 1);
        int exportX = headerChainButtonX(w, 2);
        modal.addWidget(ChromeFactory.iconButton(exportX, HEADER_BUTTON_Y, HEADER_BUTTON_SIZE, HEADER_BUTTON_SIZE, "file-up", () -> UiThemeManager.colorForRole(UiThemeManager.ROLE_ICON_INTERACTIVE), click -> {
            TabletBlueprintCodeModal.openExport(state, state.pickers.assetSelected);
            refresh.run();
        }));
        modal.addWidget(ChromeFactory.iconButton(importX, HEADER_BUTTON_Y, HEADER_BUTTON_SIZE, HEADER_BUTTON_SIZE, "file-down", () -> UiThemeManager.colorForRole(UiThemeManager.ROLE_ICON_SUCCESS), click -> {
            TabletBlueprintCodeModal.openImport(state);
            refresh.run();
        }));
    }

    private static int headerChainButtonX(int modalW, int slotLeftOfClose) {
        return headerCloseRenderX(modalW) - slotLeftOfClose * (HEADER_BUTTON_SIZE + HEADER_GAP);
    }

    private static int headerCloseRenderX(int modalW) {
        return modalW - HEADER_CLOSE_ANCHOR_RIGHT_PAD + HEADER_CLOSE_RENDER_X_OFFSET;
    }

    private static boolean isBlueprintPicker(TabletUiState state) {
        return !ModalTargetState.target(state, BLUEPRINT, state.modal.modalBlueprintTarget).isBlank();
    }

    private static List<AssetLibrary.AssetEntry> filterByKind(List<AssetLibrary.AssetEntry> entries, AssetLibrary.AssetKind first, AssetLibrary.AssetKind second) {
        return entries.stream()
                .filter(e -> e.directory() || e.kind() == first || e.kind() == second)
                .toList();
    }

    private static List<AssetLibrary.AssetEntry> filterByKind(List<AssetLibrary.AssetEntry> entries, AssetLibrary.AssetKind kind) {
        return entries.stream()
                .filter(e -> e.directory() || e.kind() == kind)
                .toList();
    }
}
