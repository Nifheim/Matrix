package net.nifheim.matrix.common.config.sub;

import net.nifheim.matrix.api.player.gamemode.GameMode;
import net.nifheim.matrix.api.server.ServerType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ServerInfoConfiguration {

    /**
     * {@link GameMode} for this server, implementations may use this information to force gamemode on join or
     * while trying to revert a player gamemode when the previous gamemode information is missing.
     *
     * @return default {@link GameMode} for this server.
     */
    @NotNull GameMode getGameMode();

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
    @Nullable String getServerName();

    /**
     * Get the server lobby.
     *
     * @return server lobby.
     */
    @Nullable String getLobbyServer();
}
