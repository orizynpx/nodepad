package io.github.orizynpx.nodepad.ui.components;

import javafx.scene.layout.StackPane;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EditorArea extends StackPane {

    private final CodeArea codeArea;

    private static final Pattern PATTERN = Pattern.compile(
            "(?<ID>@id\\([^)]+\\))" +
                    "|(?<REQ>@(?:req|requires)\\([^)]+\\))" +
                    "|(?<ISBN>@isbn\\([^)]+\\))" +
                    "|(?<DONE>@done)" +
                    "|(?<COMMENT>#.*)"
    );

    public EditorArea() {
        this.codeArea = new CodeArea();
        this.codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
        this.codeArea.setWrapText(true);
        this.codeArea.setStyle("-fx-font-family: 'Consolas', 'Monospace'; -fx-font-size: 14;");

        this.codeArea.textProperty().addListener((obs, oldText, newText) -> {
            codeArea.setStyleSpans(0, computeHighlighting(newText));
        });
        // Add to view wrapped in ScrollPane
        this.getChildren().add(new VirtualizedScrollPane<>(codeArea));
    }

    public String getText() {
        return codeArea.getText();
    }

    public void setText(String text) {
        codeArea.replaceText(text);
    }

    public CodeArea getRawCodeArea() {
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