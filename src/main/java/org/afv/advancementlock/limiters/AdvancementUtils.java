package org.afv.advancementlock.limiters;

import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.afv.advancementlock.network.AdvancementNetworking;

public class AdvancementUtils {
    public static int countUnlockedAdvancements(ServerPlayerEntity player) {
        var tracker = player.getAdvancementTracker();
        MinecraftServer server = player.getServer();
        if (server == null) return -1;
        return (int) server.getAdvancementLoader().getAdvancements().stream()
                .filter(entry -> entry.value().display().isPresent()) // proper ones
                .filter(entry -> tracker.getProgress(entry).isDone())
                .count();
    }

    public static void onAdvancementGranted(ServerPlayerEntity player, AdvancementEntry advancement) {
        if (advancement.value().display().isPresent()) {
            AdvancementNetworking.sendAdvancementCount(player);
            HealthLimiter.onAdvancementGranted(player);
        }
    }
}
