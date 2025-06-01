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
        return generateContent(
                PatchNote.header("New Features"),
                PatchNote.feature("Angels Grace", "Start flying in dev mode whenever you fall with a menu open"),
                PatchNote.feature("Patch Notes", "Added actual patch notes. Unbelievable, I know."),
                PatchNote.feature("/cache clear <id>"),
                PatchNote.feature("/cache folder"),

                PatchNote.header("Bug Fixes"),
                PatchNote.bugFix("Action Dump Reader")
        );
    }

}
