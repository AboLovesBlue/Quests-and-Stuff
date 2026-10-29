package com.abo47.questsandstuff.quest.persistence.chapter;

import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.quest.model.canvas.CanvasExclusiveChoice;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;
import com.abo47.questsandstuff.quest.persistence.GsonProvider;
import com.abo47.questsandstuff.util.io.JsonFileTree;

import com.google.gson.Gson;

public final class ChapterMetadataStore {
    private static final Gson GSON = GsonProvider.GSON;

    private final Path chaptersDir;
    private final ChapterMetadataState state = new ChapterMetadataState();

    public ChapterMetadataStore(Path chaptersDir) {
        this.chaptersDir = chaptersDir;
    }

    public List<String> chapterOrder() {
        return List.copyOf(state.chapterOrder);
    }

    public ChapterMetadataSnapshot snapshot() {
        return new ChapterMetadataSnapshot(
                List.copyOf(state.chapterOrder),
                Map.copyOf(state.chapterIcons),
                Map.copyOf(state.chapterBackgrounds),
                Map.copyOf(state.chapterCanvasBackgrounds),
                Map.copyOf(state.chapterTextAlign),
                Map.copyOf(state.chapterTextColor),
                Map.copyOf(state.chapterTextStyle),
                Map.copyOf(state.chapterTextSize),
                Map.copyOf(state.chapterLockUntilUnlocked),
                Map.copyOf(state.chapterHideUntilUnlocked),
                ChapterMetadataState.copyLayerMap(state.canvasExclusiveChoicesByChapter),
                ChapterMetadataState.copyLayerMap(state.canvasImagesByChapter),
                ChapterMetadataState.copyLayerMap(state.canvasTextsByChapter),
                ChapterMetadataState.copyLayerMap(state.canvasLayerOrderByChapter)
        );
    }

    public void restore(ChapterMetadataSnapshot snapshot) {
        state.clear();
        if (snapshot == null) {
            save();
            return;
        }
        state.chapterOrder.addAll(snapshot.chapterOrder());
        state.chapterIcons.putAll(snapshot.chapterIcons());
        state.chapterBackgrounds.putAll(snapshot.chapterBackgrounds());
        state.chapterCanvasBackgrounds.putAll(snapshot.chapterCanvasBackgrounds());
        state.chapterTextAlign.putAll(snapshot.chapterTextAlign());
        state.chapterTextColor.putAll(snapshot.chapterTextColor());
        state.chapterTextStyle.putAll(snapshot.chapterTextStyle());
        state.chapterTextSize.putAll(snapshot.chapterTextSize());
        state.chapterLockUntilUnlocked.putAll(snapshot.chapterLockUntilUnlocked());
        state.chapterHideUntilUnlocked.putAll(snapshot.chapterHideUntilUnlocked());
        state.canvasExclusiveChoicesByChapter.putAll(mutableLayerMap(snapshot.canvasExclusiveChoicesByChapter()));
        state.canvasImagesByChapter.putAll(mutableLayerMap(snapshot.canvasImagesByChapter()));
        state.canvasTextsByChapter.putAll(mutableLayerMap(snapshot.canvasTextsByChapter()));
        state.canvasLayerOrderByChapter.putAll(mutableLayerMap(snapshot.canvasLayerOrderByChapter()));
        save();
    }

    public void setChapterOrder(List<String> chapters, Set<String> discoveredChapters) {
        state.setChapterOrder(chapters, discoveredChapters);
        save();
    }

    public void renameChapter(String fromName, String toName, Set<String> discoveredChapters) {
        state.renameChapter(fromName, toName);
        state.reconcile(discoveredChapters);
        save();
    }

    public String chapterIcon(String chapter) {
        return state.chapterIcon(chapter);
    }

    public String chapterBackground(String chapter) {
        return state.chapterBackground(chapter);
    }

    public String chapterCanvasBackground(String chapter) {
        return state.chapterCanvasBackground(chapter);
    }

    public String chapterTextAlign(String chapter) {
        return state.chapterTextAlign(chapter);
    }

    public int chapterTextColor(String chapter) {
        return state.chapterTextColor(chapter);
    }

    public String chapterTextStyle(String chapter) {
        return state.chapterTextStyle(chapter);
    }

    public int chapterTextSize(String chapter) {
        return state.chapterTextSize(chapter);
    }

    public boolean chapterLockUntilUnlocked(String chapter) {
        return state.chapterLockUntilUnlocked(chapter);
    }

    public boolean chapterHideUntilUnlocked(String chapter) {
        return state.chapterHideUntilUnlocked(chapter);
    }

