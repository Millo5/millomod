package net.millo.millomod.mod.features.impl.switcher;

import net.fabricmc.fabric.impl.client.keybinding.KeyBindingRegistryImpl;
import net.millo.millomod.MilloMod;
import net.millo.millomod.mod.Callback;
import net.millo.millomod.mod.features.Feature;
import net.millo.millomod.mod.features.FeatureHandler;
import net.millo.millomod.mod.features.IRenderable;
import net.millo.millomod.mod.features.Keybound;
import net.millo.millomod.mod.features.impl.util.NotificationTray;
import net.millo.millomod.mod.util.GlobalUtil;
import net.millo.millomod.mod.util.RenderInfo;
import net.millo.millomod.mod.util.gui.GUIStyles;
import net.millo.millomod.system.PlayerUtil;
import net.millo.millomod.system.Config;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.awt.*;
import java.util.ArrayList;

public class ModeSwitcher extends Feature implements Keybound, IRenderable {

    int x, y, width, height;
    KeyBinding openKey;

    ArrayList<Option> options = new ArrayList<>();
    private int page = 0;
    private float shown = 0f;
    private float rotateBump = 0f;

    private static void addCommandOption(ArrayList<Option> list, String text, String command) {
        list.add(new Option(Text.of(text), () -> PlayerUtil.sendCommand(command)));
    }

    @Override
    public boolean scrollInHotbar(double amount) {
        if (shown > 0.1f) {
            setPage((int) (page - amount));
            rotateBump = (float) (1.2f * MathHelper.clamp(amount, -1f, 1f));
            return true;
        }
        return false;
    }

    public void setPage(int page) {
        options = getPage(page);
    }

    private static ArrayList<ArrayList<Option>> pages;


    private ArrayList<Option> getPage(int page) {
        if (pages.isEmpty()) return new ArrayList<>();

        this.page = (pages.size() + page) % pages.size();
        return pages.get(this.page);
    }

    public ModeSwitcher() {
        onConfigUpdate(Config.getInstance());

        options = getPage(0);
    }

    @Override
    public void defaultConfig(Config config) {
        super.defaultConfig(config);
        config.set("mode_switcher.pages", 5);

        config.set("mode_switcher.0.0", "Dev;dev");
        config.set("mode_switcher.0.1", "Play;play");
        config.set("mode_switcher.0.2", "Build;build");

        config.set("mode_switcher.1.0", "Not;not");
        config.set("mode_switcher.1.1", "Cancel;cancel");
        config.set("mode_switcher.1.2", "Refer;reference");
        config.set("mode_switcher.1.3", "B.F.S.;bracket");
        config.set("mode_switcher.1.4", "G. Val;val");
        config.set("mode_switcher.1.5", "Values;values");


        config.set("mode_switcher.2.0", "Spawn;s");
        config.set("mode_switcher.2.1", "Node 1;server node1");
        config.set("mode_switcher.2.2", "Node 2;server node2");
        config.set("mode_switcher.2.3", "Node 3;server node3");
        config.set("mode_switcher.2.4", "Node 4;server node4");
        config.set("mode_switcher.2.5", "Node 5;server node5");
        config.set("mode_switcher.2.6", "Node 6;server node6");
        config.set("mode_switcher.2.7", "Node 7;server node7");
        config.set("mode_switcher.2.8", "Beta;server beta");

        config.set("mode_switcher.3.0", "C l;c l");
        config.set("mode_switcher.3.1", "C g;c g");
        config.set("mode_switcher.3.2", "C n;c n");
        config.set("mode_switcher.3.3", "C dnd;c dnd");
        config.set("mode_switcher.3.4", "Auto @;auto_command");

        config.set("mode_switcher.4.0", "Creat;gmc");
        config.set("mode_switcher.4.1", "Adven;gma");
        config.set("mode_switcher.4.2", "Survi;gms");
        config.set("mode_switcher.4.3", "Spect;gmsp");
    }

    @Override
    public void onConfigUpdate(Config config) {
        super.onConfigUpdate(config);

        pages = new ArrayList<>();
        for (int i = 0; i < config.getOrDefault("mode_switcher.pages", 0); i++) {
            ArrayList<Option> page = new ArrayList<>();
            for (int j = 0; j < 9; j++) {
                String value = config.getOrDefault("mode_switcher." + i + "." + j, null);
                if (value == null) continue;
                String[] parts = value.split(";");

                if (parts.length == 2) {
                    addCommandOption(page, parts[0], parts[1]);
                }
            }
            pages.add(page);
        }

        options = getPage(0);
    }

