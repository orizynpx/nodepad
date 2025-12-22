package io.github.orizynpx.nodepad.model;

import java.util.Objects;

public class GraphNode {

    public enum Status {
        LOCKED,   // Grey
        UNLOCKED, // Gold
        DONE      // Green
    }

    private String id;
    private String label;
    private String description = ""; // New Field
    private Status status;
    private String isbn;
    private String url;
    private int index; // NEW: Stores the order from the text file

    // ... existing getters/setters ...


    public GraphNode(String id) {
        this.id = id;
        this.label = id;
        this.status = Status.LOCKED;
    }

    // Standard Getters/Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GraphNode graphNode = (GraphNode) o;
        return Objects.equals(id, graphNode.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return this.label;
    }
}