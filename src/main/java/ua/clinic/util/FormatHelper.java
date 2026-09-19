package ua.clinic.util;

class FormatHelper {

    private FormatHelper() {
    }

    static String normalize(String value) {
        if (value == null) {
            return null;
        }
        return value.trim().toUpperCase();
    }

    static String trim(String value) {
        if (value == null) {
            return null;
        }
        return value.trim();
    }
}