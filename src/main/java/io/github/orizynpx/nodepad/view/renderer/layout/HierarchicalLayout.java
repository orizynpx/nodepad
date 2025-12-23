package io.github.orizynpx.nodepad.view.renderer.layout;

import io.github.orizynpx.nodepad.model.graph.Edge;
import io.github.orizynpx.nodepad.model.graph.GraphModel;
import io.github.orizynpx.nodepad.model.graph.TaskNode;
import javafx.geometry.Point2D;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.util.*;

public class HierarchicalLayout implements LayoutStrategy {
    // Configuration
    private static final double NODE_RADIUS = 15.0;
    private static final double MIN_NODE_GAP = 30.0;
    private static final double VERTICAL_SPACING = 100.0;
    private static final double FOREST_GAP = 150.0;
    private static final double TOP_PADDING = 50.0;
    private static final double LEFT_PADDING = 50.0;

    @Override
    public Map<String, Point2D> calculatePositions(GraphModel graph) {
        Map<String, Point2D> positions = new HashMap<>();
        if (graph.getNodes().isEmpty()) return positions;

        // 1. Preparing the data
        Map<String, TaskNode> nodeData = new HashMap<>();
        Map<String, Double> nodeWidths = new HashMap<>();
        Map<String, List<String>> childrenMap = new HashMap<>();
        Map<String, List<String>> parentsMap = new HashMap<>();
        Map<String, List<String>> undirectedAdj = new HashMap<>();

        for (TaskNode n : graph.getNodes()) {
            nodeData.put(n.getId(), n);
            childrenMap.put(n.getId(), new ArrayList<>());
            parentsMap.put(n.getId(), new ArrayList<>());
            undirectedAdj.put(n.getId(), new ArrayList<>());

            double textWidth = calculateTextWidth(n.getLabel());
            double totalWidth = Math.max(textWidth, NODE_RADIUS * 2) + MIN_NODE_GAP;
            nodeWidths.put(n.getId(), totalWidth);
        }

        for (Edge e : graph.getEdges()) {
            if (nodeData.containsKey(e.getSourceId()) && nodeData.containsKey(e.getTargetId())) {
                childrenMap.get(e.getSourceId()).add(e.getTargetId());
                parentsMap.get(e.getTargetId()).add(e.getSourceId());
                undirectedAdj.get(e.getSourceId()).add(e.getTargetId());
                undirectedAdj.get(e.getTargetId()).add(e.getSourceId());
            }
        }

        // 2. Sort children for deterministic layout
        childrenMap.values().forEach(list ->
                list.sort(Comparator.comparingInt(id -> nodeData.get(id).getIndex())));

        // 3. Layout components
        List<Set<String>> components = findConnectedComponents(nodeData.keySet(), undirectedAdj);
        // Sort components by the index of their first node to keep order stable
        components.sort(Comparator.comparingInt(comp -> comp.stream()
                .mapToInt(id -> nodeData.get(id).getIndex())
                .min().orElse(Integer.MAX_VALUE)));

        double currentX = LEFT_PADDING;

        for (Set<String> componentIds : components) {
            // Find roots in this component (nodes with no parents within the component)
            List<String> compRoots = new ArrayList<>();
            for (String id : componentIds) {
                boolean hasParentInComp = parentsMap.get(id).stream().anyMatch(componentIds::contains);
                if (!hasParentInComp) compRoots.add(id);
            }

            // Fallback for cycles, pick the node with lowest index
            if (compRoots.isEmpty() && !componentIds.isEmpty()) {
                compRoots.add(componentIds.stream()
                        .min(Comparator.comparingInt(id -> nodeData.get(id).getIndex()))
                        .orElse(componentIds.iterator().next()));
            }
            compRoots.sort(Comparator.comparingInt(id -> nodeData.get(id).getIndex()));

            double[] leafCursor = {currentX};
            for (String rootId : compRoots) {
                if (!positions.containsKey(rootId)) {
                    layoutNodeRecursively(rootId, 0, leafCursor, positions, childrenMap, nodeWidths);
                }
            }
            currentX = leafCursor[0] + FOREST_GAP;
        }

        return positions;
    }

    private void layoutNodeRecursively(String nodeId, int depth, double[] leafCursor,
                                       Map<String, Point2D> positions,
                                       Map<String, List<String>> childrenMap,
                                       Map<String, Double> nodeWidths) {
        if (positions.containsKey(nodeId)) return;

        List<String> children = childrenMap.get(nodeId);
        double y = TOP_PADDING + (depth * VERTICAL_SPACING);
        double myWidth = nodeWidths.get(nodeId);
        List<String> myPlacedChildren = new ArrayList<>();

        for (String childId : children) {
            if (!positions.containsKey(childId)) {
                layoutNodeRecursively(childId, depth + 1, leafCursor, positions, childrenMap, nodeWidths);
                myPlacedChildren.add(childId);
            }
        }

        double x;
        if (myPlacedChildren.isEmpty()) {
            x = leafCursor[0] + (myWidth / 2.0);
            leafCursor[0] += myWidth;
        } else {
            double minX = Double.MAX_VALUE, maxX = Double.MIN_VALUE;
            for (String childId : myPlacedChildren) {
                double cx = positions.get(childId).getX();
                if (cx < minX) minX = cx;
                if (cx > maxX) maxX = cx;
            }
            x = minX + ((maxX - minX) / 2.0);
            double myRightEdge = x + (myWidth / 2.0);
            if (myRightEdge > leafCursor[0]) leafCursor[0] = myRightEdge;
        }
        positions.put(nodeId, new Point2D(x, y));
    }

    private List<Set<String>> findConnectedComponents(Set<String> nodes, Map<String, List<String>> adj) {
        List<Set<String>> components = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        for (String node : nodes) {
            if (!visited.contains(node)) {
                Set<String> comp = new HashSet<>();
                Queue<String> q = new LinkedList<>();

                q.add(node);
                visited.add(node);
                comp.add(node);

                while (!q.isEmpty()) {
                    String curr = q.poll();
                    for (String neighbor : adj.getOrDefault(curr, Collections.emptyList())) {
                        if (!visited.contains(neighbor)) {
                            visited.add(neighbor);
                            comp.add(neighbor);
                            q.add(neighbor);
                        }
                    }
                }
                components.add(comp);
            }
        }
        return components;
    }

    private double calculateTextWidth(String text) {
        Text t = new Text(text);
        t.setFont(Font.font("Consolas", FontWeight.BOLD, 12));
        return t.getLayoutBounds().getWidth();
    }
}