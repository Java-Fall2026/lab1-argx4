package ua.clinic.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ua.clinic.exception.DuplicateIdentityException;
import ua.clinic.model.Appointment;
import ua.clinic.model.AppointmentStatus;
import ua.clinic.model.Doctor;
import ua.clinic.model.MedicalRecord;
import ua.clinic.model.Patient;
import ua.clinic.model.Specialization;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GenericRepository — Lab 4 Tests")
class GenericRepositoryTest {

    @Test
    @DisplayName("add and findByIdentity locate the added element")
    void addAndFindByIdentityFindsElement() {
        GenericRepository<Patient> repo = new GenericRepository<>(Patient.class, p -> p.nationalId());
        Patient patient = Patient.of("ID-100", "Ivan Kovalenko", LocalDate.of(1995, 5, 14));

        repo.add(patient);
        Optional<Patient> found = repo.findByIdentity("ID-100");

        assertTrue(found.isPresent());
        assertEquals(patient, found.get());
        assertEquals(1, repo.size());
    }

    @Test
    @DisplayName("findByIdentity returns empty Optional when identity is absent")
    void findByIdentityReturnsEmptyForMissingIdentity() {
        GenericRepository<Patient> repo = new GenericRepository<>(Patient.class, p -> p.nationalId());
        repo.add(Patient.of("ID-100", "Ivan Kovalenko", LocalDate.of(1995, 5, 14)));

        Optional<Patient> missing = repo.findByIdentity("ID-999");

        assertTrue(missing.isEmpty());
    }

    @Test
    @DisplayName("Duplicate add throws unchecked DuplicateIdentityException with identity and type name")
    void duplicateAddThrowsDuplicateIdentityException() {
        GenericRepository<Patient> repo = new GenericRepository<>(Patient.class, p -> p.nationalId());
        Patient first = Patient.of("ID-200", "Olena Sydorenko", LocalDate.of(1988, 11, 23));
        Patient duplicate = Patient.of("ID-200", "Duplicate Name", LocalDate.of(1990, 1, 1));

        repo.add(first);

        DuplicateIdentityException ex = assertThrows(
                DuplicateIdentityException.class,
                () -> repo.add(duplicate)
        );
        assertInstanceOf(IllegalStateException.class, ex);
        assertTrue(ex.getMessage().contains("ID-200"), "Message must include duplicate identity");
        assertTrue(ex.getMessage().contains("Patient"), "Message must include entity simple class name");
    }

    @Test
    @DisplayName("addIfAbsent returns false on duplicate and keeps size unchanged")
    void addIfAbsentReturnsFalseOnDuplicateWithoutChangingSize() {
        GenericRepository<Doctor> repo = new GenericRepository<>(Doctor.class, d -> d.licenseNumber());
        Doctor first = Doctor.of("DOC-10", "Dr. Petrivna", Specialization.CARDIOLOGY);
        Doctor duplicate = Doctor.of("DOC-10", "Dr. Other", Specialization.NEUROLOGY);

        assertTrue(repo.addIfAbsent(first));
        assertFalse(repo.addIfAbsent(duplicate));
        assertEquals(1, repo.size());
        assertEquals("Dr. Petrivna", repo.findByIdentity("DOC-10").orElseThrow().name());
    }

    @Test
    @DisplayName("removeByIdentity removes existing element and returns false for missing element")
    void removeByIdentityHandlesExistingAndMissingElements() {
        GenericRepository<Doctor> repo = new GenericRepository<>(Doctor.class, d -> d.licenseNumber());
        Doctor doctor = Doctor.of("DOC-20", "Dr. Melnyk", Specialization.PEDIATRICS);
        repo.add(doctor);

        assertTrue(repo.removeByIdentity("DOC-20"));
        assertEquals(0, repo.size());
        assertTrue(repo.findByIdentity("DOC-20").isEmpty());

        assertFalse(repo.removeByIdentity("DOC-20"));
        assertFalse(repo.removeByIdentity("DOC-999"));
    }

    @Test
    @DisplayName("Modifying the list returned by findAll does not affect the repository")
    void findAllReturnsUnmodifiableSnapshot() {
        GenericRepository<Patient> repo = new GenericRepository<>(Patient.class, p -> p.nationalId());
        Patient p1 = Patient.of("ID-1", "Patient One", LocalDate.of(1991, 1, 1));
        Patient p2 = Patient.of("ID-2", "Patient Two", LocalDate.of(1992, 2, 2));
        repo.addAll(List.of(p1, p2));

        List<Patient> snapshot = repo.findAll();

        assertEquals(2, snapshot.size());
        assertThrows(UnsupportedOperationException.class, snapshot::clear);
        assertEquals(2, repo.size());
        assertTrue(repo.findByIdentity("ID-1").isPresent());
    }

