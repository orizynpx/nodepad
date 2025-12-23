package io.github.orizynpx.nodepad.ui.controller;

import io.github.orizynpx.nodepad.domain.port.FileSystemRepository;
import io.github.orizynpx.nodepad.ui.ViewFactory;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;

import java.io.File;
import java.io.IOException;

public class MainController {
    @FXML private StackPane contentArea;
    @FXML private Button btnWorkshop;
    @FXML private Button btnInventory;

    private final ViewFactory viewFactory;
    private final FileSystemRepository fileSystem; // Needed to read files

    private Parent dashboardView;
    private Parent inventoryView;

    // Track active workshop
    private Parent workshopView;
    private WorkshopController workshopController;

    // Updated Constructor
    public MainController(ViewFactory viewFactory, FileSystemRepository fileSystem) {
        this.viewFactory = viewFactory;
        this.fileSystem = fileSystem;
    }

    @FXML
    public void initialize() {
        btnWorkshop.setDisable(true);
        btnInventory.setDisable(true);
        showDashboard();
    }

    @FXML
    private void showDashboard() {
        if (dashboardView == null) {
            dashboardView = viewFactory.loadDashboardView(this);
        }
        contentArea.getChildren().setAll(dashboardView);
    }

    @FXML
    private void showWorkshop() {
        if (workshopView != null) {
            contentArea.getChildren().setAll(workshopView);
        }
    }

    @FXML
    private void showInventory() {
        if (inventoryView == null) {
            inventoryView = viewFactory.loadInventoryView();
        }
        // Ideally call inventoryController.refresh() here if you kept the reference
        contentArea.getChildren().setAll(inventoryView);
    }

    /**
     * Called by Dashboard to open/create a project
     */
    public void openWorkshop(File file) {
        try {
            // 1. USE FACTORY (Fixes the crash)
            ViewFactory.WorkshopWrapper wrapper = viewFactory.loadWorkshopWrapper();
            this.workshopView = wrapper.view;
            this.workshopController = wrapper.controller;

            // 2. Enable Nav
            btnWorkshop.setDisable(false);
            btnInventory.setDisable(false);

            // 3. Load Content
            if (file != null) {
                // Use the injected repository to read the file
                String content = fileSystem.readFile(file);
                workshopController.loadContent(content);
            } else {
                // New file logic is handled by WorkshopController default
            }

            // 4. Show it
            contentArea.getChildren().setAll(workshopView);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}