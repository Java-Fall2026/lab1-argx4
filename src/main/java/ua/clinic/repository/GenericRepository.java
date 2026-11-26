package ua.clinic.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.clinic.exception.DuplicateIdentityException;

import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class GenericRepository<T> {

    private static final Logger log = LoggerFactory.getLogger(GenericRepository.class);

    private final Class<T> type;
    private final IdentityExtractor<T> extractor;
    private final Map<String, T> storage = new LinkedHashMap<>();

    public GenericRepository(Class<T> type, IdentityExtractor<T> extractor) {
        if (type == null) {
            log.error("Repository type must not be null");
            throw new IllegalArgumentException("type must not be null");
        }
        if (extractor == null) {
            log.error("Identity extractor must not be null for type {}", type.getSimpleName());
            throw new IllegalArgumentException("extractor must not be null");
        }
        this.type = type;
        this.extractor = extractor;
    }

    public void add(T item) {
        String identity = extractValidIdentity(item);
        if (storage.containsKey(identity)) {
            String message = "Duplicate identity '" + identity + "' for type " + type.getSimpleName();
            log.error(message);
            throw new DuplicateIdentityException(message);
        }
        storage.put(identity, item);
        log.info("Added {} with identity '{}'", type.getSimpleName(), identity);
    }

    public boolean addIfAbsent(T item) {
        String identity = extractValidIdentity(item);
        if (storage.containsKey(identity)) {
            log.warn("Rejected duplicate {} with identity '{}'", type.getSimpleName(), identity);
            return false;
        }
        storage.put(identity, item);
        log.info("Added {} with identity '{}'", type.getSimpleName(), identity);
        return true;
    }

    public void addAll(Collection<? extends T> items) {
        if (items == null) {
            log.error("Cannot add null collection to {} repository", type.getSimpleName());
            throw new IllegalArgumentException("items collection must not be null");
        }
        for (T item : items) {
            add(item);
        }
    }

    public boolean removeByIdentity(String identity) {
        if (identity == null || identity.isBlank()) {
            log.warn("Cannot remove {} with null or blank identity", type.getSimpleName());
            return false;
        }
        T removed = storage.remove(identity);
        if (removed == null) {
            log.warn("{} with identity '{}' not found for removal", type.getSimpleName(), identity);
            return false;
        }
        log.info("Removed {} with identity '{}'", type.getSimpleName(), identity);
        return true;
    }

    public Optional<T> findByIdentity(String identity) {
        if (identity == null || identity.isBlank()) {
            log.warn("Cannot search {} with null or blank identity", type.getSimpleName());
            return Optional.empty();
        }
        T found = storage.get(identity);
        if (found == null) {
            log.warn("{} with identity '{}' not found", type.getSimpleName(), identity);
            return Optional.empty();
        }
        log.info("Found {} with identity '{}'", type.getSimpleName(), identity);
        return Optional.of(found);
    }

    public List<T> findAll() {
        return List.copyOf(storage.values());
    }

    public List<T> findAll(Predicate<? super T> filter) {
        if (filter == null) {
            log.error("Filter predicate must not be null for {} repository", type.getSimpleName());
            throw new IllegalArgumentException("filter must not be null");
        }
        return storage.values().stream()
                .filter(filter)
                .toList();
    }

    public List<T> findAllSorted(Comparator<T> comparator) {
        if (comparator == null) {
            log.error("Comparator must not be null for {} repository", type.getSimpleName());
            throw new IllegalArgumentException("comparator must not be null");
        }
        return storage.values().stream()
                .sorted(comparator)
                .toList();
    }

    public List<T> findAllSortedByIdentity(SortOrder order) {
        if (order == null) {
            log.error("SortOrder must not be null for {} repository", type.getSimpleName());
            throw new IllegalArgumentException("order must not be null");
        }
        List<T> sorted = storage.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(order.comparator()))
                .map(Map.Entry::getValue)
                .toList();
        log.info("Sorted {} by identity ({}): returned {} elements", type.getSimpleName(), order, sorted.size());
        return sorted;
    }

    public Stream<T> stream() {
        return storage.values().stream();
    }

    public int size() {
        return storage.size();
    }

    private String extractValidIdentity(T item) {
        if (item == null) {
            String message = "Item must not be null for " + type.getSimpleName() + " repository";
            log.error(message);
            throw new IllegalArgumentException(message);
        }
        String identity = extractor.identityOf(item);
        if (identity == null || identity.isBlank()) {
            String message = "Identity must not be null or blank for " + type.getSimpleName();
            log.error(message);
            throw new IllegalArgumentException(message);
        }
        return identity;
    }
}
