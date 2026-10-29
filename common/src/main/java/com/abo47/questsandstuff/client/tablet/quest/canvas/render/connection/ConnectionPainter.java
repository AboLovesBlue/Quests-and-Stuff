package com.abo47.questsandstuff.client.tablet.quest.canvas.render.connection;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import com.lowdragmc.lowdraglib.gui.util.DrawerHelper;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasViewport;
import com.abo47.questsandstuff.client.tablet.assets.AssetLibrary;
import com.abo47.questsandstuff.client.tablet.quest.canvas.geometry.CanvasGeometry;
import com.abo47.questsandstuff.client.tablet.quest.canvas.interaction.CanvasInteractionGate;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.CanvasPoint;
import com.abo47.questsandstuff.client.tablet.state.UiState;
import com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory;
import com.abo47.questsandstuff.client.tablet.theme.tokens.ThemeColors;

import static com.abo47.questsandstuff.client.tablet.layout.PanelChrome.drawRectOutline;
import static com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory.withAlpha;
import static com.mojang.blaze3d.vertex.DefaultVertexFormat.POSITION_TEX_COLOR;

final class ConnectionPainter {
    private static final ResourceLocation CONNECTION_CHEVRON = ResourceLocation.tryBuild("questsandstuff", "textures/gui/chevron.png");
    private static final float CHEVRON_U0 = 50.0f / 256.0f;
    private static final float CHEVRON_U1 = 206.0f / 256.0f;
    private static final int CHEVRON_BASE_W = 5;
    private static final int CHEVRON_BASE_H = 9;
    private static final int ITEM_GLYPH_BASE = 12;
    private static final float DARKEN_FACTOR = 0.52f;
    private static final float SCALE_CLAMP_MIN = 0.25f;
    private static final float SCALE_CLAMP_MAX = 2.0f;
    private static final int PENDING_FILL_ALPHA = 72;
    private static final int PENDING_OUTLINE_ALPHA = 220;
    private static final float ANIMATION_ALPHA_BASE = 0.58f;
    private static final float ANIMATION_ALPHA_PROGRESS = 0.42f;
    private static final int DEFAULT_SPACING = 5;

    private ConnectionPainter() {
    }

