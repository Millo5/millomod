package net.millo.millomod.mod.util;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.millo.millomod.MilloMod;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public enum ItemUtil {

    NOT_ARROW("{count: 1, Slot: 0b, components: {\"minecraft:attribute_modifiers\": {show_in_tooltip: 0b, modifiers: []}, \"minecraft:lore\": ['{\"extra\":[{\"bold\":false,\"color\":\"gray\",\"italic\":false,\"obfuscated\":false,\"strikethrough\":false,\"text\":\"Click on a Condition block with this\",\"underlined\":false}],\"text\":\"\"}', '{\"extra\":[{\"bold\":false,\"color\":\"gray\",\"italic\":false,\"obfuscated\":false,\"strikethrough\":false,\"text\":\"to switch between \\'IF\\' and \\'IF NOT\\'.\",\"underlined\":false}],\"text\":\"\"}'], \"minecraft:custom_name\": '{\"color\":\"#ffd47f\",\"italic\":false,\"text\":\"NOT Arrow\"}', \"minecraft:custom_model_data\": 0, \"minecraft:hide_additional_tooltip\": {}, \"minecraft:damage\": 0}, id: \"minecraft:spectral_arrow\"}")

    ;

    private ItemStack item = null;
    private final String itemNbt;

    public static ItemStack fromNbt(String data) {
        try {
            NbtCompound nbt = NbtHelper.fromNbtProviderString(data);
            return ItemStack.fromNbtOrEmpty(MilloMod.MC.world.getRegistryManager(), nbt);
        } catch (CommandSyntaxException e) {
            System.out.println("Error parsing item NBT: " + e.getMessage());
            return ItemStack.EMPTY;
        }
    }

    public static Map<String, Object> getItemTags(ItemStack item) {

        NbtCompound pbv = getPBV(item);
        if (pbv == null) return null;

        HashMap<String, Object> result = new HashMap<>();

        pbv.getKeys().forEach(key -> {
            Object value = pbv.get(key);
            result.put(key, value);
        });

        return result;

    }

    public static NbtCompound getPBV(ItemStack stack) {
        ComponentMap components = stack.getComponents();
        if (components == null) return null;

        NbtComponent custom_data = components.get(DataComponentTypes.CUSTOM_DATA);
        if (custom_data == null) return null;

        NbtCompound nbt = custom_data.copyNbt();
        return nbt.getCompound("PublicBukkitValues");
    }


    public static String getPBVString(ItemStack stack, String key) {
        NbtCompound pbv = getPBV(stack);
        if (pbv == null) return null;

        if (!pbv.contains(key, NbtElement.STRING_TYPE)) return null;

        return pbv.getString(key);
    }


    public ItemStack getItem() {
        if (item != null) return item;

        World world = MilloMod.MC.world;
        if (world == null) return ItemStack.EMPTY;

        item = fromNbt(itemNbt);
        return item;
    }

    ItemUtil(String itemNbt) {
        this.itemNbt = itemNbt;
    }


}
