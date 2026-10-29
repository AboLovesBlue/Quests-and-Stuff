package com.abo47.questsandstuff.client.tablet.theme.skin;

import com.abo47.questsandstuff.client.tablet.modal.AssetPickerOpenActions;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.TransformTexture;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextAction;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextActionFactory;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuPanel;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuPlacement;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuRenderer;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuSection;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuSections;
import com.abo47.questsandstuff.client.tablet.controls.IconTextButton;
import com.abo47.questsandstuff.client.tablet.controls.FourFieldEditor;
import com.abo47.questsandstuff.client.tablet.controls.TwoFieldEditor;
import com.abo47.questsandstuff.client.tablet.modal.ModalStateQueries;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasViewport;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsRootWidget;
import com.abo47.questsandstuff.client.tablet.root.RootWidget;
import com.abo47.questsandstuff.client.tablet.state.UiState;
import com.abo47.questsandstuff.client.tablet.text.TranslationKeys;
import com.abo47.questsandstuff.client.tablet.theme.tokens.ThemeColors;
import com.abo47.questsandstuff.client.tablet.ui.factory.UiFactory;

public final class SkinEditManager {
    private SkinEditManager() {
    }

    private record CapturedOriginal(String targetKey, IGuiTexture original) {
    }

    private static final IdentityHashMap<Widget, CapturedOriginal> ORIGINAL_BACKGROUNDS = new IdentityHashMap<>();
    private static Widget selectedTargetWidget;

    public static Widget selectedTargetWidget(RootWidget root) {
        Widget w = selectedTargetWidget;
        if (w == null) return null;
        for (Widget cur = w.getParent(); cur != null; cur = cur.getParent()) {
            if (cur == root) return w;
        }
        selectedTargetWidget = null;
        return null;
    }

    public static boolean handleClick(UiState state, RootWidget root, Runnable refresher, int mouseX, int mouseY, int button) {
        if (state == null || !state.root.skinEditMode) return false;
        if (ModalStateQueries.anyOpen(state)) {
            if (!state.root.skinEditSelectedTarget.isEmpty()) {
                state.root.skinEditSelectedTarget = "";
                selectedTargetWidget = null;
                root.closeContextMenu();
                state.root.skinModeEditorOpen = false;
            }
            return false;
        }

        if (root.isContextMenuOpen()) {
            if (root.isContextMenuAt(mouseX, mouseY)) {
                root.clickContextMenu(mouseX, mouseY, button);
                return true;
            }
            if (!state.root.skinModeEditorOpen) {
                root.closeContextMenu();
                state.root.skinModeEditorOpen = false;
            }
            return true;
        }

        Widget homeBtn = root.getHomeButton();
        Widget hitWidget;
        String hitKey;
        if (homeBtn != null && homeBtn.isVisible() && homeBtn.isMouseOverElement(mouseX, mouseY)) {
            hitWidget = homeBtn;
            hitKey = SkinTargetKeys.HOME_BTN;
        } else if (isMinimapHit(state, root, mouseX, mouseY)) {
            hitWidget = null;
            hitKey = minimapHitKey(state, root, mouseX, mouseY);
        } else {
            hitWidget = SkinEditTargetResolver.findTargetWidgetAt(root, mouseX, mouseY);
            hitKey = SkinEditTargetResolver.keyForTarget(root, hitWidget);
        }

        if (button == 0) {
            state.root.skinEditSelectedTarget = hitKey != null ? hitKey : "";
            selectedTargetWidget = hitWidget;
            return true;
        }

        if (button == 1) {
            String currentSelection = state.root.skinEditSelectedTarget;
            if (hitKey != null && hitKey.equals(currentSelection)) {
                buildContextMenu(state, root, refresher, mouseX, mouseY);
                return true;
            }
            state.root.skinEditSelectedTarget = "";
            selectedTargetWidget = null;
            return true;
        }

        return true;
    }

    public static void reapplyOverrides(UiState state, WidgetGroup root) {
        if (state == null || root == null) return;
        state.root.activeSkinTargets.clear();
        if (state.root.skinFillOverrides.isEmpty()) {
            resetAllCaptured();
            return;
        }
        Set<String> activeTargets = new HashSet<>();
        for (var entry : state.root.skinFillOverrides.entrySet()) {
            String targetKey = SkinOverrideKey.bareKeyForCurrentApp(state, entry.getKey());
            if (targetKey != null) activeTargets.add(targetKey);
        }
        resetRemovedTargets(activeTargets);
        applyOverrides(state, root, activeTargets);
    }

