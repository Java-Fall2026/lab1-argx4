package ua.clinic.util;

import ua.clinic.model.Appointment;
import ua.clinic.model.Patient;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Period;

public class ClinicUtils {

    private ClinicUtils() {
    }

    public static void validateNotNull(Object value, String fieldName) {
        ValidationHelper.requireNotNull(value, fieldName);
    }

    public static void validateNotBlank(String value, String fieldName) {
        ValidationHelper.requireNotBlank(value, fieldName);
    }

    public static void validateNonNegative(double value, String fieldName) {
        ValidationHelper.requireNonNegative(value, fieldName);
    }

    public static void validateNotFutureDate(LocalDate date, String fieldName) {
        ValidationHelper.requireNotFutureDate(date, fieldName);
    }

    public static void validateTimeRange(LocalTime start, LocalTime end) {
        ValidationHelper.requireValidTimeRange(start, end);
    }

    public static void validateSpecialization(String specialization) {
        ValidationHelper.requireAllowedSpecialization(specialization);
    }

    public static void validateStatus(String status) {
        ValidationHelper.requireAllowedStatus(status);
    }

    public static String normalizeString(String value) {
        return FormatHelper.normalize(value);
    }

    public static String cleanString(String value) {
        return FormatHelper.trim(value);
    }

    public static long durationMinutes(Appointment appointment) {
        validateNotNull(appointment, "appointment");
        return Duration.between(appointment.getStartTime(), appointment.getEndTime()).toMinutes();
    }

    public static int patientAge(Patient patient) {
        validateNotNull(patient, "patient");
        return Period.between(patient.getBirthDate(), LocalDate.now()).getYears();
    }
}