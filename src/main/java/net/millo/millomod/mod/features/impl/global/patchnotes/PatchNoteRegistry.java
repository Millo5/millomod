package net.millo.millomod.mod.features.impl.global.patchnotes;

import org.reflections.Reflections;

import java.util.ArrayList;
import java.util.Set;

public class PatchNoteRegistry {

    private static final ArrayList<PatchNotes> patchNotes = new ArrayList<>();
    static {
        Reflections reflections = new Reflections("net.millo.millomod.mod.features.impl.global.patchnotes.impl");
        Set<Class<? extends PatchNotes>> classes = reflections.getSubTypesOf(PatchNotes.class);
        for (Class<? extends PatchNotes> clazz : classes) {
            try {
                PatchNotes patchNote = clazz.getDeclaredConstructor().newInstance();
                patchNotes.add(patchNote);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static ArrayList<PatchNotes> getPatchNotes() {
        return patchNotes;
    }

}
