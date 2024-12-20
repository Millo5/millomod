package net.millo.millomod.mod.features.impl.coding;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.millo.millomod.MilloMod;
import net.millo.millomod.mod.features.Feature;
import net.millo.millomod.mod.features.HandlePacket;
import net.millo.millomod.mod.hypercube.actiondump.ActionDump;
import net.millo.millomod.system.FileManager;
import net.millo.millomod.system.PlayerUtil;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.text.Text;

public class ActionDumpReader extends Feature {
    @Override
    public String getKey() {
        return "action_dump_reader";
    }

    private boolean reading = false;
    private StringBuilder fullDump;

    public void read() {
        reading = true;
        PlayerUtil.sendCommand("dumpactioninfo");
        fullDump = new StringBuilder();
    }

    @HandlePacket
    public boolean onChat(GameMessageS2CPacket message) {
        if (!reading) return false;
        String content = message.content().getString();

        if (content.startsWith("Error:")) {
            reading = false;
            return false;
        }

        fullDump.append(content.trim());

        reading = !content.equals("}");
        if (!reading) {
            JsonObject json = JsonParser.parseString(fullDump.toString()).getAsJsonObject();
            FileManager.writeJson("action_dump.json", json);

            MilloMod.MC.player.sendMessage(Text.of("Action dump saved!"), false);
        }
        return true;
    }

}
