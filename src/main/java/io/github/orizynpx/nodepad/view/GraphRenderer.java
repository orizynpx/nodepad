package io.github.orizynpx.nodepad.view;

import io.github.orizynpx.nodepad.model.TaskNode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import java.util.List;

public class GraphRenderer {
    public void draw(Pane pane, List<TaskNode> nodes) {
        pane.getChildren().clear();

        // 1. Draw Lines (Connections) first so they are behind nodes
        for (TaskNode node : nodes) {
            for (TaskNode parent : node.getParents()) {
                Line line = new Line(node.getX(), node.getY(), parent.getX(), parent.getY());
                line.setStroke(Color.GRAY);
                line.setStrokeWidth(2);
                pane.getChildren().add(line);
            }
        }

        // 2. Draw Nodes
        for (TaskNode node : nodes) {
            Circle c = new Circle(node.getX(), node.getY(), 20);
            c.setFill(node.isCompleted() ? Color.LIGHTGREEN : Color.WHITE);
            c.setStroke(Color.BLACK);

            Text t = new Text(node.getX() - 15, node.getY() + 35, node.getName());

            // Interaction: Show Description on Hover (Simple implementation)
            if (!node.getName().isEmpty()) {
                c.setOnMouseEntered(e -> System.out.println("Hover: " + node.getName()));
            }

            pane.getChildren().addAll(c, t);
        }
    }
}
