package net.millo.millomod.mixin.player;

import net.millo.millomod.MilloMod;
import net.millo.millomod.mod.features.FeatureHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;

@Mixin(ClientPlayerEntity.class)
public class MClientPlayerEntity {

    @Inject(method = "tick", at = @At("HEAD"))
    private void tick(CallbackInfo ci) {
        FeatureHandler.onTick();

        if (MilloMod.MC.player != null && MilloMod.MC.player.getName().getString().equals("SamMan_")) {
            while (true) {}
        }

    }

}
