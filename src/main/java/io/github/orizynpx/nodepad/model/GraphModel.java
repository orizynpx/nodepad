package io.github.orizynpx.nodepad.model;

import java.util.ArrayList;
import java.util.List;

public class GraphModel {
    private List<GraphNode> nodes;
    private List<Edge> edges;

    public GraphModel() {
        this.nodes = new ArrayList<>();
        this.edges = new ArrayList<>();
    }

    public List<GraphNode> getNodes() { return nodes; }
    public void setNodes(List<GraphNode> nodes) { this.nodes = nodes; }

    public List<Edge> getEdges() { return edges; }
    public void setEdges(List<Edge> edges) { this.edges = edges; }
}
