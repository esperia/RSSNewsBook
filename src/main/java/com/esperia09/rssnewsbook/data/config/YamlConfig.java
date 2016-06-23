package com.esperia09.rssnewsbook.data.config;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

/**
 * Created by esperia on 2016/06/24.
 */
public class YamlConfig {

    private final JavaPlugin plugin;
    private final File file;
    private final YamlConfiguration config;

    public YamlConfig(JavaPlugin plugin, String fileName) {
        if (plugin == null) {
            throw new IllegalArgumentException("plugin cannot be null");
        }
        this.plugin = plugin;
        File dataFolder = plugin.getDataFolder();
        if (dataFolder == null) {
            throw new IllegalStateException();
        }
        file = new File(dataFolder, fileName);

        // Save default config to dataFolder.
        if (!file.exists()) {
            plugin.saveResource(file.getName(), false);
        }

        // Load
        config = YamlConfiguration.loadConfiguration(file);
    }

    public YamlConfig(File file) {
        this.plugin = null;
        this.file = file;

        // Load
        config = YamlConfiguration.loadConfiguration(file);
    }

    public YamlConfiguration getConfig() {
        return config;
    }

    public void save() throws IOException {
        config.save(file);
    }

    public File getFile() {
        return file;
    }
}
