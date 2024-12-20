package net.millo.millomod.mod.features.impl.coding;

import net.fabricmc.fabric.impl.client.keybinding.KeyBindingRegistryImpl;
import net.millo.millomod.MilloMod;
import net.millo.millomod.SoundHandler;
import net.millo.millomod.mod.features.Feature;
import net.millo.millomod.mod.features.HandlePacket;
import net.millo.millomod.mod.features.Keybound;
import net.millo.millomod.system.Config;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;

public class PickChestValue extends Feature implements Keybound {

    KeyBinding key;

    private static String INTERNAL_ID = "millo:pick_chest_value :3";

    boolean requested = false;

    @Override
    public String getKey() {
        return "pick_chest_value";
    }

    @Override
    public void loadKeybinds() {
        key = KeyBindingRegistryImpl.registerKeyBinding(
                new KeyBinding(
                        "key.millo.pick_chest_value",
                        -1,
                        "key.category.millo"
                )
        );
    }


    @HandlePacket
    public boolean slotUpdate(ScreenHandlerSlotUpdateS2CPacket packet) {
        if (!requested) return false;
        if (packet.getStack().getName() == null) return false;
        if (!packet.getStack().getName().equals(Text.literal(INTERNAL_ID))) return false;

        requested = false;

        ItemStack chest = packet.getStack();
        ContainerComponent container = chest.get(DataComponentTypes.CONTAINER);
        if (container == null) return false;

        ItemStack item = container.iterateNonEmpty().iterator().next();
        if (item == null) return false;

        if (MilloMod.MC.getNetworkHandler() == null) return false;

        MilloMod.MC.getNetworkHandler().sendPacket(new CreativeInventoryActionC2SPacket(packet.getSlot(), ItemStack.EMPTY));
        MilloMod.MC.getNetworkHandler().sendPacket(new CreativeInventoryActionC2SPacket(packet.getSlot(), item));

        SoundHandler.playClick();

        return true;
    }


    @Override
    public void triggerKeybind(Config config) {
        while (key.wasPressed()) {
            MinecraftClient mc = MinecraftClient.getInstance();
            ClientPlayerEntity player = mc.player;

            if (mc.world == null || player == null || mc.getNetworkHandler() == null) return;

            HitResult rayHit = mc.crosshairTarget;

            if (!(rayHit instanceof BlockHitResult blockRayHit)) return;
            if (blockRayHit.getType() == HitResult.Type.MISS) return;
            BlockEntity blockEntity = mc.world.getBlockEntity(blockRayHit.getBlockPos());
            if (!(blockEntity instanceof ChestBlockEntity chest)) return;

            BlockPos pos = blockRayHit.getBlockPos();

            ItemStack item = Items.CHEST.getDefaultStack();
            NbtCompound bet = new NbtCompound();
            bet.putString("id", "minecraft:chest");
            bet.putInt("x", pos.getX());
            bet.putInt("y", pos.getY());
            bet.putInt("z", pos.getZ());
            item.set(DataComponentTypes.BLOCK_ENTITY_DATA, NbtComponent.of(bet));
            item.set(DataComponentTypes.CUSTOM_NAME, Text.literal(INTERNAL_ID));

            requested = true;

//            PlayerUtil.giveItem(item);
            mc.getNetworkHandler().sendPacket(new CreativeInventoryActionC2SPacket(-1, item));

        }
    }
}
