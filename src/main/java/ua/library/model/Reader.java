package ua.library.model;

import java.time.LocalDate;
import ua.library.util.LibraryUtils;

public record Reader(String readerTicketNumber, String fullName, LocalDate birthDate) {
    public Reader {
        readerTicketNumber = LibraryUtils.text(readerTicketNumber, "Reader ticket number");
        fullName = LibraryUtils.text(fullName, "Reader name");
        birthDate = LibraryUtils.readerBirthDate(birthDate);
    }

    // Keep the factory method from Lab 1.
    public static Reader of(String readerTicketNumber, String fullName, LocalDate birthDate) {
        return new Reader(readerTicketNumber, fullName, birthDate);
    }
}
