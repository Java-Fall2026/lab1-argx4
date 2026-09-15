package ua.common;

import java.time.LocalDateTime;

/**
 * Base class for every domain entity.
 * <p>
 * Do not modify this file. Your entities extend it and call {@code super()}
 * from their constructors. The creation moment is not part of object identity:
 * keep {@code createdAt} out of {@code equals} and {@code hashCode}.
 */
public abstract class BaseEntity {

    protected final LocalDateTime createdAt;

    protected BaseEntity() {
        this.createdAt = LocalDateTime.now();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
