package io.github.orizynpx.nodepad.service;

import io.github.orizynpx.nodepad.model.graph.Edge;
import io.github.orizynpx.nodepad.model.graph.GraphModel;
import io.github.orizynpx.nodepad.model.graph.TaskNode; // Updated to TaskNode
import io.github.orizynpx.nodepad.model.enums.NodeStatus;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParserService {

    private static final Pattern ID_PATTERN = Pattern.compile("@id\\(([^)]+)\\)");
    private static final Pattern REQ_PATTERN = Pattern.compile("@(?:req|requires)\\(([^)]+)\\)");
    private static final Pattern ISBN_PATTERN = Pattern.compile("@isbn\\(([^)]+)\\)");
    private static final Pattern URL_PATTERN = Pattern.compile("@url\\(([^)]+)\\)");
    private static final String DONE_TAG = "@done";

    public GraphModel parse(String text) {
        GraphModel graph = new GraphModel();
        if (text == null || text.isBlank()) return graph;

        String[] lines = text.split("\\R");
        // UPDATED: Map stores TaskNode
        Map<String, TaskNode> nodeMap = new LinkedHashMap<>();

        // --- PASS 1: NODES (Capture Order) ---
        TaskNode lastNode = null;
        int lineCounter = 0;

        for (String line : lines) {
            Matcher idMatcher = ID_PATTERN.matcher(line);
            if (idMatcher.find()) {
                String id = idMatcher.group(1).trim();

                // UPDATED: Instantiate TaskNode
                TaskNode node = new TaskNode(id);
                node.setIndex(lineCounter++);

                String cleanLabel = line.replaceAll("@\\w+\\([^)]*\\)", "")
                        .replace(DONE_TAG, "").replaceFirst("^\\s*[-*+]\\s+", "").trim();
                if (!cleanLabel.isEmpty()) node.setLabel(cleanLabel);

                nodeMap.put(id, node);
                lastNode = node;
            } else if (lastNode != null && !line.trim().isEmpty() && !line.trim().startsWith("-")) {
                String desc = line.trim();
                lastNode.setDescription(lastNode.getDescription().isEmpty() ? desc : lastNode.getDescription() + "\n" + desc);
            }
        }

        // --- PASS 2: EDGES ---
        List<Edge> edges = new ArrayList<>();
        Set<String> existingEdges = new HashSet<>();

        for (String line : lines) {
            Matcher idMatcher = ID_PATTERN.matcher(line);
            if (idMatcher.find()) {
                String currentId = idMatcher.group(1).trim();
                TaskNode currentNode = nodeMap.get(currentId);

                // Metadata
                Matcher isbn = ISBN_PATTERN.matcher(line);
                if (isbn.find()) currentNode.setIsbn(isbn.group(1).trim());
                Matcher url = URL_PATTERN.matcher(line);
                if (url.find()) currentNode.setUrl(url.group(1).trim());
                if (line.contains(DONE_TAG)) currentNode.setStatus(NodeStatus.DONE);

                // Dependencies
                Matcher req = REQ_PATTERN.matcher(line);
                while (req.find()) {
                    String[] requirements = req.group(1).split(",");
                    for (String reqId : requirements) {
                        reqId = reqId.trim();
                        if (nodeMap.containsKey(reqId)) {
                            String key = reqId + "->" + currentId;
                            if (!existingEdges.contains(key)) {
                                edges.add(new Edge(reqId, currentId));
                                existingEdges.add(key);
                            }
                        }
                    }
                }
            }
        }

        calculateStatuses(nodeMap, edges);

        graph.setNodes(new ArrayList<>(nodeMap.values()));
        graph.setEdges(edges);
        return graph;
    }

    // UPDATED: Signature uses TaskNode
    private void calculateStatuses(Map<String, TaskNode> nodeMap, List<Edge> edges) {
        Map<String, List<String>> incoming = new HashMap<>();
        for (Edge edge : edges) {
            incoming.computeIfAbsent(edge.getTargetId(), k -> new ArrayList<>()).add(edge.getSourceId());
        }

        for (TaskNode node : nodeMap.values()) {
            if (node.getStatus() == NodeStatus.DONE) {
                continue;
            }
            List<String> deps = incoming.getOrDefault(node.getId(), Collections.emptyList());
            if (deps.isEmpty()) {
                node.setStatus(NodeStatus.UNLOCKED);
            } else {
                boolean allDone = deps.stream().allMatch(id -> nodeMap.get(id).getStatus() == NodeStatus.DONE);
                node.setStatus(allDone ? NodeStatus.UNLOCKED : NodeStatus.LOCKED);
            }
        }
    }
}