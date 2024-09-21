package com.michielo.antivpn.cache;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;
import java.sql.*;

public class H2Cache extends AbstractCache {

    private Connection connection;

    public H2Cache(JavaPlugin plugin) {
        super(plugin);
        initializeDatabase();
    }

    private void initializeDatabase() {
        try {
            // Load H2 driver (if needed)
            Class.forName("org.h2.Driver");  // Use the correct driver class if shaded

            // Get settings from config
            String filePath = plugin.getConfig().getString("cache.h2.file_path", "cache");
            String user = plugin.getConfig().getString("cache.h2.username", "sa");
            String password = plugin.getConfig().getString("cache.h2.password", "");
            String mode = plugin.getConfig().getString("cache.h2.mode", "embedded");

            // Construct JDBC URL based on mode
            String url;
            if ("memory".equalsIgnoreCase(mode)) {
                url = "jdbc:h2:mem:" + filePath;  // Memory mode
            } else if ("server".equalsIgnoreCase(mode)) {
                url = "jdbc:h2:tcp://localhost/" + filePath;  // Server mode
            } else {
                // Embedded mode, default case
                url = "jdbc:h2:file:" + plugin.getDataFolder().getAbsolutePath() + "/" + filePath;
            }

            // Log the URL for debugging
            plugin.getLogger().info("Connecting to database with URL: " + url);

            // Establish connection to H2 database
            connection = DriverManager.getConnection(url, user, password);

            // Create the cache table if it doesn't exist
            try (Statement stmt = connection.createStatement()) {
                // create table
                stmt.execute("CREATE TABLE IF NOT EXISTS cache (id VARCHAR(255) PRIMARY KEY)");
                // add key column (VARCHAR)
                stmt.execute("ALTER TABLE cache ADD COLUMN IF NOT EXISTS \"key\" VARCHAR(255)");
                // add value column (VARCHAR)
                stmt.execute("ALTER TABLE cache ADD COLUMN IF NOT EXISTS \"value\" VARCHAR(255)");
                // add expiration
                stmt.execute("ALTER TABLE cache ADD COLUMN IF NOT EXISTS \"expiration\" BIGINT");
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to initialize H2 database: " + e.getMessage());
            e.printStackTrace();  // Log full stack trace for debugging
        } catch (ClassNotFoundException e) {
            plugin.getLogger().severe("Failed to load H2 driver: " + e.getMessage());
        }
    }


    @Override
    public void store(String key, String value, long expirationTime) {
        long expiration = System.currentTimeMillis() + expirationTime;
        String id = UUID.randomUUID().toString();

        String insertSql = "INSERT INTO \"CACHE\" (\"ID\", \"key\", \"value\", \"expiration\") VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(insertSql)) {
            stmt.setString(1, id);
            stmt.setString(2, key);
            stmt.setString(3, value);
            stmt.setLong(4, expiration);
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Error inserting into CACHE: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public String retrieve(String key) {
        CacheEntry entry = cache.get(key);
        if (entry != null && entry.expirationTime > System.currentTimeMillis()) {
            return (String) entry.value;  // Return the cached String value
        }

        try (PreparedStatement pstmt = connection.prepareStatement("SELECT \"value\", \"expiration\" FROM \"CACHE\" WHERE \"key\" = ?")) {
            pstmt.setString(1, key);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    long expiration = rs.getLong("expiration");
                    if (expiration > System.currentTimeMillis()) {
                        String value = rs.getString("value");  // Retrieve the value as a String
                        cache.put(key, new CacheEntry(value, expiration));  // Store in memory cache
                        return value;
                    } else {
                        invalidate(key);  // Invalidate the entry if expired
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to retrieve cache entry from H2: " + e.getMessage());
        }
        return null;  // Return null if the key is not found or expired
    }

    @Override
    public void invalidate(String key) {
        cache.remove(key);
        try (PreparedStatement pstmt = connection.prepareStatement("DELETE FROM \"CACHE\" WHERE \"key\" = ?")) {
            pstmt.setString(1, key);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to invalidate cache entry in H2: " + e.getMessage());
        }
    }

    @Override
    public void invalidateExpiredEntries() {
        super.invalidateExpiredEntries();
        try (PreparedStatement pstmt = connection.prepareStatement("DELETE FROM \"CACHE\" WHERE \"expiration\" <= ?")) {
            pstmt.setLong(1, System.currentTimeMillis());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to invalidate expired entries in H2: " + e.getMessage());
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
            plugin.getLogger().warning("Failed to close H2 database connection: " + e.getMessage());
        }
    }

}
