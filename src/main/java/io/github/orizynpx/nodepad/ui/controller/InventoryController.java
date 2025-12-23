package io.github.orizynpx.nodepad.ui.controller;

import io.github.orizynpx.nodepad.app.BrowserUtil;
import io.github.orizynpx.nodepad.app.ProjectContext;
import io.github.orizynpx.nodepad.domain.model.BookMetadata;
import io.github.orizynpx.nodepad.domain.model.LinkMetadata;
import io.github.orizynpx.nodepad.domain.port.LinkRepository;
import io.github.orizynpx.nodepad.domain.port.MetadataRepository;
import io.github.orizynpx.nodepad.view.ImageCache;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;

import java.util.Optional;
import java.util.Set;

public class InventoryController {

    @FXML private ScrollPane scrollPane;
    @FXML private TilePane tilePane;

    private final MetadataRepository bookRepository;
    private final LinkRepository linkRepository;

    // Constructor Injection
    public InventoryController(MetadataRepository bookRepository, LinkRepository linkRepository) {
        this.bookRepository = bookRepository;
        this.linkRepository = linkRepository;
    }

    @FXML
    public void initialize() {
        scrollPane.setFitToWidth(true);
        tilePane.setPrefColumns(3);
        refresh();
    }

    public void refresh() {
        tilePane.getChildren().clear();

        Set<String> activeIsbns = ProjectContext.getInstance().getActiveIsbns();
        for (String rawIsbn : activeIsbns) {
            String cleanIsbn = rawIsbn.replaceAll("[^0-9]", "");
            Optional<BookMetadata> bookOpt = bookRepository.findByIsbn(cleanIsbn);
            if (bookOpt.isPresent()) {
                tilePane.getChildren().add(createBookCard(bookOpt.get()));
            }
        }

        Set<String> activeUrls = ProjectContext.getInstance().getActiveUrls();
        for (String url : activeUrls) {
            Optional<LinkMetadata> linkOpt = linkRepository.findByUrl(url);
            if (linkOpt.isPresent()) {
                tilePane.getChildren().add(createLinkCard(linkOpt.get()));
            }
        }

        if(tilePane.getChildren().isEmpty()) {
            Label empty = new Label("No items found. Add @isbn or @url to tasks.");
            empty.setStyle("-fx-text-fill: gray;");
            tilePane.getChildren().add(empty);
        }
    }

    // Reuse existing createBookCard/createLinkCard logic from your dump...
    private VBox createBookCard(BookMetadata book) {
        return createGenericCard(book.getTitle(), "ISBN: " + book.getIsbn(), book.getImageUrl(), true);
    }

    private VBox createLinkCard(LinkMetadata link) {
        VBox card = createGenericCard(link.getTitle(), link.getDescription(), link.getImageUrl(), false);
        card.setCursor(Cursor.HAND);
        card.setOnMouseClicked(e -> BrowserUtil.open(link.getUrl()));
        return card;
    }

    private VBox createGenericCard(String titleText, String subText, String imgUrl, boolean isPortrait) {
        VBox card = new VBox(5);
        card.setAlignment(Pos.CENTER);
        card.setStyle("-fx-background-color: #333; -fx-padding: 10; -fx-background-radius: 5;");
        card.setPrefSize(150, 220);

        ImageView imageView = new ImageView();
        if (imgUrl != null && !imgUrl.isEmpty()) {
            try { imageView.setImage(ImageCache.get(imgUrl)); } catch (Exception ignored) {}
        }
        imageView.setFitHeight(isPortrait ? 150 : 85);
        imageView.setFitWidth(isPortrait ? 100 : 140);
        imageView.setPreserveRatio(true);

        Label title = new Label(titleText);
        title.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        title.setWrapText(true);

        card.getChildren().addAll(imageView, title);
        return card;
    }
}