package io.github.orizynpx.nodepad.controller;

import io.github.orizynpx.nodepad.dao.BookRepository;
import io.github.orizynpx.nodepad.dao.LinkRepository;
import io.github.orizynpx.nodepad.model.BookMetadata;
import io.github.orizynpx.nodepad.model.LinkMetadata;
import io.github.orizynpx.nodepad.model.SharedProjectModel;
import io.github.orizynpx.nodepad.view.ImageCache;
import javafx.collections.SetChangeListener;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;

import java.util.Optional;

public class InventoryController {

    @FXML
    private ScrollPane scrollPane;
    @FXML
    private TilePane tilePane;

    private final BookRepository bookRepository;
    private final LinkRepository linkRepository;
    private final SharedProjectModel projectModel;

    // Constructor Injection
    public InventoryController(BookRepository bookRepo, LinkRepository linkRepo, SharedProjectModel projectModel) {
        this.bookRepository = bookRepo;
        this.linkRepository = linkRepo;
        this.projectModel = projectModel;
    }

    @FXML
    public void initialize() {
        scrollPane.setFitToWidth(true);
        tilePane.setPrefColumns(3);

        // REACTIVE: Listen for changes in the shared model
        projectModel.getActiveIsbns().addListener((SetChangeListener<String>) change -> refresh());
        projectModel.getActiveUrls().addListener((SetChangeListener<String>) change -> refresh());

        // Initial render
        refresh();
    }

    // No longer public! It updates itself automatically.
    private void refresh() {
        tilePane.getChildren().clear();

        // 1. Render Books
        if (projectModel.getActiveIsbns().isEmpty() && projectModel.getActiveUrls().isEmpty()) {
            Label emptyLabel = new Label("Inventory Empty.\nAdd @isbn(..) or @url(..) to your nodes.");
            emptyLabel.setStyle("-fx-text-fill: #777; -fx-font-size: 14;");
            tilePane.getChildren().add(emptyLabel);
            return;
        }

        for (String rawIsbn : projectModel.getActiveIsbns()) {
            String cleanIsbn = rawIsbn.replaceAll("[^0-9]", "");
            Optional<BookMetadata> bookOpt = bookRepository.findByIsbn(cleanIsbn);
            if (bookOpt.isPresent()) {
                tilePane.getChildren().add(createBookCard(bookOpt.get()));
            }
        }

        // 2. Render Links
        for (String url : projectModel.getActiveUrls()) {
            Optional<LinkMetadata> linkOpt = linkRepository.findByUrl(url);
            if (linkOpt.isPresent()) {
                tilePane.getChildren().add(createLinkCard(linkOpt.get()));
            }
        }
    }

    private VBox createBookCard(BookMetadata book) {
        return createGenericCard(book.getTitle(), "ISBN: " + book.getIsbn(), book.getImageUrl(), true);
    }

    private VBox createLinkCard(LinkMetadata link) {
        VBox card = createGenericCard(link.getTitle(), link.getDescription(), link.getImageUrl(), false);
        card.setStyle(card.getStyle() + "-fx-border-color: #00ffff; -fx-border-width: 0 0 2 0;");
        return card;
    }

    private VBox createGenericCard(String titleText, String subText, String imgUrl, boolean isPortrait) {
        VBox card = new VBox(5);
        card.setAlignment(Pos.CENTER);
        card.setStyle("-fx-background-color: #333; -fx-padding: 10; -fx-background-radius: 5; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.5), 5, 0, 0, 0);");
        card.setPrefSize(150, 220);

        ImageView imageView = new ImageView();
        if (isPortrait) {
            imageView.setFitHeight(150);
            imageView.setFitWidth(100);
        } else {
            imageView.setFitHeight(85);
            imageView.setFitWidth(140);
        }
        imageView.setPreserveRatio(true);

        if (imgUrl != null && !imgUrl.isEmpty()) {
            try {
                Image img = ImageCache.get(imgUrl);
                imageView.setImage(img);
            } catch (Exception ignored) {
            }
        }

        Label title = new Label(titleText);
        title.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12;");
        title.setWrapText(true);
        title.setMaxWidth(130);
        title.setMaxHeight(40);

        Label subtitle = new Label(subText);
        subtitle.setStyle("-fx-text-fill: #aaa; -fx-font-size: 10;");
        subtitle.setWrapText(true);
        subtitle.setMaxWidth(130);
        subtitle.setMaxHeight(30);

        card.getChildren().addAll(imageView, title, subtitle);
        return card;
    }
}