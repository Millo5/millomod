package net.millo.millomod.mod.features.impl.coding.cache;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.millo.millomod.MilloMod;
import net.millo.millomod.mod.features.impl.util.teleport.TeleportHandler;
import net.millo.millomod.mod.util.gui.*;
import net.millo.millomod.system.PlayerUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.tooltip.FocusedTooltipPositioner;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.tooltip.TooltipPositioner;
import net.minecraft.client.gui.tooltip.WidgetTooltipPositioner;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Environment(EnvType.CLIENT)
public class LineElement implements ScrollableEntryI, Element, Widget, Selectable, ClickableElementI, TooltipHolder {
    private int x, y, realX, realY;
    protected int width, height;
    protected boolean hovered;
    public boolean visible = true;


    ElementFadeIn fade = new ElementFadeIn(ElementFadeIn.Direction.RIGHT);

    ArrayList<TextWidget> textWidgets = new ArrayList<>();
    HashMap<TextWidget, PressAction> pressActionMap = new HashMap<>();
    HashMap<TextWidget, String> tagMap = new HashMap<>();
    TextRenderer textRenderer;
    private boolean hasLineNum = false;

    public LineElement() {
        this.textRenderer = CacheGUI.lastOpenedGUI.getTextRenderer();
        this.x = 0;
        this.y = 0;
        this.realX = x;
        this.realY = y;
        this.height = 12;
    }
    public void init(int width, int height) {
        this.width = width;
        this.height = height;
    }



    private TextWidget createTextWidget(Text message){
        TextWidget w = new TextWidget(x, y, textRenderer.getWidth(message), height, message, textRenderer);
        textWidgets.add(w);
        return w;
    }

    public LineElement addComponent(Text message) {
        if (message.getString().isEmpty()) return this;
        createTextWidget(message);
        return this;
    }
    public LineElement addComponent(Text message, Tooltip tooltip) {
        TextWidget w = createTextWidget(message);
        w.setTooltip(tooltip);
        return this;
    }
    public LineElement addComponent(Text message, PressAction action) {
        TextWidget w = createTextWidget(message);
        pressActionMap.put(w, action);
        return this;
    }
    public LineElement addComponent(Text message, Tooltip tooltip, PressAction action) {
        TextWidget w = createTextWidget(message);
        w.setTooltip(tooltip);
        pressActionMap.put(w, action);
        return this;
    }
    public LineElement addComponent(Text message, PressAction action, String tag) {
        TextWidget w = createTextWidget(message);
        pressActionMap.put(w, action);
        tagMap.put(w, tag);
        return this;
    }
    public LineElement addComponent(Text message, Tooltip tooltip, PressAction action, String tag) {
        TextWidget w = createTextWidget(message);
        w.setTooltip(tooltip);
        pressActionMap.put(w, action);
        tagMap.put(w, tag);
        return this;
    }

    public LineElement addSpace() {
        return addComponent(Text.literal(" "));
    }
    public void setIndent(int indentation) {
        Text message = Text.literal("   ".repeat(indentation));
        textWidgets.add(0, new TextWidget(x, y, textRenderer.getWidth(message), height, message, textRenderer));
    }
    public void setLineNum(int lineNum, Vec3d targetPos) {
        Text message = Text.literal(String.valueOf(lineNum)).setStyle(GUIStyles.LINE_NUM.getStyle());
        TextWidget w = new TextWidget(x, y, 30, height, message, textRenderer);
        textWidgets.add(0, w);
        pressActionMap.put(w, button -> {
            MilloMod.MC.setScreen(null);
            TeleportHandler.teleportTo(targetPos);
        });
        this.hasLineNum = true;
    }
    public void setLineNum(int lineNum, String methodName, int progress) {
        Text message = Text.literal(String.valueOf(lineNum)).setStyle(GUIStyles.LINE_NUM.getStyle());
        TextWidget w = new TextWidget(x, y, 30, height, message, textRenderer);
        textWidgets.add(0, w);
        pressActionMap.put(w, button -> {
            MilloMod.MC.setScreen(null);
            TeleportHandler.teleportToMethod(methodName, () -> {

                TeleportHandler.teleportTo(TeleportHandler.getLastTeleportPosition().add(-1, -1.5, progress));
            });
        });
        this.hasLineNum = true;
    }


