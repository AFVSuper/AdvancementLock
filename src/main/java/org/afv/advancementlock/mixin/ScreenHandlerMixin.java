package org.afv.advancementlock.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.collection.DefaultedList;
import org.afv.advancementlock.AdvancementLock;
import org.afv.advancementlock.limiters.AdvancementUtils;
import org.afv.advancementlock.limiters.SlotLimiter;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Set;

@Mixin(ScreenHandler.class)
public abstract class ScreenHandlerMixin {
    @Shadow
    @Final
    public DefaultedList<Slot> slots;

    @Shadow
    public abstract ItemStack getCursorStack();

    @Redirect(
            method = "insertItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/screen/slot/Slot;canInsert(Lnet/minecraft/item/ItemStack;)Z"
            )
    )
    private boolean redirectCanInsert(Slot slot, ItemStack stack) {
        // Normal check first
        if (!slot.canInsert(stack)) {
            return false;
        }

        // Extra: is this slot locked?
        if (slot.inventory instanceof PlayerInventory playerInv) {
            if (playerInv.player instanceof ServerPlayerEntity serverPlayer) {
                int unlocked = AdvancementUtils.countUnlockedAdvancements(serverPlayer);
                Set<Integer> allowed = SlotLimiter.getAllowedSlots(unlocked);

                return allowed.contains(slot.getIndex()); // skip locked slots
            }
        }

        return true;
    }

    @Inject(method = "onSlotClick", at = @At("HEAD"), cancellable = true)
    private void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player, CallbackInfo ci) {
        AdvancementLock.LOGGER.debug("onSlotClick");
        if (slotIndex >= 0 && slotIndex < this.slots.size()) {
            Slot slot = this.slots.get(slotIndex);

            // Check if this slot is locked
            if (!(player instanceof ServerPlayerEntity)) return;
            AdvancementLock.LOGGER.debug("onSlotClick 2");
            if (actionType == SlotActionType.SWAP) {

                AdvancementLock.LOGGER.debug("onSlotClick SWAP, {}, {}", slot.getIndex(), button);
                if (SlotLimiter.isSlotLocked(slot, player) || SlotLimiter.isSlotLocked(button, player)) {
                    AdvancementLock.LOGGER.debug("onSlotClick 3");
                    ci.cancel();
                }
            } else if (SlotLimiter.isSlotLocked(slot, player)) {
                AdvancementLock.LOGGER.debug("onSlotClick 3");
                ci.cancel(); // cancel the swap
            }
        }
    }

    @Inject(method = "offerOrDropStack", at = @At("HEAD"), cancellable = true)
    private static void redirectOfferOrDropStack(PlayerEntity player, ItemStack stack, CallbackInfo ci) {
        // AdvancementLock.LOGGER.info("redirectOffer");
        if (!(player instanceof ServerPlayerEntity serverPlayer)) {
            // fallback: drop stack
            player.dropItem(stack, false);
            return;
        }

        PlayerInventory inv = player.getInventory();
        Set<Integer> allowed = SlotLimiter.getAllowedSlots(AdvancementUtils.countUnlockedAdvancements(serverPlayer));
        allowed.remove(40);
        // AdvancementLock.LOGGER.info(allowed.toString());

        // First, try to merge with existing stacks in allowed slots
        for (int i : allowed) {
            ItemStack invStack = inv.getStack(i);
            if (!invStack.isEmpty() && ItemStack.areItemsEqual(stack, invStack) && invStack.getCount() < invStack.getMaxCount()) {
                int add = Math.min(stack.getCount(), invStack.getMaxCount() - invStack.getCount());
                invStack.increment(add);
                stack.decrement(add);
                if (stack.isEmpty()) return;
            }
        }

        // Then, place in empty allowed slots
        for (int i : allowed) {
            ItemStack invStack = inv.getStack(i);
            if (invStack.isEmpty()) {
                inv.setStack(i, stack.copy());
                stack.setCount(0);
                return;
            }
        }

        // If some still remains, drop it
        if (!stack.isEmpty()) {
            player.dropItem(stack.copy(), false);
            stack.setCount(0);
        }

        ci.cancel();
    }
}