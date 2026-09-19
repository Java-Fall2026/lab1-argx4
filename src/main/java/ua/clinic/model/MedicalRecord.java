package ua.clinic.model;

import ua.common.BaseEntity;
import ua.clinic.util.ClinicUtils;

import java.util.Objects;

public class MedicalRecord extends BaseEntity {
    private final String recordId;
    private final Appointment appointment;
    private final String diagnosis;
    private final double treatmentCost;

    public MedicalRecord(String recordId, Appointment appointment, String diagnosis, double treatmentCost) {
        super();
        ClinicUtils.validateNotBlank(recordId, "recordId");
        ClinicUtils.validateNotNull(appointment, "appointment");
        ClinicUtils.validateNotBlank(diagnosis, "diagnosis");
        ClinicUtils.validateNonNegative(treatmentCost, "treatmentCost");

        this.recordId = ClinicUtils.cleanString(recordId);
        this.appointment = appointment;
        this.diagnosis = ClinicUtils.cleanString(diagnosis);
        this.treatmentCost = treatmentCost;
    }

    public String getRecordId() {
        return recordId;
    }

    public Appointment getAppointment() {
        return appointment;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public double getTreatmentCost() {
        return treatmentCost;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MedicalRecord that = (MedicalRecord) o;
        return Objects.equals(recordId, that.recordId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(recordId);
    }

    @Override
    public String toString() {
        return "MedicalRecord{" +
                "recordId='" + recordId + '\'' +
                ", appointment=" + appointment +
                ", diagnosis='" + diagnosis + '\'' +
                ", treatmentCost=" + treatmentCost +
                ", createdAt=" + createdAt +
                '}';
    }
}