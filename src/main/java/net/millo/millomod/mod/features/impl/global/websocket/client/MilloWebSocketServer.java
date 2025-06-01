package net.millo.millomod.mod.features.impl.global.websocket.client;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import net.millo.millomod.MilloMod;
import net.millo.millomod.mod.hypercube.template.Template;
import net.millo.millomod.mod.util.ItemUtil;
import net.millo.millomod.mod.util.gui.GUIStyles;
import net.millo.millomod.system.PlayerUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;

public class MilloWebSocketServer extends WebSocketServer {

    public MilloWebSocketServer(InetSocketAddress address) {
        super(address);
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {}

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {}

    @Override
    public void onMessage(WebSocket conn, String message) {
        String res = accept(message);
        if (res != null) conn.send(res);
    }

    private enum MessageType {
        INFO,
        ERROR,
        SUCCESS
    }
    private static void message(MessageType type, String source, String message) {
        MutableText text = switch (type) {
            case INFO -> Text.literal(" » ").setStyle(GUIStyles.LINE_NUM.getStyle());
            case ERROR -> Text.literal(" » ").setStyle(GUIStyles.SCARY.getStyle());
            case SUCCESS -> Text.literal(" » ").setStyle(GUIStyles.TRUE.getStyle());
        };

        String name = source == null ? "WSS" : source;

        if (type != MessageType.INFO) text.append(Text.literal("["+name+"] ").setStyle(GUIStyles.NAME.getStyle()));
        text.append(Text.literal(message).setStyle(GUIStyles.DEFAULT.getStyle()));

        MilloMod.MC.player.sendMessage(text, false);
    }

    private static String accept(String message) {
        JsonObject result = new JsonObject();
        if (message == null) return null;

        JsonObject dataJson;
        try {
            dataJson = JsonParser.parseString(message).getAsJsonObject();
        } catch (JsonSyntaxException e) {
            message(MessageType.ERROR, null, "Failed to parse provided JSON data.");
            message(MessageType.INFO, null, e.getMessage());
            message(MessageType.INFO, null, message);
            return null;
        }

        String type = dataJson.get("type").getAsString();
        String data = dataJson.get("data").getAsString();
        String source = dataJson.get("source").getAsString();

        if (source.isEmpty()) {
            message(MessageType.ERROR, null, "No source provided!");
            return null;
        }

        if (type.equals("item")) {
            ItemStack stack;
            try {
                stack = ItemUtil.fromNbt(data);
            } catch (Exception e) {
                message(MessageType.ERROR, source, "Failed to parse provided NBT data.");
                message(MessageType.INFO, source, e.getMessage());
                return null;
            }
            PlayerUtil.giveItem(stack);
            message(MessageType.SUCCESS, source, "Received " + stack.getName().getString() + "!");
        }

        if (type.equals("template")) {
            Template template = Template.parse(data);
            if (template == null) {
                message(MessageType.ERROR, source, "Failed to parse provided template data.");
                return null;
            }

            PlayerUtil.giveItem(template.getItem());
            message(MessageType.SUCCESS, source, "Received " + template.getName() + "!");
        }

        return result.toString();
    }


    @Override
    public void onError(WebSocket conn, Exception ex) {}

    @Override
    public void onStart() {}
}
