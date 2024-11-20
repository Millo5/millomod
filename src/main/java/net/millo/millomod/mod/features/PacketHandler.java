package net.millo.millomod.mod.features;


import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.*;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PacketHandler {

    private final Map<Class<?>, List<Method>> subscribersReceive = new HashMap<>();
    private final Map<Class<?>, List<Method>> subscribersSend = new HashMap<>();
    private final Map<Method, Feature> methodReferenceReceive = new HashMap<>();
    private final Map<Method, Feature> methodReferenceSend = new HashMap<>();


    public <T extends Feature> void register(T subscriber) {
        for (Method method : subscriber.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(HandlePacket.class)) {
                Class<?> eventType = method.getParameterTypes()[0];
                subscribersReceive.computeIfAbsent(eventType, k -> new ArrayList<>()).add(method);
                methodReferenceReceive.put(method, subscriber);
            }

            if (method.isAnnotationPresent(OnSendPacket.class)) {
                Class<?> eventType = method.getParameterTypes()[0];
                subscribersSend.computeIfAbsent(eventType, k -> new ArrayList<>()).add(method);
                methodReferenceSend.put(method, subscriber);
            }
        }
    }

    public <E extends Packet<?>> boolean handlePacket(E packet) {

        if (packet instanceof BundleS2CPacket bundle) {
            bundle.getPackets().forEach(this::handlePacket);
            return false;
        }

        List<Method> subscriberMethods = subscribersReceive.get(packet.getClass());
        if (subscriberMethods == null) return false;
        boolean re = false;
        for (Method method : subscriberMethods) {
            try {
                boolean result = (boolean) method.invoke(methodReferenceReceive.get(method), packet);
                re = re || result;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return re;
    }

    public <E extends Packet<?>> boolean onSendPacket(E packet) {
        List<Method> subscriberMethods = subscribersSend.get(packet.getClass());
        if (subscriberMethods == null) return false;
        boolean re = false;
        for (Method method : subscriberMethods) {
            try {
                boolean result = (boolean) method.invoke(methodReferenceSend.get(method), packet);
                re = re || result;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return re;
    }
}
