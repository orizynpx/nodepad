package io.github.orizynpx.nodepad.controller;

import io.github.orizynpx.nodepad.app.ServiceRegistry;
import io.github.orizynpx.nodepad.dao.BookRepository;
import io.github.orizynpx.nodepad.model.BookMetadata;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;

import java.util.List;

public class InventoryController {

    @FXML
    private ScrollPane scrollPane;
    @FXML
    private TilePane tilePane;

    private BookRepository bookRepository;

    public InventoryController() {
        this.bookRepository = ServiceRegistry.getInstance().getBookRepository();
    }

    @FXML
    public void initialize() {
        // Improve ScrollPane speed
        scrollPane.setFitToWidth(true);
        tilePane.setPrefColumns(3); // Responsive grid

        loadBooks();
    }

    private void loadBooks() {
        tilePane.getChildren().clear();
        List<BookMetadata> books = bookRepository.findAll();

        if (books.isEmpty()) {
            Label emptyLabel = new Label("No books collected yet.\nAdd @isbn(number) in your workshop!");
            emptyLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16;");
            tilePane.getChildren().add(emptyLabel);
            return;
        }

        for (BookMetadata book : books) {
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

        // Async loading for image to avoid UI freeze
        if (book.getImageUrl() != null && !book.getImageUrl().isEmpty()) {
            Image img = new Image(book.getImageUrl(), true); // true = background loading
            imageView.setImage(img);
        }

        Label title = new Label(book.getTitle());
        title.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12;");
        title.setWrapText(true);
        title.setMaxWidth(130);

        Label isbn = new Label(book.getIsbn());
        isbn.setStyle("-fx-text-fill: #aaa; -fx-font-size: 10;");

        card.getChildren().addAll(imageView, title, isbn);
        return card;
    }
}