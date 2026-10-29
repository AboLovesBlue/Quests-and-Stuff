package com.abo47.questsandstuff.client.tablet.ui.factory;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.player.Player;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import com.abo47.questsandstuff.client.tablet.TabletComposer;
import com.abo47.questsandstuff.client.tablet.assets.AssetLibrary;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.CanvasRenderer;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.CanvasPoint;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.ConnectionHit;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.QuestCardLayout;
import com.abo47.questsandstuff.client.tablet.quest.editor.EditorCanvasCommandClient;
import com.abo47.questsandstuff.client.tablet.quest.editor.EditorChapterCommandClient;
import com.abo47.questsandstuff.client.tablet.quest.editor.EditorQuestCommandClient;
import com.abo47.questsandstuff.client.tablet.state.UiState;
import com.abo47.questsandstuff.client.tablet.ui.Persistence;
import com.abo47.questsandstuff.client.tablet.ui.state.ActiveState;
import com.abo47.questsandstuff.client.tablet.ui.widget.Layout;
import com.abo47.questsandstuff.client.tablet.ui.widget.Widgets;

public final class UiFactory {
    public static final int ROOT_W = Layout.ROOT_W;
    public static final int ROOT_H = Layout.ROOT_H;
    public static final int ROOT_PAD_X = Layout.ROOT_PAD_X;
    public static final int ROOT_PAD_Y = Layout.ROOT_PAD_Y;
    public static final int PANEL_GAP = Layout.PANEL_GAP;
    public static final int PANEL_INSET = Layout.PANEL_INSET;
    public static final int CANVAS_VIEWPORT_GUTTER_X = Layout.CANVAS_VIEWPORT_GUTTER_X;
    public static final int CANVAS_VIEWPORT_GUTTER_TOP = Layout.CANVAS_VIEWPORT_GUTTER_TOP;
    public static final int CANVAS_VIEWPORT_GUTTER_BOTTOM = Layout.CANVAS_VIEWPORT_GUTTER_BOTTOM;
    public static final int CHAPTER_PANEL_GUTTER_X = Layout.CHAPTER_PANEL_GUTTER_X;
    public static final int CHAPTER_PANEL_GUTTER_BOTTOM = Layout.CHAPTER_PANEL_GUTTER_BOTTOM;
    public static final int HEADER_H = Layout.HEADER_H;
    public static final int HEADER_GAP = Layout.HEADER_GAP;
    public static final int PAD = Layout.PAD;
    public static final int PAD_Y = Layout.PAD_Y;
    public static final int GAP = Layout.GAP;
    public static final int BODY_X = Layout.BODY_X;
    public static final int BODY_Y = Layout.BODY_Y;
    public static final int BODY_W = Layout.BODY_W;
    public static final int BODY_H = Layout.BODY_H;
    public static final int CHAPTER_W = Layout.CHAPTER_W;
    public static final int CHAPTER_W_MIN = Layout.CHAPTER_W_MIN;
    public static final int CHAPTER_W_ICON = Layout.CHAPTER_W_ICON;
    public static final int CHAPTER_W_MAX = Layout.CHAPTER_W_MAX;
    public static final int CHAPTER_W_ICON_SNAP = Layout.CHAPTER_W_ICON_SNAP;
    public static final int SPLITTER_W = Layout.SPLITTER_W;
    public static final int PANEL_W_MIN = Layout.PANEL_W_MIN;
    public static final int CANVAS_W = Layout.CANVAS_W;
    public static final int CHAPTER_CARD_H = Layout.CHAPTER_CARD_H;
    public static final int CHAPTER_CARD_GAP = Layout.CHAPTER_CARD_GAP;
    public static final int CHAPTER_COLLAPSED_ROW_STEP = Layout.CHAPTER_COLLAPSED_ROW_STEP;
    public static final String DRAFT_CHAPTER = Layout.DRAFT_CHAPTER;
    public static final Path ASSETS_ROOT_DIR = Layout.ASSETS_ROOT_DIR;
    public static final int CHAPTER_X = Layout.CHAPTER_X;
    public static final int CHAPTER_Y = Layout.CHAPTER_Y;
    public static final int CHAPTER_H = Layout.CHAPTER_H;
    public static final int CANVAS_X = Layout.CANVAS_X;
    public static final int CANVAS_Y = Layout.CANVAS_Y;
    public static final int CANVAS_H = Layout.CANVAS_H;
    public static final int CANVAS_TOP_H_COMPACT = Layout.CANVAS_TOP_H_COMPACT;
    public static final int CANVAS_TOP_H_EXPANDED = Layout.CANVAS_TOP_H_EXPANDED;
    public static final int CANVAS_GRID_ROWS = Layout.CANVAS_GRID_ROWS;
    public static final int CANVAS_GRID_COLS = Layout.CANVAS_GRID_COLS;
    public static final int CARD_W = Layout.CARD_W;
    public static final int CARD_H = Layout.CARD_H;
    public static final int CONTEXT_ROW_H = Layout.CONTEXT_ROW_H;
    public static final int[] GRID_SIZES = Layout.GRID_SIZES;
    public static final int[] GRID_OPACITY = Layout.GRID_OPACITY;
    public static final int[] CANVAS_BG_OPACITY = Layout.CANVAS_BG_OPACITY;
    public static final int[] CANVAS_LIMIT_WIDTH = Layout.CANVAS_LIMIT_WIDTH;
    public static final int[] CANVAS_LIMIT_HEIGHT = Layout.CANVAS_LIMIT_HEIGHT;
    public static final String[] CANVAS_LIMIT_LABELS = Layout.CANVAS_LIMIT_LABELS;
    public static final int CHAPTER_SCROLL_W = Layout.CHAPTER_SCROLL_W;
    public static final int SHARED_MENU_W = Layout.SHARED_MENU_W;
    public static final int CONTENT_ICON_SIZE = 16;
    public static final int ACTION_ICON_SIZE = 12;

