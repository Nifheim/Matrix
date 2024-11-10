package net.nifheim.matrix.api.server;

import org.jetbrains.annotations.NotNull;

/**
 * Enum used to identify server types when checking a {@link ServerInfo}.
 *
 * @author Jaime Suárez
 */
public enum ServerType {

    PROXY,
    AUTH,
    LOBBY,
    ARENA,
    SURVIVAL,
    NONE,
    OTHER;

    @Override
    public @NotNull String toString() {
        return super.toString().toLowerCase();
    }
}
