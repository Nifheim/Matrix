package net.nifheim.matrix.auth.paper.exception;

/**
 * The UserNotRegisteredException class is an exception that is thrown when an operation is performed on
 * a user who is not registered in the system.
 *
 * @author Jaime Suárez
 */
public class UserNotRegisteredException extends Exception {

    public UserNotRegisteredException() {
        super();
    }

    public UserNotRegisteredException(String message) {
        super(message);
    }

    public UserNotRegisteredException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserNotRegisteredException(Throwable cause) {
        super(cause);
    }
}
