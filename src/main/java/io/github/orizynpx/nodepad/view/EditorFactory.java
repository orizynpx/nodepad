package io.github.orizynpx.nodepad.view;

import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EditorFactory {
    private static final Pattern PATTERN = Pattern.compile(
            "(?<ID>@id\\([^)]+\\))" +
                    "|(?<REQ>@(?:req|requires)\\([^)]+\\))" +
                    "|(?<ISBN>@isbn\\([^)]+\\))" +
                    "|(?<URL>@url\\([^)]+\\))" +
                    "|(?<DONE>@done)" +
                    "|(?<COMMENT>#.*)"
    );

    public static CodeArea createCodeArea() {
        CodeArea codeArea = new CodeArea();

        // Enable line numbers
        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));

        // Reattaching the listener. If this is missing, colors won't update
        codeArea.textProperty().addListener((obs, oldText, newText) -> {
            codeArea.setStyleSpans(0, computeHighlighting(newText));
        });

        codeArea.getStyleClass().add("code-area");
        return codeArea;
    }

    private static StyleSpans<Collection<String>> computeHighlighting(String text) {
        Matcher matcher = PATTERN.matcher(text);
        int lastKwEnd = 0;
        StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();

        while (matcher.find()) {
            String styleClass = null;

            if (matcher.group("ID") != null) {
                styleClass = "keyword-id";
            } else if (matcher.group("REQ") != null) {
                styleClass = "keyword-req";
            } else if (matcher.group("ISBN") != null) {
                styleClass = "keyword-isbn";
            } else if (matcher.group("URL") != null) {
                styleClass = "keyword-url";
            } else if (matcher.group("DONE") != null) {
                styleClass = "keyword-done";
            } else if (matcher.group("COMMENT") != null) {
                styleClass = "comment";
            }

            // 1. Add unstyled text (gap between last match and this match)
            spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);

            // 2. Add styled text (the keyword itself)
            // Safety check: if styleClass is null (shouldn't happen), use empty style
            Collection<String> style = (styleClass != null)
                    ? Collections.singleton(styleClass)
                    : Collections.emptyList();

            spansBuilder.add(style, matcher.end() - matcher.start());

            lastKwEnd = matcher.end();
        }

        // 3. Add remaining unstyled text
        spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);

        return spansBuilder.create();
    }
}