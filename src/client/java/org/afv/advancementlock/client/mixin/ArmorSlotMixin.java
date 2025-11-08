package org.afv.advancementlock.client.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.slot.Slot;
import org.afv.advancementlock.client.utils.SlotClientUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.screen.slot.ArmorSlot")
public abstract class ArmorSlotMixin {

    @Inject(method = "isEnabled", at = @At("HEAD"), cancellable = true)
    private void onIsEnabled(CallbackInfoReturnable<Boolean> cir) {
        Slot slot = (Slot) (Object) this;

        // Example: disable slot if locked
        if (slot.inventory instanceof PlayerInventory) {
            int index = slot.getIndex();
            if (SlotClientUtils.isSlotLocked(index)) {
                cir.setReturnValue(false); // disables this slot
            }
        }
    }
}