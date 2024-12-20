package net.millo.millomod.mod.features.impl.util;

import net.millo.millomod.MilloMod;
import net.millo.millomod.mod.Callback;
import net.millo.millomod.mod.features.Feature;
import net.millo.millomod.mod.features.HandlePacket;
import net.millo.millomod.mod.hypercube.Plot;
import net.millo.millomod.system.FileManager;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Tracker extends Feature {

    private static double x, z;
    private static Sequence step = Sequence.WAIT_FOR_CLEAR;
    private static boolean requestPlotId = false;
    private static final ArrayList<Callback> plotIdCallbacks = new ArrayList<>();
    private static Plot plot;


    public Tracker() {
        plot = Plot.spawn();
    }


    public enum Mode {
        PLAY, DEV, BUILD, SPAWN
    }


    public static Mode mode = Mode.SPAWN;


    private static void toDev() {
        setMode(Mode.DEV);
        plot = new Plot((int) (x + 9.5), (int) (z - 10.5));
    }
    private static void setMode(Mode mode) {
        Tracker.mode = mode;
        step = Sequence.WAIT_FOR_CLEAR;
        requestPlotId = true;
        requestPlotIdDelay = 10;
    }

    public static Plot getPlot() {
        return plot;
    }

    @HandlePacket
    public boolean clearTitle(ClearTitleS2CPacket clear) {
        if (clear.shouldReset()) {
            step = Sequence.WAIT_FOR_POS;
        }
        return false;
    }

    @HandlePacket
    public boolean positionLook(PlayerPositionLookS2CPacket packet) {
        if (step == Sequence.WAIT_FOR_POS) {
            x = packet.change().position().getX();
            z = packet.change().position().getZ();
            step = Sequence.WAIT_FOR_MESSAGE;
        }
        return false;
    }

    @HandlePacket
    public boolean overlay(OverlayMessageS2CPacket overlay) {
        if (step == Sequence.WAIT_FOR_MESSAGE && overlay.text().getString().startsWith("DiamondFire - ")) {
            setMode(Mode.SPAWN);
        }
        return false;
    }


    private Vec3d localPlayerPos;
    private static int requestPlotIdDelay = 0;
    @Override
    public void onTick() {
        if (requestPlotId) {
            if (requestPlotIdDelay > 0) requestPlotIdDelay--;
            else if (MilloMod.MC.getNetworkHandler() != null) {
                requestPlotIdDelay = 100;
                MilloMod.MC.getNetworkHandler().sendCommand("locate");
            }
        }

        if (MilloMod.MC.player != null) localPlayerPos = getPlot().getPos().relativize(MilloMod.MC.player.getPos()).add(-1, 0, 0);
    }

    public Vec3d getLocalPlayerPos() {
        return localPlayerPos;
    }


    @HandlePacket
    public boolean gameMessage(GameMessageS2CPacket message) {
        String content = message.content().getString();
        if (step == Sequence.WAIT_FOR_MESSAGE) {
            if (content.equals("» You are now in dev mode.")) toDev();
            if (content.equals("» You are now in build mode.")) setMode(Mode.BUILD);
            if (content.startsWith("» Joined game: ")) setMode(Mode.PLAY);
        }
        // example: `                                       \nYou are currently coding on:\n\n→ 🌊 MENACES [41800]\n→ Owner: BupBoi_ \n→ Server: Node 2\n                                       `
        if (requestPlotId && content.startsWith("                          ")) {
            String regex = "\\[\\d+\\] (?=\\[[\\w-]+\\]\\n|\\n)";
            Matcher matcher = Pattern.compile(regex).matcher(content);
            if (matcher.find()) {

                // plot name
                String nameRegex = "(?<=→ ).*(?= \\[\\d+\\] (?=\\[[\\w-]+\\]\\n|\\n))";
                Matcher nameMatcher = Pattern.compile(nameRegex).matcher(content);
                if (nameMatcher.find()) {
                    String plotName = nameMatcher.group().trim();

                    // plot id
                    String plotIdString = matcher.group().trim().replace("[", "").replace("]", "");
                    int id = Integer.parseInt(plotIdString);
                    plot.setId(id);
                    plot.setName(plotName);
                    requestPlotId = false;
                    plotIdCallbacks.forEach(Callback::run);
                    plotIdCallbacks.clear();

                    FileManager.dnsAdd(id, plotName);
                    FileManager.dnsSave();
                    return true;
                }
            }
            regex = "spawn\\n";
            matcher = Pattern.compile(regex).matcher(content);
            if (matcher.find()) {
                plot = Plot.spawn();
                requestPlotId = false;
                return true;
            }
        }
        return false;
    }

    public static void requestPlotId(Callback callback) {
        requestPlotId = true;
        plotIdCallbacks.add(callback);
    }


    @Override
    public boolean alwaysActive() {
        return true;
    }
    @Override
    public String getKey() {
        return "tracker";
    }



    private enum Sequence {
        WAIT_FOR_CLEAR,
        WAIT_FOR_POS,
        WAIT_FOR_MESSAGE,
    }
}
