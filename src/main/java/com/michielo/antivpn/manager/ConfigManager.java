package com.michielo.antivpn.manager;

import com.michielo.antivpn.AntiVPN;

import java.util.List;

public class ConfigManager {

    private static AntiVPN plugin;

    public ConfigManager(AntiVPN p) {
        plugin = p;
        plugin.saveDefaultConfig();
    }

    public static boolean getBoolean(String key) {
        return plugin.getConfig().getBoolean(key);
    }
    public static String getString(String key) {
        return plugin.getConfig().getString(key);
    }
    public static List<String> getStringList(String key) {
        return plugin.getConfig().getStringList(key);
    }
    public static Long getLong(String key) {
        return plugin.getConfig().getLong(key);
    }

}
