package io.github.orizynpx.nodepad.domain.model;

public class Edge {
    private String sourceId;
    private String targetId;

    public Edge(String sourceId, String targetId) {
        this.sourceId = sourceId;
        this.targetId = targetId;
    }

    // (d) Encapsulation
    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }
}