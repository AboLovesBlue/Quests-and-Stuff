package com.abo47.questsandstuff.client.tablet.ui;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.modular.ModularUIGuiContainer;

import com.abo47.questsandstuff.client.tablet.quest.canvas.render.CanvasWorldPortalCapture;
import com.abo47.questsandstuff.client.tablet.ui.state.ActiveState;

public final class GuiContainer extends ModularUIGuiContainer {
    public GuiContainer(ModularUI modularUI, int windowId) {
        super(modularUI, windowId);
    }

    @Override
    public void render(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        RenderSystem.clear(GL11.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);
        CanvasWorldPortalCapture.capture(ActiveState.getActiveTabletState());
        super.render(graphics, mouseX, mouseY, partialTicks);
    }

    @Override
    public void removed() {
        CanvasWorldPortalCapture.dispose();
        super.removed();
    }

    @Override
    public void renderFloatingItem(GuiGraphics graphics, ItemStack stack, int x, int y, @Nullable String amountText) {
    }
}
