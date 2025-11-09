package org.afv.advancementlock;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.afv.advancementlock.config.ModConfig;
import org.afv.advancementlock.diff.LockDifficulty;
import org.afv.advancementlock.diff.LockDifficultyState;
import org.afv.advancementlock.init.ModCommands;
import org.afv.advancementlock.limiters.AdvancementUtils;
import org.afv.advancementlock.limiters.HealthLimiter;
import org.afv.advancementlock.network.AdvancementCountResponsePayload;
import org.afv.advancementlock.network.AdvancementNetworking;
import org.afv.advancementlock.network.AdvancementTotalPayload;
import org.afv.advancementlock.network.RequestAdvancementCountPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

public class AdvancementLock implements ModInitializer {
    public static final String ModID = "advancementlock";
    public static final Logger LOGGER = LoggerFactory.getLogger(ModID);
    private static int AdvancementTotal;

    public static ModConfig CONFIG;

    private static double diffModifier = 1.5;     // default for Normal

    @Override
    public void onInitialize() {
        AutoConfig.register(ModConfig.class, GsonConfigSerializer::new);
        CONFIG = AutoConfig.getConfigHolder(ModConfig.class).getConfig();

        ServerLifecycleEvents.SERVER_STARTED.register(this::advancementCounter);

        PayloadTypeRegistry.playS2C().register(AdvancementCountResponsePayload.ID, AdvancementCountResponsePayload.CODEC);
        PayloadTypeRegistry.playC2S().register(RequestAdvancementCountPayload.ID, RequestAdvancementCountPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(RequestAdvancementCountPayload.ID,
                (payload, context) -> {
                    ServerPlayerEntity player = context.player();
                    int count = AdvancementUtils.countUnlockedAdvancements(player);

                    // Send response
                    ServerPlayNetworking.send(player, new AdvancementCountResponsePayload(count));
                }
        );

        PayloadTypeRegistry.playS2C().register(
                AdvancementTotalPayload.ID,
                AdvancementTotalPayload.CODEC
        );

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            ModCommands.register(dispatcher);
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.player;
            // Your custom method
            AdvancementNetworking.sendAdvancementTotal(player);
            onPlayerJoin(player);
        });

        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            // Reapply max health modifier after respawn
            int unlocked = AdvancementUtils.countUnlockedAdvancements(newPlayer);
            HealthLimiter.updatePlayerHealth(newPlayer, unlocked);
        });

        ServerWorldEvents.LOAD.register((server, world) -> {
            applyDifficulty(server);
        });
    }

    public static void applyDifficulty(MinecraftServer server) {
        LockDifficultyState state = LockDifficultyState.getServerState(server);
        LockDifficulty difficulty = state.getDifficulty();

        switch (difficulty) {
            case HARD -> diffModifier = 1.0;
            case NORMAL -> diffModifier = 1.5;
            case EASY -> diffModifier = 2.0;
        }
    }

    private void advancementCounter(MinecraftServer server) {
        Collection<AdvancementEntry> all = server.getAdvancementLoader().getAdvancements();

        // Only keep ones with a display (i.e., shown in tabs)
        long visible = all.stream()
                .map(AdvancementEntry::value)
                .filter(adv -> adv.display().isPresent()) // must have display
                .count();

        AdvancementTotal = (int) visible;
        LOGGER.info("Found {} visible advancements (tab entries).", visible);
        LOGGER.info("{}", getAdvancementTotal());
    }

    public void onPlayerJoin(PlayerEntity player) {
        if (player instanceof ServerPlayerEntity serverPlayer) {
            HealthLimiter.updatePlayerHealth(serverPlayer, AdvancementUtils.countUnlockedAdvancements(serverPlayer));
            AdvancementNetworking.sendAdvancementCount(serverPlayer);
        }
    }

    public static double getDiffModifier() {
        return diffModifier;
    }
    public static int getAdvancementTotal() { return AdvancementTotal; }
    public static void setServerAdvancementTotal(int total) { AdvancementTotal = total; }
}
