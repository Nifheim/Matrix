package net.nifheim.matrix.common.server;

import net.nifheim.matrix.api.server.ServerInfo;

/**
 * Represents the server platform information. Address, port, max players, etc.
 *
 * @author Jaime Suárez
 * @see ServerInfo
 */
public record ServerPlatformInfo(String address, int port, int maxPlayers) {

    public ServerPlatformInfo {
        if (port < 2000 || port > 65535) {
            throw new IllegalArgumentException("Port must be between 2000 and 65535");
        }
        if (maxPlayers < 0) {
            throw new IllegalArgumentException("Max players must be greater than 0");
        }
    }
}
