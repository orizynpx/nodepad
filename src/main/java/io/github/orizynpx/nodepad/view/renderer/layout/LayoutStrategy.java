package io.github.orizynpx.nodepad.view.renderer.layout;

import io.github.orizynpx.nodepad.model.graph.GraphModel;
import javafx.geometry.Point2D;

import java.util.Map;

public interface LayoutStrategy {
    Map<String, Point2D> calculatePositions(GraphModel graph);
}
