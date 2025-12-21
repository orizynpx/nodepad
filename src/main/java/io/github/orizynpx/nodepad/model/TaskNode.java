package io.github.orizynpx.nodepad.model;

import java.util.ArrayList;
import java.util.List;

public class TaskNode {
    private String id;
    private String name;
    private boolean isCompleted;
    private String description;

    // Graph Topology
    private List<String> requiredIds = new ArrayList<>();
    private List<TaskNode> parents = new ArrayList<>();
    private List<TaskNode> children = new ArrayList<>();

    // Visual Coordinates (Calculated by LayoutService)
    private double x;
    private double y;

    public TaskNode(String id, String name, boolean isCompleted) {
        this.id = id;
        this.name = name;
        this.isCompleted = isCompleted;
    }

    // --- Graph Linkers ---
    public void addRequirement(String reqId) {
        requiredIds.add(reqId);
    }
    public void addParent(TaskNode node) { this.parents.add(node); }
    public void addChild(TaskNode node) { this.children.add(node); }

    // --- Getters & Setters ---
    public String getId() { return id; }
    public String getName() { return name; }
    public boolean isCompleted() { return isCompleted; }
    public List<String> getRequiredIds() { return requiredIds; }
    public List<TaskNode> getParents() { return parents; }
    public List<TaskNode> getChildren() { return children; }

    public double getX() { return x; }
    public void setX(double x) { this.x = x; }
    public double getY() { return y; }
    public void setY(double y) { this.y = y; }
}
