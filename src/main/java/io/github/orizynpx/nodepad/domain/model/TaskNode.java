package io.github.orizynpx.nodepad.domain.model;

// (f) Class inheriting from GraphElement
public class TaskNode extends GraphElement {

    private String description = "";
    private NodeStatus status;
    private String isbn;
    private String url;
    private int index; // Preserves text order

    // (g) Multiple Constructor 1: Detailed
    public TaskNode(String id, String label, NodeStatus status) {
        super(id, label);
        this.status = status;
    }

    // (g) Multiple Constructor 2: Minimal (Defaults)
    public TaskNode(String id) {
        this(id, id, NodeStatus.LOCKED);
    }

    // (f) Overriding parent method
    @Override
    public String getType() {
        return "TASK";
    }

    // (d) Getters and Setters
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public NodeStatus getStatus() {
        return status;
    }

    public void setStatus(NodeStatus status) {
        this.status = status;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}