package io.github.orizynpx.nodepad.view;

import javafx.scene.text.Font;
import javafx.scene.text.Text;
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
                    "|(?<REQ>@(?:req|requires)\\([^)]+\\))" + // UPDATED regex here
                    "|(?<ISBN>@isbn\\([^)]+\\))" +
                    "|(?<DONE>@done)" +
                    "|(?<COMMENT>#.*)"
    );

    public static CodeArea createCodeArea() {
        CodeArea codeArea = new CodeArea();
        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));

        // ... highlighting listener ...

        // 1. FONT SETUP
        codeArea.setStyle("-fx-font-family: 'Consolas', 'Monospace'; -fx-font-size: 14;");

        // 2. TAB SIZE CONFIGURATION
        // We calculate the pixel width of 4 spaces (' ') using the current font
        Text t = new Text("    ");
        t.setFont(Font.font("Consolas", 14)); // Must match the CSS above
        double tabWidth = t.getLayoutBounds().getWidth();

        // Apply logic: "Tab behaves like 4 spaces"
        //codeArea.updateProperties(); // Refresh CSS first
        // There isn't a direct "setTabSize" in basic RichTextFX without helper,
        // but typically users just type spaces.
        // However, if they paste tabs, ParserService now handles them (line.replaceAll).

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
