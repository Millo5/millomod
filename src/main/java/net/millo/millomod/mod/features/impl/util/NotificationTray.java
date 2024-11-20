package net.millo.millomod.mod.features.impl.util;

import net.millo.millomod.mod.util.GlobalUtil;
import net.millo.millomod.mod.util.MathUtil;
import net.millo.millomod.mod.util.RenderInfo;
import net.millo.millomod.system.Config;
import net.millo.millomod.mod.features.Feature;
import net.millo.millomod.mod.features.IRenderable;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.awt.*;
import java.util.ArrayList;

public class NotificationTray extends Feature implements IRenderable {


    // TODO: add /dfgive give messages
    // TODO: add auto command from modeswitcher
    // TODO: Common messages in chat ( enter dev / play )
    // TODO: Optionally plot ads / boosts
    // TODO:

    public static void pushNotification(Text message) {
        notifications.add(new Notification(message, notifications.size() * 10));
    }
    public static void pushNotification(Text ...messages) {
        MutableText message = Text.empty();
        int i = 0;
        for (Text text : messages) {
            message.append(text);
            if (++i < messages.length) message.append(" ");
        }
        notifications.add(new Notification(message, notifications.size() * 10));
    }

    static ArrayList<Notification> notifications;
    int x = 50, y = 50;
    public NotificationTray() {
        notifications = new ArrayList<>();
    }

    @Override
    public void render(RenderInfo info) {
        if (!enabled) return;
        DrawContext context = info.context();
        TextRenderer textRenderer = info.textRenderer();

        for (int i = notifications.size()-1; i > 0; i--) {
            float t = MathHelper.clamp(GlobalUtil.frameDelta(), 0f, 1f);

            Notification notification = notifications.get(i);
            float lifetime = notification.getLifeTime(GlobalUtil.frameDelta());

            float targetY = i * 10;
            float targetX = lifetime < 60 ? getWidth() : -5;

            if (lifetime > 60 && notification.x < 0f) {
                notifications.remove(notification);
            }

            notification.y = MathHelper.lerp(t, notification.y, targetY);
            notification.x = MathUtil.clampLerp(notification.x, targetX, t);

            float x = (getX() + getWidth() - notification.x);
            float y = (getY() + notification.y);

            context.getMatrices().push();
            context.getMatrices().translate(x, y, 0f);
            context.drawText(textRenderer, notification.message, 0, 0, Color.WHITE.hashCode(), true);
            context.getMatrices().pop();
        }
    }


    @Override
    public void onConfigUpdate(Config config) {
        super.onConfigUpdate(config);
        updatePosFromConfig(config);
    }

    public static class Notification {
        private float lifeTime = 0f;
        private Text message;

        public float x, y;
        public Notification(Text message, int y) {
            this.message = message;

            this.x = 0;
            this.y = y;
        }


        public float getLifeTime(float delta) {
            lifeTime += delta;
            return lifeTime;
        }

        public void setMessage(Text message) {
            this.message = message;
            this.lifeTime = 0f;
        }
    }

    public void setX(int x) {
        this.x = x;
    }
    public void setY(int y) {
        this.y = y;
    }
    public int getX() {
        return x;
    }
    public int getY() {
        return y;
    }
    public int getWidth() {
        return 200;
    }
    public int getHeight() {
        return 50;
    }

    @Override
    public String getKey() {
        return "notification_tray";
    }

}
