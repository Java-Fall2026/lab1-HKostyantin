
package ua.library;

import java.time.LocalDate;
import ua.library.model.Author;
import ua.library.model.Book;
import ua.library.model.BookLoan;
import ua.library.model.Genre;
import ua.library.model.LoanStatus;
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
    }
}
