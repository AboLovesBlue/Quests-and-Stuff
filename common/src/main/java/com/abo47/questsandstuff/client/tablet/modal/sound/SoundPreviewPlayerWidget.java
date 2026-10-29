package com.abo47.questsandstuff.client.tablet.modal.sound;

import java.util.function.IntSupplier;
import javax.annotation.Nonnull;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import com.abo47.questsandstuff.client.quest.sound.QuestSoundPreview;
import com.abo47.questsandstuff.client.tablet.icons.IconAtlas;

import com.abo47.questsandstuff.quest.model.QuestDisplay;

import static com.abo47.questsandstuff.client.tablet.theme.tokens.UiThemeTokens.*;
import com.abo47.questsandstuff.client.tablet.modal.TabletModalPanel;

public final class SoundPreviewPlayerWidget extends WidgetGroup {
    private static final int MIN_ICON_SIZE = ICON_22;
    private static final int ICON_PAD = GRID_8;

    private final String soundId;
    private final IntSupplier volumePercent;

    SoundPreviewPlayerWidget(int x, int y, int width, String soundId) {
        this(x, y, width, soundId, () -> QuestDisplay.DEFAULT_COMPLETION_SOUND_VOLUME);
    }

    SoundPreviewPlayerWidget(int x, int y, int width, String soundId, IntSupplier volumePercent) {
        this(x, y, width, 34, soundId, volumePercent);
    }

    public SoundPreviewPlayerWidget(int x, int y, int width, int height, String soundId, IntSupplier volumePercent) {
        super(x, y, width, height);
        this.soundId = soundId == null ? "" : soundId.trim();
        this.volumePercent = volumePercent == null ? () -> QuestDisplay.DEFAULT_COMPLETION_SOUND_VOLUME : volumePercent;
        updateTooltip();
    }

    @Override
    public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        if (soundId.isBlank()) {
            return;
        }
        IGuiTexture skinBg = getBackgroundTexture();
        if (skinBg != null && !skinBg.equals(IGuiTexture.EMPTY)) {
            skinBg.draw(graphics, mouseX, mouseY, getPositionX(), getPositionY(), getSizeWidth(), getSizeHeight());
        }
        int x = getPositionX();
        int y = getPositionY();
        int w = getSizeWidth();
        int h = getSizeHeight();
        boolean playing = QuestSoundPreview.isPlaying(soundId);
        int iconSize = Math.max(MIN_ICON_SIZE, Math.min(w, h) - ICON_PAD);
        int iconX = x + (w - iconSize) / 2;
        int iconY = y + (h - iconSize) / 2;
        var icon = IconAtlas.iconTexture(playing ? "pause" : "play");
        if (icon != null) {
            icon.draw(graphics, mouseX, mouseY, iconX, iconY, iconSize, iconSize);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0 || soundId.isBlank() || !isMouseOverElement(mouseX, mouseY)) {
            return super.mouseClicked(mouseX, mouseY, button);
        }
        QuestSoundPreview.toggle(soundId, volumePercent.getAsInt());
        updateTooltip();
        return true;
    }

    private void updateTooltip() {
        setHoverTooltips(new Component[]{
                Component.literal(TabletModalPanel.tr(QuestSoundPreview.isPlaying(soundId)
                        ? "ui.questsandstuff.common.pause"
                        : "ui.questsandstuff.common.play")),
                Component.literal(soundId)
        });
    }
}
