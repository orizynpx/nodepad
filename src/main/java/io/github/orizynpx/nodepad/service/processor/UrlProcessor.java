package io.github.orizynpx.nodepad.service.processor;

import io.github.orizynpx.nodepad.model.TaskNode;
import io.github.orizynpx.nodepad.service.SyntaxDefinitions;
import java.util.regex.Matcher;

public class UrlProcessor implements TagProcessor {

    @Override
    public void process(TaskNode node, String line) {
        Matcher m = SyntaxDefinitions.URL.matcher(line);
        if (m.find()) {
            String url = m.group(1).trim();

            if (SyntaxDefinitions.isValidUrl(url)) {
                node.setUrl(url);
            }
        }
    }
}