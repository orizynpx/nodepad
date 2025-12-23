package io.github.orizynpx.nodepad.controller;

import io.github.orizynpx.nodepad.app.BrowserUtil;
import io.github.orizynpx.nodepad.app.ProjectContext;
import io.github.orizynpx.nodepad.app.ServiceRegistry;
import io.github.orizynpx.nodepad.dao.BookRepository;
import io.github.orizynpx.nodepad.dao.LinkRepository; // Import this
import io.github.orizynpx.nodepad.model.BookMetadata;
import io.github.orizynpx.nodepad.model.LinkMetadata; // Import this
import io.github.orizynpx.nodepad.view.ImageCache;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;

import java.util.Optional;
import java.util.Set;

public class InventoryController {

    @FXML
    private ScrollPane scrollPane;
    @FXML
    private TilePane tilePane;

    private final BookRepository bookRepository;
    private final LinkRepository linkRepository; // 1. Add Repository

    public InventoryController() {
        this.bookRepository = ServiceRegistry.getInstance().getBookRepository();
        this.linkRepository = ServiceRegistry.getInstance().getLinkRepository(); // 2. Initialize it
    }

    @FXML
    public void initialize() {
        scrollPane.setFitToWidth(true);
        tilePane.setPrefColumns(3);
    }

    public void refresh() {
        tilePane.getChildren().clear();

        // --- 1. RENDER BOOKS ---
        Set<String> activeIsbns = ProjectContext.getInstance().getActiveIsbns();
        for (String rawIsbn : activeIsbns) {
            String cleanIsbn = rawIsbn.replaceAll("[^0-9]", "");
            Optional<BookMetadata> bookOpt = bookRepository.findByIsbn(cleanIsbn);
            if (bookOpt.isPresent()) {
                tilePane.getChildren().add(createBookCard(bookOpt.get()));
            }
        }

        // --- 2. RENDER LINKS (New Logic) ---
        Set<String> activeUrls = ProjectContext.getInstance().getActiveUrls();

        if (activeIsbns.isEmpty() && activeUrls.isEmpty()) {
            Label emptyLabel = new Label("Inventory Empty.\nAdd @isbn(..) or @url(..) to your nodes.");
            emptyLabel.setStyle("-fx-text-fill: #777; -fx-font-size: 14;");
            tilePane.getChildren().add(emptyLabel);
            return;
        }

        for (String url : activeUrls) {
            Optional<LinkMetadata> linkOpt = linkRepository.findByUrl(url);
            if (linkOpt.isPresent()) {
                tilePane.getChildren().add(createLinkCard(linkOpt.get()));
            } else {
                // Optional: Show placeholder if it hasn't finished fetching yet
            }
        }
    }

    private VBox createBookCard(BookMetadata book) {
        // ... (Keep your existing book card logic) ...
        return createGenericCard(book.getTitle(), "ISBN: " + book.getIsbn(), book.getImageUrl(), true);
    }

    // --- 3. CREATE LINK CARD ---
    private VBox createLinkCard(LinkMetadata link) {
        VBox card = createGenericCard(link.getTitle(), link.getDescription(), link.getImageUrl(), false);

        // 1. VISUAL CUE: Change cursor to Hand
        card.setCursor(Cursor.HAND);

        // 2. STYLE: Add a hover effect (Glow)
        card.setOnMouseEntered(e -> card.setStyle(card.getStyle() + "-fx-effect: dropshadow(three-pass-box, #00ffff, 10, 0, 0, 0);"));
        card.setOnMouseExited(e -> card.setStyle(card.getStyle().replace("-fx-effect: dropshadow(three-pass-box, #00ffff, 10, 0, 0, 0);", "")));

        // 3. ACTION: Open Browser
        card.setOnMouseClicked(e -> {
            System.out.println("Opening Link: " + link.getUrl());
            BrowserUtil.open(link.getUrl());
        });

        // Specific border for links
        card.setStyle(card.getStyle() + "-fx-border-color: #00ffff; -fx-border-width: 0 0 2 0;");
        return card;
    }

    // Helper to reduce code duplication between Books and Links
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
            // Website previews are usually landscape (16:9)
            imageView.setFitHeight(85);
            imageView.setFitWidth(140);
        }
        imageView.setPreserveRatio(true);

        if (imgUrl != null && !imgUrl.isEmpty()) {
            try {
                Image img = ImageCache.get(imgUrl);
                imageView.setImage(img);
            } catch (Exception ignored) { }
        }

        Label title = new Label(titleText);
        title.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12;");
        title.setWrapText(true);
        title.setMaxWidth(130);
        title.setMaxHeight(40); // Limit height

        Label subtitle = new Label(subText);
        subtitle.setStyle("-fx-text-fill: #aaa; -fx-font-size: 10;");
        subtitle.setWrapText(true);
        subtitle.setMaxWidth(130);
        subtitle.setMaxHeight(30);

        card.getChildren().addAll(imageView, title, subtitle);
        return card;
    }
}