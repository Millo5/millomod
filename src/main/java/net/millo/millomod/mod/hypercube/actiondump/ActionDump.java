package net.millo.millomod.mod.hypercube.actiondump;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import net.millo.millomod.system.FileManager;

public class ActionDump {

    public Sound[] sounds;

    private static ActionDump instance;
    public static ActionDump getActionDump() {
        if (instance == null) {
            try {
                String a = FileManager.readJson("action_dump.json");
                if (a == null) return null;
                instance = new Gson().fromJson(a, ActionDump.class);
            } catch (Exception e) {
                return null;
            }
        }
        return instance;
    }

}
