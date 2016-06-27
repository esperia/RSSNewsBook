package com.esperia09.rssnewsbook.utils;

import java.io.File;

/**
 * Created by neske on 2016/06/27.
 */
public class YamlUtilsForTest {

    public static File getFile(String yamlFileName) {
        String resourcesDirPath = System.getProperty("user.dir")
                + File.separator + "src"
                + File.separator + "main"
                + File.separator + "resources";
        File path = new File(resourcesDirPath, "plugin.yml");

        return path;
    }
}
