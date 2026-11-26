package ua.library.model;

import ua.library.util.LibraryUtils;

public record Book(String isbn, String title, Author author, Genre genre, int publishedYear, double price) implements Comparable<Book> {
    public Book {
        isbn = LibraryUtils.text(isbn, "ISBN");
        title = LibraryUtils.text(title, "Book title");
        LibraryUtils.notNull(author, "Book author");
        LibraryUtils.notNull(genre, "Book genre");
        publishedYear = LibraryUtils.publishedYear(publishedYear, author);
        if (!Double.isFinite(price) || price < 0) {
            throw new IllegalArgumentException("Book price must be finite and non-negative");
        }
    }
    /**
     * lab5
     */

    public Book(String isbn, String title, Author author, Genre genre, int publishedYear) {
        this(isbn, title, author, genre, publishedYear, 0.0);
    }

    @Override
    public int compareTo(Book other) {
        int cmp = Double.compare(price, other.price);
        if (cmp != 0) return cmp;
        cmp = title.compareTo(other.title);
        if (cmp != 0) return cmp;
        cmp = isbn.compareTo(other.isbn);
        if (cmp != 0) return cmp;
        cmp = author.compareTo(other.author);
        if (cmp != 0) return cmp;
        cmp = genre.compareTo(other.genre);
        if (cmp != 0) return cmp;
        return Integer.compare(publishedYear, other.publishedYear);
    }
}
