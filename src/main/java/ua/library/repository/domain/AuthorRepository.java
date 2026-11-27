package ua.library.repository.domain;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.library.model.Author;
import ua.library.repository.GenericRepository;

public final class AuthorRepository extends GenericRepository<Author> {
    private static final Logger log = LoggerFactory.getLogger(AuthorRepository.class);

    public AuthorRepository() {
        super("Authors", Author::authorCode);
    }

    public List<Author> findSortedByName() {
        List<Author> result = findAllSorted(Comparator.comparing(Author::name)
                .thenComparing(Author::authorCode));
        log.info("Sort authors by name, then code: {} results", result.size());
        return result;
    }

    public List<Author> findSortedByBirthYearDescendingThenName() {
        Comparator<Author> comparator = Comparator.comparingInt(Author::birthYear)
                .reversed()
                .thenComparing(Author::name)
                .thenComparing(Author::authorCode);
        List<Author> result = findAllSorted(comparator);
        log.info("Sort authors by birth year DESC, name ASC, code: {} results", result.size());
        return result;
    }

    public List<Author> findByName(String fragment) {
        String query = normalized(fragment);
        List<Author> result = stream()
                .filter(author -> author.name().toLowerCase(Locale.ROOT).contains(query))
                .sorted()
                .toList();
        logSearch("name contains '" + fragment + "'", result.size());
        return result;
    }

    public List<Author> findByBirthYearRange(int from, int to) {
        if (from > to) throw new IllegalArgumentException("Invalid birth-year range");
        List<Author> result = stream()
                .filter(author -> author.birthYear() >= from && author.birthYear() <= to)
                .sorted()
                .toList();
        logSearch("birth year from " + from + " to " + to, result.size());
        return result;
    }

    public List<Author> findByNameAndBornSince(String fragment, int fromYear) {
        String query = normalized(fragment);
        List<Author> result = stream()
                .filter(author -> author.name().toLowerCase(Locale.ROOT).contains(query))
                .filter(author -> author.birthYear() >= fromYear)
                .sorted()
                .toList();
        logSearch("name contains '" + fragment + "' AND birth year >= " + fromYear,
                result.size());
        return result;
    }

    public Optional<Author> findFirstByExactName(String name) {
        String query = normalized(name);
        Optional<Author> result = stream()
                .filter(author -> author.name().toLowerCase(Locale.ROOT).equals(query))
                .sorted()
                .findFirst();
        logSearch("exact name '" + name + "'", result.isEmpty() ? 0 : 1);
        return result;
    }

    public Map<Integer, List<Author>> groupByBirthCentury() {
        Map<Integer, List<Author>> result = stream()
                .collect(Collectors.groupingBy(author -> (author.birthYear() - 1) / 100 + 1));
        log.info("Group authors by birth century: {} groups", result.size());
        return result;
    }

    public Map<Integer, Long> countByBirthCentury() {
        Map<Integer, Long> result = stream()
                .collect(Collectors.groupingBy(author -> (author.birthYear() - 1) / 100 + 1,
                        Collectors.counting()));
        log.info("Count authors by birth century: {} groups", result.size());
        return result;
    }

    private static String normalized(String text) {
        Objects.requireNonNull(text, "Search text must not be null");
        return text.trim().toLowerCase(Locale.ROOT);
    }

    private static void logSearch(String criterion, int count) {
        if (count == 0) log.warn("Author search '{}' returned 0 results", criterion);
        else log.info("Author search '{}': {} results", criterion, count);
    }
}
