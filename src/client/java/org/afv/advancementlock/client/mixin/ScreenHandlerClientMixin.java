package org.afv.advancementlock.client.mixin;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.collection.DefaultedList;
import org.afv.advancementlock.AdvancementLock;
import org.afv.advancementlock.client.utils.SlotClientUtils;
import org.afv.advancementlock.limiters.SlotLimiter;
import org.afv.advancementlock.mixin.PlayerScreenHandlerAccessor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ScreenHandler.class)
public abstract class ScreenHandlerClientMixin {
    @Shadow
    @Final
    public DefaultedList<Slot> slots;

    @Shadow
    public abstract Slot getSlot(int index);

    @Inject(
        method = "insertItem",
        at = @At("HEAD"),
        cancellable = true
    )
    private void onInsertItem(ItemStack stack, int startIndex, int endIndex, boolean fromLast, CallbackInfoReturnable<Boolean> cir) {
        ScreenHandler self = (ScreenHandler)(Object)this;

        // Try to get player
        if (self instanceof PlayerScreenHandler playerHandler) {
            PlayerEntity player = ((PlayerScreenHandlerAccessor) playerHandler).invokeGetPlayer();
            
            if (player instanceof ClientPlayerEntity) {
                // Loop through slots where insertion might happen
                for (int i = startIndex; i < endIndex; i++) {
                    if (SlotClientUtils.isSlotLocked(getSlot(i))) {
                        cir.setReturnValue(false); // block completely
                        return;
                    }
                }
            }
        }
    }

    @Inject(method = "onSlotClick", at = @At("HEAD"), cancellable = true)
    private void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player, CallbackInfo ci) {
        if (!(player instanceof ClientPlayerEntity)) return;

        if (slotIndex < 0 || slotIndex >= this.slots.size()) return;
        Slot slot = this.slots.get(slotIndex);

        if (actionType == SlotActionType.SWAP) {
            if (button >= this.slots.size()) return;
            Slot hotbarSlot = this.slots.get(button);
            if (SlotClientUtils.isSlotLocked(slot) || SlotClientUtils.isSlotLocked(hotbarSlot)) {
                ci.cancel();

                // Revert cursor stack to what it was
                player.currentScreenHandler.setCursorStack(player.currentScreenHandler.getCursorStack().copy());
            }
        } else if (SlotClientUtils.isSlotLocked(slot)) {
            ci.cancel();
            player.currentScreenHandler.setCursorStack(player.currentScreenHandler.getCursorStack().copy());
        }
    }
}