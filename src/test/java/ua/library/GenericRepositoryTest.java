
package ua.library;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ua.library.io.LibraryCsvLoader;
import ua.library.model.Author;
import ua.library.model.Book;
import ua.library.model.BookLoan;
import ua.library.model.Genre;
import ua.library.model.LoanStatus;
import ua.library.model.Reader;
import ua.library.repository.DuplicateIdentityException;
import ua.library.repository.GenericRepository;
import ua.library.repository.IdentityExtractor;

import static org.junit.jupiter.api.Assertions.*;

class GenericRepositoryTest {

    private GenericRepository<Author> newAuthors() {
        return new GenericRepository<>("Authors", author -> author.authorCode());
    }

    private Author newAuthor() {
        return new Author("A-1", "Test Author", 1900);
    }

    private Book newBook() {
        return new Book("ISBN-1", "Test Book", newAuthor(), Genre.FICTION, 2000);
    }

    @Test
    @DisplayName("Add and find an existing identity")
    void addAndFind() {
        GenericRepository<Author> repo = newAuthors();
        Author author = newAuthor();
        repo.add(author);
        assertEquals(Optional.of(author), repo.findByIdentity("A-1"));
        assertEquals(1, repo.size());
    }

    @Test
    @DisplayName("Return an empty Optional when identity is missing")
    void missingIdentity() {
        assertTrue(newAuthors().findByIdentity("A-404").isEmpty());
    }

    @Test
    @DisplayName("Throw a custom unchecked exception on duplicate add")
    void duplicateAdd() {
        GenericRepository<Author> repo = newAuthors();
        repo.add(newAuthor());
        DuplicateIdentityException error = assertThrows(DuplicateIdentityException.class,
                () -> repo.add(new Author("A-1", "Another Name", 1901)));
        assertTrue(error.getMessage().contains("A-1"));
        assertTrue(error.getMessage().contains("Authors"));
        assertTrue(error instanceof IllegalStateException);
        assertEquals(1, repo.size());
        assertEquals("Test Author", repo.findByIdentity("A-1").orElseThrow().name());
    }

    @Test
    @DisplayName("Add if absent returns false without replacing a value")
    void addIfAbsentDoesNotReplace() {
        GenericRepository<Author> repo = newAuthors();
        Author author = newAuthor();
        repo.add(author);
        assertFalse(repo.addIfAbsent(author));
        assertFalse(repo.addIfAbsent(new Author("A-1", "Second", 1902)));
        assertEquals(1, repo.size());
        assertEquals(author, repo.findByIdentity("A-1").orElseThrow());
    }

    @Test
    @DisplayName("Remove existing and missing identities")
    void removeByIdentity() {
        GenericRepository<Author> repo = newAuthors();
        repo.add(newAuthor());
        assertTrue(repo.removeByIdentity("A-1"));
        assertFalse(repo.removeByIdentity("A-1"));
        assertFalse(repo.removeByIdentity("A-999"));
        assertEquals(0, repo.size());
    }

    @Test
    @DisplayName("Find all returns an immutable independent list")
    void findAllIsSafe() {
        GenericRepository<Author> repo = newAuthors();
        repo.add(newAuthor());
        List<Author> snapshot = repo.findAll();
        assertThrows(UnsupportedOperationException.class, snapshot::clear);
        assertThrows(UnsupportedOperationException.class,
                () -> snapshot.add(new Author("A-2", "Another", 1900)));
        assertEquals(1, repo.size());
        repo.add(new Author("A-2", "Another", 1900));
        assertEquals(1, snapshot.size());
    }

    @Test
    @DisplayName("One generic repository class supports Author and Book")
    void worksForDifferentRecords() {
        GenericRepository<Author> authors = newAuthors();
        GenericRepository<Book> books = new GenericRepository<>("Books", book -> book.isbn());
        authors.add(newAuthor());
        books.add(newBook());
        assertTrue(authors.findByIdentity("A-1").isPresent());
        assertEquals("Test Book", books.findByIdentity("ISBN-1").orElseThrow().title());
    }

    @Test
    @DisplayName("The repository also supports a mutable BookLoan class")
    void mutableLoanUsesStableIdentity() {
        Book book = newBook();
        Reader reader = Reader.of("R-1", "Test Reader", LocalDate.of(2000, 1, 1));
        BookLoan loan = BookLoan.of(book, reader, LocalDate.of(2020, 1, 1),
                LocalDate.of(2020, 1, 15), null, LoanStatus.ACTIVE);
        GenericRepository<BookLoan> loans = new GenericRepository<>("Loans",
                item -> item.getBook().isbn() + ":"
                        + item.getReader().readerTicketNumber() + ":" + item.getIssueDate());
        loans.add(loan);
        loan.setStatus(LoanStatus.RETURNED);
        loan.setReturnDate(LocalDate.of(2020, 1, 17));
        assertEquals(loan, loans.findByIdentity("ISBN-1:R-1:2020-01-01").orElseThrow());
    }

