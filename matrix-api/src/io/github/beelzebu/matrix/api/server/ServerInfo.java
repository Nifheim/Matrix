package io.github.beelzebu.matrix.api.server;

import io.github.beelzebu.matrix.api.Matrix;
import io.github.beelzebu.matrix.api.player.MatrixPlayer;

public class ServerInfo {

    private final String groupName;
    private final String serverName;
    private final GameType gameType;
    private final ServerType serverType;

    public ServerInfo(String groupName, String serverName, GameType gameType, ServerType serverType) {
        this.groupName = groupName;
        this.serverName = serverName;
        this.gameType = gameType;
        this.serverType = serverType;
    }

    public ServerInfo(String serverName, GameType gameType, ServerType serverType) {
        this(serverName, serverName, gameType, serverType);
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

    public String toString() {
        return "ServerInfo(serverName=" + serverName + ", gameType=" + gameType + ", serverType=" + serverType + ")";
    }

    public String getGroupName() {
        return groupName;
    }
}
