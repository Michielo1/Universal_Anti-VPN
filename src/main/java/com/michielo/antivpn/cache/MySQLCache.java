package com.michielo.antivpn.cache;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;
import java.sql.*;

public class MySQLCache extends AbstractCache {

    private Connection connection;

    public MySQLCache(JavaPlugin plugin) {
        super(plugin);
        initializeDatabase();
    }

    private void initializeDatabase() {
        try {
            // Load MySQL driver
            Class.forName("com.mysql.jdbc.Driver");

            // Get settings from config
            String host = plugin.getConfig().getString("cache.mysql.host", "localhost");
            String port = plugin.getConfig().getString("cache.mysql.port", "3306");
            String database = plugin.getConfig().getString("cache.mysql.database_name", "antivpn");
            String user = plugin.getConfig().getString("cache.mysql.username", "root");
            String password = plugin.getConfig().getString("cache.mysql.password", "");

            // Construct JDBC URL
            String url = "jdbc:mysql://" + host + ":" + port + "/" + database;

            // Log the URL for debugging
            plugin.getLogger().info("Connecting to database with URL: " + url);

            // Establish connection to MySQL database
            connection = DriverManager.getConnection(url, user, password);

            // Create the cache table if it doesn't exist
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("CREATE TABLE IF NOT EXISTS cache (" +
                        "id VARCHAR(255) PRIMARY KEY, " +
                        "`key` VARCHAR(255), " +
                        "`value` VARCHAR(255), " +
                        "expiration BIGINT)");
            }

        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to initialize MySQL database: " + e.getMessage());
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            plugin.getLogger().severe("Failed to load MySQL driver: " + e.getMessage());
        }
    }

    @Override
    public void store(String key, String value, long expirationTime) {
        long expiration = System.currentTimeMillis() + expirationTime;
        String id = UUID.randomUUID().toString();

        String insertSql = "INSERT INTO cache (id, `key`, `value`, expiration) VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(insertSql)) {
            stmt.setString(1, id);
            stmt.setString(2, key);
            stmt.setString(3, value);
            stmt.setLong(4, expiration);
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Error inserting into cache: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public String retrieve(String key) {
        CacheEntry entry = cache.get(key);
        if (entry != null && entry.expirationTime > System.currentTimeMillis()) {
            return (String) entry.value;
        }

        try (PreparedStatement pstmt = connection.prepareStatement("SELECT `value`, expiration FROM cache WHERE `key` = ?")) {
            pstmt.setString(1, key);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    long expiration = rs.getLong("expiration");
                    if (expiration > System.currentTimeMillis()) {
                        String value = rs.getString("value");
                        cache.put(key, new CacheEntry(value, expiration));
                        return value;
                    } else {
                        invalidate(key);
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to retrieve cache entry from MySQL: " + e.getMessage());
        }
        return null;
    }

    @Override
    public void invalidate(String key) {
        cache.remove(key);
        try (PreparedStatement pstmt = connection.prepareStatement("DELETE FROM cache WHERE `key` = ?")) {
            pstmt.setString(1, key);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to invalidate cache entry in MySQL: " + e.getMessage());
        }
    }

    @Override
    public void invalidateExpiredEntries() {
        super.invalidateExpiredEntries();
        try (PreparedStatement pstmt = connection.prepareStatement("DELETE FROM cache WHERE expiration <= ?")) {
            pstmt.setLong(1, System.currentTimeMillis());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to invalidate expired entries in MySQL: " + e.getMessage());
        }
    }

    @Override
    public void shutdown() {
        super.shutdown();
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to close MySQL database connection: " + e.getMessage());
        }
    }
}