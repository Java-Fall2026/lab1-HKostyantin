package ua.library.model;

import ua.library.util.LibraryUtils;

public record Book(String isbn, String title, Author author, Genre genre, int publishedYear) {
    public Book {
        isbn = LibraryUtils.text(isbn, "ISBN");
        title = LibraryUtils.text(title, "Book title");
        LibraryUtils.notNull(author, "Book author");
        LibraryUtils.notNull(genre, "Book genre");
        publishedYear = LibraryUtils.publishedYear(publishedYear, author);
    }
}
