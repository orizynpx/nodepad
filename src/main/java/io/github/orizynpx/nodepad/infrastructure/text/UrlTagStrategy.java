package io.github.orizynpx.nodepad.infrastructure.text;

import io.github.orizynpx.nodepad.domain.model.TaskNode;
import io.github.orizynpx.nodepad.domain.port.TagStrategy;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UrlTagStrategy implements TagStrategy {
    private static final Pattern PATTERN = Pattern.compile("@url\\(([^)]+)\\)");

    @Override
    public void process(TaskNode node, String line) {
        Matcher m = PATTERN.matcher(line);
        if (m.find()) {
            node.setUrl(m.group(1).trim());
        }
    }
}