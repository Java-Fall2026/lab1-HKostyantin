package ua.library.model;

import java.time.LocalDate;
import ua.common.BaseEntity;
import ua.library.util.LibraryUtils;

public final class BookLoan extends BaseEntity {
    private final Book book;
    private final Reader reader;
    private final LocalDate issueDate;
    private final LocalDate dueDate;
    private LocalDate returnDate;
    private String status;

    private BookLoan(Book book, Reader reader, LocalDate issueDate, LocalDate dueDate,
                     LocalDate returnDate, String status) {
        super();
        LibraryUtils.notNull(book, "Книга у видачі");
        LibraryUtils.notNull(reader, "Читач у видачі");
        this.book = book;
        this.reader = reader;
        this.issueDate = LibraryUtils.issueDate(issueDate);
        this.dueDate = LibraryUtils.dueDate(dueDate, this.issueDate);
        setReturnDate(returnDate);
        setStatus(status);
    }

    public static BookLoan of(Book book, Reader reader, LocalDate issueDate, LocalDate dueDate,
                              LocalDate returnDate, String status) {
        return new BookLoan(book, reader, issueDate, dueDate, returnDate, status);
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

    public String getStatus() {
        return status;
    }

    public final void setReturnDate(LocalDate returnDate) {
        this.returnDate = LibraryUtils.returnDate(returnDate, issueDate);
    }

    public final void setStatus(String status) {
        this.status = LibraryUtils.status(status);
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
        return "Видача книги{книга=" + book + ", читач=" + reader + ", дата видачі=" + issueDate
                + ", повернути до=" + dueDate + ", фактична дата повернення=" + LibraryUtils.dateText(returnDate) + ", стан='" + LibraryUtils.statusName(status)
                + "', створено=" + createdAt + "}";
    }
}
