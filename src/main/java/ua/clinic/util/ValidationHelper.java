package ua.clinic.util;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

class ValidationHelper {
    static final List<String> ALLOWED_SPECIALIZATIONS = List.of(
            "CARDIOLOGY", "NEUROLOGY", "PEDIATRICS", "DENTISTRY"
    );

    static final List<String> ALLOWED_STATUSES = List.of(
            "SCHEDULED", "IN_PROGRESS", "COMPLETED", "CANCELLED"
    );

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
            throw new IllegalArgumentException("endTime must be strictly after startTime, got start: " + start + ", end: " + end);
        }
    }

    static void requireAllowedSpecialization(String specialization) {
        requireNotBlank(specialization, "specialization");
        if (!ALLOWED_SPECIALIZATIONS.contains(specialization)) {
            throw new IllegalArgumentException("specialization must be one of " + ALLOWED_SPECIALIZATIONS + ", got: " + specialization);
        }
    }

    static void requireAllowedStatus(String status) {
        requireNotBlank(status, "status");
        if (!ALLOWED_STATUSES.contains(status)) {
            throw new IllegalArgumentException("status must be one of " + ALLOWED_STATUSES + ", got: " + status);
        }
    }
}