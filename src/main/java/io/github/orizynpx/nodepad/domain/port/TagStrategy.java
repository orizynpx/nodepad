package io.github.orizynpx.nodepad.domain.port;

import io.github.orizynpx.nodepad.domain.model.TaskNode;

public interface TagStrategy {
    void process(TaskNode node, String lineContent);
}