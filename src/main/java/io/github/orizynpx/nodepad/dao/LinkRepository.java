package io.github.orizynpx.nodepad.dao;

import io.github.orizynpx.nodepad.model.entity.LinkMetadata;
import java.util.Optional;

public interface LinkRepository {
    void save(LinkMetadata metadata);
    Optional<LinkMetadata> findByUrl(String url);
}