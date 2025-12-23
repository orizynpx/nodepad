package io.github.orizynpx.nodepad.model.graph;

import io.github.orizynpx.nodepad.model.enums.NodeStatus;

public class TaskNode extends BaseNode {

    private String description = "";
    private NodeStatus status;
    private String isbn;
    private String url;
    private int index; // Used for preserving order

    // Detailed
    public TaskNode(String id, String label, NodeStatus status) {
        super(id, label);
        this.status = status;
    }

    // Minimal
    public TaskNode(String id) {
        this(id, id, NodeStatus.LOCKED); // Defaults to ID as label and LOCKED status
    }

    @Override
    public String getType() {
        return "TASK";
    }

    // --- Getters & setters (used by ParserService) ---

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