package org.afv.advancementlock.client.mixin;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.afv.advancementlock.client.utils.SlotClientUtils;
import org.afv.advancementlock.network.RequestAdvancementCountPayload;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin<T extends ScreenHandler> extends Screen {

    @Shadow
    @Final
    protected T handler;

    @Shadow protected int x; // GUI left
    @Shadow protected int y; // GUI top

    @Unique
    private static final Identifier VANILLA_INVENTORY =
            Identifier.of("minecraft", "textures/gui/container/inventory.png");

    protected HandledScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "drawForeground", at = @At("TAIL"))
    private void onDrawForeground(DrawContext context, int mouseX, int mouseY, CallbackInfo ci) {
        if (this.handler == null) return;

        // Optionally log once to confirm mixin runs (remove in production)
        // AdvancementLock.LOGGER.debug("HandledScreenMixin.onRender for {}", this.getClass().getSimpleName());

        for (Slot slot : this.handler.slots) {
            // Debug: try drawing for ALL slots to confirm rendering works
            // context.fill(slot.x, slot.y, slot.x + 16, slot.y + 16, 0x2200FF00);

            if (SlotClientUtils.isSlotLocked(slot)) {
                HandledScreen<?> screen = (HandledScreen<?>)(Object)this;
                // translucent red overlay
                float texWidth = 256f;
                float texHeight = 256f;

                // region of the texture to copy
                float u1 = 100f / texWidth;
                float u2 = 101f / texWidth;
                float v1 = 70f / texHeight;
                float v2 = 71f / texHeight;

                // draw on top of slot
                context.drawTexturedQuad(
                        VANILLA_INVENTORY,
                        slot.x - 1,
                        slot.y - 1,
                        slot.x + 17,
                        slot.y + 17,
                        u1, u2, v1, v2
                );
            }
        }
    }

    @Inject(method = "renderBackground", at = @At("TAIL"))
    private void onRenderBack(DrawContext context, int mouseX, int mouseY, float deltaTicks, CallbackInfo ci) {
        if (this.handler == null) return;

        // Optionally log once to confirm mixin runs (remove in production)
        // AdvancementLock.LOGGER.debug("HandledScreenMixin.onRender for {}", this.getClass().getSimpleName());

        for (Slot slot : this.handler.slots) {
            // Debug: try drawing for ALL slots to confirm rendering works
            // context.fill(slot.x, slot.y, slot.x + 16, slot.y + 16, 0x2200FF00);

            if (SlotClientUtils.isSlotLocked(slot)) {
                HandledScreen<?> screen = (HandledScreen<?>)(Object)this;
                // translucent red overlay
                float texWidth = 256f;
                float texHeight = 256f;

                // region of the texture to copy
                float u1 = 100f / texWidth;
                float u2 = 101f / texWidth;
                float v1 = 70f / texHeight;
                float v2 = 71f / texHeight;

                // draw on top of slot
                context.drawTexturedQuad(
                        VANILLA_INVENTORY,
                        x + slot.x - 1,
                        y + slot.y - 1,
                        x + slot.x + 17,
                        y + slot.y + 17,
                        u1, u2, v1, v2
                );
            }
        }
    }

    @Inject(method = "init", at = @At("HEAD"))
    private void onOpenInventory(CallbackInfo ci) {
        if (ClientPlayNetworking.canSend(RequestAdvancementCountPayload.ID)) {
            ClientPlayNetworking.send(new RequestAdvancementCountPayload());
        }
    }
}

