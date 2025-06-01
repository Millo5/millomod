package net.millo.millomod.mod.features.impl.global.patchnotes.impl;

import net.millo.millomod.mod.features.impl.global.patchnotes.PatchNotes;

import java.util.ArrayList;


public class Patch173 extends PatchNotes {

    @Override
    public String getVersion() {
        return "1.7.3";
    }

    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public ArrayList<PatchNote> getContent() {
        ArrayList<PatchNote> content = new ArrayList<>();
        content.add(new PatchNote(PatchNote.Type.FEATURE, "Angels Grace", "Start flying in dev mode whenever you fall with a menu open"));
        content.add(new PatchNote(PatchNote.Type.FEATURE, "Patch Notes", "Added actual patch notes. Unbelievable, I know."));
        content.add(new PatchNote(PatchNote.Type.FEATURE, "/cache clear <id>"));
        content.add(new PatchNote(PatchNote.Type.FEATURE, "/cache folder"));
        content.add(new PatchNote(PatchNote.Type.BUG_FIX, "Action Dump Reader"));
        return content;
    }

}
