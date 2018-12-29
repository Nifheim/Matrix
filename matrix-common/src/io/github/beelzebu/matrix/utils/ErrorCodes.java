package io.github.beelzebu.matrix.utils;

import lombok.AllArgsConstructor;

/**
 * @author Beelzebu
 */
@AllArgsConstructor
public enum ErrorCodes {

    NULL_PLAYER(1),
    UUID_DONTMATCH(2);

    private final int id;

    public String getId() {
        return id < 100 ? id < 9 ? "00" + id : "0" + id : Integer.toString(id);
    }
}
