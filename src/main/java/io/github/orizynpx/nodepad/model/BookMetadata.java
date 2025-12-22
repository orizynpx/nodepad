package io.github.orizynpx.nodepad.model;

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

    public BookMetadata(String isbn) {
        this(isbn, "Loading...", "Fetching data from OpenLibrary...", "");
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