    public String getString() {
        return textWidgets.stream().map(i -> i.getMessage().getString()).collect(Collectors.joining());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (this.visible) {
            this.hovered = mouseX >= this.getRealX() && mouseY >= this.getRealY() && mouseX < this.getRealX() + this.width && mouseY < this.getRealY() + this.height;
            this.renderWidget(context, mouseX, mouseY, delta);
        }
    }

    public SearchResult searchText(String methodName, String query) {
        query = query.toLowerCase();
        int ind = 0;
        for (TextWidget textWidget : textWidgets) {
            if (textWidget.getMessage().getString().toLowerCase().contains(query)) {
                return new SearchResult(methodName, this, ind);
            }
            ind++;
        }
        return null;
    }

    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        fade.fadeIn(delta);

        int x = getX() + fade.getXOffset();
        int y = getY() + fade.getYOffset();

        int xOff = 0;
        context.getMatrices().push();

        if (hasLineNum) {
            context.fill(x + 30, y, x + 31, y + height, 0xff333333);
        }

        int ind = 0;
        String searchHighlight = CacheGUI.lastOpenedGUI.getSearchText().toLowerCase();

        for (TextWidget textWidget : textWidgets) {
            ind ++;

            textWidget.setX(x);
            textWidget.setY(y);

            // Background Hover
            int xx = textWidget.getX() + xOff + getRealX();
            int yy = textWidget.getY() + getRealY();
            boolean hovered = mouseX >= xx && mouseX < xx+textWidget.getWidth() && mouseY >= yy && mouseY < yy + textWidget.getHeight();
            boolean tooltip = false;
            if (hovered) {
                context.fill(x, y, x+textWidget.getWidth(), y + textWidget.getHeight(), new Color(255, 255, 255, 20).hashCode());
                if (textWidget.getTooltip() != null) {
                    setTooltip(textWidget.getTooltip());
                    tooltip = true;
                }
            }
            if (!searchHighlight.isEmpty() && textWidget.getMessage().getString().toLowerCase().contains(searchHighlight)) {
                context.fill(x, y, x+textWidget.getWidth(), y + textWidget.getHeight(), new Color(0, 255, 255, 80).hashCode());
                context.fill(x, y+textWidget.getHeight()-2, x+textWidget.getWidth(), y+textWidget.getHeight()-1, new Color(255, 255, 255).hashCode());
            }

            // Text
            textWidget.renderWidget(context, mouseX, mouseY, delta);
            Screen screen = MinecraftClient.getInstance().currentScreen;
            if (tooltip && screen instanceof GUI gui) {
                gui.setTooltip(getTooltip().getTooltip(), this.createPositioner(getNavigationFocus(), hovered, isFocused()), isFocused());
            }


            xOff += textWidget.getWidth();
            context.getMatrices().translate(textWidget.getWidth(), 0, 0);
            if (ind == 1 && hasLineNum) {
                xOff += 10;
                context.getMatrices().translate(10, 0, 0);
            }
        }
        context.getMatrices().pop();
    }

    private TooltipPositioner createPositioner(ScreenRect focus, boolean hovered, boolean focused) {
        return !hovered && focused && MinecraftClient.getInstance().getNavigationType().isKeyboard() ? new FocusedTooltipPositioner(focus) : new WidgetTooltipPositioner(focus);
    }


    public void setFade(ElementFadeIn fade) {
        this.fade = fade;
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
        int xOff = 0;
        int ind = 0;
        for (TextWidget textWidget : textWidgets) {
            ind ++;

            int xx = textWidget.getX() + xOff + getRealX();
            int yy = textWidget.getY() + getRealY();
            boolean hovered = mouseX >= xx && mouseX < xx+textWidget.getWidth() && mouseY >= yy && mouseY < yy + textWidget.getHeight();

            if (hovered && pressActionMap.containsKey(textWidget)) {
                pressActionMap.get(textWidget).onPress(this);
            }

            xOff += textWidget.getWidth();
            if (ind == 1 && hasLineNum) {
                xOff += 10;
            }
        }
    }

    public LineElement addArguments(ArrayList<ArgumentItem> arguments) {
        addComponent(Text.literal("("));
        var args = arguments.iterator();
        while (args.hasNext()) {
            ArgumentItem arg = args.next();
            arg.addTo(this);
            if (args.hasNext()) addComponent(Text.literal(", "));
        }
        addComponent(Text.literal(")"));

        return this;
    }

    public void highlight(String searchText) {

    }


    @Environment(EnvType.CLIENT)
    public interface PressAction {
        void onPress(LineElement button);
    }
}
