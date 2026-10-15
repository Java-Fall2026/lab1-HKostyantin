package ua.library.model;

import java.time.LocalDate;
import ua.common.BaseEntity;
import ua.library.util.LibraryUtils;

public final class Reader extends BaseEntity {
    private final String readerTicketNumber;
    private final String fullName;
    private final LocalDate birthDate;

    private Reader(String readerTicketNumber, String fullName, LocalDate birthDate) {
        super();
        this.readerTicketNumber = LibraryUtils.text(readerTicketNumber, "Номер читацького квитка");
        this.fullName = LibraryUtils.text(fullName, "ПІБ читача");
        this.birthDate = LibraryUtils.readerBirthDate(birthDate);
    }

    public static Reader of(String readerTicketNumber, String fullName, LocalDate birthDate) {
        return new Reader(readerTicketNumber, fullName, birthDate);
    }

    public String getReaderTicketNumber() {
        return readerTicketNumber;
    }

    public String getFullName() {
        return fullName;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        Reader reader = (Reader) other;
        return readerTicketNumber.equals(reader.readerTicketNumber);
    }

    @Override
    public int hashCode() {
        return readerTicketNumber.hashCode();
    }

    @Override
    public String toString() {
        return "Читач{номер квитка='" + readerTicketNumber + "', ім’я='" + fullName
                + "', дата народження=" + birthDate + ", створено=" + createdAt + "}";
    }
}
