package net.millo.millomod.mod.features.impl.coding;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.impl.client.keybinding.KeyBindingRegistryImpl;
import net.millo.millomod.MilloMod;
import net.millo.millomod.mod.features.Feature;
import net.millo.millomod.mod.features.Keybound;
import net.millo.millomod.mod.util.ItemUtil;
import net.millo.millomod.system.Config;
import net.millo.millomod.system.PlayerUtil;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SpectralArrowItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.RaycastContext;

public class NotSwitcher extends Feature implements Keybound {

    KeyBinding key;

    @Override
    public String getKey() {
        return "not_switcher";
    }

    @Override
    public void loadKeybinds() {
        key = KeyBindingRegistryImpl.registerKeyBinding(
                new KeyBinding(
                        "key.millo.not_switcher",
                        InputUtil.Type.KEYSYM,
                        -1,
                        "key.category.millo"
                )
        );
    }

    @Override
    public void triggerKeybind(Config config) {

        while (key.wasPressed()) {
            MinecraftClient mc = MilloMod.MC;
            ClientPlayerEntity player = mc.player;

            if (mc.getNetworkHandler() == null || player == null || mc.world == null) return;

            BlockHitResult rayHit = mc.world.raycast(new RaycastContext(player.getEyePos(),
                    player.getEyePos().add(player.getRotationVector().multiply(5d)),
                    RaycastContext.ShapeType.OUTLINE,
                    RaycastContext.FluidHandling.NONE,
                    ShapeContext.absent()
            ));

            if (rayHit.getType() == HitResult.Type.MISS) return;

            BlockPos blockPos = rayHit.getBlockPos();
            BlockEntity block = mc.world.getBlockEntity(blockPos);
            if (!(block instanceof SignBlockEntity sign)) return;
            String line = sign.getText(true).getMessage(0, false).getString();
            if (!line.contains("IF") && !line.contains("SELECT") && !line.contains("REPEAT")) return;

            ItemStack item = player.getMainHandStack();

            PlayerUtil.sendHandItem(ItemUtil.NOT_ARROW.getItem());
            PlayerUtil.rightClickPos(blockPos);
            PlayerUtil.sendHandItem(item);
        }

    }
}
