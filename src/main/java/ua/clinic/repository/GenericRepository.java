package ua.clinic.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.clinic.exception.DuplicateIdentityException;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Generic in-memory repository storing entities of type {@code T} keyed by their unique string identity.
 * Uses {@link LinkedHashMap} for O(1) average lookup while preserving insertion order.
 *
 * @param <T> the entity type stored in this repository
 */
public class GenericRepository<T> {

    private static final Logger log = LoggerFactory.getLogger(GenericRepository.class);

    private final Class<T> type;
    private final IdentityExtractor<T> extractor;
    private final Map<String, T> storage = new LinkedHashMap<>();

    /**
     * Creates a new repository for the specified entity type and identity extractor.
     *
     * @param type      runtime class of the stored entities
     * @param extractor lambda or function extracting a unique identity from an entity
     */
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

    /**
     * Adds an item to the repository.
     *
     * @param item the item to add
     * @throws IllegalArgumentException   if {@code item} is null or its identity is null/blank
     * @throws DuplicateIdentityException if an item with the same identity already exists
     */
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

    /**
     * Adds an item to the repository only if its identity is not already present.
     *
     * @param item the item to add
     * @return {@code true} if the item was added, {@code false} if an item with the same identity already exists
     * @throws IllegalArgumentException if {@code item} is null or its identity is null/blank
     */
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

    /**
     * Adds all items from the given collection to the repository.
     *
     * @param items collection of items to add
     * @throws IllegalArgumentException   if {@code items} is null or contains invalid items
     * @throws DuplicateIdentityException if any item has a duplicate identity
     */
    public void addAll(Collection<? extends T> items) {
        if (items == null) {
            log.error("Cannot add null collection to {} repository", type.getSimpleName());
            throw new IllegalArgumentException("items collection must not be null");
        }
        for (T item : items) {
            add(item);
        }
    }

    /**
     * Removes an item by its identity.
     *
     * @param identity the identity of the item to remove
     * @return {@code true} if the item existed and was removed, {@code false} otherwise
     */
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

    /**
     * Finds an item by its identity.
     *
     * @param identity the identity to look up
     * @return an {@link Optional} containing the found item, or empty if not found
     */
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

    /**
     * Returns an unmodifiable snapshot list of all stored items in insertion order.
     * Modifications to the returned list cannot affect repository state.
     *
     * @return unmodifiable list of stored items
     */
    public List<T> findAll() {
        return List.copyOf(storage.values());
    }

    /**
     * Returns an unmodifiable list of stored items matching the given predicate.
     *
     * @param filter predicate used to filter stored items
     * @return unmodifiable list of matching items
     * @throws IllegalArgumentException if {@code filter} is null
     */
    public List<T> findAll(Predicate<? super T> filter) {
        if (filter == null) {
            log.error("Filter predicate must not be null for {} repository", type.getSimpleName());
            throw new IllegalArgumentException("filter must not be null");
        }
        return storage.values().stream()
                .filter(filter)
                .toList();
    }

    /**
     * Returns the number of items currently stored in the repository.
     *
     * @return repository size
     */
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
