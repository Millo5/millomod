package net.millo.millomod.system;

import net.fabricmc.loader.api.FabricLoader;
import net.millo.millomod.mod.features.FeatureHandler;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Config {

    private static Config INSTANCE;
    private static final HashMap<String, ConfigEntry<?>> config = new HashMap<>();

    private final File file;

    @SuppressWarnings("FieldCanBeLocal")
    private boolean broken = false;


    public static class ConfigEntry<T> {
        private final String key;
        private final T defaultValue;
        private T value;

        public ConfigEntry(String key, T value) {
            this.key = key;
            this.value = value;
            defaultValue = value;
        }

        public <U> boolean isOfType(Class<U> otherType) {
            return getValue().getClass().equals(otherType);
        }

        public static ConfigEntry<?> of(String from) {
            /// key:i=5
            /// key:s=some more text
            String[] parts = from.split("[=:]", 3);
            if (parts.length != 3) {
                throw new RuntimeException("Syntax error in config file on: " + from);
            }

            String key = parts[0];
            String type = parts[1];
            String value = parts[2];

            return switch(type) {
                case "b" -> new ConfigEntry<>(key, value.equals("true"));
                case "d" -> new ConfigEntry<>(key, Double.parseDouble(value));
                case "i" -> new ConfigEntry<>(key, Integer.parseInt(value));
                case "s" -> new ConfigEntry<>(key, value);
                default -> throw new IllegalStateException("Unexpected value: " + type);
            };
        }

        public void setValue(T value) {
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public T getDefaultValue() {
            return defaultValue;
        }

        public T getValue() {
            return value;
        }

        @Override
        public String toString() {
            String type = "";
            if (value instanceof Boolean) type = "b";
            if (value instanceof Integer) type = "i";
            if (value instanceof Double) type = "d";
            if (value instanceof String) type = "s";
            return key+":"+ type +"=" + value;
        }
    }


    public Config(String filename) {
        this(Config.of(filename));
    }
    public Config(File file) {
        this.file = file;

        if (!file.exists()) {
            try {
                createConfig();
            } catch (IOException e) {
                broken = true;
            }
        }

        if (broken) return;

        try {
            loadConfig();
        } catch (IOException e) {
            broken = true;
        }

    }


    public static Config getInstance() {
        if (INSTANCE == null) INSTANCE = new Config("millomod");
        return INSTANCE;
    }

//    public <T extends Comparable<? super T>> void set(String key, T value) {
//        if (!(value instanceof Integer || value instanceof Double || value instanceof String || value instanceof Boolean)) {
//            throw new IllegalArgumentException("Type T must be Integer, Double, String, or Boolean.");
//        }
//        config.put(key, value.toString());
//    }


    /// Sets only when the key doesn't exist in the config file
    public <T> void setIfNull(String key, T value) {
        ConfigEntry<T> entry = (ConfigEntry<T>) getEntry(key);
        if (entry == null) {
            entry = new ConfigEntry<>(key, value);
            config.put(key, entry);
        }
    }

    public <T> void set(String key, T value) {
        ConfigEntry<T> entry = (ConfigEntry<T>) getEntry(key);
        if (entry != null) {
            entry.setValue(value);
            return;
        }

        entry = new ConfigEntry<>(key, value);
        config.put(key, entry);
    }

//    public <T> void set(ConfigEntry<T> entry, T value) {
//        config.put(entry.getKey(), );
//    }

    private void createConfig() throws IOException {
        file.getParentFile().mkdirs();
        Files.createFile(file.toPath());

        saveConfig();
    }
    private void loadConfig() throws IOException {
        Scanner reader = new Scanner(file);
        while (reader.hasNextLine()) {
            parseConfigEntry(reader.nextLine());
        }
    }
    public void saveConfig() throws IOException {
        // Generate the config string
        StringBuilder configString = new StringBuilder();
        for (Map.Entry<String, ConfigEntry<?>> entry : config.entrySet()) {
            configString.append(entry.getValue().toString()).append("\n");
        }

        // THE CONFIG OUTPUT MEUHTHEHAA
//        System.out.println("Saving Config: " + configString);

        // Write the config in the file
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            Files.createFile(file.toPath());
        } else {
            Files.write(file.toPath(), new byte[0]);
        }

        PrintWriter writer = new PrintWriter(file, "UTF-8");
        writer.write(configString.toString());
        writer.close();

        FeatureHandler.configUpdate(this);
    }


    public static File of(String filename) {
        Path path = FabricLoader.getInstance().getConfigDir();
        return path.resolve(filename + ".config").toFile();
    }

    private void parseConfigEntry(String entry) {
        if (entry.isEmpty() || entry.startsWith("#")) return;
        ConfigEntry<?> configEntry = ConfigEntry.of(entry);
        config.put(configEntry.getKey(), configEntry);
    }




    private ConfigEntry<?> getEntry(String key) {
        return config.get(key);
    }
    public <T> T get(String key) {
        ConfigEntry<?> entry = config.get(key);
        if (entry == null) {
            System.out.println("CONFIG NOT FOUND! >> " + key);
            return null;
        }
        return (T) entry.getValue();
    }

    public <T> T getOrDefault(String key, T defaultValue) {
        ConfigEntry<?> entry = config.get(key);
        if (entry == null) {
            return defaultValue;
        }
        return (T) entry.getValue();
    }

}
