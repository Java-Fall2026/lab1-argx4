package ua.clinic.model;

import ua.common.BaseEntity;
import ua.clinic.util.ClinicUtils;

import java.time.LocalDate;
import java.util.Objects;

public class Patient extends BaseEntity {
    private final String nationalId;
    private final String fullName;
    private final LocalDate birthDate;

    private Patient(String nationalId, String fullName, LocalDate birthDate) {
        super();
        ClinicUtils.validateNotBlank(nationalId, "nationalId");
        ClinicUtils.validateNotBlank(fullName, "fullName");
        ClinicUtils.validateNotFutureDate(birthDate, "birthDate");

        this.nationalId = ClinicUtils.cleanString(nationalId);
        this.fullName = ClinicUtils.cleanString(fullName);
        this.birthDate = birthDate;
    }

    public static Patient of(String nationalId, String fullName, LocalDate birthDate) {
        return new Patient(nationalId, fullName, birthDate);
    }

    public String getNationalId() {
        return nationalId;
    }

    public String getFullName() {
        return fullName;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Patient patient = (Patient) o;
        return Objects.equals(nationalId, patient.nationalId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nationalId);
    }

    @Override
    public String toString() {
        return "Patient{" +
                "nationalId='" + nationalId + '\'' +
                ", fullName='" + fullName + '\'' +
                ", birthDate=" + birthDate +
                ", createdAt=" + createdAt +
                '}';
    }
}