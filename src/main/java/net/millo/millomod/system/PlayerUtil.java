package net.millo.millomod.system;

import net.millo.millomod.MilloMod;
import net.millo.millomod.mod.util.gui.GUIStyles;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class PlayerUtil {

    public static void sendCommand(String command) {
        if (MilloMod.MC.getNetworkHandler() == null) return;

        MilloMod.MC.getNetworkHandler().sendCommand(command);
    }

    public static void sendHandItem(ItemStack item) {
        MilloMod.MC.getNetworkHandler().sendPacket(new CreativeInventoryActionC2SPacket(36 + MilloMod.MC.player.getInventory().selectedSlot, item));
    }

    public static void rightClickPos(BlockPos pos) {
        MilloMod.MC.interactionManager.interactBlock(MilloMod.MC.player, Hand.MAIN_HAND, new BlockHitResult(
                pos.toCenterPos(), Direction.UP, pos, false
        ));
    }

    public static void sendSneak(boolean sneaking) {
        MilloMod.MC.getNetworkHandler().sendPacket(
                new ClientCommandC2SPacket(MilloMod.MC.player,
                        sneaking ?
                                ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY :
                                ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY)
        );
    }

    public static void sendOffhandItem(ItemStack itemStack) {
        MilloMod.MC.getNetworkHandler().sendPacket(new CreativeInventoryActionC2SPacket(45, itemStack));
        MilloMod.MC.player.getInventory().setStack(45, itemStack);
    }

    public static void giveItem(ItemStack item) {
        MinecraftClient mc = MilloMod.MC;
        DefaultedList<ItemStack> inv = mc.player.getInventory().main;

        if (!mc.player.isCreative()) return;
        if (mc.interactionManager == null) return;

        for (int index = 0; index < inv.size(); index++) {
            ItemStack i = inv.get(index);
            ItemStack compareItem = i.copy();
            compareItem.setCount(item.getCount());
            if (item == compareItem) {
                while (i.getCount() < i.getMaxCount() && item.getCount() > 0) {
                    i.setCount(i.getCount() + 1);
                    item.setCount(item.getCount() - 1);
                }
            } else {
                if (i.getItem() == Items.AIR) {
                    if (index < 9)
                        mc.interactionManager.clickCreativeStack(item, index + 36);
                    inv.set(index, item);
                    return;
                }
            }
        }

        int slot = mc.player.getInventory().getEmptySlot();

        if (slot == -1) {
            mc.player.sendMessage(Text.literal("No inventory room!").setStyle(GUIStyles.SCARY.getStyle()), false);
            return;
        }

        mc.player.getInventory().setStack(slot, item);
        mc.interactionManager.clickCreativeStack(item, slot);
    }

    public static void setItem(int slot, ItemStack item) {
        if (MilloMod.MC.interactionManager == null) return;
        if (MilloMod.MC.player == null) return;

        MilloMod.MC.player.getInventory().setStack(slot, item);
        MilloMod.MC.interactionManager.clickCreativeStack(item, slot);
    }
}
