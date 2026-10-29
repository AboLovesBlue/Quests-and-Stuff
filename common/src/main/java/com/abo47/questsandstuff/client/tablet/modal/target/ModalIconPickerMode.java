package com.abo47.questsandstuff.client.tablet.modal.target;

import net.minecraft.network.chat.Component;

import com.abo47.questsandstuff.client.tablet.state.TabletUiState;

    public enum ModalIconPickerMode {
    USABLE_ITEMS("usable_items", "send-horizontal"),
    ITEMS("items", "mode_items"),
    TAGS("tags", "mode_tags"),
    BLOCKS("blocks", "brick-wall"),
    FLUIDS("fluids", "mode_fluids"),
    ENTITIES("entities", "entity"),
    INVENTORY("inventory", "mode_inventory");

    private static final ModalIconPickerMode[] MODEL_ITEM_CYCLE = {ITEMS, TAGS};
    private static final ModalIconPickerMode[] USE_ITEM_CYCLE = {USABLE_ITEMS, ITEMS, TAGS, FLUIDS};
    private static final ModalIconPickerMode[] USE_ITEM_WITH_INVENTORY_CYCLE = {USABLE_ITEMS, ITEMS, TAGS, FLUIDS, INVENTORY};
    private static final ModalIconPickerMode[] GENERAL_CYCLE = {ITEMS, TAGS, FLUIDS};
    private static final ModalIconPickerMode[] GENERAL_WITH_ENTITY_CYCLE = {ITEMS, TAGS, FLUIDS, ENTITIES};
    private static final ModalIconPickerMode[] GENERAL_WITH_INVENTORY_CYCLE = {ITEMS, TAGS, FLUIDS, INVENTORY};
    private static final ModalIconPickerMode[] GENERAL_WITH_ENTITY_AND_INVENTORY_CYCLE = {ITEMS, TAGS, FLUIDS, ENTITIES, INVENTORY};
    private static final ModalIconPickerMode[] BACKGROUND_CYCLE = {ITEMS, TAGS, BLOCKS};

    private final String logName;
    private final String icon;

    ModalIconPickerMode(String logName, String icon) {
        this.logName = logName;
        this.icon = icon;
    }

    public String icon() {
        return icon;
    }

    public String logName() {
        return logName;
    }

    public boolean showingTags() {
        return this == TAGS;
    }

    public boolean showingBlocks() {
        return this == BLOCKS;
    }

    public boolean showingFluids() {
        return this == FLUIDS;
    }

    public boolean showingEntities() {
        return this == ENTITIES;
    }

    public boolean showingInventory() {
        return this == INVENTORY;
    }

    public Component[] tooltip() {
        return new Component[]{Component.translatable("ui.questsandstuff.icon_picker.mode." + logName)};
    }

    public static ModalIconPickerMode[] modelItemCycle() {
        return MODEL_ITEM_CYCLE;
    }

    public static ModalIconPickerMode[] cycleForContext(boolean supportsEntityIcons, boolean supportsInventoryIcons, boolean useItemPicker) {
        return cycleFor(supportsEntityIcons, supportsInventoryIcons, useItemPicker);
    }

    public static int cycleIndex(ModalIconPickerMode mode, ModalIconPickerMode[] cycle) {
        ModalIconPickerMode current = safe(mode);
        ModalIconPickerMode[] safeCycle = cycle == null || cycle.length == 0 ? GENERAL_CYCLE : cycle;
        for (int i = 0; i < safeCycle.length; i++) {
            if (safeCycle[i] == current) {
                return i;
            }
        }
        return 0;
    }

    public static String iconAt(ModalIconPickerMode[] cycle, int index) {
        ModalIconPickerMode[] safeCycle = cycle == null || cycle.length == 0 ? GENERAL_CYCLE : cycle;
        return safeCycle[Math.floorMod(index, safeCycle.length)].icon();
    }

    public static ModalIconPickerMode safe(ModalIconPickerMode mode) {
        return mode == null ? ITEMS : mode;
    }

    public static void normalizeForContext(
            TabletUiState state,
            boolean entityPicker,
            boolean itemModelPicker,
            boolean supportsEntityIcons,
            boolean supportsInventoryIcons,
            boolean useItemPicker
    ) {
        if (state != null) {
            state.pickers.iconMode = normalize(state.pickers.iconMode, entityPicker, itemModelPicker, supportsEntityIcons, supportsInventoryIcons, useItemPicker);
        }
    }

    public static ModalIconPickerMode[] gameBackgroundCycle() {
        return BACKGROUND_CYCLE;
    }

    public static void normalizeForBackground(TabletUiState state) {
        if (state == null) {
            return;
        }
        ModalIconPickerMode current = safe(state.pickers.iconMode);
        if (current != ITEMS && current != TAGS && current != BLOCKS) {
            state.pickers.iconMode = ITEMS;
        }
    }

    public static void cycleBackground(TabletUiState state, int direction) {
        if (state == null) {
            return;
        }
        normalizeForBackground(state);
        state.pickers.iconMode = cycleIn(safe(state.pickers.iconMode), direction, BACKGROUND_CYCLE);
        state.pickers.iconScroll = 0;
    }

    public static void cycleModelItems(TabletUiState state, int direction) {
        if (state == null) {
            return;
        }
        ModalIconPickerMode current = normalize(state.pickers.iconMode, false, true, false, false, false);
        state.pickers.iconMode = cycleIn(current, direction, MODEL_ITEM_CYCLE);
        state.pickers.iconScroll = 0;
    }

    public static void cycle(
            TabletUiState state,
            boolean supportsEntityIcons,
            boolean supportsInventoryIcons,
            boolean useItemPicker,
            int direction
    ) {
        if (state == null) {
            return;
        }
        ModalIconPickerMode[] cycle = cycleFor(supportsEntityIcons, supportsInventoryIcons, useItemPicker);
        ModalIconPickerMode current = normalize(state.pickers.iconMode, false, false, supportsEntityIcons, supportsInventoryIcons, useItemPicker);
        state.pickers.iconMode = cycleIn(current, direction, cycle);
        state.pickers.iconScroll = 0;
    }

    public static void reset(TabletUiState state) {
        resetTo(state, ITEMS);
    }

    public static void resetForTarget(TabletUiState state, String target) {
        resetTo(state, initialModeForTarget(target));
    }

    public static void resetTo(TabletUiState state, ModalIconPickerMode mode) {
        if (state != null) {
            state.pickers.iconMode = safe(mode);
        }
    }

    public static boolean isUseItemPickerTarget(ModalTargetParser.Target target) {
        return target != null && target.isTaskSimpleIcon() && "item_use".equals(typePath(target.type()));
    }

    private static ModalIconPickerMode normalize(
            ModalIconPickerMode mode,
            boolean entityPicker,
            boolean itemModelPicker,
            boolean supportsEntityIcons,
            boolean supportsInventoryIcons,
            boolean useItemPicker
    ) {
        if (entityPicker) {
            return ENTITIES;
        }
        ModalIconPickerMode current = safe(mode);
        if (itemModelPicker) {
            return current == TAGS ? TAGS : ITEMS;
        }
        if (useItemPicker) {
            if (current == USABLE_ITEMS || current == ITEMS || current == TAGS || current == FLUIDS) {
                return current;
            }
            return current == INVENTORY && supportsInventoryIcons ? INVENTORY : USABLE_ITEMS;
        }
        if (current == USABLE_ITEMS || current == BLOCKS) {
            return ITEMS;
        }
        if (current == ENTITIES && !supportsEntityIcons) {
            return ITEMS;
        }
        if (current == INVENTORY && !supportsInventoryIcons) {
            return ITEMS;
        }
        return current;
    }

    private static ModalIconPickerMode initialModeForTarget(String target) {
        ModalTargetParser.Target parsed = ModalTargetParser.parse(target);
        if (parsed.isEntityIconPickerTarget()) {
            return ENTITIES;
        }
        if (isUseItemPickerTarget(parsed)) {
            return USABLE_ITEMS;
        }
        return ITEMS;
    }

    private static ModalIconPickerMode cycleIn(ModalIconPickerMode current, int direction, ModalIconPickerMode[] cycle) {
        int currentIndex = 0;
        for (int i = 0; i < cycle.length; i++) {
            if (cycle[i] == current) {
                currentIndex = i;
                break;
            }
        }
        return cycle[Math.floorMod(currentIndex + direction, cycle.length)];
    }

    private static ModalIconPickerMode[] cycleFor(boolean supportsEntityIcons, boolean supportsInventoryIcons, boolean useItemPicker) {
        if (useItemPicker) {
            return supportsInventoryIcons ? USE_ITEM_WITH_INVENTORY_CYCLE : USE_ITEM_CYCLE;
        }
        if (supportsEntityIcons && supportsInventoryIcons) {
            return GENERAL_WITH_ENTITY_AND_INVENTORY_CYCLE;
        }
        if (supportsEntityIcons) {
            return GENERAL_WITH_ENTITY_CYCLE;
        }
        if (supportsInventoryIcons) {
            return GENERAL_WITH_INVENTORY_CYCLE;
        }
        return GENERAL_CYCLE;
    }

    private static String typePath(String type) {
        String value = type == null ? "" : type.trim();
        int namespaceSeparator = value.indexOf(':');
        return namespaceSeparator >= 0 ? value.substring(namespaceSeparator + 1) : value;
    }
}
