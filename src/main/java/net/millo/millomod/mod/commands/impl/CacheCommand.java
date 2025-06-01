package net.millo.millomod.mod.commands.impl;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.millo.millomod.MilloMod;
import net.millo.millomod.mod.commands.ArgBuilder;
import net.millo.millomod.mod.commands.Command;
import net.millo.millomod.mod.features.FeatureHandler;
import net.millo.millomod.mod.features.impl.coding.cache.CacheGUI;
import net.millo.millomod.mod.features.impl.coding.cache.CachedPlot;
import net.millo.millomod.mod.features.impl.coding.cache.PlotCaching;
import net.millo.millomod.mod.util.gui.GUIStyles;
import net.millo.millomod.system.FileManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class CacheCommand extends Command {
    @Override
    public void register(MinecraftClient instance, CommandDispatcher<FabricClientCommandSource> cd, CommandRegistryAccess context) {
        cd.register(ArgBuilder.literal("cache")
                        .then(ArgBuilder.literal("plotold")
                                        .then(ArgBuilder.literal("mega").executes(ctx -> {
                                            boolean s = ((PlotCaching) FeatureHandler.getFeature("plot_caching")).scanPlotOld("mega");
                                            if (MilloMod.MC.player != null)
                                                MilloMod.MC.player.sendMessage(Text.of(s ? "Scanning entire plot" : "Quit scan"), false);
                                            return 1;
                                        }))
                                        .then(ArgBuilder.literal("massive").executes(ctx -> {
                                            boolean s = ((PlotCaching) FeatureHandler.getFeature("plot_caching")).scanPlotOld("massive");
                                            if (MilloMod.MC.player != null)
                                                MilloMod.MC.player.sendMessage(Text.of(s ? "Scanning entire plot" : "Quit scan"), false);
                                            return 1;
                                        }))
                                        .then(ArgBuilder.literal("large").executes(ctx -> {
                                            boolean s = ((PlotCaching) FeatureHandler.getFeature("plot_caching")).scanPlotOld("large");
                                            if (MilloMod.MC.player != null)
                                                MilloMod.MC.player.sendMessage(Text.of(s ? "Scanning entire plot" : "Quit scan"), false);
                                            return 1;
                                        }))
                                        .then(ArgBuilder.literal("basic").executes(ctx -> {
                                            boolean s = ((PlotCaching) FeatureHandler.getFeature("plot_caching")).scanPlotOld("basic");
                                            if (MilloMod.MC.player != null)
                                                MilloMod.MC.player.sendMessage(Text.of(s ? "Scanning entire plot" : "Quit scan"), false);
                                            return 1;
                                        }))
                        )
                        .then(ArgBuilder.literal("plot").executes(ctx -> {
                            boolean s = ((PlotCaching) FeatureHandler.getFeature("plot_caching")).scanPlot();
                            if (MilloMod.MC.player != null)
                                MilloMod.MC.player.sendMessage(Text.of(s ? "Scanning entire plot" : "Quit scan"), false);
                            return 1;
                        }))
                        .then(ArgBuilder.literal("gui").executes(ctx -> {
                            new CacheGUI().open();
                            return 1;
                        }))
                        .then(ArgBuilder.literal("list").executes(ctx -> {
                            if (MilloMod.MC.player == null) return 1;
                            MilloMod.MC.player.sendMessage(Text.of("Cached plots: "), false);
                            ((PlotCaching) FeatureHandler.getFeature("plot_caching")).getCachedPlots().forEach(id -> {
                                MilloMod.MC.player.sendMessage(Text.of(" - " + id), false);
                            });
                            return 1;
                        }))
                        .then(ArgBuilder.literal("diff")
                                .then(ArgBuilder.argument("id1", IntegerArgumentType.integer())
                                        .then(ArgBuilder.argument("id2", IntegerArgumentType.integer())
                                                .executes(ctx -> {
                                                    int id1 = IntegerArgumentType.getInteger(ctx, "id1");
                                                    int id2 = IntegerArgumentType.getInteger(ctx, "id2");
                                                    diff(id1, id2);
                                                    return 1;
                                                })
                                        )
                                )
                        )
                        .then(ArgBuilder.literal("export")
                                .then(ArgBuilder.argument("id", IntegerArgumentType.integer())
                                        .executes(ctx -> {
                                            int id = IntegerArgumentType.getInteger(ctx, "id");
                                            export(id);
                                            return 1;
                                        })
                                )
                        )
                        .then(ArgBuilder.literal("clear")
                                .then(ArgBuilder.argument("id", IntegerArgumentType.integer())
                                        .executes(ctx -> {
                                            int id = IntegerArgumentType.getInteger(ctx, "id");
                                            FileManager.clearCachedPlot(id);
                                            MilloMod.MC.player.sendMessage(Text.of("Cleared plot " + id), false);
                                            return 1;
                                        })
                                )
                        )
                        .then(ArgBuilder.literal("folder")
                                .executes(ctx -> {
                                    FileManager.openCachedPlotsFolder();
                                    return 1;
                                })
                        )
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

    private void export(int id) {
        CachedPlot plot;
        try {
            plot = new CachedPlot(id);
        } catch (Exception e) {
            MilloMod.MC.player.sendMessage(Text.of("Plot " + id + " is not cached"), false);
            return;
        }

        FileManager.clearExportedPlot(id);
        plot.getMethodNames().forEach(method -> {
            String data = plot.getMethodData(method);
            FileManager.writeExportedPlot(id, method, data);
        });

        MilloMod.MC.player.sendMessage(Text.of("Exported plot " + id + " (" + plot.getMethodNames().size() + ")"), false);

    }

    private static void diff(int id1, int id2) {
        ArrayList<Integer> plots = FileManager.getPlotsCached();
        ClientPlayerEntity player = MilloMod.MC.player;
        if (player == null) return;

        if (!plots.contains(id1)) {
            player.sendMessage(Text.of("Plot " + id1 + " is not cached"), false);
            return;
        }
        if (!plots.contains(id2)) {
            player.sendMessage(Text.of("Plot " + id2 + " is not cached"), false);
            return;
        }

        CachedPlot plotA = new CachedPlot(id1);
        CachedPlot plotB = new CachedPlot(id2);

        // print method data of first methods
        List<String> methodsA = plotA.getMethodNames();
        List<String> methodsB = plotB.getMethodNames();

        player.sendMessage(Text.of("Method count: " + methodsA.size() + " vs " + methodsB.size()), false);

        FileManager.clearDiff();

        while (!methodsA.isEmpty()) {
            String method = methodsA.removeFirst();
            String dataA = plotA.getMethodData(method);
            String dataB = plotB.getMethodData(method);
            if (dataB == null) {
                removed(player, method, dataA);
                continue;
            }
            if (!dataA.equals(dataB)) {
                changed(player, method, dataA, dataB);
            }

            // remove method from B
            methodsB.remove(method);
        }

        // print method data of new methods
        while (!methodsB.isEmpty()) {
            String method = methodsB.removeFirst();
            String dataB = plotB.getMethodData(method);
            added(player, method, dataB);
        }
    }

    private static void changed(ClientPlayerEntity player, String method, String oldData, String newData) {
        player.sendMessage(Text.literal("Method " + method + " got changed").setStyle(GUIStyles.CHANGED.getStyle()), false);

//        System.out.println("OLD: " + oldData);
//        System.out.println("NEW: " + newData);

        List<String> oldLines = List.of(oldData.split("\n"));
        List<String> newLines = List.of(newData.split("\n"));

        StringBuilder unified = new StringBuilder();
        int oldIndex = 0, newIndex = 0;

        while (oldIndex < oldLines.size() || newIndex < newLines.size()) {
            if (oldIndex < oldLines.size() && newIndex < newLines.size()) {
                if (oldLines.get(oldIndex).equals(newLines.get(newIndex))) {
                    unified.append("  ").append(oldLines.get(oldIndex)).append("\n");
                    oldIndex++;
                    newIndex++;
                } else {
                    // find the next common line
//                    int nextOld = oldIndex + 1;
//                    int nextNew = newIndex + 1;
//                    while (nextOld < oldLines.size() && nextNew < newLines.size() &&
//                            !oldLines.get(nextOld).equals(newLines.get(nextNew))) {
//                        nextOld++;
//                        nextNew++;
//                    }
//
//                    // add all removed lines
//                    while (oldIndex < nextOld) {
//                        unified.append("- ").append(oldLines.get(oldIndex)).append("\n");
//                        oldIndex++;
//                    }
//
//                    // add all added lines
//                    while (newIndex < nextNew) {
//                        unified.append("+ ").append(newLines.get(newIndex)).append("\n");
//                        newIndex++;
//                    }
//
//                    // update indices
//                    oldIndex = nextOld;
//                    newIndex = nextNew;

                    int nextOld = oldIndex;
                    int nextNew = newIndex;
                    boolean foundMatch = false;

// Search for the next common line
                    while (nextOld < oldLines.size() && !foundMatch) {
                        for (int j = nextNew; j < newLines.size(); j++) {
                            if (oldLines.get(nextOld).equals(newLines.get(j))) {
                                nextNew = j;
                                foundMatch = true;
                                break;
                            }
                        }
                        if (!foundMatch) nextOld++;
                    }

// Add all removed lines until the next common line in oldLines
                    while (oldIndex < nextOld) {
                        unified.append("- ").append(oldLines.get(oldIndex)).append("\n");
                        oldIndex++;
                    }

// Add all added lines until the next common line in newLines
                    while (newIndex < nextNew) {
                        unified.append("+ ").append(newLines.get(newIndex)).append("\n");
                        newIndex++;
                    }

// If a match was found, update indices
                    if (foundMatch) {
                        oldIndex = nextOld;
                        newIndex = nextNew;
                    }

                }
            } else if (oldIndex < oldLines.size()) {
                unified.append("- ").append(oldLines.get(oldIndex)).append("\n");
                oldIndex++;
            } else {
                unified.append("+ ").append(newLines.get(newIndex)).append("\n");
                newIndex++;
            }
        }

        FileManager.writeDiff(method, unified.toString());

    }

    private static void added(ClientPlayerEntity player, String method, String data) {
        player.sendMessage(Text.literal("Method " + method + " got added").setStyle(GUIStyles.ADDED.getStyle()), false);

        // add "+ " to each line
        List<String> lines = List.of(data.split("\n"));
        StringBuilder sb = new StringBuilder();
        lines.forEach(line -> sb.append("+ ").append(line).append("\n"));
        FileManager.writeDiff(method, sb.toString());
    }

    private static void removed(ClientPlayerEntity player, String method, String data) {
        player.sendMessage(Text.literal("Method " + method + " got removed").setStyle(GUIStyles.REMOVED.getStyle()), false);

        // add "- " to each line
        List<String> lines = List.of(data.split("\n"));
        StringBuilder sb = new StringBuilder();
        lines.forEach(line -> sb.append("- ").append(line).append("\n"));
        FileManager.writeDiff(method, sb.toString());
    }


}










