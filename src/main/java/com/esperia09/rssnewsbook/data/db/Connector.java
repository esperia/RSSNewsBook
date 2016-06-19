package com.esperia09.rssnewsbook.data.db;

import com.esperia09.rssnewsbook.data.config.ConfigKeys;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Created by esperia on 2016/06/19.
 */
public class Connector {

    private static Connector sInstance;
    private static Connection connection;

    public static final Connector getInstance() {
        if (sInstance == null) {
            sInstance = new Connector();
        }
        return sInstance;
    }

    public void connect(Plugin plugin) throws SQLException {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("jdbc driver unavailable!", e);
        }

        // Get connection
        FileConfiguration config = plugin.getConfig();
        String username = config.getString(ConfigKeys.DB_USERNAME);
        String url = config.getString(ConfigKeys.DB_URL);
        String password = config.getString(ConfigKeys.DB_PASSWORD);
        connection = DriverManager.getConnection(url, username, password);
    }

    public void disconnect(Plugin plugin) throws SQLException {
        if (connection != null) {
            connection.close();
            connection = null;
        }
    }
}