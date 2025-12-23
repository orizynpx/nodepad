package io.github.orizynpx.nodepad.controller;

import io.github.orizynpx.nodepad.dao.FileRepository;
import io.github.orizynpx.nodepad.model.entity.FileRecord;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class DashboardController {

    @FXML private ListView<FileRecord> recentFilesList;

    private MainController mainController;
    private final FileRepository fileRepository;

    public DashboardController(FileRepository fileRepo) {
        this.fileRepository = fileRepo;
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    public void initialize() {
        // 1. Custom Cell Factory with Context Menu
        recentFilesList.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(FileRecord item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().removeAll("recent-file-cell-empty", "recent-file-cell-filled");

                if (empty || item == null) {
                    setText(null);
                    setContextMenu(null); // Clear menu for empty cells
                    getStyleClass().add("recent-file-cell-empty");
                } else {
                    setText(item.getFilePath());
                    getStyleClass().add("recent-file-cell-filled");

                    // --- CONTEXT MENU FOR CRUD ---
                    ContextMenu menu = new ContextMenu();

                    MenuItem openItem = new MenuItem("Open");
                    openItem.setOnAction(e -> openFile(new File(item.getFilePath())));

                    MenuItem renameItem = new MenuItem("Rename File...");
                    renameItem.setOnAction(e -> handleRename(item));

                    MenuItem removeItem = new MenuItem("Remove from List");
                    removeItem.setOnAction(e -> handleRemoveRecord(item));

                    MenuItem deleteItem = new MenuItem("Delete from Disk");
                    deleteItem.setStyle("-fx-text-fill: red;");
                    deleteItem.setOnAction(e -> handleDeleteFile(item));

                    menu.getItems().addAll(openItem, new SeparatorMenuItem(), renameItem, removeItem, deleteItem);
                    setContextMenu(menu);
                }
            }
        });

        // 2. Single Click to Open (Check for PRIMARY button to allow Right-Click for context menu)
        recentFilesList.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 1) {
                FileRecord selected = recentFilesList.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    openFile(new File(selected.getFilePath()));
                }
            }
        });

        loadRecents();
    }

    private void handleRename(FileRecord item) {
        File oldFile = new File(item.getFilePath());

        TextInputDialog dialog = new TextInputDialog(oldFile.getName());
        dialog.setTitle("Rename File");
        dialog.setHeaderText("Renaming: " + oldFile.getName());
        dialog.setContentText("New Name:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newName -> {
            if (newName.isBlank()) return;

            // Ensure extension is kept or added if user forgot
            if (!newName.endsWith(".txt") && oldFile.getName().endsWith(".txt")) {
                newName += ".txt";
            }

            File newFile = new File(oldFile.getParent(), newName);

            // 1. Rename on Disk
            boolean success = oldFile.renameTo(newFile);

            if (success) {
                // 2. Update DB
                fileRepository.updateFilePath(oldFile.getAbsolutePath(), newFile.getAbsolutePath());
                refresh();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Could not rename file. It may be open or locked.");
                alert.show();
            }
        });
    }

    private void handleRemoveRecord(FileRecord item) {
        // Just remove from the database list, don't touch the file
        fileRepository.removeFileRecord(item.getFilePath());
        refresh();
    }

    private void handleDeleteFile(FileRecord item) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Are you sure you want to delete this file permanently?\n" + item.getFilePath(),
                ButtonType.YES, ButtonType.NO);

        alert.setTitle("Delete File");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    // 1. Delete from Disk
                    Files.deleteIfExists(Path.of(item.getFilePath()));
                    // 2. Remove from DB
                    fileRepository.removeFileRecord(item.getFilePath());
                    refresh();
                } catch (IOException e) {
                    new Alert(Alert.AlertType.ERROR, "Failed to delete file: " + e.getMessage()).show();
                }
            }
        });
    }

    private void loadRecents() {
        recentFilesList.getItems().setAll(fileRepository.getRecentFiles());
    }

    // ... (Keep handleNewFile, handleOpenFile, openFile logic same as before) ...
    @FXML private void handleNewFile() { if (mainController != null) mainController.openWorkshop(null); }
    @FXML private void handleOpenFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
        File file = fileChooser.showOpenDialog(recentFilesList.getScene().getWindow());
        if (file != null) openFile(file);
    }

    private void openFile(File file) {
        if (!file.exists()) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "File not found: " + file.getAbsolutePath());
            alert.showAndWait();
            // Optional: Auto-remove dead link
            fileRepository.removeFileRecord(file.getAbsolutePath());
            refresh();
            return;
        }
        fileRepository.addOrUpdateFile(file.getAbsolutePath());
        if (mainController != null) mainController.openWorkshop(file);
    }

    public void refresh() {
        loadRecents();
    }
}