package com.abo47.questsandstuff.client.tablet.app;

import net.minecraft.resources.ResourceLocation;

public record TabletAppDescriptor(String id, String translationKey, ResourceLocation iconTexture, String iconKey, TabletAppComposer composer) {
    public TabletAppDescriptor(String id, String translationKey, ResourceLocation iconTexture, TabletAppComposer composer) {
        this(id, translationKey, iconTexture, null, composer);
    }
}
