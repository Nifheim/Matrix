package net.nifheim.matrix.api.service;

import java.io.Closeable;

/**
 * Represents a matrix service of any kind.
 *
 * @author Jaime Suárez
 */
public interface MatrixService extends Closeable, AutoCloseable {

    /**
     * Shutdown this service, so it can't be used anymore.
     */
    void shutdown() throws InactiveServiceException;

    /**
     * Can be used to check if this service is usable, this service may be inactive because still needs additional steps
     * to start or was shutdown using {@link #shutdown()}
     *
     * @return if the service is active or not.
     */
    boolean isActive();

    @Override
    default void close() throws InactiveServiceException {
        shutdown();
    }
}
