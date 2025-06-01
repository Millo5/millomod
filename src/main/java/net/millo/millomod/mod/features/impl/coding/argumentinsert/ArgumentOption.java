package net.millo.millomod.mod.features.impl.coding.argumentinsert;

import net.millo.millomod.mod.util.gui.GUIStyles;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;

import java.awt.*;
import java.util.List;

public abstract class ArgumentOption {
    protected final String id;
    protected final ItemStack icon;

    private boolean selected = false;
    private float hover = 0f;
    public ArgumentOption(Item icon, String id) {
        this.id = id;
        this.icon = new ItemStack(icon);
    }

    public ItemStack getItem(String value) {
        ItemStack item = new ItemStack(icon.getItem());

        NbtCompound pbv = new NbtCompound();
        pbv.putString("hypercube:varitem", getVarItemString(item, value));

        NbtCompound custom_nbt = new NbtCompound();
        custom_nbt.putInt("CustomModelData", 5000);
        custom_nbt.put("PublicBukkitValues", pbv);

        NbtComponent custom_data = NbtComponent.of(custom_nbt);

        item.set(DataComponentTypes.CUSTOM_DATA, custom_data);

        return item;
    }

    protected abstract String getVarItemString(ItemStack item, String value);


    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isSelected() {
        return selected;
    }


    private float x, y;
    public void draw(DrawContext context, int x, int y, float delta, float shown) {
        hover = MathHelper.clampedLerp(hover, isSelected() ? 1f : 0f, delta);

        this.x = x;
        this.y = y;

        context.getMatrices().push();
        context.getMatrices().translate(x, y, 1f);
        context.getMatrices().scale(shown, shown, 1);
        context.getMatrices().scale(hover*0.2f+1f, hover*0.2f+1f, 1);

        int color = new Color(0f, 0f, 0f, 0.2f + hover * 0.3f).hashCode();
        int borderCol = new Color(1f-hover, 1f, 1f, 1f).hashCode();
        context.fill(-8, -8, 8, 8, color);
        context.drawBorder(-8, -8, 16, 16, borderCol);

        context.drawItem(icon, -8, -8);

        context.getMatrices().pop();
    }

    public void drawAugment(DrawContext context, int x, int y, float delta) {
        this.x = MathHelper.clampedLerp(this.x, x, delta);
        this.y = MathHelper.clampedLerp(this.y, y, delta);

        context.getMatrices().push();
        context.getMatrices().translate(this.x, this.y, 1f);
        context.getMatrices().scale(0.9f, 0.9f, 1f);

        context.drawItem(icon, -8, -8);

        context.getMatrices().pop();
    }


    public static class NumberOption extends ArgumentOption {
        public NumberOption() {
            super(Items.SLIME_BALL, "num");
        }

        @Override
        protected String getVarItemString(ItemStack item, String value) {
            if (value.equalsIgnoreCase("z")) value = "0"; // Quick hand for 0. as I can only reach up to 9 without moving my hand. (I know, I am lazy)
            item.set(DataComponentTypes.CUSTOM_NAME, Text.literal(value).setStyle(Style.EMPTY.withColor(0xff5555).withItalic(false)));
            return "{\"id\":\"num\",\"data\":{\"name\":\"" + value + "\"}}";
        }
    }
    public static class TextOption extends ArgumentOption {
        public TextOption() {
            super(Items.STRING, "txt");
        }

        @Override
        protected String getVarItemString(ItemStack item, String value) {
            item.set(DataComponentTypes.CUSTOM_NAME, Text.literal(value).setStyle(Style.EMPTY.withItalic(false)));
            return "{\"id\":\"txt\",\"data\":{\"name\":\"" + value + "\"}}";
        }
    }
    public static class CompOption extends ArgumentOption {
        public CompOption() {
            super(Items.BOOK, "comp");
        }

        @Override
        protected String getVarItemString(ItemStack item, String value) {
            item.set(DataComponentTypes.CUSTOM_NAME, Text.literal(value).setStyle(Style.EMPTY.withColor(Formatting.AQUA).withItalic(false)));
            return "{\"id\":\"comp\",\"data\":{\"name\":\"" + value + "\"}}";
        }
    }

    public static class VarOption extends ArgumentOption {
        public VarOption() {
            super(Items.MAGMA_CREAM, "var");
        }


        private enum Scope {
            LINE("line", "-i", Text.literal("LINE").setStyle(GUIStyles.LINE.getStyle().withItalic(false))),
            LOCAL("local", "-l", Text.literal("LOCAL").setStyle(GUIStyles.LOCAL.getStyle().withItalic(false))),
            SAVED("saved", "-s", Text.literal("SAVE").setStyle(GUIStyles.SAVED.getStyle().withItalic(false))),
            UNSAVED("unsaved", "\n", Text.literal("GAME").setStyle(GUIStyles.UNSAVED.getStyle().withItalic(false)));
//            LINE("line", "-i", "{\"italic\":false,\"color\":\"#55AAFF\",\"text\":\"LINE\"}"),
//            LOCAL("local", "-l", "{\"italic\":false,\"color\":\"#55FF55\",\"text\":\"LOCAL\"}"),
//            SAVED("saved", "-s", "{\"italic\":false,\"color\":\"#FFFF55\",\"text\":\"SAVE\"}"),
//            UNSAVED("unsaved", "\n", "{\"italic\":false,\"color\":\"#AAAAAA\",\"text\":\"GAME\"}");

            private final String scope, key;
            private final Text lore;

            Scope(String scope, String key, Text lore) {
                this.scope = scope;
                this.key = key;
                this.lore = lore;
            }
        }

        @Override
        protected String getVarItemString(ItemStack item, String value) {
            Scope scope = Scope.UNSAVED;
            for (Scope s : Scope.values()) {
                if (value.endsWith(s.key)) {
                    scope = s;
                    value = value.substring(0, value.length() - 3);
                    break;
                }
            }

            LoreComponent lore = new LoreComponent(List.of(scope.lore));

            item.set(DataComponentTypes.CUSTOM_NAME, Text.literal(value).setStyle(Style.EMPTY.withColor(Formatting.WHITE).withItalic(false)));
            item.set(DataComponentTypes.LORE, lore);

            return "{\"id\":\"var\",\"data\":{\"name\":\"" + value + "\",\"scope\":\"" + scope.scope + "\"}}";
        }
    }


}
