package io.github.orizynpx.nodepad.controller;

import io.github.orizynpx.nodepad.app.ServiceRegistry;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;

import java.io.File;
import java.io.IOException;

public class MainController {
    @FXML
    private StackPane contentArea;
    @FXML
    private Button btnWorkshop;
    @FXML
    private Button btnInventory;

    private Parent dashboardView;
    private Parent workshopView;
    private Parent inventoryView;

    private DashboardController dashboardController;
    private WorkshopController workshopController;
    private InventoryController inventoryController;

    @FXML
    public void initialize() {
        btnWorkshop.setDisable(true);
        btnInventory.setDisable(true);
        showDashboard();
    }

    @FXML
    public void showDashboard() {
        try {
            if (dashboardView == null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/dashboard.fxml"));
                dashboardView = loader.load();
                dashboardController = loader.getController();
                dashboardController.setMainController(this);
            }

            dashboardController.refresh();
            contentArea.getChildren().setAll(dashboardView);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void showWorkshop() {
        if (workshopView == null) {
            // First time loading (empty project)
            openWorkshop(null);
        } else {
            // Resume existing session
            contentArea.getChildren().setAll(workshopView);
        }
    }

    @FXML
    public void showInventory() {
        try {
            if (inventoryView == null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/inventory.fxml"));
                inventoryView = loader.load();
                inventoryController = loader.getController();
            }

            // CRITICAL: Tell inventory to reload based on current context
            inventoryController.refresh();

            contentArea.getChildren().setAll(inventoryView);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void openWorkshop(File file) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/workshop.fxml"));
            workshopView = loader.load();
            this.workshopController = loader.getController();

            btnWorkshop.setDisable(false);
            btnInventory.setDisable(false);

            if (file != null) {
                workshopController.setCurrentFile(file);
                String content = ServiceRegistry.getInstance().getContentRepository().loadContent(file);
                workshopController.loadContent(content);
            } else {
                workshopController.setCurrentFile(null);
            }

            contentArea.getChildren().setAll(workshopView);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadView(String fxmlName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/" + fxmlName + ".fxml"));
            contentArea.getChildren().setAll((Parent) loader.load());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}