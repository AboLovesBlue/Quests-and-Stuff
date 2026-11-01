package com.abolovesblue.questsandstuff.quest.model.task;

import com.mojang.serialization.Codec;

import net.minecraft.resources.ResourceLocation;

public record QuestTaskType<T extends QuestTaskDefinition>(
        ResourceLocation id,
        Codec<T> codec,
        String widgetFactoryId
) {
}
