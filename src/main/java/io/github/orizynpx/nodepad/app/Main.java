package io.github.orizynpx.nodepad.app;

import io.github.orizynpx.nodepad.dao.DatabaseManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        try {
            // 1. Init DB
            DatabaseManager.connect(); // Or getInstance() if you kept Singleton

            // 2. Debug: Print the path we are looking for
            String fxmlPath = "/view/main-view.fxml";
            System.out.println("Looking for FXML at: " + fxmlPath);

            URL fxmlUrl = getClass().getResource(fxmlPath);
            if (fxmlUrl == null) {
                throw new IllegalStateException("CRITICAL: FXML file not found at " + fxmlPath +
                        "\nCheck src/main/resources/io/github/orizynpx/nodepad/view/");
            }

            // 3. Load UI
            FXMLLoader fxmlLoader = new FXMLLoader(fxmlUrl);
            Parent root = fxmlLoader.load();
            Scene scene = new Scene(root, AppConfig.WIDTH, AppConfig.HEIGHT);

            // 4. Load CSS
            URL themeUrl = getClass().getResource("/css/theme.css");
            URL editorUrl = getClass().getResource("/css/editor.css");
            if (themeUrl != null) scene.getStylesheets().add(themeUrl.toExternalForm());
            if (editorUrl != null) scene.getStylesheets().add(editorUrl.toExternalForm());

            stage.setTitle(AppConfig.APP_TITLE);
            stage.setScene(scene);
            stage.show();

        } catch (Throwable e) {
            // CATCH ALL ERRORS and print them
            System.err.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            System.err.println("APP CRASHED IN START():");
            e.printStackTrace();
            System.err.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        }
    }

    public static void main(String[] args) {
        launch();
    }
}