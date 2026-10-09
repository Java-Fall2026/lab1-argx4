package ua.clinic.util;

import ua.clinic.model.Appointment;
import ua.clinic.model.AppointmentStatus;
import ua.clinic.model.Patient;
import ua.clinic.model.Specialization;

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
        return Period.between(patient.birthDate(), LocalDate.now()).getYears();
    }

    public static String getSpecializationDepartment(Specialization specialization) {
        validateNotNull(specialization, "specialization");
        return switch (specialization) {
            case CARDIOLOGY -> "Department of Cardiology, Corpus A, Floor 3";
            case NEUROLOGY -> "Department of Neurology, Corpus B, Floor 2";
            case PEDIATRICS -> "Children's Health Center, Corpus C, Floor 1";
            case DENTISTRY -> "Dental Care Wing, Corpus A, Floor 1";
        };
    }

    public static String getNextStepInstruction(AppointmentStatus status) {
        validateNotNull(status, "status");
        return switch (status) {
            case SCHEDULED -> "Send SMS reminder to patient 24h prior to visit";
            case IN_PROGRESS -> "Doctor consultation in progress, cabinet is occupied";
            case COMPLETED -> "Generate medical record and issue invoice for payment";
            case CANCELLED -> "Free up doctor slot in schedule and notify patient";
        };
    }
}
