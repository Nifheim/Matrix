package com.github.beelzebu.matrix.server;

import com.github.beelzebu.matrix.api.Matrix;
import com.github.beelzebu.matrix.api.player.GameMode;
import com.github.beelzebu.matrix.api.server.GameType;
import com.github.beelzebu.matrix.api.server.ServerInfo;
import com.github.beelzebu.matrix.api.server.ServerType;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class ServerInfoImpl implements ServerInfo {

    public static final String MAIN_LOBBY_GROUP = "lobby";
    private final String groupName;
    private final String serverName;
    private final GameType gameType;
    private final ServerType serverType;
    private final GameMode gameMode;

    public ServerInfoImpl(GameType gameType, ServerType serverType, String groupName, GameMode gameMode) {
        this.gameType = Objects.requireNonNull(gameType, "gameType can't be null");
        this.serverType = Objects.requireNonNull(serverType, "serverType name can't be null");
        this.groupName = Objects.requireNonNull(groupName != null ? groupName : gameType.getGameName(), "groupName can't be null");
        this.serverName = generateServerName();
        this.gameMode = gameMode == null ? (serverType == ServerType.SURVIVAL ? GameMode.SURVIVAL : GameMode.ADVENTURE) : gameMode;
    }

    @Deprecated
    public ServerInfoImpl(GameType gameType, ServerType serverType, String groupName, String serverName, GameMode gameMode) {
        this.gameType = Objects.requireNonNull(gameType, "gameType can't be null");
        this.serverType = Objects.requireNonNull(serverType, "serverType name can't be null");
        this.groupName = Objects.requireNonNull(groupName != null ? groupName : gameType.getGameName(), "groupName can't be null");
        this.serverName = serverName;
        this.gameMode = gameMode == null ? (serverType == ServerType.SURVIVAL ? GameMode.SURVIVAL : GameMode.ADVENTURE) : gameMode;
    }

    public ServerInfoImpl(GameType gameType, ServerType serverType, GameMode gameMode) {
        this(gameType, serverType, gameType.getGameName(), gameMode);
    }

    @Deprecated
    public ServerInfoImpl(String name, Map<String, String> data) {
        String groupName = data.get("group");
        GameType gameType = GameType.valueOf(data.get("gametype"));
        ServerType serverType = ServerType.valueOf(data.get("servertype"));
        GameMode gameMode = GameMode.valueOf(data.get("gamemode"));
        this.gameType = Objects.requireNonNull(gameType, "gameType can't be null");
        this.serverType = Objects.requireNonNull(serverType, "serverType name can't be null");
        this.groupName = Objects.requireNonNull(groupName != null ? groupName : gameType.getGameName(), "groupName can't be null");
        this.serverName = name;
        this.gameMode = gameMode;
    }

    private String generateServerName() {
        String name;
        List<String> servers = Matrix.getAPI().getCache().getServers(groupName).stream().map(ServerInfo::getServerName).filter(n -> n.startsWith(ServerInfo.formatServerName(groupName, gameType, serverType))).collect(Collectors.toList());
        for (int i = 1; ; ) {
            name = ServerInfo.formatServerName(groupName, gameType, serverType) + i;
            if (!servers.contains(name)) {
                break;
            } else {
                i++;
            }
        }
        return name;
    }

    public GameMode getDefaultGameMode() {
        return this.gameMode != null ? this.gameMode : GameMode.ADVENTURE;
    }

    public String getGroupName() {
        return this.groupName;
    }

    public String getServerName() {
        return this.serverName;
    }

    public String getLobbyServer() {
        return ServerInfo.findLobbyForServer(this);
    }

    public GameType getGameType() {
        return this.gameType;
    }

    public ServerType getServerType() {
        return this.serverType;
    }

    public String toString() {
        return "ServerInfo(serverName=" + this.getServerName() + ", gameType=" + this.gameType + ", serverType=" + this.serverType + ")";
    }
}