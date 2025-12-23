package io.github.orizynpx.nodepad.view.renderer;

import io.github.orizynpx.nodepad.model.enums.NodeStatus;
import io.github.orizynpx.nodepad.model.graph.Edge;
import io.github.orizynpx.nodepad.model.graph.GraphModel;
import io.github.orizynpx.nodepad.model.graph.TaskNode;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.CubicCurve;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import java.util.*;
import java.util.function.BiFunction;

public class GraphRenderer extends Pane {
    private static final double NODE_RADIUS = 15.0;
    private static final double VERTICAL_SPACING = 100.0;
    private static final double MIN_NODE_GAP = 30.0;
    private static final double FOREST_GAP = 150.0;
    private static final double BEZIER_OFFSET = 50.0;
    private static final double TOP_PADDING = 50.0;
    private static final double LEFT_PADDING = 50.0;

    private BiFunction<TaskNode, Runnable, Node> overlayProvider;

    private Node activeOverlay;
    private final Group contentGroup = new Group();

    private double lastMouseX, lastMouseY;
    private boolean isDragging = false;

    public GraphRenderer() {
        getChildren().add(contentGroup);
        setPickOnBounds(true);

        getStyleClass().add("graph-pane");

        setCursor(Cursor.MOVE);
        enablePanning();

        this.setOnMouseClicked(e -> {
            if (!isDragging && e.getTarget() == this) {
                closeOverlay();
            }
        });
    }

    public void setOverlayProvider(BiFunction<TaskNode, Runnable, Node> overlayProvider) {
        this.overlayProvider = overlayProvider;
    }

