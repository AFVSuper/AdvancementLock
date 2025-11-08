package org.afv.advancementlock.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import org.afv.advancementlock.AdvancementLock;
import org.afv.advancementlock.limiters.AdvancementUtils;

public class AdvancementNetworking {
    public static void sendAdvancementCount(ServerPlayerEntity player) {
        int unlocked = AdvancementUtils.countUnlockedAdvancements(player);

        AdvancementCountResponsePayload payload = new AdvancementCountResponsePayload(unlocked);
        ServerPlayNetworking.send(player, payload);

        AdvancementLock.LOGGER.debug("Sent packet to {}: {}", player.getName().getString(), unlocked);
    }

    public static void sendAdvancementTotal(ServerPlayerEntity player) {
        int total = AdvancementLock.getAdvancementTotal();
        ServerPlayNetworking.send(player, new AdvancementTotalPayload(total));
    }
}
