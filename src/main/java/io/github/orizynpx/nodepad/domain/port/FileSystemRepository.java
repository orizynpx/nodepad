package io.github.orizynpx.nodepad.domain.port;

import java.io.File;
import java.io.IOException;

public interface FileSystemRepository {
    String readFile(File file) throws IOException;
    void writeFile(File file, String content) throws IOException;
}