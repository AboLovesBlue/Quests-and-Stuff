package com.abo47.questsandstuff.client.tablet.quest.details.task;

import java.util.Locale;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.sync.state.ClientQuestStateFacade;
import com.abo47.questsandstuff.client.tablet.entity.EntityPreviewRenderer;
import com.abo47.questsandstuff.client.tablet.quest.details.task.edit.QuestTaskLootTableRewardEditor;
import com.abo47.questsandstuff.client.tablet.quest.details.task.model.QuestTaskJsonDefaults;
import com.abo47.questsandstuff.client.tablet.quest.details.task.model.QuestTaskJsonFactory;
import com.abo47.questsandstuff.client.tablet.text.QuestTranslationKeys;
import com.abo47.questsandstuff.client.tablet.text.TabletTranslationKeys;
import com.abo47.questsandstuff.client.tablet.text.format.DisplayNameFormatter;
import com.abo47.questsandstuff.client.tablet.text.format.StatTargetFormatter;

import com.google.gson.JsonObject;

public final class QuestTaskDisplayText {
    private QuestTaskDisplayText() {
    }

    public static int amount(JsonObject json) {
        if (json == null || !json.has("amount")) {
            return 1;
        }
        try {
            return Math.max(1, json.get("amount").getAsInt());
        } catch (RuntimeException exception) {
            QuestsAndStuffMod.debugLog(
                    "[QnS:UI] task amount fallback key=amount fallback=1 diagnostic={}",
                    exception.toString()
            );
            return 1;
        }
    }

