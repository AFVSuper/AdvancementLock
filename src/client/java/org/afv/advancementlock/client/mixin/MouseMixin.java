package org.afv.advancementlock.client.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.entity.player.PlayerInventory;
import org.afv.advancementlock.client.utils.SlotClientUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public class MouseMixin {
    @Shadow @Final
    private MinecraftClient client;

    @Inject(method = "onMouseScroll", at = @At("HEAD"), cancellable = true)
    private void onMouseScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        if (this.client.player == null) return;

        if (this.client.currentScreen != null || vertical == 0) return;

        PlayerInventory inv = this.client.player.getInventory();
        int direction = (vertical > 0 || horizontal > 0) ? -1 : 1; // vanilla scroll: up = -1, down = +1

        int next = inv.getSelectedSlot();

        // Try up to 9 times to find next unlocked slot
        for (int i = 0; i < PlayerInventory.getHotbarSize(); i++) {
            next = (next + direction + PlayerInventory.getHotbarSize()) % PlayerInventory.getHotbarSize();
            if (!SlotClientUtils.isSlotLocked(next)) {
                inv.setSelectedSlot(next);
                break;
            }
        }

        ci.cancel(); // cancel vanilla scrolling logic
    }
}