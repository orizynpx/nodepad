package io.github.orizynpx.nodepad.domain.model;

public class BookMetadata {
    private String isbn;
    private String title;
    private String description;
    private String imageUrl;

    public BookMetadata(String isbn, String title, String description, String imageUrl) {
        this.isbn = isbn;
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
    }

    // Getters
    public String getIsbn() {
        return isbn;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}