package com.abo47.questsandstuff.client.tablet.modal.asset;

import java.util.List;

import net.minecraft.world.entity.player.Player;

import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import com.abo47.questsandstuff.client.tablet.assets.AssetLibrary;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextAction;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextActionFactory;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuController;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuPanel;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuSection;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuSections;
import com.abo47.questsandstuff.client.tablet.controls.StyledTextFields;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.tokens.TabletColors;
import com.abo47.questsandstuff.client.tablet.ui.state.TabletStateQueries;
import com.abo47.questsandstuff.util.naming.QuestIdentity;
import com.abo47.questsandstuff.client.tablet.modal.ModalContextMenuPlacement;
import com.abo47.questsandstuff.client.tablet.modal.TabletModalPanel;

import static com.abo47.questsandstuff.client.tablet.modal.ModalCloseActions.closeAll;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.deleteAssetFile;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.flatHitButton;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.renameAssetFile;

final class AssetPickerContextActions {
    private AssetPickerContextActions() {
    }

    static void addAssetContextDismissLayer(WidgetGroup modal, TabletUiState state, Runnable refresh, int w, int h) {
        int rootW = TabletStateQueries.rootWidth(state);
        int rootH = TabletStateQueries.rootHeight(state);
        int modalX = ModalContextMenuPlacement.modalX(state, w);
        int modalY = ModalContextMenuPlacement.modalY(state, h);
        ButtonWidget dismiss = flatHitButton(-modalX, -modalY, rootW, rootH, click -> {
            state.pickers.assetContextOpen = false;
            state.pickers.assetRenameOpen = false;
            ContextMenuController.clearDeleteConfirm(state);
            refresh.run();
        });
        modal.addWidget(dismiss);
    }

    static void beginInlineRename(TabletUiState state, String relative) {
        state.pickers.assetContextOpen = false;
        state.pickers.assetContextFile = relative == null ? "" : relative;
        state.pickers.assetRenameOpen = !state.pickers.assetContextFile.isBlank();
        state.pickers.assetRenameDraft = TabletModalPanel.fileNameFromRelativePath(state.pickers.assetContextFile);
        state.pickers.assetSearchFocused = false;
        ContextMenuController.clearDeleteConfirm(state);
    }

    static TextFieldWidget assetInlineRenameField(TabletUiState state, Runnable refresh, String relative, int x, int y, int w) {
        final TextFieldWidget[] renameRef = new TextFieldWidget[1];
        TextFieldWidget rename = StyledTextFields.commitField(
                x,
                y,
                w,
                12,
                null,
                value -> state.pickers.assetRenameDraft = value == null ? "" : value.trim(),
                () -> {
                    commitAssetRename(state, renameRef[0]);
                    refresh.run();
                },
                () -> {
                    state.pickers.assetRenameOpen = false;
                    refresh.run();
                },
                () -> {
                    commitAssetRename(state, renameRef[0]);
                    refresh.run();
                }
        );
        renameRef[0] = rename;
        rename.setClientSideWidget();
        rename.setMaxStringLength(QuestIdentity.TITLE_MAX_LENGTH);
        rename.setCurrentString(state.pickers.assetRenameDraft.isBlank() ? TabletModalPanel.fileNameFromRelativePath(relative) : state.pickers.assetRenameDraft);
        StyledTextFields.applyStandardStyle(rename, TabletColors.SURFACE_BASE, TabletColors.BORDER_ACCENT);
        rename.setFocus(true);
        return rename;
    }

    static void anchorAssetContextAtPointer(TabletUiState state, int modalW, int modalH) {
        state.pickers.assetContextX = ModalContextMenuPlacement.localPointerX(state, modalW);
        state.pickers.assetContextY = ModalContextMenuPlacement.localPointerY(state, modalH);
    }

