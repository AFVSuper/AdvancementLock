package org.afv.advancementlock.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.afv.advancementlock.limiters.AdvancementUtils;

public class DebugCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                CommandManager.literal("mod-debug")
                        .requires(source -> source.hasPermissionLevel(2))
                        .then(CommandManager.literal("adcount")
                                .executes(DebugCommand::getCount)));
    }

    private static int getCount(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        player.sendMessage(player.getName().copy().append(Text.literal(" has " +
                AdvancementUtils.countUnlockedAdvancements(player) + " advancements.")), false);
        return Command.SINGLE_SUCCESS;
    }
}
