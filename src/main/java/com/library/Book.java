package com.library;

/**
 * Book model for the fields used by the current add/search/issue/return flow.
 * The four-argument constructor remains available for existing DAO callers.
 */
public class Book {
    private int bookId;
    private String title;
    private String author;
    private int availableCopies;
    private String category;
    private byte[] coverImage;
    private String coverMimeType;
    private boolean hasCover;

    public Book(int bookId, String title, String author, int availableCopies) {
        this(bookId, title, author, availableCopies, null, null);
    }

    public Book(int bookId, String title, String author, int availableCopies,
                byte[] coverImage, String coverMimeType) {
        this.bookId = bookId;
        this.title = title;
        this.author = author;
        this.availableCopies = availableCopies;
        this.coverImage = coverImage;
        this.coverMimeType = coverMimeType;
        this.hasCover = coverImage != null && coverImage.length > 0;
    }

    public int getBookId() { return bookId; }
    public void setBookId(int bookId) { this.bookId = bookId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public int getAvailableCopies() { return availableCopies; }
    public void setAvailableCopies(int availableCopies) { this.availableCopies = availableCopies; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public byte[] getCoverImage() { return coverImage; }
    public void setCoverImage(byte[] coverImage) { this.coverImage = coverImage; }
    public String getCoverMimeType() { return coverMimeType; }
    public void setCoverMimeType(String coverMimeType) { this.coverMimeType = coverMimeType; }
    public boolean hasCover() { return hasCover || (coverImage != null && coverImage.length > 0); }
    public void setHasCover(boolean hasCover) { this.hasCover = hasCover; }
}
