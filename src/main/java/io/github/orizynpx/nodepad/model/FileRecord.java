package io.github.orizynpx.nodepad.model;

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

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public long getLastOpened() {
        return lastOpened;
    }

    public void setLastOpened(long lastOpened) {
        this.lastOpened = lastOpened;
    }

    public boolean isPinned() {
        return isPinned;
    }

    public void setPinned(boolean pinned) {
        isPinned = pinned;
    }
}