package com.abo47.questsandstuff.client.tablet.quest.details;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import com.abo47.questsandstuff.client.tablet.modal.target.ModalTargetParser;
import com.abo47.questsandstuff.client.tablet.state.UiState;
import com.abo47.questsandstuff.client.tablet.ui.factory.UiFactory;

public final class QuestDetailsWindow {
    static final int WINDOW_W = UiFactory.BODY_W;
    static final int WINDOW_H = UiFactory.BODY_H;
    public static final int CONTENT_INSET = UiFactory.PANEL_INSET;
    public static final int TOP_Y = CONTENT_INSET;
    public static final int HEADER_H = UiFactory.HEADER_H;
    static final int HEADER_GAP = UiFactory.HEADER_GAP;
    static final int TOOL_SIZE = HEADER_H;

    private QuestDetailsWindow() {
    }

    public static void open(UiState state, String questId) {
        QuestDetailsWindowLifecycle.open(state, questId);
    }

    public static void openAtSource(UiState state, String questId, int sourceX, int sourceY, int sourceW, int sourceH) {
        QuestDetailsWindowLifecycle.openAtSource(state, questId, sourceX, sourceY, sourceW, sourceH);
    }

    public static void swapQuest(UiState state, String questId) {
        QuestDetailsWindowLifecycle.swapQuest(state, questId);
    }

    public static void rebuild(WidgetGroup layer, UiState state, Player player, Runnable refresh) {
        QuestDetailsWindowLayout.rebuild(layer, state, player, refresh);
    }

    public static void syncScreenOrigin(WidgetGroup layer, UiState state) {
        QuestDetailsWindowLayout.syncScreenOrigin(layer, state);
    }

    public static boolean isVisible(UiState state) {
        return state != null && (state.questDetails.questDetailsOpen || state.questDetails.questDetailsClosing);
    }

    public static boolean isInteractive(UiState state) {
        return state != null && state.questDetails.questDetailsOpen && !state.questDetails.questDetailsClosing;
    }

    public static boolean finishCloseIfDone(UiState state) {
        return QuestDetailsWindowLifecycle.finishCloseIfDone(state);
    }

    public static boolean isInside(UiState state, double mouseX, double mouseY) {
        return QuestDetailsWindowHitTest.isInside(state, mouseX, mouseY);
    }

    public static boolean isContextMenuHit(UiState state, double mouseX, double mouseY) {
        return QuestDetailsWindowHitTest.isContextMenuHit(state, mouseX, mouseY);
    }

    public static boolean isTextStyleMenuHit(UiState state, double mouseX, double mouseY) {
        return QuestDetailsWindowHitTest.isTextStyleMenuHit(state, mouseX, mouseY);
    }

    public static boolean isTextStyleOwnerHit(UiState state, double mouseX, double mouseY) {
        return QuestDetailsWindowHitTest.isTextStyleOwnerHit(state, mouseX, mouseY);
    }

    public static boolean isTextEditorHit(UiState state, double mouseX, double mouseY) {
        return QuestDetailsWindowHitTest.isTextEditorHit(state, mouseX, mouseY);
    }

    public static void applyIconPick(Player player, UiState state, String entry) {
        QuestDetailsWindowActions.applyIconPick(player, state, entry);
    }

    public static void applyBiomePick(Player player, UiState state, String biome) {
        QuestDetailsWindowActions.applyBiomePick(player, state, biome);
    }

    public static void applyAdvancementPick(Player player, UiState state, String advancement) {
        QuestDetailsWindowActions.applyAdvancementPick(player, state, advancement);
    }

    public static void applyRecipePick(Player player, UiState state, String recipe) {
        QuestDetailsWindowActions.applyRecipePick(player, state, recipe);
    }

    public static void applyStructurePick(Player player, UiState state, String structure) {
        QuestDetailsWindowActions.applyStructurePick(player, state, structure);
    }

    public static void applyBlockPick(Player player, UiState state, String block) {
        QuestDetailsWindowActions.applyBlockPick(player, state, block);
    }

    public static void applyStatPick(Player player, UiState state, String stat) {
        QuestDetailsWindowActions.applyStatPick(player, state, stat);
    }

    public static void applyDimensionPick(Player player, UiState state, String dimension) {
        QuestDetailsWindowActions.applyDimensionPick(player, state, dimension);
    }

    public static void applyLootTablePick(Player player, UiState state, String lootTable) {
        QuestDetailsWindowActions.applyLootTablePick(player, state, lootTable);
    }

    public static void applyItemLockPick(Player player, UiState state, String entry) {
        QuestDetailsWindowActions.applyItemLockPick(player, state, entry);
    }

