package net.nifheim.matrix.common.server;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import net.nifheim.matrix.api.player.gamemode.GameMode;
import net.nifheim.matrix.api.server.ServerInfo;
import net.nifheim.matrix.api.server.ServerInfoFactory;
import net.nifheim.matrix.api.server.ServerName;
import net.nifheim.matrix.api.server.ServerType;
import net.nifheim.matrix.common.config.sub.ServerInfoConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

/**
 * {@inheritDoc}
 *
 * @author Jaime Suárez
 * @see ServerInfoConfiguration
 */
public final class ServerInfoFactoryImpl implements ServerInfoFactory {

    private final Logger logger;
    private final ServerManagerImpl serverManager;


    public ServerInfoFactoryImpl(Logger logger, ServerManagerImpl serverManager) {
        this.logger = logger;
        this.serverManager = serverManager;
    }

    @Override
    public ServerInfo createServerInfo(String groupName, ServerType serverType, GameMode gameMode, String address, int port, int maxPlayers) {
        int number = generateNumber(groupName, serverType);
        ServerInfo serverInfo = new ServerInfoImpl(groupName, number, serverType, gameMode, address, port, maxPlayers);
        validateServer(serverInfo);
        return serverInfo;
    }

    @Override
    public ServerInfo createServerInfo(String groupName, @Nullable String rawServerName, ServerType serverType, GameMode gameMode, String address, int port, int maxPlayers) {
        if (rawServerName == null || rawServerName.isBlank()) {
            logger.warn("Server name is null, using unique number");
            return createServerInfo(groupName, serverType, gameMode, address, port, maxPlayers);
        }
        ServerInfo serverInfo = new ServerInfoImpl(groupName, rawServerName, serverType, gameMode, address, port, maxPlayers);
        validateServer(serverInfo);
        return serverInfo;
    }

    /**
     * Build a server info from the given configuration.
     *
     * @param serverInfoConfiguration configuration.
     * @param platformInfo            platform info.
     * @return server info.
     */
    public ServerInfo createServerInfo(ServerInfoConfiguration serverInfoConfiguration, ServerPlatformInfo platformInfo) {
        return createServerInfo(serverInfoConfiguration.getGroupName(), serverInfoConfiguration.getServerName(), serverInfoConfiguration.getServerType(), serverInfoConfiguration.getGameMode(), platformInfo.address(), platformInfo.port(), platformInfo.maxPlayers());
    }

    /**
     * Build a server info from the given information of the redis hash, it must contain the following keys:
     * <ul>
     *     <li>groupName</li>
     *     <li>rawServerName</li>
     *     <li>serverType</li>
     *     <li>gameMode</li>
     *     <li>address</li>
     *     <li>port</li>
     *     <li>maxPlayers</li>
     * </ul>
     *
     * @param hash hash with all the information.
     * @return server info.
     */
    public ServerInfo createServerInfo(@NotNull Map<String, String> hash) {
        Objects.requireNonNull(hash, "hash");
        String groupName = Objects.requireNonNull(hash.get("groupName"), "groupName");
        String rawServerName = hash.get("rawServerName");
        String serverNumber = hash.get("serverNumber");
        String serverType = Objects.requireNonNull(hash.get("serverType"), "serverType");
        String gameMode = Objects.requireNonNull(hash.get("gameMode"), "gameMode");
        String address = Objects.requireNonNull(hash.get("address"), "address");
        String port = Objects.requireNonNull(hash.get("port"), "port");
        String maxPlayers = Objects.requireNonNull(hash.get("maxPlayers"), "maxPlayers");
        return new ServerInfoImpl(
                groupName,
                rawServerName,
                serverNumber != null ? Integer.parseInt(serverNumber) : -1,
                ServerType.valueOf(serverType),
                GameMode.valueOf(gameMode),
                address,
                Integer.parseInt(port),
                Integer.parseInt(maxPlayers)
        );
    }

    private int generateNumber(String groupName, ServerType serverType) {
        Set<String> futureServers = serverManager.getServerNamesSync(groupName);
        for (int i = 1; i <= futureServers.size(); i++) {
            String serverName = ServerName.of(groupName, serverType, i);
            if (serverManager.isServerRegisteredInGroupSync(groupName, serverName)) {
                continue;
            }
            return i;
        }
        return 1;
    }

    private void validateServer(ServerInfo serverInfo) {
        if (!serverInfo.isUnique() && serverManager.isServerRegisteredInGroupSync(serverInfo.getGroupName(), serverInfo.getName())) {
            // check server last heartbeat
            long lastHeartbeat = serverManager.getLastHeartbeatSync(serverInfo).orElse(0);
            if (lastHeartbeat == 0 || lastHeartbeat + TimeUnit.MINUTES.toMillis(5) < System.currentTimeMillis()) {
                serverManager.removeServer(serverInfo);
                return;
            }
            throw new IllegalArgumentException("Server with name " + serverInfo.getName() + " already exists");
        }
    }
}
