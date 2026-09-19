package ua.clinic.model;

import ua.common.BaseEntity;
import ua.clinic.util.ClinicUtils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

public class Appointment extends BaseEntity {
    private final Patient patient;
    private final Doctor doctor;
    private final LocalDate appointmentDate;
    private final LocalTime startTime;
    private final LocalTime endTime;
    private String status;

    public Appointment(Patient patient, Doctor doctor, LocalDate appointmentDate, LocalTime startTime, LocalTime endTime, String status) {
        super();
        ClinicUtils.validateNotNull(patient, "patient");
        ClinicUtils.validateNotNull(doctor, "doctor");
        ClinicUtils.validateNotNull(appointmentDate, "appointmentDate");
        ClinicUtils.validateTimeRange(startTime, endTime);

        this.patient = patient;
        this.doctor = doctor;
        this.appointmentDate = appointmentDate;
        this.startTime = startTime;
        this.endTime = endTime;
        setStatus(status);
    }

    public final void setStatus(String status) {
        String normalizedStatus = ClinicUtils.normalizeString(status);
        ClinicUtils.validateStatus(normalizedStatus);
        this.status = normalizedStatus;
    }

    public Patient getPatient() {
        return patient;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public LocalDate getAppointmentDate() {
        return appointmentDate;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public String getStatus() {
        return status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Appointment that = (Appointment) o;
        return Objects.equals(doctor, that.doctor) &&
                Objects.equals(appointmentDate, that.appointmentDate) &&
                Objects.equals(startTime, that.startTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(doctor, appointmentDate, startTime);
    }

    @Override
    public String toString() {
        return "Appointment{" +
                "patient=" + patient +
                ", doctor=" + doctor +
                ", appointmentDate=" + appointmentDate +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", status='" + status + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}