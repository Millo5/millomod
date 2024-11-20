package net.millo.millomod.mod.commands.impl;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.millo.millomod.MilloMod;
import net.millo.millomod.mod.commands.ArgBuilder;
import net.millo.millomod.mod.commands.Command;
import net.millo.millomod.mod.features.FeatureHandler;
import net.millo.millomod.mod.features.impl.coding.cache.CacheGUI;
import net.millo.millomod.mod.features.impl.coding.cache.PlotCaching;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.text.Text;

public class CacheCommand extends Command {
    @Override
    public void register(MinecraftClient instance, CommandDispatcher<FabricClientCommandSource> cd, CommandRegistryAccess context) {
        cd.register(ArgBuilder.literal("cache")
                        .then(ArgBuilder.literal("plot").executes(ctx -> {
                            boolean s = ((PlotCaching) FeatureHandler.getFeature("plot_caching")).scanPlot();
                            if (MilloMod.MC.player != null)
                                MilloMod.MC.player.sendMessage(Text.of(s ? "Scanning entire plot" : "Quit scan"), false);
                            return 1;
                        }))
                .executes(ctx -> {
                    new CacheGUI().open();
                    return 1;
                })
        );
    }

    @Override
    public String getKey() {
        return "cache";
    }
}
