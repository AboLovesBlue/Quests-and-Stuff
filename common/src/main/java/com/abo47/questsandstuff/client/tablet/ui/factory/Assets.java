package com.abo47.questsandstuff.client.tablet.ui.factory;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import com.mojang.blaze3d.platform.NativeImage;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.sync.state.ClientQuestStateFacade;
import com.abo47.questsandstuff.client.tablet.assets.AssetLibrary;
import com.abo47.questsandstuff.client.tablet.entity.EntityPreviewRenderer;
import com.abo47.questsandstuff.client.tablet.icons.DisplayIconProvider;
import com.abo47.questsandstuff.client.tablet.icons.IconAtlas;
import com.abo47.questsandstuff.client.tablet.icons.IconRegistry;
import com.abo47.questsandstuff.client.tablet.modal.recipe.RecipePickerChoiceIndex;
import com.abo47.questsandstuff.client.tablet.quest.canvas.recipe.CanvasRecipeCardRecipes;
import com.abo47.questsandstuff.client.tablet.theme.BackgroundModes;
import com.abo47.questsandstuff.client.tablet.theme.tokens.ThemeColors;
import com.abo47.questsandstuff.client.tablet.ui.widget.Layout;

import static com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory.withAlpha;

final class Assets {
    private Assets() {
    }

    static int chapterBackgroundFill(String background, int fallback) {
        String stripped = BackgroundModes.stripMode(background);
        return switch (stripped == null ? "" : stripped) {
            case "hexagons" -> withAlpha(ThemeColors.INTERACTIVE, 86);
            case "octagons" -> withAlpha(ThemeColors.WARNING, 86);
            case "circles" -> withAlpha(ThemeColors.SUCCESS, 86);
            case "diamonds" -> withAlpha(ThemeColors.TEXT_SECONDARY, 86);
            case "gears" -> withAlpha(ThemeColors.BORDER_ACCENT, 92);
            case "hearts" -> withAlpha(ThemeColors.ERROR, 74);
            case "pentagons" -> withAlpha(ThemeColors.INTERACTIVE, 108);
            case "rounded_squares" -> withAlpha(ThemeColors.SURFACE_PANEL_ALT, 120);
            default -> {
                if (stripped != null && !stripped.isBlank() && !"default".equals(stripped) && chapterBackgroundTexture(stripped) != null) {
                    yield withAlpha(ThemeColors.SURFACE_PANEL_ALT, 90);
                }
                yield fallback;
            }
        };
    }

    static IGuiTexture chapterBackgroundTexture(String background) {
        return AssetLibrary.chapterBackgroundTexture(Layout.ASSETS_ROOT_DIR, background);
    }

    static IGuiTexture chapterBackgroundTexture(String background, boolean grayscale) {
        return AssetLibrary.chapterBackgroundTexture(Layout.ASSETS_ROOT_DIR, background, grayscale);
    }

    static ItemStackTexture iconTexture(String iconId) {
        return DisplayIconProvider.iconTexture(iconId);
    }

    static List<AssetLibrary.AssetEntry> listAssetEntries(String relativeDir) {
        return AssetLibrary.listAssetEntries(Layout.ASSETS_ROOT_DIR, relativeDir);
    }

    static List<AssetLibrary.AssetEntry> searchAssetEntries(String relativeDir, String query) {
        return AssetLibrary.searchAssetEntries(Layout.ASSETS_ROOT_DIR, relativeDir, query);
    }

    static AssetLibrary.AssetDimensions assetDimensions(String relativePath) {
        return AssetLibrary.assetDimensions(Layout.ASSETS_ROOT_DIR, relativePath);
    }

    static IGuiTexture assetThumbnailTexture(String relativePath) {
        return AssetLibrary.assetThumbnailTexture(Layout.ASSETS_ROOT_DIR, relativePath);
    }

    static void ensureAssetsDirs() {
        AssetLibrary.ensureAssetsDirs(Layout.ASSETS_ROOT_DIR);
    }

    static void deleteAssetFile(String relativePath) {
        AssetLibrary.deleteAssetFile(Layout.ASSETS_ROOT_DIR, relativePath);
    }

    static void renameAssetFile(String relativePath, String targetNameRaw) {
        AssetLibrary.renameAssetFile(Layout.ASSETS_ROOT_DIR, relativePath, targetNameRaw);
    }

    static void prewarmClientUiAssets() {
        UiPerfProfiler.profile("ui.prewarm.assetsDirs", Assets::ensureAssetsDirs);
        UiPerfProfiler.profile("ui.prewarm.icons", () -> IconAtlas.prewarm(IconRegistry.preloadKeys()));
        UiPerfProfiler.profile("ui.prewarm.chapterBackgrounds", () -> {
            Set<String> backgrounds = new HashSet<>();
            for (String chapter : ClientQuestStateFacade.chapterOrder()) {
                String background = ClientQuestStateFacade.chapterBackground(chapter);
                if (background == null || background.isBlank() || "default".equals(background)) {
                    continue;
                }
                backgrounds.add(background);
            }
            for (String background : backgrounds) {
                chapterBackgroundTexture(background);
            }
        });
        UiPerfProfiler.profile("ui.prewarm.assetThumbnails", Assets::prewarmAssetThumbnails);
        UiPerfProfiler.profile("ui.prewarm.modLogo", Assets::prewarmModLogo);
        UiPerfProfiler.profile("ui.prewarm.fluidEntries", () -> DisplayIconProvider.prewarmFluidEntries());
        UiPerfProfiler.profile("ui.prewarm.entityPreviews", () -> EntityPreviewRenderer.prewarmEntityCache());
        UiPerfProfiler.profile("ui.prewarm.recipeIndex", () -> RecipePickerChoiceIndex.prewarm());
        UiPerfProfiler.profile("ui.prewarm.recipeCards", () -> CanvasRecipeCardRecipes.prewarm());
    }

    private static void prewarmAssetThumbnails() {
        Path root = Layout.ASSETS_ROOT_DIR;
        if (!Files.exists(root)) {
            return;
        }
        try (Stream<Path> walk = Files.walk(root)) {
            walk.filter(Files::isRegularFile).forEach(path -> {
                Path relative = root.relativize(path);
                String rel = relative.toString().replace('\\', '/');
                if (AssetLibrary.assetKind(rel).hasImageThumbnail()) {
                    assetThumbnailTexture(rel);
                }
            });
        } catch (Exception e) {
            QuestsAndStuffMod.debugLog("[QnS:UI] failed to prewarm asset thumbnails");
        }
    }

    private static void prewarmModLogo() {
        try (InputStream is = Assets.class.getClassLoader().getResourceAsStream("questsandstuff.png")) {
            if (is != null) {
                NativeImage image = NativeImage.read(is);
                DynamicTexture texture = new DynamicTexture(image);
                ResourceLocation id = new ResourceLocation("questsandstuff", "textures/gui/questsandstuff.png");
                Minecraft.getInstance().getTextureManager().register(id, texture);
            }
        } catch (Exception e) {
            QuestsAndStuffMod.debugLog("[QnS:UI] failed to prewarm mod logo texture");
        }
    }
}
