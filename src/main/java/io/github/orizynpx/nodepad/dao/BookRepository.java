package io.github.orizynpx.nodepad.dao;

import io.github.orizynpx.nodepad.model.entity.BookMetadata;
import java.util.List;
import java.util.Optional;

public interface BookRepository {
    void save(BookMetadata book);
    Optional<BookMetadata> findByIsbn(String isbn);
    List<BookMetadata> findAll();
    void delete(String isbn);
}