package io.github.beelzebu.matrix.api.server;

import io.github.beelzebu.matrix.api.Matrix;
import io.github.beelzebu.matrix.api.player.MatrixPlayer;

public class ServerInfo {

    private final String serverName;
    private final GameType gameType;
    private final ServerType serverType;

    public ServerInfo(String serverName, GameType gameType, ServerType serverType) {
        this.serverName = serverName;
        this.gameType = gameType;
        this.serverType = serverType;
    }

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

    public String getServerName() {
        return serverName;
    }

    public GameType getGameType() {
        return gameType;
    }

    public ServerType getServerType() {
        return serverType;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof io.github.beelzebu.matrix.api.server.ServerInfo)) {
            return false;
        }
        io.github.beelzebu.matrix.api.server.ServerInfo other = (io.github.beelzebu.matrix.api.server.ServerInfo) o;
        if (!other.canEqual((java.lang.Object) this)) {
            return false;
        }
        java.lang.Object this$serverName = serverName;
        java.lang.Object other$serverName = other.serverName;
        if (this$serverName == null ? other$serverName != null : !this$serverName.equals(other$serverName)) {
            return false;
        }
        java.lang.Object this$gameType = gameType;
        java.lang.Object other$gameType = other.gameType;
        if (this$gameType == null ? other$gameType != null : !this$gameType.equals(other$gameType)) {
            return false;
        }
        java.lang.Object this$serverType = serverType;
        java.lang.Object other$serverType = other.serverType;
        if (this$serverType == null ? other$serverType != null : !this$serverType.equals(other$serverType)) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        java.lang.Object $serverName = serverName;
        result = result * PRIME + ($serverName == null ? 43 : $serverName.hashCode());
        java.lang.Object $gameType = gameType;
        result = result * PRIME + ($gameType == null ? 43 : $gameType.hashCode());
        java.lang.Object $serverType = serverType;
        result = result * PRIME + ($serverType == null ? 43 : $serverType.hashCode());
        return result;
    }

    public String toString() {
        return "ServerInfo(serverName=" + serverName + ", gameType=" + gameType + ", serverType=" + serverType + ")";
    }

    protected boolean canEqual(Object other) {
        return other instanceof io.github.beelzebu.matrix.api.server.ServerInfo;
    }
}
