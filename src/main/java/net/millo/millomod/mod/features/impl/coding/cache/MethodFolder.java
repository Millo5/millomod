package net.millo.millomod.mod.features.impl.coding.cache;

import net.millo.millomod.SoundHandler;
import net.millo.millomod.mod.util.gui.ElementFadeIn;
import net.millo.millomod.mod.util.gui.GUIStyles;
import net.millo.millomod.mod.util.gui.elements.ButtonElement;
import net.millo.millomod.mod.util.gui.elements.ContextElement;
import net.millo.millomod.mod.util.gui.elements.ScrollableElement;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4f;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

public class MethodFolder extends HierarchyElement {

    float openAmount = 0;

    String folderName;
    ArrayList<ButtonElement> methods;
    HashMap<String, MethodFolder> subFolders = new HashMap<>();
    public MethodFolder(int height, String folderName, PressAction onPress, TextRenderer textRenderer) {
        super(height,
                Text.of(folderName),
                onPress, textRenderer);
        methods = new ArrayList<>();

        textWidget.alignLeft();

        this.folderName = folderName;
    }


    @Override
    public void onPress(double mouseX, double mouseY, int button) {
        if (button == 0) opened = !opened;
        if (button == 1) {
            CacheGUI.lastOpenedGUI.openContext(mouseX, mouseY,
                    new ContextElement(100, textRenderer)
                            .add(Text.literal("Delete").setStyle(GUIStyles.SCARY.getStyle()), (b) -> {
                                SoundHandler.playClick();
                                delete();
                                CacheGUI.lastOpenedGUI.reload();
                                CacheGUI.lastOpenedGUI.closeContext();
                            })
            );
        }
//        if (button == 0) super.onPress(mouseX, mouseY, button);
    }

    @Override
    public void delete() {
        // delete all subfolders and methods
        for (ButtonElement method : methods) {
            if (method instanceof HierarchyElement element) {
                element.delete();
            }
        }
    }

    float arrowAngle = 0f;
    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        if (!isParentFolderOpen()) return;

        fade.fadeIn(delta);
        openAmount = MathHelper.clampedLerp(openAmount, 1f, delta);

        int x = getX() + fade.getXOffset();
        int y = getY() + fade.getYOffset();

        int xOffset = 4 * getFolderDepth();

        int color = new Color(0, 0, 0, (int)(fade.getProgress() * 150)).hashCode();
        if (isHovered()) color = new Color(12, 11, 9, (int)(fade.getProgress() * 150)).hashCode();

        context.fill(x, y, x+width, y+height, 0, color);

        // draw lil triangle
        float w = 3.4f;
        arrowAngle = MathHelper.clampedLerp(arrowAngle, isOpen() ? 1.5707f : 0f, delta);

        Matrix4f matrix4f = context.getMatrices().peek().getPositionMatrix();

        float x1 = MathHelper.cos(arrowAngle) * w;
        float y1 = MathHelper.sin(arrowAngle) * w;

        float x2 = MathHelper.cos(arrowAngle + 2.094f) * w;
        float y2 = MathHelper.sin(arrowAngle + 2.094f) * w;

        float x3 = MathHelper.cos(arrowAngle + 4.188f) * w;
        float y3 = MathHelper.sin(arrowAngle + 4.188f) * w;

        context.draw((provider) -> {
            VertexConsumer consumer = provider.getBuffer(RenderLayer.getGui());

            consumer.vertex(matrix4f, x + xOffset + x3 + 4, y + y3 + height / 2f, 0).color(0xFFFFFFFF);
            consumer.vertex(matrix4f, x + xOffset + x2 + 4, y + y2 + height / 2f, 0).color(0xFFFFFFFF);
            consumer.vertex(matrix4f, x + xOffset + x2 + 4, y + y2 + height / 2f, 0).color(0xFFFFFFFF);
            consumer.vertex(matrix4f, x + xOffset + x1 + 4, y + y1 + height / 2f, 0).color(0xFFFFFFFF);
        });


        context.draw();


        if (textWidget == null) return;
        textWidget.setX(x+10 + xOffset);
        textWidget.setY(y);
        textWidget.render(context, mouseX, mouseY, delta);
    }

    public void add(ButtonElement elementToAdd) {
        if (methods.contains(elementToAdd)) return;
        methods.add(elementToAdd);
    }

    public void addTo(ScrollableElement templates, ElementFadeIn fade) {
        setFade(fade);
        templates.addDrawableChild(this);

        for (ButtonElement method : methods) {
            if (method instanceof MethodFolder folder) {
                folder.setParentFolder(this);
                folder.setDepth(depth + 1);
                folder.addTo(templates, fade);
            } else if (method instanceof MethodElement methodElement) {
                methodElement.setParentFolder(this);
                methodElement.setFade(fade);
                templates.addDrawableChild(methodElement);
            }
        }
    }


    private boolean opened = false;
    public void open() {
        opened = true;
    }
    public void close() {
        opened = false;
    }
    public boolean isOpen() {
        return opened && isParentFolderOpen();
    }


    private int depth = 1;
    public void setDepth(int depth) {
        this.depth = depth;
    }

    public int getDepth() {
        return depth;
    }

    public MethodFolder subFolder(String folderName) {
        if (subFolders.containsKey(folderName)) {
            return subFolders.get(folderName);
        }
        MethodFolder subFolder = new MethodFolder(16, folderName, (button) -> {}, textRenderer);
        subFolders.put(folderName, subFolder);
        add(subFolder);
        return subFolder;
    }
}

