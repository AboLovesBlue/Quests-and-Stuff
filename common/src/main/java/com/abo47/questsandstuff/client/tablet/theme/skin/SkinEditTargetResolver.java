package com.abo47.questsandstuff.client.tablet.theme.skin;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.gui.GuiGraphics;

import com.lowdragmc.lowdraglib.gui.texture.ColorRectTexture;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import com.abo47.questsandstuff.client.tablet.animation.SourceOriginRevealWidget;
import com.abo47.questsandstuff.client.tablet.controls.InlineRenameField;
import com.abo47.questsandstuff.client.tablet.controls.ScissoredWidgetGroup;
import com.abo47.questsandstuff.client.tablet.home.HomeOverviewPanel;
import com.abo47.questsandstuff.client.tablet.icons.DisplayIconWidget;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasViewport;
import com.abo47.questsandstuff.client.tablet.quest.chapter.ChapterRowRenderer;
import com.abo47.questsandstuff.client.tablet.quest.details.description.QuestDetailsDescriptionCanvas;

public final class SkinEditTargetResolver {
    private static final Map<Class<?>, Boolean> CUSTOM_CHROME_CACHE = new HashMap<>();

    private SkinEditTargetResolver() {
    }

    public static String findTargetKeyAt(WidgetGroup root, int mouseX, int mouseY) {
        return keyForTarget(root, findTargetWidgetAt(root, mouseX, mouseY));
    }

    public static String keyForTarget(WidgetGroup root, Widget hit) {
        if (hit == null) return null;
        if (hit == root) return SkinTargetKeys.ROOT;
        return resolveHit(hit);
    }

    public static Widget findTargetWidgetAt(WidgetGroup root, int mouseX, int mouseY) {
        Widget hit = topTargetIn(SkinAnchorRegistry.findByKey("quest_details_layer"), mouseX, mouseY);
        if (hit == null) hit = topTargetIn(root, mouseX, mouseY);
        if (hit != null) return hit;
        return root.isMouseOverElement(mouseX, mouseY) ? root : null;
    }

    private static Widget topTargetIn(Widget layer, int mouseX, int mouseY) {
        if (!(layer instanceof WidgetGroup group)) return null;
        Widget hit = frontIn(group, mouseX, mouseY, null);
        return hit != null && hit != group ? targetableHit(hit) : null;
    }

    private static Widget targetableHit(Widget hit) {
        return isTargetable(hit) && !isCanvasPanelKey(stableKeyFor(hit)) ? hit : null;
    }

    private static String resolveHit(Widget hit) {
        String key = stableKeyFor(hit);
        if (isCanvasPanelKey(key)) return null;
        String shared = SkinAnchorRegistry.sharedKeyFor(hit);
        if (shared != null && SkinOverrideKey.isCardKey(shared)) return shared;
        return key;
    }

    private static boolean isCanvasPanelKey(String key) {
        return SkinOverrideKey.isViewportBackgroundKey(key);
    }

    private static Widget frontIn(WidgetGroup group, int mouseX, int mouseY, Rectangle clip) {
        clip = visibleClipFor(group, clip);
        if (clip != null && !clip.contains(mouseX, mouseY)) return null;

        Widget blocker = null;
        for (int i = group.widgets.size() - 1; i >= 0; i--) {
            Widget child = group.widgets.get(i);
            if (!child.isVisible()) continue;
            if (!child.isMouseOverElement(mouseX, mouseY)) continue;
            if (child instanceof WidgetGroup childGroup) {
                Widget deeper = frontIn(childGroup, mouseX, mouseY, clip);
                if (deeper != null) return deeper;
            }
            if (isTargetable(child)) return child;
            if (blocker == null && isBlockingContent(child)) blocker = child;
        }

        return blocker;
    }

    private static boolean isBlockingContent(Widget widget) {
        if (widget instanceof CanvasViewport) return false;
        return hasVisibleBackground(widget) || hasCustomChrome(widget);
    }

    private static boolean isClipContainer(Widget widget) {
        return widget instanceof ScissoredWidgetGroup
                || widget instanceof CanvasViewport
                || widget instanceof QuestDetailsDescriptionCanvas;
    }

    private static Rectangle visibleClipFor(Widget widget, Rectangle parentClip) {
        if (!isClipContainer(widget)) return parentClip;
        Rectangle bounds = new Rectangle(widget.getPositionX(), widget.getPositionY(),
                widget.getSizeWidth(), widget.getSizeHeight());
        return parentClip == null ? bounds : parentClip.intersection(bounds);
    }

    public static Widget widgetForKey(WidgetGroup root, String targetKey) {
        if (targetKey == null || targetKey.isBlank()) return null;
        Widget registered = SkinAnchorRegistry.findByKey(targetKey);
        if (registered != null) return registered;
        for (Widget w : SkinAnchorRegistry.sharedWidgetsFor(targetKey)) {
            return w;
        }
        for (Widget w : root.widgets) {
            Widget found = searchForKey(w, targetKey);
            if (found != null) return found;
        }
        return null;
    }

    private static Widget searchForKey(Widget widget, String targetKey) {
        String key = stableKeyFor(widget);
        if (key != null && key.equals(targetKey)) return widget;
        if (widget instanceof WidgetGroup group) {
            for (Widget child : group.widgets) {
                Widget found = searchForKey(child, targetKey);
                if (found != null) return found;
            }
        }
        return null;
    }

    public static String stableKeyFor(Widget widget) {
        String registered = SkinAnchorRegistry.keyFor(widget);
        if (registered != null) return registered;
        return buildPathKey(widget);
    }

