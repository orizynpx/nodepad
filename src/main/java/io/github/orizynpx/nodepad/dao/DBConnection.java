package io.github.orizynpx.nodepad.dao;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DBConnection {
    private static final String APP_FOLDER = System.getProperty("user.home") + "/.skilltree_rpg/";
    private static final String DB_NAME = "user_data.db";
    private static final String CONNECTION_STRING = "jdbc:sqlite:" + APP_FOLDER + DB_NAME;

    public static void init() {
        try {
            // Ensure folder exists
            Files.createDirectories(Paths.get(APP_FOLDER));

            // Create Table if not exists
            try (Connection conn = connect();
                 Statement stmt = conn.createStatement()) {
                String sql = "CREATE TABLE IF NOT EXISTS documents (" +
                        "id INTEGER PRIMARY KEY, " +
                        "content TEXT)";
                stmt.execute(sql);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(CONNECTION_STRING);
    }
}
