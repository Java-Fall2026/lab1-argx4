package ua.clinic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.clinic.exception.DuplicateIdentityException;
import ua.clinic.io.DoctorLoader;
import ua.clinic.io.PatientLoader;
import ua.clinic.model.Appointment;
import ua.clinic.model.AppointmentStatus;
import ua.clinic.model.Doctor;
import ua.clinic.model.Patient;
import ua.clinic.repository.GenericRepository;
import ua.clinic.repository.IdentityExtractor;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        log.info("Application started");

        // 1. Fill Patient repository from CSV file (including a duplicate identity row in CSV)
        System.out.println("=== 1. Наповнення репозиторію пацієнтів із CSV-файла ===");
        GenericRepository<Patient> patientRepo =
                new GenericRepository<>(Patient.class, p -> p.nationalId());

        List<Patient> csvPatients = PatientLoader.loadFromCsv(Path.of("data/patients.csv"));
        for (Patient p : csvPatients) {
            try {
                patientRepo.add(p);
            } catch (DuplicateIdentityException e) {
                // Error level is already logged inside GenericRepository.add before throwing
                System.out.println("Перехоплено дублікат із CSV: " + e.getMessage());
            }
        }
        System.out.println("Унікальних пацієнтів у репозиторії: " + patientRepo.size());

        // 2. Create repositories for Doctor and Appointment (mutable status class)
        System.out.println("\n=== 2. Репозиторії інших типів (Doctor та Appointment) ===");
        GenericRepository<Doctor> doctorRepo =
                new GenericRepository<>(Doctor.class, d -> d.licenseNumber());
        List<Doctor> csvDoctors = DoctorLoader.loadFromCsv(Path.of("data/doctors.csv"));
        doctorRepo.addAll(csvDoctors);
        System.out.println("Лікарів у репозиторії: " + doctorRepo.size());

        IdentityExtractor<Appointment> appointmentExtractor =
                a -> a.getDoctor().licenseNumber() + "|" + a.getAppointmentDate() + "|" + a.getStartTime();
        GenericRepository<Appointment> appointmentRepo =
                new GenericRepository<>(Appointment.class, appointmentExtractor);

        Patient samplePatient = patientRepo.findByIdentity("ID-001").orElseThrow();
        Doctor sampleDoctor = doctorRepo.findByIdentity("DOC-101").orElseThrow();
        LocalDate visitDate = LocalDate.now().plusDays(1);
        LocalTime startTime = LocalTime.of(10, 0);
        LocalTime endTime = LocalTime.of(10, 30);

        Appointment appointment = new Appointment(
                samplePatient, sampleDoctor, visitDate, startTime, endTime, AppointmentStatus.SCHEDULED);
        appointmentRepo.add(appointment);

        // Mutate non-identity field (status) after adding to repository
        appointment.setStatus(AppointmentStatus.COMPLETED);
        String apptKey = appointmentExtractor.identityOf(appointment);
        System.out.println("Записів на прийом у репозиторії: " + appointmentRepo.size());

        // 3. Search by identity across all 3 repositories (found and not found)
        System.out.println("\n=== 3. Пошук за identity у трьох репозиторіях ===");
        printSearchResult("Пацієнт ID-002", patientRepo.findByIdentity("ID-002"));
        printSearchResult("Пацієнт ID-999", patientRepo.findByIdentity("ID-999"));

        printSearchResult("Лікар DOC-102", doctorRepo.findByIdentity("DOC-102"));
        printSearchResult("Лікар DOC-999", doctorRepo.findByIdentity("DOC-999"));

        printSearchResult("Прийом (після зміни статусу)", appointmentRepo.findByIdentity(apptKey));
        printSearchResult("Прийом неіснуючий", appointmentRepo.findByIdentity("DOC-000|2026-01-01|00:00"));

        // 4. Duplicate handling: add (throws) vs addIfAbsent (returns false)
        System.out.println("\n=== 4. Обробка дубліката: add vs addIfAbsent ===");
        Patient duplicatePatient = Patient.of("ID-002", "Олена Сидоренко (повтор)", LocalDate.of(1988, 11, 23));

        try {
            patientRepo.add(duplicatePatient);
        } catch (DuplicateIdentityException e) {
            System.out.println("add() кинув виключення: " + e.getMessage());
        }

        boolean addedIfAbsent = patientRepo.addIfAbsent(duplicatePatient);
        System.out.println("addIfAbsent() повернув: " + addedIfAbsent
                + " (розмір репозиторію залишився " + patientRepo.size() + ")");

        // 5. Removal of existing and missing elements
        System.out.println("\n=== 5. Видалення наявного та відсутнього елемента ===");
        boolean removedExisting = patientRepo.removeByIdentity("ID-003");
        System.out.println("Видалення ID-003: " + (removedExisting ? "успішно" : "не знайдено"));

        boolean removedMissing = patientRepo.removeByIdentity("ID-404");
        System.out.println("Видалення ID-404: " + (removedMissing ? "успішно" : "не знайдено"));

        System.out.println("\nПідсумкова кількість пацієнтів у репозиторії: " + patientRepo.size());
        for (Patient p : patientRepo.findAll()) {
            System.out.println("  " + p);
        }

        log.info("Application finished");
    }

    private static <T> void printSearchResult(String label, Optional<T> result) {
        if (result.isPresent()) {
            System.out.println(label + " -> знайдено: " + result.get());
        } else {
            System.out.println(label + " -> не знайдено (порожній Optional)");
        }
    }
}
