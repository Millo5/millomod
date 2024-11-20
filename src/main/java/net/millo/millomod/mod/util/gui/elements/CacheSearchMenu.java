package net.millo.millomod.mod.util.gui.elements;

import net.millo.millomod.mod.features.impl.coding.cache.CacheGUI;
import net.millo.millomod.mod.features.impl.coding.cache.LineElement;
import net.millo.millomod.mod.hypercube.template.Template;
import net.millo.millomod.mod.hypercube.template.TemplateBlock;
import net.millo.millomod.mod.util.GlobalUtil;
import net.millo.millomod.mod.util.gui.ElementFadeIn;
import net.millo.millomod.mod.util.gui.SearchResult;
import net.millo.millomod.system.FileManager;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.function.Consumer;

public class CacheSearchMenu implements Drawable, Element, Widget, Selectable {

    private static String searchText = "";

    ElementFadeIn fade = new ElementFadeIn(ElementFadeIn.Direction.LEFT);
    private int x, y, width, height;
    private int targetX, targetHeight;

    private CacheGUI cacheGUI;

    private TextFieldElement searchTextField;
    private ButtonElement searchButton;

    // [x,y] in parameters is the top right corner
    public CacheSearchMenu(CacheGUI cacheGUI, int x, int y) {
        this.cacheGUI = cacheGUI;

        this.x = x;
        this.y = y;
        this.width = cacheGUI.width/5;
        this.height = 80;
        targetHeight = 80;

        targetX = x - width;

        searchTextField = new TextFieldElement(cacheGUI.getTextRenderer(), targetX+1, y+1, width-2, 16, Text.literal("Search"));
        searchTextField.setZ(11);
        searchTextField.setText(searchText);
        searchTextField.setChangedListener((s) -> searchText = s);
        cacheGUI.addDrawableChild(searchTextField);

        searchButton = new ButtonElement(targetX+1, y+17, (width-2)/2, 16, Text.literal("Search"), this::search, cacheGUI.getTextRenderer());
        searchButton.setZ(11);
        cacheGUI.addDrawableChild(searchButton);
    }

    public String getSearchText() {
        return searchText;
    }

    private void search(ButtonElement buttonElement) {
        System.out.println(searchTextField.getText());

        String txt = searchTextField.getText().toLowerCase();

        // Search through all files

        int totalResults = 0;

        ArrayList<SearchResult> results = new ArrayList<>();
        ArrayList<String> methodNames = cacheGUI.getAllMethods();
        for (String methodName : methodNames) {
            Template template = FileManager.readTemplate(cacheGUI.getPlotId(), methodName);

            int templateResults = 0;

            if (template == null) continue;
            for (TemplateBlock block : template.blocks) {
                LineElement line = block.toLine();
                SearchResult result = line.searchText(txt);
                if (result != null) {
                    totalResults ++;
                    templateResults ++;
                    System.out.println(methodName + ": " + line.getString() + " " + result);
                }
            }

        }

    }



    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        fade.fadeIn(delta);

        x = (int) MathHelper.clampedLerp(x, targetX, GlobalUtil.frameDelta());
        height = (int) MathHelper.clampedLerp(height, targetHeight, GlobalUtil.frameDelta());
        searchTextField.setPosition(x + 1 + fade.getXOffset(), y + 1 + fade.getYOffset());

        context.getMatrices().push();
        context.getMatrices().translate(fade.getXOffset(), fade.getYOffset(), 0);
        context.getMatrices().translate(x, y, 10);

        context.fill(0, 0, width, height, 0x96000000);
        context.drawBorder(0, 0, width, height, 0xFFFFFFFF);

        context.getMatrices().pop();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return Element.super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void setFocused(boolean focused) {

    }

    @Override
    public boolean isFocused() {
        return false;
    }

    @Override
    public ScreenRect getNavigationFocus() {
        return new ScreenRect(getX(), getY(), width, height);
    }

    @Override
    public SelectionType getType() {
        return SelectionType.NONE;
    }

    public void appendNarrations(NarrationMessageBuilder builder) {

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
    public void forEachChild(Consumer<ClickableWidget> consumer) {}
}
