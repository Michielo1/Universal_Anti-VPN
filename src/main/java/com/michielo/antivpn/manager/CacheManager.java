package com.michielo.antivpn.manager;

import com.michielo.antivpn.cache.AbstractCache;
import com.michielo.antivpn.cache.FlatFileCache;
import com.michielo.antivpn.cache.H2Cache;
import com.michielo.antivpn.cache.MySQLCache;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class CacheManager {

    private static CacheManager instance;
    public static CacheManager getInstance() {
        return instance;
    }

    private AbstractCache cache;

    public CacheManager(JavaPlugin plugin) {
        // set instance
        instance = this;

        // check if cache is enabled
        if (ConfigManager.getBoolean("cache.enabled")) {
            String type = ConfigManager.getString("cache.type");

            // validate valid type
            if (!type.equalsIgnoreCase("H2") &&
                    !type.equalsIgnoreCase("mysql") &&
                    !type.equalsIgnoreCase("flat")) {
                Bukkit.getLogger().severe("[AntiVPN] Invalid cache type! Defaulting to H2...");
                type = "H2";
            }

            if (type.equalsIgnoreCase("H2")) {
                this.cache = new H2Cache(plugin);
            } else if (type.equalsIgnoreCase("mysql")) {
                this.cache = new MySQLCache(plugin);
            } else if (type.equalsIgnoreCase("flat")) {
                this.cache = new FlatFileCache(plugin);
            }
        }

        // handle cleanup
        long cleanupInterval = ConfigManager.getLong("cache.cleanup_interval") * 60 * 20; // Convert minutes to ticks
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> cache.invalidateExpiredEntries(), cleanupInterval, cleanupInterval);
    }

    public void shutdown() {
        this.cache.shutdown();
    }

    public AbstractCache getCache() {
        return this.cache;
    }

}
