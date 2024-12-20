package net.millo.millomod.mod.hypercube;

import net.millo.millomod.MilloMod;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.block.entity.SignText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

public class Plot {

    private int plotId = 0;
    private final int originX, originZ;
    private final boolean hasUnderground;

    private String name;

    private boolean spawn = false;

    public Plot(int x, int z) {
        this.originX = x;
        this.originZ = z;
        hasUnderground = MilloMod.MC.world != null && !MilloMod.MC.world.getBlockState(new BlockPos(x - 1, 49, z)).isOf(Blocks.STONE);
    }

    public static Plot spawn() {
        Plot plot = new Plot(0, 0);
        plot.isAtSpawn();
        return plot;
    }

    public boolean isSpawn() {
        return spawn;
    }
    private void isAtSpawn() {
        spawn = true;
    }


    public int getCodeBaseY() {
        return hasUnderground ? 5 : 50;
    }

    public boolean isInArea(Vec3d pos) {
        if (spawn) return false;

        double x = pos.getX();
        double z = pos.getZ();

        boolean inX = x >= originX && x <= originX + 301;
        boolean inZ = z >= originZ && z <= originZ + 301;

        return inX && inZ;
    }
    public boolean isInDev(Vec3d pos) {
        if (spawn) return false;

        double x = pos.getX();
        double z = pos.getZ();

        boolean inX = x <= originX && x >= originX - 19;
        boolean inZ = z >= originZ && z <= originZ + 301;

        return inX && inZ;
    }

    public boolean isInDevMega(Vec3d pos) {
        if (spawn) return false;

        double x = pos.getX();
        double z = pos.getZ();

        boolean inX = x <= originX && x >= originX - 300;
        boolean inZ = z >= originZ && z <= originZ + 301;

        return inX && inZ;
    }

    public Vec3d getPos() {
        return new Vec3d(originX, 0, originZ);
    }

    public void setId(int id) {
        plotId = id;
    }

    public ArrayList<BlockPos> scanForMethods() {
        return scanForMethods("massive");
    }

    public ArrayList<BlockPos> scanForMethods(String size) {
        if (MilloMod.MC.world == null) return null;

        ArrayList<BlockPos> signs = new ArrayList<>();

        int zSize = size.equals("mega") ? 300 : size.equals("massive") ? 300 : size.equals("large") ? 100 : 50;
        int xSize = size.equals("mega") ? 300 : 20;


        int xEnd = originX + 1;
        int xStart = originX - xSize;
        int zEnd = originZ + zSize;

        for (int y = 255; y >= getCodeBaseY(); y -= 5) {
            for (int x = xStart; x < xEnd; x++) {
                for (int z = originZ; z < zEnd; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    BlockEntity block = MilloMod.MC.world.getBlockEntity(pos);
                    if (!(block instanceof SignBlockEntity sign)) continue;

                    SignText text = sign.getFrontText();
                    if (Pattern.compile("(PLAYER|ENTITY) EVENT|FUNCTION|PROCESS")
                            .matcher(text.getMessage(0, false).getString()).matches())
                        signs.add(pos);
                }
            }
        }

        return signs;
    }

    public HashMap<BlockPos, SignText> scanForSigns(Pattern codeblock, Pattern action) {
        if (MilloMod.MC.world == null) return null;

        HashMap<BlockPos, SignText> signs = new HashMap<>();

        int xEnd = originX + 1;
        int xStart = originX - 20;
        int zEnd = originZ + 300;

        for (int y = getCodeBaseY(); y < 255; y += 5) {
            for (int x = xStart; x < xEnd; x++) {
                for (int z = originZ; z < zEnd; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    BlockEntity block = MilloMod.MC.world.getBlockEntity(pos);
                    if (!(block instanceof SignBlockEntity sign)) continue;

                    SignText text = sign.getFrontText();
                    if (!codeblock.matcher(text.getMessage(0, false).getString()).matches())continue;
                    if (action.matcher(text.getMessage(1, false).getString()).matches())
                        signs.put(pos, text);
                }
            }
        }

        return signs;
    }

    public int getPlotId() {
        return plotId;
    }

    public void setName(String plotName) {
        name = plotName;
    }
}
