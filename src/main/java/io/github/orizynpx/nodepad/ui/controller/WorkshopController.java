package io.github.orizynpx.nodepad.ui.controller;

import io.github.orizynpx.nodepad.domain.logic.GraphLayoutEngine;
import io.github.orizynpx.nodepad.domain.logic.TaskMutatorService; // NEW IMPORT
import io.github.orizynpx.nodepad.domain.logic.TextParser;
import io.github.orizynpx.nodepad.domain.model.GraphModel;
import io.github.orizynpx.nodepad.ui.components.EditorArea;
import io.github.orizynpx.nodepad.ui.components.GraphCanvas;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.StackPane;

public class WorkshopController {
    @FXML
    private StackPane editorContainer;
    @FXML
    private StackPane graphContainer;

    private EditorArea editor;
    private GraphCanvas canvas;

    // Dependencies injected
    private final TextParser textParser;
    private final GraphLayoutEngine layoutEngine;
    private final TaskMutatorService taskMutatorService;

    // Current state (only for logic, not for direct UI manipulation)
    private GraphModel currentGraphModel;

    // Constructor Injection
    public WorkshopController(TextParser textParser,
                              GraphLayoutEngine layoutEngine,
                              TaskMutatorService taskMutatorService) {
        this.textParser = textParser;
        this.layoutEngine = layoutEngine;
        this.taskMutatorService = taskMutatorService; // ASSIGN
    }

    @FXML
    public void initialize() {
        // 1. Setup Editor
        editor = new EditorArea();
        editorContainer.getChildren().add(editor);

        // 2. Setup Graph Canvas
        canvas = new GraphCanvas();
        ScrollPane scrollPane = new ScrollPane(canvas);
        scrollPane.setPannable(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: #252525; -fx-background-color: #252525;");

        graphContainer.getChildren().add(scrollPane);

        // 3. Set Default Text (initial content for a new project)
        String defaultText = """
                Project: Refactored Arch
                - Domain Layer @id(domain)
                - Infrastructure @id(infra) @req(domain)
                - UI Layer @id(ui) @req(domain)
                - Dependency Injection @id(di) @req(infra, ui)
                """;

        editor.setText(defaultText);
        updateGraph(defaultText); // Initial render

        // 4. Bind Updates from Editor to Graph
        editor.getRawCodeArea().textProperty().addListener((obs, old, val) -> updateGraph(val));

        // 5. Bind Actions from Graph Canvas to Controller
        canvas.setOnNodeAction(this::toggleTaskStatus); // Use new method name
    }

    public void loadContent(String content) {
        Platform.runLater(() -> {
            editor.setText(content);
            updateGraph(content);
        });
    }

    private void updateGraph(String text) {
        // 1. Parse Text -> Domain Objects
        this.currentGraphModel = textParser.parse(text);

        // 2. Calculate Layout
        var positions = layoutEngine.calculateLayout(currentGraphModel.getNodes(), currentGraphModel.getEdges());

        // 3. Draw
        Platform.runLater(() -> canvas.draw(currentGraphModel.getNodes(), currentGraphModel.getEdges(), positions));
}

        /**
         * Handles user action on a graph node (e.g., double click).
         * Delegates to the domain service for text modification.
         */
    private void toggleTaskStatus(String nodeId) { // Method name changed for clarity
        String currentText = editor.getText();

        // Delegate to the domain service
        String newText = taskMutatorService.toggleTaskStatus(currentText, currentGraphModel, nodeId);

        // Update editor ONLY if the text has actually changed
        if (!newText.equals(currentText)) {
            editor.setText(newText);
        }
    }
}