    @Test
    @DisplayName("Reject null items and blank identities")
    void rejectInvalidItems() {
        GenericRepository<Author> repo = newAuthors();
        assertThrows(IllegalArgumentException.class, () -> repo.add(null));
        assertThrows(IllegalArgumentException.class, () -> repo.addIfAbsent(null));
        GenericRepository<Author> bad = new GenericRepository<>("Bad", a -> " ");
        assertThrows(IllegalArgumentException.class, () -> bad.add(newAuthor()));
        assertEquals(0, bad.size());
    }

    @Test
    @DisplayName("Reject invalid repository constructor arguments")
    void invalidRepositoryArguments() {
        assertThrows(IllegalArgumentException.class,
                () -> new GenericRepository<Author>(" ", a -> a.authorCode()));
        assertThrows(IllegalArgumentException.class,
                () -> new GenericRepository<Author>("Authors", null));
    }

    @Test
    @DisplayName("Add all accepts a collection without raw types")
    void addAll() {
        GenericRepository<Author> repo = newAuthors();
        List<Author> authors = List.of(newAuthor(), new Author("A-2", "Second", 1901));
        repo.addAll(authors);
        assertEquals(2, repo.size());
        assertEquals("A-1", repo.findAll().get(0).authorCode());
    }

    @Test
    @DisplayName("Real CSV-style duplicate is rejected by the repository")
    void duplicateFromCsv(@TempDir Path dir) throws Exception {
        Path csv = dir.resolve("authors.csv");
        Files.writeString(csv, "authorCode,name,birthYear\n"
                + "A-1,First,1900\n"
                + "A-1,Another,1901\n"
                + "A-2,Third,1902\n");
        GenericRepository<Author> repo = newAuthors();
        int duplicates = 0;
        for (Author author : LibraryCsvLoader.loadAuthors(csv)) {
            try {
                repo.add(author);
            } catch (DuplicateIdentityException e) {
                duplicates++;
            }
        }
        assertEquals(1, duplicates);
        assertEquals(2, repo.size());
        assertEquals("First", repo.findByIdentity("A-1").orElseThrow().name());
    }

    @Test
    @DisplayName("A passed lambda determines the identity field")
    void extractorIsPassedAsLambda() {
        IdentityExtractor<Book> extractor = value -> value.isbn();
        GenericRepository<Book> repo = new GenericRepository<>("Books", extractor);
        repo.add(newBook());
        assertTrue(repo.findByIdentity("ISBN-1").isPresent());
    }

    @Test
    @DisplayName("Reject a null collection and an invalid extractor result")
    void rejectBadCollectionAndExtractor() {
        GenericRepository<Author> repo = newAuthors();
        assertThrows(IllegalArgumentException.class, () -> repo.addAll(null));
        GenericRepository<Author> bad = new GenericRepository<>("Bad", a -> null);
        assertThrows(IllegalArgumentException.class, () -> bad.add(newAuthor()));
    }

    @Test
    @DisplayName("Reject a duplicate read from the project's actual authors CSV")
    void duplicateFromProjectCsv() {
        List<Author> parsed = LibraryCsvLoader.loadAuthors(Path.of("data", "authors.csv"));
        GenericRepository<Author> repo = newAuthors();
        int duplicateCount = 0;
        for (Author author : parsed) {
            try {
                repo.add(author);
            } catch (DuplicateIdentityException e) {
                duplicateCount++;
            }
        }
        assertEquals(1, duplicateCount);
        assertEquals(3, repo.size());
        assertEquals("Lesya Ukrainka", repo.findByIdentity("A-15").orElseThrow().name());
    }

    @Test
    @DisplayName("Add if absent inserts a new item")
    void addIfAbsentNewItem() {
        GenericRepository<Author> repo = newAuthors();
        assertTrue(repo.addIfAbsent(newAuthor()));
        assertEquals(1, repo.size());
    }

    @Test
    @DisplayName("Reject a blank extracted identity and preserve repository contents")
    void rejectBlankIdentity() {
        GenericRepository<Author> repo = new GenericRepository<>("Bad", a -> "\t");
        assertThrows(IllegalArgumentException.class, () -> repo.addIfAbsent(newAuthor()));
        assertEquals(0, repo.size());
    }
}
