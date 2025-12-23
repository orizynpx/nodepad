package io.github.orizynpx.nodepad.model;

import java.util.ArrayList;
import java.util.List;

public class GraphModel {
    // Now holds TaskNode (which extends BaseNode)
    private List<TaskNode> nodes;
    private List<Edge> edges;

    public GraphModel(List<TaskNode> nodes, List<Edge> edges) {
        this.nodes = nodes;
        this.edges = edges;
    }

    public List<TaskNode> getNodes() {
        return nodes;
    }

    public void setNodes(List<TaskNode> nodes) {
        this.nodes = nodes;
    }

    public List<Edge> getEdges() {
        return edges;
    }

    public void setEdges(List<Edge> edges) {
        this.edges = edges;
    }
}