package ua.clinic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.clinic.exception.DuplicateIdentityException;
import ua.clinic.io.DoctorLoader;
import ua.clinic.io.PatientLoader;
import ua.clinic.model.Appointment;
import ua.clinic.model.AppointmentStatus;
import ua.clinic.model.Doctor;
import ua.clinic.model.MedicalRecord;
import ua.clinic.model.Patient;
import ua.clinic.model.Specialization;
import ua.clinic.repository.GenericRepository;
import ua.clinic.repository.MedicalRecordRepository;
import ua.clinic.repository.PatientRepository;
import ua.clinic.repository.SortOrder;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        log.info("Lab 5 application started");

        System.out.println("=== 1. Наповнення доменних репозиторіїв ===");
        PatientRepository patientRepo = new PatientRepository();
        List<Patient> csvPatients = PatientLoader.loadFromCsv(Path.of("data/patients.csv"));
        csvPatients.forEach(patient -> {
            try {
                patientRepo.add(patient);
            } catch (DuplicateIdentityException e) {
                System.out.println("Перехоплено дублікат пацієнта із CSV: " + e.getMessage());
            }
        });
        patientRepo.addIfAbsent(Patient.of("ID-009", "Аліна Коваленко", LocalDate.of(1995, 5, 14)));
        patientRepo.addIfAbsent(Patient.of("ID-010", "Богдан Савченко", LocalDate.of(2001, 3, 10)));
        System.out.println("Пацієнтів у PatientRepository: " + patientRepo.size());

        GenericRepository<Doctor> doctorRepo = new GenericRepository<>(Doctor.class, Doctor::licenseNumber);
        doctorRepo.addAll(DoctorLoader.loadFromCsv(Path.of("data/doctors.csv")));

        MedicalRecordRepository recordRepo = populateMedicalRecords(patientRepo, doctorRepo);
        System.out.println("Медичних записів у MedicalRecordRepository: " + recordRepo.size());

        System.out.println("\n=== 2. Сортування за природним порядком і доменними критеріями ===");
        List<Patient> mutablePatients = new ArrayList<>(patientRepo.findAll());
        Collections.sort(mutablePatients);
        System.out.println("Пацієнти після Collections.sort (природний порядок — ПІБ, дата, ID):");
        mutablePatients.forEach(p -> System.out.println("  " + p));

        System.out.println("\nПацієнти за ПІБ (ASC) через PatientRepository.findAllSortedByFullName:");
        patientRepo.findAllSortedByFullName()
                .forEach(p -> System.out.println("  " + p.fullName() + " (" + p.nationalId() + ")"));

        System.out.println("\nПацієнти за датою народження (DESC) і ПІБ (ASC) [багаторівневе]:");
        patientRepo.findAllSortedByBirthDateDescThenName()
                .forEach(p -> System.out.println("  " + p.birthDate() + " | " + p.fullName() + " (" + p.nationalId() + ")"));

        System.out.println("\nМедичні записи за вартістю лікування (ASC, дробові значення):");
        recordRepo.findAllSortedByTreatmentCost()
                .forEach(r -> System.out.printf("  %s | %.2f грн | %s%n",
                        r.recordId(), r.treatmentCost(), r.diagnosis()));

        System.out.println("\nМедичні записи за спеціалізацією (ASC) і вартістю (DESC) [багаторівневе]:");
        recordRepo.findAllSortedBySpecializationThenCostDesc()
                .forEach(r -> System.out.printf("  %-10s | %7.2f грн | %s (%s)%n",
                        r.appointment().getDoctor().specialization(), r.treatmentCost(), r.recordId(), r.diagnosis()));

        System.out.println("\n=== 3. Сортування за identity (SortOrder.ASC та SortOrder.DESC) ===");
        System.out.println("Пацієнти за identity ASC:");
        patientRepo.findAllSortedByIdentity(SortOrder.ASC)
                .forEach(p -> System.out.println("  " + p.nationalId() + " — " + p.fullName()));

        System.out.println("Пацієнти за identity DESC:");
        patientRepo.findAllSortedByIdentity(SortOrder.DESC)
                .forEach(p -> System.out.println("  " + p.nationalId() + " — " + p.fullName()));

        System.out.println("\n=== 4. Поведінка TreeSet із компаратором, не узгодженим з equals ===");
        Comparator<Patient> byBirthDateOnly = Comparator.comparing(Patient::birthDate);
        Set<Patient> patientsByBirthDate = new TreeSet<>(byBirthDateOnly);

        Patient p1 = Patient.of("ID-001", "Іван Коваленко", LocalDate.of(1995, 5, 14));
        Patient p2 = Patient.of("ID-009", "Аліна Коваленко", LocalDate.of(1995, 5, 14));
        patientsByBirthDate.add(p1);
        patientsByBirthDate.add(p2);

        System.out.println("p1.equals(p2) = " + p1.equals(p2));
        System.out.println("comparator.compare(p1, p2) = " + byBirthDateOnly.compare(p1, p2));
        System.out.println("Розмір TreeSet після додавання двох різних пацієнтів з однією датою народження: "
                + patientsByBirthDate.size());
        patientsByBirthDate.forEach(p -> System.out.println("  Збережений елемент: " + p));

        System.out.println("\n=== 5. Пошук через Stream API за всіма критеріями ===");
        System.out.println("Пацієнти з 'Коваленко' у ПІБ:");
        patientRepo.findByFullNameContaining("коваленко")
                .forEach(p -> System.out.println("  " + p));

        System.out.println("Пацієнти, народжені у [1985-01-01, 1996-12-31]:");
        patientRepo.findByBirthDateRange(LocalDate.of(1985, 1, 1), LocalDate.of(1996, 12, 31))
                .forEach(p -> System.out.println("  " + p.birthDate() + " — " + p.fullName()));

        System.out.println("Пацієнти з 'Коваленко' у ПІБ і датою народження >= 1990-01-01 (2 умови):");
        patientRepo.findByNameContainingAndBornFrom("Коваленко", LocalDate.of(1990, 1, 1))
                .forEach(p -> System.out.println("  " + p));

        System.out.println("Унікальні роки народження пацієнтів (map + distinct + sorted): "
                + patientRepo.findDistinctBirthYears());
        System.out.println("Топ-2 найстарших пацієнти (sorted + limit):");
        patientRepo.findOldestPatients(2)
                .forEach(p -> System.out.println("  " + p.birthDate() + " — " + p.fullName()));

        System.out.println("\nМедичні записи з діагнозом, що містить 'огляд':");
        recordRepo.findByDiagnosisContaining("огляд")
                .forEach(r -> System.out.printf("  %s | %.2f грн | %s%n",
                        r.recordId(), r.treatmentCost(), r.diagnosis()));

        System.out.println("Медичні записи з вартістю у [400.00, 600.00]:");
        recordRepo.findByTreatmentCostRange(400.0, 600.0)
                .forEach(r -> System.out.printf("  %s | %.2f грн | %s%n",
                        r.recordId(), r.treatmentCost(), r.diagnosis()));

        System.out.println("Кардіологія та вартість <= 500.00 (2 умови):");
        recordRepo.findBySpecializationAndMaxCost(Specialization.CARDIOLOGY, 500.0)
                .forEach(r -> System.out.printf("  %s | %.2f грн | %s%n",
                        r.recordId(), r.treatmentCost(), r.diagnosis()));

        System.out.println("Унікальні діагнози (map + distinct + sorted): " + recordRepo.findDistinctDiagnoses());
        System.out.println("Топ-3 найдорожчі медичні записи (sorted + limit):");
        recordRepo.findTopExpensiveRecords(3)
                .forEach(r -> System.out.printf("  %s | %.2f грн | %s%n",
                        r.recordId(), r.treatmentCost(), r.diagnosis()));

        System.out.println("\n=== 6. Робота з Optional (orElse, orElseGet, orElseThrow, ifPresent, map) ===");
        patientRepo.findFirstByFullNameIgnoreCase("Олена Сидоренко")
                .ifPresent(p -> System.out.println("ifPresent -> знайдено пацієнта: " + p.fullName()
                        + " (" + p.nationalId() + ")"));

        String missingPatientMessage = patientRepo.findFirstByFullNameIgnoreCase("Неіснуючий Пацієнт")
                .map(Patient::fullName)
                .orElse("Пацієнта з таким ПІБ не знайдено");
        System.out.println("map + orElse -> " + missingPatientMessage);

        MedicalRecord topCardiology = recordRepo.findMostExpensiveBySpecialization(Specialization.CARDIOLOGY)
                .orElseThrow(() -> new IllegalStateException("Expected at least one cardiology record"));
        System.out.printf("orElseThrow -> найдорожчий запис CARDIOLOGY: %s (%.2f грн)%n",
                topCardiology.recordId(), topCardiology.treatmentCost());

        String missingPediatrics = recordRepo.findMostExpensiveBySpecialization(Specialization.PEDIATRICS)
                .map(r -> r.recordId() + " (" + r.treatmentCost() + " грн)")
                .orElseGet(() -> "Записів для спеціалізації PEDIATRICS наразі немає");
        System.out.println("map + orElseGet -> " + missingPediatrics);

        System.out.println("\n=== 7. Групування та агрегація ===");
        Map<Specialization, List<MedicalRecord>> groupedBySpec = recordRepo.groupBySpecialization();
        System.out.println("Розподіл медичних записів за спеціалізацією (groupingBy):");
        groupedBySpec.forEach((spec, records) ->
                System.out.println("  " + spec + ": " + records.size() + " запис(ів)"));

        Map<Specialization, Double> avgCostBySpec = recordRepo.averageCostBySpecialization();
        System.out.println("Середня вартість лікування за спеціалізацією (groupingBy + averagingDouble):");
        avgCostBySpec.forEach((spec, avg) ->
                System.out.printf("  %s: %.2f грн%n", spec, avg));

        Map<Integer, Long> patientsPerDecade = patientRepo.countByBirthDecade();
        System.out.println("Кількість пацієнтів за десятиліттями народження (groupingBy + counting):");
        patientsPerDecade.forEach((decade, count) ->
                System.out.println("  " + decade + "-ті: " + count));

        double totalTreatmentCost = recordRepo.calculateTotalTreatmentCost();
        System.out.printf("Загальна вартість усіх медичних записів (reduce): %.2f грн%n", totalTreatmentCost);

        System.out.println("\n=== 8. Одноразовість стріму (Stream<T>) ===");
        Stream<Patient> patientStream = patientRepo.stream();
        long counted = patientStream.count();
        System.out.println("Перший прохід стріму (count): " + counted);

        try {
            patientStream.toList();
        } catch (IllegalStateException e) {
            log.error("Cannot reuse an already consumed stream: {}", e.getMessage());
            System.out.println("Другий прохід перехоплено (" + e.getClass().getSimpleName() + "): " + e.getMessage());
        }

        System.out.println("\nПрограма успішно завершила всі демонстрації ЛР5.");
        log.info("Lab 5 application finished");
    }

    private static MedicalRecordRepository populateMedicalRecords(
            PatientRepository patientRepo, GenericRepository<Doctor> doctorRepo) {
        MedicalRecordRepository recordRepo = new MedicalRecordRepository();

        Patient p1 = patientRepo.findByIdentity("ID-001").orElseThrow();
        Patient p2 = patientRepo.findByIdentity("ID-002").orElseThrow();
        Patient p3 = patientRepo.findByIdentity("ID-003").orElseThrow();
        Patient p8 = patientRepo.findByIdentity("ID-008").orElseThrow();

        Doctor cardiologist = doctorRepo.findByIdentity("DOC-101").orElseThrow();
        Doctor neurologist = doctorRepo.findByIdentity("DOC-102").orElseThrow();
        Doctor dentist = doctorRepo.findByIdentity("DOC-107").orElseThrow();

        LocalDate baseDate = LocalDate.of(2026, 11, 20);

        Appointment a1 = new Appointment(p1, cardiologist, baseDate,
                LocalTime.of(9, 0), LocalTime.of(9, 30), AppointmentStatus.COMPLETED);
        Appointment a2 = new Appointment(p2, cardiologist, baseDate,
                LocalTime.of(10, 0), LocalTime.of(10, 30), AppointmentStatus.COMPLETED);
        Appointment a3 = new Appointment(p3, cardiologist, baseDate.plusDays(1),
                LocalTime.of(11, 0), LocalTime.of(11, 45), AppointmentStatus.COMPLETED);
        Appointment a4 = new Appointment(p2, neurologist, baseDate.plusDays(1),
                LocalTime.of(12, 0), LocalTime.of(12, 30), AppointmentStatus.COMPLETED);
        Appointment a5 = new Appointment(p8, neurologist, baseDate.plusDays(2),
                LocalTime.of(14, 0), LocalTime.of(14, 40), AppointmentStatus.COMPLETED);
        Appointment a6 = new Appointment(p1, dentist, baseDate.plusDays(2),
                LocalTime.of(15, 0), LocalTime.of(15, 45), AppointmentStatus.COMPLETED);
        Appointment a7 = new Appointment(p3, dentist, baseDate.plusDays(3),
                LocalTime.of(16, 0), LocalTime.of(16, 30), AppointmentStatus.COMPLETED);

        recordRepo.addAll(List.of(
                new MedicalRecord("REC-104", a1, "Профілактичний огляд", 450.80),
                new MedicalRecord("REC-101", a2, "Аритмія легкого ступеня", 450.25),
                new MedicalRecord("REC-105", a3, "Гіпертонія (контрольний огляд)", 1250.50),
                new MedicalRecord("REC-102", a4, "Мігрень хронічна", 780.00),
                new MedicalRecord("REC-106", a5, "Невралгія трійчастого нерва", 920.40),
                new MedicalRecord("REC-103", a6, "Профілактичний огляд", 320.00),
                new MedicalRecord("REC-107", a7, "Лікування карієсу", 590.75)
        ));

        return recordRepo;
    }
}
