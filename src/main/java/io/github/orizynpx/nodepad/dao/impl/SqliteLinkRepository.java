package io.github.orizynpx.nodepad.dao.impl;

import io.github.orizynpx.nodepad.dao.DatabaseFactory;
import io.github.orizynpx.nodepad.dao.LinkRepository;
import io.github.orizynpx.nodepad.model.entity.LinkMetadata;

import java.sql.*;
import java.util.Optional;

public class SqliteLinkRepository implements LinkRepository {
    private final DatabaseFactory databaseFactory;

    public SqliteLinkRepository(DatabaseFactory databaseFactory) {
        this.databaseFactory = databaseFactory;
    }

    @Override
    public void save(LinkMetadata meta) {
        String sql = "INSERT OR REPLACE INTO link_cache(url, title, description, image_url, fetched_at) VALUES(?, ?, ?, ?, ?)";
        try (Connection conn = databaseFactory.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, meta.getUrl());
            pstmt.setString(2, meta.getTitle());
            pstmt.setString(3, meta.getDescription());
            pstmt.setString(4, meta.getImageUrl());
            pstmt.setLong(5, System.currentTimeMillis());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Optional<LinkMetadata> findByUrl(String url) {
        String sql = "SELECT * FROM link_cache WHERE url = ?";
        try (Connection conn = databaseFactory.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, url);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return Optional.of(new LinkMetadata(
                        rs.getString("url"), rs.getString("title"),
                        rs.getString("description"), rs.getString("image_url")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }
}