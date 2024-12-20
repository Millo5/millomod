package net.millo.millomod.mod.features.impl.util.teleport;

import net.millo.millomod.MilloMod;
import net.millo.millomod.mod.Callback;
import net.millo.millomod.mod.features.Feature;
import net.millo.millomod.mod.features.HandlePacket;
import net.millo.millomod.system.PlayerUtil;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.c2s.play.TeleportConfirmC2SPacket;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.util.math.Vec3d;

public class TeleportHandler extends Feature {

    private static boolean serverSide = false;
    private static boolean active = false;
    private static Vec3d target;
    private static Callback callback;
    private static String methodTarget;

    private static Vec3d lastTeleportPosition;

    public static boolean lastSuccess = false;
    public static Vec3d getLastTeleportPosition() {
        return lastTeleportPosition;
    }


    @HandlePacket
    public boolean chat(GameMessageS2CPacket message) {
        if (!active) return false;
        String content = message.content().getString();

        if (content.equals("Error: That location is out of bounds!")) {
            active = false;
            callback.run();
            lastSuccess = false;
            return true;
        }
        return false;
    }

    @HandlePacket
    public boolean positionLook(PlayerPositionLookS2CPacket packet) {
        if (!active) return false;

        if (MilloMod.MC.getNetworkHandler() == null || MilloMod.MC.player == null) return false;

        if (serverSide) {
            MilloMod.MC.getNetworkHandler().sendPacket(new TeleportConfirmC2SPacket(packet.teleportId()));
        }

        ClientPlayerEntity player = MilloMod.MC.player;
        if (player == null) return serverSide;

        Vec3d pos = packet.change().position();

        lastTeleportPosition = new Vec3d(pos.getX(), pos.getY(), pos.getZ());

        if (target != null) {
            if (target.equals(new Vec3d(pos.getX(), pos.getY(), pos.getZ()))) {
                active = false;
                lastSuccess = true;
                callback.run();
            }
        } else {
            if (!packet.relatives().contains(PositionFlag.X_ROT) && !packet.relatives().contains(PositionFlag.Y_ROT)
                    && packet.change().pitch() == 0 && packet.change().yaw() == 0) {
                active = false;
                lastSuccess = true;
                callback.run();
            }
        }

        return serverSide;
    }

//    @OnSendPacket
//    public boolean positionLookSend(PlayerPositionLookS2CPacket pos) {
////        if (teleport == null || teleport.aborting()) return false;
////        return teleport.positionLookSend(pos);
//    }

    public static void serverSide() {
        TeleportHandler.serverSide = true;
    }

    public static void teleportTo(Vec3d target, Callback callback) {
        PlayerUtil.sendCommand("p tp " + target.x + " " + target.y + " " + target.z);
        TeleportHandler.active = true;
        TeleportHandler.target = target;
        TeleportHandler.callback = callback;
        TeleportHandler.serverSide = false;
    }

    public static void teleportTo(Vec3d target) {
        teleportTo(target, () -> {});
    }

    public static void teleportToMethod(String methodname, Callback callback) {
        PlayerUtil.sendCommand("ctp " + methodname);
        TeleportHandler.active = true;
        TeleportHandler.methodTarget = methodname;
        TeleportHandler.callback = callback;
        TeleportHandler.target = null;
        TeleportHandler.serverSide = false;
    }

    @Override
    public void onTick() {
//        if (teleport == null || teleport.aborting()) return;
//        teleport.onTick();
    }



    @Override
    public String getKey() {
        return "teleport_handler";
    }
    @Override
    public boolean alwaysActive() {
        return true;
    }
}
