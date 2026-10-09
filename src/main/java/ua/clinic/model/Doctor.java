package ua.clinic.model;

import ua.clinic.util.ClinicUtils;

public record Doctor(String licenseNumber, String name, Specialization specialization) {

    public Doctor {
        ClinicUtils.validateNotBlank(licenseNumber, "licenseNumber");
        ClinicUtils.validateNotBlank(name, "name");
        ClinicUtils.validateNotNull(specialization, "specialization");

        licenseNumber = ClinicUtils.cleanString(licenseNumber);
        name = ClinicUtils.cleanString(name);
    }

    public static Doctor of(String licenseNumber, String name, Specialization specialization) {
        return new Doctor(licenseNumber, name, specialization);
    }
}
