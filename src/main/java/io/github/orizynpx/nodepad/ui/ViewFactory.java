package io.github.orizynpx.nodepad.ui;

import io.github.orizynpx.nodepad.config.AppContainer;
import io.github.orizynpx.nodepad.ui.controller.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import java.io.IOException;

public class ViewFactory {

    private final AppContainer appContainer;

    public ViewFactory(AppContainer appContainer) {
        this.appContainer = appContainer;
    }

    public Parent loadMainView() {
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
        // Now injects both repositories needed by Inventory
        return loadFxml("/view/inventory.fxml", new InventoryController(
                appContainer.getMetadataRepository(),
                appContainer.getLinkRepository()
        ));
    }

    public WorkshopWrapper loadWorkshopWrapper() {
        WorkshopController controller = new WorkshopController(
                appContainer.getTextParser(),
                appContainer.getGraphLayoutEngine(),
                appContainer.getTaskMutatorService()
        );
        Parent view = loadFxml("/view/workshop.fxml", controller);
        return new WorkshopWrapper(view, controller);
    }

    public static class WorkshopWrapper {
        public final Parent view;
        public final WorkshopController controller;
        public WorkshopWrapper(Parent v, WorkshopController c) { view = v; controller = c; }
    }

    private Parent loadFxml(String path, Object controller) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            loader.setControllerFactory(param -> controller);
            return loader.load();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load FXML: " + path, e);
        }
    }
}