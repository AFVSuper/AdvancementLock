package org.afv.advancementlock.client.mixin;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.slot.Slot;
import org.afv.advancementlock.client.utils.SlotClientUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(Slot.class)
public class SlotMixin {
    /**
     * @author AFVSuper
     * @reason Slot rendering rework for mod
     */
    @Overwrite
    public boolean isEnabled() {
        Slot slot = (Slot) (Object) this;
        if (!(slot.inventory instanceof PlayerInventory)) return true;
        return !SlotClientUtils.isSlotLocked(slot);
    }
}
