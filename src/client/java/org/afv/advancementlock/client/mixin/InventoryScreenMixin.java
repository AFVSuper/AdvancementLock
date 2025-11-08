package org.afv.advancementlock.client.mixin;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.afv.advancementlock.client.utils.SlotClientUtils;
import org.afv.advancementlock.network.RequestAdvancementCountPayload;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin extends HandledScreen<PlayerScreenHandler> {

    @Unique
    private static final Identifier VANILLA_INVENTORY =
            Identifier.of("minecraft", "textures/gui/container/inventory.png");

    public InventoryScreenMixin(PlayerScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Inject(method = "drawForeground", at = @At("TAIL"))
    private void onDrawForeground(DrawContext context, int mouseX, int mouseY, CallbackInfo ci) {
        for (Slot slot : this.handler.slots) {
            if (SlotClientUtils.isSlotLocked(slot)) {
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

    @Inject(method = "init", at = @At("HEAD"))
    private void onOpenInventory(CallbackInfo ci) {
        if (ClientPlayNetworking.canSend(RequestAdvancementCountPayload.ID)) {
            ClientPlayNetworking.send(new RequestAdvancementCountPayload());
        }
    }
}
