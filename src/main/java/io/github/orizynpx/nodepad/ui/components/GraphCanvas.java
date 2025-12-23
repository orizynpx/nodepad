package io.github.orizynpx.nodepad.ui.components;

import io.github.orizynpx.nodepad.domain.logic.GraphLayoutEngine.NodePosition;
import io.github.orizynpx.nodepad.domain.model.Edge;
import io.github.orizynpx.nodepad.domain.model.NodeStatus;
import io.github.orizynpx.nodepad.domain.model.TaskNode;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.CubicCurve;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class GraphCanvas extends Pane {

    private static final double NODE_RADIUS = 15.0;

    // Interaction State
    private final Group contentGroup = new Group(); // To support panning
    private double lastMouseX, lastMouseY;
    private boolean isDragging = false;
    private VBox activeOverlay;

    // Callback for Controller
    private Consumer<String> onNodeAction;

    public GraphCanvas() {
        getChildren().add(contentGroup);

        // Panning Setup
        this.setStyle("-fx-background-color: transparent;");
        this.setPickOnBounds(true);
        this.setCursor(Cursor.MOVE);

        enablePanning();

        // Click background to close overlay
        this.setOnMouseClicked(e -> {
            if (!isDragging && e.getTarget() == this) closeOverlay();
        });
    }

    public void setOnNodeAction(Consumer<String> action) {
        this.onNodeAction = action;
    }

    public void draw(List<TaskNode> nodes, List<Edge> edges, Map<String, NodePosition> positions) {
        contentGroup.getChildren().clear();
        activeOverlay = null;

        // 1. Draw Edges
        for (Edge edge : edges) {
            if (positions.containsKey(edge.getSourceId()) && positions.containsKey(edge.getTargetId())) {
                drawBezier(positions.get(edge.getSourceId()), positions.get(edge.getTargetId()));
            }
        }

        // 2. Draw Nodes
        for (TaskNode node : nodes) {
            if (positions.containsKey(node.getId())) {
                drawNode(node, positions.get(node.getId()));
            }
        }
    }

    private void drawBezier(NodePosition start, NodePosition end) {
        CubicCurve curve = new CubicCurve();
        curve.setStartX(start.x);
        curve.setStartY(start.y + NODE_RADIUS);
        curve.setEndX(end.x);
        curve.setEndY(end.y - NODE_RADIUS);
        curve.setControlX1(start.x);
        curve.setControlY1(start.y + NODE_RADIUS + 50);
        curve.setControlX2(end.x);
        curve.setControlY2(end.y - NODE_RADIUS - 50);
        curve.setStroke(Color.web("#555"));
        curve.setStrokeWidth(2);
        curve.setFill(null);
        contentGroup.getChildren().add(curve);
    }

    private void drawNode(TaskNode node, NodePosition pos) {
        Circle c = new Circle(pos.x, pos.y, NODE_RADIUS);
        c.setCursor(Cursor.HAND);

        if (node.getStatus() == NodeStatus.DONE) {
            c.setFill(Color.web("#00ff99")); c.setStroke(Color.web("#00ff99"));
        } else if (node.getStatus() == NodeStatus.UNLOCKED) {
            c.setFill(Color.web("#ffd700")); c.setStroke(Color.web("#ffd700"));
        } else {
            c.setFill(Color.web("#333")); c.setStroke(Color.web("#666"));
        }

        // Click Handler
        c.setOnMouseClicked(e -> {
            e.consume(); // Prevent panning logic
            showOverlay(node, pos.x, pos.y);
        });

        Label label = new Label(node.getLabel());
        label.setTextFill(Color.WHITE);
        label.setFont(Font.font("Consolas", FontWeight.BOLD, 12));
        label.setTextAlignment(TextAlignment.CENTER);

        // Center label
        label.layoutBoundsProperty().addListener((obs, old, bounds) -> {
            label.setLayoutX(pos.x - (bounds.getWidth() / 2));
            label.setLayoutY(pos.y + NODE_RADIUS + 5);
        });

        contentGroup.getChildren().addAll(c, label);
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

    private void showOverlay(TaskNode node, double x, double y) {
        closeOverlay();

        VBox overlay = new VBox(8);
        overlay.setPadding(new Insets(10));
        overlay.setStyle("-fx-background-color: rgba(30, 30, 30, 0.98); -fx-border-color: #00ffff; -fx-border-radius: 8; -fx-effect: dropshadow(three-pass-box, black, 10, 0, 0, 0);");
        overlay.setMinWidth(200);
        overlay.setCursor(Cursor.DEFAULT);

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

        // Action Button
        Button btn = new Button(node.getStatus() == NodeStatus.DONE ? "Mark Incomplete" : "Mark Completed");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setStyle("-fx-background-color: #333; -fx-text-fill: white; -fx-border-color: #555; -fx-cursor: hand;");

        btn.setOnAction(e -> {
            if (onNodeAction != null) onNodeAction.accept(node.getId());
            closeOverlay();
        });
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