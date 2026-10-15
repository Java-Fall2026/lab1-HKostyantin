package ua.library.util;

final class FormatHelper {
    private FormatHelper() {
    }

    static String normalize(String text) {
        return text.trim().toUpperCase(java.util.Locale.ROOT);
    }

    static String capitalize(String text) {
        String value = text.trim();
        if (value.isEmpty()) {
            return value;
        }
        return value.substring(0, 1).toUpperCase() + value.substring(1);
    }

    static String formatMoney(double money) {
        return String.format("%.2f", money);
    }

    static String genreName(String genre) {
        if (genre.equals("FICTION")) {
            return "художня література";
        }
        if (genre.equals("SCIENCE")) {
            return "наукова література";
        }
        if (genre.equals("HISTORY")) {
            return "історична література";
        }
        if (genre.equals("BIOGRAPHY")) {
            return "біографія";
        }
        return genre;
    }

    static String statusName(String status) {
        if (status.equals("ACTIVE")) {
            return "активна";
        }
        if (status.equals("RETURNED")) {
            return "повернено";
        }
        if (status.equals("OVERDUE")) {
            return "прострочено";
        }
        return status;
    }
}
