package ua.library;

import java.time.LocalDate;
import ua.library.model.Author;
import ua.library.model.Book;
import ua.library.model.BookLoan;
import ua.library.model.Reader;
import ua.library.util.LibraryUtils;

public class Main {
    private static String yesOrNo(boolean value) {
        if (value) {
            return "так";
        }
        return "ні";
    }

    public static void main(String[] args) {
        System.out.println("=== Лабораторна робота №1. Бібліотечна система. Варіант 3 ===");

        Author author = new Author("A-15", "Леся Українка", 1871);
        Book book = new Book("978-617-001", "Лісова пісня", author, " fiction ", 1911);
        Reader reader = Reader.of(" R-101 ", " Олена Коваленко ", LocalDate.of(2003, 4, 18));
        LocalDate issue = LocalDate.now().minusDays(20);
        BookLoan loan = BookLoan.of(book, reader, issue, issue.plusDays(14), null, " active ");

        System.out.println("\nСтворення об'єктів:");
        System.out.println("Автор, створений через конструктор: " + author.getName());
        System.out.println("Читач, створений через фабричний метод: " + reader.getFullName());
        System.out.println("Жанр після нормалізації: " + LibraryUtils.genreName(book.getGenre()));
        System.out.println("Стан видачі після нормалізації: " + LibraryUtils.statusName(loan.getStatus()));
        System.out.println("Номер читацького квитка без пробілів: " + reader.getReaderTicketNumber());

        System.out.println("\nТри приклади некоректних даних:");
        try {
            Reader.of("R-102", "Молодий читач", LocalDate.now().minusYears(10));
        } catch (IllegalArgumentException e) {
            System.out.println("1. " + e.getMessage());
        }
        try {
            new Book("978-617-002", "Книга з помилкою", author, " невідомий жанр ", 2010);
        } catch (IllegalArgumentException e) {
            System.out.println("2. " + e.getMessage());
        }
        try {
            BookLoan.of(book, reader, issue, issue, null, "ACTIVE");
        } catch (IllegalArgumentException e) {
            System.out.println("3. " + e.getMessage());
        }

        System.out.println("\nПеревірка зміни даних:");
        try {
            loan.setReturnDate(issue.minusDays(1));
        } catch (IllegalArgumentException e) {
            System.out.println("Помилка під час зміни дати повернення: " + e.getMessage());
        }
        System.out.println("Дата повернення після невдалої зміни: "
                + LibraryUtils.dateText(loan.getReturnDate()));
        loan.setReturnDate(issue.plusDays(17));
        loan.setStatus(" returned ");
        System.out.println("Стан видачі після зміни: " + LibraryUtils.statusName(loan.getStatus()));

        Reader sameReader = Reader.of("R-101", "Олена Коваленко", LocalDate.of(2003, 4, 18));
        Reader otherReader = Reader.of("R-202", "Інший читач", LocalDate.of(2002, 2, 2));
        System.out.println("\nПорівняння об'єктів:");
        System.out.println("Це той самий об'єкт (порівняння посилань): " + yesOrNo(reader == sameReader));
        System.out.println("Читачі рівні за значенням: " + yesOrNo(reader.equals(sameReader)));
        System.out.println("Хеш-коди читачів збігаються: "
                + yesOrNo(reader.hashCode() == sameReader.hashCode()));
        System.out.println("Читачі з різними номерами однакові: "
                + yesOrNo(reader.equals(otherReader)));

        BookLoan sameLoan = BookLoan.of(book, sameReader, issue, issue.plusDays(14), null, "ACTIVE");
        System.out.println("Записи про видачу однакові за змістом: " + yesOrNo(loan.equals(sameLoan)));
        System.out.println("Коди записів про видачу збігаються: "
                + yesOrNo(loan.hashCode() == sameLoan.hashCode()));

        System.out.println("\nОбчислення:");
        System.out.println("Термін видачі книги (днів): " + loan.loanDays());
        System.out.println("Прострочення повернення (днів): " + loan.overdueDays());
        System.out.println("Прострочення для ще не поверненої книги (днів): "
                + sameLoan.overdueDays());

        System.out.println("\nУсі об'єкти бібліотечної системи:");
        System.out.println(author);
        System.out.println(book);
        System.out.println(reader);
        System.out.println(loan);

        // The protected createdAt field is inherited from BaseEntity.
        // ValidationHelper is accessible only within the ua.library.util package.
        // This line would not compile because ValidationHelper is package-private:
        // ua.library.util.ValidationHelper.requireNotNull(reader, "Reader");
        // The Reader constructor is private, so this call would not compile either:
        // Reader wrong = new Reader("R-500", "Example", LocalDate.of(2000, 1, 1));
    }
}
