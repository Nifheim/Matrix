package net.nifheim.matrix.velocity.util;

import net.nifheim.matrix.common.util.ErrorCodes;

/**
 * @author Jaime Suárez
 */
public class LoginException extends Exception {

    private final ErrorCodes errorCodes;
    private final LoginState loginState;

    public LoginException(ErrorCodes errorCodes, LoginState loginState) {
        super("There was an error processing your " + loginState + " error code: " + errorCodes);
        this.errorCodes = errorCodes;
        this.loginState = loginState;
    }

    public LoginException(ErrorCodes errorCodes, LoginState loginState, String message) {
        super("There was an error processing your " + loginState + " error code: " + errorCodes + " extra: " + message);
        this.errorCodes = errorCodes;
        this.loginState = loginState;
    }

    public ErrorCodes getErrorCodes() {
        return errorCodes;
    }

    public LoginState getLoginState() {
        return loginState;
    }
}