    private static void applyOverrides(UiState state, WidgetGroup root, Set<String> activeTargets) {
        Set<Widget> touched = new HashSet<>();
        for (var entry : state.root.skinFillOverrides.entrySet()) {
            String entryKey = entry.getKey();

            String targetKey = SkinOverrideKey.bareKeyForCurrentApp(state, entryKey);
            if (targetKey == null) continue;
            if (SkinTargetKeys.ROOT.equals(targetKey)) continue;
            if (SkinOverrideKey.isSharedKey(targetKey) && !SkinOverrideKey.isSharedKey(entryKey)) continue;

            if (isMinimapKey(targetKey)) {
                SkinFillOverride.clearCache();
                state.root.activeSkinTargets.add(targetKey);
                continue;
            }

            Widget w = SkinEditTargetResolver.widgetForKey(root, targetKey);
            if (w == null) {
                QuestsAndStuffMod.debugLog("[QnS:Skin] reapply SKIP widget not found: target={}, entryKey={}", targetKey, entryKey);
                continue;
            }
            SkinFillOverride override = SkinFillOverride.parse(entry.getValue());
            if (override == null) continue;
            IGuiTexture tex = override.createTexture();
            if (tex == null) continue;

            QuestsAndStuffMod.debugLog("[QnS:Skin] reapply target={}, mode={}, texClass={}, shared={}, card={}, root={}",
                    targetKey, override.mode(), tex.getClass().getSimpleName(),
                    SkinOverrideKey.isSharedKey(targetKey), SkinOverrideKey.isCardKey(targetKey), SkinOverrideKey.isRootKey(targetKey));

            if (SkinOverrideKey.isSharedKey(targetKey) && w instanceof WidgetGroup wg) {
                if (SkinOverrideKey.isRootKey(targetKey)) {
                    applyToWidget(w, targetKey, tex, touched);
                } else if (!SkinOverrideKey.isCardKey(targetKey)) {
                    for (Widget child : wg.widgets) {
                        if (!SkinEditTargetResolver.hasCustomChrome(child)
                                || child instanceof IconTextButton
                                || child instanceof TextFieldWidget) {
                            if (SkinTargetKeys.SETTINGS_TAB_LAYER.equals(targetKey) && child instanceof WidgetGroup tabContainer && !tabContainer.widgets.isEmpty()) {
                                applyToWidget(tabContainer.widgets.get(0), targetKey, tex, touched);
                            } else {
                                applyToWidget(child, targetKey, tex, touched);
                            }
                        }
                    }
                }
            } else {
                applyToWidget(w, targetKey, tex, touched);
            }
            state.root.activeSkinTargets.add(targetKey);
        }
        pruneStaleCaptures(touched);
    }

    private static void applyToWidget(Widget w, String targetKey, IGuiTexture tex, Set<Widget> touched) {
        CapturedOriginal cap = ORIGINAL_BACKGROUNDS.computeIfAbsent(w, k -> new CapturedOriginal(targetKey, w.getBackgroundTexture()));
        int[] offsets = skinExtendOffsets(w, targetKey, cap.original());
        if (offsets != null) {
            IGuiTexture inner = tex;
            int dx = offsets[0];
            int dy = offsets[1];
            int dw = offsets[2];
            int dh = offsets[3];
            tex = new TransformTexture() {
                {
                    xOffset = dx;
                    yOffset = dy;
                }
                @Override
                protected void drawInternal(GuiGraphics graphics, int mouseX, int mouseY, float x, float y, int width, int height) {
                    inner.draw(graphics, mouseX, mouseY, x, y, width + dw, height + dh);
                }
            };
        }

        touched.add(w);
        w.setBackground(tex);
        w.setDrawBackgroundWhenHover(true);
    }

    private static int[] skinExtendOffsets(Widget w, String targetKey, IGuiTexture original) {
        if (original instanceof GuiTextureGroup) {
            return new int[]{-1, -1, 2, 2};
        }
        if (original == null || original.equals(IGuiTexture.EMPTY)) {
            if (SkinOverrideKey.isCardKey(targetKey)) {
                return new int[]{-1, -1, 2, 2};
            }
            Class<?> cls = w.getClass();
            if (cls == IconTextButton.class || cls == QuestDetailsRootWidget.class
                    || cls == TextFieldWidget.class) {
                return new int[]{-1, -1, 2, 2};
            }
        }
        return null;
    }

