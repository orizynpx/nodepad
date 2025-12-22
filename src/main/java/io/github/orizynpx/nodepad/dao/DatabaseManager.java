package io.github.orizynpx.nodepad.dao;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

public class DatabaseManager {

    private static final String DB_URL = "jdbc:sqlite:nodepad.db";

    // Static block executes once when the class is first loaded
    static {
        try {
            // Explicitly load the driver
            Class.forName("org.sqlite.JDBC");
            initSchema();
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite JDBC Driver not found!");
            e.printStackTrace();
        }
    }

    // Private constructor prevents instantiation
    private DatabaseManager() {}

    /**
     * STATIC method to get a connection.
     * Usage: try (Connection conn = DatabaseManager.connect()) { ... }
     */
    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    private static void initSchema() {
        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {

            // Ensure the directory exists in src/main/resources/sql/schema.sql
            String sql = loadResourceFile("/sql/schema.sql");

            if (sql.isEmpty()) {
                System.out.println("No schema.sql found, skipping DB init.");
                return;
            }

            // Split by semicolon to execute multiple statements
            String[] statements = sql.split(";");

            for (String s : statements) {
                if (!s.trim().isEmpty()) {
                    stmt.execute(s);
                }
            }
            System.out.println("Database initialized successfully.");

        } catch (Exception e) {
            System.err.println("Database Initialization Failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static String loadResourceFile(String path) {
        try (InputStream is = DatabaseManager.class.getResourceAsStream(path);
             Scanner scanner = new Scanner(is, StandardCharsets.UTF_8)) {
            if (is == null) return "";
            return scanner.useDelimiter("\\A").next();
        } catch (Exception e) {
            System.err.println("Could not load resource: " + path);
            return "";
        }
    }
}