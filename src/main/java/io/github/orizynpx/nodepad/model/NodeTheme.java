package io.github.orizynpx.nodepad.model;

public class NodeTheme {
    public static String getColorForStatus(NodeStatus status) {
        return switch (status) {
            case DONE -> "#00ff99";
            case UNLOCKED -> "#ffd700";
            default -> "#333333";
        };
    }
}