    @Override
    public void render(RenderInfo info) {
        shown = MathHelper.lerp(info.delta(), shown, openKey.isPressed() ? 1f : 0f);
        if (shown < 0.01f) return;
        rotateBump = MathHelper.lerp(info.delta(), rotateBump, 0f);

        DrawContext context = info.context();
        TextRenderer textRenderer = info.textRenderer();
        int centerX = info.width()/2;
        int centerY = info.height()/2;


        for (int i = 0; i < options.size(); i++) {
            double angle = Math.toRadians(i * (360d / options.size()) - 90) - (1f - shown) * 0.8d + rotateBump;
            int x = centerX + (int) (Math.cos(angle) * 80 * shown);
            int y = centerY + (int) (Math.sin(angle) * 80 * shown);

            Option option = options.get(i);
            option.draw(context, x, y, textRenderer, info.delta(), shown);
        }


        int totalPages = pages.size();
        int w = ((totalPages) * 5) / 2 - 1;
        context.getMatrices().push();
        context.getMatrices().translate(centerX, centerY + 10, 0);
        context.getMatrices().scale(shown, shown, 0);

        for (int i = 0; i < totalPages; i++) {
            if (i == page) {
                context.fill(i * 5 - w - 1, -1, i * 5 + 3 - w, 3, Color.white.hashCode());
            } else {
                context.fill(i * 5 - w, 0, i * 5 + 2 - w, 2, Color.gray.hashCode());
            }
        }

        context.getMatrices().pop();

    }


    boolean cameraLocked = false;
    @Override
    public void onTick() {
        if (!GlobalUtil.isKeyDown(openKey) || MilloMod.MC.currentScreen != null) {
            if (cameraLocked) {
                MilloMod.MC.mouse.lockCursor();
                cameraLocked = false;

                for (Option option : options) {
                    if (option.isSelected()) {
                        option.trigger();
                        return;
                    }
                }
            }
            return;
        }

        if (!cameraLocked) {
            page = 0;
            options = getPage(0);
        }

        cameraLocked = true;
        MilloMod.MC.mouse.unlockCursor();

        double mouseX = MilloMod.MC.mouse.getX();
        double mouseY = MilloMod.MC.mouse.getY();

        int centerX = MilloMod.MC.getWindow().getWidth() / 2;
        int centerY = MilloMod.MC.getWindow().getHeight() / 2;

        int dx = (int) (mouseX - centerX);
        int dy = (int) (mouseY - centerY);
        float dist = (float) Math.sqrt(dx*dx + dy*dy);
        if (dist < 40) {
            options.forEach(i -> i.setSelected(false));
            return;
        }


        double mouseAngle = Math.atan2(mouseY - centerY, mouseX - centerX);
        if (mouseAngle < 0) {
            mouseAngle += 2 * Math.PI;
        }

        double angleSize = Math.toRadians(360d / options.size());
        for (int i = 0; i < options.size(); i++) {
            double angle = i * angleSize - Math.toRadians(90);

            if (angle < 0) {
                angle += 2 * Math.PI;
            }

            options.get(i).setSelected(angle > mouseAngle - angleSize / 2d && angle < mouseAngle + angleSize / 2d);
        }
    }

    private static class Option {
        private boolean selected = false;
        private final Text text;
        private float hover = 0f;
        private Callback callback;
        public Option(Text text, Callback callback) {
            this.text = text;
            this.callback = callback;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }

        public boolean isSelected() {
            return selected;
        }

        public void draw(DrawContext context, int x, int y, TextRenderer textRenderer, float delta, float shown) {
            hover = MathHelper.clampedLerp(hover, isSelected() ? 1f : 0f, delta);

            if (selected) drawMouseLine(context, x, y);

            context.getMatrices().push();
            context.getMatrices().translate(x, y, 0);
            context.getMatrices().scale(shown, shown, 0);
            context.getMatrices().scale(hover*0.2f+1f, hover*0.2f+1f, 1);

            int color = new Color(0f, 0f, 0f, 0.2f + hover * 0.3f).hashCode();
            int borderCol = new Color(1f-hover, 1f, 1f, 1f).hashCode();
            context.fill(-20, -20, 20, 20, color);
            context.drawBorder(-20, -20, 40, 40, borderCol);

            int w = textRenderer.getWidth(text);
            context.drawText(textRenderer, text, -w / 2, -5, Color.WHITE.hashCode(), true);

            context.getMatrices().pop();
        }

        private void drawMouseLine(DrawContext context, int x, int y) {
            var window = MilloMod.MC.getWindow();
            double mouseX = MilloMod.MC.mouse.getX() / window.getWidth() * window.getScaledWidth();
            double mouseY = MilloMod.MC.mouse.getY() / window.getHeight() * window.getScaledHeight();;

            double dx = (x - mouseX);
            double dy = (y - mouseY);

            double dist = Math.sqrt(dx*dx + dy*dy);
            dx /= dist;
            dy /= dist;

            for (int j = 0; j < 20; j++) {
                mouseX += dx;
                mouseY += dy;

                int color = new Color(1f, 1f, 1f, (1f - j/20f) * hover).hashCode();
                context.fill((int) mouseX, (int) mouseY, (int) (mouseX+1), (int) (mouseY+1), color);
            }
        }

        public void trigger() {
            callback.run();
        }
    }

    @Override
    public String getKey() {
        return "mode_switcher";
    }

    @Override
    public void loadKeybinds() {
        openKey = KeyBindingRegistryImpl.registerKeyBinding(
                new KeyBinding(
                        "key.millo.mode_switcher",
                        InputUtil.Type.KEYSYM,
                        -1,
                        "key.category.millo"
                )
        );
    }

    public void triggerKeybind(Config config) {
        while (GlobalUtil.isKeyPressed(openKey)) {
            page = 0;
            options = getPage(page);
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
        return width;
    }
    public int getHeight() {
        return height;
    }

}