    static void renderConnectionLines(WidgetGroup canvasViewport, UiState state, List<ConnectionLine> lines) {
        if (lines.isEmpty()) {
            return;
        }
        canvasViewport.addWidget(new WidgetGroup(0, 0, canvasViewport.getSizeWidth(), canvasViewport.getSizeHeight()) {
            @Override
            public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
                int originX = getPositionX();
                int originY = getPositionY();
                int clipMinX = originX - state.canvas.canvasLivePanX;
                int clipMinY = originY - state.canvas.canvasLivePanY;
                int clipMaxX = clipMinX + getSizeWidth();
                int clipMaxY = clipMinY + getSizeHeight();
                long now = System.currentTimeMillis();
                for (ConnectionLine line : lines) {
                    drawConnection(graphics, originX, originY, state, this, line, mouseX, mouseY, now, clipMinX, clipMinY, clipMaxX, clipMaxY);
                }
            }
        });
    }

    static List<CanvasPoint> connectionPath(UiState state, int originX, int originY, int sourceX, int sourceY, int targetX, int targetY, boolean direct) {
        if (direct) {
            return List.of(new CanvasPoint(sourceX, sourceY), new CanvasPoint(targetX, targetY));
        }
        int cell = Math.max(1, state.canvas.gridCellPx);
        int localSourceX = sourceX - originX;
        int localTargetX = targetX - originX;
        int midLocalX = snapScreenLocalToGrid(state, (localSourceX + localTargetX) / 2, cell);
        int midX = originX + midLocalX;
        if (Math.abs(midX - sourceX) < cell / 2) {
            midX += targetX >= sourceX ? cell : -cell;
        }
        return List.of(
                new CanvasPoint(sourceX, sourceY),
                new CanvasPoint(midX, sourceY),
                new CanvasPoint(midX, targetY),
                new CanvasPoint(targetX, targetY)
        );
    }

    static void drawStaticChevrons(GuiGraphics graphics, List<CanvasPoint> path, int color, int alpha, int clipMinX, int clipMinY, int clipMaxX, int clipMaxY) {
        drawTexturedChevrons(graphics, path, new ChevronDraw(color, alpha, 1.0f, null, 0, CHEVRON_BASE_W, CHEVRON_BASE_H), clipMinX, clipMinY, clipMaxX, clipMaxY);
    }

    static void drawStaticChevrons(GuiGraphics graphics, List<CanvasPoint> path, int color, int alpha, float scale, int clipMinX, int clipMinY, int clipMaxX, int clipMaxY) {
        float safeScale = clampScale(scale);
        int gW = scaledGlyphDim(CHEVRON_BASE_W, safeScale);
        int gH = scaledGlyphDim(CHEVRON_BASE_H, safeScale);
        drawTexturedChevrons(graphics, path, new ChevronDraw(color, alpha, 1.0f, null, gW, gW, gH), clipMinX, clipMinY, clipMaxX, clipMaxY);
    }

    static void drawTexturedChevrons(GuiGraphics graphics, List<CanvasPoint> path, int color, int alpha, float scale, String textureStr, int clipMinX, int clipMinY, int clipMaxX, int clipMaxY) {
        ResourceLocation tex = ConnectionTextureResolver.resolveTexture(textureStr);
        float safeScale = clampScale(scale);
        int[] glyph = glyphSize(tex, textureStr, safeScale);
        drawTexturedChevrons(graphics, path, new ChevronDraw(color, alpha, 1.0f, tex, glyph[0], glyph[0], glyph[1]), clipMinX, clipMinY, clipMaxX, clipMaxY);
    }

    private static void drawConnection(
            GuiGraphics graphics,
            int originX,
            int originY,
            UiState state,
            Widget view,
            ConnectionLine line,
            int mouseX,
            int mouseY,
            long now,
            int clipMinX,
            int clipMinY,
            int clipMaxX,
            int clipMaxY
    ) {
        int sourceOffsetX = selectionDragOffsetX(state, line.sourceQuestId());
        int sourceOffsetY = selectionDragOffsetY(state, line.sourceQuestId());
        int targetOffsetX = selectionDragOffsetX(state, line.targetQuestId());
        int targetOffsetY = selectionDragOffsetY(state, line.targetQuestId());
        int startX = originX + line.startX() + sourceOffsetX;
        int startY = originY + line.startY() + sourceOffsetY;
        int endX = originX + line.endX() + targetOffsetX;
        int endY = originY + line.endY() + targetOffsetY;

        if (line.pending()) {
            int halfSize = CHEVRON_BASE_W;
            SurfaceFactory.fill(withAlpha(ThemeColors.SUCCESS, PENDING_FILL_ALPHA)).draw(graphics, 0, 0, startX - halfSize, startY - halfSize, halfSize * 2 + 1, halfSize * 2 + 1);
            drawRectOutline(graphics, startX - halfSize, startY - halfSize, halfSize * 2 + 1, halfSize * 2 + 1, withAlpha(ThemeColors.SUCCESS, PENDING_OUTLINE_ALPHA));
            return;
        }

        boolean hoveringEndpoint = isHoveringEndpoint(
                originX,
                originY,
                line,
                mouseX,
                mouseY,
                sourceOffsetX,
                sourceOffsetY,
                targetOffsetX,
                targetOffsetY
        ) && CanvasInteractionGate.hoverAllowed(state, view, mouseX, mouseY);
        if (line.hidden() && !state.root.canEdit && !hoveringEndpoint) {
            return;
        }
        int alpha = line.hidden() && hoveringEndpoint ? ConnectionRenderStyle.VISIBLE_ALPHA : line.alpha();
        List<CanvasPoint> path = connectionPath(state, originX, originY, startX, startY, endX, endY, line.direct());
        String rawTextureStr = line.texture();
        ResourceLocation texture = ConnectionTextureResolver.resolveTexture(rawTextureStr);
        int spacing = line.textureSpacing();
        if (spacing == 0 && texture != null) {
            int tw = ConnectionTextureResolver.textureWidth(rawTextureStr);
            if (tw > 0) spacing = Math.max(tw, DEFAULT_SPACING);
        }
        float zoom = state.canvas.canvasZoom;
        float safeScale = clampScale(zoom);
        int[] glyph = glyphSize(texture, rawTextureStr, safeScale);
        ConnectionAnimation.AnimationState animation = ConnectionAnimation.current(state, line.connectionId(), now);
        ItemStack gameStack = AssetLibrary.gameItemStack(rawTextureStr);
        if (gameStack != null && !gameStack.isEmpty()) {
            int itemGlyph = scaledGlyphDim(ITEM_GLYPH_BASE, safeScale);
            float progress = animation.running() ? animation.progress() : 1.0f;
            int itemAlpha = animation.running()
                    ? Math.min(255, Math.round(alpha * (ANIMATION_ALPHA_BASE + ANIMATION_ALPHA_PROGRESS * animation.progress())))
                    : alpha;
            drawItemChevrons(graphics, path, gameStack, itemAlpha, progress, spacing, itemGlyph, itemGlyph, clipMinX, clipMinY, clipMaxX, clipMaxY);
            return;
        }
        if (animation.running()) {
            int animatedAlpha = Math.min(255, Math.round(alpha * (ANIMATION_ALPHA_BASE + ANIMATION_ALPHA_PROGRESS * animation.progress())));
            drawTexturedChevrons(graphics, path, new ChevronDraw(line.color(), animatedAlpha, animation.progress(), texture, spacing, glyph[0], glyph[1]), clipMinX, clipMinY, clipMaxX, clipMaxY);
            return;
        }
        drawTexturedChevrons(graphics, path, new ChevronDraw(line.color(), alpha, 1.0f, texture, spacing, glyph[0], glyph[1]), clipMinX, clipMinY, clipMaxX, clipMaxY);
    }

    private static boolean isHoveringEndpoint(
            int originX,
            int originY,
            ConnectionLine line,
            int mouseX,
            int mouseY,
            int sourceOffsetX,
            int sourceOffsetY,
            int targetOffsetX,
            int targetOffsetY
    ) {
        return inside(mouseX, mouseY, originX + line.sourceX() + sourceOffsetX, originY + line.sourceY() + sourceOffsetY, line.sourceW(), line.sourceH())
                || inside(mouseX, mouseY, originX + line.targetX() + targetOffsetX, originY + line.targetY() + targetOffsetY, line.targetW(), line.targetH());
    }

    private static boolean inside(int mouseX, int mouseY, int x, int y, int w, int h) {
        return mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
    }

    private static boolean inSelection(UiState state, String elementId) {
        return state.canvas.canvasSelection.questIds().contains(elementId)
                || state.canvas.canvasSelection.exclusiveChoiceIds().contains(elementId);
    }

    private static int selectionDragOffsetX(UiState state, String elementId) {
        if (!state.canvas.draggingSelection || elementId == null || elementId.isBlank() || !inSelection(state, elementId)) {
            return 0;
        }
        return CanvasGeometry.dragDelta(state, state.canvas.dragStartBoundsLeft, state.canvas.dragSelectionDeltaX);
    }

    private static int selectionDragOffsetY(UiState state, String elementId) {
        if (!state.canvas.draggingSelection || elementId == null || elementId.isBlank() || !inSelection(state, elementId)) {
            return 0;
        }
        return CanvasGeometry.dragDelta(state, state.canvas.dragStartBoundsTop, state.canvas.dragSelectionDeltaY);
    }

    private static int snapScreenLocalToGrid(UiState state, int localX, int cell) {
        int relative = localX - state.canvas.canvasContentX - state.canvas.canvasOffsetX;
        int snapped = Math.round((float) relative / (float) cell) * cell;
        return state.canvas.canvasContentX + state.canvas.canvasOffsetX + snapped;
    }

    private static int[] glyphSize(ResourceLocation tex, String textureStr, float safeScale) {
        int[] dims = tex != null ? ConnectionTextureResolver.textureDims(textureStr) : null;
        if (dims != null && dims[0] > 0 && dims[1] > 0) {
            double baseArea = CHEVRON_BASE_W * CHEVRON_BASE_H;
            double texAspect = (double) dims[0] / (double) dims[1];
            double aW = Math.sqrt(baseArea * texAspect);
            double aH = baseArea / aW;
            return new int[]{scaledGlyphDim((int) Math.round(aW), safeScale), scaledGlyphDim((int) Math.round(aH), safeScale)};
        }
        return new int[]{scaledGlyphDim(CHEVRON_BASE_W, safeScale), scaledGlyphDim(CHEVRON_BASE_H, safeScale)};
    }

    private static void drawTexturedChevrons(
            GuiGraphics graphics,
            List<CanvasPoint> path,
            ChevronDraw draw,
            int clipMinX,
            int clipMinY,
            int clipMaxX,
            int clipMaxY
    ) {
        int glyphW = draw.glyphW();
        int glyphH = draw.glyphH();
        double spacing = chevronStride(draw.customSpacing(), glyphW);
        boolean customTex = draw.texture() != null;
        double totalLength = pathLength(path);
        if (totalLength < glyphW) {
            return;
        }
        double visibleLength = Math.max(glyphW / 2.0, totalLength * Math.max(0.0f, Math.min(1.0f, draw.progress())));
        List<ChevronGlyph> glyphs = chevronGlyphs(path, draw.color(), draw.alpha(), visibleLength, glyphW, glyphH, spacing, clipMinX, clipMinY, clipMaxX, clipMaxY);
        if (glyphs.isEmpty()) {
            return;
        }
        ResourceLocation tex = customTex ? draw.texture() : CONNECTION_CHEVRON;
        float u0 = customTex ? 0.0f : CHEVRON_U0;
        float u1 = customTex ? 1.0f : CHEVRON_U1;
        setChevronTextureFilter(tex, GL11.GL_LINEAR);
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, tex);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder buffer = tessellator.getBuilder();
        Matrix4f matrix = graphics.pose().last().pose();
        buffer.begin(VertexFormat.Mode.QUADS, POSITION_TEX_COLOR);
        for (ChevronGlyph glyph : glyphs) {
            boolean flipU = customTex && glyph.dirX() < 0;
            emitChevronQuad(buffer, matrix, glyph, glyphW, glyphH, flipU ? u1 : u0, flipU ? u0 : u1);
        }
        tessellator.end();
        RenderSystem.disableBlend();
        setChevronTextureFilter(tex, GL11.GL_NEAREST);
    }

    private static double chevronStride(int customSpacing, int glyphW) {
        if (customSpacing == 0) {
            return Math.max(glyphW, DEFAULT_SPACING);
        }
        if (customSpacing > 0) {
            return Math.max(glyphW, customSpacing);
        }
        return Math.max(1, glyphW * (100 + customSpacing) / 100.0);
    }

    private static void drawItemChevrons(
            GuiGraphics graphics,
            List<CanvasPoint> path,
            ItemStack stack,
            int alpha,
            float progress,
            int customSpacing,
            int glyphW,
            int glyphH,
            int clipMinX,
            int clipMinY,
            int clipMaxX,
            int clipMaxY
    ) {
        double spacing = chevronStride(customSpacing, glyphW);
        double totalLength = pathLength(path);
        if (totalLength < glyphW) {
            return;
        }
        double visibleLength = Math.max(glyphW / 2.0, totalLength * Math.max(0.0f, Math.min(1.0f, progress)));
        List<ChevronGlyph> glyphs = chevronGlyphs(path, 0xFFFFFF, alpha, visibleLength, glyphW, glyphH, spacing, clipMinX, clipMinY, clipMaxX, clipMaxY);
        if (glyphs.isEmpty()) {
            return;
        }
        int size = Math.max(1, Math.min(glyphW, glyphH));
        for (ChevronGlyph glyph : glyphs) {
            graphics.pose().pushPose();
            graphics.pose().translate((float) (glyph.x() - size / 2.0), (float) (glyph.y() - size / 2.0), 0.0f);
            graphics.pose().scale(size / 16.0f, size / 16.0f, 1.0f);
            DrawerHelper.drawItemStack(graphics, stack, 0, 0, -1, null);
            graphics.pose().popPose();
        }
        graphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
    }

    private static double pathLength(List<CanvasPoint> path) {
        double total = 0.0;
        for (int i = 0; i + 1 < path.size(); i++) {
            CanvasPoint a = path.get(i);
            CanvasPoint b = path.get(i + 1);
            double dx = b.x - a.x;
            double dy = b.y - a.y;
            total += Math.sqrt(dx * dx + dy * dy);
        }
        return total;
    }

    private static List<ChevronGlyph> chevronGlyphs(
            List<CanvasPoint> path,
            int color,
            int alpha,
            double visibleLength,
            int glyphW,
            int glyphH,
            double spacing,
            int clipMinX,
            int clipMinY,
            int clipMaxX,
            int clipMaxY
    ) {
        int safeAlpha = Math.max(0, Math.min(255, alpha));
        int lightColor = withAlpha(color, safeAlpha);
        int darkColor = withAlpha(darkenColor(color, DARKEN_FACTOR), safeAlpha);
        int pad = Math.max(glyphW, glyphH) + 2;
        double startDistance = glyphW / 2.0;
        double walked = 0.0D;
        List<ChevronGlyph> glyphs = new ArrayList<>();
        for (int i = 0; i + 1 < path.size(); i++) {
            CanvasPoint a = path.get(i);
            CanvasPoint b = path.get(i + 1);
            double dx = b.x - a.x;
            double dy = b.y - a.y;
            double length = Math.sqrt(dx * dx + dy * dy);
            if (length <= 0.0) {
                continue;
            }
            double segmentEnd = walked + length;
            if (segmentEnd <= startDistance || walked >= visibleLength) {
                walked = segmentEnd;
                continue;
            }
            double[] range = clippedSegmentRange(
                    a.x,
                    a.y,
                    b.x,
                    b.y,
                    clipMinX - pad,
                    clipMinY - pad,
                    clipMaxX + pad,
                    clipMaxY + pad
            );
            if (range != null) {
                double rangeStart = Math.max(startDistance, walked + range[0] * length);
                double rangeEnd = Math.min(visibleLength, walked + range[1] * length);
                int firstIndex = (int) Math.ceil((rangeStart - startDistance) / spacing);
                for (double distance = startDistance + firstIndex * spacing; distance < rangeEnd; distance += spacing, firstIndex++) {
                    double segmentDistance = distance - walked;
                    double dirX = dx / length;
                    double dirY = dy / length;
                    int chevronColor = (firstIndex % 2 == 0) ? lightColor : darkColor;
                    glyphs.add(new ChevronGlyph(a.x + dirX * segmentDistance, a.y + dirY * segmentDistance, dirX, dirY, chevronColor));
                }
            }
            walked = segmentEnd;
        }
        return glyphs;
    }

    private static double[] clippedSegmentRange(double x0, double y0, double x1, double y1, double minX, double minY, double maxX, double maxY) {
        double[] range = {0.0D, 1.0D};
        double dx = x1 - x0;
        double dy = y1 - y0;
        if (!clipTest(-dx, x0 - minX, range)) {
            return null;
        }
        if (!clipTest(dx, maxX - x0, range)) {
            return null;
        }
        if (!clipTest(-dy, y0 - minY, range)) {
            return null;
        }
        if (!clipTest(dy, maxY - y0, range)) {
            return null;
        }
        return range;
    }

    private static boolean clipTest(double p, double q, double[] range) {
        if (p == 0.0D) {
            return q >= 0.0D;
        }
        double r = q / p;
        if (p < 0.0D) {
            if (r > range[1]) {
                return false;
            }
            if (r > range[0]) {
                range[0] = r;
            }
            return true;
        }
        if (r < range[0]) {
            return false;
        }
        if (r < range[1]) {
            range[1] = r;
        }
        return true;
    }

    private static void setChevronTextureFilter(ResourceLocation texture, int filter) {
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, filter);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, filter);
    }

    private static float clampScale(float scale) {
        return Math.max(SCALE_CLAMP_MIN, Math.min(SCALE_CLAMP_MAX, scale));
    }

    private static int scaledGlyphDim(int baseDim, float scale) {
        return Math.max(1, (int) Math.round(baseDim * scale));
    }

    private static int darkenColor(int color, float factor) {
        int alpha = color & 0xFF000000;
        int r = Math.max(0, Math.min(255, Math.round(((color >> 16) & 0xFF) * factor)));
        int g = Math.max(0, Math.min(255, Math.round(((color >> 8) & 0xFF) * factor)));
        int b = Math.max(0, Math.min(255, Math.round((color & 0xFF) * factor)));
        return alpha | (r << 16) | (g << 8) | b;
    }

    private static void emitChevronQuad(BufferBuilder buffer, Matrix4f matrix, ChevronGlyph glyph, int glyphW, int glyphH, float u0, float u1) {
        float halfW = glyphW / 2.0f;
        float halfH = glyphH / 2.0f;
        emitChevronVertex(buffer, matrix, glyph, -halfW, halfH, u0, 1.0f);
        emitChevronVertex(buffer, matrix, glyph, halfW, halfH, u1, 1.0f);
        emitChevronVertex(buffer, matrix, glyph, halfW, -halfH, u1, 0.0f);
        emitChevronVertex(buffer, matrix, glyph, -halfW, -halfH, u0, 0.0f);
    }

    private static void emitChevronVertex(BufferBuilder buffer, Matrix4f matrix, ChevronGlyph glyph, float localX, float localY, float u, float v) {
        float x = (float) (glyph.x() + glyph.dirX() * localX - glyph.dirY() * localY);
        float y = (float) (glyph.y() + glyph.dirY() * localX + glyph.dirX() * localY);
        buffer.vertex(matrix, x, y, 0.0f).uv(u, v).color(glyph.color()).endVertex();
    }

    private record ChevronGlyph(double x, double y, double dirX, double dirY, int color) {
    }

    private record ChevronDraw(int color, int alpha, float progress, ResourceLocation texture, int customSpacing, int glyphW, int glyphH) {
    }
}
