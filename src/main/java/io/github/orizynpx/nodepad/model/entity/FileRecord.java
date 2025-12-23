package io.github.orizynpx.nodepad.model.entity;

public class FileRecord {
    private String filePath;
    private long lastOpened; // Unix timestamp
    private boolean isPinned;

    public FileRecord(String filePath, long lastOpened, boolean isPinned) {
        this.filePath = filePath;
        this.lastOpened = lastOpened;
        this.isPinned = isPinned;
    }

    public String getFilePath() {
        return filePath;
    }

    public long getLastOpened() {
        return lastOpened;
    }

    public boolean isPinned() {
        return isPinned;
    }
}