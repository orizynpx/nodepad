package io.github.orizynpx.nodepad.view.renderer;

import io.github.orizynpx.nodepad.model.enums.NodeStatus;
import io.github.orizynpx.nodepad.model.graph.Edge;
import io.github.orizynpx.nodepad.model.graph.GraphModel;
import io.github.orizynpx.nodepad.model.graph.TaskNode;
import io.github.orizynpx.nodepad.view.renderer.layout.LayoutStrategy;
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
import javafx.scene.text.TextAlignment;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public class GraphRenderer extends Pane {

    // Visual Styling Constants
    private static final double NODE_RADIUS = 15.0;
    private static final double BEZIER_OFFSET = 50.0;

    private final LayoutStrategy layoutStrategy;
    private BiFunction<TaskNode, Runnable, Node> overlayProvider;
    private Node activeOverlay;
    private final Group contentGroup = new Group();

    // Panning State
    private double lastMouseX, lastMouseY;
    private boolean isDragging = false;

    // CONSTRUCTOR INJECTION: We pass the strategy here
    public GraphRenderer(LayoutStrategy layoutStrategy) {
        this.layoutStrategy = layoutStrategy;

        getChildren().add(contentGroup);
        setPickOnBounds(true);
        getStyleClass().add("graph-pane");
        setCursor(Cursor.MOVE);
        enablePanning();

        // Click background to close overlay
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

        // 1. DELEGATE MATH TO STRATEGY
        Map<String, Point2D> positions = layoutStrategy.calculatePositions(graph);

        // Map nodes for quick lookup
        Map<String, TaskNode> nodeMap = new HashMap<>();
        for (TaskNode n : graph.getNodes()) nodeMap.put(n.getId(), n);

        // 2. DRAW EDGES
        for (Edge e : graph.getEdges()) {
            if (positions.containsKey(e.getSourceId()) && positions.containsKey(e.getTargetId())) {
                Point2D start = positions.get(e.getSourceId());
                Point2D end = positions.get(e.getTargetId());
                drawCurve(start, end);
            }
        }

        // 3. DRAW NODES
        for (TaskNode node : graph.getNodes()) {
            if (!positions.containsKey(node.getId())) continue;
            Point2D pos = positions.get(node.getId());
            drawNode(node, pos.getX(), pos.getY());
        }
    }

    private void drawCurve(Point2D start, Point2D end) {
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

    private void drawNode(TaskNode node, double x, double y) {
        // Circle
        Circle c = new Circle(x, y, NODE_RADIUS);
        c.setCursor(Cursor.HAND);
        Color fillColor = getStatusColor(node.getStatus());
        c.setFill(fillColor);
        c.setStroke(fillColor.darker());

        c.setOnMouseClicked(e -> {
            e.consume();
            showOverlay(node, x, y);
        });

        // Label
        Label label = new Label(node.getLabel());
        label.setTextFill(Color.WHITE);
        label.setFont(Font.font("Consolas", FontWeight.BOLD, 12));
        label.setTextAlignment(TextAlignment.CENTER);

        // Center label below node
        label.layoutBoundsProperty().addListener((obs, old, bounds) -> {
            label.setLayoutX(x - (bounds.getWidth() / 2));
            label.setLayoutY(y + NODE_RADIUS + 5);
        });
        // Initial positioning approximation (will be corrected by listener)
        label.setLayoutX(x);
        label.setLayoutY(y + NODE_RADIUS + 5);

        contentGroup.getChildren().addAll(c, label);

        // Icons
        if (node.getIsbn() != null || node.getUrl() != null) {
            String iconText = (node.getIsbn() != null ? "📖" : "") + (node.getUrl() != null ? "🔗" : "");
            Label icon = new Label(iconText);
            icon.setTextFill(node.getStatus() == NodeStatus.LOCKED ? Color.LIGHTGRAY : Color.BLACK);
            icon.setFont(Font.font(10));
            icon.setMouseTransparent(true);

            icon.layoutBoundsProperty().addListener((obs, old, bounds) -> {
                icon.setLayoutX(x - (bounds.getWidth() / 2));
                icon.setLayoutY(y - (bounds.getHeight() / 2));
            });
            contentGroup.getChildren().add(icon);
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
            case DONE -> Color.web("#00ff99");
            case UNLOCKED -> Color.web("#ffd700");
            default -> Color.web("#333333");
        };
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