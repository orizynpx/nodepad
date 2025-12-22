package io.github.orizynpx.nodepad.controller;

import io.github.orizynpx.nodepad.app.ServiceRegistry;
import io.github.orizynpx.nodepad.dao.FileRepository;
import io.github.orizynpx.nodepad.model.FileRecord;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.stage.FileChooser;
import java.io.File;

public class DashboardController {

    @FXML private ListView<FileRecord> recentFilesList;

    private MainController mainController;

    private final FileRepository fileRepository;

    public DashboardController() {
        this.fileRepository = ServiceRegistry.getInstance().getFileRepository();
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    public void initialize() {
        // Custom Cell Factory to show just the filename, not the full object string
        recentFilesList.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(FileRecord item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    setText(item.getFilePath());
                    setStyle("-fx-text-fill: white; -fx-background-color: #252525; -fx-padding: 10; -fx-border-color: transparent transparent #333 transparent;");
                }
            }
        });

        // Double click to open
        recentFilesList.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2 && recentFilesList.getSelectionModel().getSelectedItem() != null) {
                openFile(new File(recentFilesList.getSelectionModel().getSelectedItem().getFilePath()));
            }
        });

        loadRecents();
    }

    private void loadRecents() {
        recentFilesList.getItems().setAll(fileRepository.getRecentFiles());
    }

    @FXML
    private void handleNewFile() {
        // Just switch to workshop with empty/default template
        if (mainController != null) {
            mainController.openWorkshop(null);
        }
    }

    @FXML
    private void handleOpenFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Nodepad Files", "*.npad", "*.txt"));
        File file = fileChooser.showOpenDialog(recentFilesList.getScene().getWindow());

        if (file != null) {
            openFile(file);
        }
    }

    private void openFile(File file) {
        // 1. Save to DB
        fileRepository.addOrUpdateFile(file.getAbsolutePath());

        // 2. Open in Workshop
        if (mainController != null) {
            mainController.openWorkshop(file);
        }
    }
}