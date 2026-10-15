package ua.library.model;

import ua.common.BaseEntity;
import ua.library.util.LibraryUtils;

public final class Book extends BaseEntity {
    private final String isbn;
    private final String title;
    private final Author author;
    private final String genre;
    private final int publishedYear;

    public Book(String isbn, String title, Author author, String genre, int publishedYear) {
        super();
        this.isbn = LibraryUtils.text(isbn, "ISBN");
        this.title = LibraryUtils.text(title, "Назва книги");
        LibraryUtils.notNull(author, "Автор книги");
        this.author = author;
        this.genre = LibraryUtils.genre(genre);
        this.publishedYear = LibraryUtils.publishedYear(publishedYear, author);
    }

    public String getIsbn() {
        return isbn;
    }

    public String getTitle() {
        return title;
    }

    public Author getAuthor() {
        return author;
    }

    public String getGenre() {
        return genre;
    }

    public int getPublishedYear() {
        return publishedYear;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        Book book = (Book) other;
        return isbn.equals(book.isbn);
    }

    @Override
    public int hashCode() {
        return isbn.hashCode();
    }

    @Override
    public String toString() {
        return "Книга{ISBN='" + isbn + "', назва='" + title + "', автор=" + author
                + ", жанр='" + LibraryUtils.genreName(genre) + "', рік видання=" + publishedYear
                + ", створено=" + createdAt + "}";
    }
}
