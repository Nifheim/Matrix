package io.github.beelzebu.matrix.api.server;

import io.github.beelzebu.matrix.api.Matrix;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class ServerInfo {

    private final String serverName;
    private ServerType serverType;

    /**
     * Get lobby name for this server.
     *
     * @return Lobby server name from the config or null if this server isn't a minigame server.
     */
    public String getLobby() {
        if (serverType.isMinigame() && Matrix.getAPISafe().isPresent()) {
            return Matrix.getAPI().getConfig().getLobby();
        }
        return null;
    }
}
