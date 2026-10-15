package ua.library.util;

import java.time.LocalDate;
import ua.library.model.Author;
import ua.library.model.BookLoan;

// Public entry point for validation, formatting and calculations.
public final class LibraryUtils {
    private LibraryUtils() {
    }

    public static String text(String value, String fieldName) {
        ValidationHelper.requireNotBlank(value, fieldName);
        return value.trim();
    }

    public static void notNull(Object value, String fieldName) {
        ValidationHelper.requireNotNull(value, fieldName);
    }

    public static String genre(String value) {
        ValidationHelper.requireNotBlank(value, "Жанр");
        String result = FormatHelper.normalize(value);
        ValidationHelper.requireGenre(result);
        return result;
    }

    public static String status(String value) {
        ValidationHelper.requireNotBlank(value, "Стан видачі");
        String result = FormatHelper.normalize(value);
        ValidationHelper.requireStatus(result);
        return result;
    }

    public static LocalDate readerBirthDate(LocalDate value) {
        ValidationHelper.requireNotNull(value, "Дата народження читача");
        if (value.isAfter(LocalDate.now().minusYears(ValidationHelper.MIN_READER_AGE))) {
            throw new IllegalArgumentException(
                    "Вік читача: очікується не менше 14 років, отримано дату народження: " + value);
        }
        return value;
    }

    public static int authorBirthYear(int year) {
        ValidationHelper.requireInRange(year, ValidationHelper.MIN_AUTHOR_YEAR,
                LocalDate.now().getYear(), "Рік народження автора");
        return year;
    }

    public static int publishedYear(int year, Author author) {
        ValidationHelper.requireNotNull(author, "Автор книги");
        ValidationHelper.requireInRange(year, author.getBirthYear(),
                LocalDate.now().getYear(), "Рік видання");
        return year;
    }

    public static LocalDate issueDate(LocalDate value) {
        ValidationHelper.requireNotNull(value, "Дата видачі");
        if (value.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Дата видачі: очікується дата не пізніше сьогодні, отримано: " + value);
        }
        return value;
    }

    public static LocalDate dueDate(LocalDate value, LocalDate issueDate) {
        ValidationHelper.requireNotNull(value, "Гранична дата повернення");
        if (!value.isAfter(issueDate)) {
            throw new IllegalArgumentException(
                    "Дата повернення: очікується пізніше дати видачі " + issueDate + ", отримано: " + value);
        }
        return value;
    }

    public static LocalDate returnDate(LocalDate value, LocalDate issueDate) {
        if (value != null && value.isBefore(issueDate)) {
            throw new IllegalArgumentException(
                    "Фактична дата повернення: очікується не раніше дати видачі " + issueDate + ", отримано: " + value);
        }
        return value;
    }

    public static String genreName(String value) {
        return FormatHelper.genreName(value);
    }

    public static String statusName(String value) {
        return FormatHelper.statusName(value);
    }

    public static String dateText(LocalDate value) {
        if (value == null) {
            return "книгу ще не повернуто";
        }
        return value.toString();
    }


    public static long loanDays(BookLoan loan) {
        ValidationHelper.requireNotNull(loan, "Видача книги");
        return loan.getDueDate().toEpochDay() - loan.getIssueDate().toEpochDay();
    }

    public static long overdueDays(BookLoan loan) {
        ValidationHelper.requireNotNull(loan, "Видача книги");
        if (loan.getReturnDate() == null) {
            return 0;
        }
        long days = loan.getReturnDate().toEpochDay() - loan.getDueDate().toEpochDay();
        if (days > 0) {
            return days;
        }
        return 0;
    }
}
