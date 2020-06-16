package com.github.beelzebu.matrix.api.server;

import com.github.beelzebu.matrix.api.player.GameMode;
import java.util.Objects;

public class ServerInfo {

    private final String groupName;
    private final String serverName;
    private final String lobbyServer;
    private final GameType gameType;
    private final ServerType serverType;
    private final GameMode gameMode;

    public ServerInfo(String groupName, String serverName, String lobbyServer, GameType gameType, ServerType serverType, GameMode gameMode) {
        this.groupName = Objects.requireNonNull(groupName != null ? groupName : serverName, "groupName can't be null");
        this.serverName = Objects.requireNonNull(serverName, "serverName can't be null");
        this.lobbyServer = lobbyServer;
        this.gameType = Objects.requireNonNull(gameType, "gameType can't be null");
        this.serverType = Objects.requireNonNull(serverType, "serverType name can't be null");
        this.gameMode = serverType == ServerType.SURVIVAL ? GameMode.SURVIVAL : gameMode;
    }

    public ServerInfo(String groupName, String serverName, GameType gameType, ServerType serverType, GameMode gameMode) {
        this(groupName, serverName, null, gameType, serverType, gameMode);
    }

    public ServerInfo(String serverName, GameType gameType, ServerType serverType, GameMode gameMode) {
        this(serverName, serverName, gameType, serverType, gameMode);
    }

    public GameMode getDefaultGameMode() {
        return gameMode != null ? gameMode : GameMode.ADVENTURE;
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
