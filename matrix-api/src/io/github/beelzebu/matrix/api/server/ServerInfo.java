package io.github.beelzebu.matrix.api.server;

import io.github.beelzebu.matrix.api.Matrix;
import io.github.beelzebu.matrix.api.player.MatrixPlayer;
import lombok.Data;

@Data
public class ServerInfo {

    private final String serverName;
    private final GameType gameType;
    private final ServerType serverType;

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

    public MatrixPlayer.GameMode getDefaultGameMode() {
        return MatrixPlayer.GameMode.valueOf(Matrix.getAPI().getConfig().getString("default.game-mode", "ADVENTURE"));
    }
}
