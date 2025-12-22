package io.github.orizynpx.nodepad.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class MainController {

    @FXML
    private BorderPane rootLayout;
    @FXML
    private StackPane contentArea;

    @FXML
    public void initialize() {
        showDashboard(); // Start on Dashboard now
    }

    @FXML
    private void showDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/dashboard.fxml"));
            Parent view = loader.load();

            // Pass reference to child so it can call openWorkshop
            DashboardController controller = loader.getController();
            controller.setMainController(this);

            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void showWorkshop() {
        // Default button click (resume current or new)
        openWorkshop(null);
    }

    @FXML
    private void showInventory() {
        loadView("inventory");
    }

    /**
     * Loads the workshop and populates it with file content.
     */
    public void openWorkshop(File file) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/workshop.fxml"));
            Parent view = loader.load();

            WorkshopController controller = loader.getController();

            if (file != null) {
                // Load file content
                try {
                    String content = Files.readString(file.toPath());
                    controller.loadContent(content); // We need to add this method next
                } catch (IOException e) {
                    System.err.println("Error reading file: " + e.getMessage());
                }
            } else {
                // Load Default/New Template
                // WorkshopController handles its own default in initialize,
                // but we can force a blank one here if needed.
            }

            contentArea.getChildren().setAll(view);
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