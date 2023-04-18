package com.github.beelzebu.matrix.util;

import org.jetbrains.annotations.NotNull;

/**
 * @author Jaime Suárez
 */
public enum ErrorCodes {

    NULL_PLAYER(1),
    UUID_DONTMATCH(2),
    CANT_UPDATE_UUID(3),
    UNKNOWN(4);

    private final int id;

    ErrorCodes(int id) {
        this.id = id;
    }

    public @NotNull String getId() {
        return id < 100 ? id < 9 ? "00" + id : "0" + id : Integer.toString(id);
    }
}
