package io.github.orizynpx.nodepad.service;

import io.github.orizynpx.nodepad.model.Edge;
import io.github.orizynpx.nodepad.model.GraphModel;
import java.util.*;

public class TaskMutatorService {

    /**
     * Toggles the @done status of a node in the raw text.
     * Handles Cascading Undo: If marking incomplete, recursively unchecks dependents.
     */
    public String toggleTaskStatus(String currentText, GraphModel currentModel, String nodeId) {
        String targetTag = "@id(" + nodeId + ")";
        String doneTag = targetTag + " @done";

        if (currentText.contains(doneTag)) {
            // --- CASE: UNCHECKING (Marking Incomplete) ---
            // Must also uncheck any task that REQUIRES this one.
            Set<String> nodesToReset = new HashSet<>();
            nodesToReset.add(nodeId);

            if (currentModel != null) {
                collectDependentNodes(nodeId, currentModel, nodesToReset);
            }

            String newText = currentText;
            for (String id : nodesToReset) {
                String t = "@id(" + id + ")";
                // Remove @done from self and all dependents
                newText = newText.replace(t + " @done", t);
            }
            return newText;

        } else if (currentText.contains(targetTag)) {
            // --- CASE: CHECKING (Marking Complete) ---
            // Simple replace. Dependencies are checked by Parser logic, not Mutator.
            return currentText.replace(targetTag, doneTag);
        }

        return currentText;
    }

    // BFS to find all nodes downstream that depend on the rootId
    private void collectDependentNodes(String rootId, GraphModel model, Set<String> results) {
        Map<String, List<String>> adj = new HashMap<>();
        // Build Adjacency: Source -> Targets (Who depends on Source?)
        for (Edge e : model.getEdges()) {
            adj.computeIfAbsent(e.getSourceId(), k -> new ArrayList<>()).add(e.getTargetId());
        }

        Queue<String> queue = new LinkedList<>();
        queue.add(rootId);

        while (!queue.isEmpty()) {
            String curr = queue.poll();
            if (adj.containsKey(curr)) {
                for (String child : adj.get(curr)) {
                    if (!results.contains(child)) {
                        results.add(child);
                        queue.add(child);
                    }
                }
            }
        }
    }
}