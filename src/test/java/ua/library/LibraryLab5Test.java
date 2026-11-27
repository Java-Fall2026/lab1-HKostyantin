package ua.library;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ua.library.model.Author;
import ua.library.model.Book;
import ua.library.model.Genre;
import ua.library.repository.SortOrder;
import ua.library.repository.domain.AuthorRepository;
import ua.library.repository.domain.BookRepository;

import static org.junit.jupiter.api.Assertions.*;

class LibraryLab5Test {
    private static final Author LESYA = new Author("A-15", "Lesya", 1871);
    private static final Author FRANKO = new Author("A-16", "Ivan", 1856);
    private static final Author OTHER_LESYA = new Author("A-17", "Lesya", 1901);

    private static Book book(String isbn, String title, Author author, Genre genre,
                             int year, double price) {
        return new Book(isbn, title, author, genre, year, price);
    }

    private static BookRepository books() {
        BookRepository repo = new BookRepository();
        repo.add(book("B-3", "Forest Song", LESYA, Genre.FICTION, 1911, 100.75));
        repo.add(book("B-1", "History", FRANKO, Genre.HISTORY, 1905, 100.25));
        repo.add(book("B-2", "More Songs", LESYA, Genre.FICTION, 1910, 125.00));
        repo.add(book("B-4", "Science", FRANKO, Genre.SCIENCE, 1906, 90.00));
        return repo;
    }

    private static AuthorRepository authors() {
        AuthorRepository repo = new AuthorRepository();
        repo.add(LESYA);
        repo.add(FRANKO);
        repo.add(OTHER_LESYA);
        return repo;
    }

    @Test
    @DisplayName("Collections.sort uses Author natural order with tie breakers")
    void naturalAuthors() {
        List<Author> list = new ArrayList<>(List.of(OTHER_LESYA, LESYA, FRANKO));
        Collections.sort(list);
        assertEquals(List.of(FRANKO, LESYA, OTHER_LESYA), list);
    }

    @Test
    @DisplayName("Collections.sort uses fractional Book prices without subtracting")
    void naturalBooksFractional() {
        List<Book> list = new ArrayList<>(books().findAll());
        Collections.sort(list);
        assertEquals(List.of("B-4", "B-1", "B-3", "B-2"),
                list.stream().map(Book::isbn).toList());
        assertTrue(list.get(1).price() < list.get(2).price());
    }

    @Test
    @DisplayName("Author compareTo is consistent with record equals")
    void authorComparableEquality() {
        Author same = new Author("A-15", "Lesya", 1871);
        assertEquals(0, LESYA.compareTo(same));
        assertEquals(LESYA, same);
        assertNotEquals(0, LESYA.compareTo(OTHER_LESYA));
    }

    @Test
    @DisplayName("Book compareTo is zero exactly when record values are equal")
    void bookComparableEquality() {
        Book one = book("B-1", "Same", LESYA, Genre.FICTION, 1911, 10.5);
        Book same = book("B-1", "Same", LESYA, Genre.FICTION, 1911, 10.5);
        Book different = book("B-2", "Same", LESYA, Genre.FICTION, 1911, 10.5);
        assertEquals(0, one.compareTo(same));
        assertEquals(one, same);
        assertNotEquals(0, one.compareTo(different));
    }

    @Test
    @DisplayName("Fractional values less than one apart are not considered equal")
    void fractionalPriceComparison() {
        Book a = book("B-1", "A", FRANKO, Genre.HISTORY, 1905, 99.25);
        Book b = book("B-2", "B", FRANKO, Genre.HISTORY, 1905, 99.75);
        assertTrue(a.compareTo(b) < 0);
        assertEquals(List.of(a, b), List.of(b, a).stream().sorted().toList());
    }

    @Test
    @DisplayName("Identity ascending order is reverse of descending order")
    void identitySortDirections() {
        BookRepository repo = books();
        List<String> asc = repo.findAllSortedByIdentity(SortOrder.ASC).stream()
                .map(Book::isbn).toList();
        List<String> desc = repo.findAllSortedByIdentity(SortOrder.DESC).stream()
                .map(Book::isbn).toList();
        List<String> reversed = new ArrayList<>(asc);
        Collections.reverse(reversed);
        assertEquals(List.of("B-1", "B-2", "B-3", "B-4"), asc);
        assertEquals(reversed, desc);
        assertEquals(asc, repo.findAllSortedByIdentity(SortOrder.ASC).stream()
                .map(Book::isbn).toList());
    }

