package net.millo.millomod.mod.features.gui;

import net.millo.millomod.MilloMod;
import net.millo.millomod.SoundHandler;
import net.millo.millomod.system.Config;
import net.millo.millomod.mod.commands.Command;
import net.millo.millomod.mod.commands.CommandHandler;
import net.millo.millomod.mod.features.Feature;
import net.millo.millomod.mod.features.FeatureHandler;
import net.millo.millomod.mod.util.gui.GUI;
import net.millo.millomod.mod.util.gui.GUIStyles;
import net.millo.millomod.mod.util.gui.elements.ButtonElement;
import net.millo.millomod.mod.util.gui.elements.ScrollableElement;
import net.millo.millomod.mod.util.gui.elements.TextElement;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.text.Text;

public class MilloGUI extends GUI {
    Config config;
    public MilloGUI() {
        super(Text.literal("Settings"));
        config = Config.getInstance();
    }

    @Override
    protected void init() {
        super.init();

        int x = paddingX;
        int y = paddingY;
        addDrawableChild(new TextElement(x, y + 10, backgroundWidth, 16,
                Text.literal("Millo Mod").setStyle(GUIStyles.NAME.getStyle()), textRenderer));
        addDrawableChild(new TextElement(x, y + 26, backgroundWidth, 16,
                Text.literal("v"+ MilloMod.MOD_VERSION).setStyle(GUIStyles.COMMENT.getStyle()), textRenderer));
        addDrawableChild(new TextElement(x, y + 40, backgroundWidth, 16,
                Text.literal("Nothing better than gatekeeping :3").setStyle(GUIStyles.COMMENT.getStyle()), textRenderer));

        addDrawableChild(new TextElement(x, y + 58, backgroundWidth, 16,
                Text.literal("Credits:").setStyle(GUIStyles.LINE.getStyle()), textRenderer));
        addDrawableChild(new TextElement(x, y + 68, backgroundWidth, 16,
                Text.literal("Millo5 - Thigh highs for focus").setStyle(GUIStyles.UNSAVED.getStyle()), textRenderer));
        addDrawableChild(new TextElement(x, y + 78, backgroundWidth, 16,
                Text.literal("xtreemes & endersaltz - Testing / Ideas").setStyle(GUIStyles.UNSAVED.getStyle()), textRenderer));
//        addDrawableChild(new TextElement(x, y + 88, backgroundWidth, 16,
//                Text.literal("").setStyle(GUIStyles.UNSAVED.getStyle()), textRenderer));
//        addDrawableChild(new TextElement(x, y + 98, backgroundWidth, 16,
//                Text.literal("").setStyle(GUIStyles.UNSAVED.getStyle()), textRenderer));


//        addDrawableChild(new ButtonElement(x + backgroundWidth/2 - 50, y + 116, 100, 16, Text.of("Settings"), (button) -> {
        addDrawableChild(new ButtonElement(x + backgroundWidth/2 - 50 - 55, y + 116, 100, 16, Text.of("Settings"), (button) -> {
            SoundHandler.playClick();
            GUI gui = new SettingsGUI();
            gui.setParent(this);
            gui.setFade(this.getFade());
            gui.open();
        }, textRenderer));

        addDrawableChild(new ButtonElement(x + backgroundWidth/2 - 50 + 55, y + 116, 100, 16, Text.of("Patch Notes"), (button) -> {
            SoundHandler.playClick();
            GUI gui = new PatchNotesGUI();
            gui.setParent(this);
            gui.setFade(this.getFade());
            gui.open();
        }, textRenderer));


        ScrollableElement featureList = new ScrollableElement(x + backgroundWidth/2 - 80, y + 134, 160, backgroundHeight - y - 134,
                Text.of("Feature List"));

        // Features
        featureList.addDrawableChild(
                new TextElement(0, 0, 150, 16, Text.translatable("config.millo.features").setStyle(GUIStyles.HEADER.getStyle()),textRenderer)
        );
        for (Feature feature : FeatureHandler.getFeatures()) {
            TextElement text = new TextElement(0, 0, 150, 12,
                    Text.translatable("millo.feature."+feature.getKey()).setStyle(GUIStyles.CONTROL.getStyle()), textRenderer);
            text.setTooltip(Tooltip.of(Text.translatable("millo.feature."+feature.getKey()+".desc")));
            featureList.addDrawableChild(text);
        }

        // Commands
        featureList.addDrawableChild(
                new TextElement(0, 0, 150, 16, Text.translatable("config.millo.commands").setStyle(GUIStyles.HEADER.getStyle()),textRenderer)
        );
        for (Command command : CommandHandler.getCommands()) {
            TextElement text = new TextElement(0, 0, 150, 12,
                    Text.literal("/").append(Text.translatable("millo.command." + command.getKey())).setStyle(GUIStyles.CONTROL.getStyle()), textRenderer);
            text.setTooltip(Tooltip.of(Text.translatable("millo.command."+command.getKey()+".desc")));
            featureList.addDrawableChild(text);
        }
        addDrawableChild(featureList);


    }

}