    private static void resetRemovedTargets(Set<String> activeTargets) {
        ORIGINAL_BACKGROUNDS.entrySet().removeIf(entry -> {
            if (!activeTargets.contains(entry.getValue().targetKey())) {
                Widget w = entry.getKey();
                if (w != null) {
                    w.setBackground(entry.getValue().original());
                }
                return true;
            }
            return false;
        });
    }

    private static void pruneStaleCaptures(Set<Widget> touched) {
        ORIGINAL_BACKGROUNDS.keySet().removeIf(widget -> !touched.contains(widget));
    }

    private static void resetAllCaptured() {
        for (var entry : ORIGINAL_BACKGROUNDS.entrySet()) {
            Widget w = entry.getKey();
            if (w != null) {
                w.setBackground(entry.getValue().original());
            }
        }
        ORIGINAL_BACKGROUNDS.clear();
    }

    public static void restoreOverride(UiState state, WidgetGroup root, String targetKey) {
        if (targetKey == null) return;
        String bare = SkinOverrideKey.resolveTargetKey(state, targetKey);
        ORIGINAL_BACKGROUNDS.entrySet().removeIf(entry -> {
            if (entry.getValue().targetKey().equals(bare)) {
                Widget w = entry.getKey();
                if (w != null) {
                    w.setBackground(entry.getValue().original());
                }
                return true;
            }
            return false;
        });
        reapplyOverrides(state, root);
    }

    public static Widget findWidgetByKey(WidgetGroup root, String targetKey) {
        return SkinEditTargetResolver.widgetForKey(root, targetKey);
    }

    private static String resolveSkinTarget(UiState state, String targetKey) {
        return SkinOverrideKey.resolveTargetKey(state, targetKey);
    }

    private static void buildContextMenu(UiState state, RootWidget root, Runnable refresher, int mouseX, int mouseY) {
        String targetKey = state.root.skinEditSelectedTarget;
        if (targetKey == null || targetKey.isBlank()) return;

        String resolvedTarget = resolveSkinTarget(state, targetKey);

        String rawOverride = SkinOverrideKey.resolveOverride(state, resolvedTarget);
        SkinFillOverride currentOverride = SkinFillOverride.parse(rawOverride);
        String currentMode = currentOverride != null ? currentOverride.mode() : "stretch";
        String currentAsset = currentOverride != null ? currentOverride.path() : "";

        ContextMenuSections sections = new ContextMenuSections();
        sections.add(ContextMenuSection.PRIMARY, ContextActionFactory.action(
                TranslationKeys.text("ui.questsandstuff.skin.change_texture"),
                "image",
                ThemeColors.INTERACTIVE,
                () -> {
                    root.closeContextMenu();
                    state.modal.skinEditFillTarget = resolvedTarget;
                    AssetPickerOpenActions.openAssetPicker(state, resolvedTarget, currentAsset);
                    if (refresher != null) refresher.run();
                }));

        List<ContextAction> modeActions = new ArrayList<>();
        for (SkinFillMode spec : SkinFillMode.values()) {
            boolean active = currentMode.equals(spec.mode);
            modeActions.add(ContextActionFactory.action(
                    TranslationKeys.text(spec.titleKey),
                    spec.icon,
                    active ? ThemeColors.SUCCESS : ThemeColors.TEXT_SECONDARY,
                    () -> {
                        QuestsAndStuffMod.debugLog("[QnS:Skin] mode action clicked: {}, asset={}", spec.mode, currentAsset);
                        root.closeContextMenu();
                        openModeEditorFor(state, root, refresher, resolvedTarget, currentAsset, mouseX, mouseY, spec, currentOverride);
                    }));
        }
        sections.add(ContextMenuSection.APPEARANCE, ContextActionFactory.submenu(
                TranslationKeys.text("ui.questsandstuff.skin.change_mode"),
                "layout-dashboard",
                ThemeColors.TEXT_PRIMARY,
                modeActions));

        if (rawOverride != null && !rawOverride.isBlank()) {
            String skinTexKey = "skin_remove_tex:" + resolvedTarget;
            sections.add(ContextMenuSection.DANGER, ContextActionFactory.warningDelete(state, skinTexKey,
                    TranslationKeys.text("ui.questsandstuff.skin.remove_texture"),
                    () -> {
                        root.closeContextMenu();
                        String qualified = SkinOverrideKey.overrideKey(state, resolvedTarget);
                        state.root.skinFillOverrides.remove(qualified);
                        String bare = SkinOverrideKey.resolveTargetKey(state, resolvedTarget);
                        if (!bare.equals(qualified)) {
                            state.root.skinFillOverrides.remove(bare);
                        }
                        restoreOverride(state, root, resolvedTarget);
                        if (refresher != null) refresher.run();
                        UiFactory.persistSkinState(state);
                    }));
        }

        List<ContextAction> built = sections.build();
        List<String> labels = new ArrayList<>();
        for (ContextAction a : built) labels.add(a.label());
        int menuW = ContextMenuRenderer.preferredMenuWidth(labels, 90, 120);
        int menuH = ContextMenuPanel.heightFor(built, built.size());

        int screenW = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int screenH = Minecraft.getInstance().getWindow().getGuiScaledHeight();
        int px = ContextMenuPlacement.fitRightOrLeft(mouseX, screenW, menuW);
        int py = ContextMenuPlacement.fitBelowOrAbove(mouseY, screenH, menuH);

        root.setContextMenu(
                ContextMenuPanel.build(px, py, menuW, built, 0, built.size(), ThemeColors.BORDER_BASE, state, a -> {
                    if (state.root.skinModeEditorOpen) {
                        return;
                    }
                    if (root.isContextMenuOpen()) {
                        buildContextMenu(state, root, refresher, mouseX, mouseY);
                    }
                }),
                px, py, menuW, menuH
        );
    }

