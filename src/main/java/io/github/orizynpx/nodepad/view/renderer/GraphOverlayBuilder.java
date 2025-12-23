package io.github.orizynpx.nodepad.view.renderer;

import io.github.orizynpx.nodepad.model.entity.BookMetadata;
import io.github.orizynpx.nodepad.model.entity.LinkMetadata;
import io.github.orizynpx.nodepad.model.enums.NodeStatus;
import io.github.orizynpx.nodepad.model.graph.TaskNode;
import io.github.orizynpx.nodepad.service.LinkPreviewService;
import io.github.orizynpx.nodepad.service.OpenLibraryService;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;

public class GraphOverlayBuilder {

    private final Consumer<String> statusToggleAction;
    private final OpenLibraryService openLibraryService;
    private final LinkPreviewService linkPreviewService;

    public GraphOverlayBuilder(Consumer<String> statusToggleAction,
                               OpenLibraryService openLibraryService,
                               LinkPreviewService linkPreviewService) {
        this.statusToggleAction = statusToggleAction;
        this.openLibraryService = openLibraryService;
        this.linkPreviewService = linkPreviewService;
    }

    public Node createOverlay(TaskNode node, Runnable closeCallback) {
        VBox overlay = new VBox(8);
        overlay.setPadding(new Insets(10));
        overlay.getStyleClass().add("graph-overlay");
        overlay.setMinWidth(220);
        overlay.setMaxWidth(300);
        overlay.setCursor(Cursor.DEFAULT);

        Label title = new Label(node.getLabel());
        title.getStyleClass().add("overlay-title");
        title.setWrapText(true);
        overlay.getChildren().add(title);

        if (!node.getDescription().isEmpty()) {
            Label desc = new Label(node.getDescription());
            desc.getStyleClass().add("overlay-desc");
            desc.setWrapText(true);
            overlay.getChildren().add(desc);
        }

        if (node.getIsbn() != null) {
            Label loading = new Label("Fetching Book Data...");
            loading.getStyleClass().add("overlay-loading");
            overlay.getChildren().add(loading);

            openLibraryService.fetchBookInfo(node.getIsbn())
                    .thenAccept(book -> Platform.runLater(() -> {
                        overlay.getChildren().remove(loading);
                        if (book != null) {
                            overlay.getChildren().add(createBookEmbed(book));
                        } else {
                            Label err = new Label("Book not found.");
                            err.getStyleClass().add("overlay-error");
                            overlay.getChildren().add(err);
                        }
                    }));
        }

        if (node.getUrl() != null) {
            Label loading = new Label("Loading Preview...");
            loading.getStyleClass().add("overlay-loading-link");
            overlay.getChildren().add(loading);

            linkPreviewService.fetchPreview(node.getUrl())
                    .thenAccept(meta -> Platform.runLater(() -> {
                        overlay.getChildren().remove(loading);
                        if (meta != null) {
                            overlay.getChildren().add(createLinkEmbed(meta));
                        }
                    }));
        }

        Button btn = new Button();
        btn.setMaxWidth(Double.MAX_VALUE);

        if (node.getStatus() == NodeStatus.DONE) {
            btn.setText("Mark Incomplete");
            btn.getStyleClass().add("btn-mark-incomplete");
            btn.setOnAction(e -> {
                if (statusToggleAction != null) statusToggleAction.accept(node.getId());
                closeCallback.run();
            });
        } else if (node.getStatus() == NodeStatus.LOCKED) {
            btn.setText("Locked");
            btn.setDisable(true);
        } else {
            btn.setText("Mark Completed");
            btn.getStyleClass().add("btn-mark-complete");
            btn.setOnAction(e -> {
                if (statusToggleAction != null) statusToggleAction.accept(node.getId());
                closeCallback.run();
            });
        }
        overlay.getChildren().add(btn);

        return overlay;
    }

    private VBox createBookEmbed(BookMetadata book) {
        VBox embed = new VBox(5);
        embed.getStyleClass().add("embed-box-book");
        Label lbl = new Label("LIBRARY REFERENCE");
        lbl.getStyleClass().add("embed-label-ref");
        Label title = new Label(book.getTitle());
        title.getStyleClass().add("embed-title");
        title.setWrapText(true);
        Label isbn = new Label("ISBN: " + book.getIsbn());
        isbn.getStyleClass().add("embed-subtitle");
        embed.getChildren().addAll(lbl, title, isbn);
        if (book.getImageUrl() != null && !book.getImageUrl().isEmpty()) {
            try {
                ImageView img = new ImageView(new Image(book.getImageUrl(), true));
                img.setFitHeight(120);
                img.setFitWidth(80);
                img.setPreserveRatio(true);
                embed.getChildren().add(img);
            } catch (Exception ignored) {
            }
        }
        return embed;
    }

    private VBox createLinkEmbed(LinkMetadata meta) {
        VBox embed = new VBox(5);
        embed.getStyleClass().add("embed-box-link");
        Label title = new Label(meta.getTitle());
        title.getStyleClass().add("embed-link-title");
        Label desc = new Label(meta.getDescription());
        desc.getStyleClass().add("embed-link-desc");
        desc.setWrapText(true);
        desc.setMaxWidth(250);
        embed.getChildren().addAll(title, desc);
        if (meta.getImageUrl() != null && !meta.getImageUrl().isEmpty()) {
            try {
                ImageView img = new ImageView(new Image(meta.getImageUrl(), true));
                img.setFitHeight(100);
                img.setFitWidth(200);
                img.setPreserveRatio(true);
                embed.getChildren().add(img);
            } catch (Exception ignored) {
            }
        }
        return embed;
    }
}