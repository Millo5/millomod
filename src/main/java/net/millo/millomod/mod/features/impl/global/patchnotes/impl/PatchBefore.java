package net.millo.millomod.mod.features.impl.global.patchnotes.impl;

import net.millo.millomod.mod.features.impl.global.patchnotes.PatchNotes;

import java.util.ArrayList;

public class PatchBefore extends PatchNotes {
    @Override
    public String getVersion() {
        return "Before v1.7.3";
    }

    @Override
    public String getTitle() {
        return getVersion();
    }

    @Override
    public ArrayList<PatchNote> getContent() {
        ArrayList<PatchNote> content = new ArrayList<>();
        content.add(PatchNote.header("Cache Changes"));
        content.add(PatchNote.change("Cache Search", "Result is shown in chat instead of just the console"));
        content.add(PatchNote.change("Instant Caching", "Caching is now instant, if you're an admin :)"));
        content.add(PatchNote.feature("Cache Export", "Export an entire plot to a folder in transpiled code"));
        content.add(PatchNote.feature("Cache Diff", "Compare two plots and see the differences in transpiled code"));
        content.add(PatchNote.feature("Cache List"));
        content.add(PatchNote.bugFix("Illegal characters in method names"));

        content.add(PatchNote.header("Other"));
        content.add(PatchNote.bugFix("Sound Preview Crash", "Whenever the actiondump was not loaded correctly, the sound preview would crash the game"));
        content.add(PatchNote.change("Command Wheel commands are configurable in the config file"));

        return content;
    }
}
