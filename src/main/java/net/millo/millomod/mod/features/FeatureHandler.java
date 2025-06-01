package net.millo.millomod.mod.features;

import net.millo.millomod.mod.features.impl.coding.*;
import net.millo.millomod.mod.features.impl.coding.argumentinsert.ArgumentInsert;
import net.millo.millomod.mod.features.impl.global.*;
import net.millo.millomod.mod.features.impl.global.sidechat.SideChatFeature;
import net.millo.millomod.mod.features.impl.global.websocket.SocketServe;
import net.millo.millomod.mod.features.impl.switcher.ModeSwitcher;
import net.millo.millomod.mod.features.impl.util.NotificationTray;
import net.millo.millomod.mod.features.impl.util.Tracker;
import net.millo.millomod.mod.util.RenderInfo;
import net.millo.millomod.system.Config;
import net.millo.millomod.mod.features.impl.coding.cache.PlotCaching;
import net.millo.millomod.mod.features.impl.util.teleport.TeleportHandler;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;

import java.io.IOException;
import java.util.*;

public class FeatureHandler {

    private static final HashMap<String, Feature> features = new HashMap<>();

    static PacketHandler packetHandler;
    public static void load() {
        packetHandler = new PacketHandler();

        // (@) configurable, (-) always off, (+) always on, (?) other/keybinding
        features.clear();
        register(
                new LagslayerHUD(), // @
                new PreviewSkin(), // -
                new MenuSearch(), // @
                new AutoCommand(), // @
                new NotificationTray(), // @
                new Search(), // -
                new PlotCaching(), // ?
                new Tracker(), // +
                new TeleportHandler(), // +
                new ShowTags(), // ?
                new FSToggle(), // ?
                new ModeSwitcher(), // ?
                new NotSwitcher(), // ?
                new CodeClientPlotFix(),
                new ArgumentInsert(), // @
                new NoClientClick(), // @
                new SideChatFeature(), // @
//                new CodeHider(), // @
                new SocketServe(), // +
                new PickChestValue(), // ?
                new SpectatorToggle(), // ?  -Cannot exit rn
                new SoundPreview(), // @
                new ActionDumpReader(), // +
                new AngelsGrace() // @
        );

        Config config = Config.getInstance();
        defaultConfig(config);
        configUpdate(config);

        try {
            config.saveConfig();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private static void register(Feature feature) {
        packetHandler.register(feature);
        features.put(feature.getKey(), feature);
    }

    private static void register(Feature ...features) {
        for (Feature feature : features) {
            register(feature);
        }
    }



//     Pass through
    public static <T extends PacketListener> boolean handlePacket(Packet<T> packet) {
        return packetHandler.handlePacket(packet);
    }
    public static <T extends PacketListener> boolean onSendPacket(Packet<T> packet) {
        return packetHandler.onSendPacket(packet);
    }

    public static void renderHUD(RenderInfo info) {
        for (Feature feature : getFeatures()) {
            if (feature instanceof IRenderable) {
                ((IRenderable) feature).render(info);
            }
        }
    }

    public static void configUpdate(Config config) {
        features.forEach((key, feature) -> {
            feature.onConfigUpdate(config);
        });
    }
    private static void defaultConfig(Config config) {
        features.forEach((key, feature) -> feature.defaultConfig(config));
    }


    // Getters // setters

    public static Collection<Feature> getFeatures() {
        return features.values();
    }
    public static Feature getFeature(String key) {
        return features.get(key);
    }
    public static <T extends Feature> Feature getFeature(Class<T> clazz) {
        return features.values().stream()
                .filter(clazz::isInstance)
                .findFirst()
                .orElse(null);
    }


    public static void onRender(MatrixStack matrices, VertexConsumerProvider.Immediate vertexConsumers, double cameraX, double cameraY, double cameraZ) {
        for (Feature feature : getFeatures()) {
            if (feature instanceof IWorldRenderable) {
                ((IWorldRenderable) feature).renderWorld(matrices, vertexConsumers, cameraX, cameraY, cameraZ);
            }
        }
    }

    public static void onTick() {
        getFeatures().forEach(Feature::onTick);
    }

    public static boolean scrollInHotBar(double scrollAmount) {
        boolean re = false;
        for (Feature feature : getFeatures()) re = re || feature.scrollInHotbar(scrollAmount);
        return re;
    }
}
