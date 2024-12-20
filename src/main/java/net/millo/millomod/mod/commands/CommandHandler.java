package net.millo.millomod.mod.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.millo.millomod.mod.commands.impl.*;
import net.millo.millomod.mod.commands.impl.savestate.RedevCommand;
import net.millo.millomod.mod.commands.impl.savestate.SaveCommand;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandRegistryAccess;

import java.util.ArrayList;
import java.util.List;

public class CommandHandler {
    private static final List<Command> cmds = new ArrayList<>();

    public static List<Command> getCommands() {
        return cmds;
    }

    public static void load(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess context) {
        cmds.clear();
        register(dispatcher, context,
                new SettingsCommand(),
                new RedevCommand(),
                new SaveCommand(),
                new ColorsCommand(),
                new MilloModCommand(),
                new CacheCommand(),
                new DfGiveCommand(),
                new ActionDumpCommand()
        );
    }

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess context, Command cmd) {
        cmd.register(MinecraftClient.getInstance(), dispatcher, context);
        cmds.add(cmd);
    }
    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess context, Command... cmds) {
        for (Command cmd : cmds) {
            register(dispatcher, context, cmd);
        }
    }
}