    public static void applyInventoryItemPick(Player player, UiState state, ItemStack stack) {
        QuestDetailsWindowActions.applyInventoryItemPick(player, state, stack);
    }

    public static void applyAssetPick(Player player, UiState state, String asset) {
        QuestDetailsWindowActions.applyAssetPick(player, state, asset);
    }

    public static void applyAssetPick(Player player, UiState state, ModalTargetParser.Target target, String asset) {
        QuestDetailsWindowActions.applyAssetPick(player, state, target, asset);
    }

    public static String descriptionImageAsset(String questId, String imageId) {
        return QuestDetailsWindowActions.descriptionImageAsset(questId, imageId);
    }

    public static void applyEntityVariantPick(Player player, UiState state, String questId, String imageId, String variantKey) {
        QuestDetailsWindowActions.applyEntityVariantPick(player, state, questId, imageId, variantKey);
    }

    public static void applyTextColor(Player player, UiState state, String target, int color) {
        QuestDetailsWindowActions.applyTextColor(player, state, target, color);
    }

    public static void applyTextColor(Player player, UiState state, ModalTargetParser.Target target, int color) {
        QuestDetailsWindowActions.applyTextColor(player, state, target, color);
    }

    public static boolean handleClipboardShortcut(Player player, UiState state, int keyCode) {
        return QuestDetailsWindowActions.handleClipboardShortcut(player, state, keyCode);
    }

    public static boolean beginSelectedRename(UiState state) {
        return QuestDetailsWindowActions.beginSelectedRename(state);
    }

    public static boolean deleteSelected(Player player, UiState state) {
        return QuestDetailsWindowActions.deleteSelected(player, state);
    }

    public static boolean duplicateSelected(Player player, UiState state) {
        return QuestDetailsWindowActions.duplicateSelected(player, state);
    }

    public static boolean selectAllDescription(UiState state) {
        return QuestDetailsWindowActions.selectAllDescription(state);
    }

    public static boolean nudgeSelected(Player player, UiState state, int dx, int dy) {
        return QuestDetailsWindowActions.nudgeSelected(player, state, dx, dy);
    }

    public static void claimAll(Player player, String questId) {
        QuestDetailsWindowActions.claimAll(player, questId);
    }

    public static void openIconPicker(UiState state, String target) {
        QuestDetailsWindowActions.openIconPicker(state, target);
    }

    public static void openBiomePicker(UiState state, String target) {
        QuestDetailsWindowActions.openBiomePicker(state, target);
    }

    public static void openAdvancementPicker(UiState state, String target) {
        QuestDetailsWindowActions.openAdvancementPicker(state, target);
    }

    public static void openRecipePicker(UiState state, String target) {
        QuestDetailsWindowActions.openRecipePicker(state, target);
    }

    public static void openStructurePicker(UiState state, String target) {
        QuestDetailsWindowActions.openStructurePicker(state, target);
    }

    public static void openBlockPicker(UiState state, String target) {
        QuestDetailsWindowActions.openBlockPicker(state, target);
    }

    public static void openStatPicker(UiState state, String target) {
        QuestDetailsWindowActions.openStatPicker(state, target);
    }

    public static void openDimensionPicker(UiState state, String target) {
        QuestDetailsWindowActions.openDimensionPicker(state, target);
    }

    public static void openLootTablePicker(UiState state, String target) {
        QuestDetailsWindowActions.openLootTablePicker(state, target);
    }

    public static void openItemLockPicker(UiState state, String target) {
        QuestDetailsWindowActions.openItemLockPicker(state, target);
    }

    public static void openItemInventoryPicker(UiState state, String target) {
        QuestDetailsWindowActions.openItemInventoryPicker(state, target);
    }

    public static void openAssetPicker(UiState state, String target) {
        QuestDetailsWindowActions.openAssetPicker(state, target);
    }

    public static void close(UiState state) {
        QuestDetailsWindowLifecycle.close(state);
    }

    public static void finishCloseAnimation(UiState state) {
        QuestDetailsWindowLifecycle.finishClose(state);
    }

    public static int leftPanelWidth(UiState state) {
        return QuestDetailsWindowGeometry.leftPanelWidth(state);
    }

    public static int descriptionContentWidth(UiState state) {
        return QuestDetailsWindowGeometry.descriptionContentWidth(state);
    }

    static int canvasPanelWidth(int leftW, int frameW) {
        return QuestDetailsWindowGeometry.canvasPanelWidth(leftW, frameW);
    }

    static int[] mainCanvasViewport(int canvasW, int frameH) {
        return QuestDetailsWindowGeometry.mainCanvasViewport(canvasW, frameH);
    }

    static void openAdjacentQuest(UiState state, String questId, int direction) {
        QuestDetailsWindowLifecycle.openAdjacentQuest(state, questId, direction);
    }
}