    private UiFactory() {
    }

    public static void setActiveTabletRefresh(Runnable refresh) {
        ActiveState.setActiveTabletRefresh(refresh);
    }

    public static UiState getActiveTabletState() {
        return ActiveState.getActiveTabletState();
    }

    public static void setActiveTabletState(UiState state) {
        ActiveState.setActiveTabletState(state);
    }

    public static void refreshActiveTablet() {
        ActiveState.refreshActiveTablet();
    }

    public static String activeSelectedChapter() {
        return ActiveState.activeSelectedChapter();
    }

    public static void syncCanvasStateFromCache(UiState state) {
        ActiveState.syncCanvasStateFromCache(state);
    }

    public static void syncActiveCanvasStateFromCache() {
        ActiveState.syncActiveCanvasStateFromCache();
    }

    public static void selectPastedQuests(ListTag ids) {
        ActiveState.selectPastedQuests(ids);
    }

    public static void selectPastedQuests(CompoundTag payload) {
        ActiveState.selectPastedQuests(payload);
    }

    public static WidgetGroup create(Player player) {
        return TabletComposer.create(player);
    }

    public static WidgetGroup create(Player player, int rootWidth, int rootHeight, boolean fullScreenMode) {
        return TabletComposer.create(player, rootWidth, rootHeight, fullScreenMode);
    }

    public static void applyRootSize(UiState state, int width, int height, boolean fullScreenMode) {
        Layout.applyRootSize(state, width, height, fullScreenMode);
    }

    public static int chapterHeight(UiState state) {
        return Layout.chapterHeight(state);
    }

    public static int canvasHeight(UiState state) {
        return Layout.canvasHeight(state);
    }

    public static int chapterPanelWidth(UiState state) {
        return Layout.chapterPanelWidth(state);
    }

    public static boolean isChapterPanelCollapsed(UiState state) {
        return Layout.isChapterPanelCollapsed(state);
    }

    public static int canvasPanelX(UiState state) {
        return Layout.canvasPanelX(state);
    }

    public static int canvasPanelWidth(UiState state) {
        return Layout.canvasPanelWidth(state);
    }

    public static int[] canvasViewportBounds(int panelW, int panelH, int topH) {
        return Layout.canvasViewportBounds(panelW, panelH, topH);
    }

    public static String uniqueChapterName(String preferred, String excludeCurrent) {
        return EditorChapterCommandClient.uniqueChapterName(preferred, excludeCurrent);
    }

