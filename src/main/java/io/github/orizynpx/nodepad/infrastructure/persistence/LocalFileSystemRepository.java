package io.github.orizynpx.nodepad.infrastructure.persistence;

import io.github.orizynpx.nodepad.domain.port.FileSystemRepository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

public class LocalFileSystemRepository implements FileSystemRepository {

    @Override
    public String readFile(File file) throws IOException {
        if (file == null || !file.exists()) return "";
        return Files.readString(file.toPath());
    }

    @Override
    public void writeFile(File file, String content) throws IOException {
        if (file == null) return;
        Files.writeString(file.toPath(), content,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }
}