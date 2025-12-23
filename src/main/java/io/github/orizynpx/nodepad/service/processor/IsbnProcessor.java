package io.github.orizynpx.nodepad.service.processor;

import io.github.orizynpx.nodepad.model.TaskNode;
import io.github.orizynpx.nodepad.service.SyntaxDefinitions;
import java.util.regex.Matcher;

public class IsbnProcessor implements TagProcessor {
    @Override
    public void process(TaskNode node, String line) {
        Matcher m = SyntaxDefinitions.ISBN.matcher(line);
        if (m.find()) {
            String isbn = m.group(1).trim();

            // Validate before attaching to prevent bad API calls later
            if (SyntaxDefinitions.isValidIsbn(isbn)) {
                node.setIsbn(isbn);
            } else {
                System.err.println("Invalid ISBN ignored: " + isbn);
            }
        }
    }
}