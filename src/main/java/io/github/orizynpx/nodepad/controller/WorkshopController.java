package io.github.orizynpx.nodepad.controller;

import io.github.orizynpx.nodepad.app.ServiceRegistry;
import io.github.orizynpx.nodepad.model.Edge;
import io.github.orizynpx.nodepad.model.GraphModel;
import io.github.orizynpx.nodepad.service.ParserService;
import io.github.orizynpx.nodepad.service.TaskMutatorService;
import io.github.orizynpx.nodepad.view.EditorFactory;
import io.github.orizynpx.nodepad.view.GraphRenderer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;

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
    private GraphModel currentModel;

    public WorkshopController() {
        this.parserService = ServiceRegistry.getInstance().getParserService();
        this.taskMutatorService = ServiceRegistry.getInstance().getTaskMutatorService();
    }

    @FXML
    public void initialize() {
        // 1. Editor
        codeArea = EditorFactory.createCodeArea();
        codeArea.setWrapText(true);
        editorContainer.getChildren().add(new VirtualizedScrollPane<>(codeArea));

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

    private void updateGraph(String text) {
        if (text == null) return;
        currentModel = parserService.parse(text); // Store model for logic
        renderer.render(currentModel);
    }

    /**
     * Toggles task status.
     * If marking INCOMPLETE, it cascades to children to lock them.
     */
    private void toggleTask(String nodeId) {
        String currentText = codeArea.getText();

        // LOGIC MOVED TO SERVICE (Satisfies Source 10 & 18)
        String newText = taskMutatorService.toggleTaskStatus(currentText, currentModel, nodeId);

        if (!newText.equals(currentText)) {
            codeArea.replaceText(newText);
        }
    }

    /**
     * BFS to find all nodes downstream from the root.
     */
    private void collectDescendants(String rootId, Set<String> results) {
        // Build Adjacency List (Source -> Targets)
        Map<String, List<String>> adj = new HashMap<>();
        for (Edge e : currentModel.getEdges()) {
            adj.computeIfAbsent(e.getSourceId(), k -> new ArrayList<>()).add(e.getTargetId());
        }

        Queue<String> queue = new LinkedList<>();
        queue.add(rootId);

        while (!queue.isEmpty()) {
            String curr = queue.poll();
            if (adj.containsKey(curr)) {
                for (String child : adj.get(curr)) {
                    if (!results.contains(child)) {
                        results.add(child);
                        queue.add(child);
                    }
                }
            }
        }
    }
}