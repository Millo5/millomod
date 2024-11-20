package net.millo.millomod;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;

public class SoundHandler {


    private static MinecraftClient mc;
    public static void load() {
        mc = MilloMod.MC;
    }

    public static void playClick() {
        playSound("minecraft:entity.item_frame.rotate_item", 0.5f, 1);
    }

    public static void playSound(String name) {
        playSound(name, SoundCategory.MASTER, 1, 1);
    }

    public static void playSound(String name, float volume, float pitch) {
        playSound(name, SoundCategory.MASTER, volume, pitch);
    }

    public static void playSound(String name, SoundCategory category, float volume, float pitch) {
        ClientPlayerEntity player = mc.player;
        if (player == null) return;
        player.playSoundToPlayer(SoundEvent.of(Identifier.of(name)), category, volume, pitch);
    }

    public static void playSound(SoundEvent soundEvent, SoundCategory category, float volume, float pitch) {
        ClientPlayerEntity player = mc.player;
        if (player == null) return;
        player.playSoundToPlayer(soundEvent, category, volume, pitch);
    }



}
