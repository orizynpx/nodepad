package io.github.orizynpx.nodepad.service;

import io.github.orizynpx.nodepad.model.TaskNode;

import java.util.*;

public class LayoutService {
    public void calculateLayout(List<TaskNode> nodes) {
        if (nodes.isEmpty()) return;

        // Group by Depth (Level)
        Map<Integer, List<TaskNode>> levels = new HashMap<>();
        int maxDepth = 0;

        for (TaskNode node : nodes) {
            int depth = getDepth(node, new HashSet<>());
            levels.computeIfAbsent(depth, k -> new ArrayList<>()).add(node);
            maxDepth = Math.max(maxDepth, depth);
        }

        // Assign Coordinates
        double startY = 50;
        double levelHeight = 100; // Vertical gap
        double nodeSpacing = 120; // Horizontal gap

        for (int d = 0; d <= maxDepth; d++) {
            List<TaskNode> levelNodes = levels.getOrDefault(d, Collections.emptyList());
            double totalWidth = levelNodes.size() * nodeSpacing;
            double startX = (800 - totalWidth) / 2; // Center in a generic 800px width

            for (int i = 0; i < levelNodes.size(); i++) {
                TaskNode node = levelNodes.get(i);
                node.setY(startY + (d * levelHeight));
                node.setX(startX + (i * nodeSpacing));
            }
        }
    }

    private int getDepth(TaskNode node, Set<String> visited) {
        if (node.getParents().isEmpty()) return 0;
        if (visited.contains(node.getId())) return 0; // Prevent cycles

        visited.add(node.getId());
        int maxP = 0;
        for (TaskNode p : node.getParents()) {
            maxP = Math.max(maxP, getDepth(p, visited));
        }
        return maxP + 1;
    }
}
