package ua.clinic.model;

import ua.clinic.util.ClinicUtils;

public record MedicalRecord(String recordId, Appointment appointment, String diagnosis, double treatmentCost) {

    public MedicalRecord {
        ClinicUtils.validateNotBlank(recordId, "recordId");
        ClinicUtils.validateNotNull(appointment, "appointment");
        ClinicUtils.validateNotBlank(diagnosis, "diagnosis");
        ClinicUtils.validateNonNegative(treatmentCost, "treatmentCost");

        recordId = ClinicUtils.cleanString(recordId);
        diagnosis = ClinicUtils.cleanString(diagnosis);
    }
}
