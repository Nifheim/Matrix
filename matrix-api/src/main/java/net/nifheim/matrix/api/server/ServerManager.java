package net.nifheim.matrix.api.server;

import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Server manager, used to handle servers connected to the network.
 *
 * @author Jaime Suárez
 */
public interface ServerManager {

    /**
     * Get the information of every running server connected to the network. Separated by group name.
     *
     * @return information of every server connected to the network.
     */
    @NotNull CompletableFuture<Map<String, Set<ServerInfo>>> getAllServers();

    /**
     * Get the information of every server that is member of the given group.
     *
     * @param groupName group name.
     * @return information of every server that is member of the given group.
     */
    @NotNull CompletableFuture<Set<String>> getServerNames(@NotNull String groupName);

    /**
     * Get the information of the server with the given name.
     *
     * @param serverName server name.
     * @return information of the server with the given name.
     */
    @NotNull CompletableFuture<Optional<ServerInfo>> getServer(@NotNull String serverName);

    /**
     * Add a new server to the network.
     *
     * @param serverInfo server information.
     */
    @NotNull CompletableFuture<Void> addServer(@NotNull ServerInfo serverInfo);

    /**
     * Add multiple servers to the network.
     * <p>
     * This method is useful when you want to add multiple servers at once, for example when you want to add all the
     * servers that are currently running.
     * </p>
     * <p>
     * This method will not fail if one of the servers fails to be added, it will just ignore it.
     * </p>
     * <p>
     * This method will not fail if the server already exists, it will just ignore it.
     * </p>
     *
     * @param serverInfos server information.
     */
    @NotNull CompletableFuture<Void> addServers(@NotNull ServerInfo[] serverInfos);

    /**
     * Remove a server from the network.
     *
     * @param serverInfo server information.
     * @return a future that will complete when the server is removed from the network.
     */
    @NotNull CompletableFuture<Void> removeServer(@NotNull ServerInfo serverInfo);

    /**
     * Remove the current server from the network.
     *
     * @return a future that will complete when the server is removed from the network.
     */
    @NotNull CompletableFuture<Void> removeServer();

    /**
     * Send a heartbeat to mark this server as alive, this method should be called periodically, so the server is not
     * marked as offline and removed from the network.
     */
    @NotNull CompletableFuture<Void> heartbeat() throws IllegalStateException;

    /**
     * Get the last heartbeat of the given server.
     *
     * @param serverInfo server information.
     * @return last heartbeat of the given server.
     */
    @NotNull CompletableFuture<OptionalLong> getLastHeartbeat(@NotNull ServerInfo serverInfo);

    /**
     * Get the names of all the groups that are currently registered in the network and have at least one server
     * running.
     *
     * @return names of all the groups that are currently registered in the network.
     */
    @NotNull CompletableFuture<Set<String>> getGroupsNames();

    /**
     * Get the lobby server name of the given group.
     *
     * @param groupName group name.
     * @return lobby server name of the given group.
     */
    @NotNull String getLobbyForGroup(@NotNull String groupName);

    /**
     * Generate the lobby server name based on the {@link ServerInfo#getGroupName()} and {@link
     * ServerInfo#getServerType()}, if there is no lobby server defined on this group then we fall back to auth1 server,
     * so implementations may redirect the player to real lobby servers if they are logged in, and force them to
     * authenticate if they are logged out.
     *
     * @return Lobby server name or "auth1" if there is no lobby server.
     * @see ServerName for more information about server names.
     */
    @NotNull CompletableFuture<@Nullable String> getLobbyServerName(ServerInfo serverInfo);
}
