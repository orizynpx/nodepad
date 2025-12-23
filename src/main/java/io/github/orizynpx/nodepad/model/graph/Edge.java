package io.github.orizynpx.nodepad.model.graph;

public class Edge {
    private String sourceId; // The dependency (must be done first)
    private String targetId; // The current task

    public Edge(String sourceId, String targetId) {
        this.sourceId = sourceId;
        this.targetId = targetId;
    }

    public String getSourceId() { return sourceId; }
    public void setSourceId(String sourceId) { this.sourceId = sourceId; }

    public String getTargetId() { return targetId; }
    public void setTargetId(String targetId) { this.targetId = targetId; }
}