package org.afv.advancementlock.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import org.afv.advancementlock.AdvancementLock;
import org.afv.advancementlock.diff.LockDifficulty;
import org.afv.advancementlock.diff.LockDifficultyState;
import org.afv.advancementlock.limiters.AdvancementUtils;
import org.afv.advancementlock.limiters.HealthLimiter;
import org.afv.advancementlock.limiters.SlotLimiter;

import java.util.Set;

public class LockDifficultyCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("lockdifficulty")
            .then(CommandManager.literal("get")
                .requires(source -> source.hasPermissionLevel(2))
                .executes(ctx -> {
                    ServerCommandSource source = ctx.getSource();
                    LockDifficultyState state = LockDifficultyState.getServerState(source.getServer());
                    LockDifficulty diff = state.getDifficulty();

                    source.sendMessage(
                        Text.literal("Current Lock Difficulty: " + diff.getDisplayName())
                    );
                    return 1;
                })
            )
            .then(CommandManager.literal("set")
                .then(CommandManager.argument("difficulty", StringArgumentType.word())
                    .suggests((context, builder) -> {
                        for (LockDifficulty d : LockDifficulty.values()) {
                            builder.suggest(d.name());
                        }
                        return builder.buildFuture();
                    })
                    .executes(ctx -> {
                        ServerCommandSource source = ctx.getSource();
                        String input = StringArgumentType.getString(ctx, "difficulty");
                        LockDifficulty diff;
                        try {
                            diff = LockDifficulty.valueOf(input.toUpperCase());
                        } catch (IllegalArgumentException e) {
                            source.sendError(Text.literal("Invalid difficulty! Use EASY, NORMAL, HARD."));
                            return 0;
                        }

                        LockDifficultyState state = LockDifficultyState.getServerState(source.getServer());
                        state.setDifficulty(diff);
                        AdvancementLock.applyDifficulty(source.getServer());

                        source.sendMessage(Text.literal("Lock Difficulty set to " + diff.getDisplayName()));
                        emptyNewLockedSlots(source);
                        updateAllPlayersHealth(source);
                        return 1;
                    })
                )
            )
            .then(CommandManager.literal("value")
                .executes(ctx -> {
                    ServerCommandSource source = ctx.getSource();

                    source.sendMessage(
                            Text.literal("Current Lock Difficulty Value: " + AdvancementLock.getDiffModifier())
                    );
                    return 1;
                })
            )
        );
    }

    private static void emptyNewLockedSlots(ServerCommandSource source) {
        for (ServerWorld world : source.getServer().getWorlds()) {
            for (ServerPlayerEntity player : world.getPlayers()) {
                PlayerInventory inv = player.getInventory();
                Set<Integer> slots = SlotLimiter.getAllowedSlots(AdvancementUtils.countUnlockedAdvancements(player));
                for (int i = 0; i < 41; i++) {
                    if (!slots.contains(i) && !inv.getStack(i).isEmpty()) {
                        player.dropItem(inv.getStack(i).copy(), false);
                        inv.setStack(i, ItemStack.EMPTY);
                    }
                }
            }
        }
    }

    public static void updateAllPlayersHealth(ServerCommandSource source) {
        for (ServerWorld world : source.getServer().getWorlds()) {
            for (ServerPlayerEntity player : world.getPlayers()) {
                HealthLimiter.updatePlayerHealth(player, AdvancementUtils.countUnlockedAdvancements(player));
            }
        }
    }
}