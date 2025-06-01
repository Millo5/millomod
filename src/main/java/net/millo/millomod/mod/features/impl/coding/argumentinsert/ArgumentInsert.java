package net.millo.millomod.mod.features.impl.coding.argumentinsert;

import net.millo.millomod.MilloMod;
import net.millo.millomod.mod.features.Feature;
import net.millo.millomod.mod.features.OnSendPacket;
import net.millo.millomod.mod.features.impl.util.Tracker;
import net.millo.millomod.mod.util.GlobalUtil;
import net.millo.millomod.system.PlayerUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.awt.*;
import java.util.ArrayList;

public class ArgumentInsert extends Feature {

    private boolean selectorOpen = false;
    private int selectorSlot = -1;
    private final ArrayList<ArgumentOption> options;
    private int handlerX, handlerY;

    // TODO: Items are incorrect
    // TODO: Input field doesn't get focus automatically

    public ArgumentInsert() {
        options = new ArrayList<>();
        options.add(new ArgumentOption.NumberOption());
        options.add(new ArgumentOption.TextOption());
        options.add(new ArgumentOption.VarOption());
        options.add(new ArgumentOption.CompOption());
//        options.add(new ArgumentOption(Items.STRING, "txt"));
//        options.add(new ArgumentOption(Items.MAGMA_CREAM, "var"));
//        options.add(new ArgumentOption(Items.BOOK, "comp"));
//        options.add(new ArgumentOption(Items.PRIS-MARINE_SHARD, "vec"));
    }


    private String value = "meow";
    public void setValue(String value) {
        this.value = value;
    }
    private boolean showTextField = false;
    public boolean showTextField() {
        return showTextField;
    }

    public boolean isSelectorOpen() {
        return selectorOpen;
    }

    private void success() {
        showTextField = false;

        ClientPlayNetworkHandler net = MilloMod.MC.getNetworkHandler();

        if (net == null || MilloMod.MC.player == null || MilloMod.MC.interactionManager == null) return;

        ItemStack oldOffhandItem = MilloMod.MC.player.getInventory().getStack(45);
        PlayerUtil.sendOffhandItem(selectedOption.getItem(value));

        MilloMod.MC.interactionManager.clickSlot(
                MilloMod.MC.player.currentScreenHandler.syncId,
                getSlot(),
                40,
                SlotActionType.SWAP,
                MilloMod.MC.player
        );

        PlayerUtil.sendOffhandItem(oldOffhandItem);
    }

    @OnSendPacket
    public boolean onSlotClick(ClickSlotC2SPacket packet) {
        if (!isEnabled()) return false;
        if (Tracker.mode != Tracker.Mode.DEV) return false;
        if (isSelectorOpen() || showTextField()) return true;
        if (!Screen.hasShiftDown()) return false;

        if (packet.getStack().getItem().equals(Items.AIR)) {
            if (packet.getModifiedStacks().isEmpty()) {
                if (packet.getButton() == 1 && packet.getActionType() == SlotActionType.QUICK_MOVE) {
                    selectorOpen = true;
                    selectorSlot = packet.getSlot();
                    shown = 0f;
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public void onTick() {
        var player = MilloMod.MC.player;
        if (player == null) return;

        if (!isSelectorOpen()) return;
        if (MilloMod.MC.currentScreen instanceof HandledScreen<?> screen) {
//            if (getSlot() > screen.getScreenHandler().slots.size()) {
//                if (screen.getScreenHandler().getSlot(getSlot()).getStack().getItem() != Items.AIR) {
//                    onClose();
//                }
//            }
            selectOption(screen);
        }

    }

    private void selectOption(HandledScreen<?> screen) {
        double mouseX = MilloMod.MC.mouse.getX();
        double mouseY = MilloMod.MC.mouse.getY();

        Slot slot = screen.getScreenHandler().slots.get(getSlot());
        int x = slot.x + handlerX;
        int y = slot.y + handlerY;

        x = (int) ((x + 8) * MilloMod.MC.getWindow().getScaleFactor());
        y = (int) ((y + 8) * MilloMod.MC.getWindow().getScaleFactor());

        int dx = (int) (mouseX - x);
        int dy = (int) (mouseY - y);
        float dist = (float) Math.sqrt(dx*dx + dy*dy);
        if (dist < 15) {
            options.forEach(i -> i.setSelected(false));
            return;
        }

        double mouseAngle = Math.atan2(mouseY - y, mouseX - x);
        if (mouseAngle < 0) {
            mouseAngle += 2 * Math.PI;
        }

        double angleSize = Math.toRadians(360d / options.size());
        var offset = Math.toRadians(90);
        for (int i = 0; i < options.size(); i++) {
            double angle = i * angleSize - offset;
            if (angle < 0) {
                angle += 2 * Math.PI;
            }

            options.get(i).setSelected(angle > mouseAngle - angleSize / 2d && angle < mouseAngle + angleSize / 2d);
        }

    }

    private float shown = 0f;
    public void render(DrawContext context, int mouseX, int mouseY, float delta, Slot slot, CallbackInfo ci) {
        if (!enabled) return;


        shown = MathHelper.clampedLerp(shown, isSelectorOpen() ? 1f : 0f, GlobalUtil.frameDelta());
        int x = slot.x;
        int y = slot.y;

        int col = new Color(255, 175, 175, (int)(shown *255)).hashCode();

        context.getMatrices().translate(x+8, y+8, 500f);
        context.fill(-8, -8, +8, +8, col);

        if (showTextField) {
            selectedOption.setSelected(false);
            selectedOption.drawAugment(context, 0, 0, GlobalUtil.frameDelta());
        }

        for (int i = 0; i < options.size(); i++) {
            double angle = Math.toRadians(i * (360d / options.size()) - 90) - (1f - shown) * 0.8d;
            int xx = (int) (Math.cos(angle) * 20 * shown);
            int yy = (int) (Math.sin(angle) * 20 * shown);

            ArgumentOption option = options.get(i);
            option.draw(context, xx, yy, GlobalUtil.frameDelta(), shown);
        }

    }


    @Override
    public String getKey() {
        return "argument_insert";
    }

    public int getSlot() {
        return selectorSlot;
    }

    public void onClose() {
        selectorOpen = false;
    }


    ArgumentOption selectedOption;
    public void mouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (isSelectorOpen()) {
            for (ArgumentOption option : options) {
                if (option.isSelected()) {
                    selectedOption = option;
                    showTextField = true;
                    selectorOpen = false;
                    cir.setReturnValue(true);
                    return;
                }
            }
            onClose();
            cir.setReturnValue(true);

            return;
        }
        if (showTextField) {
            cir.setReturnValue(true);
            success();
        }
    }



    public void setHandlerPosRef(int x, int y) {
        handlerX = x;
        handlerY = y;
    }

    public void keyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (showTextField) {
            cir.setReturnValue(true);
            if (keyCode == 257) {
                success();
            }
        }

    }
}
