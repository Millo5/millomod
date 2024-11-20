package net.millo.millomod.mod.commands.impl.savestate;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class SaveState {

    public static class Item {
        public final ItemStack stack;
        public final int slot;

        public Item(ItemStack stack, int slot) {
            this.stack = stack;
            this.slot = slot;
        }
    }

    private final Vec3d savedPos;
    private final float savedPitch, savedYaw;
    private final List<Item> savedItems;

    public SaveState(Vec3d pos, float pitch, float yaw, List<Item> items) {
        savedPos = pos;
        savedPitch = pitch;
        savedYaw = yaw;
        savedItems = items;
    }

    public List<Item> getItems() {
        return savedItems;
    }

    public float getPitch() {
        return savedPitch;
    }

    public float getYaw() {
        return savedYaw;
    }

    public Vec3d getPos() {
        return savedPos;
    }
}
