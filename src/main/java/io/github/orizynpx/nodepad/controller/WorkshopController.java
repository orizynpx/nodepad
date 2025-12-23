package io.github.orizynpx.nodepad.controller;

import io.github.orizynpx.nodepad.dao.ContentRepository;
import io.github.orizynpx.nodepad.dao.FileRepository;
import io.github.orizynpx.nodepad.model.graph.GraphModel;
import io.github.orizynpx.nodepad.model.entity.SharedProjectModel;
import io.github.orizynpx.nodepad.model.graph.TaskNode;
import io.github.orizynpx.nodepad.service.ParserService;
import io.github.orizynpx.nodepad.service.TaskService;
import io.github.orizynpx.nodepad.view.EditorFactory;
import io.github.orizynpx.nodepad.view.renderer.GraphOverlayBuilder;
import io.github.orizynpx.nodepad.view.renderer.GraphRenderer;
import io.github.orizynpx.nodepad.view.renderer.layout.HierarchicalLayout;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class WorkshopController {

    @FXML private StackPane editorContainer;
    @FXML private StackPane graphContainer;

    private CodeArea codeArea;
    private GraphRenderer renderer;
    private GraphOverlayBuilder overlayBuilder;

    private final ParserService parserService;
    private final TaskService taskService;
    private final ContentRepository contentRepository;
    private final FileRepository fileRepository;
    private final SharedProjectModel projectModel;

    private GraphModel currentModel;
    private File currentFile;

    public WorkshopController(ParserService parser,
                              TaskService mutator,
                              ContentRepository contentRepo,
                              FileRepository fileRepo,
                              SharedProjectModel projectModel) {
        this.parserService = parser;
        this.taskService = mutator;
        this.contentRepository = contentRepo;
        this.fileRepository = fileRepo;
        this.projectModel = projectModel;
    }

    public void setCurrentFile(File file) {
        this.currentFile = file;
    }

    @FXML
    public void initialize() {
        codeArea = EditorFactory.createCodeArea();
        codeArea.setWrapText(true);
        editorContainer.getChildren().add(new VirtualizedScrollPane<>(codeArea));

        Button saveBtn = new Button("SAVE");
        saveBtn.getStyleClass().add("btn-save");
        saveBtn.setOnAction(e -> save());
        StackPane.setAlignment(saveBtn, Pos.TOP_RIGHT);
        StackPane.setMargin(saveBtn, new Insets(10));
        editorContainer.getChildren().add(saveBtn);

        // 2. UI Builder & Renderer Setup
        this.overlayBuilder = new GraphOverlayBuilder(this::toggleTask);
        this.renderer = new GraphRenderer(new HierarchicalLayout());
        this.renderer.setOverlayProvider(overlayBuilder::createOverlay);
        graphContainer.getChildren().add(renderer);

        // 3. Default Content
        String defaultText = """
                Project: RPG Roadmap
                - Learn C# Basics @id(c_sharp) @done
                """; // Shortened for brevity

        Platform.runLater(() -> {
            if (codeArea.getText().isEmpty()) {
                codeArea.replaceText(defaultText);
                updateGraph(defaultText);
            }
        });

        codeArea.textProperty().addListener((obs, oldText, newText) -> updateGraph(newText));
    }

    public void loadContent(String content) {
        Platform.runLater(() -> {
            codeArea.replaceText(content);
            updateGraph(content);
        });
    }

    private void save() {
        String content = codeArea.getText();
        if (currentFile == null) {
            saveAs();
        } else {
            try {
                contentRepository.saveContent(currentFile, content);
                fileRepository.addOrUpdateFile(currentFile.getAbsolutePath());
                System.out.println("File saved: " + currentFile.getAbsolutePath());
            } catch (IOException e) { e.printStackTrace(); }
        }
    }

    private void saveAs() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Project");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
        File file = fileChooser.showSaveDialog(editorContainer.getScene().getWindow());
        if (file != null) {
            if (!file.getName().endsWith(".txt")) file = new File(file.getAbsolutePath() + ".txt");
            this.currentFile = file;
            save();
        }
    }

    private void updateGraph(String text) {
        if (text == null) return;
        currentModel = parserService.parse(text);
        renderer.render(currentModel);
        pushContextToInventory();
    }

    private void pushContextToInventory() {
        Set<String> isbns = new HashSet<>();
        Set<String> urls = new HashSet<>();

        if (currentModel != null) {
            for (TaskNode node : currentModel.getNodes()) {
                if (node.getIsbn() != null) isbns.add(node.getIsbn());
                if (node.getUrl() != null) urls.add(node.getUrl());
            }
        }
        // Push to Shared Model -> Updates Inventory Automatically
        projectModel.updateContext(isbns, urls);
    }

    private void toggleTask(String nodeId) {
        String currentText = codeArea.getText();
        String newText = taskService.toggleTaskStatus(currentText, currentModel, nodeId);
        if (!newText.equals(currentText)) {
            codeArea.replaceText(newText);
        }
    }
}