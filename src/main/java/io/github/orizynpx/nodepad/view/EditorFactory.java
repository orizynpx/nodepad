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
                    "|(?<URL>@url\\([^)]+\\))" + // ADDED THIS
                    "|(?<DONE>@done)" +
                    "|(?<COMMENT>#.*)"
    );

    public static CodeArea createCodeArea() {
        CodeArea codeArea = new CodeArea();
        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));

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
            String styleClass =
                    matcher.group("ID") != null ? "keyword-id" :
                            matcher.group("REQ") != null ? "keyword-req" :
                                    matcher.group("ISBN") != null ? "keyword-isbn" :
                                            matcher.group("URL") != null ? "keyword-url" : // MAPPED HERE
                                                    matcher.group("DONE") != null ? "keyword-done" :
                                                            matcher.group("COMMENT") != null ? "comment" :
                                                                    null;

            spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
            spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
            lastKwEnd = matcher.end();
        }
        spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
        return spansBuilder.create();
    }
}