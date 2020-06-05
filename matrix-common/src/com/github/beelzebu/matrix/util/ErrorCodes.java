package com.github.beelzebu.matrix.util;

/**
 * @author Beelzebu
 */
public enum ErrorCodes {

    NULL_PLAYER(1),
    UUID_DONTMATCH(2);

    private final int id;

    private ErrorCodes(int id) {
        this.id = id;
    }

    public String getId() {
        return id < 100 ? id < 9 ? "00" + id : "0" + id : Integer.toString(id);
    }
}
