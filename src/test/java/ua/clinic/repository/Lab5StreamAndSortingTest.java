package ua.clinic.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ua.clinic.model.Appointment;
import ua.clinic.model.AppointmentStatus;
import ua.clinic.model.Doctor;
import ua.clinic.model.MedicalRecord;
import ua.clinic.model.Patient;
import ua.clinic.model.Specialization;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Lab 5 — Comparable, Comparator, and Stream API Tests")
class Lab5StreamAndSortingTest {

    private PatientRepository patientRepo;
    private MedicalRecordRepository recordRepo;

    private Patient pIvan;
    private Patient pAlina;
    private Patient pOlena;
    private Patient pPetro;

    private Doctor docCardio;
    private Doctor docNeuro;
    private Doctor docDental;

    private MedicalRecord recCardioCheap;
    private MedicalRecord recCardioMid;
    private MedicalRecord recCardioExpensive;
    private MedicalRecord recNeuro;
    private MedicalRecord recDental;

    @BeforeEach
    void setUp() {
        patientRepo = new PatientRepository();
        recordRepo = new MedicalRecordRepository();

        pIvan = Patient.of("ID-003", "Ivan Kovalenko", LocalDate.of(1995, 5, 14));
        pAlina = Patient.of("ID-001", "Alina Kovalenko", LocalDate.of(1995, 5, 14));
        pOlena = Patient.of("ID-002", "Olena Sydorenko", LocalDate.of(1988, 11, 23));
        pPetro = Patient.of("ID-004", "Petro Melnyk", LocalDate.of(2001, 3, 10));

        patientRepo.addAll(List.of(pIvan, pAlina, pOlena, pPetro));

        docCardio = Doctor.of("DOC-101", "Dr. Petrivna", Specialization.CARDIOLOGY);
        docNeuro = Doctor.of("DOC-102", "Dr. Kovalchuk", Specialization.NEUROLOGY);
        docDental = Doctor.of("DOC-107", "Dr. Oliinyk", Specialization.DENTISTRY);

        LocalDate date = LocalDate.of(2026, 11, 20);
        Appointment a1 = new Appointment(pIvan, docCardio, date,
                LocalTime.of(9, 0), LocalTime.of(9, 30), AppointmentStatus.COMPLETED);
        Appointment a2 = new Appointment(pAlina, docCardio, date,
                LocalTime.of(10, 0), LocalTime.of(10, 30), AppointmentStatus.COMPLETED);
        Appointment a3 = new Appointment(pOlena, docCardio, date.plusDays(1),
                LocalTime.of(11, 0), LocalTime.of(11, 30), AppointmentStatus.COMPLETED);
        Appointment a4 = new Appointment(pPetro, docNeuro, date.plusDays(1),
                LocalTime.of(12, 0), LocalTime.of(12, 30), AppointmentStatus.COMPLETED);
        Appointment a5 = new Appointment(pIvan, docDental, date.plusDays(2),
                LocalTime.of(14, 0), LocalTime.of(14, 30), AppointmentStatus.COMPLETED);

        recCardioMid = new MedicalRecord("REC-202", a1, "Heart checkup", 450.80);
        recCardioCheap = new MedicalRecord("REC-201", a2, "Mild arrhythmia", 450.25);
        recCardioExpensive = new MedicalRecord("REC-205", a3, "Hypertension care", 1200.00);
        recNeuro = new MedicalRecord("REC-203", a4, "Migraine treatment", 780.00);
        recDental = new MedicalRecord("REC-204", a5, "Heart checkup", 320.00);

        recordRepo.addAll(List.of(recCardioMid, recCardioCheap, recCardioExpensive, recNeuro, recDental));
    }

