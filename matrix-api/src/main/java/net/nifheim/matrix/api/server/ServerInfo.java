package net.nifheim.matrix.api.server;

import net.nifheim.matrix.api.player.gamemode.GameMode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a minecraft server information.
 *
 * @author Jaime Suárez
 */
public interface ServerInfo {

    /**
     * Default {@link GameMode} for this server, implementations may use this information to force gamemode on join or
     * while trying to revert a player gamemode when the previous gamemode information is missing.
     *
     * @return default {@link GameMode} for this server.
     */
    @NotNull GameMode getDefaultGameMode();

    /**
     * Get the group name of this server, server groups may be used by implementations or other apps to identify servers
     * where a feature must be enabled or storing data based on the group instead of the server.
     *
     * @return group name of this server.
     */
    @NotNull String getGroupName();

    /**
     * Get the {@link ServerType} of this server, it may be used by implementations or other apps to  identify servers
     * where a feature must be enabled or storing data based on the server type.
     *
     * @return {@link ServerType} of this server.
     */
    @NotNull ServerType getServerType();

    /**
     * Get the name used as unique identifier of this server, servers may not have a name as unique identifier, in that
     * case this method will return null.
     *
     * @return name of this server or null if there is no name.
     */
    @Nullable String getRawName();

    /**
     * Get the server number, this number is used to identify the server in the group, it is used only when {@link #isUnique()}
     * is false.
     *
     * @return server number.
     */
    int getServerNumber();

    /**
     * Get the formatted server name using our naming pattern.
     *
     * @return formatted server name.
     * @see ServerName information about the naming pattern.
     */
    @NotNull String getName();

    /**
     * Get the server address.
     *
     * @return server address.
     */
    @NotNull String getAddress();

    /**
     * Get the server port.
     *
     * @return server port.
     */
    int getPort();

    /**
     * Get the server max players.
     *
     * @return server max players.
     */
    int getMaxPlayers();

    /**
     * Get the server lobby.
     *
     * @return server lobby.
     */
    @Nullable String getDefaultLobbyName();

    /**
     * Get the server unique status.
     *
     * @return server unique status.
     */
    boolean isUnique();
}
