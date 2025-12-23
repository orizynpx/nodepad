package io.github.orizynpx.nodepad.ui;

import io.github.orizynpx.nodepad.config.AppContainer;
import io.github.orizynpx.nodepad.ui.controller.DashboardController;
import io.github.orizynpx.nodepad.ui.controller.InventoryController;
import io.github.orizynpx.nodepad.ui.controller.MainController;
import io.github.orizynpx.nodepad.ui.controller.WorkshopController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import java.io.IOException;

public class ViewFactory {

    private final AppContainer appContainer;

    public ViewFactory(AppContainer appContainer) {
        this.appContainer = appContainer;
    }

    public Parent loadMainView() {
        // Inject FileSystemRepository into MainController so it can open files
        return loadFxml("/view/main-view.fxml", new MainController(
                this,
                appContainer.getFileSystemRepository()
        ));
    }

    public Parent loadDashboardView(MainController mainNav) {
        return loadFxml("/view/dashboard.fxml", new DashboardController(
                mainNav,
                appContainer.getFileRepository()
        ));
    }

    public Parent loadInventoryView() {
        return loadFxml("/view/inventory.fxml", new InventoryController(
                appContainer.getMetadataRepository()
        ));
    }

    // --- Helper for Workshop (Need both View and Controller) ---
    public static class WorkshopWrapper {
        public final Parent view;
        public final WorkshopController controller;
        public WorkshopWrapper(Parent v, WorkshopController c) { view = v; controller = c; }
    }

    public WorkshopWrapper loadWorkshopWrapper() {
        // INJECT ALL 3 SERVICES HERE
        WorkshopController controller = new WorkshopController(
                appContainer.getTextParser(),
                appContainer.getGraphLayoutEngine(),
                appContainer.getTaskMutatorService()
        );
        Parent view = loadFxml("/view/workshop.fxml", controller);
        return new WorkshopWrapper(view, controller);
    }

    // --- Core Loader ---
    private Parent loadFxml(String path, Object controller) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            loader.setControllerFactory(param -> controller); // DI Magic
            return loader.load();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load FXML: " + path, e);
        }
    }
}