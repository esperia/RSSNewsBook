package com.esperia09.rssnewsbook.data.config;

import org.bukkit.plugin.Plugin;

import java.io.File;

/**
 * Created by esperia on 2016/06/19.
 */
public class MyConfig {

    public void init(Plugin plugin) {
        File configYml = new File(plugin.getDataFolder(), "config.yml");
        if (!configYml.exists()) {
            plugin.saveResource(configYml.getName(), false);
        }
    }
}
