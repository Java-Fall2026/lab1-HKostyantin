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
}
