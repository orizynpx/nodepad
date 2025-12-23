package io.github.orizynpx.nodepad.model.graph;

import java.util.Objects;

public abstract class BaseNode {
    protected String id;
    protected String label;

    public BaseNode(String id, String label) {
        this.id = id;
        this.label = label;
    }

    // Abstract method forcing child classes to define their type
    public abstract String getType();

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseNode baseNode = (BaseNode) o;
        return Objects.equals(id, baseNode.id);
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