    static void addContext(WidgetGroup modal, TabletUiState state, Player player, Runnable refresh, int modalW, int modalH, int rightW, List<AssetLibrary.AssetEntry> assets) {
        AssetLibrary.AssetEntry contextEntry = assets.stream()
                .filter(asset -> asset.relativePath().equals(state.pickers.assetContextFile))
                .findFirst()
                .orElse(null);
        boolean isDir = contextEntry != null && contextEntry.directory();
        List<ContextAction> actions = assetContextActions(state, player, contextEntry, isDir);
        int ctxW = Math.min(150, Math.max(96, rightW - 8));
        int rowCount = ContextMenuPanel.rowActionCount(actions);
        int visibleRows = ContextMenuPanel.safeVisibleRows(rowCount, rowCount);
        int menuH = ContextMenuPanel.heightFor(actions, visibleRows);
        ModalContextMenuPlacement.Placement placement = ModalContextMenuPlacement.fitToRootFromModal(state, state.pickers.assetContextX, state.pickers.assetContextY, ctxW, menuH, modalW, modalH);
        int ctxX = placement.x();
        int ctxY = placement.y();
        state.pickers.assetContextMenuX = ctxX;
        state.pickers.assetContextMenuY = ctxY;
        state.pickers.assetContextMenuW = ctxW;
        state.pickers.assetContextMenuH = menuH;
        modal.addWidget(ContextMenuPanel.build(ctxX, ctxY, ctxW, actions, 0, visibleRows, TabletColors.BORDER_ACCENT, state, action -> {
            if (action.closeAfterClick()) {
                state.pickers.assetContextOpen = false;
                if (!state.pickers.assetRenameOpen) {
                    state.pickers.assetRenameDraft = "";
                }
                ContextMenuController.clearDeleteConfirm(state);
            }
            refresh.run();
        }, modalW, modalH));
    }

    static List<ContextAction> assetContextActions(TabletUiState state, Player player, AssetLibrary.AssetEntry contextEntry, boolean isDir) {
        ContextMenuSections sections = new ContextMenuSections();
        sections.add(ContextMenuSection.PRIMARY, ContextActionFactory.action(TabletModalPanel.tr("ui.questsandstuff.common.use"), isDir ? "open" : "background", TabletColors.INTERACTIVE, () -> {
            if (isDir) {
                state.pickers.assetBrowseDir = state.pickers.assetContextFile;
                state.pickers.saveBrowseDirForMode();
                state.pickers.assetGridScroll = 0;
            } else {
                state.pickers.assetSelected = state.pickers.assetContextFile;
                TabletModalPanel.runAssetBackgroundAction(player, state, state.pickers.assetContextFile);
                closeAll(state);
            }
        }));
        if (contextEntry != null && contextEntry.kind() == AssetLibrary.AssetKind.BLUEPRINT) {
            sections.add(ContextMenuSection.PRIMARY, ContextActionFactory.action(TabletModalPanel.tr("ui.questsandstuff.blueprints.export"), "file-up", TabletColors.INTERACTIVE, () ->
                    TabletBlueprintCodeModal.openExport(state, state.pickers.assetContextFile)));
        }
        if (!isDir) {
            sections.add(ContextMenuSection.ARRANGE, ContextActionFactory.rename(TabletModalPanel.tr("ui.questsandstuff.menu.rename"), () -> beginInlineRename(state, state.pickers.assetContextFile)));
        }
        if (!isDir) {
            String deleteKey = "asset:delete:" + state.pickers.assetContextFile;
            sections.add(ContextMenuSection.DANGER, ContextActionFactory.warningDelete(state, deleteKey, TabletModalPanel.tr("ui.questsandstuff.menu.remove"), () -> {
                deleteAssetFile(state.pickers.assetContextFile);
                state.pickers.assetContextOpen = false;
                state.pickers.assetRenameOpen = false;
            }));
        }
        return sections.build();
    }

    static void commitAssetRename(TabletUiState state, TextFieldWidget rename) {
        String oldRelative = state.pickers.assetContextFile;
        String parent = TabletModalPanel.parentRelativePath(oldRelative);
        String name = rename == null || rename.getCurrentString() == null ? state.pickers.assetRenameDraft : rename.getCurrentString().trim();
        if (oldRelative == null || oldRelative.isBlank() || name.isBlank()) {
            state.pickers.assetRenameOpen = false;
            state.pickers.assetContextOpen = false;
            return;
        }
        String nextRelative = renamedAssetPath(oldRelative, parent, name);
        renameAssetFile(oldRelative, name);
        state.pickers.assetContextFile = nextRelative;
        state.pickers.assetSelected = nextRelative;
        state.pickers.assetRenameOpen = false;
        state.pickers.assetContextOpen = false;
    }

    static String renamedAssetPath(String oldRelative, String parent, String name) {
        String nextName = name;
        if (!nextName.contains(".")) {
            String oldName = TabletModalPanel.fileNameFromRelativePath(oldRelative);
            int dot = oldName.lastIndexOf('.');
            if (dot > 0) {
                nextName = nextName + oldName.substring(dot);
            }
        }
        return parent.isBlank() ? nextName : parent + "/" + nextName;
    }
}
