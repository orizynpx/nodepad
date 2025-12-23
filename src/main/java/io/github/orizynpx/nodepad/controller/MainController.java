package io.github.orizynpx.nodepad.controller;

import io.github.orizynpx.nodepad.app.Main;
import io.github.orizynpx.nodepad.app.ServiceRegistry;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;

import java.io.File;
import java.io.IOException;

public class MainController {
    @FXML private StackPane contentArea;
    @FXML private Button btnWorkshop;
    @FXML private Button btnInventory;

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

                // --- FIX: Use the Factory from Main ---
                loader.setControllerFactory(Main.getControllerFactory());

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
            openWorkshop(null);
        } else {
            contentArea.getChildren().setAll(workshopView);
        }
    }

    @FXML
    public void showInventory() {
        try {
            if (inventoryView == null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/inventory.fxml"));

                // --- FIX: Use the Factory from Main ---
                loader.setControllerFactory(Main.getControllerFactory());

                inventoryView = loader.load();
                inventoryController = loader.getController();
            }

            contentArea.getChildren().setAll(inventoryView);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void openWorkshop(File file) {
        try {
            if (workshopView == null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/workshop.fxml"));

                loader.setControllerFactory(Main.getControllerFactory());

                workshopView = loader.load();
                this.workshopController = loader.getController();
            }

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
}