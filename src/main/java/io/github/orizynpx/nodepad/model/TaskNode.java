package io.github.orizynpx.nodepad.model;

import io.github.orizynpx.nodepad.model.NodeStatus; // Ensure you import your existing Enum

public class TaskNode extends BaseNode implements Renderable {

    private String description = "";
    private NodeStatus status;
    private String isbn;
    private String url;
    private int index; // Used for preserving order

    // --- POLYMORPHISM: Constructor 1 (Detailed) ---
    public TaskNode(String id, String label, NodeStatus status) {
        super(id, label);
        this.status = status;
    }

    // --- POLYMORPHISM: Constructor 2 (Minimal) ---
    public TaskNode(String id) {
        this(id, id, NodeStatus.LOCKED); // Defaults to ID as label and LOCKED status
    }

    @Override
    public String getType() {
        return "TASK";
    }

    // --- INTERFACE IMPLEMENTATION ---
    @Override
    public String getDisplayColor() {
        if (status == null) return "#333333";
        return switch (status) {
            case DONE -> "#00ff99";   // Green
            case UNLOCKED -> "#ffd700"; // Gold
            default -> "#333333";     // Grey
        };
    }

    // --- Getters & Setters (Required by ParserService) ---

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public NodeStatus getStatus() {
        return status;
    }

    public void setStatus(NodeStatus status) {
        this.status = status;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}