package io.github.orizynpx.nodepad.dao;

import io.github.orizynpx.nodepad.model.FileRecord;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SqliteFileDao implements FileRepository {
    private final ConnectionFactory connectionFactory;

    public SqliteFileDao(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public void addOrUpdateFile(String filePath) {
        // ... (Keep existing code) ...
        String sql = "INSERT OR REPLACE INTO recent_files(file_path, last_opened, is_pinned) VALUES(?, ?, COALESCE((SELECT is_pinned FROM recent_files WHERE file_path=?), 0))";
        try (Connection conn = connectionFactory.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, filePath);
            pstmt.setLong(2, System.currentTimeMillis());
            pstmt.setString(3, filePath);
            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @Override
    public List<FileRecord> getRecentFiles() {
        // ... (Keep existing code) ...
        List<FileRecord> files = new ArrayList<>();
        String sql = "SELECT * FROM recent_files ORDER BY last_opened DESC LIMIT 10";
        try (Connection conn = connectionFactory.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                files.add(new FileRecord(
                        rs.getString("file_path"),
                        rs.getLong("last_opened"),
                        rs.getBoolean("is_pinned")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return files;
    }

    // --- NEW IMPLEMENTATIONS ---

    @Override
    public void updateFilePath(String oldPath, String newPath) {
        String sql = "UPDATE recent_files SET file_path = ? WHERE file_path = ?";
        try (Connection conn = connectionFactory.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newPath);
            pstmt.setString(2, oldPath);
            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @Override
    public void removeFileRecord(String filePath) {
        String sql = "DELETE FROM recent_files WHERE file_path = ?";
        try (Connection conn = connectionFactory.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, filePath);
            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }
}