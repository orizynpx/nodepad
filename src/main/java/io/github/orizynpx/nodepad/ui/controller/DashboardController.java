package io.github.orizynpx.nodepad.ui.controller;

import io.github.orizynpx.nodepad.domain.model.FileRecord;
import io.github.orizynpx.nodepad.domain.port.FileRepository;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.stage.FileChooser;
import java.io.File;

public class DashboardController {

    @FXML private ListView<FileRecord> recentFilesList;

    private final MainController mainNav;
    private final FileRepository fileRepository;

    // Constructor Injection
    public DashboardController(MainController mainNav, FileRepository fileRepository) {
        this.mainNav = mainNav;
        this.fileRepository = fileRepository;
    }

    @FXML
    public void initialize() {
        recentFilesList.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(FileRecord item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    setText(item.getFilePath());
                    setStyle("-fx-text-fill: white; -fx-background-color: #252525; -fx-padding: 10;");
                }
            }
        });

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
        mainNav.openWorkshop(null);
    }

    @FXML
    private void handleOpenFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
        File file = fileChooser.showOpenDialog(recentFilesList.getScene().getWindow());

        if (file != null) {
            openFile(file);
        }
    }

    private void openFile(File file) {
        fileRepository.addOrUpdateFile(file.getAbsolutePath());
        mainNav.openWorkshop(file);
    }
}