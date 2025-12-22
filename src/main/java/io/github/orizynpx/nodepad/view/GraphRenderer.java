package io.github.orizynpx.nodepad.view;

import io.github.orizynpx.nodepad.model.Edge;
import io.github.orizynpx.nodepad.model.GraphModel;
import io.github.orizynpx.nodepad.model.TaskNode;
import io.github.orizynpx.nodepad.model.NodeStatus;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.CubicCurve;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import java.awt.Desktop;
import java.net.URI;
import java.util.*;
import java.util.function.Consumer;

public class GraphRenderer extends Pane {

    // --- CONFIGURATION ---
    private static final double NODE_RADIUS = 15.0;
    private static final double VERTICAL_SPACING = 100.0;
    private static final double MIN_NODE_GAP = 30.0;
    private static final double FOREST_GAP = 150.0;
    private static final double BEZIER_OFFSET = 50.0;
    private static final double TOP_PADDING = 50.0;
    private static final double LEFT_PADDING = 50.0;

    private Consumer<String> onStatusToggle;
    private VBox activeOverlay;
    private double lastMouseX, lastMouseY;
    private final Group contentGroup = new Group();

    public GraphRenderer() {
        getChildren().add(contentGroup);
        setPickOnBounds(true);
        setStyle("-fx-background-color: transparent;");
        setCursor(Cursor.MOVE);
        enablePanning();
    }

    public void setOnStatusToggle(Consumer<String> onStatusToggle) {
        this.onStatusToggle = onStatusToggle;
    }

    public void render(GraphModel graph) {
        contentGroup.getChildren().clear();
        activeOverlay = null;

        if (graph.getNodes().isEmpty()) return;

        // 1. DATA PREP
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

        // 2. GROUP COMPONENTS
        List<Set<String>> components = findConnectedComponents(nodeData.keySet(), undirectedAdj);
        components.sort(Comparator.comparingInt(comp -> comp.stream()
                .mapToInt(id -> nodeData.get(id).getIndex())
                .min().orElse(Integer.MAX_VALUE)));

        // 3. LAYOUT (Restored logic!)
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

        // 4. DRAW
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
            // Polymorphic Color Call
            String color = node.getDisplayColor();
            c.setFill(Color.web(color));
            c.setStroke(Color.web(color).darker());

            c.setOnMouseClicked(e -> {
                e.consume();
                showOverlay(node, x, y);
            });

            Label label = new Label(node.getLabel());
            label.setTextFill(Color.WHITE);
            label.setFont(Font.font("Consolas", FontWeight.BOLD, 12));
            label.setTextAlignment(TextAlignment.CENTER);

            if (node.getIsbn() != null || node.getUrl() != null) {
                Label meta = new Label((node.getIsbn() != null ? "📖 " : "") + (node.getUrl() != null ? "🔗" : ""));
                meta.setTextFill(Color.GRAY);
                meta.setFont(Font.font(10));
                meta.setLayoutX(x + 10);
                meta.setLayoutY(y - 15);
                contentGroup.getChildren().add(meta);
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

    private void layoutNodeRecursively(String nodeId, int depth, double[] leafCursor,
                                       Map<String, Point2D> positions,
                                       Map<String, List<String>> childrenMap,
                                       Map<String, Double> nodeWidths) {
        if (positions.containsKey(nodeId)) return;
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
            if (activeOverlay != null) {
                contentGroup.getChildren().remove(activeOverlay);
                activeOverlay = null;
            }
            lastMouseX = event.getSceneX();
            lastMouseY = event.getSceneY();
        });
        this.setOnMouseDragged(event -> {
            double deltaX = event.getSceneX() - lastMouseX;
            double deltaY = event.getSceneY() - lastMouseY;
            contentGroup.setTranslateX(contentGroup.getTranslateX() + deltaX);
            contentGroup.setTranslateY(contentGroup.getTranslateY() + deltaY);
            lastMouseX = event.getSceneX();
            lastMouseY = event.getSceneY();
        });
    }

    private void showOverlay(TaskNode node, double x, double y) {
        if (activeOverlay != null) contentGroup.getChildren().remove(activeOverlay);

        VBox overlay = new VBox(8);
        overlay.setPadding(new Insets(10));
        overlay.setStyle("-fx-background-color: rgba(30, 30, 30, 0.98); -fx-border-color: #00ffff; -fx-border-radius: 8; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, black, 10, 0, 0, 0);");
        overlay.setMinWidth(220);
        overlay.setMaxWidth(300);

        Label title = new Label(node.getLabel());
        title.setStyle("-fx-text-fill: #00ffff; -fx-font-weight: bold; -fx-font-size: 14px;");
        title.setWrapText(true);
        overlay.getChildren().add(title);

        if (!node.getDescription().isEmpty()) {
            Label desc = new Label(node.getDescription());
            desc.setStyle("-fx-text-fill: #cccccc; -fx-font-size: 12px;");
            desc.setWrapText(true);
            overlay.getChildren().add(desc);
        }

        Button btn = new Button();
        btn.setMaxWidth(Double.MAX_VALUE);

        if (node.getStatus() == NodeStatus.DONE) {
            btn.setText("Mark Incomplete");
            btn.setStyle("-fx-background-color: #442222; -fx-text-fill: #ff9999;");
            btn.setOnAction(e -> {
                if (onStatusToggle != null) onStatusToggle.accept(node.getId());
                closeOverlay();
            });
        } else if (node.getStatus() == NodeStatus.LOCKED) {
            btn.setText("Locked");
            btn.setDisable(true);
        } else {
            btn.setText("Mark Completed");
            btn.setStyle("-fx-background-color: #224422; -fx-text-fill: #99ff99;");
            btn.setOnAction(e -> {
                if (onStatusToggle != null) onStatusToggle.accept(node.getId());
                closeOverlay();
            });
        }
        overlay.getChildren().add(btn);

        overlay.setLayoutX(x + 25);
        overlay.setLayoutY(y - 20);
        this.activeOverlay = overlay;
        contentGroup.getChildren().add(overlay);
    }

    private void closeOverlay() {
        if (activeOverlay != null) {
            contentGroup.getChildren().remove(activeOverlay);
            activeOverlay = null;
        }
    }
}