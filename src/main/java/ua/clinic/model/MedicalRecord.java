package ua.clinic.model;

import ua.clinic.util.ClinicUtils;

import java.util.Comparator;
import java.util.Objects;

public record MedicalRecord(String recordId, Appointment appointment, String diagnosis, double treatmentCost)
        implements Comparable<MedicalRecord> {

    private static final Comparator<MedicalRecord> NATURAL_ORDER = Comparator
            .comparingDouble(MedicalRecord::treatmentCost)
            .thenComparing(MedicalRecord::recordId)
            .thenComparing(MedicalRecord::diagnosis)
            .thenComparing(r -> r.appointment().getDoctor().licenseNumber())
            .thenComparing(r -> r.appointment().getDoctor().name())
            .thenComparing(r -> r.appointment().getDoctor().specialization())
            .thenComparing(r -> r.appointment().getAppointmentDate())
            .thenComparing(r -> r.appointment().getStartTime());

    public MedicalRecord {
        ClinicUtils.validateNotBlank(recordId, "recordId");
        ClinicUtils.validateNotNull(appointment, "appointment");
        ClinicUtils.validateNotBlank(diagnosis, "diagnosis");
        ClinicUtils.validateNonNegative(treatmentCost, "treatmentCost");

        recordId = ClinicUtils.cleanString(recordId);
        diagnosis = ClinicUtils.cleanString(diagnosis);
    }

    @Override
    public int compareTo(MedicalRecord other) {
        Objects.requireNonNull(other, "other medical record must not be null");
        return NATURAL_ORDER.compare(this, other);
    }
}
