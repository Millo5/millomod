package net.millo.millomod.mod.features.impl.coding.cache;

import net.millo.millomod.mod.util.gui.elements.ButtonElement;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.Text;

public abstract class HierarchyElement extends ButtonElement {

    protected MethodFolder parentFolder = null;

    public HierarchyElement(int height, Text text, PressAction onPress, TextRenderer textRenderer) {
        super(0, 0, 10, height, text, onPress, textRenderer);
    }

    protected void setParentFolder(MethodFolder methodFolder) {
        parentFolder = methodFolder;
    }

    public MethodFolder getParentFolder() {
        return parentFolder;
    }

    protected int getFolderDepth() {
        if (parentFolder == null) return 0;
        return parentFolder.getDepth();
    }

    public boolean isParentFolderOpen() {
        if (parentFolder == null) return true;
        return parentFolder.isOpen();
    }

    @Override
    public int getHeight() {
        if (!isParentFolderOpen()) return 0;
        return super.getHeight();
    }

    public abstract void delete();
}
