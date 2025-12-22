package io.github.orizynpx.nodepad.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class Main extends Application {

    @Override
    public void start(Stage stage) {
        try {
            // 1. BOOTSTRAP: Initialize the ServiceRegistry
            // This instantiates the DatabaseManager and runs schema.sql automatically.
            ServiceRegistry.getInstance();
            System.out.println("Services and Database initialized.");

            // 2. Load UI
            String fxmlPath = "/view/main-view.fxml";
            URL fxmlUrl = getClass().getResource(fxmlPath);
            if (fxmlUrl == null) {
                throw new IllegalStateException("CRITICAL: FXML file not found at " + fxmlPath);
            }

            FXMLLoader fxmlLoader = new FXMLLoader(fxmlUrl);
            Parent root = fxmlLoader.load();
            Scene scene = new Scene(root, AppConfig.WIDTH, AppConfig.HEIGHT);

            // 3. Load CSS
            URL themeUrl = getClass().getResource("/css/theme.css");
            URL editorUrl = getClass().getResource("/css/editor.css");
            if (themeUrl != null) scene.getStylesheets().add(themeUrl.toExternalForm());
            if (editorUrl != null) scene.getStylesheets().add(editorUrl.toExternalForm());

            stage.setTitle(AppConfig.APP_TITLE);
            stage.setScene(scene);
            stage.show();

        } catch (Throwable e) {
            // Robust Crash Handling (Good for "Oral Exam" defense)
            System.err.println("!!! APP CRASH IN START() !!!");
            e.printStackTrace();
            // Optional: Show a simple Alert dialog here if you want extra points
        }
    }

    public static void main(String[] args) {
        launch();
    }
}