    @Test
    @DisplayName("Same repository class works with domain records and mutable class")
    void sameRepositoryWorksWithMultipleTypes() {
        GenericRepository<Patient> patientRepo =
                new GenericRepository<>(Patient.class, p -> p.nationalId());
        GenericRepository<Doctor> doctorRepo =
                new GenericRepository<>(Doctor.class, d -> d.licenseNumber());
        IdentityExtractor<Appointment> appointmentExtractor =
                a -> a.getDoctor().licenseNumber() + "|" + a.getAppointmentDate() + "|" + a.getStartTime();
        GenericRepository<Appointment> appointmentRepo =
                new GenericRepository<>(Appointment.class, appointmentExtractor);
        GenericRepository<MedicalRecord> recordRepo =
                new GenericRepository<>(MedicalRecord.class, r -> r.recordId());

        Patient patient = Patient.of("ID-300", "Petro Melnyk", LocalDate.of(2001, 3, 10));
        Doctor doctor = Doctor.of("DOC-300", "Nataliia Lysenko", Specialization.DENTISTRY);
        LocalDate date = LocalDate.of(2026, 12, 5);
        LocalTime start = LocalTime.of(11, 0);
        Appointment appointment = new Appointment(
                patient, doctor, date, start, LocalTime.of(11, 30), AppointmentStatus.SCHEDULED);
        MedicalRecord record = new MedicalRecord("REC-9001", appointment, "Healthy", 350.0);

        patientRepo.add(patient);
        doctorRepo.add(doctor);
        appointmentRepo.add(appointment);
        appointment.setStatus(AppointmentStatus.COMPLETED);
        recordRepo.add(record);

        String apptIdentity = appointmentExtractor.identityOf(appointment);
        assertEquals(patient, patientRepo.findByIdentity("ID-300").orElseThrow());
        assertEquals(doctor, doctorRepo.findByIdentity("DOC-300").orElseThrow());
        assertEquals(AppointmentStatus.COMPLETED, appointmentRepo.findByIdentity(apptIdentity).orElseThrow().getStatus());
        assertEquals(record, recordRepo.findByIdentity("REC-9001").orElseThrow());
    }

    @Test
    @DisplayName("add throws IllegalArgumentException for null item or blank identity")
    void addRejectsNullItemAndBlankIdentity() {
        GenericRepository<Patient> blankIdentityRepo = new GenericRepository<>(Patient.class, p -> "   ");
        Patient patient = Patient.of("ID-400", "Test User", LocalDate.of(1999, 4, 4));

        assertThrows(IllegalArgumentException.class, () -> blankIdentityRepo.add(null));
        assertThrows(IllegalArgumentException.class, () -> blankIdentityRepo.add(patient));
        assertThrows(IllegalArgumentException.class, () -> blankIdentityRepo.addIfAbsent(null));
    }

    @Test
    @DisplayName("Wildcard methods addAll and filtered findAll support subtype collections and supertype predicates")
    void wildcardMethodsWorkWithSubtypesAndSupertypePredicates() {
        GenericRepository<Record> recordRepo = new GenericRepository<>(Record.class, r -> r.toString());
        Collection<Patient> patients = List.of(
                Patient.of("ID-501", "Alice", LocalDate.of(1990, 1, 1)),
                Patient.of("ID-502", "Bob", LocalDate.of(2005, 5, 5))
        );
        recordRepo.addAll(patients);
        assertEquals(2, recordRepo.size());

        GenericRepository<Patient> patientRepo = new GenericRepository<>(Patient.class, p -> p.nationalId());
        patientRepo.addAll(patients);

        Predicate<Object> matchesAlice = obj -> obj.toString().contains("Alice");
        List<Patient> filtered = patientRepo.findAll(matchesAlice);

        assertEquals(1, filtered.size());
        assertEquals("ID-501", filtered.get(0).nationalId());
        assertThrows(UnsupportedOperationException.class, filtered::clear);
        assertThrows(IllegalArgumentException.class, () -> patientRepo.findAll(null));
    }
}
