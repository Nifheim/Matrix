package net.nifheim.matrix.api.service;

import java.io.Serial;

/**
 * Exception that can be thrown by service implementations when an operation is called and the service is not active.
 *
 * @author Jaime Suárez
 */
public class InactiveServiceException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1;

    public InactiveServiceException(String message) {
        super(message);
    }

    public InactiveServiceException(Throwable cause) {
        super(cause);
    }

    public InactiveServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
