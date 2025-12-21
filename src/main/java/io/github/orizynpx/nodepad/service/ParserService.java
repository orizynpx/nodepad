package io.github.orizynpx.nodepad.service;

import io.github.orizynpx.nodepad.model.TaskNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParserService {
    private static final Pattern TASK_PATTERN = Pattern.compile("- \\[(x| )\\] (.*?) @id:([\\w_]+)");
    // Matches: @requires(ID,ID)
    private static final Pattern REQ_PATTERN = Pattern.compile("@requires\\((.*?)\\)");

    public List<TaskNode> parse(String text) {
        if (text == null) return new ArrayList<>();

        Map<String, TaskNode> nodeMap = new HashMap<>();
        List<TaskNode> nodes = new ArrayList<>();
        String[] lines = text.split("\n");

        // 1. Create Nodes
        for (String line : lines) {
            Matcher m = TASK_PATTERN.matcher(line);
            if (m.find()) {
                boolean isComplete = "x".equals(m.group(1));
                String name = m.group(2).trim();
                String id = m.group(3);

                TaskNode node = new TaskNode(id, name, isComplete);

                Matcher reqMatcher = REQ_PATTERN.matcher(line);
                if (reqMatcher.find()) {
                    String[] reqs = reqMatcher.group(1).split(",");
                    for (String r : reqs) node.addRequirement(r.trim());
                }

                nodeMap.put(id, node);
                nodes.add(node);
            }
        }

        // 2. Link Nodes
        for (TaskNode node : nodes) {
            for (String reqId : node.getRequiredIds()) {
                TaskNode parent = nodeMap.get(reqId);
                if (parent != null) {
                    node.addParent(parent);
                    parent.addChild(node);
                }
            }
        }
        return nodes;
    }
}