    private static void openModeEditorFor(UiState state, RootWidget root, Runnable refresher,
            String targetKey, String asset, int mouseX, int mouseY, SkinFillMode spec, SkinFillOverride currentOverride) {
        boolean editingCurrent = currentOverride != null && spec.mode.equals(currentOverride.mode());
        int left = editingCurrent ? currentOverride.leftEdge() : spec.defaultLeft;
        int right = editingCurrent ? currentOverride.rightEdge() : spec.defaultRight;
        int top = editingCurrent ? currentOverride.topEdge() : spec.defaultTop;
        int bottom = editingCurrent ? currentOverride.bottomEdge() : spec.defaultBottom;
        switch (spec) {
            case DYNAMIC -> openDynamicModeEditor(state, root, refresher, targetKey, asset, mouseX, mouseY, left, right, top, bottom);
            case TILE, HRSTRETCH -> openModeEditor(state, root, refresher, targetKey, asset, mouseX, mouseY,
                    spec.mode, spec.titleKey, spec.leftLabelKey, spec.rightLabelKey, left, right);
            default -> setFillMode(state, targetKey, spec.mode, asset, 0, 0, root, refresher);
        }
    }

    public static void setFillModeFromAssetPicker(UiState state, String targetKey, String asset, Runnable refresher) {
        SkinFillOverride prior = SkinFillOverride.parse(SkinOverrideKey.resolveOverride(state, targetKey));
        String mode = prior != null ? prior.mode() : "stretch";
        int left = prior != null ? prior.leftEdge() : 0;
        int right = prior != null ? prior.rightEdge() : 0;
        setFillMode(state, targetKey, mode, asset, left, right, 0, 0, null, refresher);
    }

    private static void setFillMode(UiState state, String targetKey, String mode, String asset, int leftEdge, int rightEdge, WidgetGroup root, Runnable refresher) {
        setFillMode(state, targetKey, mode, asset, leftEdge, rightEdge, 0, 0, root, refresher);
    }

    private static void setFillMode(UiState state, String targetKey, String mode, String asset, int leftEdge, int rightEdge, int topEdge, int bottomEdge, WidgetGroup root, Runnable refresher) {
        if (asset == null || asset.isBlank()) {
            QuestsAndStuffMod.debugLog("[QnS:Skin] setFillMode ABORTED: asset is blank, target={}, mode={}", targetKey, mode);
            return;
        }
        String entryKey = SkinOverrideKey.overrideKey(state, targetKey);
        SkinFillOverride override = new SkinFillOverride(mode, leftEdge, rightEdge, topEdge, bottomEdge, asset);
        String encoded = override.encode();
        state.root.skinFillOverrides.put(entryKey, encoded);
        SkinFillOverride.clearCache();
        QuestsAndStuffMod.debugLog("[QnS:Skin] setFillMode: target={}, mode={}, asset={}, entryKey={}, encoded={}", targetKey, mode, asset, entryKey, encoded);
        reapplyOverrides(state, root);
        if (refresher != null) refresher.run();
        UiFactory.persistSkinState(state);
    }