    public void render(GraphModel graph) {
        contentGroup.getChildren().clear();
        activeOverlay = null;

        if (graph.getNodes().isEmpty()) return;

        // --- 1. DATA PREP ---
        Map<String, TaskNode> nodeData = new HashMap<>();
        Map<String, Double> nodeWidths = new HashMap<>();
        Map<String, List<String>> childrenMap = new HashMap<>();
        Map<String, List<String>> parentsMap = new HashMap<>();
        Map<String, List<String>> undirectedAdj = new HashMap<>();

        for (TaskNode n : graph.getNodes()) {
            nodeData.put(n.getId(), n);
            childrenMap.put(n.getId(), new ArrayList<>());
            parentsMap.put(n.getId(), new ArrayList<>());
            undirectedAdj.put(n.getId(), new ArrayList<>());

            double textWidth = calculateTextWidth(n.getLabel());
            double totalWidth = Math.max(textWidth, NODE_RADIUS * 2) + MIN_NODE_GAP;
            nodeWidths.put(n.getId(), totalWidth);
        }

        for (Edge e : graph.getEdges()) {
            if (childrenMap.containsKey(e.getSourceId()) && childrenMap.containsKey(e.getTargetId())) {
                childrenMap.get(e.getSourceId()).add(e.getTargetId());
                parentsMap.get(e.getTargetId()).add(e.getSourceId());
                undirectedAdj.get(e.getSourceId()).add(e.getTargetId());
                undirectedAdj.get(e.getTargetId()).add(e.getSourceId());
            }
        }

        for (List<String> children : childrenMap.values()) {
            children.sort(Comparator.comparingInt(id -> nodeData.get(id).getIndex()));
        }

        // --- 2. LAYOUT ---
        List<Set<String>> components = findConnectedComponents(nodeData.keySet(), undirectedAdj);
        components.sort(Comparator.comparingInt(comp -> comp.stream()
                .mapToInt(id -> nodeData.get(id).getIndex())
                .min().orElse(Integer.MAX_VALUE)));

        Map<String, Point2D> positions = new HashMap<>();
        double currentX = LEFT_PADDING;

        for (Set<String> componentIds : components) {
            List<String> compRoots = new ArrayList<>();
            for (String id : componentIds) {
                if (parentsMap.get(id).isEmpty()) compRoots.add(id);
            }
            if (compRoots.isEmpty() && !componentIds.isEmpty()) {
                compRoots.add(componentIds.stream()
                        .min(Comparator.comparingInt(id -> nodeData.get(id).getIndex()))
                        .orElse(componentIds.iterator().next()));
            }
            compRoots.sort(Comparator.comparingInt(id -> nodeData.get(id).getIndex()));

            double[] leafCursor = {currentX};
            for (String rootId : compRoots) {
                if (positions.containsKey(rootId)) continue;
                layoutNodeRecursively(rootId, 0, leafCursor, positions, childrenMap, nodeWidths);
            }
            currentX = leafCursor[0] + FOREST_GAP;
        }

        // --- 3. DRAW ---
        // Edges
        for (Edge e : graph.getEdges()) {
            if (positions.containsKey(e.getSourceId()) && positions.containsKey(e.getTargetId())) {
                Point2D start = positions.get(e.getSourceId());
                Point2D end = positions.get(e.getTargetId());

                CubicCurve curve = new CubicCurve();
                curve.setStartX(start.getX());
                curve.setStartY(start.getY() + NODE_RADIUS);
                curve.setEndX(end.getX());
                curve.setEndY(end.getY() - NODE_RADIUS);
                curve.setControlX1(start.getX());
                curve.setControlY1(start.getY() + NODE_RADIUS + BEZIER_OFFSET);
                curve.setControlX2(end.getX());
                curve.setControlY2(end.getY() - NODE_RADIUS - BEZIER_OFFSET);

                curve.setStroke(Color.web("#555"));
                curve.setStrokeWidth(2);
                curve.setFill(null);
                contentGroup.getChildren().add(curve);
            }
        }

        // Nodes
        for (TaskNode node : graph.getNodes()) {
            if (!positions.containsKey(node.getId())) continue;
            Point2D pos = positions.get(node.getId());
            double x = pos.getX();
            double y = pos.getY();

            Circle c = new Circle(x, y, NODE_RADIUS);
            c.setCursor(Cursor.HAND);

            // FIX: Use internal logic for coloring, not the Node's logic
            Color fillColor = getStatusColor(node.getStatus());
            c.setFill(fillColor);
            c.setStroke(fillColor.darker());

            c.setOnMouseClicked(e -> {
                e.consume();
                showOverlay(node, x, y);
            });

            Label label = new Label(node.getLabel());
            label.setTextFill(Color.WHITE);
            label.setFont(Font.font("Consolas", FontWeight.BOLD, 12));
            label.setTextAlignment(TextAlignment.CENTER);

            // Icon indicators
            if (node.getIsbn() != null || node.getUrl() != null) {
                // Combine icons if both exist
                String iconText = (node.getIsbn() != null ? "📖" : "") + (node.getUrl() != null ? "🔗" : "");

                Label icon = new Label(iconText);
                // Use black for visibility on Gold/Green, White for visibility on Dark Grey
                icon.setTextFill(node.getStatus() == NodeStatus.LOCKED ? Color.LIGHTGRAY : Color.BLACK);
                icon.setFont(Font.font(10)); // Small enough to fit
                icon.setMouseTransparent(true); // CRITICAL: Lets clicks pass through to the Circle

                // Center logic
                icon.layoutBoundsProperty().addListener((obs, old, bounds) -> {
                    icon.setLayoutX(x - (bounds.getWidth() / 2));
                    icon.setLayoutY(y - (bounds.getHeight() / 2));
                });

                contentGroup.getChildren().add(icon);
            }

            label.layoutBoundsProperty().addListener((obs, old, bounds) -> {
                label.setLayoutX(x - (bounds.getWidth() / 2));
                label.setLayoutY(y + NODE_RADIUS + 5);
            });
            label.setLayoutX(x - (nodeWidths.get(node.getId()) / 2));
            label.setLayoutY(y + NODE_RADIUS + 5);

            contentGroup.getChildren().addAll(c, label);
        }
    }

