package io.github.orizynpx.nodepad.domain.logic;

import io.github.orizynpx.nodepad.domain.model.Edge;
import io.github.orizynpx.nodepad.domain.model.GraphModel;
import io.github.orizynpx.nodepad.domain.model.NodeStatus;
import io.github.orizynpx.nodepad.domain.model.TaskNode;
import io.github.orizynpx.nodepad.domain.port.TagStrategy;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextParser {

    private static final Pattern ID_PATTERN = Pattern.compile("@id\\(([^)]+)\\)");
    private static final Pattern REQ_PATTERN = Pattern.compile("@(?:req|requires)\\(([^)]+)\\)");
    private static final String DONE_TAG = "@done";

    private final List<TagStrategy> tagStrategies;

    public TextParser(List<TagStrategy> tagStrategies) {
        this.tagStrategies = tagStrategies;
    }

    public GraphModel parse(String text) {
        Map<String, TaskNode> nodeMap = new LinkedHashMap<>();
        List<Edge> edges = new ArrayList<>();

        if (text == null || text.isBlank()) {
            return new GraphModel(new ArrayList<>(), new ArrayList<>());
        }

        String[] lines = text.split("\\R");
        TaskNode lastNode = null;
        int lineCounter = 0;

        // --- Pass 1: Nodes ---
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) continue;

            if (trimmed.matches("^[-*+]\\s+.*")) { // Task line
                Matcher m = ID_PATTERN.matcher(line);
                if (m.find()) {
                    String id = m.group(1).trim();
                    TaskNode node = new TaskNode(id);
                    node.setIndex(lineCounter++);

                    String label = line.replaceAll("@\\w+\\([^)]*\\)", "")
                            .replace(DONE_TAG, "")
                            .replaceFirst("^\\s*[-*+]\\s+", "")
                            .trim();
                    node.setLabel(label);

                    if (line.contains(DONE_TAG)) node.setStatus(NodeStatus.DONE);

                    // (h) Usage of Interface Strategy
                    for (TagStrategy strategy : tagStrategies) {
                        strategy.process(node, line);
                    }

                    nodeMap.put(id, node);
                    lastNode = node;
                }
            } else if (lastNode != null && line.startsWith("  ")) {
                // Description
                String desc = lastNode.getDescription();
                lastNode.setDescription(desc.isEmpty() ? trimmed : desc + "\n" + trimmed);
            }
        }

        // --- Pass 2: Edges & Logic ---
        // (Simplified logic for brevity, matches original ParserService)
        for (String line : lines) {
            Matcher m = ID_PATTERN.matcher(line);
            if (!m.find()) {
                String currentId = m.group(1).trim();
                Matcher req = REQ_PATTERN.matcher(line);
                while (req.find()) {
                    String[] reqs = req.group(1).split(",");
                    for (String r : reqs) {
                        String reqId = r.trim();
                        if (nodeMap.containsKey(reqId)) {
                            edges.add(new Edge(reqId, currentId));
                        }
                    }
                }
            }
        }

        calculateStatuses(nodeMap, edges);

        return new GraphModel(new ArrayList<>(nodeMap.values()), edges);
    }

    private void calculateStatuses(Map<String, TaskNode> nodeMap, List<Edge> edges) {
        Map<String, List<String>> incoming = new HashMap<>();
        for (Edge e : edges) incoming.computeIfAbsent(e.getTargetId(), k -> new ArrayList<>()).add(e.getSourceId());

        for (TaskNode node : nodeMap.values()) {
            if (node.getStatus() == NodeStatus.DONE) continue;
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