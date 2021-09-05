package com.github.beelzebu.matrix.exception;

import com.github.beelzebu.matrix.util.ErrorCodes;
import com.github.beelzebu.matrix.util.LoginState;

/**
 * @author Beelzebu
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
