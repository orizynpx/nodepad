package io.github.orizynpx.nodepad.domain.port;

import io.github.orizynpx.nodepad.domain.model.FileRecord;
import java.util.List;

public interface FileRepository {
    void addOrUpdateFile(String filePath);
    List<FileRecord> getRecentFiles();
}