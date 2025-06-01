package net.millo.millomod.mod.hypercube.template;

import com.google.gson.Gson;
import net.millo.millomod.mod.features.impl.util.Tracker;
import net.millo.millomod.mod.util.gui.GUIStyles;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

public class Template {

    public Vec3d startPos;
    public String b64Code;
    public int plotId;
    public ArrayList<TemplateBlock> blocks;

    public static Template parseItem(String codeTemplateData) {
        CodeTemplateData templateData = new Gson().fromJson(codeTemplateData, CodeTemplateData.class);
        return parse(templateData.code);
    }

    public static Template parse(String data) {
        try {
            byte[] decompressed = decompress(Base64.getDecoder().decode(data));

            Template template = new Gson().fromJson(new String(decompressed), Template.class);
            template.b64Code = data;
            template.plotId = Tracker.getPlot().getPlotId();
            return template;
        } catch (IOException e) {
            return null;
        }
    }

    private static byte[] decompress(byte[] compressedData) throws IOException {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(compressedData);
             GZIPInputStream gis = new GZIPInputStream(bis);
             ByteArrayOutputStream bos = new ByteArrayOutputStream())
        {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = gis.read(buffer)) != -1) {
                bos.write(buffer, 0, bytesRead);
            }

            return bos.toByteArray();
        }
    }

    public String getFileName() {
        return parseFileName(getName()) + "." + getMethodName();
    }

    public static String parseFileName(String fileName) {
        String replacements = "[<>:\"/\\|?*]";

        StringBuilder result = new StringBuilder();

        Matcher matcher = Pattern.compile(replacements).matcher(fileName);
        while (matcher.find()) {
            int index = replacements.indexOf(matcher.group());
            if (index != -1) matcher.appendReplacement(result, "_"+index+"_");
        }

        matcher.appendTail(result);
        return result.toString();
    }

    public static String reverseFileName(String fileName) {
        char[] forbidden = { '<', '>', ':', '"', '/', '\\', '|', '?', '*' };
        Pattern pattern = Pattern.compile("_(\\d+)_");

        StringBuilder result = new StringBuilder();
        Matcher matcher = pattern.matcher(fileName);
        int lastMatchEnd = 0;
        while (matcher.find()) {
            result.append(fileName, lastMatchEnd, matcher.start());
            int index = Integer.parseInt(matcher.group(1));
            if (index >= 0 && index < forbidden.length) {
                result.append(forbidden[index-1]);
            } else {
                result.append(matcher.group());
            }
            lastMatchEnd = matcher.end();
        }
        result.append(fileName, lastMatchEnd, fileName.length());
        return result.toString();
    }

    public String getMethodName() {
        return blocks.get(0).block;
    }

    public String getName() {
        String name = blocks.get(0).data;
        if (name == null) name = blocks.get(0).action;
        if (name.isEmpty()) name = blocks.get(0).block +" " + startPos;
        return name;
    }

    public ItemStack getItem() {

        ItemStack item = new ItemStack(Items.ENDER_CHEST);

        NbtCompound nbt = new NbtCompound();

        NbtCompound pbv = new NbtCompound();
        String data = "{\"author\":\"MILLOMOD\",\"name\":\"§6» §e#NAME\",\"version\":1,\"code\":\"#CODE\"}"
                .replace("#NAME", getName())
                .replace("#CODE", b64Code);
        pbv.putString("hypercube:codetemplatedata", data);

        nbt.put("PublicBukkitValues", pbv);

        NbtComponent custom_data = NbtComponent.of(nbt);

        item.set(DataComponentTypes.ITEM_NAME, Text.literal(getName()).setStyle(GUIStyles.BLOCK_TAG.getStyle()));
        item.set(DataComponentTypes.CUSTOM_DATA, custom_data);

        return item;
    }

}