    private void showOverlay(TaskNode node, double x, double y) {
        if (activeOverlay != null) contentGroup.getChildren().remove(activeOverlay);

        if (overlayProvider != null) {
            Node overlay = overlayProvider.apply(node, this::closeOverlay);
            if (overlay != null) {
                overlay.setLayoutX(x + 25);
                overlay.setLayoutY(y - 20);
                this.activeOverlay = overlay;
                contentGroup.getChildren().add(overlay);
            }
        }
    }

    public void closeOverlay() {
        if (activeOverlay != null) {
            contentGroup.getChildren().remove(activeOverlay);
            activeOverlay = null;
        }
    }

    private Color getStatusColor(NodeStatus status) {
        if (status == null) return Color.web("#333333");
        return switch (status) {
            case DONE -> Color.web("#00ff99");    // Green
            case UNLOCKED -> Color.web("#ffd700"); // Gold
            default -> Color.web("#333333");      // Grey
        };
    }

    // --- Helpers (Layout Logic) ---
    private void layoutNodeRecursively(String nodeId, int depth, double[] leafCursor,
                                       Map<String, Point2D> positions,
                                       Map<String, List<String>> childrenMap,
                                       Map<String, Double> nodeWidths) {
        if (positions.containsKey(nodeId)) {
            return;
        }

        List<String> children = childrenMap.get(nodeId);
        double y = TOP_PADDING + (depth * VERTICAL_SPACING);
        double myWidth = nodeWidths.get(nodeId);
        List<String> myPlacedChildren = new ArrayList<>();

        for (String childId : children) {
            if (!positions.containsKey(childId)) {
                layoutNodeRecursively(childId, depth + 1, leafCursor, positions, childrenMap, nodeWidths);
                myPlacedChildren.add(childId);
            }
        }

        double x;
        if (myPlacedChildren.isEmpty()) {
            x = leafCursor[0] + (myWidth / 2.0);
            leafCursor[0] += myWidth;
        } else {
            double minX = Double.MAX_VALUE, maxX = Double.MIN_VALUE;
            for (String childId : myPlacedChildren) {
                double cx = positions.get(childId).getX();
                if (cx < minX) minX = cx;
                if (cx > maxX) maxX = cx;
            }
            x = minX + ((maxX - minX) / 2.0);
            double myRightEdge = x + (myWidth / 2.0);
            if (myRightEdge > leafCursor[0]) leafCursor[0] = myRightEdge;
        }
        positions.put(nodeId, new Point2D(x, y));
    }

    private List<Set<String>> findConnectedComponents(Set<String> nodes, Map<String, List<String>> adj) {
        List<Set<String>> components = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        for (String node : nodes) {
            if (!visited.contains(node)) {
                Set<String> comp = new HashSet<>();
                Queue<String> q = new LinkedList<>();

                q.add(node);
                visited.add(node);
                comp.add(node);

                while (!q.isEmpty()) {
                    String curr = q.poll();
                    for (String neighbor : adj.getOrDefault(curr, Collections.emptyList())) {
                        if (!visited.contains(neighbor)) {
                            visited.add(neighbor);
                            comp.add(neighbor);
                            q.add(neighbor);
                        }
                    }
                }
                components.add(comp);
            }
        }
        return components;
    }

    private double calculateTextWidth(String text) {
        Text t = new Text(text);
        t.setFont(Font.font("Consolas", FontWeight.BOLD, 12));
        return t.getLayoutBounds().getWidth();
    }

    private void enablePanning() {
        this.setOnMousePressed(event -> {
            lastMouseX = event.getSceneX();
            lastMouseY = event.getSceneY();
            isDragging = false;
        });
        this.setOnMouseDragged(event -> {
            isDragging = true;
            double deltaX = event.getSceneX() - lastMouseX;
            double deltaY = event.getSceneY() - lastMouseY;
            contentGroup.setTranslateX(contentGroup.getTranslateX() + deltaX);
            contentGroup.setTranslateY(contentGroup.getTranslateY() + deltaY);
            lastMouseX = event.getSceneX();
            lastMouseY = event.getSceneY();
        });
    }
}