    @Test
    @DisplayName("1. Natural order: Collections.sort produces expected sequence and is consistent with equals")
    void naturalOrderSortsPatientsAndRecordsConsistentlyWithEquals() {
        List<Patient> patients = new ArrayList<>(List.of(pPetro, pIvan, pOlena, pAlina));
        Collections.sort(patients);
        assertEquals(List.of(pAlina, pIvan, pOlena, pPetro), patients);

        Patient sameAsIvan = Patient.of("ID-003", "Ivan Kovalenko", LocalDate.of(1995, 5, 14));
        Patient sameNameDifferentId = Patient.of("ID-999", "Ivan Kovalenko", LocalDate.of(1995, 5, 14));
        assertEquals(0, pIvan.compareTo(sameAsIvan));
        assertEquals(pIvan, sameAsIvan);
        assertNotEquals(0, pIvan.compareTo(sameNameDifferentId));
        assertNotEquals(pIvan, sameNameDifferentId);

        List<MedicalRecord> records = new ArrayList<>(List.of(recCardioExpensive, recCardioMid, recDental, recCardioCheap));
        Collections.sort(records);
        assertEquals(List.of(recDental, recCardioCheap, recCardioMid, recCardioExpensive), records);
    }

    @Test
    @DisplayName("2. Multi-level comparator orders items with identical primary key by secondary key")
    void multiLevelComparatorOrdersTiesBySecondaryKey() {
        List<Patient> byBirthDescThenName = patientRepo.findAllSortedByBirthDateDescThenName();
        assertEquals(List.of(pPetro, pAlina, pIvan, pOlena), byBirthDescThenName);

        List<MedicalRecord> bySpecThenCostDesc = recordRepo.findAllSortedBySpecializationThenCostDesc();
        assertEquals(
                List.of(recCardioExpensive, recCardioMid, recCardioCheap, recNeuro, recDental),
                bySpecThenCostDesc);
    }

    @Test
    @DisplayName("3. findAllSortedByIdentity in ASC and DESC return mutually reversed lists")
    void findAllSortedByIdentityAscAndDescAreMutuallyReversed() {
        List<Patient> asc = patientRepo.findAllSortedByIdentity(SortOrder.ASC);
        List<Patient> desc = patientRepo.findAllSortedByIdentity(SortOrder.DESC);

        assertEquals(List.of(pAlina, pOlena, pIvan, pPetro), asc);
        assertEquals(asc.reversed(), desc);

        List<MedicalRecord> recAsc = recordRepo.findAllSortedByIdentity(SortOrder.ASC);
        List<MedicalRecord> recDesc = recordRepo.findAllSortedByIdentity(SortOrder.DESC);
        assertEquals(recAsc.reversed(), recDesc);
    }

    @Test
    @DisplayName("4. Two records with fractional cost difference less than 1.0 sort accurately")
    void fractionalCostDifferenceLessThanOneSortsCorrectly() {
        MedicalRecordRepository smallRepo = new MedicalRecordRepository();
        smallRepo.add(recCardioMid);
        smallRepo.add(recCardioCheap);

        assertTrue(recCardioCheap.compareTo(recCardioMid) < 0);
        assertTrue(recCardioMid.compareTo(recCardioCheap) > 0);
        assertEquals(List.of(recCardioCheap, recCardioMid), smallRepo.findAllSortedByTreatmentCost());
        assertEquals(List.of(recCardioCheap, recCardioMid), smallRepo.findAllSortedNaturally());
    }

    @Test
    @DisplayName("5. TreeSet with comparator inconsistent with equals drops second distinct object with equal key")
    void treeSetWithInconsistentComparatorTreatsDistinctEqualKeyObjectsAsDuplicate() {
        Comparator<Patient> birthDateOnly = Comparator.comparing(Patient::birthDate);
        Set<Patient> treeSet = new TreeSet<>(birthDateOnly);

        assertNotEquals(pAlina, pIvan);
        assertEquals(0, birthDateOnly.compare(pAlina, pIvan));

        assertTrue(treeSet.add(pAlina));
        assertFalse(treeSet.add(pIvan));
        assertEquals(1, treeSet.size());
        assertTrue(treeSet.contains(pAlina));
    }

