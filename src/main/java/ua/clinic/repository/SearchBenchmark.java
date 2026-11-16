package ua.clinic.repository;

import ua.clinic.model.Patient;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Benchmarks identity lookup performance in {@link GenericRepository} (Map-backed)
 * versus linear search in a {@link List}.
 */
public class SearchBenchmark {

    private static final int ELEMENT_COUNT = 100_000;
    private static final int WARMUP_ROUNDS = 5;
    private static final int MEASURE_ROUNDS = 21;
    private static final int MAP_BATCH_SIZE = 10_000;
    private static final int LIST_BATCH_SIZE = 100;

    private static volatile long blackhole;

    public static void main(String[] args) {
        // Silence logging before SLF4J initializes so console I/O does not distort benchmark timings
        System.setProperty("LOG_LEVEL", "OFF");

        IdentityExtractor<Patient> extractor = p -> p.nationalId();
        GenericRepository<Patient> repository = new GenericRepository<>(Patient.class, extractor);

        LocalDate defaultBirthDate = LocalDate.of(1990, 1, 1);
        for (int i = 0; i < ELEMENT_COUNT; i++) {
            String id = String.format("ID-%06d", i);
            repository.add(Patient.of(id, "Patient " + i, defaultBirthDate));
        }

        // Snapshot list taken once before measurements
        List<Patient> list = repository.findAll();

        String firstId = "ID-000000";
        String lastId = String.format("ID-%06d", ELEMENT_COUNT - 1);
        String missingId = "ID-999999";

        // Warmup runs to trigger JIT compilation
        long sink = 0;
        for (int r = 0; r < WARMUP_ROUNDS; r++) {
            sink += runMapBatch(repository, firstId, MAP_BATCH_SIZE);
            sink += runMapBatch(repository, lastId, MAP_BATCH_SIZE);
            sink += runMapBatch(repository, missingId, MAP_BATCH_SIZE);

            sink += runListBatch(list, firstId, extractor, LIST_BATCH_SIZE);
            sink += runListBatch(list, lastId, extractor, LIST_BATCH_SIZE);
            sink += runListBatch(list, missingId, extractor, LIST_BATCH_SIZE);
        }
        blackhole = sink;

        double mapFirstNs = measureMedianMapNs(repository, firstId);
        double mapLastNs = measureMedianMapNs(repository, lastId);
        double mapMissingNs = measureMedianMapNs(repository, missingId);

        double listFirstNs = measureMedianListNs(list, firstId, extractor);
        double listLastNs = measureMedianListNs(list, lastId, extractor);
        double listMissingNs = measureMedianListNs(list, missingId, extractor);

        System.out.println("Бенчмарк пошуку серед " + ELEMENT_COUNT + " об'єктів Patient (медіана з " + MEASURE_ROUNDS + " замірів):");
        System.out.println("---------------------------------------------------------------------");
        System.out.printf("%-22s | %15s | %15s%n", "Сценарій", "Map (нс)", "List (нс)");
        System.out.println("---------------------------------------------------------------------");
        System.out.printf("%-22s | %15.2f | %15.2f%n", "Перший елемент", mapFirstNs, listFirstNs);
        System.out.printf("%-22s | %15.2f | %15.2f%n", "Останній елемент", mapLastNs, listLastNs);
        System.out.printf("%-22s | %15.2f | %15.2f%n", "Відсутній елемент", mapMissingNs, listMissingNs);
        System.out.println("---------------------------------------------------------------------");
    }

    private static <E> Optional<E> linearSearch(
            List<E> items, String identity, IdentityExtractor<E> extractor) {
        for (E item : items) {
            if (identity.equals(extractor.identityOf(item))) {
                return Optional.of(item);
            }
        }
        return Optional.empty();
    }

    private static double measureMedianMapNs(GenericRepository<Patient> repository, String identity) {
        double[] samples = new double[MEASURE_ROUNDS];
        long sink = 0;
        for (int i = 0; i < MEASURE_ROUNDS; i++) {
            long start = System.nanoTime();
            sink += runMapBatch(repository, identity, MAP_BATCH_SIZE);
            long elapsed = System.nanoTime() - start;
            samples[i] = (double) elapsed / MAP_BATCH_SIZE;
        }
        blackhole = sink;
        return median(samples);
    }

    private static double measureMedianListNs(
            List<Patient> list, String identity, IdentityExtractor<Patient> extractor) {
        double[] samples = new double[MEASURE_ROUNDS];
        long sink = 0;
        for (int i = 0; i < MEASURE_ROUNDS; i++) {
            long start = System.nanoTime();
            sink += runListBatch(list, identity, extractor, LIST_BATCH_SIZE);
            long elapsed = System.nanoTime() - start;
            samples[i] = (double) elapsed / LIST_BATCH_SIZE;
        }
        blackhole = sink;
        return median(samples);
    }

    private static long runMapBatch(GenericRepository<Patient> repository, String identity, int iterations) {
        long foundCount = 0;
        for (int i = 0; i < iterations; i++) {
            if (repository.findByIdentity(identity).isPresent()) {
                foundCount++;
            }
        }
        return foundCount;
    }

    private static long runListBatch(
            List<Patient> list, String identity, IdentityExtractor<Patient> extractor, int iterations) {
        long foundCount = 0;
        for (int i = 0; i < iterations; i++) {
            if (linearSearch(list, identity, extractor).isPresent()) {
                foundCount++;
            }
        }
        return foundCount;
    }

    private static double median(double[] values) {
        double[] sorted = Arrays.copyOf(values, values.length);
        Arrays.sort(sorted);
        int mid = sorted.length / 2;
        if (sorted.length % 2 == 0) {
            return (sorted[mid - 1] + sorted[mid]) / 2.0;
        }
        return sorted[mid];
    }
}
