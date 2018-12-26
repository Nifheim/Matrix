package io.github.beelzebu.matrix.api.config;

import java.io.File;

/**
 * @author Beelzebu
 */
public abstract class MatrixConfig extends AbstractConfig {

    public MatrixConfig(File file) {
        super(file);
    }

    /**
     * Get lobby for this game server
     *
     * @return Lobby server name from the config, if lobby server is not configured it will return "lobby#2"
     */
    public String getLobby() {
        return getString("lobby-server", "lobby#2");
    }
}
