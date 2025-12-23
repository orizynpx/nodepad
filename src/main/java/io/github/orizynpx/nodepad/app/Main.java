package io.github.orizynpx.nodepad.app;

import io.github.orizynpx.nodepad.controller.DashboardController;
import io.github.orizynpx.nodepad.controller.InventoryController;
import io.github.orizynpx.nodepad.controller.WorkshopController;
import io.github.orizynpx.nodepad.model.SharedProjectModel;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.net.URL;

public class Main extends Application {

    // Singleton Scope for the Shared Model (Lifetime of the App)
    // We create it ONCE here and pass it down.
    private static final SharedProjectModel sharedModel = new SharedProjectModel();

    public static Callback<Class<?>, Object> getControllerFactory() {
        return type -> {
            try {
                if (type == InventoryController.class) {
                    return new InventoryController(
                            ServiceRegistry.getInstance().getBookRepository(),
                            ServiceRegistry.getInstance().getLinkRepository(),
                            sharedModel // Inject Shared Model
                    );
                }
                if (type == DashboardController.class) {
                    return new DashboardController(
                            ServiceRegistry.getInstance().getFileRepository()
                    );
                }
                if (type == WorkshopController.class) {
                    return new WorkshopController(
                            ServiceRegistry.getInstance().getParserService(),
                            ServiceRegistry.getInstance().getTaskMutatorService(),
                            ServiceRegistry.getInstance().getContentRepository(),
                            ServiceRegistry.getInstance().getFileRepository(),
                            sharedModel // Inject Shared Model
                    );
                }
                // Default
                return type.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new RuntimeException("DI Failure for " + type.getName(), e);
            }
        };
    }

    @Override
    public void start(Stage stage) {
        try {
            ServiceRegistry.getInstance(); // Init DB

            String fxmlPath = "/view/main-view.fxml";
            URL fxmlUrl = getClass().getResource(fxmlPath);
            if (fxmlUrl == null) throw new IllegalStateException("FXML not found");

            FXMLLoader fxmlLoader = new FXMLLoader(fxmlUrl);
            fxmlLoader.setControllerFactory(getControllerFactory());
            Parent root = fxmlLoader.load();

            Scene scene = new Scene(root, AppConfig.WIDTH, AppConfig.HEIGHT);

            URL themeUrl = getClass().getResource("/css/theme.css");
            URL editorUrl = getClass().getResource("/css/editor.css");

            if (themeUrl != null) {
                scene.getStylesheets().add(themeUrl.toExternalForm());
            }
            if (editorUrl != null) {
                scene.getStylesheets().add(editorUrl.toExternalForm());
            }

            stage.setTitle(AppConfig.APP_TITLE);
            stage.setScene(scene);
            stage.show();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch();
    }
}