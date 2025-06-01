package net.millo.millomod.mod.features.gui;

import net.millo.millomod.SoundHandler;
import net.millo.millomod.mod.util.gui.elements.*;
import net.millo.millomod.system.Config;
import net.millo.millomod.mod.util.gui.ElementFadeIn;
import net.millo.millomod.mod.util.gui.GUI;
import net.millo.millomod.mod.util.gui.GUIStyles;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.text.Text;

import java.io.IOException;
import java.util.function.Consumer;

public class SettingsGUI extends GUI {
    Config config;
    public SettingsGUI() {
        super(Text.literal("Settings"));
        config = Config.getInstance();
    }

    @Override
    protected void init() {
        super.init();

        ScrollableElement settingsList = new ScrollableElement(paddingX, paddingY, backgroundWidth, backgroundHeight, Text.literal(""));
        int x = backgroundWidth / 2 - 100;

        addTitle(settingsList, "config.millo.title");

        settingsList.addDrawableChild(new ButtonElement(x, 0, 200, 20, Text.translatable("config.millo.modify_positions"), (button) -> {
            GUI gui = new PositionsGUI(this);
            gui.open();
        }, textRenderer));

        addHeader(settingsList, "config.millo.features");

        addFeatureToggle(settingsList, "lagslayer");
        addFeatureToggle(settingsList, "menu_search");
        addFeatureToggle(settingsList, "auto_command");
        addFeatureToggle(settingsList, "notification_tray");
        addFeatureToggle(settingsList, "argument_insert");
        addFeatureToggle(settingsList, "no_client_click");
        addFeatureToggle(settingsList, "sound_preview");
        addFeatureToggle(settingsList, "angels_grace");


        addHeader(settingsList, "Side Chat");
        addFeatureToggle(settingsList, "side_chat");
        addStringOption(settingsList, "side_chat.filter");
        addBooleanOption(settingsList,
                "side_chat.simple_filter",
                "config.millo.side_chat.simple_filter",
                "config.millo.side_chat.simple_filter.desc");
        addBooleanOption(settingsList,
                "side_chat.private_chat",
                "config.millo.side_chat.private_chat",
                "config.millo.side_chat.private_chat.desc");
        addBooleanOption(settingsList,
                "side_chat.support_chat",
                "config.millo.side_chat.support_chat",
                "config.millo.side_chat.support_chat.desc");
        addBooleanOption(settingsList,
                "side_chat.mod_chat",
                "config.millo.side_chat.mod_chat",
                "config.millo.side_chat.mod_chat.desc");
        addBooleanOption(settingsList,
                "side_chat.admin_chat",
                "config.millo.side_chat.admin_chat",
                "config.millo.side_chat.admin_chat.desc");

//        addIntegerOption(settingsList, "fs_toggle.speed", 0, 1000);

//        addHeader(settingsList, "Unstable Features");
//        addBooleanOption(settingsList, "preview_skin.enabled","Preview Skin", "Do not use. Will crash your game");

        addDrawableChild(settingsList);
    }



    @Override
    public void close() {
        super.close();
        try {
            config.saveConfig();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
    }

    private void addTitle(ScrollableElement list, String titleKey) {
        list.addDrawableChild(new TextElement(0, 0, backgroundWidth, 30,
                Text.translatable(titleKey).setStyle(GUIStyles.TITLE.getStyle()), textRenderer)
        );
    }
    private void addHeader(ScrollableElement list, String headerKey) {
        list.addDrawableChild(new TextElement(0, 0, backgroundWidth, 30,
                Text.translatable(headerKey).setStyle(GUIStyles.HEADER.getStyle()), textRenderer)
        );
    }


    private void addStringOption(ScrollableElement list, String key) {
        String value = config.getOrDefault(key, "");
        int x = backgroundWidth / 2 - 100;

        String tooltip = Text.translatable("millo.feature."+key+".desc").getString();

        TextFieldElement field = new TextFieldElement(textRenderer, x, 0, 200, 20, Text.literal(value));
        field.setText(value);
        field.setMaxLength(999999);
        field.setChangedListener(s -> config.set(key, s.trim()));

        field.setTooltip(Tooltip.of(Text.literal(tooltip)));
        field.setFade(new ElementFadeIn(ElementFadeIn.Direction.RIGHT));
        list.addDrawableChild(field);

        addSelectableChild(field);
    }

    private void addFeatureToggle(ScrollableElement list, String key) {
        String name = Text.translatable("millo.feature."+key).getString();
        String tooltip = Text.translatable("millo.feature."+key+".desc").getString();

        addBooleanOption(list, key+".enabled", name, tooltip);
    }

    private void addBooleanOption(ScrollableElement list, String key, String name, String tooltip) {
        boolean state = config.getOrDefault(key, false);
        int x = backgroundWidth / 2 - 100;

        ButtonElement b = new ButtonElement(x, 0, 200, 20, Text.literal(name+": ").append(GUIStyles.getTrueFalse(state)), button -> {
            SoundHandler.playClick();
            boolean newState = !(boolean)config.get(key);
            config.set(key, newState);
            button.setText(Text.literal(name+": ").append(GUIStyles.getTrueFalse(newState)));
        }, textRenderer);
        b.setTooltip(Text.literal(tooltip));
        b.setFade(new ElementFadeIn(ElementFadeIn.Direction.RIGHT));

        list.addDrawableChild(b);
    }

    private void addIntegerOption(ScrollableElement list, String key, int min, int max) {
        int current = config.getOrDefault(key, 0);

        String name = Text.translatable("millo.feature."+key).getString();
        String tooltip = Text.translatable("millo.feature."+key+".desc").getString();

        int x = backgroundWidth / 2 - 100;

        IntegerElement b = new IntegerElement(x, 0, min, max, Text.literal(name), current, (change) -> config.set(key, change), textRenderer);

        list.addDrawableChild(b);
    }

}
