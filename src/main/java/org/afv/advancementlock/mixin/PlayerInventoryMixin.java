package org.afv.advancementlock.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.collection.DefaultedList;
import org.afv.advancementlock.AdvancementLock;
import org.afv.advancementlock.limiters.AdvancementUtils;
import org.afv.advancementlock.limiters.SlotLimiter;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

@Mixin(PlayerInventory.class)
public abstract class PlayerInventoryMixin {
    @Final
    @Shadow
    public PlayerEntity player;

    @Shadow
    private int selectedSlot;

    @Shadow public abstract int size();

    @Shadow public abstract ItemStack getStack(int slot);

    @Shadow public abstract void setStack(int slot, ItemStack stack);

    @Shadow protected abstract boolean canStackAddMore(ItemStack existingStack, ItemStack stack);

    @Inject(
            method = "insertStack(ILnet/minecraft/item/ItemStack;)Z", // int, ItemStack → returns boolean
            at = @At("HEAD"),
            cancellable = true
    )
    private void onInsertStack(int slot, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (!(player instanceof ServerPlayerEntity)) return;
        int unlocked = AdvancementUtils.countUnlockedAdvancements((ServerPlayerEntity) player);
        Set<Integer> allowed = SlotLimiter.getAllowedSlots(unlocked);
        AdvancementLock.LOGGER.debug("insertStack, {}", slot);

        if (slot == -1) {
            // Pass 1: merge with existing stacks
            for (int i = 0; i < 36; i++) {
                if (!allowed.contains(i)) continue;

                ItemStack invStack = this.getStack(i);
                if (!stack.isDamaged() && this.canStackAddMore(invStack, stack)) {
                    int max_stack = stack.getMaxCount();
                    int added = Math.min(max_stack - invStack.getCount(), stack.getCount());
                    if (added > 0) {
                        invStack.increment(added);
                        stack.decrement(added);
                        if (stack.isEmpty()) {
                            cir.setReturnValue(true);
                            return;
                        }
                    }
                }
            }

            // Pass 2: put into empty slots
            for (int i = 0; i < 36; i++) {
                if (!allowed.contains(i)) continue;

                ItemStack invStack = this.getStack(i);
                if (invStack.isEmpty()) {
                    this.setStack(i, stack.copy());
                    stack.setCount(0);
                    cir.setReturnValue(true);
                    return;
                }
            }

            // No space available
            // AdvancementLock.LOGGER.info("Has inv? : " + (player.currentScreenHandler != player.playerScreenHandler));
            if (!stack.isEmpty()) {
                cir.setReturnValue(false);
//                if (player instanceof ServerPlayerEntity serverPlayer) {
//                    serverPlayer.dropItem(stack.copy(), false); // drop at feet
//                    stack.setCount(0); // prevent duping
//                }
//                cir.setReturnValue(true); // mark as handled
            }
        }

        // AdvancementLock.LOGGER.info(allowed.toString() + " - Slot: " + slot);
    }
}