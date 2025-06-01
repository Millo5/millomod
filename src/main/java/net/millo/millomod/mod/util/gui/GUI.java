package net.millo.millomod.mod.util.gui;


import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.millo.millomod.mod.util.gui.elements.ContextElement;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.TooltipPositioner;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;

import java.awt.*;
import java.util.List;

@Environment(EnvType.CLIENT)
public abstract class GUI extends Screen {

    protected int backgroundWidth, backgroundHeight;
    protected int paddingX = 40;
    protected int paddingY = 40;
    private ElementFadeIn fade = new ElementFadeIn(ElementFadeIn.Direction.UP);
    private GUI parent;
    private PositionedTooltip tooltip;

    private static class PositionedTooltip {
        List<OrderedText> tooltip;
        TooltipPositioner positioner;

        public PositionedTooltip(List<OrderedText> tooltip, TooltipPositioner positioner) {
            this.tooltip = tooltip;
            this.positioner = positioner;
        }
    }


    public GUI(Text title) {
        super(title);
    }

    @Override
    protected void init() {
        super.init();
        backgroundWidth = width - paddingX *2;
        backgroundHeight = height - paddingY *2;
    }

    @Override
    public void close() {
        if (parent != null && client != null) {
            client.setScreen(parent);
        } else super.close();
    }

    @Override
    public void setTooltip(List<OrderedText> tooltip, TooltipPositioner positioner, boolean focused) {
        if (this.tooltip == null || focused) {
            this.tooltip = new PositionedTooltip(tooltip, positioner);
        }
    }

    public void open() {
        MinecraftClient.getInstance().send(() -> MinecraftClient.getInstance().setScreen(this));
    }

    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {

        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;

        y += (int)((1f - fade.getProgress()) * 10f);

        int color = new Color(0, 0, 0, (int)(fade.getProgress() * 150)).hashCode();

        context.getMatrices().push();
        context.getMatrices().translate(0f, 0f, -20f);
        context.fill(x, y, x+backgroundWidth, y+backgroundHeight, 0, color);
        context.getMatrices().pop();
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        fade.fadeIn(delta);

        if (tooltip != null) {
            context.drawTooltip(textRenderer, tooltip.tooltip, tooltip.positioner, mouseX, mouseY);
            this.tooltip = null;
        }

        if (fade.getProgress() >= 1f) {
            try {
                super.render(context, mouseX, mouseY, delta);
            } catch (Exception ignored) {} // Concurrency error, ignore it
            if (contextMenu != null) contextMenu.render(context, mouseX, mouseY, delta);
        } else {
            renderBackground(context, mouseX, mouseY, delta);
        }
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (contextMenu != null && contextMenu.inBounds(mouseX, mouseY))
            return contextMenu.mouseReleased(mouseX, mouseY, button);
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (contextMenu != null) {
            if (!contextMenu.inBounds(mouseX, mouseY)) {
                closeContext();
            }
            return false;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    public ElementFadeIn getFade() {
        return fade;
    }

    public void setParent(GUI gui) {
        this.parent = gui;
    }

    public void setFade(ElementFadeIn fade) {
        this.fade = fade;
    }


    ContextElement contextMenu = null;
    public void openContext(double mouseX, double mouseY, ContextElement contextElement) {
        contextMenu = contextElement;
        contextMenu.setX((int) mouseX);
        contextMenu.setY((int) mouseY);
    }
    public void closeContext() {
        contextMenu = null;
    }
}
