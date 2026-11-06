package ua.library;

import java.nio.file.Path;
import java.nio.file.Files;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.time.format.DateTimeParseException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ua.library.exception.InvalidDataException;
import ua.library.io.LibraryCsvLoader;
import ua.library.model.Author;
import ua.library.model.Book;
import ua.library.model.BookLoan;
import ua.library.model.Genre;
import ua.library.model.LoanStatus;
import ua.library.model.Reader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LibraryLab3Test {
    private BookLoan makeLoan() {
        Author author = new Author("A-1", "Test Author", 1900);
        Book book = new Book("ISBN-1", "Test Book", author, Genre.FICTION, 2000);
        Reader reader = new Reader("R-1", "Test Reader", LocalDate.of(2000, 1, 1));
        return BookLoan.of(book, reader, LocalDate.of(2020, 1, 1),
                LocalDate.of(2020, 1, 15), null, LoanStatus.ACTIVE);
    }

    @Test
    @DisplayName("Create a valid author from a CSV row")
    void validAuthorRow() throws InvalidDataException {
        Author author = LibraryCsvLoader.parseAuthor(" A-1 , Test Author , 1900 ", 2);
        assertEquals("A-1", author.authorCode());
        assertEquals("Test Author", author.name());
        assertEquals(1900, author.birthYear());
    }

    @Test
    @DisplayName("Create a valid reader from a CSV row")
    void validReaderRow() throws InvalidDataException {
        Reader reader = LibraryCsvLoader.parseReader("R-1,Test Reader,2000-01-01", 2);
        assertEquals("R-1", reader.readerTicketNumber());
        assertEquals(LocalDate.of(2000, 1, 1), reader.birthDate());
    }

    @Test
    @DisplayName("Reject an author row with missing columns")
    void invalidAuthorColumnCount() {
        InvalidDataException error = assertThrows(InvalidDataException.class,
                () -> LibraryCsvLoader.parseAuthor("A-1,Test Author", 4));
        assertTrue(error.getMessage().contains("Line 4"));
    }

    @Test
    @DisplayName("Preserve the cause of an invalid author birth year")
    void invalidAuthorNumber() {
        InvalidDataException error = assertThrows(InvalidDataException.class,
                () -> LibraryCsvLoader.parseAuthor("A-1,Test Author,wrong", 7));
        assertTrue(error.getMessage().contains("Line 7"));
        assertNotNull(error.getCause());
        assertTrue(error.getCause() instanceof NumberFormatException);
    }

    @Test
    @DisplayName("Reject a birth year that violates the author record rules")
    void invalidAuthorDomainRule() {
        InvalidDataException error = assertThrows(InvalidDataException.class,
                () -> LibraryCsvLoader.parseAuthor("A-1,Test Author,1700", 8));
        assertTrue(error.getMessage().contains("Line 8"));
        assertTrue(error.getCause() instanceof IllegalArgumentException);
    }

    @Test
    @DisplayName("Preserve the cause of an invalid reader date")
    void invalidReaderDate() {
        InvalidDataException error = assertThrows(InvalidDataException.class,
                () -> LibraryCsvLoader.parseReader("R-1,Test Reader,2000-99-99", 6));
        assertTrue(error.getMessage().contains("Line 6"));
        assertTrue(error.getCause() instanceof DateTimeParseException);
    }

    @Test
    @DisplayName("Reject a reader younger than the minimum age")
    void invalidReaderAge() {
        String row = "R-1,Test Reader," + LocalDate.now().minusYears(10);
        InvalidDataException error = assertThrows(InvalidDataException.class,
                () -> LibraryCsvLoader.parseReader(row, 9));
        assertTrue(error.getMessage().contains("Line 9"));
        assertTrue(error.getCause() instanceof IllegalArgumentException);
    }

    @Test
    @DisplayName("Calculate the loan duration in days")
    void loanDays() {
        assertEquals(14, makeLoan().loanDays());
    }

    @Test
    @DisplayName("Calculate overdue days after returning a book")
    void overdueDays() {
        BookLoan loan = makeLoan();
        loan.setReturnDate(LocalDate.of(2020, 1, 18));
        assertEquals(3, loan.overdueDays());
    }

    @Test
    @DisplayName("An active loan has no overdue return days")
    void notYetReturned() {
        assertEquals(0, makeLoan().overdueDays());
    }

    @Test
    @DisplayName("Reject a return date earlier than the issue date")
    void invalidReturnDate() {
        BookLoan loan = makeLoan();
        assertThrows(IllegalArgumentException.class,
                () -> loan.setReturnDate(LocalDate.of(2019, 12, 31)));
    }

    @Test
    @DisplayName("Continue loading after an invalid CSV row")
    void skipBadRowAndContinue(@TempDir Path testDirectory) throws IOException {
        Path testFile = testDirectory.resolve("authors_lab3_test.csv");
        Files.writeString(testFile,
                "authorCode,name,birthYear\n" +
                "A-1,First Author,1900\n" +
                "A-2,Bad Author,invalid-year\n" +
                "A-3,Second Author,1950\n");

        List<Author> authors = LibraryCsvLoader.loadAuthors(testFile);
        assertEquals(2, authors.size());
        assertEquals("A-3", authors.get(1).authorCode());
    }

    @Test
    @DisplayName("Return an empty list when the CSV file is missing")
    void missingFile(@TempDir Path testDirectory) {
        List<Author> authors = LibraryCsvLoader.loadAuthors(
                testDirectory.resolve("file_that_does_not_exist_lab3_test.csv"));
        assertTrue(authors.isEmpty());
    }

    @Test
    @DisplayName("Reject additional CSV columns")
    void extraCsvColumn() {
        assertThrows(InvalidDataException.class,
                () -> LibraryCsvLoader.parseReader("R-1,Test Reader,2000-01-01,extra", 10));
    }
}
