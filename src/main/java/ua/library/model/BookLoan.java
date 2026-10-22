package ua.library.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import ua.library.util.LibraryUtils;

// BookLoan stays a class because returnDate and status can change.
public final class BookLoan {
    private final LocalDateTime createdAt;
    private final Book book;
    private final Reader reader;
    private final LocalDate issueDate;
    private final LocalDate dueDate;
    private LocalDate returnDate;
    private LoanStatus status;

    private BookLoan(Book book, Reader reader, LocalDate issueDate, LocalDate dueDate,
                     LocalDate returnDate, LoanStatus status) {
        this.createdAt = LocalDateTime.now();
        LibraryUtils.notNull(book, "Loan book");
        LibraryUtils.notNull(reader, "Loan reader");
        this.book = book;
        this.reader = reader;
        this.issueDate = LibraryUtils.issueDate(issueDate);
        this.dueDate = LibraryUtils.dueDate(dueDate, this.issueDate);
        setReturnDate(returnDate);
        setStatus(status);
    }

    public static BookLoan of(Book book, Reader reader, LocalDate issueDate, LocalDate dueDate,
                              LocalDate returnDate, LoanStatus status) {
        return new BookLoan(book, reader, issueDate, dueDate, returnDate, status);
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Book getBook() {
        return book;
    }

    public Reader getReader() {
        return reader;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public LoanStatus getStatus() {
        return status;
    }

    public final void setReturnDate(LocalDate returnDate) {
        this.returnDate = LibraryUtils.returnDate(returnDate, issueDate);
    }

    public final void setStatus(LoanStatus status) {
        LibraryUtils.notNull(status, "Loan status");
        this.status = status;
    }

    public long loanDays() {
        return LibraryUtils.loanDays(this);
    }

    public long overdueDays() {
        return LibraryUtils.overdueDays(this);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        BookLoan loan = (BookLoan) other;
        return book.equals(loan.book)
                && reader.equals(loan.reader)
                && issueDate.equals(loan.issueDate);
    }

    @Override
    public int hashCode() {
        int result = book.hashCode();
        result = 31 * result + reader.hashCode();
        result = 31 * result + issueDate.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "BookLoan{book=" + book + ", reader=" + reader + ", issueDate=" + issueDate
                + ", dueDate=" + dueDate + ", returnDate=" + LibraryUtils.dateText(returnDate)
                + ", status=" + status + ", createdAt=" + createdAt + "}";
    }
}
