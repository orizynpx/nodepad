package io.github.orizynpx.nodepad.view;

import io.github.orizynpx.nodepad.app.ServiceRegistry;
import io.github.orizynpx.nodepad.model.BookMetadata;
import io.github.orizynpx.nodepad.model.LinkMetadata;
import io.github.orizynpx.nodepad.model.NodeStatus;
import io.github.orizynpx.nodepad.model.TaskNode;
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

    public GraphOverlayBuilder(Consumer<String> statusToggleAction) {
        this.statusToggleAction = statusToggleAction;
    }

    public Node createOverlay(TaskNode node, Runnable closeCallback) {
        VBox overlay = new VBox(8);
        overlay.setPadding(new Insets(10));
        overlay.setStyle("-fx-background-color: rgba(30, 30, 30, 0.98); -fx-border-color: #00ffff; -fx-border-radius: 8; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, black, 10, 0, 0, 0);");
        overlay.setMinWidth(220);
        overlay.setMaxWidth(300);
        overlay.setCursor(Cursor.DEFAULT);

        Label title = new Label(node.getLabel());
        title.setStyle("-fx-text-fill: #00ffff; -fx-font-weight: bold; -fx-font-size: 14px;");
        title.setWrapText(true);
        overlay.getChildren().add(title);

        if (!node.getDescription().isEmpty()) {
            Label desc = new Label(node.getDescription());
            desc.setStyle("-fx-text-fill: #cccccc; -fx-font-size: 12px;");
            desc.setWrapText(true);
            overlay.getChildren().add(desc);
        }

        // --- Async Fetching Logic (Now Isolated) ---
        if (node.getIsbn() != null) {
            Label loading = new Label("Fetching Book Data...");
            loading.setStyle("-fx-text-fill: #ffd700; -fx-font-style: italic; -fx-font-size: 10px;");
            overlay.getChildren().add(loading);

            ServiceRegistry.getInstance().getOpenLibraryService()
                    .fetchBookInfo(node.getIsbn())
                    .thenAccept(book -> Platform.runLater(() -> {
                        overlay.getChildren().remove(loading);
                        if (book != null) {
                            overlay.getChildren().add(createBookEmbed(book));
                        } else {
                            Label err = new Label("Book not found.");
                            err.setStyle("-fx-text-fill: #ff5555; -fx-font-size: 10px;");
                            overlay.getChildren().add(err);
                        }
                    }));
        }

        if (node.getUrl() != null) {
            Label loading = new Label("Loading Preview...");
            loading.setStyle("-fx-text-fill: #aaa; -fx-font-style: italic;");
            overlay.getChildren().add(loading);

            ServiceRegistry.getInstance().getLinkPreviewService()
                    .fetchPreview(node.getUrl())
                    .thenAccept(meta -> Platform.runLater(() -> {
                        overlay.getChildren().remove(loading);
                        if (meta != null) {
                            overlay.getChildren().add(createLinkEmbed(meta));
                        }
                    }));
        }

        // --- Action Buttons ---
        Button btn = new Button();
        btn.setMaxWidth(Double.MAX_VALUE);

        if (node.getStatus() == NodeStatus.DONE) {
            btn.setText("Mark Incomplete");
            btn.setStyle("-fx-background-color: #442222; -fx-text-fill: #ff9999;");
            btn.setOnAction(e -> {
                if (statusToggleAction != null) statusToggleAction.accept(node.getId());
                closeCallback.run();
            });
        } else if (node.getStatus() == NodeStatus.LOCKED) {
            btn.setText("Locked");
            btn.setDisable(true);
        } else {
            btn.setText("Mark Completed");
            btn.setStyle("-fx-background-color: #224422; -fx-text-fill: #99ff99;");
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
        embed.setStyle("-fx-border-color: #ffd700; -fx-border-width: 0 0 0 3; -fx-padding: 0 0 0 8; -fx-background-color: rgba(255, 215, 0, 0.1);");

        Label lbl = new Label("LIBRARY REFERENCE");
        lbl.setStyle("-fx-text-fill: #ffd700; -fx-font-size: 8px; -fx-font-weight: bold;");

        Label title = new Label(book.getTitle());
        title.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px;");
        title.setWrapText(true);

        Label isbn = new Label("ISBN: " + book.getIsbn());
        isbn.setStyle("-fx-text-fill: #aaaaaa; -fx-font-size: 9px; -fx-font-family: 'Monospace';");

        embed.getChildren().addAll(lbl, title, isbn);

        if (book.getImageUrl() != null && !book.getImageUrl().isEmpty()) {
            try {
                ImageView img = new ImageView(new Image(book.getImageUrl(), true));
                img.setFitHeight(120);
                img.setFitWidth(80);
                img.setPreserveRatio(true);
                embed.getChildren().add(img);
            } catch (Exception ignored) { }
        }
        return embed;
    }

    private VBox createLinkEmbed(LinkMetadata meta) {
        VBox embed = new VBox(5);
        embed.setStyle("-fx-border-color: #00ffff; -fx-border-width: 0 0 0 3; -fx-padding: 0 0 0 8; -fx-background-color: rgba(0,0,0,0.2);");

        Label title = new Label(meta.getTitle());
        title.setStyle("-fx-text-fill: #00aaff; -fx-font-weight: bold; -fx-font-size: 11px;");

        Label desc = new Label(meta.getDescription());
        desc.setStyle("-fx-text-fill: #aaaaaa; -fx-font-size: 10px;");
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
            } catch (Exception ignored) { }
        }
        return embed;
    }
}