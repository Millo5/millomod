package net.millo.millomod.mod.features.impl.util.teleport;

import net.millo.millomod.MilloMod;
import net.millo.millomod.mod.Callback;
import net.millo.millomod.mod.features.impl.util.Tracker;
import net.millo.millomod.mod.util.LocationItem;
import net.millo.millomod.system.PlayerUtil;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.*;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class Teleport {

    private final Callback callback;
    private boolean active = false;
    public Vec3d target;
    public String methodTarget;
    private ItemStack locationItem;
    private int buffer, lastTickPackets = 1, thisTickPackets, attempts;
    private boolean doNotSuppress = false;
    private boolean haltForTp = false;
    private int locationItemDelay = 0;

    public Teleport(Vec3d target, Callback callback) {
        this.callback = callback;
        this.target = target;
        this.methodTarget = null;

        go();
    }

    public Teleport(String methodname, Callback callback) {
        this.callback = callback;
        this.target = null;
        this.methodTarget = methodname;

        go();
    }


    public boolean positionLook(PlayerPositionLookS2CPacket pos) {
        if (locationItem == null || !active) return false;
        if (MilloMod.MC.getNetworkHandler() == null || MilloMod.MC.player == null) return false;
        MilloMod.MC.getNetworkHandler().sendPacket(new TeleportConfirmC2SPacket(pos.teleportId()));
        if (!haltForTp) {
            MilloMod.MC.player.setPosition(target);
            active = false;
            callback.run();
        } else {
            locationItemDelay = 5;
            haltForTp = false;
        }
        return true;
    }
    public boolean positionLookSend(PlayerPositionLookS2CPacket pos) {
        if (doNotSuppress) doNotSuppress = false;
        else return true;
        return false;
    }

    public void onTick() {
        if (locationItemDelay > 0) {
            locationItemDelay--;
            return;
        }
        if (haltForTp) return;

        attempts ++;

        ClientPlayerEntity player = MilloMod.MC.player;
        if (player == null) {
            active = false;
            callback.run();
            return;
        }

        player.setVelocity(0, 0, 0);
        if (player.getPos().equals(target)) {
            active = false;
            callback.run();
            return;
        }

        if (attempts == 4 && MilloMod.MC.getNetworkHandler() != null) {
            locationItem = new LocationItem(Tracker.getPlot().getPos().relativize(target))
                    .setRotation(player.getPitch(), player.getYaw())
                    .toStack();

            haltForTp = true;
            useLocationItem(player, MilloMod.MC.getNetworkHandler());
        }
        hackTowards();
        lastTickPackets=thisTickPackets;
    }

    public void go() {
        if (MilloMod.MC.player == null) return;

        ClientPlayerEntity player = MilloMod.MC.player;
        if (!player.isCreative()) return;

        if (target != null) {
            PlayerUtil.sendCommand("p tp " + target.x + " " + target.y + " " + target.z);
        } else {
            PlayerUtil.sendCommand("ctp " + methodTarget);
        }

        active = true;


//        if (Tracker.getPlot().isInArea(target)) {
//            locationItem = new LocationItem(Tracker.getPlot().getPos().relativize(target))
//                    .setRotation(player.getPitch(), player.getYaw())
//                    .toStack();
//
//            useLocationItem(player, net);
//        }

    }

    private void useLocationItem(ClientPlayerEntity player, ClientPlayNetworkHandler net) {
        ItemStack handItem = player.getMainHandStack();
        boolean sneaking = !player.isSneaking();

        net.sendPacket(new CreativeInventoryActionC2SPacket(36 + player.getInventory().selectedSlot, locationItem));
        if (sneaking)
            net.sendPacket(new ClientCommandC2SPacket(player, ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY));
        net.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, player.getBlockPos(), Direction.UP));
        net.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, player.getBlockPos(), Direction.UP));
        if (sneaking)
            net.sendPacket(new ClientCommandC2SPacket(player, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));
        net.sendPacket(new CreativeInventoryActionC2SPacket(36 + player.getInventory().selectedSlot, handItem));
    }

    private void hackTowards() {
        if(MilloMod.MC.player == null || MilloMod.MC.getNetworkHandler() == null) return;


        buffer++;
        if(buffer > 1) {
            buffer = 0;
            return;
        }
        if(!active) return;
        if(MilloMod.MC.player == null) return;
        Vec3d pos = MilloMod.MC.player.getPos();

        Vec3d offset = pos.relativize(target);
        double maxLength = 50;
        double distance = offset.length();
        Vec3d jump = distance > maxLength ? pos.add(offset.normalize().multiply(maxLength)) : target;
        if(distance > 10) {
            for (int i = 0; i < Math.min(lastTickPackets + 5,50); i++) {
                thisTickPackets++;
                doNotSuppress = true;
                MilloMod.MC.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(false, false));
            }
        }
        thisTickPackets++;
        doNotSuppress = true;
        MilloMod.MC.player.setPos(jump.x, jump.y, jump.z);
    }


    public void abort() {
        active = false;
    }
    public boolean aborting() {
        return !active;
    }
}
