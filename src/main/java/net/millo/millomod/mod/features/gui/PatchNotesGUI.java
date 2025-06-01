package net.millo.millomod.mod.features.gui;

import net.millo.millomod.SoundHandler;
import net.millo.millomod.mod.features.impl.global.patchnotes.PatchNoteRegistry;
import net.millo.millomod.mod.features.impl.global.patchnotes.PatchNotes;
import net.millo.millomod.mod.util.gui.GUI;
import net.millo.millomod.mod.util.gui.elements.ButtonElement;
import net.minecraft.text.Text;

import java.util.ArrayList;

public class PatchNotesGUI extends GUI {
    public PatchNotesGUI() {
        super(Text.literal("Patch Notes"));
    }

    @Override
    protected void init() {
        super.init();

        ArrayList<PatchNotes> patches = PatchNoteRegistry.getPatchNotes();
        patches.sort((a, b) -> b.getVersion().compareTo(a.getVersion()));

        int x = paddingX + 20;
        int y = paddingY + 20;
        for (PatchNotes patch : patches) {

            ButtonElement button = new ButtonElement(x, y, 100, 20,
                    Text.literal(patch.getName()), (b) -> {
                SoundHandler.playClick();
                PatchNotesContentGUI contentGUI = new PatchNotesContentGUI(patch);
                contentGUI.setParent(this);
                contentGUI.setFade(this.getFade());
                contentGUI.open();
            }, textRenderer);

            button.setTooltip(Text.literal("Version: " + patch.getVersion()));
            addDrawableChild(button);

            x += 110;
            if (x + 100 > backgroundWidth + paddingX) {
                x = paddingX;
                y += 30;
            }
        }

        if (x == paddingX && y == paddingY) {
            addDrawableChild(new ButtonElement(x, y, 100, 20, Text.literal("No Patch Notes Available"), (b) -> {}, textRenderer));
        }

        addDrawableChild(new ButtonElement(paddingX + 20, paddingY + backgroundHeight - 30, 100, 20, Text.literal("Back"), (b) -> {
            SoundHandler.playClick();
            close();
        }, textRenderer));

    }
}