    public static int parsePositive(String value, int fallback) {
        try {
            return Math.max(1, Math.min(99999, Integer.parseInt(value == null ? "" : value.trim())));
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    public static String taskIcon(JsonObject json) {
        String explicitIcon = QuestTaskJsonFactory.asString(json, "icon", "");
        String path = QuestTaskJsonFactory.typePath(QuestTaskJsonFactory.asString(json, "type", ""));
        if ("xp".equals(path) && (explicitIcon.isBlank() || "xp".equals(explicitIcon))) {
            return QuestTaskJsonFactory.XP_CARD_ICON;
        }
        if (!explicitIcon.isBlank()) {
            return explicitIcon;
        }
        String tag = QuestTaskJsonFactory.asString(json, "tag", "");
        if (!tag.isBlank()) {
            return "#" + tag;
        }
        String target = QuestTaskJsonFactory.asString(json, "target", "");
        if (isEntityTask(path) && !target.isBlank()) {
            return EntityPreviewRenderer.entityAsset(target);
        }
        return QuestTaskJsonFactory.firstPresent(json, "item", "fallback_item", "minecraft:book");
    }

    public static String rewardIcon(JsonObject json) {
        String path = QuestTaskJsonFactory.typePath(QuestTaskJsonFactory.asString(json, "type", ""));
        String explicitIcon = QuestTaskJsonFactory.asString(json, "icon", "");
        if ("xp".equals(path) && (explicitIcon.isBlank() || "xp".equals(explicitIcon))) {
            return QuestTaskJsonFactory.XP_CARD_ICON;
        }
        if ("loot_table".equals(path) || "loot".equals(path)) {
            return QuestTaskJsonFactory.firstPresent(json, "icon", "minecraft:chest");
        }
        return QuestTaskJsonFactory.firstPresent(json, "icon", "item", "fallback_item", QuestTaskJsonDefaults.rewardFallback(path));
    }

    public static String displayName(JsonObject json, String type) {
        String path = QuestTaskJsonFactory.typePath(type);
        String title = QuestTaskJsonFactory.asString(json, "title", "");
        if (!title.isBlank()) {
            return title;
        }
        if ("biome".equals(path)) {
            return TabletTranslationKeys.text(QuestTranslationKeys.VISIT_TARGET, readableIdName(QuestTaskJsonFactory.asString(json, "target", "")));
        }
        if ("advancement".equals(path)) {
            String advancementName = DisplayNameFormatter.advancement(
                    QuestTaskJsonFactory.asString(json, "target", ""),
                    ClientQuestStateFacade.advancementDisplays()
            );
            return advancementName.isBlank() ? typeLabel(type) : advancementName;
        }
        if ("recipe".equals(path)) {
            String recipeTarget = QuestTaskJsonFactory.asString(json, "target", "");
            if (recipeTarget.startsWith("#")) {
                return readableTagName(recipeTarget);
            }
            String recipeName = itemName(recipeTarget);
            return recipeName.isBlank() ? typeLabel(type) : recipeName;
        }
        if ("structure".equals(path)) {
            String structureName = DisplayNameFormatter.resourceLeaf(QuestTaskJsonFactory.asString(json, "target", ""));
            return structureName.isBlank() ? typeLabel(type) : TabletTranslationKeys.text(QuestTranslationKeys.VISIT_TARGET, structureName);
        }
        if ("block_interact".equals(path) || "block_interaction".equals(path)) {
            String blockName = blockTargetName(QuestTaskJsonFactory.asString(json, "target", ""));
            return blockName.isBlank() ? typeLabel(type) : TabletTranslationKeys.text(QuestTranslationKeys.INTERACT_TARGET, blockName);
        }
        if ("stat".equals(path)) {
            String statName = StatTargetFormatter.displayName(QuestTaskJsonFactory.asString(json, "target", ""));
            return statName.isBlank() ? typeLabel(type) : statName;
        }
        if ("location".equals(path)) {
            return TabletTranslationKeys.text(QuestTranslationKeys.VISIT_TARGET, readableIdName(QuestTaskJsonFactory.asString(json, "dimension", "")));
        }
        if ("kill_entity".equals(path)) {
            String entityId = QuestTaskJsonFactory.asString(json, "target", "");
            String entityName = EntityPreviewRenderer.entityDisplayName(entityId);
            return entityName.isBlank()
                    ? TabletTranslationKeys.text(QuestTranslationKeys.KILL_ENTITY)
                    : TabletTranslationKeys.text(QuestTranslationKeys.KILL_ENTITY_NAMED, entityName);
        }
        if ("scan_entity".equals(path)) {
            String entityId = QuestTaskJsonFactory.asString(json, "target", "");
            String entityName = EntityPreviewRenderer.entityDisplayName(entityId);
            return entityName.isBlank()
                    ? TabletTranslationKeys.text(QuestTranslationKeys.SCAN_ENTITY)
                    : TabletTranslationKeys.text(QuestTranslationKeys.SCAN_ENTITY_NAMED, entityName);
        }
        if ("entity_interact".equals(path) || "entity_interaction".equals(path)) {
            String entityName = EntityPreviewRenderer.entityDisplayName(QuestTaskJsonFactory.asString(json, "target", ""));
            return entityName.isBlank() ? typeLabel(type) : TabletTranslationKeys.text(QuestTranslationKeys.INTERACT_TARGET, entityName);
        }
        if ("item_interact".equals(path) || "item_interaction".equals(path)) {
            String itemName = itemTargetName(QuestTaskJsonFactory.asString(json, "target", ""));
            return itemName.isBlank() ? typeLabel(type) : TabletTranslationKeys.text(QuestTranslationKeys.INTERACT_TARGET, itemName);
        }
        if ("item_use".equals(path)) {
            String itemName = itemTargetName(QuestTaskJsonFactory.asString(json, "target", ""));
            return itemName.isBlank() ? typeLabel(type) : TabletTranslationKeys.text(QuestTranslationKeys.USE_TARGET, itemName);
        }
        if ("loot_table".equals(path) || "loot".equals(path)) {
            String lootName = QuestTaskLootTableRewardEditor.displayName(QuestTaskJsonFactory.asString(json, "loot_table", ""));
            if (!lootName.isBlank()) {
                return lootName;
            }
        }
        String tag = QuestTaskJsonFactory.asString(json, "tag", "");
        if (!tag.isBlank()) {
            return readableTagName(tag);
        }
        String item = QuestTaskJsonFactory.firstPresent(json, "item", "fallback_item", "icon");
        if (!item.isBlank()) {
            String name = itemName(item);
            if (!name.isBlank()) {
                return name;
            }
        }
        String subtitle = QuestTaskJsonFactory.firstPresent(json, "target", "dimension", "loot_table", "command");
        return subtitle.isBlank() ? typeLabel(type) : subtitle;
    }

    public static String typeLabel(String type) {
        String path = QuestTaskJsonFactory.typePath(type);
        String spaced = path.replace('_', ' ');
        return spaced.isBlank() ? TabletTranslationKeys.text(TabletTranslationKeys.COMMON_UNKNOWN) : Character.toUpperCase(spaced.charAt(0)) + spaced.substring(1);
    }

    public static boolean isManualTask(JsonObject json) {
        String path = QuestTaskJsonFactory.typePath(QuestTaskJsonFactory.asString(json, "type", ""));
        return "check".equals(path) || "dummy".equals(path);
    }

    public static boolean isManualXpTask(JsonObject json) {
        String path = QuestTaskJsonFactory.typePath(QuestTaskJsonFactory.asString(json, "type", ""));
        String collection = QuestTaskJsonFactory.asString(json, "collection", "automatic");
        return "xp".equals(path) && ("manual".equals(collection) || "consume".equals(collection));
    }

    public static boolean usesAmountField(JsonObject json, boolean task) {
        String path = QuestTaskJsonFactory.typePath(QuestTaskJsonFactory.asString(json, "type", ""));
        if (task) {
            return !isManualTask(json)
                    && !"biome".equals(path)
                    && !"scan_entity".equals(path)
                    && !"advancement".equals(path)
                    && !"structure".equals(path)
                    && !"block_interact".equals(path)
                    && !"block_interaction".equals(path)
                    && !"location".equals(path);
        }
        return !"command".equals(path) && !"loot_table".equals(path) && !"loot".equals(path) && !"selectable".equals(path);
    }


    private static boolean isEntityTask(String typePath) {
        return "kill_entity".equals(typePath)
                || "scan_entity".equals(typePath)
                || "entity_interact".equals(typePath)
                || "entity_interaction".equals(typePath);
    }

    private static String itemName(String value) {
        ResourceLocation id = ResourceLocation.tryParse(value);
        if (id == null) {
            return "";
        }
        Item item = BuiltInRegistries.ITEM.get(id);
        if (item == Items.AIR && !"minecraft:air".equals(value)) {
            return value;
        }
        return item.getDescription().getString();
    }

    private static String blockName(String value) {
        ResourceLocation id = ResourceLocation.tryParse(value);
        if (id == null) {
            return "";
        }
        Block block = BuiltInRegistries.BLOCK.get(id);
        if (block == Blocks.AIR && !"minecraft:air".equals(value)) {
            return value;
        }
        return block.getName().getString();
    }

    private static String blockTargetName(String value) {
        String clean = value == null ? "" : value.trim();
        if (clean.startsWith("#")) {
            return DisplayNameFormatter.resourceLeaf(clean.substring(1));
        }
        return blockName(clean);
    }

    private static String itemTargetName(String value) {
        String clean = value == null ? "" : value.trim();
        if (clean.startsWith("#")) {
            return readableTagName(clean);
        }
        return itemName(clean);
    }

    private static String readableIdName(String value) {
        String clean = value == null ? "" : value.trim();
        int colon = clean.indexOf(':');
        if (colon >= 0) {
            clean = clean.substring(colon + 1);
        }
        clean = clean.replace('/', ' ').replace('_', ' ').trim();
        return clean.isBlank() ? TabletTranslationKeys.text(QuestTranslationKeys.BIOME_FALLBACK) : clean.toLowerCase(Locale.ROOT);
    }

    private static String readableTagName(String tag) {
        String value = tag == null ? "" : tag.trim();
        if (value.startsWith("#")) {
            value = value.substring(1);
        }
        int colon = value.indexOf(':');
        String path = colon >= 0 ? value.substring(colon + 1) : value;
        path = path.replace('\\', '/');
        if (path.isBlank()) {
            return TabletTranslationKeys.text(QuestTranslationKeys.ANY_ITEM);
        }
        String[] parts = path.split("/");
        String name;
        if (parts.length >= 2) {
            String group = singular(parts[0]);
            String material = prettify(parts[parts.length - 1]);
            name = material + " " + group;
        } else {
            name = singular(prettify(path));
        }
        return TabletTranslationKeys.text(QuestTranslationKeys.ANY_NAMED_ITEM, name);
    }

    private static String prettify(String value) {
        return value == null ? "" : value.replace('_', ' ').trim().toLowerCase(Locale.ROOT);
    }

    private static String singular(String value) {
        String clean = prettify(value);
        if (clean.endsWith("ies") && clean.length() > 3) {
            return clean.substring(0, clean.length() - 3) + "y";
        }
        if (clean.endsWith("sses") || clean.endsWith("ches") || clean.endsWith("shes")) {
            return clean.substring(0, clean.length() - 2);
        }
        if (clean.endsWith("s") && !clean.endsWith("ss")) {
            return clean.substring(0, clean.length() - 1);
        }
        return clean;
    }
}
