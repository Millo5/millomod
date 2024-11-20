package net.millo.millomod.mod.commands.impl.savestate;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.millo.millomod.mod.commands.Command;
import net.millo.millomod.mod.commands.ArgBuilder;
import net.millo.millomod.mod.features.impl.util.teleport.TeleportHandler;
import net.millo.millomod.system.PlayerUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RedevCommand extends Command {

    public enum ALLOWED_STATES {
        Alpha,
        Beta,
        Gamma,
        Delta,
        Epsilon
    }

    private static final HashMap<ALLOWED_STATES, SaveState> saveStates = new HashMap<>();

    public static int save(ClientPlayerEntity player) {
        return save(player, ALLOWED_STATES.Alpha);
    }
    public static int save(ClientPlayerEntity player, ALLOWED_STATES stateSlot) {
        Vec3d savedPos = player.getPos();
        float savedPitch = player.getPitch();
        float savedYaw = player.getYaw();

        List<SaveState.Item> savedItems = new ArrayList<>();

        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stackInSlot = player.getInventory().getStack(i);
            if (!stackInSlot.isEmpty()) {
                savedItems.add(new SaveState.Item(stackInSlot.copy(), i));
            }
        }
        saveStates.put(stateSlot, new SaveState(savedPos, savedPitch, savedYaw, savedItems));

        return 1;
    }

    private static void load(ClientPlayerEntity player, boolean loadInventory) {
        load(player, ALLOWED_STATES.Alpha, loadInventory);
    }
    private static void load(ClientPlayerEntity player, ALLOWED_STATES stateSlot, boolean loadInventory) {
        if (!player.isCreative()) return;

        SaveState state = saveStates.get(stateSlot);

        Vec3d savedPos = state.getPos();
        List<SaveState.Item> savedItems = state.getItems();

        player.getAbilities().flying = true;
        player.setPitch(state.getPitch());
        player.setYaw(state.getYaw());
        player.setVelocity(0, 0, 0);

        if (loadInventory) {
            player.getInventory().clear();
            for (SaveState.Item itemTag : savedItems) {
                int slot = itemTag.slot;

                if (slot >= 0 && slot < player.getInventory().size()) {
                    PlayerUtil.setItem(slot, itemTag.stack.copy());
                }
            }
        }

        TeleportHandler.teleportTo(savedPos);
    }


    private int runCmd(MinecraftClient mc, ALLOWED_STATES stateSlot, boolean loadInv) {
        assert mc.player != null;
        if (saveStates.containsKey(stateSlot)) {
            load(mc.player, stateSlot, loadInv);
        }
        return 1;
    }
    @Override
    public void register(MinecraftClient mc, CommandDispatcher<FabricClientCommandSource> cd, CommandRegistryAccess context) {
        LiteralArgumentBuilder<FabricClientCommandSource> builder = ArgBuilder.literal("redev")
                .executes(ctx -> runCmd(mc, ALLOWED_STATES.Alpha, false));

        for (ALLOWED_STATES state : ALLOWED_STATES.values()) {
            LiteralArgumentBuilder<FabricClientCommandSource> subCmd =  ArgBuilder.literal(state.name().toLowerCase())
                    .executes(ctx -> runCmd(mc, state, false))
                    .then(ArgBuilder.literal("-i")
                            .executes(ctx -> runCmd(mc, state, true)));
            builder.then(subCmd);
        }

        cd.register(builder);
    }

    @Override
    public String getKey() {
        return "redev";
    }

}
