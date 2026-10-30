package ua.library.util;

import java.time.LocalDate;
import ua.library.model.Author;
import ua.library.model.BookLoan;
import ua.library.model.Genre;
import ua.library.model.LoanStatus;

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

    public static LocalDate readerBirthDate(LocalDate value) {
        ValidationHelper.requireNotNull(value, "Reader birth date");
        if (value.isAfter(LocalDate.now().minusYears(ValidationHelper.MIN_READER_AGE))) {
            throw new IllegalArgumentException(
                    "Reader age: expected at least 14 years, got " + value);
        }
        return value;
    }

    public static int authorBirthYear(int year) {
        ValidationHelper.requireInRange(year, ValidationHelper.MIN_AUTHOR_YEAR,
                LocalDate.now().getYear(), "Author birth year");
        return year;
    }

    public static int publishedYear(int year, Author author) {
        ValidationHelper.requireNotNull(author, "Book author");
        ValidationHelper.requireInRange(year, author.birthYear(),
                LocalDate.now().getYear(), "Publication year");
        return year;
    }

    public static LocalDate issueDate(LocalDate value) {
        ValidationHelper.requireNotNull(value, "Loan issue date");
        if (value.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "Loan issue date: expected today or earlier, got " + value);
        }
        return value;
    }

    public static LocalDate dueDate(LocalDate value, LocalDate issueDate) {
        ValidationHelper.requireNotNull(value, "Loan due date");
        if (!value.isAfter(issueDate)) {
            throw new IllegalArgumentException(
                    "Loan due date: expected after " + issueDate + ", got " + value);
        }
        return value;
    }

    public static LocalDate returnDate(LocalDate value, LocalDate issueDate) {
        if (value != null && value.isBefore(issueDate)) {
            throw new IllegalArgumentException(
                    "Actual return date: expected not before " + issueDate + ", got " + value);
        }
        return value;
    }

    // Switch expression 1: the genre name.
    public static String genreName(Genre value) {
        ValidationHelper.requireNotNull(value, "Book genre");
        return switch (value) {
            case FICTION -> "художня література";
            case SCIENCE -> "наукова література";
            case HISTORY -> "історична література";
            case BIOGRAPHY -> "біографія";
        };
    }

    // Switch expression 2: the loan status name.
    public static String statusName(LoanStatus value) {
        ValidationHelper.requireNotNull(value, "Loan status");
        return switch (value) {
            case ACTIVE -> "активна";
            case RETURNED -> "повернено";
            case OVERDUE -> "прострочено";
        };
    }

    public static String dateText(LocalDate value) {
        if (value == null) {
            return "книгу ще не повернуто";
        }
        return value.toString();
    }

    public static long loanDays(BookLoan loan) {
        ValidationHelper.requireNotNull(loan, "Book loan");
        return loan.getDueDate().toEpochDay() - loan.getIssueDate().toEpochDay();
    }

    public static long overdueDays(BookLoan loan) {
        ValidationHelper.requireNotNull(loan, "Book loan");
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
