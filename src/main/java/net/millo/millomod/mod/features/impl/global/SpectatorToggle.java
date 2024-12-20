package net.millo.millomod.mod.features.impl.global;

import net.fabricmc.fabric.impl.client.keybinding.KeyBindingRegistryImpl;
import net.millo.millomod.MilloMod;
import net.millo.millomod.mod.features.Feature;
import net.millo.millomod.mod.features.Keybound;
import net.millo.millomod.system.Config;
import net.millo.millomod.system.PlayerUtil;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.math.MathHelper;

public class SpectatorToggle extends Feature implements Keybound {

    KeyBinding toggleKey;
    private boolean on = false;

    @Override
    public String getKey() {
        return "spectator_toggle";
    }


    @Override
    public void loadKeybinds() {
        toggleKey = KeyBindingRegistryImpl.registerKeyBinding(
                new KeyBinding(
                        "key.millo.spectator_toggle",
                        InputUtil.Type.KEYSYM,
                        -1,
                        "key.category.millo"
                )
        );
    }

    @Override
    public void triggerKeybind(Config config) {
        if (MilloMod.MC.getNetworkHandler() == null) return;
        while (toggleKey.wasPressed()) {
            if (MilloMod.MC.player == null || !MilloMod.MC.player.isCreative()) return;
            if (on) {
                PlayerUtil.sendCommand("gmc");
                on = false;
            } else {
                PlayerUtil.sendCommand("gmsp");
                on = true;
            }
        }
    }
}
