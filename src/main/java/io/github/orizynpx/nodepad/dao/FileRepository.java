package io.github.orizynpx.nodepad.dao;

import io.github.orizynpx.nodepad.model.FileRecord;
import java.util.List;

public interface FileRepository {
    void addOrUpdateFile(String filePath);
    List<FileRecord> getRecentFiles();
}