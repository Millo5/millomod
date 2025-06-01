package net.millo.millomod.mod.features.impl.coding;

import net.millo.millomod.MilloMod;
import net.millo.millomod.SoundHandler;
import net.millo.millomod.mod.features.Feature;
import net.millo.millomod.mod.features.impl.util.Tracker;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.sound.SoundEvent;

public class AngelsGrace extends Feature {
    @Override
    public String getKey() {
        return "angels_grace";
    }

    @Override
    public void onTick() {
        if (!isEnabled()) return;
        if (Tracker.mode != Tracker.Mode.DEV) return;

        Screen screen = MilloMod.MC.currentScreen;
        if (screen instanceof HandledScreen<?>) {
            ClientPlayerEntity player = MilloMod.MC.player;
            if (player != null && !player.getAbilities().flying) {
                if (player.getVelocity().y < -0.3 && !player.isOnGround()) {
                    if (player.getAbilities().creativeMode) {
                        player.getAbilities().flying = true;
                        player.sendAbilitiesUpdate();

                        SoundHandler.playSound("minecraft:entity.breeze.charge", 1f, 1.3f);
                    }
                }
            }
        }

    }
}
