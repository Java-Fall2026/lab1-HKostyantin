package ua.library.util;

final class ValidationHelper {
    static final int MIN_READER_AGE = 14;
    static final int MIN_AUTHOR_YEAR = 1800;

    private ValidationHelper() {
    }

    static void requireNotNull(Object value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + ": expected non-null value, got null");
        }
    }

    static void requireNotBlank(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + ": expected non-empty text, got " + value);
        }
    }

    static void requirePositive(double value, String fieldName) {
        if (!(value > 0)) {
            throw new IllegalArgumentException(fieldName + ": expected a number greater than 0, got " + value);
        }
    }

    static void requireInRange(int value, int min, int max, String fieldName) {
        if (value < min || value > max) {
            throw new IllegalArgumentException(fieldName + ": expected a number from " + min
                    + " to " + max + ", got " + value);
        }
    }
}
