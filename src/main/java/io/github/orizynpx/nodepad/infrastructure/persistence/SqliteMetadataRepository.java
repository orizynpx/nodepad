package io.github.orizynpx.nodepad.infrastructure.persistence;

import io.github.orizynpx.nodepad.domain.model.BookMetadata;
import io.github.orizynpx.nodepad.domain.port.MetadataRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SqliteMetadataRepository implements MetadataRepository {

    @Override
    public void save(BookMetadata metadata) {
        String sql = "INSERT OR REPLACE INTO api_cache(isbn, title, description, image_url, fetched_at) VALUES(?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = DatabaseConnection.getInstance().getConnection().prepareStatement(sql)) {
            pstmt.setString(1, metadata.getIsbn());
            pstmt.setString(2, metadata.getTitle());
            pstmt.setString(3, metadata.getDescription());
            pstmt.setString(4, metadata.getImageUrl());
            pstmt.setLong(5, System.currentTimeMillis());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Optional<BookMetadata> findByIsbn(String isbn) {
        String sql = "SELECT * FROM api_cache WHERE isbn = ?";
        try (PreparedStatement pstmt = DatabaseConnection.getInstance().getConnection().prepareStatement(sql)) {
            pstmt.setString(1, isbn);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return Optional.of(new BookMetadata(
                        rs.getString("isbn"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getString("image_url")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public List<BookMetadata> findAll() {
        List<BookMetadata> list = new ArrayList<>();
        String sql = "SELECT * FROM api_cache ORDER BY fetched_at DESC";
        try (Statement stmt = DatabaseConnection.getInstance().getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new BookMetadata(
                        rs.getString("isbn"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getString("image_url")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}