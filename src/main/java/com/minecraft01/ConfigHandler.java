package com.minecraft01;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.file.*;

public class ConfigHandler {
    public static final String CONFIG_FILE_NAME = "minecraft01.json";
     private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir()
            .resolve(CONFIG_FILE_NAME);

    public static MyModConfig config;

    public static void loadConfig() {
        if (Files.exists(CONFIG_PATH)) {
            try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
                config = GSON.fromJson(reader, MyModConfig.class);
            } catch (IOException e) {
                e.printStackTrace();
                config = new MyModConfig();
            }
        } else {
            config = new MyModConfig();
            config.alertWebHook="";
            config.annoucementWebHook="";
            config.cartographerWebHook="";
            saveConfig(); // Save default config
        }
    }
    public static void saveConfig() {
        try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
            GSON.toJson(config, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
