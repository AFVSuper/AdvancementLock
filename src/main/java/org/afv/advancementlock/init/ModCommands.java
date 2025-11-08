package org.afv.advancementlock.init;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.ServerCommandSource;
import org.afv.advancementlock.command.DebugCommand;

public class ModCommands {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        DebugCommand.register(dispatcher);
    }
}