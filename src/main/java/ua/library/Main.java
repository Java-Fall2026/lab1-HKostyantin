
package ua.library;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.nio.file.Path;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.library.exception.InvalidDataException;
import ua.library.io.LibraryCsvLoader;
import ua.library.model.Author;
import ua.library.model.Book;
import ua.library.model.BookLoan;
import ua.library.model.Genre;
import ua.library.model.LoanStatus;
import ua.library.model.Reader;
import ua.library.repository.DuplicateIdentityException;
import ua.library.repository.GenericRepository;
import ua.library.util.LibraryUtils;


public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    private static String yesOrNo(boolean value) {
        if (value) {
            return "так";
        }
        return "ні";
    }

    public static void main(String[] args) {
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
        System.setErr(new PrintStream(System.err, true, StandardCharsets.UTF_8));
        System.out.println("=== Лабораторна робота №2. Бібліотечна система. Варіант 3 ===");

        Author author = new Author("A-15", "Леся Українка", 1871);
        Book book = new Book("978-617-001", "Лісова пісня", author, Genre.FICTION, 1911);
        Reader reader = Reader.of(" R-101 ", " Олена Коваленко ", LocalDate.of(2003, 4, 18));

        LocalDate issue = LocalDate.now().minusDays(20);

        BookLoan loan = BookLoan.of(
                book, reader, issue, issue.plusDays(14),
                null, LoanStatus.ACTIVE
        );

        System.out.println("\nСтворення об'єктів:");
        System.out.println("Автор: " + author.name());
        System.out.println("Книга: " + book.title());
        System.out.println("Читач: " + reader.fullName());
        System.out.println("Номер читацького квитка: " + reader.readerTicketNumber());
        System.out.println("Жанр книги: " + LibraryUtils.genreName(book.genre()));
        System.out.println("Стан видачі: " + LibraryUtils.statusName(loan.getStatus()));

        System.out.println("\nДоступні жанри книг:");

        for (Genre genre : Genre.values()) {
            System.out.println(
                    LibraryUtils.genreName(genre)
                            + " — полиця №" + genre.getShelfNumber()
            );
        }

        System.out.println("\nПеревірка некоректних даних:");

        try {
            Reader.of("R-102", "Молодий читач", LocalDate.now().minusYears(10));
        } catch (IllegalArgumentException e) {
            System.out.println("1. " + e.getMessage());
        }

        try {
            new Book("978-617-002", "  ", author, Genre.SCIENCE, 2010);
        } catch (IllegalArgumentException e) {
            System.out.println("2. " + e.getMessage());
        }

        try {
            BookLoan.of(book, reader, issue, issue, null, LoanStatus.ACTIVE);
        } catch (IllegalArgumentException e) {
            System.out.println("3. " + e.getMessage());
        }

        try {
            Genre.valueOf("UNKNOWN");
        } catch (IllegalArgumentException e) {
            System.out.println("4. Жанр UNKNOWN не знайдено.");
        }

        System.out.println("\nПовернення книги:");

        try {
            loan.setReturnDate(issue.minusDays(1));
        } catch (IllegalArgumentException e) {
            log.warn("Invalid return date was rejected: {}", e.getMessage());
            System.out.println("Неправильна дата повернення: " + e.getMessage());
        }

        System.out.println("Дата повернення: "
                + LibraryUtils.dateText(loan.getReturnDate()));

        loan.setReturnDate(issue.plusDays(17));
        loan.setStatus(LoanStatus.RETURNED);

        System.out.println("Новий стан видачі: "
                + LibraryUtils.statusName(loan.getStatus()));

        Reader sameReader = Reader.of(
                "R-101", "Олена Коваленко",
                LocalDate.of(2003, 4, 18)
        );

        Reader otherReader = Reader.of(
                "R-202", "Інший читач",
                LocalDate.of(2002, 2, 2)
        );

        System.out.println("\nПорівняння читачів:");

        System.out.println("Однакові дані читачів: "
                + yesOrNo(reader.equals(sameReader)));

        System.out.println("Збігаються контрольні коди даних: "
                + yesOrNo(reader.hashCode() == sameReader.hashCode()));

        System.out.println("Різні читачі однакові: "
                + yesOrNo(reader.equals(otherReader)));

        BookLoan sameLoan = BookLoan.of(
                book, sameReader, issue,
                issue.plusDays(14), null, LoanStatus.ACTIVE
        );

        System.out.println("Записи про видачу збігаються: "
                + yesOrNo(loan.equals(sameLoan)));

        System.out.println("\nОбчислення:");

        System.out.println("Термін видачі книги (днів): "
                + loan.loanDays());

        System.out.println("Прострочення повернення (днів): "
                + loan.overdueDays());

        System.out.println("Прострочення для ще не поверненої книги (днів): "
                + sameLoan.overdueDays());

        System.out.println("\nУсі об'єкти бібліотечної системи:");

        System.out.println(author);
        System.out.println(book);
        System.out.println(reader);
        System.out.println(loan);

        System.out.println("\n=== Лабораторна робота №3. Читання CSV ===");

        List<Author> csvAuthors = LibraryCsvLoader.loadAuthors(Path.of("data", "authors.csv"));
        List<Reader> csvReaders = LibraryCsvLoader.loadReaders(Path.of("data", "readers.csv"));
        System.out.println("Завантажено авторів: " + csvAuthors.size());
        System.out.println("Завантажено читачів: " + csvReaders.size());

        System.out.println("\nПеревірка checked-виключення:");
        try {
            LibraryCsvLoader.parseAuthor("A-99,Incorrect Author,not-a-year", 99);
        } catch (InvalidDataException e) {
            log.warn("InvalidDataException was handled: {}", e.getMessage());
            System.out.println("Некоректний рядок: " + e.getMessage());
        }

        System.out.println("\nПеревірка відсутнього файла:");
        List<Author> missingAuthors = LibraryCsvLoader.loadAuthors(
                Path.of("data", "missing_authors.csv"));
        System.out.println("Завантажено з відсутнього файла: " + missingAuthors.size());

        log.info("Lab 3 demonstration completed");

        System.out.println("\n=== Лабораторна робота №4. Варіант 3: бібліотека ===");

        GenericRepository<Author> authors = new GenericRepository<>(
                "Authors", item -> item.authorCode());
        GenericRepository<Book> books = new GenericRepository<>(
                "Books", item -> item.isbn());
        GenericRepository<BookLoan> loans = new GenericRepository<>(
                "Loans", item -> item.getBook().isbn() + ":"
                + item.getReader().readerTicketNumber() + ":" + item.getIssueDate());

        books.add(book);
        books.addAll(List.of(new Book("978-617-002", "Захар Беркут",
                new Author("A-16", "Іван Франко", 1856), Genre.FICTION, 1883)));
        loans.add(loan);

        System.out.println("\nЗавантаження авторів із CSV у порожній репозиторій:");
        int duplicateRows = 0;
        for (Author csvAuthor : csvAuthors) {
            try {
                authors.add(csvAuthor);
                System.out.println("Додано автора " + csvAuthor.authorCode());
            } catch (DuplicateIdentityException e) {
                duplicateRows++;
                System.out.println("Дублікат у CSV: " + csvAuthor.authorCode());
            }
        }
        System.out.println("Повторних ключів у CSV: " + duplicateRows);
        System.out.println("Унікальних авторів: " + authors.size());

        System.out.println("\nПошук за унікальним полем у трьох репозиторіях:");
        System.out.println("Автор A-15: "
                + authors.findByIdentity("A-15").map(Author::name).orElse("не знайдено"));
        System.out.println("Автор A-999 знайдений: "
                + yesOrNo(authors.findByIdentity("A-999").isPresent()));
        System.out.println("Книга 978-617-001: "
                + books.findByIdentity("978-617-001").map(Book::title).orElse("не знайдено"));
        System.out.println("Книга без ISBN знайдена: "
                + yesOrNo(books.findByIdentity("неіснуючий-ISBN").isPresent()));
        String loanId = book.isbn() + ":" + reader.readerTicketNumber() + ":" + issue;
        System.out.println("Видача знайдена: "
                + yesOrNo(loans.findByIdentity(loanId).isPresent()));
        System.out.println("Невідома видача знайдена: "
                + yesOrNo(loans.findByIdentity("невідома").isPresent()));

        System.out.println("\nДублікат через add та addIfAbsent:");
        try {
            authors.add(new Author("A-15", "Інший автор", 1900));
        } catch (DuplicateIdentityException e) {
            System.out.println("add: отримано виключення: " + e.getMessage());
        }
        Author existingAuthor = authors.findByIdentity("A-15").orElseThrow();
        boolean added = authors.addIfAbsent(existingAuthor);
        System.out.println("addIfAbsent для того самого об'єкта додав: " + yesOrNo(added));
        System.out.println("Кількість авторів після перевірок: " + authors.size());

        System.out.println("\nВидалення:");
        System.out.println("Автор A-16 видалений: " + yesOrNo(authors.removeByIdentity("A-16")));
        System.out.println("Автор A-999 видалений: " + yesOrNo(authors.removeByIdentity("A-999")));

        System.out.println("\nЗмінюване поле у BookLoan не впливає на ключ:");
        loan.setStatus(LoanStatus.OVERDUE);
        System.out.println("Пошук після зміни статусу: "
                + yesOrNo(loans.findByIdentity(loanId).isPresent()));

        System.out.println("\nЗахист від змін списку findAll:");
        try {
            books.findAll().clear();
        } catch (UnsupportedOperationException e) {
            System.out.println("Список не можна очистити, книг у репозиторії: " + books.size());
        }

        System.out.println("\nПриклад помилки, що передається далі (ERROR):");
        try {
            books.add(null);
        } catch (IllegalArgumentException e) {
            System.out.println("Некоректний об'єкт відхилено: " + e.getMessage());
        }
        System.out.println("Події INFO, WARN та ERROR записані через SLF4J.");
    }
}
