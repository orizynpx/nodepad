package io.github.orizynpx.nodepad.domain.model;

import java.util.List;

/**
 * Represents the state of the graph at a specific point in time.
 * Holds Nodes and Edges purely for logic processing.
 */
public class GraphModel {
    private final List<TaskNode> nodes;
    private final List<Edge> edges;

    public GraphModel(List<TaskNode> nodes, List<Edge> edges) {
        this.nodes = nodes;
        this.edges = edges;
    }

    public List<TaskNode> getNodes() { return nodes; }
    public List<Edge> getEdges() { return edges; }
}