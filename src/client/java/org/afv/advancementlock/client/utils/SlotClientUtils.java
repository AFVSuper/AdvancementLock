package org.afv.advancementlock.client.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.slot.Slot;
import net.minecraft.world.GameMode;
import org.afv.advancementlock.AdvancementLock;
import org.afv.advancementlock.client.AdvancementLockClient;
import org.afv.advancementlock.limiters.SlotLimiter;


public class SlotClientUtils {
    public static boolean isSlotLocked(Slot slot) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return true; // fail-safe

        if (!(slot.inventory instanceof PlayerInventory)
                || player.getGameMode() == GameMode.CREATIVE
                || player.getGameMode() == GameMode.SPECTATOR
        ) {
            // Not from player inventory
            return false;
        }

        int unlocked = AdvancementLockClient.getAdvancementCount();

        // AdvancementLock.LOGGER.info(SlotLimiter.getAllowedSlots(unlocked).toString());
        return !SlotLimiter.getAllowedSlots(unlocked).contains(slot.getIndex());
    }

    public static boolean isSlotLocked(int slot) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return true; // fail-safe

        int unlocked = AdvancementLockClient.getAdvancementCount();

        // AdvancementLock.LOGGER.info(SlotLimiter.getAllowedSlots(unlocked).toString());
        return !SlotLimiter.getAllowedSlots(unlocked).contains(slot);
    }
}
