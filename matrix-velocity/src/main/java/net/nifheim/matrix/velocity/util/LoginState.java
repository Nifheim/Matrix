package net.nifheim.matrix.velocity.util;

import org.jetbrains.annotations.Nullable;

/**
 * @author Jaime Suárez
 */
public enum LoginState {
    HANDSHAKE(null),
    PRE_LOGIN(HANDSHAKE),
    LOGIN(PRE_LOGIN),
    POST_LOGIN(LOGIN);

    private final @Nullable LoginState previousState;

    LoginState(@Nullable LoginState previousState) {
        this.previousState = previousState;
    }

    public @Nullable LoginState getPreviousState() {
        return previousState;
    }
}