    public java.util.Map<String, List<CanvasImageLayer>> canvasImagesByChapter() {
        return ChapterMetadataState.copyLayerMap(state.canvasImagesByChapter);
    }

    public java.util.Map<String, List<CanvasTextLayer>> canvasTextsByChapter() {
        return ChapterMetadataState.copyLayerMap(state.canvasTextsByChapter);
    }

    public java.util.Map<String, List<String>> canvasLayerOrderByChapter() {
        return ChapterMetadataState.copyLayerMap(state.canvasLayerOrderByChapter);
    }

    public List<CanvasImageLayer> canvasImages(String chapter) {
        return List.copyOf(state.canvasImagesByChapter.getOrDefault(ChapterMetadataState.normalizeChapterName(chapter), List.of()));
    }

    public List<CanvasTextLayer> canvasTexts(String chapter) {
        return List.copyOf(state.canvasTextsByChapter.getOrDefault(ChapterMetadataState.normalizeChapterName(chapter), List.of()));
    }

    public List<String> canvasLayerOrder(String chapter) {
        return List.copyOf(state.canvasLayerOrderByChapter.getOrDefault(ChapterMetadataState.normalizeChapterName(chapter), List.of()));
    }

    public List<CanvasExclusiveChoice> canvasExclusiveChoices(String chapter) {
        return List.copyOf(state.canvasExclusiveChoicesByChapter.getOrDefault(ChapterMetadataState.normalizeChapterName(chapter), List.of()));
    }

    public void setChapterIcon(String chapter, String icon) {
        set(chapter, "icon", icon == null ? "" : icon.trim(), state.chapterIcons);
    }

    public void setChapterBackground(String chapter, String background) {
        set(chapter, "background", background == null || background.isBlank() ? "default" : background.trim(), state.chapterBackgrounds);
    }

    public void setChapterCanvasBackground(String chapter, String background) {
        set(chapter, "canvas_background", background == null || background.isBlank() ? "default" : background.trim(), state.chapterCanvasBackgrounds);
    }

    public void setChapterTextAlign(String chapter, String align) {
        set(chapter, "text_align", ChapterMetadataJsonCodec.normalizeTextAlign(align), state.chapterTextAlign);
    }

    public void setChapterTextColor(String chapter, int color) {
        set(chapter, "text_color", color, state.chapterTextColor);
    }

    public void setChapterTextStyle(String chapter, String style) {
        set(chapter, "text_style", ChapterMetadataJsonCodec.normalizeTextStyle(style), state.chapterTextStyle);
    }

    public void setChapterTextSize(String chapter, int size) {
        set(chapter, "text_size", CanvasTextLayer.clampFontSize(size), state.chapterTextSize);
    }

    public void setChapterLockUntilUnlocked(String chapter, boolean lockUntilUnlocked) {
        set(chapter, "lock_until_unlocked", lockUntilUnlocked, state.chapterLockUntilUnlocked);
    }

    public void setChapterHideUntilUnlocked(String chapter, boolean hideUntilUnlocked) {
        set(chapter, "hide_until_unlocked", hideUntilUnlocked, state.chapterHideUntilUnlocked);
    }

    public void putCanvasExclusiveChoice(String chapter, CanvasExclusiveChoice ec) {
        String normalized = ChapterMetadataState.normalizeChapterName(chapter);
        if (ChapterCanvasLayerMutations.put(state, normalized, ChapterCanvasLayerMutations.EXCLUSIVE_CHOICE_ID_PREFIX, ec, state.canvasExclusiveChoicesByChapter, CanvasExclusiveChoice::id)) {
            saveChapter(normalized);
        }
    }

    public boolean removeCanvasExclusiveChoice(String chapter, String ecId) {
        String normalized = ChapterMetadataState.normalizeChapterName(chapter);
        if (!ChapterCanvasLayerMutations.remove(state, normalized, ecId, ChapterCanvasLayerMutations.EXCLUSIVE_CHOICE_ID_PREFIX, state.canvasExclusiveChoicesByChapter, CanvasExclusiveChoice::id)) {
            return false;
        }
        saveChapter(normalized);
        return true;
    }

    public void putCanvasImage(String chapter, CanvasImageLayer image) {
        String normalized = ChapterMetadataState.normalizeChapterName(chapter);
        if (ChapterCanvasLayerMutations.put(state, normalized, ChapterCanvasLayerMutations.IMAGE_ID_PREFIX, image, state.canvasImagesByChapter, CanvasImageLayer::id)) {
            saveChapter(normalized);
        }
    }

