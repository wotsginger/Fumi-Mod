package club.sitmc.fumiFabric;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.*;
import java.nio.file.Path;

public class FumiConfig {
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    public String url = "nats://web.sitmc.club:4222";
    public String token = "";
    public String subject = "test";
    public String sourceName = "minecraft";
    public String chatFormat = "&2&l{source} &8| &7{username} &8► &7{message}";

    public static FumiConfig load(Path configPath) {
        File configFile = configPath.toFile();
        if (!configFile.exists()) {
            FumiConfig defaultConfig = new FumiConfig();
            save(defaultConfig, configFile);
            return defaultConfig;
        }
        try (Reader reader = new FileReader(configFile)) {
            return GSON.fromJson(reader, FumiConfig.class);
        } catch (IOException e) {
            return new FumiConfig();
        }
    }

    public static void save(FumiConfig config, File file) {
        try {
            if (!file.getParentFile().exists()) file.getParentFile().mkdirs();
            try (Writer writer = new FileWriter(file)) {
                GSON.toJson(config, writer);
            }
        } catch (IOException ignored) {}
    }
}