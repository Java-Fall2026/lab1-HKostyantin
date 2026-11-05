package ua.library.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.library.exception.InvalidDataException;
import ua.library.model.Author;
import ua.library.model.Reader;

public final class LibraryCsvLoader {
    private static final Logger log = LoggerFactory.getLogger(LibraryCsvLoader.class);

    private LibraryCsvLoader() {
    }

    public static Author parseAuthor(String line, int lineNumber) throws InvalidDataException {
        String[] parts = line.split(",", -1);
        if (parts.length != 3 || line.contains("\"")) {
            throw new InvalidDataException("Line " + lineNumber + ": expected authorCode,name,birthYear");
        }
        try {
            return new Author(parts[0].trim(), parts[1].trim(),
                    Integer.parseInt(parts[2].trim()));
        } catch (IllegalArgumentException e) {
            throw new InvalidDataException("Line " + lineNumber + ": invalid author data", e);
        }
    }

    public static Reader parseReader(String line, int lineNumber) throws InvalidDataException {
        String[] parts = line.split(",", -1);
        if (parts.length != 3 || line.contains("\"")) {
            throw new InvalidDataException("Line " + lineNumber + ": expected readerTicketNumber,fullName,birthDate");
        }
        try {
            return new Reader(parts[0].trim(), parts[1].trim(),
                    LocalDate.parse(parts[2].trim()));
        } catch (IllegalArgumentException | DateTimeParseException e) {
            throw new InvalidDataException("Line " + lineNumber + ": invalid reader data", e);
        }
    }

    public static List<Author> loadAuthors(Path path) {
        List<Author> authors = new ArrayList<>();
        log.info("Reading authors from {}", path);
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line = reader.readLine();
            int lineNumber = 1;
            if (line == null || !line.equals("authorCode,name,birthYear")) {
                log.error("Invalid or missing authors CSV header in {}", path);
                return authors;
            }
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                try {
                    Author author = parseAuthor(line, lineNumber);
                    authors.add(author);
                    log.info("Created author {}", author.authorCode());
                } catch (InvalidDataException e) {
                    log.warn("Skipping invalid authors row: {}", e.getMessage());
                }
            }
            log.info("Loaded {} authors", authors.size());
        } catch (NoSuchFileException | AccessDeniedException e) {
            log.error("Authors CSV unavailable: {}", path, e);
        } catch (IOException e) {
            log.error("Failed to read authors CSV: {}", path, e);
        } finally {
            log.info("Finished authors CSV read attempt: {}", path);
        }
        return authors;
    }

    public static List<Reader> loadReaders(Path path) {
        List<Reader> readers = new ArrayList<>();
        log.info("Reading readers from {}", path);
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line = reader.readLine();
            int lineNumber = 1;
            if (line == null || !line.equals("readerTicketNumber,fullName,birthDate")) {
                log.error("Invalid or missing readers CSV header in {}", path);
                return readers;
            }
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                try {
                    Reader value = parseReader(line, lineNumber);
                    readers.add(value);
                    log.info("Created reader {}", value.readerTicketNumber());
                } catch (InvalidDataException e) {
                    log.warn("Skipping invalid readers row: {}", e.getMessage());
                }
            }
            log.info("Loaded {} readers", readers.size());
        } catch (NoSuchFileException | AccessDeniedException e) {
            log.error("Readers CSV unavailable: {}", path, e);
        } catch (IOException e) {
            log.error("Failed to read readers CSV: {}", path, e);
        } finally {
            log.info("Finished readers CSV read attempt: {}", path);
        }
        return readers;
    }
}
