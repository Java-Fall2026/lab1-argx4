package ua.clinic.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.clinic.model.Patient;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class PatientRepository extends GenericRepository<Patient> {

    private static final Logger log = LoggerFactory.getLogger(PatientRepository.class);

    public PatientRepository() {
        super(Patient.class, Patient::nationalId);
    }

    public List<Patient> findAllSortedNaturally() {
        List<Patient> sorted = findAllSorted(Comparator.naturalOrder());
        log.info("Sorted Patient by natural order (fullName, birthDate, nationalId): returned {} elements",
                sorted.size());
        return sorted;
    }

    public List<Patient> findAllSortedByFullName() {
        Comparator<Patient> byName = (a, b) -> a.fullName().compareTo(b.fullName());
        List<Patient> sorted = findAllSorted(byName.thenComparing(Patient::nationalId));
        log.info("Sorted Patient by fullName ASC, nationalId ASC: returned {} elements", sorted.size());
        return sorted;
    }

    public List<Patient> findAllSortedByBirthDateDescThenName() {
        Comparator<Patient> comparator = Comparator.comparing(Patient::birthDate)
                .reversed()
                .thenComparing(Patient::fullName)
                .thenComparing(Patient::nationalId);
        List<Patient> sorted = findAllSorted(comparator);
        log.info("Sorted Patient by birthDate DESC, fullName ASC: returned {} elements", sorted.size());
        return sorted;
    }

    public List<Patient> findByFullNameContaining(String query) {
        if (query == null || query.isBlank()) {
            log.error("Name search query must not be null or blank");
            throw new IllegalArgumentException("query must not be null or blank");
        }
        String normalized = query.trim().toLowerCase(Locale.ROOT);
        List<Patient> result = stream()
                .filter(p -> p.fullName().toLowerCase(Locale.ROOT).contains(normalized))
                .sorted()
                .toList();
        logSearchResult("fullName contains '" + query.trim() + "'", result.size());
        return result;
    }

    public List<Patient> findByBirthDateRange(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            log.error("Birth date range bounds must not be null");
            throw new IllegalArgumentException("from and to dates must not be null");
        }
        if (from.isAfter(to)) {
            log.error("Invalid birth date range: from {} is after to {}", from, to);
            throw new IllegalArgumentException("from date must not be after to date");
        }
        List<Patient> result = stream()
                .filter(p -> !p.birthDate().isBefore(from) && !p.birthDate().isAfter(to))
                .sorted(Comparator.comparing(Patient::birthDate).thenComparing(Patient::nationalId))
                .toList();
        logSearchResult("birthDate in [" + from + ", " + to + "]", result.size());
        return result;
    }

    public List<Patient> findByNameContainingAndBornFrom(String query, LocalDate minBirthDate) {
        if (query == null || query.isBlank()) {
            log.error("Name query must not be null or blank for combined search");
            throw new IllegalArgumentException("query must not be null or blank");
        }
        if (minBirthDate == null) {
            log.error("minBirthDate must not be null for combined search");
            throw new IllegalArgumentException("minBirthDate must not be null");
        }
        String normalized = query.trim().toLowerCase(Locale.ROOT);
        List<Patient> result = stream()
                .filter(p -> p.fullName().toLowerCase(Locale.ROOT).contains(normalized)
                        && !p.birthDate().isBefore(minBirthDate))
                .sorted()
                .toList();
        logSearchResult("fullName contains '" + query.trim() + "' AND birthDate >= " + minBirthDate, result.size());
        return result;
    }

    public List<Integer> findDistinctBirthYears() {
        List<Integer> years = stream()
                .map(p -> p.birthDate().getYear())
                .distinct()
                .sorted()
                .toList();
        logSearchResult("distinct birth years", years.size());
        return years;
    }

    public List<Patient> findOldestPatients(int limit) {
        if (limit < 0) {
            log.error("Limit for oldest patients must be non-negative, got {}", limit);
            throw new IllegalArgumentException("limit must be non-negative");
        }
        List<Patient> result = stream()
                .sorted(Comparator.comparing(Patient::birthDate).thenComparing(Patient::nationalId))
                .limit(limit)
                .toList();
        logSearchResult("top " + limit + " oldest patients", result.size());
        return result;
    }

    public Optional<Patient> findFirstByFullNameIgnoreCase(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            log.error("Full name must not be null or blank for single-patient search");
            throw new IllegalArgumentException("fullName must not be null or blank");
        }
        String target = fullName.trim();
        Optional<Patient> found = stream()
                .filter(p -> p.fullName().equalsIgnoreCase(target))
                .findFirst();
        found.ifPresentOrElse(
                patient -> log.info("Search Patient by exact fullName '{}': found 1 match ({})",
                        target, patient.nationalId()),
                () -> log.warn("Search Patient by exact fullName '{}': no match found", target));
        return found;
    }

    public Map<Integer, List<Patient>> groupByBirthDecade() {
        Map<Integer, List<Patient>> grouped = stream()
                .collect(Collectors.groupingBy(p -> (p.birthDate().getYear() / 10) * 10));
        if (grouped.isEmpty()) {
            log.warn("Grouped Patient by birth decade: repository is empty (0 groups)");
        } else {
            log.info("Grouped Patient by birth decade: {} groups", grouped.size());
        }
        return grouped;
    }

    public Map<Integer, Long> countByBirthDecade() {
        Map<Integer, Long> counts = stream()
                .collect(Collectors.groupingBy(
                        p -> (p.birthDate().getYear() / 10) * 10,
                        Collectors.counting()));
        if (counts.isEmpty()) {
            log.warn("Counted Patient by birth decade: repository is empty (0 groups)");
        } else {
            log.info("Counted Patient by birth decade: {} groups", counts.size());
        }
        return counts;
    }

    private void logSearchResult(String criterion, int count) {
        if (count == 0) {
            log.warn("Search Patient by [{}]: found 0 elements", criterion);
        } else {
            log.info("Search Patient by [{}]: found {} elements", criterion, count);
        }
    }
}
