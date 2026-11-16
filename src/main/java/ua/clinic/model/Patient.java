package ua.clinic.model;

import ua.clinic.util.ClinicUtils;

import java.time.LocalDate;

public record Patient(String nationalId, String fullName, LocalDate birthDate) {

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
}
