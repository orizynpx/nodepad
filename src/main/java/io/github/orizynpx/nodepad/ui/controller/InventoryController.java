package io.github.orizynpx.nodepad.ui.controller;

import io.github.orizynpx.nodepad.domain.model.BookMetadata;
import io.github.orizynpx.nodepad.domain.port.MetadataRepository;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;

public class InventoryController {

    @FXML private ScrollPane scrollPane;
    @FXML private TilePane tilePane;

    private final MetadataRepository metadataRepo;

    public InventoryController(MetadataRepository metadataRepo) {
        this.metadataRepo = metadataRepo;
    }

    @FXML
    public void initialize() {
        scrollPane.setFitToWidth(true);
        tilePane.setPrefColumns(3);
        refresh();
    }

    public void refresh() {
        tilePane.getChildren().clear();
        for (BookMetadata book : metadataRepo.findAll()) {
            tilePane.getChildren().add(createBookCard(book));
        }
    }

    private VBox createBookCard(BookMetadata book) {
        VBox card = new VBox(5);
        card.setAlignment(Pos.CENTER);
        card.setStyle("-fx-background-color: #333; -fx-padding: 10; -fx-background-radius: 5; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.5), 5, 0, 0, 0);");
        card.setPrefSize(150, 220);

        ImageView imageView = new ImageView();
        imageView.setFitHeight(150);
        imageView.setFitWidth(100);
        imageView.setPreserveRatio(true);

        if (book.getImageUrl() != null && !book.getImageUrl().isEmpty()) {
            try {
                // Async image loading
                imageView.setImage(new Image(book.getImageUrl(), true));
            } catch (Exception ignored) {}
        }

        Label title = new Label(book.getTitle());
        title.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12;");
        title.setWrapText(true);
        title.setMaxWidth(130);

        card.getChildren().addAll(imageView, title);
        return card;
    }
}