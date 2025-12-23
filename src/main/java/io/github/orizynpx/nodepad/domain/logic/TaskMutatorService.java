package io.github.orizynpx.nodepad.domain.logic;

import io.github.orizynpx.nodepad.domain.model.Edge;
import io.github.orizynpx.nodepad.domain.model.GraphModel;

import java.util.*;

public class TaskMutatorService {

    /**
     * Toggles the task status in the raw text string.
     * If marking INCOMPLETE, it automatically unchecks dependent tasks (Cascade).
     *
     * @param currentText The raw editor text
     * @param model The current parsed graph model (to calculate dependencies)
     * @param targetId The ID of the node being clicked
     * @return The updated text string
     */
    public String toggleTaskStatus(String currentText, GraphModel model, String targetId) {
        String targetTag = "@id(" + targetId + ")";
        String doneTag = targetTag + " @done";

        if (currentText.contains(doneTag)) {
            // --- UNCHECKING (Cascading Undo) ---
            Set<String> idsToReset = new HashSet<>();
            idsToReset.add(targetId); // Always reset self

            // Find all descendants
            collectDescendants(targetId, model, idsToReset);

            // Batch string replacement
            String newText = currentText;
            for (String id : idsToReset) {
                String tag = "@id(" + id + ")";
                // Replace "@id(x) @done" with "@id(x)"
                newText = newText.replace(tag + " @done", tag);
            }
            return newText;

        } else if (currentText.contains(targetTag)) {
            // --- CHECKING (Simple) ---
            // Just mark this one. Dependencies are enforced by the Status calculation logic,
            // not by preventing the text edit.
            return currentText.replace(targetTag, doneTag);
        }

        return currentText; // No change found
    }

    /**
     * Traverses the graph to find all nodes that depend on the rootId.
     */
    private void collectDescendants(String rootId, GraphModel model, Set<String> results) {
        if (model == null) return;

        // 1. Build Adjacency List (Source -> Targets)
        Map<String, List<String>> adj = new HashMap<>();
        for (Edge e : model.getEdges()) {
            adj.computeIfAbsent(e.getSourceId(), k -> new ArrayList<>()).add(e.getTargetId());
        }

        // 2. BFS Traversal
        Queue<String> queue = new LinkedList<>();
        queue.add(rootId);

        while (!queue.isEmpty()) {
            String current = queue.poll();
            if (!adj.containsKey(current)) {
                return;
            }
            for (String child : adj.get(current)) {
                if (!results.contains(child)) {
                    results.add(child); // Add to result set
                    queue.add(child);   // Continue traversing down
                }
            }
        }
    }
}