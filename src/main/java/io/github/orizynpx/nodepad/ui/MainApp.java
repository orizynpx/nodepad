package io.github.orizynpx.nodepad.ui;

import io.github.orizynpx.nodepad.config.AppConfig;
import io.github.orizynpx.nodepad.config.AppContainer;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) {
        // 1. Initialize Logic & Wiring
        AppContainer container = AppContainer.getInstance();
        ViewFactory viewFactory = new ViewFactory(container);

        // 2. Load Main Layout via Factory
        Parent root = viewFactory.loadMainView();

        // 3. Setup Scene
        Scene scene = new Scene(root, AppConfig.WIDTH, AppConfig.HEIGHT);

        // Load CSS (Assuming they are in resources/css/)
        try {
            scene.getStylesheets().add(getClass().getResource("/css/theme.css").toExternalForm());
            scene.getStylesheets().add(getClass().getResource("/css/editor.css").toExternalForm());
        } catch (Exception e) {
            System.err.println("CSS not found, running without styles.");
        }

        stage.setTitle(AppConfig.APP_TITLE);
        stage.setScene(scene);
        stage.show();
    }
}