    @Test
    @DisplayName("Authors can also be sorted by identity in both directions")
    void authorIdentitySortDirections() {
        AuthorRepository repo = authors();
        assertEquals(List.of("A-15", "A-16", "A-17"),
                repo.findAllSortedByIdentity(SortOrder.ASC).stream().map(Author::authorCode).toList());
        assertEquals(List.of("A-17", "A-16", "A-15"),
                repo.findAllSortedByIdentity(SortOrder.DESC).stream().map(Author::authorCode).toList());
    }

    @Test
    @DisplayName("Multi-level comparator reverses years but leaves name tie break ascending")
    void multiLevelAuthorSort() {
        AuthorRepository repo = new AuthorRepository();
        repo.add(new Author("A-3", "Zoe", 1900));
        repo.add(new Author("A-1", "Anna", 1900));
        repo.add(new Author("A-2", "Old", 1890));
        assertEquals(List.of("Anna", "Zoe", "Old"), repo.findSortedByBirthYearDescendingThenName()
                .stream().map(Author::name).toList());
    }

    @Test
    @DisplayName("Books sort by genre then descending publication year")
    void multiLevelBookSort() {
        assertEquals(List.of("B-3", "B-2", "B-4", "B-1"),
                books().findSortedByGenreThenYearDescending().stream().map(Book::isbn).toList());
    }

    @Test
    @DisplayName("Domain sorters handle equal prices with deterministic ISBN tie breaker")
    void sortByPriceAndIsbn() {
        BookRepository repo = new BookRepository();
        repo.add(book("B-2", "B", LESYA, Genre.FICTION, 1901, 50.0));
        repo.add(book("B-1", "A", LESYA, Genre.FICTION, 1901, 50.0));
        assertEquals(List.of("B-1", "B-2"), repo.findSortedByPrice()
                .stream().map(Book::isbn).toList());
    }

    @Test
    @DisplayName("TreeSet using genre alone merges non-equal books")
    void treeSetComparatorInconsistentWithEquals() {
        Book first = book("1", "A", LESYA, Genre.FICTION, 1911, 10);
        Book second = book("2", "B", LESYA, Genre.FICTION, 1911, 20);
        assertNotEquals(first, second);
        TreeSet<Book> set = new TreeSet<>(Comparator.comparing(Book::genre));
        assertTrue(set.add(first));
        assertFalse(set.add(second));
        assertEquals(1, set.size());
    }

    @Test
    @DisplayName("Book title fragment search ignores letter case")
    void findByBookTitle() {
        assertEquals(List.of("B-3"), books().findByTitle("FOREST").stream()
                .map(Book::isbn).toList());
    }

    @Test
    @DisplayName("Book price search uses inclusive endpoints")
    void findBookPriceRange() {
        assertEquals(List.of("B-1", "B-3"), books().findByPriceRange(100.25, 100.75)
                .stream().map(Book::isbn).toList());
        assertThrows(IllegalArgumentException.class, () -> books().findByPriceRange(10, 1));
    }

    @Test
    @DisplayName("Book multi-criteria search rejects items matching only one criterion")
    void findBookByGenreAndYear() {
        assertEquals(List.of("B-3"), books().findByGenreAndPublishedSince(Genre.FICTION, 1911)
                .stream().map(Book::isbn).toList());
        assertTrue(books().findByGenreAndPublishedSince(Genre.HISTORY, 2000).isEmpty());
    }

    @Test
    @DisplayName("Author name search is case insensitive")
    void authorNameSearch() {
        assertEquals(2, authors().findByName("LESYA").size());
    }

    @Test
    @DisplayName("Author birth-year range search includes boundaries")
    void authorBirthYearRange() {
        assertEquals(2, authors().findByBirthYearRange(1856, 1871).size());
        assertTrue(authors().findByBirthYearRange(2000, 2010).isEmpty());
    }

