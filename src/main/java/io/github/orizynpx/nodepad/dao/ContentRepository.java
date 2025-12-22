package io.github.orizynpx.nodepad.dao;

import java.io.File;
import java.io.IOException;

public interface ContentRepository {
    String loadContent(File file) throws IOException;
    void saveContent(File file, String content) throws IOException;
}