package com.github.beelzebu.matrix.api.server;

import com.github.beelzebu.matrix.api.Matrix;
import com.github.beelzebu.matrix.api.player.GameMode;
import java.util.Objects;

public class ServerInfo {

    private final String groupName;
    private final String serverName;
    private final String lobbyServer;
    private final GameType gameType;
    private final ServerType serverType;

    public ServerInfo(String groupName, String serverName, String lobbyServer, GameType gameType, ServerType serverType) {
        this.groupName = Objects.requireNonNull(groupName != null ? groupName : serverName, "groupName can't be null");
        this.serverName = Objects.requireNonNull(serverName, "serverName can't be null");
        this.lobbyServer = lobbyServer;
        this.gameType = Objects.requireNonNull(gameType, "gameType can't be null");
        this.serverType = Objects.requireNonNull(serverType, "serverType name can't be null");
    }

    public ServerInfo(String groupName, String serverName, GameType gameType, ServerType serverType) {
        this(groupName, serverName, null, gameType, serverType);
    }

    public ServerInfo(String serverName, GameType gameType, ServerType serverType) {
        this(serverName, serverName, gameType, serverType);
    }

    public GameMode getDefaultGameMode() {
        return GameMode.valueOf(Matrix.getAPI().getConfig().getString("default.game-mode", "ADVENTURE"));
    }

    public String getGroupName() {
        return groupName;
    }

    public String getServerName() {
        return serverName;
    }

    public String getLobbyServer() {
        return lobbyServer;
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


}
