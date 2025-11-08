package org.afv.advancementlock.client.mixin;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.component.type.EquippableComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import org.afv.advancementlock.client.utils.SlotClientUtils;
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
        if (!(player instanceof ClientPlayerEntity)) return;
        int armorIndex = 36 + this.slot.getEntitySlotId(); // this.slot is the target EquipmentSlot

        if (SlotClientUtils.isSlotLocked(armorIndex)) {
            // Block equip and keep item in hand
            cir.setReturnValue(ActionResult.FAIL);
        }
    }
}