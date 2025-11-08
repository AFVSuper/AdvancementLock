package org.afv.advancementlock.client.config;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class IconButton extends ButtonWidget {
    private final Identifier texture;

    public IconButton(int x, int y, int size, Identifier texture, PressAction onPress) {
        super(x, y, size, size, Text.empty(), onPress, DEFAULT_NARRATION_SUPPLIER);
        this.texture = texture;
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        boolean hovered = this.isMouseOver(mouseX, mouseY);
        super.renderWidget(context, mouseX, mouseY, delta);

        // Draw icon centered inside the button
        int iconX = this.getX() + (this.getWidth() - 16) / 2;  // assuming icon 16x16
        int iconY = this.getY() + (this.getHeight() - 16) / 2;
        context.drawTexture(RenderPipelines.GUI_TEXTURED ,texture, iconX, iconY, 0, 0, 16, 16, 16, 16);
    }

    private String id;
    public void setId(String id) { this.id = id; }
    public String getId() { return id; }
//
//    @Override
//    public void playDownSound(MinecraftClient client) {
//        client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
//    }
}