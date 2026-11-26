package ua.clinic.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.clinic.model.MedicalRecord;
import ua.clinic.model.Specialization;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class MedicalRecordRepository extends GenericRepository<MedicalRecord> {

    private static final Logger log = LoggerFactory.getLogger(MedicalRecordRepository.class);

    public MedicalRecordRepository() {
        super(MedicalRecord.class, MedicalRecord::recordId);
    }

    public List<MedicalRecord> findAllSortedNaturally() {
        List<MedicalRecord> sorted = findAllSorted(Comparator.naturalOrder());
        log.info("Sorted MedicalRecord by natural order (treatmentCost, recordId, ...): returned {} elements",
                sorted.size());
        return sorted;
    }

    public List<MedicalRecord> findAllSortedByTreatmentCost() {
        Comparator<MedicalRecord> byCost = (a, b) -> Double.compare(a.treatmentCost(), b.treatmentCost());
        List<MedicalRecord> sorted = findAllSorted(byCost.thenComparing(MedicalRecord::recordId));
        log.info("Sorted MedicalRecord by treatmentCost ASC, recordId ASC: returned {} elements", sorted.size());
        return sorted;
    }

    public List<MedicalRecord> findAllSortedBySpecializationThenCostDesc() {
        Comparator<MedicalRecord> comparator = Comparator
                .comparing((MedicalRecord r) -> r.appointment().getDoctor().specialization())
                .thenComparing(Comparator.comparingDouble(MedicalRecord::treatmentCost).reversed())
                .thenComparing(MedicalRecord::recordId);
        List<MedicalRecord> sorted = findAllSorted(comparator);
        log.info("Sorted MedicalRecord by specialization ASC, treatmentCost DESC, recordId ASC: returned {} elements",
                sorted.size());
        return sorted;
    }

    public List<MedicalRecord> findByDiagnosisContaining(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            log.error("Diagnosis search keyword must not be null or blank");
            throw new IllegalArgumentException("keyword must not be null or blank");
        }
        String normalized = keyword.trim().toLowerCase(Locale.ROOT);
        List<MedicalRecord> result = stream()
                .filter(r -> r.diagnosis().toLowerCase(Locale.ROOT).contains(normalized))
                .sorted()
                .toList();
        logSearchResult("diagnosis contains '" + keyword.trim() + "'", result.size());
        return result;
    }

    public List<MedicalRecord> findByTreatmentCostRange(double minCost, double maxCost) {
        if (Double.isNaN(minCost) || Double.isNaN(maxCost) || minCost < 0 || maxCost < 0) {
            log.error("Invalid cost range bounds: minCost={}, maxCost={}", minCost, maxCost);
            throw new IllegalArgumentException("cost bounds must be non-negative numbers");
        }
        if (Double.compare(minCost, maxCost) > 0) {
            log.error("Invalid cost range: minCost {} is greater than maxCost {}", minCost, maxCost);
            throw new IllegalArgumentException("minCost must not be greater than maxCost");
        }
        List<MedicalRecord> result = stream()
                .filter(r -> Double.compare(r.treatmentCost(), minCost) >= 0
                        && Double.compare(r.treatmentCost(), maxCost) <= 0)
                .sorted(Comparator.comparingDouble(MedicalRecord::treatmentCost)
                        .thenComparing(MedicalRecord::recordId))
                .toList();
        logSearchResult("treatmentCost in [" + minCost + ", " + maxCost + "]", result.size());
        return result;
    }

    public List<MedicalRecord> findBySpecializationAndMaxCost(Specialization specialization, double maxCost) {
        if (specialization == null) {
            log.error("Specialization must not be null for combined search");
            throw new IllegalArgumentException("specialization must not be null");
        }
        if (Double.isNaN(maxCost) || maxCost < 0) {
            log.error("maxCost must be a non-negative number, got {}", maxCost);
            throw new IllegalArgumentException("maxCost must be a non-negative number");
        }
        List<MedicalRecord> result = stream()
                .filter(r -> r.appointment().getDoctor().specialization() == specialization
                        && Double.compare(r.treatmentCost(), maxCost) <= 0)
                .sorted()
                .toList();
        logSearchResult("specialization == " + specialization + " AND treatmentCost <= " + maxCost, result.size());
        return result;
    }

    public List<String> findDistinctDiagnoses() {
        List<String> diagnoses = stream()
                .map(MedicalRecord::diagnosis)
                .distinct()
                .sorted()
                .toList();
        logSearchResult("distinct diagnoses", diagnoses.size());
        return diagnoses;
    }

    public List<MedicalRecord> findTopExpensiveRecords(int limit) {
        if (limit < 0) {
            log.error("Limit for top expensive records must be non-negative, got {}", limit);
            throw new IllegalArgumentException("limit must be non-negative");
        }
        List<MedicalRecord> result = stream()
                .sorted(Comparator.comparingDouble(MedicalRecord::treatmentCost)
                        .reversed()
                        .thenComparing(MedicalRecord::recordId))
                .limit(limit)
                .toList();
        logSearchResult("top " + limit + " most expensive records", result.size());
        return result;
    }

    public Optional<MedicalRecord> findMostExpensiveBySpecialization(Specialization specialization) {
        if (specialization == null) {
            log.error("Specialization must not be null for single-record search");
            throw new IllegalArgumentException("specialization must not be null");
        }
        Optional<MedicalRecord> found = stream()
                .filter(r -> r.appointment().getDoctor().specialization() == specialization)
                .sorted(Comparator.comparingDouble(MedicalRecord::treatmentCost)
                        .reversed()
                        .thenComparing(MedicalRecord::recordId))
                .findFirst();
        found.ifPresentOrElse(
                record -> log.info("Search most expensive MedicalRecord for specialization {}: found 1 match ({})",
                        specialization, record.recordId()),
                () -> log.warn("Search most expensive MedicalRecord for specialization {}: no match found",
                        specialization));
        return found;
    }

    public Map<Specialization, List<MedicalRecord>> groupBySpecialization() {
        Map<Specialization, List<MedicalRecord>> grouped = stream()
                .collect(Collectors.groupingBy(r -> r.appointment().getDoctor().specialization()));
        if (grouped.isEmpty()) {
            log.warn("Grouped MedicalRecord by specialization: repository is empty (0 groups)");
        } else {
            log.info("Grouped MedicalRecord by specialization: {} groups", grouped.size());
        }
        return grouped;
    }

    public Map<Specialization, Double> averageCostBySpecialization() {
        Map<Specialization, Double> averages = stream()
                .collect(Collectors.groupingBy(
                        r -> r.appointment().getDoctor().specialization(),
                        Collectors.averagingDouble(MedicalRecord::treatmentCost)));
        if (averages.isEmpty()) {
            log.warn("Calculated average treatmentCost by specialization: repository is empty (0 groups)");
        } else {
            log.info("Calculated average treatmentCost by specialization: {} groups", averages.size());
        }
        return averages;
    }

    public double calculateTotalTreatmentCost() {
        double total = stream()
                .map(MedicalRecord::treatmentCost)
                .reduce(0.0, Double::sum);
        if (size() == 0) {
            log.warn("Calculated total treatmentCost on empty MedicalRecord repository: 0.00");
        } else {
            log.info("Calculated total treatmentCost across {} MedicalRecord elements: {}",
                    size(), String.format(Locale.ROOT, "%.2f", total));
        }
        return total;
    }

    private void logSearchResult(String criterion, int count) {
        if (count == 0) {
            log.warn("Search MedicalRecord by [{}]: found 0 elements", criterion);
        } else {
            log.info("Search MedicalRecord by [{}]: found {} elements", criterion, count);
        }
    }
}
