package net.millo.millomod.mod.util.gui.elements;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.millo.millomod.mod.util.gui.ClickableElementI;
import net.millo.millomod.mod.util.gui.ElementFadeIn;
import net.millo.millomod.mod.util.gui.ScrollableEntryI;
import net.millo.millomod.mod.util.gui.TooltipHolder;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.tooltip.TooltipBackgroundRenderer;
import net.minecraft.client.gui.tooltip.TooltipState;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.item.Item;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public class ButtonElement implements ScrollableEntryI, Element, Widget, Selectable, ClickableElementI, TooltipHolder {
    private int x, y, realX, realY;
    protected int width, height;
    protected boolean hovered;
    public boolean visible = true;

    PressAction onPress;
    protected ElementFadeIn fade = new ElementFadeIn(ElementFadeIn.Direction.RIGHT);

    protected TextWidget textWidget;
    protected TextRenderer textRenderer;
    public ButtonElement(int x, int y, int width, int height, PressAction onPress) {
        this.x = x;
        this.y = y;
        this.realX = x;
        this.realY = y;
        this.width = width;
        this.height = height;
        this.onPress = onPress;
    }

    public ButtonElement(int x, int y, int width, int height, Text message, PressAction onPress, TextRenderer textRenderer) {
        this(x, y, width, height, onPress);

        this.textRenderer = textRenderer;
        textWidget = new TextWidget(x, y, width, height, message, textRenderer);
    }

    public void setFade(ElementFadeIn fade) {
        this.fade = fade;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (this.visible) {
            this.hovered = mouseX >= this.getRealX() && mouseY >= this.getRealY() && mouseX < this.getRealX() + this.width && mouseY < this.getRealY() + this.height;
            this.renderWidget(context, mouseX, mouseY, delta);
            this.tooltip.render(this.isHovered(), this.isFocused(), this.getNavigationFocus());
        }
    }

    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        fade.fadeIn(delta);

        context.getMatrices().push();
        context.getMatrices().translate(0, 0, z);

        int x = getX() + fade.getXOffset();
        int y = getY() + fade.getYOffset();
        int color = new Color(0, 0, 0, (int)(fade.getProgress() * 150)).hashCode();
        if (isHovered()) color = new Color(12, 11, 9, (int)(fade.getProgress() * 150)).hashCode();

        context.fill(x, y, x+width, y+height, 0, color);

        if (textWidget == null) return;
        textWidget.setX(x);
        textWidget.setY(y);
        textWidget.render(context, mouseX, mouseY, delta);

        context.getMatrices().pop();
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void setRealX(int x) {
        realX = x;
    }

    public void setRealY(int y) {
        realY = y;
    }

    public int getRealX() {
        return realX;
    }

    public int getRealY() {
        return realY;
    }

    public void setWidth(int width) {
        this.width = width;
        textWidget.setWidth(width);
    }

    public void forEachChild(Consumer<ClickableWidget> consumer) {}



    @Override
    public void setFocused(boolean focused) {}

    @Override
    public boolean isFocused() {
        return false;
    }

    @Override
    public SelectionType getType() {
        return SelectionType.NONE;
    }
    public boolean isHovered() {
        return this.hovered;
    }

    @Override
    public void appendNarrations(NarrationMessageBuilder builder) {

    }

    public ScreenRect getNavigationFocus() {
        return new ScreenRect(this.getX(), this.getY(), this.getWidth(), this.getHeight());
    }


    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isHovered()) return false;
        onPress(mouseX, mouseY, button);
        return true;
    }

    @Override
    public void onPress(double mouseX, double mouseY, int button) {
        onPress.onPress(this);
    }

    public void setText(Text message) {
        textWidget = new TextWidget(x, y, width, height, message, textRenderer);
    }


    private int z = 0;
    public void setZ(int z) {
        this.z = z;
    }

    @Environment(EnvType.CLIENT)
    public interface PressAction {
        void onPress(ButtonElement button);
    }
}
