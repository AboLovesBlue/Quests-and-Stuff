package com.abo47.questsandstuff.client.tablet.modal.pickers;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsWindow;
import com.abo47.questsandstuff.client.tablet.state.UiState;
import com.abo47.questsandstuff.client.tablet.text.QuestTranslationKeys;
import com.abo47.questsandstuff.client.tablet.text.TranslationKeys;
import com.abo47.questsandstuff.client.tablet.text.format.DisplayNameFormatter;
import com.abo47.questsandstuff.client.tablet.theme.tokens.ThemeColors;

import static com.abo47.questsandstuff.client.tablet.modal.ModalSession.TargetSlot.CANVAS_MODEL;
import com.abo47.questsandstuff.client.tablet.modal.ModalPanel;
import com.abo47.questsandstuff.client.tablet.modal.target.ModalTargetState;
import com.abo47.questsandstuff.client.tablet.modal.ModalSession;

public final class BlockPickerModal {
    private BlockPickerModal() {
    }

    public static void prewarm() {
        RegistryTilePicker.prewarm("block_items", BlockPickerModal::blockChoices);
    }

    public static TextFieldWidget rebuild(WidgetGroup modal, UiState state, Player player, Runnable refresh, int w, int h) {
        return RegistryTilePicker.rebuild(modal, state, player, refresh, w, h, new RegistryTilePicker.Config(
                TranslationKeys.text(QuestTranslationKeys.CHOOSE_BLOCK),
                TranslationKeys.text(QuestTranslationKeys.NO_BLOCKS),
                "block",
                "block",
                ThemeColors.INTERACTIVE,
                new RegistryTilePicker.StateAccess(
                        () -> state.pickers.blockSearch,
                        value -> state.pickers.blockSearch = value,
                        () -> state.pickers.blockScroll,
                        value -> state.pickers.blockScroll = value,
                        () -> state.pickers.blockScrollDragging,
                        dragging -> state.pickers.blockScrollDragging = dragging,
                        focused -> state.pickers.blockSearchFocused = focused,
                        () -> state.pickers.blockTagMode,
                        () -> state.pickers.blockTagMode = !state.pickers.blockTagMode),
                "block_items",
                BlockPickerModal::blockChoices,
                "block_tags",
                BlockPickerModal::tagChoices,
                (pickPlayer, pickState, entry) -> {
                    if (entry.value().isBlank()) {
                        return true;
                    }
                    String canvasModelTarget = ModalTargetState.target(pickState, CANVAS_MODEL, pickState.modal.modalCanvasModelTarget);
                    if (!canvasModelTarget.isBlank()) {
                        return ModalPanel.runCanvasModelAction(pickState, canvasModelTarget, entry.value());
                    }
                    QuestDetailsWindow.applyBlockPick(pickPlayer, pickState, entry.value());
                    return true;
                }));
    }

    private static List<RegistryTilePicker.Choice> blockChoices() {
        return BuiltInRegistries.ITEM.stream()
                .filter(item -> item instanceof BlockItem)
                .map(BlockPickerModal::choice)
                .filter(choice -> choice != null)
                .toList();
    }

    private static List<RegistryTilePicker.Choice> tagChoices() {
        return BuiltInRegistries.BLOCK.getTagNames()
                .map(BlockPickerModal::tagChoice)
                .toList();
    }

    private static RegistryTilePicker.Choice choice(Item item) {
        if (!(item instanceof BlockItem blockItem)) {
            return null;
        }
        Block block = blockItem.getBlock();
        if (!isPickable(block)) {
            return null;
        }
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);
        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(block);
        if (itemId == null || blockId == null) {
            return null;
        }
        return new RegistryTilePicker.Choice(blockId.toString(), itemId.toString(), item.getDescription().getString(), new ItemStack[]{new ItemStack(item)}, false);
    }

    private static RegistryTilePicker.Choice tagChoice(TagKey<Block> tag) {
        String value = "#" + tag.location();
        ItemStack[] previews = tagPreviews(tag);
        String previewId = previews.length == 0 ? "box" : BuiltInRegistries.ITEM.getKey(previews[0].getItem()).toString();
        return new RegistryTilePicker.Choice(value, previewId, DisplayNameFormatter.resourceLeaf(tag.location().toString()), previews, true);
    }

    private static ItemStack[] tagPreviews(TagKey<Block> tag) {
        List<ItemStack> stacks = new ArrayList<>();
        for (var holder : BuiltInRegistries.BLOCK.getTagOrEmpty(tag)) {
            Block block = holder.value();
            Item item = block.asItem();
            if (item != Items.AIR) {
                stacks.add(new ItemStack(item));
            }
        }
        return stacks.toArray(ItemStack[]::new);
    }

    private static boolean isPickable(Block block) {
        return block != Blocks.AIR && block != Blocks.CAVE_AIR && block != Blocks.VOID_AIR;
    }
}