    @Test
    @DisplayName("6a. PatientRepository search methods enforce exact boundaries and combined conditions")
    void patientSearchMethodsRespectTextRangeAndTwoConditions() {
        List<Patient> kovalenkos = patientRepo.findByFullNameContaining("kovalenko");
        assertEquals(List.of(pAlina, pIvan), kovalenkos);

        List<Patient> nineties = patientRepo.findByBirthDateRange(
                LocalDate.of(1990, 1, 1), LocalDate.of(1999, 12, 31));
        assertEquals(List.of(pAlina, pIvan), nineties);
        assertFalse(nineties.contains(pOlena));
        assertFalse(nineties.contains(pPetro));

        Patient olderKovalenko = Patient.of("ID-005", "Serhii Kovalenko", LocalDate.of(1980, 1, 1));
        patientRepo.add(olderKovalenko);
        List<Patient> combined = patientRepo.findByNameContainingAndBornFrom("Kovalenko", LocalDate.of(1990, 1, 1));
        assertEquals(List.of(pAlina, pIvan), combined);
        assertFalse(combined.contains(olderKovalenko), "Matches name only, not birthDate");
        assertFalse(combined.contains(pPetro), "Matches birthDate only, not name");

        assertEquals(List.of(1980, 1988, 1995, 2001), patientRepo.findDistinctBirthYears());
        assertEquals(List.of(olderKovalenko, pOlena), patientRepo.findOldestPatients(2));
    }

    @Test
    @DisplayName("6b. MedicalRecordRepository search methods enforce range bounds and two conditions")
    void medicalRecordSearchMethodsRespectTextRangeAndTwoConditions() {
        List<MedicalRecord> checkups = recordRepo.findByDiagnosisContaining("checkup");
        assertEquals(List.of(recDental, recCardioMid), checkups);

        List<MedicalRecord> midCost = recordRepo.findByTreatmentCostRange(400.0, 800.0);
        assertEquals(List.of(recCardioCheap, recCardioMid, recNeuro), midCost);
        assertFalse(midCost.contains(recDental), "320.00 is below lower bound");
        assertFalse(midCost.contains(recCardioExpensive), "1200.00 is above upper bound");

        List<MedicalRecord> affordableCardio =
                recordRepo.findBySpecializationAndMaxCost(Specialization.CARDIOLOGY, 500.0);
        assertEquals(List.of(recCardioCheap, recCardioMid), affordableCardio);
        assertFalse(affordableCardio.contains(recCardioExpensive), "Matches specialization only");
        assertFalse(affordableCardio.contains(recDental), "Matches maxCost only");

        assertEquals(
                List.of("Heart checkup", "Hypertension care", "Migraine treatment", "Mild arrhythmia"),
                recordRepo.findDistinctDiagnoses());

        assertEquals(List.of(recCardioExpensive, recNeuro), recordRepo.findTopExpensiveRecords(2));
    }

    @Test
    @DisplayName("7. Single-object search via findFirst returns Optional.empty when no match exists")
    void singleObjectSearchReturnsOptionalEmptyWhenMissing() {
        Optional<Patient> foundPatient = patientRepo.findFirstByFullNameIgnoreCase("olena sydorenko");
        assertEquals(Optional.of(pOlena), foundPatient);

        Optional<Patient> missingPatient = patientRepo.findFirstByFullNameIgnoreCase("Nobody Here");
        assertEquals(Optional.empty(), missingPatient);

        Optional<MedicalRecord> topCardio = recordRepo.findMostExpensiveBySpecialization(Specialization.CARDIOLOGY);
        assertEquals(Optional.of(recCardioExpensive), topCardio);

        Optional<MedicalRecord> missingPediatrics =
                recordRepo.findMostExpensiveBySpecialization(Specialization.PEDIATRICS);
        assertEquals(Optional.empty(), missingPediatrics);
    }

