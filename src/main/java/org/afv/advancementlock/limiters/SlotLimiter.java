package org.afv.advancementlock.limiters;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import org.afv.advancementlock.AdvancementLock;

import java.util.HashSet;
import java.util.Set;

public class SlotLimiter {
    public static Set<Integer> getAllowedSlots(int unlockedAdvancements) {
        Set<Integer> allowed = new HashSet<>();

        // Always unlocked: hotbar 0,1,2
        allowed.add(0);
        allowed.add(1);
        allowed.add(2);
        allowed.add(40);

        // AdvancementLock.LOGGER.info("Total: {}", AdvancementLock.getAdvancementTotal());
        int slotsToUnlock = Math.min(37, (int) ((AdvancementLock.getDiffModifier() * 222 * unlockedAdvancements) / (5 * AdvancementLock.getAdvancementTotal()))); // each advancement = 1 slot for simplicity

        // Progressively unlock
        int hotbarIndex = 3; // start unlocking from hotbar slot 3 (index = 3)
        int invIndex = 9;    // first inventory slot
        int armorIndex = 36; // start from boots

        int hotbarCount = 0;
        int armorCount = 0;
        int slot = 0;
        while (slotsToUnlock > 0) {
            if ((slot - hotbarCount - armorCount) / 4 == hotbarCount + 1) {
                allowed.add(hotbarIndex);
                hotbarIndex++;
                hotbarCount++;
            } else if ((slot - hotbarCount - armorCount) / 7 == armorCount + 1) {
                allowed.add(armorIndex);
                armorIndex++;
                armorCount++;
            } else if (slot == 36) {
                allowed.add(armorIndex);
            } else {
                allowed.add(invIndex);
                invIndex++;
            }
            slot++;
            slotsToUnlock--;
        }

        return allowed;
    }

    public static boolean isSlotLocked(Slot slot, PlayerEntity player) {
        if (!(slot.inventory instanceof PlayerInventory)) return false;
        return !getAllowedSlots(AdvancementUtils.countUnlockedAdvancements((ServerPlayerEntity) player)).contains(slot.getIndex());
    }

    public static boolean isSlotLocked(int slot, PlayerEntity player) {
        return !getAllowedSlots(AdvancementUtils.countUnlockedAdvancements((ServerPlayerEntity) player)).contains(slot);
    }
}
