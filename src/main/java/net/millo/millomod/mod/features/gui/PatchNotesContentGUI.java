package net.millo.millomod.mod.features.gui;

import net.millo.millomod.SoundHandler;
import net.millo.millomod.mod.features.impl.global.patchnotes.PatchNotes;
import net.millo.millomod.mod.util.gui.ElementFadeIn;
import net.millo.millomod.mod.util.gui.GUI;
import net.millo.millomod.mod.util.gui.elements.ButtonElement;
import net.millo.millomod.mod.util.gui.elements.TextElement;
import net.minecraft.text.Text;

public class PatchNotesContentGUI extends GUI {

    private final PatchNotes patch;

    public PatchNotesContentGUI(PatchNotes patch) {
        super(Text.literal(patch.getName()));
        this.patch = patch;
    }

    @Override
    protected void init() {
        super.init();

        int x = paddingX + 20;
        int y = paddingY + 20;

        TextElement text = new TextElement(x, y, backgroundWidth, 14,Text.literal("Version: " + patch.getVersion()), textRenderer);
        text.setFade(new ElementFadeIn(ElementFadeIn.Direction.UP));
        text.alignLeft();
        addDrawableChild(text);

        y += 20;
        if (patch.getTitle() != null && !patch.getTitle().isEmpty()) {
            text = new TextElement(x, y, backgroundWidth, 14,Text.literal(patch.getTitle()), textRenderer);
            text.setFade(new ElementFadeIn(ElementFadeIn.Direction.UP));
            text.alignLeft();
            y += 20;
        }

        for (PatchNotes.PatchNote note : patch.getContent()) {
            if (note.getType() == PatchNotes.PatchNote.Type.HEADER) {
                y += 10; // Add extra space for headers
            }
            text = new TextElement(x, y, backgroundWidth, 14, note.getText(), textRenderer);
            text.setFade(new ElementFadeIn(ElementFadeIn.Direction.UP));
            text.alignLeft();
            addDrawableChild(text);
            y += 14;

        }

        addDrawableChild(new ButtonElement(paddingX + 20, paddingY + backgroundHeight - 30, 100, 20, Text.literal("Back"), (b) -> {
            SoundHandler.playClick();
            close();
        }, textRenderer));
    }
}
