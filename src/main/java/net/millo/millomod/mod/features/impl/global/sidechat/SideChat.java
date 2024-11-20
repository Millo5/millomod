package net.millo.millomod.mod.features.impl.global.sidechat;

import net.millo.millomod.MilloMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.render.RenderTickCounter;

import java.awt.*;

public class SideChat extends ChatHud {

    private final MinecraftClient mc;
    private int xOffset = 0;

    public SideChat() {
        super(MilloMod.MC);
        mc = MilloMod.MC;
    }

    @Override
    public void render(DrawContext context, int currentTick, int mouseX, int mouseY, boolean focused) {
        xOffset = mc.getWindow().getScaledWidth() - getWidth() - tailWidth(getChatScale());

        context.getMatrices().push();
        context.getMatrices().translate((float) xOffset, 0f, 0f);
        super.render(context, currentTick, mouseX - xOffset, mouseY, focused);
        context.getMatrices().pop();
    }

    @Override
    public int getWidth() {

        var actualParentWidth = super.getWidth() + tailWidth(super.getChatScale());
        var flexWidth = mc.getWindow().getScaledWidth() - actualParentWidth - 16;
        var actualWidth = Math.max(1, Math.min(actualParentWidth, flexWidth));

        return actualWidth - tailWidth(getChatScale());
    }

    private int tailWidth(double scale) {
        return (int)(12 * scale);
    }

    public double getXOffset() {
        return xOffset;
    }
}

