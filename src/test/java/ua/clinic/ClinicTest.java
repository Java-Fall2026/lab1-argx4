package ua.clinic;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ua.clinic.exception.InvalidDataException;
import ua.clinic.io.DoctorLoader;
import ua.clinic.io.PatientLoader;
import ua.clinic.model.Appointment;
import ua.clinic.model.Doctor;
import ua.clinic.model.MedicalRecord;
import ua.clinic.model.Patient;
import ua.clinic.util.ClinicUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Medical Clinic — Lab 3 Tests")
class ClinicTest {

    @Test
    @DisplayName("Patient is created with valid data")
    void patientCreatedWithValidData() {
        Patient patient = Patient.of("ID-001", "John Doe", LocalDate.of(1990, 1, 1));
        assertNotNull(patient);
        assertEquals("ID-001", patient.getNationalId());
        assertEquals("John Doe", patient.getFullName());
        assertEquals(LocalDate.of(1990, 1, 1), patient.getBirthDate());
    }

    @Test
    @DisplayName("Doctor is created with valid data")
    void doctorCreatedWithValidData() {
        Doctor doctor = Doctor.of("DOC-100", "Jane Smith", "CARDIOLOGY");
        assertNotNull(doctor);
        assertEquals("DOC-100", doctor.getLicenseNumber());
        assertEquals("Jane Smith", doctor.getName());
        assertEquals("CARDIOLOGY", doctor.getSpecialization());
    }

    @Test
    @DisplayName("Appointment is created with valid data")
    void appointmentCreatedWithValidData() {
        Patient patient = Patient.of("ID-010", "Test Patient", LocalDate.of(2000, 6, 15));
        Doctor doctor = Doctor.of("DOC-010", "Test Doctor", "NEUROLOGY");
        Appointment appointment = new Appointment(
                patient, doctor,
                LocalDate.of(2026, 12, 1),
                LocalTime.of(9, 0),
                LocalTime.of(9, 30),
                "SCHEDULED"
        );
        assertNotNull(appointment);
        assertEquals("SCHEDULED", appointment.getStatus());
    }

    @Test
    @DisplayName("MedicalRecord is created with valid data")
    void medicalRecordCreatedWithValidData() {
        Patient patient = Patient.of("ID-020", "Record Patient", LocalDate.of(1985, 3, 20));
        Doctor doctor = Doctor.of("DOC-020", "Record Doctor", "DENTISTRY");
        Appointment appointment = new Appointment(
                patient, doctor,
                LocalDate.of(2026, 11, 1),
                LocalTime.of(14, 0),
                LocalTime.of(14, 45),
                "COMPLETED"
        );
        MedicalRecord record = new MedicalRecord("REC-001", appointment, "Routine checkup", 200.0);
        assertNotNull(record);
        assertEquals("REC-001", record.getRecordId());
        assertEquals(200.0, record.getTreatmentCost());
    }

    @Test
    @DisplayName("InvalidDataException is thrown for patient line with invalid date")
    void invalidDataExceptionOnBadPatientDate() {
        assertThrows(InvalidDataException.class,
                () -> PatientLoader.parseLine("ID-001,Test,not-a-date", 2));
    }

    @Test
    @DisplayName("InvalidDataException is thrown for patient line with blank nationalId")
    void invalidDataExceptionOnBlankPatientId() {
        assertThrows(InvalidDataException.class,
                () -> PatientLoader.parseLine(",Test,2000-01-01", 3));
    }

    @Test
    @DisplayName("InvalidDataException is thrown for doctor line with invalid specialization")
    void invalidDataExceptionOnBadDoctorSpecialization() {
        assertThrows(InvalidDataException.class,
                () -> DoctorLoader.parseLine("DOC-999,Test,SURGERY", 4));
    }

    @Test
    @DisplayName("InvalidDataException is thrown for line with wrong number of fields")
    void invalidDataExceptionOnWrongFieldCount() {
        assertThrows(InvalidDataException.class,
                () -> PatientLoader.parseLine("only,two", 5));
    }

    @Test
    @DisplayName("InvalidDataException has cause when wrapping another exception")
    void invalidDataExceptionPreservesCause() {
        InvalidDataException ex = assertThrows(InvalidDataException.class,
                () -> PatientLoader.parseLine("ID-001,Test,not-a-date", 2));
        assertNotNull(ex.getCause(), "Cause should be preserved");
    }

    @Test
    @DisplayName("durationMinutes calculates correct appointment duration")
    void durationMinutesCalculatesCorrectly() {
        Patient patient = Patient.of("ID-050", "Duration Test", LocalDate.of(1990, 1, 1));
        Doctor doctor = Doctor.of("DOC-050", "Duration Doc", "PEDIATRICS");
        Appointment appointment = new Appointment(
                patient, doctor,
                LocalDate.of(2026, 12, 1),
                LocalTime.of(10, 0),
                LocalTime.of(10, 45),
                "SCHEDULED"
        );
        assertEquals(45, ClinicUtils.durationMinutes(appointment));
    }

    @Test
    @DisplayName("patientAge returns correct full years")
    void patientAgeReturnsCorrectYears() {
        LocalDate birthDate = LocalDate.now().minusYears(30).minusDays(1);
        Patient patient = Patient.of("ID-060", "Age Test", birthDate);
        assertEquals(30, ClinicUtils.patientAge(patient));
    }

    @Test
    @DisplayName("PatientLoader skips invalid lines and loads valid ones")
    void patientLoaderSkipsInvalidLines() throws IOException {
        Path tempFile = Files.createTempFile("patients_test", ".csv");
        try {
            Files.writeString(tempFile,
                    "nationalId,fullName,birthDate\n" +
                    "ID-001,Valid Patient,2000-01-15\n" +
                    ",Missing Id,2000-01-15\n" +
                    "ID-003,Future Date,2099-12-31\n" +
                    "ID-004,Another Valid,1985-06-10\n"
            );
            List<Patient> patients = PatientLoader.loadFromCsv(tempFile);
            assertEquals(2, patients.size(), "Only 2 valid patients should be loaded");
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    @Test
    @DisplayName("PatientLoader returns empty list for missing file")
    void patientLoaderReturnsEmptyForMissingFile() {
        List<Patient> result = PatientLoader.loadFromCsv(Path.of("no/such/file.csv"));
        assertTrue(result.isEmpty());
    }
}
