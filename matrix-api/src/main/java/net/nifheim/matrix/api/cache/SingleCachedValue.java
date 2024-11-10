package net.nifheim.matrix.api.cache;

import java.util.concurrent.CompletableFuture;
import org.jetbrains.annotations.Nullable;

/**
 * A class representing a single cached value. It allows retrieving the value asynchronously using CompletableFuture,
 * and keeps track of the last value that was retrieved.
 *
 * @param <T> The type of the cached value
 * @author Jaime Suárez
 */
public interface SingleCachedValue <T> {

    /**
     * Retrieves the cached value asynchronously using CompletableFuture. If the cached value is still available
     * (not null), completes the CompletableFuture with the cached value. Otherwise, initiates a refresh of the
     * cached value and returns a CompletableFuture that will be completed once the refresh is finished.
     *
     * @return a CompletableFuture that represents the cached value or a future result of the refresh operation
     */
    CompletableFuture<T> get();

    /**
     * Retrieves the last value that was retrieved.
     *
     * @return the last value that was retrieved
     */
    @Nullable T getLastValue();
}
