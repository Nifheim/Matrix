package net.nifheim.matrix.auth.paper.exception;

/**
 * Exception class representing an invalid password.
 * This exception is thrown when an invalid password is encountered in the application.
 *
 * @author Jaime Suárez
 */
public class InvalidPasswordException extends Exception {

    private final InvalidReason reason;

    public InvalidPasswordException(InvalidReason reason) {
        super(reason.toString());
        this.reason = reason;
    }

    public InvalidPasswordException(String message, InvalidReason reason) {
        super(message);
        this.reason = reason;
    }

    public InvalidPasswordException(String message, Throwable cause, InvalidReason reason) {
        super(message, cause);
        this.reason = reason;
    }

    public InvalidPasswordException(Throwable cause, InvalidReason reason) {
        super(cause);
        this.reason = reason;
    }

    public InvalidPasswordException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, InvalidReason reason) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.reason = reason;
    }

    public InvalidReason getReason() {
        return reason;
    }

    public enum InvalidReason {
        LENGTH,
        BLACKLISTED
    }
}
