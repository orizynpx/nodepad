package io.github.orizynpx.nodepad.model.enums;

public enum NodeStatus {
    LOCKED,   // Dependencies not met (Grey)
    UNLOCKED, // Dependencies met, ready to do (Gold/Neon)
    DONE      // Completed (Green)
}
