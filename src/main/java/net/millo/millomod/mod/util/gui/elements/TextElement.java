package net.millo.millomod.mod.util.gui.elements;

import net.millo.millomod.mod.util.gui.ElementFadeIn;
import net.millo.millomod.mod.util.gui.ScrollableEntryI;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.text.Text;

public class TextElement extends TextWidget implements ScrollableEntryI {

    private int x, y, realX, realY;
    private ElementFadeIn fade = new ElementFadeIn(ElementFadeIn.Direction.RIGHT);

    public TextElement(Text message, TextRenderer textRenderer) {
        super(message, textRenderer);
    }

    public TextElement(int width, int height, Text message, TextRenderer textRenderer) {
        super(width, height, message, textRenderer);
    }

    public TextElement(int x, int y, int width, int height, Text message, TextRenderer textRenderer) {
        super(x, y, width, height, message, textRenderer);
        this.x = x;
        this.y = y;
    }


    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        fade.fadeIn(delta);
        super.setX(x + fade.getXOffset());
        super.setY(y + fade.getYOffset());
        super.renderWidget(context, mouseX, mouseY, delta);
    }

    public void setRealX(int x) {
        realX = x;
    }
    public void setRealY(int y) {
        realY = y;
    }
    @Override
    public int getRealX() {
        return realX;
    }

    @Override
    public int getRealY() {
        return realY;
    }

    public void setFade(ElementFadeIn fade) {
        this.fade = fade;
    }
}