    private static void openModeEditor(UiState state, RootWidget root, Runnable refresher, String targetKey, String asset, int mouseX, int mouseY,
            String mode, String titleKey, String leftLabelKey, String rightLabelKey, int left, int right) {
        int w = 240;
        int h = ContextMenuRenderer.CONTEXT_MENU_BODY_H;
        Rectangle bounds = modeEditorBounds(mouseX, mouseY, w, h);
        int x = bounds.x;
        int y = bounds.y;
        Runnable cancel = () -> {
            state.root.skinModeEditorOpen = false;
            root.closeContextMenu();
        };
        WidgetGroup popup = TwoFieldEditor.build(state, x, y, w, h, titleKey, leftLabelKey, rightLabelKey, left, right,
                (l, r) -> {
                    QuestsAndStuffMod.debugLog("[QnS:Skin] mode editor apply: target={}, mode={}, asset={}, edges=({},{}), currentMode={}", targetKey, mode, asset, l, r);
                    state.root.skinModeEditorOpen = false;
                    root.closeContextMenu();
                    setFillMode(state, targetKey, mode, asset, l, r, root, refresher);
                },
                cancel);
        state.root.skinModeEditorOpen = true;
        root.setContextMenu(popup, x, y, w, h);
    }

    private static void openDynamicModeEditor(UiState state, RootWidget root, Runnable refresher, String targetKey, String asset, int mouseX, int mouseY, int l, int r, int t, int b) {
        int w = 240;
        int h = ContextMenuRenderer.CONTEXT_MENU_BODY_H;
        Rectangle bounds = modeEditorBounds(mouseX, mouseY, w, h);
        int x = bounds.x;
        int y = bounds.y;
        Runnable cancel = () -> {
            state.root.skinModeEditorOpen = false;
            root.closeContextMenu();
        };
        WidgetGroup popup = FourFieldEditor.build(state, x, y, w, h, "ui.questsandstuff.skin.mode_dynamic",
                "ui.questsandstuff.skin.dynamic_left", "ui.questsandstuff.skin.dynamic_right",
                "ui.questsandstuff.skin.dynamic_top", "ui.questsandstuff.skin.dynamic_bottom", l, r, t, b, (a, c, d, e) -> {
                    state.root.skinModeEditorOpen = false;
                    root.closeContextMenu();
                    setFillMode(state, targetKey, "dynamic", asset, a, c, d, e, root, refresher);
                }, cancel);
        state.root.skinModeEditorOpen = true;
        root.setContextMenu(popup, x, y, w, h);
    }

    private static Rectangle modeEditorBounds(int mouseX, int mouseY, int w, int h) {
        int screenW = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int screenH = Minecraft.getInstance().getWindow().getGuiScaledHeight();
        int x = Math.max(4, Math.min(mouseX, screenW - w - 4));
        int y = Math.max(4, Math.min(mouseY, screenH - h - 4));
        return new Rectangle(x, y, w, h);
    }

    static boolean isMinimapKey(String key) {
        return SkinTargetKeys.MINIMAP_BODY.equals(key) || SkinTargetKeys.MINIMAP_TOGGLE.equals(key);
    }

    static boolean isMinimapHit(UiState state, RootWidget root, int mx, int my) {
        CanvasViewport vp = root.getCanvasViewport();
        int[] body = SkinMinimapRects.bodyRect(state, vp);
        int[] toggle = SkinMinimapRects.toggleRect(state, vp);
        return contains(body, mx, my) || contains(toggle, mx, my);
    }

    static String minimapHitKey(UiState state, RootWidget root, int mx, int my) {
        CanvasViewport vp = root.getCanvasViewport();
        if (contains(SkinMinimapRects.toggleRect(state, vp), mx, my)) return SkinTargetKeys.MINIMAP_TOGGLE;
        if (contains(SkinMinimapRects.bodyRect(state, vp), mx, my)) return SkinTargetKeys.MINIMAP_BODY;
        return null;
    }

    private static boolean contains(int[] rect, int x, int y) {
        if (rect == null || rect[2] <= 0 || rect[3] <= 0) return false;
        return x >= rect[0] && x < rect[0] + rect[2] && y >= rect[1] && y < rect[1] + rect[3];
    }
}