    @Test
    @DisplayName("8. groupingBy produces expected group count, group sizes, and downstream aggregations")
    void groupingByProducesExpectedGroupsAndAggregations() {
        Map<Specialization, List<MedicalRecord>> bySpec = recordRepo.groupBySpecialization();
        assertEquals(3, bySpec.size());
        assertEquals(3, bySpec.get(Specialization.CARDIOLOGY).size());
        assertEquals(1, bySpec.get(Specialization.NEUROLOGY).size());
        assertEquals(1, bySpec.get(Specialization.DENTISTRY).size());

        Map<Specialization, Double> avgCost = recordRepo.averageCostBySpecialization();
        assertEquals(3, avgCost.size());
        assertEquals((450.80 + 450.25 + 1200.00) / 3.0, avgCost.get(Specialization.CARDIOLOGY), 0.0001);
        assertEquals(780.00, avgCost.get(Specialization.NEUROLOGY), 0.0001);

        Map<Integer, List<Patient>> byDecade = patientRepo.groupByBirthDecade();
        assertEquals(3, byDecade.size());
        assertEquals(2, byDecade.get(1990).size());

        Map<Integer, Long> decadeCounts = patientRepo.countByBirthDecade();
        assertEquals(3, decadeCounts.size());
        assertEquals(1L, decadeCounts.get(1980));
        assertEquals(2L, decadeCounts.get(1990));
        assertEquals(1L, decadeCounts.get(2000));
    }

    @Test
    @DisplayName("9. Aggregation and grouping on empty repository do not fail and return meaningful values")
    void aggregationOnEmptyRepositoryReturnsSafeValues() {
        MedicalRecordRepository emptyRecords = new MedicalRecordRepository();
        PatientRepository emptyPatients = new PatientRepository();

        assertEquals(0.0, emptyRecords.calculateTotalTreatmentCost(), 0.0001);
        assertTrue(emptyRecords.groupBySpecialization().isEmpty());
        assertTrue(emptyRecords.averageCostBySpecialization().isEmpty());
        assertEquals(Optional.empty(), emptyRecords.findMostExpensiveBySpecialization(Specialization.CARDIOLOGY));

        assertTrue(emptyPatients.groupByBirthDecade().isEmpty());
        assertTrue(emptyPatients.countByBirthDecade().isEmpty());
        assertTrue(emptyPatients.findDistinctBirthYears().isEmpty());
    }

    @Test
    @DisplayName("10. Reusing a consumed Stream from GenericRepository.stream throws IllegalStateException")
    void reusingConsumedStreamThrowsIllegalStateException() {
        Stream<Patient> stream = patientRepo.stream();
        assertEquals(4, stream.count());

        assertThrows(IllegalStateException.class, stream::toList);
    }

    @Test
    @DisplayName("11. Total treatment cost reduce sums all costs accurately")
    void calculateTotalTreatmentCostSumsAllRecords() {
        double expected = 450.80 + 450.25 + 1200.00 + 780.00 + 320.00;
        assertEquals(expected, recordRepo.calculateTotalTreatmentCost(), 0.0001);
    }

    @Test
    @DisplayName("12. Sorting and search methods reject invalid arguments with IllegalArgumentException")
    void invalidArgumentsThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> patientRepo.findAllSorted(null));
        assertThrows(IllegalArgumentException.class, () -> patientRepo.findAllSortedByIdentity(null));
        assertThrows(IllegalArgumentException.class,
                () -> patientRepo.findByBirthDateRange(LocalDate.of(2000, 1, 1), LocalDate.of(1990, 1, 1)));
        assertThrows(IllegalArgumentException.class, () -> patientRepo.findOldestPatients(-1));
        assertThrows(IllegalArgumentException.class, () -> recordRepo.findByTreatmentCostRange(500.0, 100.0));
        assertThrows(IllegalArgumentException.class, () -> recordRepo.findByTreatmentCostRange(-1.0, 100.0));
        assertThrows(IllegalArgumentException.class,
                () -> recordRepo.findBySpecializationAndMaxCost(null, 500.0));
    }
}