    public static String sanitizeChapterName(String value) {
        return EditorChapterCommandClient.sanitizeChapterName(value);
    }

    public static void runChapterAction(Player player, UiState state, String action, String chapter, String value, int offset) {
        EditorChapterCommandClient.runChapterAction(player, state, action, chapter, value, offset);
    }

    public static void runCanvasMoveAction(Player player, UiState state, Map<String, CanvasPoint> positions) {
        EditorCanvasCommandClient.runCanvasMoveAction(player, state, positions);
    }

    public static void runPrerequisiteAction(Player player, String questId, String prerequisiteId, boolean add) {
        EditorCanvasCommandClient.runPrerequisiteAction(player, questId, prerequisiteId, add);
    }

    public static void runQuestIconAction(Player player, String questId, String icon) {
        EditorQuestCommandClient.runQuestIconAction(player, questId, icon);
    }

    public static void runRemoveQuestAction(Player player, String questId) {
        EditorQuestCommandClient.runRemoveQuestAction(player, questId);
    }

    public static void addQuestAt(Player player, UiState state, int logicalX, int logicalY, String title) {
        EditorQuestCommandClient.addQuestAt(player, state, logicalX, logicalY, title);
    }

    public static int snapToGrid(UiState state, int value) {
        return CanvasRenderer.snapToGrid(state, value);
    }

    public static QuestCardLayout hitTestCard(List<QuestCardLayout> cards, int x, int y) {
        return CanvasRenderer.hitTestCard(cards, x, y);
    }

    public static ConnectionHit hitTestConnection(UiState state, List<QuestCardLayout> cards, Map<String, QuestCardLayout> byQuestId, int x, int y) {
        return CanvasRenderer.hitTestConnection(state, cards, byQuestId, x, y);
    }

    public static boolean isContextMenuHit(UiState state, int x, int y) {
        return CanvasRenderer.isContextMenuHit(state, x, y);
    }

    public static int chapterBackgroundFill(String background, int fallback) {
        return Assets.chapterBackgroundFill(background, fallback);
    }

    public static IGuiTexture chapterBackgroundTexture(String background) {
        return Assets.chapterBackgroundTexture(background);
    }

    public static IGuiTexture chapterBackgroundTexture(String background, boolean grayscale) {
        return Assets.chapterBackgroundTexture(background, grayscale);
    }

    public static ItemStackTexture iconTexture(String iconId) {
        return Assets.iconTexture(iconId);
    }

    public static List<AssetLibrary.AssetEntry> listAssetEntries(String relativeDir) {
        return Assets.listAssetEntries(relativeDir);
    }

    public static List<AssetLibrary.AssetEntry> searchAssetEntries(String relativeDir, String query) {
        return Assets.searchAssetEntries(relativeDir, query);
    }

    public static AssetLibrary.AssetDimensions assetDimensions(String relativePath) {
        return Assets.assetDimensions(relativePath);
    }

    public static IGuiTexture assetThumbnailTexture(String relativePath) {
        return Assets.assetThumbnailTexture(relativePath);
    }

    public static void ensureAssetsDirs() {
        Assets.ensureAssetsDirs();
    }

    public static void prewarmClientUiAssets() {
        Assets.prewarmClientUiAssets();
    }

    public static int indexAtY(int localY, UiState state) {
        return Layout.indexAtY(localY, state);
    }

    public static int chapterInsertIndexAtY(int localY, UiState state) {
        return Layout.chapterInsertIndexAtY(localY, state);
    }

    public static int chapterIndexAtY(int localY, UiState state) {
        return Layout.chapterIndexAtY(localY, state);
    }

    public static int chapterRowStep(UiState state) {
        return Layout.chapterRowStep(state);
    }

    public static boolean isChapterScrollBarHit(int localX, int localY, UiState state) {
        return Layout.isChapterScrollBarHit(localX, localY, state);
    }

    public static boolean isChapterCardAreaHit(int localX, int localY, UiState state) {
        return Layout.isChapterCardAreaHit(localX, localY, state);
    }

    public static void updateChapterScrollByMouse(double mouseY, UiState state) {
        Layout.updateChapterScrollByMouse(mouseY, state);
    }

