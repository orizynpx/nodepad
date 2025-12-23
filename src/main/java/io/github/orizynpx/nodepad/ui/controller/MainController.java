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
    private final FileSystemRepository fileSystem;

    private Parent dashboardView;
    private Parent inventoryView;
    private Parent workshopView;
    private WorkshopController workshopController;

    // Constructor Injection (Called by ViewFactory)
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
    public void showDashboard() {
        if (dashboardView == null) {
            dashboardView = viewFactory.loadDashboardView(this);
        }
        contentArea.getChildren().setAll(dashboardView);
    }

    @FXML
    public void showWorkshop() {
        if (workshopView != null) {
            contentArea.getChildren().setAll(workshopView);
        } else {
            openWorkshop(null); // Fallback
        }
    }

    @FXML
    public void showInventory() {
        if (inventoryView == null) {
            inventoryView = viewFactory.loadInventoryView();
        } else {
            // If you have a refresh method in InventoryController, cast and call it here
            // ((InventoryController)inventoryView.getUserData()).refresh();
            // Note: This requires managing controller references better, but rebuilding view works for now:
            inventoryView = viewFactory.loadInventoryView();
        }
        contentArea.getChildren().setAll(inventoryView);
    }

    public void openWorkshop(File file) {
        try {
            // FIX: Use Factory to ensure Dependencies are injected
            ViewFactory.WorkshopWrapper wrapper = viewFactory.loadWorkshopWrapper();
            this.workshopView = wrapper.view;
            this.workshopController = wrapper.controller;

            btnWorkshop.setDisable(false);
            btnInventory.setDisable(false);

            if (file != null) {
                String content = fileSystem.readFile(file);
                workshopController.loadContent(content);
                workshopController.setCurrentFile(file);
            } else {
                workshopController.setCurrentFile(null);
            }

            contentArea.getChildren().setAll(workshopView);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}