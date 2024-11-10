package net.nifheim.matrix.common.server;

import java.util.Locale;
import net.nifheim.matrix.api.player.gamemode.GameMode;
import net.nifheim.matrix.api.server.ServerInfo;
import net.nifheim.matrix.api.server.ServerName;
import net.nifheim.matrix.api.server.ServerType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ServerInfoImpl implements ServerInfo {

    private final @NotNull String groupName;
    private final @Nullable String rawServerName;
    private final int serverNumber;
    private final @NotNull String serverName;
    private final @NotNull ServerType serverType;
    private final @NotNull GameMode gameMode;
    private final @NotNull String address;
    private final int port;
    private final int maxPlayers;

    public ServerInfoImpl(
            @NotNull String groupName,
            int serverNumber,
            @NotNull ServerType serverType,
            @NotNull GameMode gameMode,
            @NotNull String address,
            int port,
            int maxPlayers
    ) {

        this(groupName, null, serverNumber, serverType, gameMode, address, port, maxPlayers);
    }

    public ServerInfoImpl(
            @NotNull String groupName,
            @NotNull String rawServerName,
            @NotNull ServerType serverType,
            @NotNull GameMode gameMode,
            @NotNull String address,
            int port,
            int maxPlayers
    ) {
        this(groupName, rawServerName, -1, serverType, gameMode, address, port, maxPlayers);
    }

    public ServerInfoImpl(
            @NotNull String groupName,
            @Nullable String rawServerName,
            int serverNumber,
            @NotNull ServerType serverType,
            @NotNull GameMode gameMode,
            @NotNull String address,
            int port,
            int maxPlayers
    ) {
        this.groupName = groupName;
        this.rawServerName = rawServerName;
        this.serverNumber = serverNumber;
        if (rawServerName != null) {
            this.serverName = ServerName.of(groupName, serverType, rawServerName);
        } else {
            this.serverName = ServerName.of(groupName, serverType, serverNumber);
        }
        this.serverType = serverType;
        this.gameMode = gameMode;
        this.address = address;
        this.port = port;
        this.maxPlayers = maxPlayers;
    }

    @Override
    public @NotNull GameMode getDefaultGameMode() {
        return this.gameMode;
    }

    @Override
    public @NotNull String getGroupName() {
        return this.groupName;
    }

    @Override
    public @Nullable String getRawName() {
        if (rawServerName != null) {
            return rawServerName.trim().toLowerCase(Locale.ROOT);
        }
        return null;
    }

    @Override
    public int getServerNumber() {
        return serverNumber;
    }

    @Override
    public @NotNull String getName() {
        return serverName.trim().toLowerCase(Locale.ROOT);
    }

    @Override
    public @NotNull String getAddress() {
        return address;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public int getMaxPlayers() {
        return maxPlayers;
    }

    @Override
    public @NotNull String getDefaultLobbyName() {
        return ServerName.of(groupName, ServerType.LOBBY, 1);
    }

    @Override
    public boolean isUnique() {
        return rawServerName != null;
    }

    @Override
    public @NotNull ServerType getServerType() {
        return this.serverType;
    }
}
