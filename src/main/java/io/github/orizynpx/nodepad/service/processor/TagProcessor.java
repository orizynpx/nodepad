package io.github.orizynpx.nodepad.service.processor;

import io.github.orizynpx.nodepad.model.TaskNode;

public interface TagProcessor {
    void process(TaskNode node, String line);
}