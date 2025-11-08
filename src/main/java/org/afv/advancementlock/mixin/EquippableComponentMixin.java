package org.afv.advancementlock.mixin;

import net.minecraft.component.type.EquippableComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import org.afv.advancementlock.limiters.SlotLimiter;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EquippableComponent.class)
public abstract class EquippableComponentMixin {
    @Final
    @Shadow
    private EquipmentSlot slot;

    @Inject(method = "equip", at = @At("HEAD"), cancellable = true)
    private void onEquip(ItemStack stack, PlayerEntity player, CallbackInfoReturnable<ActionResult> cir) {
        if (!(player instanceof ServerPlayerEntity serverPlayer)) return;
        int armorIndex = 36 + this.slot.getEntitySlotId(); // this.slot is the target EquipmentSlot

        if (SlotLimiter.isSlotLocked(armorIndex, serverPlayer)) {
            // Block equip and keep item in hand
            cir.setReturnValue(ActionResult.FAIL);
        }
    }
}