package com.michielo.antivpn.cache;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class FlatFileCache extends AbstractCache {

    private final File cacheFile;
    private FileConfiguration cacheConfig;

    public FlatFileCache(JavaPlugin plugin) {
        super(plugin);
        cacheFile = new File(plugin.getDataFolder(), "cache.yml");
        if (!cacheFile.exists()) {
            try {
                cacheFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create cache.yml: " + e.getMessage());
            }
        }
        cacheConfig = YamlConfiguration.loadConfiguration(cacheFile);
    }

    private void saveCacheFile() {
        try {
            cacheConfig.save(cacheFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save cache.yml: " + e.getMessage());
        }
    }

    @Override
    public void store(String key, String value, long expirationTime) {
        long expiration = System.currentTimeMillis() + expirationTime;
        String id = UUID.randomUUID().toString();

        // Store the value as a plain string
        cache.put(key, new CacheEntry(value, expiration));

        // Store in YAML file
        cacheConfig.set(id + ".key", key);
        cacheConfig.set(id + ".value", value);
        cacheConfig.set(id + ".expiration", expiration);
        saveCacheFile();
    }

    @Override
    public String retrieve(String key) {
        CacheEntry entry = cache.get(key);
        if (entry != null && entry.expirationTime > System.currentTimeMillis()) {
            return entry.value;
        }

        // Search in YAML file if not found in memory
        for (String id : cacheConfig.getKeys(false)) {
            String cachedKey = cacheConfig.getString(id + ".key");
            if (key.equals(cachedKey)) {
                long expiration = cacheConfig.getLong(id + ".expiration");
                if (expiration > System.currentTimeMillis()) {
                    String value = cacheConfig.getString(id + ".value");  // Get value as string
                    cache.put(key, new CacheEntry(value, expiration));
                    return value;
                } else {
                    invalidate(key);  // Invalidate if expired
                }
            }
        }
        return null;  // Not found or expired
    }

    @Override
    public void invalidate(String key) {
        cache.remove(key);
        try {
            for (String id : cacheConfig.getKeys(false)) {
                String cachedKey = cacheConfig.getString(id + ".key");
                if (key.equals(cachedKey)) {
                    cacheConfig.set(id, null);  // Remove the entry from YAML
                    saveCacheFile();
                    break;
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to invalidate cache entry: " + e.getMessage());
        }
    }

    @Override
    public void invalidateExpiredEntries() {
        super.invalidateExpiredEntries();
        long now = System.currentTimeMillis();
        try {
            for (String id : cacheConfig.getKeys(false)) {
                long expiration = cacheConfig.getLong(id + ".expiration");
                if (expiration <= now) {
                    cacheConfig.set(id, null);  // Remove expired entries
                }
            }
            saveCacheFile();
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to invalidate expired cache entries: " + e.getMessage());
        }
    }

    @Override
    public void storePermanent(String key, String value) {
        String id = UUID.randomUUID().toString();

        // Store the string value directly
        cacheConfig.set(id + ".key", key);
        cacheConfig.set(id + ".value", value);  // Directly store the string value
        cacheConfig.set(id + ".permanent", true);  // Mark as permanent
        saveCacheFile();
    }

    @Override
    public String retrievePermanent(String key) {
        for (String id : cacheConfig.getKeys(false)) {
            String cachedKey = cacheConfig.getString(id + ".key");
            boolean isPermanent = cacheConfig.getBoolean(id + ".permanent", false);
            if (key.equals(cachedKey) && isPermanent) {
                return cacheConfig.getString(id + ".value");  // Return value as string
            }
        }
        return null;  // Not found or not marked permanent
    }

    @Override
    public void removePermanent(String key) {
        for (String id : cacheConfig.getKeys(false)) {
            String cachedKey = cacheConfig.getString(id + ".key");
            boolean isPermanent = cacheConfig.getBoolean(id + ".permanent", false);
            if (key.equals(cachedKey) && isPermanent) {
                cacheConfig.set(id, null);  // Remove the permanent entry
                saveCacheFile();
                break;
            }
        }
    }

    @Override
    public void clearPermanentStorage() {
        for (String id : cacheConfig.getKeys(false)) {
            boolean isPermanent = cacheConfig.getBoolean(id + ".permanent", false);
            if (isPermanent) {
                cacheConfig.set(id, null);  // Remove all permanent entries
            }
        }
        saveCacheFile();
    }

    @Override
    public void shutdown() {
        super.shutdown();
        // Save any remaining unsaved data
        saveCacheFile();
    }
}
