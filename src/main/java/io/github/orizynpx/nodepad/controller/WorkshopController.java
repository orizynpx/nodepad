package io.github.orizynpx.nodepad.controller;

import io.github.orizynpx.nodepad.app.ServiceRegistry;
import io.github.orizynpx.nodepad.dao.ContentRepository;
import io.github.orizynpx.nodepad.dao.FileRepository;
import io.github.orizynpx.nodepad.model.Edge;
import io.github.orizynpx.nodepad.model.GraphModel;
import io.github.orizynpx.nodepad.service.ParserService;
import io.github.orizynpx.nodepad.service.TaskMutatorService;
import io.github.orizynpx.nodepad.view.EditorFactory;
import io.github.orizynpx.nodepad.view.GraphRenderer;
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
import java.util.*;

public class WorkshopController {

    @FXML
    private StackPane editorContainer;
    @FXML
    private StackPane graphContainer;

    private CodeArea codeArea;
    private GraphRenderer renderer;

    private final ParserService parserService;
    private final TaskMutatorService taskMutatorService;
    private final ContentRepository contentRepository;
    private final FileRepository fileRepository;

    private GraphModel currentModel;
    private File currentFile;

    public WorkshopController() {
        this.parserService = ServiceRegistry.getInstance().getParserService();
        this.taskMutatorService = ServiceRegistry.getInstance().getTaskMutatorService();
        this.contentRepository = ServiceRegistry.getInstance().getContentRepository();
        this.fileRepository = ServiceRegistry.getInstance().getFileRepository();
    }

    public void setCurrentFile(File file) {
        this.currentFile = file;
    }

    @FXML
    public void initialize() {
        // 1. Editor
        codeArea = EditorFactory.createCodeArea();
        codeArea.setWrapText(true);
        editorContainer.getChildren().add(new VirtualizedScrollPane<>(codeArea));

        Button saveBtn = new Button("SAVE");
        saveBtn.setStyle("-fx-background-color: #00ffff; -fx-text-fill: black; -fx-font-weight: bold; -fx-cursor: hand;");
        saveBtn.setOnAction(e -> save());

        // Position it top-right of the editor
        StackPane.setAlignment(saveBtn, Pos.TOP_RIGHT);
        StackPane.setMargin(saveBtn, new Insets(10));
        editorContainer.getChildren().add(saveBtn);

        // 2. Renderer
        renderer = new GraphRenderer();
        renderer.setOnStatusToggle(this::toggleTask);
        graphContainer.getChildren().add(renderer);

        // 3. Default Text
        String defaultText = """
                Project: RPG Roadmap
                - Learn C# Basics @id(c_sharp) @done
                  Master variables, loops, and classes.
                - Install Unity Engine @id(unity_install) @done
                - Player Movement Script @id(mv_script) @done @req(c_sharp, unity_install)
                  Basic WASD movement implementation.
                - 2D Physics Assets @id(phys_2d) @done @req(unity_install)
                - Jump Mechanic @id(jump) @done @req(mv_script, phys_2d)
                  Requires both code knowledge and physics engine setup.
                - Level Design 101 @id(lvl_des) @done @isbn(978-1466598645)
                - Create First Level @id(lvl_1) @done @req(lvl_des, phys_2d)
                - Game Loop Manager @id(gm_loop) @done @req(c_sharp)
                - Alpha Build @id(alpha) @req(jump, lvl_1, gm_loop)
                  The converging point where code, art, and design meet.
                """;

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
            saveAs(); // If new project, ask where to save
        } else {
            try {
                // 1. Write to disk
                contentRepository.saveContent(currentFile, content);
                // 2. Update DB (Recent Files)
                fileRepository.addOrUpdateFile(currentFile.getAbsolutePath());
                System.out.println("File saved: " + currentFile.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void saveAs() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Project");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));

        File file = fileChooser.showSaveDialog(editorContainer.getScene().getWindow());
        if (file != null) {
            // Force .txt extension if missing
            if (!file.getName().endsWith(".txt")) {
                file = new File(file.getAbsolutePath() + ".txt");
            }

            this.currentFile = file;
            save(); // Execute save now that we have a file
        }
    }

    private void updateGraph(String text) {
        if (text == null) {
            return;
        }

        currentModel = parserService.parse(text); // Store model for logic
        renderer.render(currentModel);
    }

    private void toggleTask(String nodeId) {
        String currentText = codeArea.getText();
        String newText = taskMutatorService.toggleTaskStatus(currentText, currentModel, nodeId);

        if (!newText.equals(currentText)) {
            codeArea.replaceText(newText);
        }
    }
}