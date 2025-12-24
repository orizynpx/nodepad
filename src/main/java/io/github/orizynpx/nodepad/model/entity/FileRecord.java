package io.github.orizynpx.nodepad.model.entity;

public class FileRecord {
    private String filePath;
    private long lastOpened; // Unix timestamp

    public FileRecord(String filePath, long lastOpened) {
        this.filePath = filePath;
        this.lastOpened = lastOpened;
    }

    public String getFilePath() {
        return filePath;
    }

    public long getLastOpened() {
        return lastOpened;
    }
}