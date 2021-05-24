package com.github.beelzebu.matrix.server;

import com.github.beelzebu.matrix.api.Matrix;
import com.github.beelzebu.matrix.api.player.GameMode;
import com.github.beelzebu.matrix.api.server.ServerInfo;
import com.github.beelzebu.matrix.api.server.ServerType;
import com.github.beelzebu.matrix.util.FinalCachedValue;
import com.github.beelzebu.matrix.util.SingleCachedValue;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ServerInfoImpl extends ServerInfo {

    public static final String AUTH_GROUP = ServerType.AUTH.getFriendlyName();
    public static final String PROXY_GROUP = ServerType.PROXY.getFriendlyName();
    public static final String MAIN_LOBBY_GROUP = ServerType.LOBBY.getFriendlyName();
    private final @NotNull String groupName;
    private final String serverName;
    private final ServerType serverType;
    private final @NotNull GameMode gameMode;
    private final boolean unique;
    private final @NotNull SingleCachedValue<String> lobby;

    public ServerInfoImpl(@NotNull ServerType serverType, @NotNull String groupName, @NotNull String serverName, GameMode gameMode, boolean unique) {
        this(serverType, groupName, serverName, gameMode, unique, Matrix.getAPI().getConfig().getString("server-info.lobby"), false);
    }

    public ServerInfoImpl(@NotNull ServerType serverType, @NotNull String groupName, @NotNull String serverName, @Nullable GameMode gameMode, boolean unique, @Nullable String lobby, boolean decoded) {
        this.serverType = Objects.requireNonNull(serverType, "serverType name can't be null");
        this.groupName = Objects.requireNonNull(groupName, "groupName can't be null").toLowerCase();
        this.unique = !Objects.isNull(serverName) && unique;
        if (decoded) {
            this.serverName = serverName.toLowerCase();
        } else {
            this.serverName = generateServerName(serverType, groupName, serverName);
        }
        if (this.serverName == null || this.serverName.trim().isEmpty()) {
            throw new IllegalArgumentException("serverName");
        }
        this.gameMode = gameMode == null ? (serverType == ServerType.SURVIVAL ? GameMode.SURVIVAL : GameMode.ADVENTURE) : gameMode;
        if (serverType == ServerType.PROXY || serverType == ServerType.AUTH || serverType == ServerType.LOBBY) {
            this.lobby = new FinalCachedValue<>(() -> null);
        } else if (lobby != null) {
            this.lobby = new FinalCachedValue<>(() -> lobby);
        } else {
            this.lobby = new SingleCachedValue<>(() -> {
                Matrix.getLogger().debug("Finding lobby for server");
                return Matrix.getAPI().getServerManager().getLobbyForGroup(groupName);
            }, 10, TimeUnit.MINUTES);
        }
    }

    @Deprecated
    public ServerInfoImpl(@NotNull String name, @NotNull Map<String, String> data) {
        this(ServerType.valueOf(Objects.requireNonNull(data.get("servertype"), "servertype for " + name)),
                Objects.requireNonNull(data.get("group"), "groupName can't be null for " + name),
                name,
                GameMode.valueOf(Objects.requireNonNull(data.get("gamemode"), "gamemode for " + name)),
                Boolean.parseBoolean(data.get("unique")),
                data.get("lobby"),
                true);
    }

    /**
     * Generate the server name based on {@link this#getServerType()}, {@link this#getGroupName()} and {@link
     * this#getServerName()} (only if provided), if this server is marked as {@link #unique}, then it will be the only
     * server with a name generated with the same parameters, otherwise it will append a number at the end to make the
     * server names generated unique.
     *
     * @param serverType {@link ServerType} for the server.
     * @param groupName  name for the group of the server.
     * @param serverName name for the server.
     * @return the generated name.
     * @see #formatServerName(ServerType, String, String)
     */
    private String generateServerName(@NotNull ServerType serverType, @NotNull String groupName, @Nullable String serverName) {
        Matrix.getLogger().debug("Generating server name: " + serverType + " " + groupName + " " + serverName);
        if (unique) {
            Matrix.getLogger().debug("Server is unique, returning default name");
            return formatServerName(serverType, groupName, serverName);
        }
        String name;
        List<String> servers = Matrix.getAPI().getServerManager().getServers(groupName).thenApply(serverInfos -> serverInfos.stream()
                .map(ServerInfo::getServerName)
                .filter(n ->
                        n.startsWith(formatServerName(serverType, groupName, serverName))
                ).collect(Collectors.toList())).join();

        for (int i = 1; ; ) {
            name = formatServerName(serverType, groupName, null) + i;
            if (!servers.contains(name)) {
                break;
            } else {
                i++;
            }
        }
        return name;
    }

    private @NotNull String formatServerName(@NotNull ServerType serverType, @NotNull String groupName, @Nullable String serverName) {
        StringBuilder name = new StringBuilder();
        name.append(groupName);
        if (serverName != null && !Objects.equals(groupName, serverName)) {
            name.append(":").append(serverName);
        }
        if (!Objects.equals(serverType.getFriendlyName(), groupName) && !(serverType == ServerType.PROXY || serverType == ServerType.AUTH || serverType == ServerType.SURVIVAL)) {
            name.append(":").append(serverType.getFriendlyName());
        }
        return name.toString().toLowerCase();
    }

    @Override
    public @NotNull GameMode getDefaultGameMode() {
        return this.gameMode != null ? this.gameMode : GameMode.ADVENTURE;
    }

    @Override
    public @NotNull String getGroupName() {
        return this.groupName;
    }

    @Override
    public @NotNull String getServerName() {
        return this.serverName;
    }

    @Override
    public @NotNull CompletableFuture<String> getLobbyServer() {
        return lobby.get();
    }

    public @NotNull SingleCachedValue<String> getCachedLobby() {
        return lobby;
    }

    @Override
    public boolean isUnique() {
        return unique;
    }

    @Override
    public @NotNull ServerType getServerType() {
        return this.serverType;
    }
}