    private static String buildPathKey(Widget widget) {
        List<Widget> path = new ArrayList<>();
        Widget cur = widget;
        while (cur != null) {
            path.add(cur);
            cur = cur.getParent();
        }

        StringBuilder sb = new StringBuilder();
        for (int i = path.size() - 1; i >= 0; i--) {
            if (sb.length() > 0) sb.append("/");
            Widget w = path.get(i);
            String name = w.getClass().getSimpleName();
            if (name.isEmpty()) name = w.getClass().getName().replace('.', '_');

            WidgetGroup parent = w.getParent();
            if (parent != null) {
                int count = 0;
                int idx = 0;
                for (Widget sibling : parent.widgets) {
                    if (sibling.getClass() == w.getClass()) {
                        if (sibling == w) idx = count;
                        count++;
                    }
                }
                if (count > 1) {
                    name += "[" + idx + "]";
                }
            }
            sb.append(name);
        }
        return sb.toString();
    }

    public static boolean isTargetable(Widget widget) {
        if (isSkinExcluded(widget)) return false;
        if (hasVisibleBackground(widget)) return true;
        return SkinAnchorRegistry.keyFor(widget) != null;
    }

    private static boolean isSkinExcluded(Widget widget) {
        Widget cur = widget;
        while (cur != null) {
            if (cur instanceof CanvasViewport) return true;
            cur = cur.getParent();
        }
        return widget instanceof SourceOriginRevealWidget
                || widget instanceof ImageWidget
                || widget instanceof DisplayIconWidget
                || widget instanceof QuestDetailsDescriptionCanvas
                || widget instanceof HomeOverviewPanel
                || widget instanceof ChapterRowRenderer.CollapsedChapterTileWidget
                || widget instanceof ChapterRowRenderer.ChapterCompletionNoticeWidget
                || widget instanceof InlineRenameField
                || isInsideChapterCardArea(widget);
    }

    private static boolean isInsideChapterCardArea(Widget widget) {
        Widget cur = widget.getParent();
        while (cur != null) {
            if (cur instanceof ScissoredWidgetGroup) return true;
            cur = cur.getParent();
        }
        return false;
    }

    private static boolean hasVisibleBackground(Widget widget) {
        IGuiTexture bg = widget.getBackgroundTexture();
        if (bg == null) return false;
        if (bg.equals(IGuiTexture.EMPTY)) return false;
        if (bg instanceof ColorRectTexture crt && (crt.color >>> 24) == 0) return false;
        return true;
    }

    public static boolean hasCustomChrome(Widget widget) {
        Class<?> cls = widget.getClass();
        return CUSTOM_CHROME_CACHE.computeIfAbsent(cls, k -> {
            try {
                if (k.getMethod("drawInBackground", GuiGraphics.class, int.class, int.class, float.class)
                        .getDeclaringClass() != WidgetGroup.class) {
                    return true;
                }
            } catch (NoSuchMethodException e) {
            }
            try {
                if (k.getMethod("drawInForeground", GuiGraphics.class, int.class, int.class, float.class)
                        .getDeclaringClass() != WidgetGroup.class) {
                    return true;
                }
            } catch (NoSuchMethodException e) {
            }
            return false;
        });
    }

    public static List<Rectangle> ancestorBounds(Widget widget, WidgetGroup stopAt) {
        List<Rectangle> rects = new ArrayList<>();
        Widget cur = widget.getParent();
        while (cur != null && cur != stopAt) {
            rects.add(new Rectangle(cur.getPositionX(), cur.getPositionY(),
                    cur.getSizeWidth(), cur.getSizeHeight()));
            cur = cur.getParent();
        }
        rects.add(new Rectangle(stopAt.getPositionX(), stopAt.getPositionY(),
                stopAt.getSizeWidth(), stopAt.getSizeHeight()));
        return rects;
    }

    /**
     * Find bounds of all visible content nested inside {@code widget} (not including widget itself).
     * Recursion stops as soon as visible content is found along a branch, since visible content's
     * own bounds already cover whatever is nested further inside it. Scissored layers stop the
     * search at their own bounds and clip nested holes to the visible region.
     */
    public static List<Rectangle> nestedTargetBounds(Widget widget, WidgetGroup root) {
        List<Rectangle> out = new ArrayList<>();
        if (widget instanceof WidgetGroup group) {
            for (Widget child : group.widgets) {
                collectNestedTargets(child, out, null);
            }
        }
        return out;
    }

    private static void collectNestedTargets(Widget widget, List<Rectangle> out, Rectangle clip) {
        if (!widget.isVisible()) return;
        Rectangle area = visibleArea(widget, clip);
        if (area.isEmpty()) return;
        if (isTargetable(widget)) {
            out.add(area);
            return;
        }
        if (isClipContainer(widget)) {
            out.add(area);
            return;
        }
        if (widget instanceof WidgetGroup group) {
            int before = out.size();
            Rectangle childClip = visibleClipFor(widget, clip);
            for (Widget child : group.widgets) {
                collectNestedTargets(child, out, childClip);
            }
            if (out.size() == before && (hasVisibleBackground(widget) || hasCustomChrome(widget))) {
                out.add(area);
            }
        } else if (hasVisibleBackground(widget) || hasCustomChrome(widget)) {
            out.add(area);
        }
    }

    private static Rectangle visibleArea(Widget widget, Rectangle clip) {
        Rectangle own = new Rectangle(widget.getPositionX(), widget.getPositionY(),
                widget.getSizeWidth(), widget.getSizeHeight());
        if (clip == null) return own;
        return clip.intersection(own);
    }
}
