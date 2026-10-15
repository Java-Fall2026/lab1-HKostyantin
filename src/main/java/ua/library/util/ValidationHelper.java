package ua.library.util;


final class ValidationHelper {
    static final int MIN_READER_AGE = 14;
    static final int MIN_AUTHOR_YEAR = 1800;

    static final String GENRE_FICTION = "FICTION";
    static final String GENRE_SCIENCE = "SCIENCE";
    static final String GENRE_HISTORY = "HISTORY";
    static final String GENRE_BIOGRAPHY = "BIOGRAPHY";

    static final String STATUS_ACTIVE = "ACTIVE";
    static final String STATUS_RETURNED = "RETURNED";
    static final String STATUS_OVERDUE = "OVERDUE";

    private ValidationHelper() {
    }

    static void requireNotNull(Object value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + ": очікується значення, отримано: null");
        }
    }

    static void requireNotBlank(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + ": очікується непорожній текст, отримано: " + value);
        }
    }

    static void requirePositive(double value, String fieldName) {
        if (!(value > 0)) {
            throw new IllegalArgumentException(fieldName + ": очікується число більше 0, отримано: " + value);
        }
    }

    static void requireInRange(int value, int min, int max, String fieldName) {
        if (value < min || value > max) {
            throw new IllegalArgumentException(fieldName + ": очікується число від " + min + " до " + max + ", отримано: " + value);
        }
    }

    static void requireGenre(String value) {
        if (!value.equals(GENRE_FICTION)
                && !value.equals(GENRE_SCIENCE)
                && !value.equals(GENRE_HISTORY)
                && !value.equals(GENRE_BIOGRAPHY)) {
            throw new IllegalArgumentException(
                    "Жанр: очікується один із кодів FICTION, SCIENCE, HISTORY або BIOGRAPHY, отримано: " + value);
        }
    }

    static void requireStatus(String value) {
        if (!value.equals(STATUS_ACTIVE)
                && !value.equals(STATUS_RETURNED)
                && !value.equals(STATUS_OVERDUE)) {
            throw new IllegalArgumentException(
                    "Стан видачі: очікується один із кодів ACTIVE, RETURNED або OVERDUE, отримано: " + value);
        }
    }
}
