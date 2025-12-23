package io.github.orizynpx.nodepad.domain.port;

import io.github.orizynpx.nodepad.domain.model.BookMetadata;
import java.util.Optional;
import java.util.List;

public interface MetadataRepository {
    void save(BookMetadata metadata);
    Optional<BookMetadata> findByIsbn(String isbn);
    List<BookMetadata> findAll();
}