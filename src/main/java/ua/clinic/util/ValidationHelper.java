package ua.clinic.util;

import java.time.LocalDate;
import java.time.LocalTime;

class ValidationHelper {

    private ValidationHelper() {
    }

    static void requireNotNull(Object value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " must not be null");
        }
    }

    static void requireNotBlank(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
    }

    static void requireNonNegative(double value, String fieldName) {
        if (value < 0) {
            throw new IllegalArgumentException(fieldName + " must be non-negative, got: " + value);
        }
    }

    static void requireNotFutureDate(LocalDate date, String fieldName) {
        requireNotNull(date, fieldName);
        if (date.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException(fieldName + " cannot be in the future, got: " + date);
        }
    }

    static void requireValidTimeRange(LocalTime start, LocalTime end) {
        requireNotNull(start, "startTime");
        requireNotNull(end, "endTime");
        if (!end.isAfter(start)) {
            throw new IllegalArgumentException(
                    "endTime must be strictly after startTime, got start: " + start + ", end: " + end);
        }
    }
}
