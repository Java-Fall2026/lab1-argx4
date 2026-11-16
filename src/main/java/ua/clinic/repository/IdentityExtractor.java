package ua.clinic.repository;

/**
 * Functional interface for extracting a unique string identity from an entity.
 *
 * @param <T> the entity type
 */
@FunctionalInterface
public interface IdentityExtractor<T> {

    /**
     * Returns the unique identity string of the given item.
     *
     * @param item the entity instance
     * @return unique identity string
     */
    String identityOf(T item);
}
