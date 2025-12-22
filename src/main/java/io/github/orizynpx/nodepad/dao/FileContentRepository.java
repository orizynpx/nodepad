package io.github.orizynpx.nodepad.dao;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

public class FileContentRepository implements ContentRepository {
    @Override
    public String loadContent(File file) throws IOException {
        if (file == null || !file.exists()) {
            return "";
        }
        return Files.readString(file.toPath());
    }

    @Override
    public void saveContent(File file, String content) throws IOException {
        if (file == null) {
            return;
        }
        Files.writeString(file.toPath(), content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }
}