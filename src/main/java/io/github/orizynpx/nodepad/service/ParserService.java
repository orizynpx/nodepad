package io.github.orizynpx.nodepad.service;

import io.github.orizynpx.nodepad.model.Edge;
import io.github.orizynpx.nodepad.model.GraphModel;
import io.github.orizynpx.nodepad.model.TaskNode;
import io.github.orizynpx.nodepad.service.processor.*;

import java.util.*;
import java.util.regex.Matcher;

public class ParserService {

    private final List<TagProcessor> processors;

    public ParserService() {
        this.processors = new ArrayList<>();
        processors.add(new IsbnProcessor());
        processors.add(new UrlProcessor());
    }

    public GraphModel parse(String text) {
        Map<String, TaskNode> nodeMap = new LinkedHashMap<>();
        List<Edge> edges = new ArrayList<>();
        Set<String> existingEdges = new HashSet<>();

        if (text == null || text.isBlank()) return new GraphModel(new ArrayList<>(), edges);

        String[] lines = text.split("\\R");
        TaskNode lastNode = null;
        int lineCounter = 0;

        // --- PASS 1: NODES & DESCRIPTIONS ---
        for (String rawLine : lines) {
            // 1. Normalize Tabs: Treat 1 Tab as 4 Spaces
            String line = rawLine.replaceAll("\t", "    ");
            String trimmed = line.trim();

            if (trimmed.isEmpty()) continue;

            // 2. DETECT TASK: Must start with a bullet (-, *, +)
            // Regex: Start of string, optional space, bullet, mandatory space
            boolean isTaskLine = trimmed.matches("^[-*+]\\s+.*");

            if (isTaskLine) {
                // It is a Task
                Matcher idMatcher = SyntaxDefinitions.ID.matcher(line);
                if (idMatcher.find()) {
                    String id = idMatcher.group(1).trim();

                    // Create Node
                    TaskNode node = new TaskNode(id);
                    node.setIndex(lineCounter++);

                    // Extract Label: Remove tags and bullets
                    String cleanLabel = line.replaceAll("@\\w+\\([^)]*\\)", "")
                            .replace("@done", "")
                            .replaceFirst("^\\s*[-*+]\\s+", "") // Remove bullet
                            .trim();

                    node.setLabel(cleanLabel);

                    // Run Processors (ISBN, URL, etc.)
                    for (TagProcessor processor : processors) {
                        processor.process(node, line);
                    }

                    // Check @done manually or via processor
                    if (line.contains("@done")) {
                        node.setStatus(io.github.orizynpx.nodepad.model.NodeStatus.DONE);
                    }

                    nodeMap.put(id, node);
                    lastNode = node;
                }
            } else {
                // 3. DETECT DESCRIPTION
                // Condition: Not a task, Last node exists, and Indented >= 2 spaces
                if (lastNode != null && line.startsWith("  ")) {
                    String descLine = trimmed;
                    String currentDesc = lastNode.getDescription();

                    // Append line (Multi-line support)
                    if (currentDesc.isEmpty()) {
                        lastNode.setDescription(descLine);
                    } else {
                        lastNode.setDescription(currentDesc + "\n" + descLine);
                    }
                }
            }
        }

        // --- PASS 2: EDGES (Dependencies) ---
        // (This logic remains largely the same, but we iterate lines again to find @req)
        for (String line : lines) {
            Matcher idMatcher = SyntaxDefinitions.ID.matcher(line);
            if (idMatcher.find()) {
                String currentId = idMatcher.group(1).trim();

                Matcher req = SyntaxDefinitions.REQ.matcher(line);
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

        // Calculate Locked/Unlocked statuses
        calculateStatuses(nodeMap, edges);

        return new GraphModel(new ArrayList<>(nodeMap.values()), edges);
    }

    private void calculateStatuses(Map<String, TaskNode> nodeMap, List<Edge> edges) {
        // (Keep your existing status logic here)
        Map<String, List<String>> incoming = new HashMap<>();
        for (Edge edge : edges) {
            incoming.computeIfAbsent(edge.getTargetId(), k -> new ArrayList<>()).add(edge.getSourceId());
        }

        for (TaskNode node : nodeMap.values()) {
            if (node.getStatus() == io.github.orizynpx.nodepad.model.NodeStatus.DONE) continue;

            List<String> deps = incoming.getOrDefault(node.getId(), Collections.emptyList());
            if (deps.isEmpty()) {
                node.setStatus(io.github.orizynpx.nodepad.model.NodeStatus.UNLOCKED);
            } else {
                boolean allDone = deps.stream().allMatch(id -> nodeMap.get(id).getStatus() == io.github.orizynpx.nodepad.model.NodeStatus.DONE);
                node.setStatus(allDone ? io.github.orizynpx.nodepad.model.NodeStatus.UNLOCKED : io.github.orizynpx.nodepad.model.NodeStatus.LOCKED);
            }
        }
    }
}