package com.esperia09.rssnewsbook.data.storage;

import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

public class MyFileUtils {

    public static File getNewsCacheFile(Plugin plugin, String newsId) throws IOException {
        File newscacheDir = new File(plugin.getDataFolder(), "cachednews");
        if (!newscacheDir.exists()) {
            if (!newscacheDir.mkdirs()) {
                throw new IOException(String.format(Locale.US, "Cannot create dir. %1$s", newscacheDir.getAbsolutePath()));
            }
        }
        File newsCacheFile = new File(newscacheDir.getAbsoluteFile(), newsId + ".xml");
        return newsCacheFile;
    }

}
