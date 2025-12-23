package io.github.orizynpx.nodepad.dao;

import io.github.orizynpx.nodepad.model.BookMetadata;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SqliteBookDao implements BookRepository {
    private final ConnectionFactory connectionFactory;

    public SqliteBookDao(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public void save(BookMetadata book) {
        String sql = "INSERT OR REPLACE INTO api_cache(isbn, title, description, image_url, fetched_at) VALUES(?, ?, ?, ?, ?)";

        System.out.println(">>> ATTEMPTING TO SAVE BOOK: " + book.getIsbn());

        try (
                Connection conn = connectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {

            pstmt.setString(1, book.getIsbn());
            pstmt.setString(2, book.getTitle());
            pstmt.setString(3, book.getDescription());
            pstmt.setString(4, book.getImageUrl());
            pstmt.setLong(5, System.currentTimeMillis());

            int rows = pstmt.executeUpdate();

            System.out.println(">>> SAVE SUCCESS. Rows affected: " + rows);
        } catch (SQLException e) {
            System.err.println(">>> SQL ERROR SAVING BOOK:");
            e.printStackTrace();
        }
    }

    @Override
    public Optional<BookMetadata> findByIsbn(String isbn) {
        String sql = "SELECT * FROM api_cache WHERE isbn = ?";
        try (Connection conn = connectionFactory.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

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

    public List<BookMetadata> findAll() {
        List<BookMetadata> books = new ArrayList<>();
        String sql = "SELECT * FROM api_cache ORDER BY fetched_at DESC";
        try (Connection conn = connectionFactory.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                books.add(new BookMetadata(
                        rs.getString("isbn"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getString("image_url")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return books;
    }

    public void delete(String isbn) {
        String sql = "DELETE FROM api_cache WHERE isbn = ?";
        try (Connection conn = connectionFactory.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, isbn);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}