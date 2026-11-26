package ua.clinic.model;

import ua.clinic.util.ClinicUtils;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.Objects;

public record Patient(String nationalId, String fullName, LocalDate birthDate) implements Comparable<Patient> {

    private static final Comparator<Patient> NATURAL_ORDER = Comparator
            .comparing(Patient::fullName)
            .thenComparing(Patient::birthDate)
            .thenComparing(Patient::nationalId);

    public Patient {
        ClinicUtils.validateNotBlank(nationalId, "nationalId");
        ClinicUtils.validateNotBlank(fullName, "fullName");
        ClinicUtils.validateNotFutureDate(birthDate, "birthDate");

        nationalId = ClinicUtils.cleanString(nationalId);
        fullName = ClinicUtils.cleanString(fullName);
    }

    public static Patient of(String nationalId, String fullName, LocalDate birthDate) {
        return new Patient(nationalId, fullName, birthDate);
    }

    @Override
    public int compareTo(Patient other) {
        Objects.requireNonNull(other, "other patient must not be null");
        return NATURAL_ORDER.compare(this, other);
    }
}
