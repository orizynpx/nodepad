package io.github.orizynpx.nodepad.infrastructure.persistence;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

public class DatabaseConnection {

    private static final String DB_URL = "jdbc:sqlite:nodepad.db";
    private static DatabaseConnection instance;
    private Connection connection;

    private DatabaseConnection() {
        try {
            Class.forName("org.sqlite.JDBC");
            this.connection = DriverManager.getConnection(DB_URL);
            initSchema();
        } catch (Exception e) {
            throw new RuntimeException("Failed to connect to database", e);
        }
    }

    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }

    private void initSchema() {
        try (Statement stmt = connection.createStatement()) {
            String sql = loadResourceFile("/sql/schema.sql");
            if (sql.isEmpty()) return;

            String[] statements = sql.split(";");
            for (String s : statements) {
                if (!s.trim().isEmpty()) stmt.execute(s);
            }
            System.out.println("Database initialized.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String loadResourceFile(String path) {
        try (InputStream is = getClass().getResourceAsStream(path);
             Scanner scanner = new Scanner(is, StandardCharsets.UTF_8)) {
            if (is == null) return "";
            return scanner.useDelimiter("\\A").next();
        } catch (Exception e) {
            return "";
        }
    }
}