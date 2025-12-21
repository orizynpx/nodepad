package io.github.orizynpx.nodepad.view;

import io.github.orizynpx.nodepad.dao.ContentDAO;
import io.github.orizynpx.nodepad.model.TaskNode;
import io.github.orizynpx.nodepad.service.LayoutService;
import io.github.orizynpx.nodepad.service.ParserService;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Pane;

import java.util.List;

public class MainController {
    @FXML private TextArea editorArea;
    @FXML private Pane graphPane;

    private final ParserService parser = new ParserService();
    private final LayoutService layout = new LayoutService();
    private final GraphRenderer renderer = new GraphRenderer();
    private final ContentDAO contentDAO = new ContentDAO();

    @FXML
    public void initialize() {
        // Load saved data
        String savedText = contentDAO.loadContent();
        editorArea.setText(savedText);
        refreshGraph(savedText);

        // Auto-update graph and auto-save on text change
        editorArea.textProperty().addListener((obs, oldVal, newVal) -> {
            refreshGraph(newVal);
            contentDAO.saveContent(newVal);
        });
    }

    private void refreshGraph(String text) {
        List<TaskNode> nodes = parser.parse(text);
        layout.calculateLayout(nodes);
        renderer.draw(graphPane, nodes);
    }
}
