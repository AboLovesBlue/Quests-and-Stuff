package com.abo47.questsandstuff.client.tablet.quest.details.task.model;

import com.google.gson.JsonObject;

public final class QuestTaskJsonDefaults {
    private QuestTaskJsonDefaults() {
    }

    public static JsonObject defaultTask(String id, String typePath) {
        QuestTaskTypeChoice choice = QuestTaskTypeCatalog.taskChoice(typePath);
        return choice == null ? simpleDefaultTask(id, typePath) : choice.defaultJson(id);
    }

    public static JsonObject defaultReward(String id, String typePath) {
        QuestTaskTypeChoice choice = QuestTaskTypeCatalog.rewardChoice(typePath);
        return choice == null ? base(id, QuestTaskJsonFactory.MOD + typePath) : choice.defaultJson(id);
    }

    public static JsonObject simpleDefaultTask(String id, String typePath) {
        return simpleTask(id, QuestTaskJsonFactory.MOD + typePath, defaultTarget(typePath), fallbackIcon(typePath));
    }

    public static JsonObject xpTask(String id, String typePath) {
        JsonObject json = base(id, QuestTaskJsonFactory.MOD + typePath);
        json.addProperty("amount", 1);
        json.addProperty("mode", "points");
        json.addProperty("collection", "automatic");
        json.addProperty("icon", QuestTaskJsonFactory.XP_CARD_ICON);
        return json;
    }

    public static JsonObject statTask(String id, String typePath) {
        return simpleTask(id, QuestTaskJsonFactory.MOD + typePath, "minecraft:jump", "stat");
    }

    public static JsonObject locationTask(String id, String typePath) {
        JsonObject json = base(id, QuestTaskJsonFactory.MOD + typePath);
        json.addProperty("mode", "dimension");
        json.addProperty("dimension", "minecraft:overworld");
        json.addProperty("x", 0);
        json.addProperty("y", 64);
        json.addProperty("z", 0);
        json.addProperty("radius", 6);
        json.addProperty("icon", "minecraft:compass");
        return json;
    }

    public static JsonObject checkTask(String id, String typePath) {
        JsonObject json = base(id, QuestTaskJsonFactory.MOD + typePath);
        json.addProperty("target", id);
        json.addProperty("icon", "manual_check");
        return json;
    }

    public static JsonObject itemTask(String id, String typePath) {
        JsonObject json = base(id, QuestTaskJsonFactory.MOD + typePath);
        json.addProperty("item", "minecraft:stone");
        json.addProperty("amount", 1);
        json.addProperty("nbt", "");
        json.addProperty("collection", "automatic");
        return json;
    }

    public static JsonObject itemReward(String id, String typePath) {
        JsonObject json = base(id, QuestTaskJsonFactory.MOD + typePath);
        json.addProperty("item", "minecraft:diamond");
        json.addProperty("amount", 1);
        json.addProperty("nbt", "");
        json.addProperty("icon", "minecraft:diamond");
        return json;
    }

    public static JsonObject xpReward(String id, String typePath) {
        JsonObject json = base(id, QuestTaskJsonFactory.MOD + typePath);
        json.addProperty("amount", 1);
        json.addProperty("mode", "points");
        json.addProperty("icon", QuestTaskJsonFactory.XP_CARD_ICON);
        return json;
    }

    public static JsonObject lootTableReward(String id, String typePath) {
        JsonObject json = base(id, QuestTaskJsonFactory.MOD + typePath);
        json.addProperty("loot_table", "minecraft:chests/simple_dungeon");
        json.addProperty("title", "Simple dungeon");
        json.addProperty("icon", "minecraft:chest");
        return json;
    }

    public static JsonObject commandReward(String id, String typePath) {
        JsonObject json = base(id, QuestTaskJsonFactory.MOD + typePath);
        json.addProperty("command", "say Quest reward");
        json.addProperty("title", "Command");
        json.addProperty("icon", "minecraft:command_block");
        return json;
    }

    public static JsonObject simpleTask(String id, String type, String target, String icon) {
        JsonObject json = base(id, type);
        json.addProperty("amount", 1);
        json.addProperty("target", target == null ? "" : target);
        json.addProperty("icon", icon == null ? "" : icon);
        return json;
    }

    public static JsonObject base(String id, String type) {
        JsonObject json = new JsonObject();
        json.addProperty("id", id);
        json.addProperty("type", type);
        return json;
    }

    private static String defaultTarget(String typePath) {
        return switch (typePath) {
            case "kill_entity" -> "minecraft:zombie";
            case "scan_entity" -> "minecraft:cow";
            case "advancement" -> "minecraft:story/root";
            case "recipe" -> "recipe";
            case "structure" -> "minecraft:village";
            case "block_interact", "block_interaction" -> "minecraft:crafting_table";
            case "entity_interact", "entity_interaction" -> "minecraft:villager";
            case "changed_dimension" -> "minecraft:the_nether";
            default -> "";
        };
    }

    private static String fallbackIcon(String typePath) {
        return switch (typePath) {
            case "kill_entity" -> "";
            case "scan_entity" -> "";
            case "advancement" -> "minecraft:book";
            case "recipe" -> "recipe";
            case "structure" -> "minecraft:map";
            case "biome" -> "biome";
            case "block_interact", "block_interaction" -> "minecraft:oak_button";
            case "entity_interact", "entity_interaction" -> "";
            case "item_interact", "item_interaction", "item_use" -> "minecraft:stick";
            case "changed_dimension" -> "minecraft:obsidian";
            case "xp" -> QuestTaskJsonFactory.XP_CARD_ICON;
            case "stat" -> "stat";
            case "location" -> "minecraft:compass";
            default -> "minecraft:book";
        };
    }

    public static String rewardFallback(String typePath) {
        return switch (typePath) {
            case "xp" -> QuestTaskJsonFactory.XP_CARD_ICON;
            case "loot", "loot_table" -> "minecraft:chest";
            case "command" -> "minecraft:command_block";
            case "selectable" -> "minecraft:bundle";
            default -> "minecraft:diamond";
        };
    }
}
