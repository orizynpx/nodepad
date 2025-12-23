package io.github.orizynpx.nodepad.model.graph;

import java.util.ArrayList;
import java.util.List;

public class GraphModel {
    // Now holds TaskNode (which extends BaseNode)
    private List<TaskNode> nodes;
    private List<Edge> edges;

    public GraphModel() {
        this.nodes = new ArrayList<>();
        this.edges = new ArrayList<>();
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