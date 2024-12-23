package net.millo.millomod;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;

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

    public static void playSound(SoundEvent soundEvent, double volume, double pitch) {
        playSound(soundEvent, SoundCategory.MASTER, (float) volume, (float) pitch);
    }

    public static void playSoundVariant(String soundId, long seed, float volume, float pitch) {
        ClientPlayerEntity player = mc.player;
        if (player == null) return;
        SoundEvent sound = SoundEvent.of(Identifier.of(soundId));

        PositionedSoundInstance soundInstance = new PositionedSoundInstance(sound, SoundCategory.MASTER, volume, pitch, Random.create(seed), player.getX(), player.getY(), player.getZ());
        MilloMod.MC.getSoundManager().play(soundInstance);
    }
}
