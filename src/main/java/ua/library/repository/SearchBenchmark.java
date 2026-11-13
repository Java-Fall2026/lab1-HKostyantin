package ua.library.repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import ua.library.model.Author;


public final class SearchBenchmark {
    private static volatile int blackhole;
    private static final int SIZE = 100_000;
    private static final int WARMUPS = 5;
    private static final int REPEATS = 21;

    private SearchBenchmark() {
    }

    private static <E> Optional<E> linearSearch(
            List<E> items, String identity, IdentityExtractor<E> extractor) {
        for (E item : items) {
            if (identity.equals(extractor.identityOf(item))) {
                return Optional.of(item);
            }
        }
        return Optional.empty();
    }

    private static long median(long[] values) {
        Arrays.sort(values);
        return values[values.length / 2];
    }

    private static long measureMap(GenericRepository<Author> repo, String identity) {
        long[] times = new long[REPEATS];
        for (int i = 0; i < REPEATS; i++) {
            long started = System.nanoTime();
            Optional<Author> result = repo.findByIdentity(identity);
            times[i] = System.nanoTime() - started;
            blackhole += result.isPresent() ? 1 : 0;
        }
        return median(times);
    }

    private static long measureList(List<Author> items, String identity,
                                    IdentityExtractor<Author> extractor) {
        long[] times = new long[REPEATS];
        for (int i = 0; i < REPEATS; i++) {
            long started = System.nanoTime();
            Optional<Author> result = linearSearch(items, identity, extractor);
            times[i] = System.nanoTime() - started;
            blackhole += result.isPresent() ? 1 : 0;
        }
        return median(times);
    }

    public static void main(String[] args) {
        System.setProperty("logback.configurationFile", "src/main/resources/logback-benchmark.xml");
        IdentityExtractor<Author> extractor = author -> author.authorCode();
        GenericRepository<Author> repo = new GenericRepository<>("Benchmark authors", extractor);
        for (int i = 0; i < SIZE; i++) {
            repo.add(new Author("A-" + i, "Author " + i, 1900));
        }

        List<Author> list = repo.findAll();
        List<String> cases = List.of("A-0", "A-" + (SIZE - 1), "A-missing");
        String[] names = {"First", "Last", "Missing"};

        for (String identity : cases) {
            for (int i = 0; i < WARMUPS; i++) {
                blackhole += repo.findByIdentity(identity).isPresent() ? 1 : 0;
                blackhole += linearSearch(list, identity, extractor).isPresent() ? 1 : 0;
            }
        }

        System.out.println("Benchmark: 100000 authors, 5 warmups, median of 21 measurements");
        System.out.println("Scenario | Map (ns) | List (ns)");
        for (int i = 0; i < cases.size(); i++) {
            long mapTime = measureMap(repo, cases.get(i));
            long listTime = measureList(list, cases.get(i), extractor);
            System.out.printf("%s | %d | %d%n", names[i], mapTime, listTime);
        }
        if (blackhole < 0) {
            System.out.println("Lookup counter: " + blackhole);
        }
    }
}
