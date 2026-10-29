package com.abo47.questsandstuff.client.tablet.quest.canvas.render.connection;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.nio.file.Path;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.assets.AssetLibrary;
import com.abo47.questsandstuff.client.tablet.ui.factory.UiFactory;

import com.lowdragmc.lowdraglib.gui.texture.DynamicTexture;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;

import net.minecraft.resources.ResourceLocation;

final class ConnectionTextureResolver {
    private static final Map<String, Integer> TEX_WIDTH_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, int[]> TEX_DIM_CACHE = new ConcurrentHashMap<>();

    private ConnectionTextureResolver() {
    }

    static int textureWidth(String textureStr) {
        if (textureStr == null || textureStr.isBlank()) return -1;
        Integer cached = TEX_WIDTH_CACHE.get(textureStr);
        if (cached != null) return cached;
        try {
            Path assetsRoot = UiFactory.ASSETS_ROOT_DIR;
            AssetLibrary.AssetDimensions ad = AssetLibrary.assetDimensions(assetsRoot, textureStr);
            if (ad != null) {
                TEX_WIDTH_CACHE.put(textureStr, ad.width());
                return ad.width();
            }
        } catch (Exception ignored) {
        }
        TEX_WIDTH_CACHE.put(textureStr, -1);
        return -1;
    }

    static int[] textureDims(String textureStr) {
        if (textureStr == null || textureStr.isBlank()) return null;
        int[] cached = TEX_DIM_CACHE.get(textureStr);
        if (cached != null) return cached.length > 0 ? cached : null;
        try {
            Path assetsRoot = UiFactory.ASSETS_ROOT_DIR;
            AssetLibrary.AssetDimensions ad = AssetLibrary.assetDimensions(assetsRoot, textureStr);
            if (ad != null) {
                int[] dims = new int[]{ad.width(), ad.height()};
                TEX_DIM_CACHE.put(textureStr, dims);
                return dims;
            }
        } catch (Exception ignored) {
        }
        TEX_DIM_CACHE.put(textureStr, new int[0]);
        return null;
    }

    static ResourceLocation resolveTexture(String textureStr) {
        if (textureStr == null || textureStr.isBlank()) {
            return null;
        }
        ResourceLocation parsed = ResourceLocation.tryParse(textureStr);
        if (parsed != null && parsed.getNamespace().equals(QuestsAndStuffMod.MODID)) {
            return parsed;
        }
        if (AssetLibrary.gameItemStack(textureStr) != null) {
            return null;
        }
        Path assetsRoot = UiFactory.ASSETS_ROOT_DIR;
        try {
            AssetLibrary.ensureAssetsDirs(assetsRoot);
            IGuiTexture guiTexture = AssetLibrary.chapterBackgroundTexture(assetsRoot, textureStr);
            if (guiTexture == null) {
                return null;
            }
            if (guiTexture instanceof DynamicTexture dynamic) {
                guiTexture = dynamic.textureSupplier.get();
            }
            if (guiTexture instanceof ResourceTexture resource) {
                return resource.imageLocation;
            }
            String sanitized = sanitizeAssetId(textureStr);
            return ResourceLocation.tryBuild(QuestsAndStuffMod.MODID, "chapter_asset/" + sanitized);
        } catch (Exception e) {
            return null;
        }
    }

    private static String sanitizeAssetId(String value) {
        String lower = value.toLowerCase(Locale.ROOT);
        StringBuilder out = new StringBuilder(lower.length());
        for (int i = 0; i < lower.length(); i++) {
            char c = lower.charAt(i);
            if ((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || c == '_' || c == '-' || c == '.' || c == '/') {
                out.append(c);
            } else {
                out.append('_');
            }
        }
        return out.toString();
    }
}