    public boolean removeCanvasImage(String chapter, String imageId) {
        String normalized = ChapterMetadataState.normalizeChapterName(chapter);
        if (!ChapterCanvasLayerMutations.remove(state, normalized, imageId, ChapterCanvasLayerMutations.IMAGE_ID_PREFIX, state.canvasImagesByChapter, CanvasImageLayer::id)) {
            return false;
        }
        saveChapter(normalized);
        return true;
    }

    public void putCanvasText(String chapter, CanvasTextLayer text) {
        String normalized = ChapterMetadataState.normalizeChapterName(chapter);
        if (ChapterCanvasLayerMutations.put(state, normalized, ChapterCanvasLayerMutations.TEXT_ID_PREFIX, text, state.canvasTextsByChapter, CanvasTextLayer::id)) {
            saveChapter(normalized);
        }
    }

    public boolean removeCanvasText(String chapter, String textId) {
        String normalized = ChapterMetadataState.normalizeChapterName(chapter);
        if (!ChapterCanvasLayerMutations.remove(state, normalized, textId, ChapterCanvasLayerMutations.TEXT_ID_PREFIX, state.canvasTextsByChapter, CanvasTextLayer::id)) {
            return false;
        }
        saveChapter(normalized);
        return true;
    }

    public void setCanvasLayerOrder(String chapter, List<String> order) {
        String normalized = state.ensureChapter(chapter);
        if (normalized.isBlank()) {
            return;
        }
        ChapterCanvasLayerMutations.setOrder(state, normalized, order);
        saveChapter(normalized);
    }

    public void putCanvasLayers(String chapter, List<CanvasImageLayer> images, List<CanvasTextLayer> texts, List<String> order) {
        String normalized = state.ensureChapter(chapter);
        if (normalized.isBlank()) {
            return;
        }
        ChapterCanvasLayerMutations.putAll(state, normalized, ChapterCanvasLayerMutations.IMAGE_ID_PREFIX, images, state.canvasImagesByChapter, CanvasImageLayer::id);
        ChapterCanvasLayerMutations.putAll(state, normalized, ChapterCanvasLayerMutations.TEXT_ID_PREFIX, texts, state.canvasTextsByChapter, CanvasTextLayer::id);
        ChapterCanvasLayerMutations.setOrder(state, normalized, order);
        saveChapter(normalized);
    }

    private <T> void set(String chapter, String key, T value, Map<String, T> target) {
        String normalized = ChapterMetadataState.normalizeChapterName(chapter);
        if (normalized.isBlank()) {
            return;
        }
        target.put(normalized, value);
        QuestsAndStuffMod.debugLog("[QnS:Store] chapter {} {} -> {}", key, normalized, value);
        saveChapter(normalized);
    }

    public void load(Set<String> discoveredChapters) {
        state.clear();
        try {
            boolean migrated = false;
            Map<String, Integer> orders = new HashMap<>();
            for (Path path : JsonFileTree.jsonFiles(chaptersDir)) {
                migrated |= ChapterMetadataReader.read(path, state, orders);
            }
            state.chapterOrder.sort((a, b) -> Integer.compare(orders.getOrDefault(a, Integer.MAX_VALUE), orders.getOrDefault(b, Integer.MAX_VALUE)));
            reconcile(discoveredChapters);
            if (migrated) {
                save();
            }
        } catch (Exception e) {
            QuestsAndStuffMod.LOGGER.warn("Failed to read chapter metadata from {}", chaptersDir, e);
        }
    }

    public void save() {
        ChapterMetadataWriter.save(chaptersDir, state, GSON);
    }

    public void saveChapter(String chapter) {
        String normalized = ChapterMetadataState.normalizeChapterName(chapter);
        if (!normalized.isBlank()) {
            ChapterMetadataWriter.saveGroups(chaptersDir, state, GSON, List.of(normalized));
        }
    }

    public void saveChapters(Collection<String> chapters) {
        ChapterMetadataWriter.saveGroups(chaptersDir, state, GSON, chapters);
    }

    public void reconcile(Set<String> discoveredChapters) {
        state.reconcile(discoveredChapters);
    }

    private static <T> Map<String, List<T>> mutableLayerMap(Map<String, List<T>> source) {
        Map<String, List<T>> copy = new HashMap<>();
        if (source == null) {
            return copy;
        }
        for (Map.Entry<String, List<T>> entry : source.entrySet()) {
            copy.put(entry.getKey(), List.copyOf(entry.getValue()));
        }
        return copy;
    }

}
