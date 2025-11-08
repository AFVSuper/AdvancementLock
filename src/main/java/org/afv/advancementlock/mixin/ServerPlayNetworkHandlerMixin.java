package org.afv.advancementlock.mixin;

import net.minecraft.item.ItemStack;
import net.minecraft.network.NetworkThreadUtils;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import org.afv.advancementlock.limiters.SlotLimiter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {
    @Shadow
    public ServerPlayerEntity player;

    @Inject(method = "onPlayerAction", at = @At("HEAD"), cancellable = true)
    private void onSwap(PlayerActionC2SPacket packet, CallbackInfo ci) {
        NetworkThreadUtils.forceMainThread(packet, (ServerPlayNetworkHandler) (Object) this, this.player.getWorld());
        if (this.player.isLoaded()) {
            PlayerActionC2SPacket.Action action = packet.getAction();
            if (Objects.requireNonNull(action) == PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND) {
                if (SlotLimiter.isSlotLocked(player.getInventory().getSelectedSlot(), player)) {
                    ci.cancel();
                }
            }
        }
    }
}
