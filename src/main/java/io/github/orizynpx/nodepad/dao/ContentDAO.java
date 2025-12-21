package io.github.orizynpx.nodepad.dao;

import java.sql.*;

public class ContentDAO {
    public void saveContent(String content) {
        String sql = "INSERT OR REPLACE INTO documents (id, content) VALUES (1, ?)";
        try (Connection conn = DBConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, content);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String loadContent() {
        String sql = "SELECT content FROM documents WHERE id = 1";
        try (Connection conn = DBConnection.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getString("content");
        } catch (SQLException e) {
            // Ignore (DB might be empty on first run)
        }
        return "";
    }
}
