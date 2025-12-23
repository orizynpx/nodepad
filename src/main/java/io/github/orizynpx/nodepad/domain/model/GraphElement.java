package io.github.orizynpx.nodepad.domain.model;

import java.util.Objects;

// (e) Abstract class acting as base for other classes
public abstract class GraphElement {
    protected String id;
    protected String label;

    public GraphElement(String id, String label) {
        this.id = id;
        this.label = label;
    }

    // (d) Encapsulation: Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    // Abstract method forcing implementation
    public abstract String getType();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GraphElement that = (GraphElement) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}