    @Test
    @DisplayName("Author multi-criteria search requires both filters")
    void authorMultiCriteria() {
        assertEquals(List.of(OTHER_LESYA), authors().findByNameAndBornSince("lesya", 1890));
    }

    @Test
    @DisplayName("Optional is empty on a missing exact title or name")
    void optionalEmpty() {
        assertTrue(books().findFirstByExactTitle("missing").isEmpty());
        assertTrue(authors().findFirstByExactName("missing").isEmpty());
        assertEquals("Forest Song", books().findFirstByExactTitle("FOREST SONG")
                .map(Book::title).orElseThrow());
    }

    @Test
    @DisplayName("Filter map distinct sorted and limit produce distinct authors")
    void distinctAuthorsLimited() {
        assertEquals(List.of(LESYA), books().findDistinctAuthorsByGenre(Genre.FICTION, 1));
        assertEquals(1, books().findDistinctAuthorsByGenre(Genre.FICTION, 2).size());
        assertTrue(books().findDistinctAuthorsByGenre(Genre.FICTION, 0).isEmpty());
    }

    @Test
    @DisplayName("Group books by genre and count correctly")
    void groupingAndCounting() {
        BookRepository repo = books();
        Map<Genre, List<Book>> grouped = repo.groupByGenre();
        assertEquals(3, grouped.size());
        assertEquals(2, grouped.get(Genre.FICTION).size());
        assertEquals(2L, repo.countByGenre().get(Genre.FICTION).longValue());
    }

    @Test
    @DisplayName("Group and count authors by century")
    void authorGrouping() {
        AuthorRepository repo = authors();
        assertEquals(2, repo.groupByBirthCentury().size());
        assertEquals(2L, repo.countByBirthCentury().get(19).longValue());
        assertEquals(1L, repo.countByBirthCentury().get(20).longValue());
    }

    @Test
    @DisplayName("Aggregation on empty repository is safe")
    void emptyAggregation() {
        BookRepository repo = new BookRepository();
        assertEquals(0.0, repo.totalPrice());
        assertEquals(0L, repo.priceStatistics().getCount());
        assertEquals(0.0, repo.priceStatistics().getSum());
        assertTrue(repo.groupByGenre().isEmpty());
    }

    @Test
    @DisplayName("Flatten nested lists with flatMap")
    void flatMapNestedGroups() {
        assertEquals(List.of("Forest Song", "History", "More Songs", "Science"),
                books().flattenedGroupedTitles());
    }

    @Test
    @DisplayName("A consumed stream cannot be reused after a terminal operation")
    void streamSingleUse() {
        Stream<Book> stream = books().stream();
        assertEquals(4L, stream.count());
        assertThrows(IllegalStateException.class, stream::count);
    }

    @Test
    @DisplayName("findAll is an immutable snapshot unlike a newly created stream")
    void streamVsFindAll() {
        AuthorRepository repo = authors();
        List<Author> old = repo.findAll();
        repo.add(new Author("A-19", "New", 1980));
        assertEquals(3, old.size());
        assertThrows(UnsupportedOperationException.class, old::clear);
        assertEquals(4L, repo.stream().count());
    }

    @Test
    @DisplayName("Reject null sorting parameters")
    void nullSortRejected() {
        assertThrows(NullPointerException.class, () -> books().findAllSorted(null));
        assertThrows(NullPointerException.class, () -> books().findAllSortedByIdentity(null));
    }

    @Test
    @DisplayName("Reject NaN infinity and negative book prices")
    void validateBookPrice() {
        assertThrows(IllegalArgumentException.class,
                () -> book("b1", "Bad", LESYA, Genre.FICTION, 1911, Double.NaN));
        assertThrows(IllegalArgumentException.class,
                () -> book("b2", "Bad", LESYA, Genre.FICTION, 1911, Double.POSITIVE_INFINITY));
        assertThrows(IllegalArgumentException.class,
                () -> book("b3", "Bad", LESYA, Genre.FICTION, 1911, -1));
    }
}
