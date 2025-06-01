package net.millo.millomod.system;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import net.millo.millomod.MilloMod;
import net.millo.millomod.mod.features.impl.util.NotificationTray;
import net.millo.millomod.mod.features.impl.util.Tracker;
import net.millo.millomod.mod.hypercube.template.Template;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class FileManager {

    public static File of(String filename) {
        Path path = FabricLoader.getInstance().getConfigDir();
        return path.resolve(filename).toFile();
    }

    // Cached Plots DNS

    public static Path getModFolder() {
        Path path = MilloMod.MC.runDirectory.toPath().resolve(MilloMod.MOD_ID);
        path.toFile().mkdirs();
        return path;
    }

    public static File getDNSFile() {
        Path path = getModFolder();
        File file = path.resolve("plots.dns").toFile();
        file.mkdirs();
        return file;
    }

    private static Map<Integer, String> dnsContent = new HashMap<>();
    public static void dnsRemove(int plotId) {
        dnsContent.remove(plotId);
    }
    public static void dnsAdd(int plotId, String plotName) {
        dnsContent.put(plotId, plotName);
    }
    public static void dnsSave() {
        JsonObject inf = new JsonObject();

        dnsContent.forEach((id, name) -> {
            inf.addProperty(String.valueOf(id), name);
        });

        try {
            File file = getDNSFile();
            Files.deleteIfExists(file.toPath());
            Files.createFile(file.toPath());
            if (!file.exists()) file.createNewFile();
            Files.write(file.toPath(), inf.toString().getBytes(), StandardOpenOption.WRITE);
        } catch (IOException e) {
            System.out.println("Couldn't save plot dns: " + e);
        }
    }

    public static void readDNS() {
        File file = getDNSFile();
        if (!file.exists()) {
            System.out.println("DNS file does not exist.");
            return;
        }

        try {
            String content = new String(Files.readAllBytes(file.toPath()));

            JsonObject inf = JsonParser.parseString(content).getAsJsonObject();

            dnsContent = new HashMap<>();

            inf.entrySet().forEach(entry -> {
                int id = Integer.parseInt(entry.getKey());
                String name = entry.getValue().getAsString();
                dnsContent.put(id, name);
            });
        } catch (Exception e) {
            return;
        }
    }

    public static Map<Integer, String> getDNSContent() {
        return dnsContent;
    }

    // Cached Templates

    public static Path getTemplatePath() {
        Path path = getModFolder().resolve("cache");
        path.toFile().mkdirs();
        return path;
    }
    public static Path getTemplatePlotPath(int plotId) {
        Path path = getTemplatePath().resolve(String.valueOf(plotId));
        path.toFile().mkdirs();
        return path;
    }

    public static File getTemplateFile(int plotId, String templateName) {
        Path path = getTemplatePlotPath(plotId);
        return path.resolve(templateName).toFile();
    }

    // fix writing first
    public static void writeTemplate(Template template) {
        JsonObject inf = new JsonObject();
        if (template.startPos != null) {
            var pos = new JsonArray();
            pos.add(template.startPos.x);
            pos.add(template.startPos.y);
            pos.add(template.startPos.z);
            inf.add("pos", pos);
        }
        inf.addProperty("code", template.b64Code);

        try {
            File file = getTemplateFile(template.plotId, template.getFileName());
            Files.deleteIfExists(file.toPath());
            Files.createFile(file.toPath());
            if (!file.exists()) file.createNewFile();
            Files.write(file.toPath(), inf.toString().getBytes(), StandardOpenOption.WRITE);
        } catch (IOException e) {
            System.out.println("Couldn't save template: " + e);
        }
    }

    public static void deleteTemplateFile(int plotId, String fileName) {
        File file = getTemplateFile(plotId, fileName);
        try {
            Files.deleteIfExists(file.toPath());
        } catch (IOException e) {
            System.out.println("Failed to delete: " + e);
        }
    }

    public static boolean isPlotCached(String plotId) {
        return Files.exists(getTemplatePath().resolve(plotId));
    }

    public static ArrayList<Integer> cachedPlots = new ArrayList<>();
    public static ArrayList<Integer> getCachedPlots() {
        cachedPlots = Arrays.stream(Objects.requireNonNull(getTemplatePath().toFile().listFiles()))
                .map(File::getName)
                .map(Integer::parseInt)
                .collect(Collectors.toCollection(ArrayList::new));

        return cachedPlots;
    }

    public static ArrayList<Integer> getPlotsCached() {
        if (cachedPlots.isEmpty()) return getCachedPlots();
        return cachedPlots;
    }

    public static void clearDiff() {
        try {
            File file = getModFolder().resolve("diffs").toFile();
            if (!file.exists()) return;
            Arrays.stream(Objects.requireNonNull(file.listFiles())).forEach(f -> {
                try {
                    Files.deleteIfExists(f.toPath());
                } catch (IOException e) {
                    System.out.println("Couldn't delete diff: " + e);
                }
            });
        } catch (Exception e) {
            System.out.println("Couldn't delete diffs: " + e);
        }
    }
    public static void writeDiff(String method, String data) {
        try {
            File file = getModFolder().resolve("diffs").toFile();
            if (!file.exists()) file.mkdirs();
            file = file.toPath().resolve(method + ".diff").toFile();
            Files.deleteIfExists(file.toPath());
            Files.createFile(file.toPath());
            Files.write(file.toPath(), data.getBytes(), StandardOpenOption.WRITE);
        } catch (IOException e) {
            System.out.println("Couldn't save diff: " + e);
        }
    }

    public static void writeJson(String name, JsonObject json) {
        try {
            File file = getModFolder().resolve(name).toFile();
            Files.deleteIfExists(file.toPath());
            Files.createFile(file.toPath());
            Files.write(file.toPath(), json.toString().getBytes(), StandardOpenOption.WRITE);
        } catch (IOException e) {
            System.out.println("Couldn't save json: " + e);
        }
    }

    public static String readJson(String name) {
        File file = getModFolder().resolve(name).toFile();
        if (!file.exists()) return null;

        try {
            return new String(Files.readAllBytes(file.toPath()));
        } catch (Exception e) {
            return null;
        }
    }

    public static void clearExportedPlot(int id) {
        try {
            File file = getModFolder().resolve("exports").resolve(String.valueOf(id)).toFile();
            if (!file.exists()) return;
            Arrays.stream(Objects.requireNonNull(file.listFiles())).forEach(f -> {
                try {
                    Files.deleteIfExists(f.toPath());
                } catch (IOException e) {
                    System.out.println("Couldn't delete export: " + e);
                }
            });
        } catch (Exception e) {
            System.out.println("Couldn't delete exports: " + e);
        }
    }

    public static void writeExportedPlot(int id, String method, String data) {
        try {
            File file = getModFolder().resolve("exports").resolve(String.valueOf(id)).toFile();
            if (!file.exists()) file.mkdirs();
            file = file.toPath().resolve(method).toFile();
            Files.deleteIfExists(file.toPath());
            Files.createFile(file.toPath());
            Files.write(file.toPath(), data.getBytes(), StandardOpenOption.WRITE);
        } catch (IOException e) {
            System.out.println("Couldn't save export: " + e);
        }
    }

    public static void clearCachedPlot(int id) {
        try {
            File file = getTemplatePlotPath(id).toFile();
            if (!file.exists()) return;
            Arrays.stream(Objects.requireNonNull(file.listFiles())).forEach(f -> {
                try {
                    Files.deleteIfExists(f.toPath());
                } catch (IOException e) {
                    System.out.println("Couldn't delete cached plot: " + e);
                }
            });
        } catch (Exception e) {
            System.out.println("Couldn't delete cached plot: " + e);
        }
    }

    public static void openCachedPlotsFolder() {
        int id = Tracker.getPlot().getPlotId();

        File file;
        if (id == 0) {
            file = getTemplatePath().toFile();
        } else {
            file = getTemplatePlotPath(id).toFile();
        }
        if (!file.exists()) {
            NotificationTray.pushNotification(Text.literal("Cached plots path does not exist."));
            return;
        }
        if (!file.isDirectory()) {
            NotificationTray.pushNotification(Text.literal("Cached plots path is not a directory."));
            return;
        }

        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(file);
            } else {
                NotificationTray.pushNotification(Text.literal("Desktop is not supported on this system."));
            }
        } catch (IOException e) {
            NotificationTray.pushNotification(Text.literal("Failed to open cached plots folder: " + e.getMessage()));
        }
    }


    static class ReadTemplate {
        public String code;
        public ArrayList<Double> pos;
    }
    public static Template readTemplate(int plotId, String templateName) {
        try {
            String readData = Files.readString(getTemplateFile(plotId, templateName).toPath());
            ReadTemplate inf = new Gson().fromJson(readData, ReadTemplate.class);
            Template template = Template.parse(inf.code);
            if (template == null) return null;
            if (inf.pos == null) return template;

            template.startPos = new Vec3d(inf.pos.get(0), inf.pos.get(1), inf.pos.get(2));
            return template;
        } catch (IOException e) {
            return null;
        }
    }

    public static List<String> getTemplatesFromPlot(int plotId) {
        return Arrays.stream(Objects.requireNonNull(
                getTemplatePlotPath(plotId).toFile().listFiles()))
                .map(File::getName)
                .collect(Collectors.toList());
    }



}
