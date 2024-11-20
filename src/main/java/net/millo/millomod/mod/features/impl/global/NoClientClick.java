package net.millo.millomod.mod.features.impl.global;

import net.millo.millomod.mod.features.Feature;
import net.millo.millomod.mod.features.impl.util.Tracker;
import net.millo.millomod.mod.util.GlobalUtil;
import net.millo.millomod.mod.util.ItemUtil;
import net.minecraft.item.ItemStack;

public class NoClientClick extends Feature {
    @Override
    public String getKey() {
        return "no_client_click";
    }

    public boolean shouldCancel(ItemStack stack) {
        if (!isEnabled() || Tracker.mode != Tracker.Mode.PLAY) return false;

        return GlobalUtil.applyIfNonNull(ItemUtil.getPBV(stack), (pbv) -> pbv.contains("hypercube:noclientclick"));
    }

}
