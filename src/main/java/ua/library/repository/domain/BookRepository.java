package ua.library.repository.domain;

import java.util.Comparator;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.library.model.Author;
import ua.library.model.Book;
import ua.library.model.Genre;
import ua.library.repository.GenericRepository;

public final class BookRepository extends GenericRepository<Book> {
    private static final Logger log = LoggerFactory.getLogger(BookRepository.class);

    public BookRepository() {
        super("Books", Book::isbn);
    }

    public List<Book> findSortedByPrice() {
        Comparator<Book> comparator = (left, right) -> Double.compare(left.price(), right.price());
        List<Book> result = findAllSorted(comparator.thenComparing(Book::isbn));
        log.info("Sort books by price ASC, ISBN ASC: {} results", result.size());
        return result;
    }

    public List<Book> findSortedByGenreThenYearDescending() {
        Comparator<Book> comparator = Comparator.comparing(Book::genre)
                .thenComparing(Comparator.comparingInt(Book::publishedYear).reversed())
                .thenComparing(Book::isbn);
        List<Book> result = findAllSorted(comparator);
        log.info("Sort books by genre ASC, year DESC, ISBN: {} results", result.size());
        return result;
    }

    public List<Book> findByTitle(String fragment) {
        String query = normalized(fragment);
        List<Book> result = stream()
                .filter(book -> book.title().toLowerCase(Locale.ROOT).contains(query))
                .sorted()
                .toList();
        logSearch("title contains '" + fragment + "'", result.size());
        return result;
    }

    public List<Book> findByPriceRange(double min, double max) {
        if (!Double.isFinite(min) || !Double.isFinite(max) || min > max) {
            throw new IllegalArgumentException("Invalid book price range");
        }
        List<Book> result = stream()
                .filter(book -> book.price() >= min && book.price() <= max)
                .sorted()
                .toList();
        logSearch("price from " + min + " to " + max, result.size());
        return result;
    }

    public List<Book> findByGenreAndPublishedSince(Genre genre, int fromYear) {
        Objects.requireNonNull(genre, "Genre must not be null");
        List<Book> result = stream()
                .filter(book -> book.genre() == genre && book.publishedYear() >= fromYear)
                .sorted()
                .toList();
        logSearch("genre=" + genre + " AND published >= " + fromYear, result.size());
        return result;
    }

    public Optional<Book> findFirstByExactTitle(String title) {
        String query = normalized(title);
        Optional<Book> result = stream()
                .filter(book -> book.title().toLowerCase(Locale.ROOT).equals(query))
                .sorted()
                .findFirst();
        logSearch("exact title '" + title + "'", result.isEmpty() ? 0 : 1);
        return result;
    }


    public List<Author> findDistinctAuthorsByGenre(Genre genre, int limit) {
        Objects.requireNonNull(genre, "Genre must not be null");
        if (limit < 0) throw new IllegalArgumentException("Limit must be non-negative");
        List<Author> result = stream()
                .filter(book -> book.genre() == genre)
                .map(Book::author)
                .distinct()
                .sorted(Comparator.comparing(Author::name).thenComparing(Author::authorCode))
                .limit(limit)
                .toList();
        logSearch("distinct authors for genre=" + genre + ", limit=" + limit,
                result.size());
        return result;
    }

    public Map<Genre, List<Book>> groupByGenre() {
        Map<Genre, List<Book>> result = stream().collect(Collectors.groupingBy(Book::genre));
        log.info("Group books by genre: {} groups", result.size());
        return result;
    }

    public Map<Genre, Long> countByGenre() {
        Map<Genre, Long> result = stream()
                .collect(Collectors.groupingBy(Book::genre, Collectors.counting()));
        log.info("Count books by genre: {} groups", result.size());
        return result;
    }

    public double totalPrice() {
        double total = stream().mapToDouble(Book::price).sum();
        log.info("Aggregate total book prices: {} entries, sum={}", size(), total);
        return total;
    }

    public DoubleSummaryStatistics priceStatistics() {
        DoubleSummaryStatistics stats = stream().collect(Collectors.summarizingDouble(Book::price));
        log.info("Aggregate book price statistics: {} entries", stats.getCount());
        return stats;
    }

    public List<String> flattenedGroupedTitles() {
        List<String> titles = groupByGenre().values().stream()
                .flatMap(List::stream)
                .map(Book::title)
                .sorted()
                .toList();
        log.info("Flatten grouped books into titles: {} results", titles.size());
        return titles;
    }

    private static String normalized(String text) {
        Objects.requireNonNull(text, "Search text must not be null");
        return text.trim().toLowerCase(Locale.ROOT);
    }

    private static void logSearch(String criterion, int count) {
        if (count == 0) log.warn("Book search '{}' returned 0 results", criterion);
        else log.info("Book search '{}': {} results", criterion, count);
    }
}
