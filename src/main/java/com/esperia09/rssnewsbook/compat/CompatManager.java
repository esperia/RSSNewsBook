package com.esperia09.rssnewsbook.compat;

import org.bukkit.plugin.Plugin;

/**
 * Created by esperia on 2016/06/18.
 */
public class CompatManager {

    private final String version;
    private final ICompat compat;

    public CompatManager(Plugin plugin) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        String packageName = plugin.getServer().getClass().getPackage().getName();
        version = packageName.substring(packageName.lastIndexOf('.') + 1);

        // Call Compat
        Class<?> compatClass = Class.forName("com.esperia09.rssnewsbook.compat." + version + ".Compat");
        compat = (ICompat) compatClass.newInstance();
    }

    public String getVersion() {
        return version;
    }

    public ICompat getCompat() {
        return compat;
    }
}
