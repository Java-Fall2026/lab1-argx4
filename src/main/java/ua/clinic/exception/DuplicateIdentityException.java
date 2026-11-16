package ua.clinic.exception;

/**
 * Unchecked exception thrown when attempting to add an item whose identity
 * already exists in the repository.
 */
public class DuplicateIdentityException extends IllegalStateException {

    /**
     * Constructs a new exception with the specified detail message.
     *
     * @param message detail message including the duplicate identity and entity type
     */
    public DuplicateIdentityException(String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified detail message and cause.
     *
     * @param message detail message
     * @param cause   underlying cause
     */
    public DuplicateIdentityException(String message, Throwable cause) {
        super(message, cause);
    }
}
