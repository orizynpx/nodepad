package io.github.orizynpx.nodepad.controller;

import io.github.orizynpx.nodepad.dao.BookRepository;
import io.github.orizynpx.nodepad.dao.LinkRepository;
import io.github.orizynpx.nodepad.model.entity.BookMetadata;
import io.github.orizynpx.nodepad.model.entity.LinkMetadata;
import io.github.orizynpx.nodepad.model.entity.SharedProjectModel;
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

    public InventoryController(BookRepository bookRepo, LinkRepository linkRepo, SharedProjectModel projectModel) {
        this.bookRepository = bookRepo;
        this.linkRepository = linkRepo;
        this.projectModel = projectModel;
    }

    @FXML
    public void initialize() {
        scrollPane.setFitToWidth(true);
        tilePane.setPrefColumns(3);

        projectModel.getActiveIsbns().addListener((SetChangeListener<String>) change -> refresh());
        projectModel.getActiveUrls().addListener((SetChangeListener<String>) change -> refresh());

        refresh();
    }

    private void refresh() {
        tilePane.getChildren().clear();

        if (projectModel.getActiveIsbns().isEmpty() && projectModel.getActiveUrls().isEmpty()) {
            Label emptyLabel = new Label("Inventory Empty.\nAdd @isbn(..) or @url(..) to your nodes.");
            emptyLabel.getStyleClass().add("inventory-empty-label");
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

        for (String url : projectModel.getActiveUrls()) {
            Optional<LinkMetadata> linkOpt = linkRepository.findByUrl(url);
            if (linkOpt.isPresent()) {
                tilePane.getChildren().add(createLinkCard(linkOpt.get()));
            }
        }
    }

    private VBox createBookCard(BookMetadata book) {
        return createGenericCard(book.getTitle(), "ISBN: " + book.getIsbn(), book.getImageUrl(), true, false);
    }

    private VBox createLinkCard(LinkMetadata link) {
        return createGenericCard(link.getTitle(), link.getDescription(), link.getImageUrl(), false, true);
    }

    private VBox createGenericCard(String titleText, String subText, String imgUrl, boolean isPortrait, boolean isLink) {
        VBox card = new VBox(5);
        card.setAlignment(Pos.CENTER);
        card.setPrefSize(150, 220);

        // REPLACED INLINE STYLE
        card.getStyleClass().add("card");
        if (isLink) {
            card.getStyleClass().add("card-link");
        }

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
        title.getStyleClass().add("card-title");
        title.setWrapText(true);
        title.setMaxWidth(130);
        title.setMaxHeight(40);

        Label subtitle = new Label(subText);
        subtitle.getStyleClass().add("card-subtitle");
        subtitle.setWrapText(true);
        subtitle.setMaxWidth(130);
        subtitle.setMaxHeight(30);

        card.getChildren().addAll(imageView, title, subtitle);
        return card;
    }
}