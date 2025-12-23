package io.github.orizynpx.nodepad.domain.model;

public class FileRecord {
    private String filePath;
    private long lastOpened;
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