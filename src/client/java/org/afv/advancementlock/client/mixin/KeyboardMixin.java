package org.afv.advancementlock.client.mixin;

import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import org.afv.advancementlock.client.utils.SlotClientUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public class KeyboardMixin {
    @Shadow
    @Final
    private net.minecraft.client.MinecraftClient client;

    @Inject(method = "onKey", at = @At("HEAD"), cancellable = true)
    private void onKey(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
        MinecraftClient client = this.client;
        if (client.player == null) return;

        // Hotbar key detection
        for (int i = 0; i < 9; i++) {
            if (key == client.options.hotbarKeys[i].getDefaultKey().getCode() && action == 1) {
                if (SlotClientUtils.isSlotLocked(i)) {
                    ci.cancel(); // block the hotbar change
                }
            }
        }
    }
}