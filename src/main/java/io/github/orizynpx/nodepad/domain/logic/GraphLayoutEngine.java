package io.github.orizynpx.nodepad.domain.logic;

import io.github.orizynpx.nodepad.domain.model.Edge;
import io.github.orizynpx.nodepad.domain.model.TaskNode;

import java.util.*;

public class GraphLayoutEngine {

    // Helper class for position result
    public static class NodePosition {
        public final double x;
        public final double y;

        public NodePosition(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }

    // Constants for calculation (could be injected via config)
    private static final double VERTICAL_SPACING = 100.0;
    private static final double HORIZONTAL_SPACING = 80.0;
    private static final double START_X = 50.0;
    private static final double START_Y = 50.0;

    /**
     * Calculates positions for a forest of trees.
     * Returns a Map of NodeID -> Coordinate (Pure Math)
     */
    public Map<String, NodePosition> calculateLayout(List<TaskNode> nodes, List<Edge> edges) {
        Map<String, NodePosition> positions = new HashMap<>();
        if (nodes.isEmpty()) return positions;

        // 1. Build Adjacency
        Map<String, List<String>> childrenMap = new HashMap<>();
        Map<String, List<String>> parentsMap = new HashMap<>();
        Map<String, TaskNode> nodeLookup = new HashMap<>();

        for (TaskNode n : nodes) {
            nodeLookup.put(n.getId(), n);
            childrenMap.put(n.getId(), new ArrayList<>());
            parentsMap.put(n.getId(), new ArrayList<>());
        }

        for (Edge e : edges) {
            if (childrenMap.containsKey(e.getSourceId()) && childrenMap.containsKey(e.getTargetId())) {
                childrenMap.get(e.getSourceId()).add(e.getTargetId());
                parentsMap.get(e.getTargetId()).add(e.getSourceId());
            }
        }

        // 2. Identify Roots
        List<String> roots = new ArrayList<>();
        for (TaskNode n : nodes) {
            if (parentsMap.get(n.getId()).isEmpty()) {
                roots.add(n.getId());
            }
        }
        // Cycle fallback
        if (roots.isEmpty() && !nodes.isEmpty()) {
            roots.add(nodes.get(0).getId());
        }
        // Sort roots by original text index
        roots.sort(Comparator.comparingInt(id -> nodeLookup.get(id).getIndex()));

        // 3. Recursive Placement
        double currentX = START_X;

        for (String rootId : roots) {
            if (positions.containsKey(rootId)) continue;

            // Use array to pass mutable double by reference
            double[] leafCursor = {currentX};
            layoutRecursive(rootId, 0, leafCursor, positions, childrenMap);

            // Gap between trees
            currentX = leafCursor[0] + 150.0;
        }

        return positions;
    }

    private void layoutRecursive(String nodeId, int depth, double[] leafCursor,
                                 Map<String, NodePosition> positions,
                                 Map<String, List<String>> childrenMap) {
        if (positions.containsKey(nodeId)) return;

        List<String> children = childrenMap.get(nodeId);
        double y = START_Y + (depth * VERTICAL_SPACING);

        // Check which children need placement
        List<String> unplacedChildren = new ArrayList<>();
        for (String child : children) {
            if (!positions.containsKey(child)) unplacedChildren.add(child);
        }

        double x;
        if (unplacedChildren.isEmpty()) {
            // Leaf behavior
            x = leafCursor[0];
            leafCursor[0] += HORIZONTAL_SPACING;
        } else {
            // Parent behavior: Place children first
            double minX = Double.MAX_VALUE;
            double maxX = Double.MIN_VALUE;

            for (String childId : unplacedChildren) {
                layoutRecursive(childId, depth + 1, leafCursor, positions, childrenMap);

                double childX = positions.get(childId).x;
                if (childX < minX) minX = childX;
                if (childX > maxX) maxX = childX;
            }
            // Center parent over children
            x = minX + ((maxX - minX) / 2.0);

            // Ensure parent doesn't overlap previous tree cursor
            if (x < leafCursor[0]) {
                // This is a simplified check; simpler to let leaves drive the width
            }
        }

        positions.put(nodeId, new NodePosition(x, y));
    }
}