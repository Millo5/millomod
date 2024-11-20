package net.millo.millomod.mixin.render;

import net.millo.millomod.mod.features.FeatureHandler;
import net.millo.millomod.mod.features.impl.coding.ShowTags;
import net.millo.millomod.mod.util.ItemUtil;
import net.millo.millomod.mod.util.gui.GUIStyles;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Set;

@Mixin(ItemStack.class)
public abstract class MItemStack {

    @Shadow @Nullable public abstract ComponentMap getComponents();

    @Shadow public abstract ItemStack copy();

    @Inject(method="getTooltip", at = @At("RETURN"), cancellable = true)
    private void getTooltip(Item.TooltipContext context, PlayerEntity player, TooltipType type, CallbackInfoReturnable<List<Text>> cir) {
        if (player == null) return;
        ShowTags feature = (ShowTags) FeatureHandler.getFeature("show_tags");
        if (!feature.isEnabled() || !feature.isPressed()) return;

        var tags = ItemUtil.getItemTags(copy());
        if (tags == null) return;

        Set<String> keys = tags.keySet();
        if (keys.isEmpty()) return;

        List<String> sortedKeys = keys.stream().sorted().toList();

        List<Text> t = cir.getReturnValue();
        t.add(Text.of(""));
        t.add(Text.literal("Tags:").setStyle(GUIStyles.COMMENT.getStyle()));
        sortedKeys.forEach(key -> {
                String value = tags.get(key).toString();
                value = value.length() > 50 ? value.substring(0, 50)+"..." : value;
                t.add(Text.literal(key.replaceFirst("^.+:", "")).setStyle(GUIStyles.ACTION.getStyle())
                        .append(Text.literal(" = ").setStyle(GUIStyles.DEFAULT.getStyle()))
                        .append(Text.literal(value).setStyle(GUIStyles.NAME.getStyle())));
            }
        );

        cir.setReturnValue(t);
    }

}