    public static String chapterAtY(int localY, UiState state) {
        return Layout.chapterAtY(localY, state);
    }

    public static int chapterTextMenuY(UiState state, int listHeight) {
        return Layout.chapterTextMenuY(state, listHeight);
    }

    public static int chapterTextMenuX(UiState state) {
        return Layout.chapterTextMenuX(state);
    }

    public static int chapterTextMenuWidth(UiState state) {
        return Layout.chapterTextMenuWidth(state);
    }

    public static int chapterTextMenuHeight(UiState state) {
        return Layout.chapterTextMenuHeight(state);
    }

    public static void deleteAssetFile(String relativePath) {
        Assets.deleteAssetFile(relativePath);
    }

    public static void renameAssetFile(String relativePath, String targetNameRaw) {
        Assets.renameAssetFile(relativePath, targetNameRaw);
    }

    public static String shortQuestId(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        int slash = value.lastIndexOf('/');
        return slash >= 0 && slash + 1 < value.length() ? value.substring(slash + 1) : value;
    }

    public static WidgetGroup panel(int x, int y, int w, int h, int fill, int border) {
        return Widgets.panel(x, y, w, h, fill, border);
    }

    public static LabelWidget label(int x, int y, String text, int color) {
        return Widgets.label(x, y, text, color);
    }

    public static LabelWidget dynamicLabel(int x, int y, java.util.function.Supplier<String> supplier, int color) {
        return Widgets.dynamicLabel(x, y, supplier, color);
    }

    public static void addWindowsContextRow(WidgetGroup menu, int y, int width, String text, String icon, java.util.function.Consumer<com.lowdragmc.lowdraglib.gui.util.ClickData> callback) {
        Widgets.addWindowsContextRow(menu, y, width, text, icon, callback);
    }

    public static void addWindowsContextRow(WidgetGroup menu, int y, int width, String text, String icon, boolean submenu, java.util.function.Consumer<com.lowdragmc.lowdraglib.gui.util.ClickData> callback) {
        Widgets.addWindowsContextRow(menu, y, width, text, icon, submenu, callback);
    }

    public static void addWindowsContextRow(WidgetGroup menu, int y, int width, String text, String icon, int iconColor, boolean submenu, java.util.function.Consumer<com.lowdragmc.lowdraglib.gui.util.ClickData> callback) {
        Widgets.addWindowsContextRow(menu, y, width, text, icon, iconColor, submenu, callback);
    }

    public static String contextIconForLabel(String label) {
        return Widgets.contextIconForLabel(label);
    }

    public static ButtonWidget button(int x, int y, int w, int h, String text, int baseColor, int activeColor, java.util.function.Consumer<com.lowdragmc.lowdraglib.gui.util.ClickData> callback) {
        return Widgets.button(x, y, w, h, text, baseColor, activeColor, callback);
    }

    public static ButtonWidget closeIconButton(int x, int y, int w, int h, java.util.function.Consumer<com.lowdragmc.lowdraglib.gui.util.ClickData> callback) {
        return Widgets.closeIconButton(x, y, w, h, callback);
    }

    public static ButtonWidget flatHitButton(int x, int y, int w, int h, java.util.function.Consumer<com.lowdragmc.lowdraglib.gui.util.ClickData> callback) {
        return Widgets.flatHitButton(x, y, w, h, callback);
    }

    public static String pendingDeleteLabel(UiState state, String key, String fallback) {
        return Widgets.pendingDeleteLabel(state, key, fallback);
    }

    public static boolean confirmDeleteClick(UiState state, String key) {
        return Widgets.confirmDeleteClick(state, key);
    }

    public static void readPersistedUiState(UiState state) {
        Persistence.readPersistedUiState(state);
    }

    public static boolean readPersistedEditMode() {
        return Persistence.readPersistedEditMode();
    }

    public static void persistUiState(UiState state) {
        Persistence.persistUiState(state);
    }

    public static void persistEditMode(boolean enabled) {
        Persistence.persistEditMode(enabled);
    }

    public static void readPersistedSkinState(UiState state) {
        Persistence.readPersistedSkinState(state);
    }

    public static void persistSkinState(UiState state) {
        Persistence.persistSkinState(state);
    }
}
