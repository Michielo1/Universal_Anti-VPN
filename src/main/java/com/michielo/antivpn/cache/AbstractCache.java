package com.michielo.antivpn.cache;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public abstract class AbstractCache {
    protected ConcurrentHashMap<String, CacheEntry> cache;
    protected JavaPlugin plugin;
    private ScheduledExecutorService scheduler;

    public AbstractCache(JavaPlugin plugin) {
        this.plugin = plugin;
        this.cache = new ConcurrentHashMap<>();
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
    }

    public abstract void store(String key, String value, long expirationTime);
    public abstract void storePermanent(String key, String value);
    public abstract String retrieve(String key);
    public abstract String retrievePermanent(String key);
    public abstract void invalidate(String key);

    public void clear() {
        cache.clear();
    }
    public abstract void removePermanent(String key);
    public abstract void clearPermanentStorage();

    public int size() {
        return cache.size();
    }

    protected void scheduleInvalidation(String key, long delay) {
        scheduler.schedule(() -> invalidate(key), delay, TimeUnit.MILLISECONDS);
    }

    public void invalidateExpiredEntries() {
        long now = System.currentTimeMillis();
        cache.entrySet().removeIf(entry -> entry.getValue().expirationTime <= now);
    }

    public void shutdown() {
        scheduler.shutdown();
    }

    protected static class CacheEntry {
        String value;
        long expirationTime;

        CacheEntry(String value, long expirationTime) {
            this.value = value;
            this.expirationTime = expirationTime;
        }
    }
}